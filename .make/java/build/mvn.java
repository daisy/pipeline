package build;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;

import static lib.util.*;

public class mvn {
	private mvn() {}

	/**
	 * GAV coordinates
	 */
	public static class Coords {

		public final String g;
		public final String a;
		public final String v;

		public Coords(String group, String artifact, String version) {
			g = group;
			a = artifact;
			v = version;
		}
	}

	private static final String MVN_SETTINGS = System.getenv("MVN_SETTINGS");
	private static final String MVN_PROPERTIES = System.getenv("MVN_PROPERTIES");
	private static final String MVN_LOG = System.getenv("MVN_LOG");
	private static final String MY_DIR = System.getenv("MY_DIR");
	private static final File ROOT_DIR = new File(System.getenv("ROOT_DIR"));
	private static final File EVAL_JAVA = new File(ROOT_DIR, System.getenv("SHELL"));

	public static void install(String... dirs) throws IOException, InterruptedException {
		for (String dir : dirs)
			mvn(new File(ROOT_DIR, dir),
			    "clean", "install", "-Dmaven.test.skip", "-Dinvoker.skip=true");
	}

	public static void installPom(String... dirs) throws IOException, InterruptedException {
		for (String dir : dirs)
			mvn(new File(ROOT_DIR, dir),
			    "org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file",
			    "-DpomFile=pom.xml",
			    "-Dfile=./pom.xml");
	}

	public static void installDoc(String... dirs) throws IOException, InterruptedException {
		for (String dir : dirs)
			mvn(new File(ROOT_DIR, dir),
			    "clean", "install", "-Dmaven.test.skip=true", "-Ddocumentation", "-Ddocumentation-only");
	}

	public static void test(String... dirs) throws IOException, InterruptedException {
		for (String dir : dirs)
			mvn(new File(ROOT_DIR, dir),
			    "clean", "verify");
	}

	public static void releaseDir(String dir) throws IOException, InterruptedException {
		exec("bash", "-c", String.format("%s/mvn-release.sh %s", MY_DIR, dir));
	}

	public static Function<String[],Void> releaseModulesInDir(String dir) {
		return modules -> {
			try {
				exec("bash", "-c", String.format("%s/mvn-release.sh %s %s", MY_DIR,
				                                                            dir,
				                                                            String.join(" ", modules)));
				return null;
			} catch (IOException|InterruptedException e) {
				throw new RuntimeException(e);
			}
		};
	}

	public static void eclipse(String dir) throws IOException, InterruptedException {
		exec("bash", "-c", String.format("%s/mvn-eclipse.sh %s", MY_DIR, dir));
	}

	static void mvn(File cd, String... args) throws IOException, InterruptedException {
		File settings = MVN_SETTINGS != null
			? new File(ROOT_DIR, MVN_SETTINGS)
			: null;
		List<String> properties = null; {
			if (MVN_PROPERTIES != null) {
				Pattern property = Pattern.compile("(?:[^\"\\s]|\"(\\\\\"|[^\"])*\")+");
				Matcher m = property.matcher(MVN_PROPERTIES.trim());
				while (m.find()) {
					if (properties == null) properties = new ArrayList<String>();
					properties.add(unquote(m.group()));
				}
			}
		}
		Process logProcess = null;
		CompletionService<Void> logOutputCompletion = null;
		ExecutorService logOutputExecutor = null;
		PrintStream logStream = null;
		File logFile = null; {
			if (MVN_LOG != null) {
				Pattern redirectCall = Pattern.compile(
					"^build\\.redirectTo\\s*\\(\\s*\"((\\\\\"|[^\"])*)\"\\s*\\)\\s*;$");
				Matcher m = redirectCall.matcher(MVN_LOG.trim());
				if (m.matches()) {
					logFile = new File(m.group(1));
					logStream = new PrintStream(new FileOutputStream(logFile));
				} else {
					Process p = new ProcessBuilder(EVAL_JAVA.getPath(), MVN_LOG).start();
					logProcess = p;
					logStream = new PrintStream(logProcess.getOutputStream());
					logOutputExecutor = Executors.newFixedThreadPool(2);
					logOutputCompletion =
						new ExecutorCompletionService<Void>(logOutputExecutor);
					logOutputCompletion.submit(() -> { copy(p.getInputStream(), System.out); return null; });
					logOutputCompletion.submit(() -> { copy(p.getErrorStream(), System.err); return null; });
				}
			} else
				logStream = System.out;
		}
		List<String> cmd = new ArrayList<>();
		cmd.add("mvn");
		cmd.add("--batch-mode");
		if (settings != null) {
			cmd.add("--settings");
			cmd.add(settings.getPath());
		}
		if (properties != null)
			cmd.addAll(properties);
		for (String a : args)
			cmd.add(a);
		int rv = captureOutput(logStream::println, cd, cmd);
		logStream.flush();
		logStream.close();
		if (logProcess != null) {
			try {
				for (int done = 0; done < 2; done++)
					logOutputCompletion.take().get();
			} catch (ExecutionException e) {
				Throwable t = e;
				while (t instanceof ExecutionException) t = ((ExecutionException)t).getCause();
				if (t instanceof IOException)
					// can be thrown by copy()
					throw (IOException)t;
				else if (t instanceof RuntimeException)
					throw (RuntimeException)t;
				else
					// should not happen
					throw new RuntimeException(t);
			} finally {
				logOutputExecutor.shutdownNow();
			}
			logProcess.waitFor();
		}
		if (rv != 0) {
			logStream.println("Command was: " + String.join(" ", cmd));
			System.err.print(String.format("Maven exited with value %d.", rv));
			if (logFile != null)
				System.err.println(String.format(" See %s for more info.", logFile));
			else
				System.err.println();
			exit(rv);
		}
	}

	// remove one level of quotes
	private static String unquote(String quoted) {
		StringBuilder unquoted = new StringBuilder();
		Character startQuote = null;
		boolean escape = false; // previous character was backslash
		for (int i = 0; i < quoted.length(); i++) {
			char c = quoted.charAt(i);
			if (escape) {
				escape = false;
				unquoted.append(c);
			} else if (startQuote != null) {
				if (c == startQuote)
					startQuote = null;
				else
					unquoted.append(c);
			} else if (c == '"' || c == '\'')
				startQuote = c;
			else if (c == '\\')
				escape = true;
			else
				unquoted.append(c);
		}
		if (escape)
			throw new IllegalArgumentException();
		if (startQuote != null)
			throw new IllegalArgumentException();
		return unquoted.toString();
	}
}
