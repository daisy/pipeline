package lib;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.xml.sax.InputSource;

public class util {
	private util() {}

	public static void println(Object o) {
		System.out.println(o);
	}

	public static void exit(int exitValue) {
		System.exit(exitValue);
	}

	public static void exit(boolean succeeded) {
		exit(succeeded ? 0 : 1);
	}

	public static void exitOnError(int exitValue) {
		if (exitValue != 0)
			exit(exitValue);
	}

	public static void exitOnError(boolean succeeded) {
		if (!succeeded)
			exit(succeeded);
	}

	public enum OS {
		WINDOWS,
		MACOSX,
		REDHAT,
		DEBIAN,
		LINUX
	}

	public static OS getOS() {
		String name = System.getProperty("os.name").toLowerCase();
		if (name.startsWith("windows"))
			return OS.WINDOWS;
		else if (name.startsWith("mac os x"))
			return OS.MACOSX;
		else if (new File("/etc/redhat-release").isFile())
			return OS.REDHAT;
		else if (name.startsWith("linux"))
			return OS.LINUX;
		else
			throw new RuntimeException("Unsupported OS: " + name);
	}

	public static void mkdirs(String directory) {
		mkdirs(new File(directory));
	}

	public static void mkdirs(File directory) {
		directory.mkdirs();
	}

	public static void copy(URL url, File file) throws FileNotFoundException, IOException {
		try (InputStream is = new BufferedInputStream(url.openStream());
		     OutputStream os = new FileOutputStream(file)) {
			byte data[] = new byte[1024];
			int read;
			while ((read = is.read(data)) != -1)
				os.write(data, 0, read);
		}
	}

	public static void write(File file, String string) throws IOException {
		try (OutputStream os = new FileOutputStream(file, true)) {
			os.write(string.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("coding error", e);
		}
	}

	public static void unzip(File zipFile, File directory) throws IOException {
		directory.mkdirs();
		try (ZipInputStream zip = new ZipInputStream(new FileInputStream(zipFile))) {
			ZipEntry entry;
			while ((entry = zip.getNextEntry()) != null) {
				File destFile = new File(directory, entry.getName());
				if (!entry.isDirectory()) {
					try (BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(destFile))) {
						byte[] bytes = new byte[1024];
						int read = 0;
						while ((read = zip.read(bytes)) != -1)
							os.write(bytes, 0, read);
					}
				} else
					destFile.mkdirs();
				zip.closeEntry();
			}
		}
	}

	public static void exec(File cd, String... cmd) throws IOException, InterruptedException {
		int rv = new ProcessBuilder(cmd).directory(cd)
		                                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
		                                .redirectError(ProcessBuilder.Redirect.INHERIT)
		                                .start()
		                                .waitFor();
		System.out.flush();
		System.err.flush();
		System.exit(rv);
	}

	public static void exec(String... cmd) throws IOException, InterruptedException {
		int rv = new ProcessBuilder(cmd).redirectOutput(ProcessBuilder.Redirect.INHERIT)
		                                .redirectError(ProcessBuilder.Redirect.INHERIT)
		                                .start()
		                                .waitFor();
		System.out.flush();
		System.err.flush();
		System.exit(rv);
	}

	public static void exec(List<String> cmd) throws IOException, InterruptedException {
		int rv = new ProcessBuilder(cmd).redirectOutput(ProcessBuilder.Redirect.INHERIT)
		                                .redirectError(ProcessBuilder.Redirect.INHERIT)
		                                .start()
		                                .waitFor();
		System.out.flush();
		System.err.flush();
		System.exit(rv);
	}

	public static int captureOutput(List<String> cmd, Consumer<String> collect) throws IOException, InterruptedException {
		Process p = new ProcessBuilder(cmd).redirectError(ProcessBuilder.Redirect.INHERIT).start();
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null)
			if (collect != null)
				collect.accept(line);
		int rv = p.waitFor();
		System.err.flush();
		return rv;
	}

	public static List<String> runInShell(String... cmd) throws IOException {
		List<String> newCmd = new ArrayList<>();
		for (String s : cmd)
			newCmd.add(s);
		return runInShell(newCmd);
	}

	public static List<String> runInShell(List<String> cmd) throws IOException {
		if (getOS() == OS.WINDOWS) {
			cmd = new ArrayList<>(cmd);
			for (int i = 0; i < cmd.size(); i++)
				if (Pattern.compile("[ \"]").matcher(cmd.get(i)).find())
					cmd.set(i, quote(cmd.get(i)));
			List<String> newCmd = new ArrayList<>();
			newCmd.add("cmd.exe");
			newCmd.add("/s");
			newCmd.add("/c");
			newCmd.add("\"" + String.join(" ", cmd) + "\"");
			return newCmd;
		} else {
			return cmd;
		}
	}

	private static String quote(String s) {
		return "\"" + s.replace("\"", "\\\"") + "\"";
	}

	public static String xpath(File file, String expression) throws FileNotFoundException, XPathExpressionException {
		return XPathFactory.newInstance().newXPath().compile(expression)
			.evaluate(new InputSource(new FileInputStream(file)));
	}
}
