import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class eval_java {

	public static void main(String[] args) {
		File thisExecutable = new File(args[0]); // path of eval-java(.exe) relative to current directory
		String javaCode = args[1];
		if ("true".equals(System.getenv("ECHO")))
			System.err.println(javaCode);
		try {
			javaCode =
				"import java.io.*;\n"
				+ "import static java.lang.System.err;\n"
				+ "import java.net.*;\n"
				+ "import java.nio.file.*;\n"
				+ "import java.util.*;\n"
				+ "import static lib.util.*;\n\n"
				+ "public class [CLASSNAME] {\n\n"
				+ "public static void main(String args[]) throws Throwable {\n\n"
				+ Pattern.compile(" *\\\\$", Pattern.MULTILINE).matcher(javaCode).replaceAll("") + "\n\n"
				+ "}\n}\n";
			String className = "temp_" + md5(javaCode);
			javaCode = javaCode.replace("[CLASSNAME]", className);
			File javaDir = new File(thisExecutable.getParentFile(), "java");
			File classFile = new File(javaDir, className + ".class");
			if (!classFile.exists()) {
				File javaFile = new File(javaDir, className + ".java");
				try (OutputStream os = new FileOutputStream(javaFile)) {
					os.write(javaCode.getBytes("UTF-8"));
					os.flush();
				}
				String javac = "javac";
				String JAVA_HOME = System.getenv("JAVA_HOME");
				if (JAVA_HOME != null) {
					File f = new File(new File(JAVA_HOME),
					                  System.getProperty("os.name").toLowerCase().startsWith("windows")
					                      ? "bin\\javac.exe"
					                      : "bin/javac");
					if (f.exists())
						javac = f.getAbsolutePath();
				}
				int rv = new ProcessBuilder(javac, "-cp", javaDir.getAbsolutePath(), javaFile.getAbsolutePath()).inheritIO().start().waitFor();
				System.out.flush();
				System.err.flush();
				if (rv != 0)
					System.exit(rv);
			}
			Class.forName(className).getDeclaredMethod("main", String[].class).invoke(null, (Object)new String[0]);
		} catch (Throwable e) {
			e.printStackTrace();
			System.err.flush();
			System.exit(1);
		}
	}

	public static String md5(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] bytes = MessageDigest.getInstance("MD5").digest(data.getBytes("UTF-8"));
		StringBuilder s = new StringBuilder();
		for (int i = 0; i < bytes.length; i++)
			s.append(Integer
			         .toString((bytes[i] & 0xff) + 0x100, 16)
			         .substring(1));
		return s.toString();
	}
}
