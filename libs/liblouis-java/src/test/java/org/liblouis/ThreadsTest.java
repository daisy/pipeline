package org.liblouis;

import java.io.File;
import java.util.Random;

import org.junit.Test;

import static org.liblouis.Louis.asFile;

public class ThreadsTest {
	
	// If liblouis-java is thread-safe, the test should finish without causing crashes.
	@Test
	public void testMultiThreaded() throws Exception {
		final Translator translator1 = new Translator(new File(tablesDir, "foobar.cti").getCanonicalPath());
		final Translator translator2 = new Translator(new File(tablesDir, "foobar.cti").getCanonicalPath() + "," +
		                                              new File(tablesDir, "foobar.dic").getCanonicalPath());
		final Random r1 = new Random();
		final Random r2 = new Random();
		final String alphabet = "abcdefghijklmnopqrstuvwxyz.   ";
		Thread t1 = new Thread() {
			public void run() {
				for (int i = 0; i < 10000; i++) {
					try {
						translator1.translate(generateString(r1, alphabet, r2.nextInt(100)), null, null, null); }
					catch (TranslationException e) {
						throw new RuntimeException(e); }
					catch (DisplayException e) {
						throw new RuntimeException(e); }}}};
		Thread t2 = new Thread() {
			public void run() {
				for (int i = 0; i < 10000; i++) {
					try {
						translator2.translate(generateString(r1, alphabet, r2.nextInt(100)), null, null, null); }
					catch (TranslationException e) {
						throw new RuntimeException(e); }
					catch (DisplayException e) {
						throw new RuntimeException(e); }}}};
		t1.start();
		t2.start();
		t1.join();
		t2.join();
	}
	
	// http://stackoverflow.com/questions/2863852/how-to-generate-a-random-string-in-java#2863888
	private static String generateString(Random rng, String characters, int length) {
		char[] text = new char[length];
		for (int i = 0; i < length; i++)
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		return new String(text);
	}
	
	private final File tablesDir;

	public ThreadsTest() {
		File testRootDir = asFile(this.getClass().getResource("/"));
		tablesDir = new File(testRootDir, "tables");
	}
}
