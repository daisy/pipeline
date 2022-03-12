package org.daisy.pipeline.tts.sapinative.impl;

import org.daisy.pipeline.tts.sapinative.SAPILib;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.Voice;


import java.util.Collection;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SAPINativeTest {

	@BeforeClass
	public static void load() throws SynthesisException {
		SAPIservice.loadDLL();
		SAPILib.initialize(8000, (short) 16);
	}

	@AfterClass
	public static void dispose() {
		System.out.println("WARNING notice: you may encountered an exception when the dll is released by the JVM after finishing tests, with an error log generated in the project folder.");
		System.out.println("This exception does not prevent the tests and build to complete in success, and has not yet occured in real-world production tests.");
		System.out.println("You can ignore it and the error log.");

		SAPILib.dispose();
	}

	static String SSML(String x) {
		return "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\"><s>" + x
		        + "</s></speak>";
	}

	static long speakCycle(String text) {
		String[] names = SAPILib.getVoiceNames();
		String[] vendors = SAPILib.getVoiceVendors();
		String[] locales = SAPILib.getVoiceLocales();
		Assert.assertTrue(names.length > 0);
		Assert.assertTrue(vendors.length > 0);

		long connection = SAPILib.openConnection();
		Assert.assertNotSame(0, connection);

		int error = SAPILib.speak(connection, vendors[0], names[0], text);

		int spoken = -1;
		if (error == 0) {
			spoken = SAPILib.getStreamSize(connection);
			if (spoken > 0) {
				int offset = 5000;
				byte[] audio = new byte[offset + spoken];
				SAPILib.readStream(connection, audio, offset);
			}
			if (spoken <= 200) {
				error = -1;
			}
		}

		if (error != 0)
			SAPILib.closeConnection(connection);

		Assert.assertSame(0, error);

		return connection;
	}

	@Test
	public void getVoiceNames() {
		String[] voices = SAPILib.getVoiceNames();
		Assert.assertTrue(voices.length > 0);
	}

	@Test
	public void getVoiceVendors() {
		String[] vendors = SAPILib.getVoiceVendors();
		Assert.assertTrue(vendors.length > 0);
	}

	@Test
	public void getVoiceLocales() {
		String[] locales = SAPILib.getVoiceLocales();
		Assert.assertTrue(locales.length > 0);
	}

	@Test
	public void getVoiceGenders() {
		String[] genders = SAPILib.getVoiceGenders();
		Assert.assertTrue(genders.length > 0);
	}

	@Test
	public void getVoiceAges() {
		String[] ages = SAPILib.getVoiceAges();
		Assert.assertTrue(ages.length > 0);
	}

	@Test
	public void manageConnection() {
		long connection = SAPILib.openConnection();
		Assert.assertNotSame(0, connection);
		SAPILib.closeConnection(connection);
	}

	@Test
	public void speakEasy() {
		long connection = speakCycle(SSML("this is a test"));
		SAPILib.closeConnection(connection);
	}

	private static SAPIengine allocateEngine() throws Throwable {
		SAPIservice s = new SAPIservice();
		return (SAPIengine) s.newEngine(new HashMap<String, String>());
	}

	@Test
	public void getVoiceInfo() throws Throwable {
		Collection<Voice> voices = allocateEngine().getAvailableVoices();
		Assert.assertTrue(voices.size() > 1);
	}

	@Test
	public void speakTwice() {
		String[] names = SAPILib.getVoiceNames();
		String[] vendors = SAPILib.getVoiceVendors();
		Assert.assertTrue(names.length > 0);
		Assert.assertTrue(vendors.length > 0);

		long connection = SAPILib.openConnection();
		Assert.assertNotSame(0, connection);

		String text = SSML("small test");

		int error1 = SAPILib.speak(connection, vendors[0], names[0], text);
		int spoken1 = SAPILib.getStreamSize(connection);
		if (spoken1 > 0) {
			SAPILib.readStream(connection, new byte[spoken1], 0); //skip data
		}

		int error2 = SAPILib.speak(connection, vendors[0], names[0], text);
		int spoken2 = SAPILib.getStreamSize(connection);

		SAPILib.closeConnection(connection);

		Assert.assertSame(0, error1);
		Assert.assertSame(0, error2);
		Assert.assertTrue(spoken1 >= 200);
		Assert.assertTrue(spoken2 >= 200);
		Assert.assertEquals(spoken1, spoken2);
	}

	@Test
	public void bookmarkReply() {
		long connection = speakCycle(SSML("this is <bookmark mark=\"t\"/> a bookmark"));
		String[] names = SAPILib.getBookmarkNames(connection);
		long[] pos = SAPILib.getBookmarkPositions(connection);
		SAPILib.closeConnection(connection);

		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
	}

	@Test
	public void oneBookmark() {
		String bookmark = "bmark";
		long connection = speakCycle(SSML("this is <bookmark mark=\"" + bookmark
		        + "\"/> a bookmark"));
		String[] names = SAPILib.getBookmarkNames(connection);
		long[] pos = SAPILib.getBookmarkPositions(connection);
		SAPILib.closeConnection(connection);

		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
		Assert.assertEquals(bookmark, names[0]);
	}

	@Test
	public void twoBookmarks() {
		String b1 = "bmark1";
		String b2 = "bmark2";
		long connection = speakCycle(SSML("one two three four <bookmark mark=\"" + b1
		        + "\"/> five six <bookmark mark=\"" + b2 + "\"/> seven"));
		String[] names = SAPILib.getBookmarkNames(connection);
		long[] pos = SAPILib.getBookmarkPositions(connection);
		SAPILib.closeConnection(connection);

		Assert.assertSame(2, names.length);
		Assert.assertSame(2, pos.length);
		Assert.assertEquals(b1, names[0]);
		Assert.assertEquals(b2, names[1]);
		long diff = pos[1] - pos[0];

		Assert.assertTrue(diff > 200);
		Assert.assertTrue(pos[0] > diff);
	}

	static private int[] findSize(final String[] sentences, int startShift)
	        throws InterruptedException {
		final String[] names = SAPILib.getVoiceNames();
		final String[] vendors = SAPILib.getVoiceVendors();
		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];

		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					long connection = SAPILib.openConnection();
					SAPILib.speak(connection, vendors[0], names[0], sentences[j]);
					foundSize[j] = SAPILib.getStreamSize(connection);
					SAPILib.closeConnection(connection);
				}
			};
		}

		for (int i = startShift; i < threads.length; ++i)
			threads[i].start();
		for (int i = 0; i < startShift; ++i)
			threads[i].start();

		for (Thread t : threads)
			t.join();

		return foundSize;
	}

	@Test
	public void multithreadedSpeak() throws InterruptedException {
		final String[] sentences = new String[]{
		        SSML("short"), SSML("regular size"), SSML("a bit longer size"),
		        SSML("very much longer sentence")
		};

		int[] size1 = findSize(sentences, 0);
		int[] size2 = findSize(sentences, 2);
		for (int i = 0; i < sentences.length; ++i) {
			Assert.assertNotSame(0, size1[i]);
		}
		Assert.assertArrayEquals(size1, size2);
	}
}
