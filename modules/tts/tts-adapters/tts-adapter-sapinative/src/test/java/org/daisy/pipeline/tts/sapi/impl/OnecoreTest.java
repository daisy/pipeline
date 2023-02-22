package org.daisy.pipeline.tts.sapi.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.daisy.pipeline.tts.onecore.Onecore;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.junit.*;

public class OnecoreTest {

	/**
	 * Initialization of the Onecore runtime
	 * @throws SynthesisException if the loading and initialization of Onecore fails
	 */
	@BeforeClass
	public static void load() throws SynthesisException {
		SAPIService.loadAndInitializeOnecore();
	}

	@AfterClass
	public static void dispose() {
		System.out.println("WARNING notice: you may encountered an exception when the dll is released by the JVM after finishing tests, with an error log generated in the project folder.");
		System.out.println("This exception does not prevent the tests and build to complete in success, and has not yet occured in real-world production tests.");
		System.out.println("You can ignore it and the error log.");
		SAPIService.ReleaseOnecore();
	}

	@Test
	public void getVoiceNames() {
		String[] voices = Onecore.getVoiceNames();
		Assert.assertTrue(voices.length > 0);
	}

	@Test
	public void getVoiceVendors() {
		String[] vendors = Onecore.getVoiceVendors();
		Assert.assertTrue(vendors.length > 0);
	}

	@Test
	public void getVoiceLocales() {
		String[] locales = Onecore.getVoiceLocales();
		Assert.assertTrue(locales.length > 0);
	}

	@Test
	public void getVoiceGenders() {
		String[] genders = Onecore.getVoiceGenders();
		Assert.assertTrue(genders.length > 0);
	}

	@Test
	public void getVoiceAges() {
		String[] ages = Onecore.getVoiceAges();
		Assert.assertTrue(ages.length > 0);
	}

	/**
	 * Test to connect with the TTS API
	 */
	@Test
	public void manageConnection() {
		long connection = Onecore.openConnection();
		Assert.assertNotSame(0, connection);
		Onecore.closeConnection(connection);
	}

	/**
	 * Test if voices can be recovered with the TTS engine
	 * @throws Throwable
	 */
	@Test
	public void getVoiceInfo() throws Throwable {
		Collection<Voice> voices = allocateEngine().getAvailableVoices();
		Assert.assertTrue(voices.size() > 1);
	}

	/**
	 * Create a simple "ready-to-speak" ssml sentence
	 * @param x text to speak
	 * @return a "speak" ssml markup string
	 */
	static String SSML(String x) {
		return "<speak xmlns='http://www.w3.org/2001/10/synthesis' version='1.0'><s>" + x + "</s></speak>";
	}



	/**
	 * Send a text to the TTS API and assert that
	 * - Voices are correctly retrieved
	 * - A connection to the native library can be opened
	 * - A non-empty audio stream has been produced
	 * @param text the text to speak
	 * @return the pointer of the connection opened with the TTS API
	 */
	static long speakCycle(String text) throws IOException {
		String[] names = Onecore.getVoiceNames();
		String[] vendors = Onecore.getVoiceVendors();
		String[] locales = Onecore.getVoiceLocales();
		Assert.assertTrue(names.length > 0);
		Assert.assertTrue(vendors.length > 0);
		long connection = Onecore.openConnection();
		Assert.assertNotSame(0, connection);
		int error = Onecore.speak(connection, vendors[0], names[0], text);
		int spoken = -1;
		if (error == 0) {
			spoken = Onecore.getStreamSize(connection);
			if (spoken > 0) {
				int offset = 5000;
				byte[] audio = new byte[offset + spoken];
				Onecore.readStream(connection, audio, offset);
			}
			if (spoken <= 200) {
				error = -1;
			}
		}
		if (error != 0)
			Onecore.closeConnection(connection);
		Assert.assertSame(0, error);
		return connection;
	}

	@Test
	public void speakEasy() throws IOException {
		long connection = speakCycle(SSML("this is a test"));
		Onecore.closeConnection(connection);
	}

	/**
	 * Allocate an TTS engine
	 * @return
	 * @throws Throwable
	 */
	private static SAPIEngine allocateEngine() throws Throwable {
		SAPIService s = new SAPIService();
		return (SAPIEngine)s.newEngine(new HashMap<String, String>());
	}

	/**
	 * Test continuous speaking on a connection with 2 sentences
	 */
	@Test
	public void speakTwice() throws IOException {
		String[] names = Onecore.getVoiceNames();
		String[] vendors = Onecore.getVoiceVendors();
		Assert.assertTrue(names.length > 0);
		Assert.assertTrue(vendors.length > 0);
		long connection = Onecore.openConnection();
		Assert.assertNotSame(0, connection);
		String text = SSML("small test");
		int error1 = Onecore.speak(connection, vendors[0], names[0], text);
		int spoken1 = Onecore.getStreamSize(connection);
		if (spoken1 > 0) {
			Onecore.readStream(connection, new byte[spoken1], 0); //skip data
		}
		int error2 = Onecore.speak(connection, vendors[0], names[0], text);
		int spoken2 = Onecore.getStreamSize(connection);
		Onecore.closeConnection(connection);
		Assert.assertSame(0, error1);
		Assert.assertSame(0, error2);
		Assert.assertTrue(spoken1 >= 200);
		Assert.assertTrue(spoken2 >= 200);
		Assert.assertEquals(spoken1, spoken2);
	}


	/**
	 * Test to retrieve the name and position of a mark
	 */
	@Test
	public void oneBookmark() throws IOException {
		String bookmark = "bmark";
		long connection = speakCycle(SSML("this is <mark name=\"" + bookmark + "\"/> a bookmark"));
		String[] names = Onecore.getBookmarkNames(connection);
		long[] pos = Onecore.getBookmarkPositions(connection);
		Onecore.closeConnection(connection);
		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
		Assert.assertEquals(bookmark, names[0]);
	}

	/**
	 * Test to retrieve ending marks names and position.
	 */
	@Test
	public void endingBookmark() throws IOException {
		String bookmark = "endingmark";
		long connection = speakCycle(SSML("this is an ending mark <mark name=\"" + bookmark
		        + "\"/> "));
		String[] names = Onecore.getBookmarkNames(connection);
		long[] pos = Onecore.getBookmarkPositions(connection);
		Onecore.closeConnection(connection);
		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
		Assert.assertEquals(bookmark, names[0]);
	}

	/**
	 * Test to retrieve the name and position of 2 marks in the text
	 */
	@Test
	public void twoBookmarks() throws IOException {
		String b1 = "bmark1";
		String b2 = "bmark2";
		long connection = speakCycle(SSML("one two three four <mark name=\"" + b1
		                                  + "\"/> five six <mark name=\"" + b2 + "\"/> seven"));
		String[] names = Onecore.getBookmarkNames(connection);
		long[] pos = Onecore.getBookmarkPositions(connection);
		Onecore.closeConnection(connection);
		Assert.assertSame(2, names.length);
		Assert.assertSame(2, pos.length);
		Assert.assertEquals(b1, names[0]);
		Assert.assertEquals(b2, names[1]);
		long diff = pos[1] - pos[0];
		Assert.assertTrue(diff > 200);
		Assert.assertTrue(pos[0] > diff);
	}

	/**
	 * Speak multiple sentences and returns the generated stream size for each one.
	 * @param sentences array of sentences to be asynchronously spoken
	 * @param startShift the index of the sentence to start speaking from
	 * @return an array of the stream size for each sentence spoken
	 * @throws InterruptedException
	 */
	static private int[] findSize(final String[] sentences, int startShift) throws InterruptedException {
		final String[] names = Onecore.getVoiceNames();
		final String[] vendors = Onecore.getVoiceVendors();
		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					long connection = Onecore.openConnection();
					try{
						Onecore.speak(connection, vendors[0], names[0], sentences[j]);
						foundSize[j] = Onecore.getStreamSize(connection);
					} catch (IOException e){

					}
					Onecore.closeConnection(connection);
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

	/**
	 * Testing sentences speaking with multiple thread
	 * @throws InterruptedException
	 */
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
