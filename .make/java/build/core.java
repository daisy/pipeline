package build;

import java.io.*;
import java.util.*;

import static lib.util.*;

public class core {
	private core() {}

	public static void groupEval() throws Exception {
		String MY_DIR = System.getenv("MY_DIR");
		String ANSI_YELLOW_BOLD = "\033[1;33m";
		String ANSI_RED_BOLD = "\033[1;31m";
		String ANSI_RESET = "\u001B[0m";
		LinkedList<List<String>> commands = new LinkedList<>(); {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = in.readLine()) != null) {
				LinkedList<String> cmd = new LinkedList<>();
				exitOnError(
					captureOutput(cmd::add,
					              "bash", "-c", "eval 'printf \"%s\\n\" " + line.replace("'", "'\"'\"'") + "'"));
				if (cmd.size() > 0) {
					String c = cmd.get(0);
					List<String> appendTo = null;
					if (c.startsWith(MY_DIR + "/mvn-release.sh ")
					    && (appendTo = commands.stream().filter(x -> x.get(0).equals(c)).findFirst().orElse(null)) != null)
						;
					else if (c.startsWith(MY_DIR + "/")
					           && !commands.isEmpty()
					           && commands.peekLast().get(0).equals(c))
						appendTo = commands.peekLast();
					if (appendTo != null) {
						cmd.poll();
						appendTo.addAll(cmd);
					} else
						commands.add(cmd);
				}
			}
		}
		println("-------------- " + ANSI_YELLOW_BOLD + "Build order" + ANSI_RESET + ": -------------");
		for (List<String> cmd : commands) {
			if (cmd.size() > 2)
				println(String.join(" \\\n   ", cmd));
			else
				println(String.join(" ", cmd));
		}
		println("-------------- " + ANSI_YELLOW_BOLD + "Environment" + ANSI_RESET + ": -------------");
		Map<String,String> oldEnv = new HashMap<>(); {
			File envFile = new File(System.getenv("TARGET_DIR") + "/env");
			try (BufferedReader f = new BufferedReader(new FileReader(envFile))) {
				String line;
				while ((line = f.readLine()) != null) {
					if ("".equals(line)) {
					} else if (line.contains("=")) {
						int i = line.indexOf("=");
						oldEnv.put(line.substring(0, i), line.substring(i + 1));
					} else {
						System.err.println("Unexpected line: " + line);
						exit(1);
					}
				}
			} catch (IOException e) {
				System.err.println("Could not open " + envFile);
				exit(1);
			}
		}
		Map<String,String> env = System.getenv();
		for (String k : new TreeSet<>(env.keySet())) {
			if (!"_".equals(k)) {
				String v = env.get(k);
				if (!v.equals(oldEnv.get(k)))
					println("export " + k + "=\"" + v + "\"");
			}
		}
		println("-----------------------------------------");
		for (List<String> cmd : commands) {
			System.out.print("--> " + ANSI_YELLOW_BOLD);
			if (cmd.size() > 2)
				System.out.print(String.join(" \\\n   ", cmd));
			else
				System.out.print(String.join(" ", cmd));
			println(ANSI_RESET);
			int rv = captureOutput(System.out::println, "bash", "-c", String.join(" ", cmd));
			if (rv != 0) {
				System.err.print(ANSI_RED_BOLD);
				System.err.println("\ncommand exited with value " + rv);
				System.err.print(ANSI_RESET);
				exit(rv);
			}
		}
	}
}
