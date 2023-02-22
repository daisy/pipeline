package org.daisy.pipeline.tts.sapi.impl;

import org.daisy.pipeline.tts.onecore.Onecore;
import org.daisy.pipeline.tts.onecore.SAPI;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class SAPITest {

	@BeforeClass
	public static void load() throws SynthesisException {
		SAPIService.loadAndInitializeSAPI(22050, 2);
	}

	@AfterClass
	public static void dispose() {
		System.out.println("WARNING notice: you may encountered an exception when the dll is released by the JVM after finishing tests, with an error log generated in the project folder.");
		System.out.println("This exception does not prevent the tests and build to complete in success, and has not yet occured in real-world production tests.");
		System.out.println("You can ignore it and the error log.");

		SAPIService.ReleaseSAPI();
	}

	static String SSML(String x) {
		return "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\"><s>" + x
		        + "</s></speak>";
	}

	static long speakCycle(String text) throws IOException {
		String[] names = SAPI.getVoiceNames();
		String[] vendors = SAPI.getVoiceVendors();
		String[] locales = SAPI.getVoiceLocales();
		Assert.assertTrue(names.length > 0);
		Assert.assertTrue(vendors.length > 0);
		long connection = SAPI.openConnection();
		Assert.assertNotSame(0, connection);
		int error = SAPI.speak(connection, vendors[0], names[0], text);
		int spoken = -1;
		if (error == 0) {
			spoken = SAPI.getStreamSize(connection);
			if (spoken > 0) {
				int offset = 5000;
				byte[] audio = new byte[offset + spoken];
				SAPI.readStream(connection, audio, offset);
			}
			if (spoken <= 200) {
				error = -1;
			}
		}
		if (error != 0)
			SAPI.closeConnection(connection);
		Assert.assertSame(0, error);
		return connection;
	}

	@Test
	public void getVoiceNames() {
		String[] voices = SAPI.getVoiceNames();
		Assert.assertTrue(voices.length > 0);
	}

	@Test
	public void getVoiceVendors() {
		String[] vendors = SAPI.getVoiceVendors();
		Assert.assertTrue(vendors.length > 0);
	}

	@Test
	public void getVoiceLocales() {
		String[] locales = SAPI.getVoiceLocales();
		Assert.assertTrue(locales.length > 0);
	}

	@Test
	public void getVoiceGenders() {
		String[] genders = SAPI.getVoiceGenders();
		Assert.assertTrue(genders.length > 0);
	}

	@Test
	public void getVoiceAges() {
		String[] ages = SAPI.getVoiceAges();
		Assert.assertTrue(ages.length > 0);
	}

	@Test
	public void manageConnection() throws IOException {
		long connection = SAPI.openConnection();
		Assert.assertNotSame(0, connection);
		SAPI.closeConnection(connection);
	}
	@Test
	public void speakEasy() throws IOException{
		long connection = speakCycle(SSML("this is a test"));
		SAPI.closeConnection(connection);
	}

	private static SAPIEngine allocateEngine() throws Throwable {
		SAPIService s = new SAPIService();
		return (SAPIEngine) s.newEngine(new HashMap<String, String>());
	}

	@Test
	public void getVoiceInfo() throws Throwable {
		Collection<Voice> voices = allocateEngine().getAvailableVoices();
		Assert.assertTrue(voices.size() > 1);
	}

	@Test
	public void speakTwice() throws IOException {
		String[] names = SAPI.getVoiceNames();
		String[] vendors = SAPI.getVoiceVendors();
		Assert.assertTrue(names.length > 0);
		Assert.assertTrue(vendors.length > 0);
		long connection = SAPI.openConnection();
		Assert.assertNotSame(0, connection);
		String text = SSML("small test");
		int error1 = SAPI.speak(connection, vendors[0], names[0], text);
		int spoken1 = SAPI.getStreamSize(connection);
		if (spoken1 > 0) {
			SAPI.readStream(connection, new byte[spoken1], 0); //skip data
		}
		int error2 = SAPI.speak(connection, vendors[0], names[0], text);
		int spoken2 = SAPI.getStreamSize(connection);
		SAPI.closeConnection(connection);
		Assert.assertSame(0, error1);
		Assert.assertSame(0, error2);
		Assert.assertTrue(spoken1 >= 200);
		Assert.assertTrue(spoken2 >= 200);
		Assert.assertEquals(spoken1, spoken2);
	}

	@Test
	public void bookmarkReply() throws IOException {
		long connection = speakCycle(SSML("this is <mark name=\"t\"/> a bookmark"));
		String[] names = SAPI.getBookmarkNames(connection);
		long[] pos = SAPI.getBookmarkPositions(connection);
		SAPI.closeConnection(connection);
		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
	}

	@Test
	public void oneBookmark() throws IOException {
		String bookmark = "bmark";
		long connection = speakCycle(SSML("this is <mark name=\"" + bookmark + "\"/> a bookmark"));
		String[] names = SAPI.getBookmarkNames(connection);
		long[] pos = SAPI.getBookmarkPositions(connection);
		SAPI.closeConnection(connection);
		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
		Assert.assertEquals(bookmark, names[0]);
	}

	 @Test
	public void endingBookmark() throws IOException {
		String bookmark = "endingmark";
		long connection = speakCycle(SSML("this is an ending mark <mark name=\"" + bookmark + "\"/> "));
		String[] names = SAPI.getBookmarkNames(connection);
		long[] pos = SAPI.getBookmarkPositions(connection);
		SAPI.closeConnection(connection);
		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
		Assert.assertEquals(bookmark, names[0]);
	}

	@Test
	public void twoBookmarks() throws IOException {
		String b1 = "bmark1";
		String b2 = "bmark2";
		long connection = speakCycle(SSML("one two three four <mark name=\"" + b1
		                                  + "\"/> five six <mark name=\"" + b2 + "\"/> seven"));
		String[] names = SAPI.getBookmarkNames(connection);
		long[] pos = SAPI.getBookmarkPositions(connection);
		SAPI.closeConnection(connection);
		Assert.assertSame(2, names.length);
		Assert.assertSame(2, pos.length);
		Assert.assertEquals(b1, names[0]);
		Assert.assertEquals(b2, names[1]);
		long diff = pos[1] - pos[0];
		Assert.assertTrue(diff > 200);
		Assert.assertTrue(pos[0] > diff);
	}

	static private int[] findSize(final String[] sentences, int startShift) throws InterruptedException, IOException {
		final String[] names = SAPI.getVoiceNames();
		final String[] vendors = SAPI.getVoiceVendors();
		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					try{
						long connection = SAPI.openConnection();
						SAPI.speak(connection, vendors[0], names[0], sentences[j]);
						foundSize[j] = SAPI.getStreamSize(connection);
						SAPI.closeConnection(connection);
					} catch (IOException e){

					}

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
	public void multithreadedSpeak() throws InterruptedException, IOException {
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
