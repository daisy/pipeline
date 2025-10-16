package build;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.regex.*;

import static lib.util.*;

public class gradle {
	private gradle() {}

	private static final File ROOT_DIR = new File(System.getenv("ROOT_DIR"));
	private static final String MY_DIR = System.getenv("MY_DIR");
	private static final String MVN_PROPERTIES = System.getenv("MVN_PROPERTIES");

	public static void install(String... dirs) throws IOException, InterruptedException {
		for (String dir : dirs) {
			File d = new File(ROOT_DIR, dir);
			if (egrep("publishing \\{", new File(d, "build.gradle")).count() > 0)
				gradlew(d, "publishToMavenLocal");
			else
				gradlew(d, "install");
		}
	}

	public static void test(String... dirs) throws IOException, InterruptedException {
		for (String dir : dirs)
			gradlew(new File(ROOT_DIR, dir), "test");
	}

	public static void release(String dir) throws IOException, InterruptedException {
		exec("bash", "-c", String.format("%s/gradle-release.sh %s", MY_DIR, dir));
	}

	static void gradlew(File cd, String... args) throws IOException, InterruptedException {
		String gradlewFileName = "gradlew";
		if (getOS() == OS.WINDOWS)
			gradlewFileName += ".bat";
		File gradlew = cd != null ? new File(cd, gradlewFileName) : null;
		if (gradlew == null || !gradlew.exists())
			gradlew = new File(ROOT_DIR , MY_DIR + "/" + gradlewFileName);
		List<String> properties = null; {
			if (MVN_PROPERTIES != null) {
				Pattern property = Pattern.compile("(?:[^\"\\s]|\"(\\\\\"|[^\"])*\")+");
				Matcher m = property.matcher(MVN_PROPERTIES.trim());
				while (m.find()) {
					if (properties == null) properties = new ArrayList<String>();
					properties.add(m.group());
				}
			}
		}
		List<String> cmd = new ArrayList<>();
		cmd.add(gradlew.getPath());
		if (properties != null)
			cmd.addAll(properties);
		for (String a : args)
			cmd.add(a);
		try {
			// FIXME: This system property is set to false by default in eval-java.exe, in order to
			// work around a bug in ProcessBuilder on Windows. However, for some reason, the
			// property needs to be set to true for gradlew commands to work. Find out why.
			System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "true");
			exitOnError(
				captureOutput(System.out::println, cd, cmd));
		} finally {
			System.setProperty("jdk.lang.Process.allowAmbiguousCommands", "false");
		}
	}
}
