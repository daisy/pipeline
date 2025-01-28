package build;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import static lib.util.*;

public class core {
	private core() {}

	public static void groupEval() throws Exception {
		String EVAL_JAVA = System.getenv("SHELL");
		String ANSI_YELLOW_BOLD = "\033[1;33m";
		String ANSI_RED_BOLD = "\033[1;31m";
		String ANSI_RESET = "\u001B[0m";
		LinkedList<Object> commands = new LinkedList<>(); {
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while ((line = in.readLine()) != null) {
				line = line.trim();
				Pattern methodCall = Pattern.compile(
					"^(mvn\\.releaseModulesInDir\\([^)]+\\))\\s*\\.apply\\(\\s*((?:[^\\s\"]|\"(\\\\\"|[^\"])*\")+\\s*(,\\s*(?:[^\\s\"]|\"(\\\\\"|[^\"])*\")+\\s*)*)\\)\\s*;$");
				Matcher m = methodCall.matcher(line);
				if (m.matches()) {
					String func = m.group(1);
					String arguments = m.group(2).trim();
					CombinableCommand cmd = new CombinableCommand(func, arguments);
					CombinableCommand appendTo = (CombinableCommand)
						commands.stream()
						        .filter(x -> x instanceof CombinableCommand && ((CombinableCommand)x).isCombinableWith(cmd))
						        .findFirst()
						        .orElse(null);
					if (appendTo != null)
						appendTo.append(cmd);
					else
						commands.add(cmd);
				} else
					commands.add(line);
			}
		}
		println("-------------- " + ANSI_YELLOW_BOLD + "Build order" + ANSI_RESET + ": -------------");
		for (Object cmd : commands)
			println(cmd);
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
		for (Object cmd : commands) {
			System.out.println("--> " + ANSI_YELLOW_BOLD + cmd + ANSI_RESET);
			int rv = captureOutput(System.out::println, EVAL_JAVA, cmd.toString());
			if (rv != 0) {
				System.err.print(ANSI_RED_BOLD);
				System.err.println("\ncommand exited with value " + rv);
				System.err.print(ANSI_RESET);
				exit(rv);
			}
		}
	}

	private static class CombinableCommand {

		final String func;
		private String[] args;

		CombinableCommand(String func, String... args) {
			this.func = func;
			this.args = args;
		}

		boolean isCombinableWith(Object o) {
			return o instanceof CombinableCommand && func.equals(((CombinableCommand)o).func);
		}

		void append(CombinableCommand o) {
			if (!isCombinableWith(o))
			    throw new IllegalArgumentException();
			String[] combinedArgs = new String[args.length + o.args.length];
			System.arraycopy(args, 0, combinedArgs, 0, args.length);
			System.arraycopy(o.args, 0, combinedArgs, args.length, o.args.length) ;
			args = combinedArgs;
		}

		@Override
		public String toString() {
			return String.format("%s.apply(%s);", func, args.length > 1
				? (" \\\n   " + String.join(", \\\n   ", args))
				: String.join(", ", args));
		}
	}
}
