package org.daisy.pipeline.tts.sapi.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import org.daisy.pipeline.tts.onecore.NativeSynthesisResult;
import org.daisy.pipeline.tts.onecore.Onecore;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.junit.*;

public class OnecoreTest {

	private final static String osName = System.getProperty("os.name");

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
		SAPIService.ReleaseOnecore();
	}

	@Test
	public void getVoiceNames() throws IOException {
		Voice[] voices = Onecore.getVoices();
		Assert.assertTrue(voices.length > 0);
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
	static NativeSynthesisResult speakCycle(String text) throws IOException {
		Voice[] voices = Onecore.getVoices();
		Assert.assertTrue(voices.length > 0);
		NativeSynthesisResult res;
		res = Onecore.speak(voices[0].getEngine(), voices[0].getName(), text);

		Assert.assertTrue(res.getStreamData().length > 200);
		return res;
	}

	@Test
	public void speakEasy() throws IOException {
		System.out.println(osName);
		speakCycle(SSML("this is a test"));
	}

	@Test
	public void speakALot() throws IOException {
		for(int i = 0; i < 10 ; ++i){
			speakCycle(SSML("this is a test with a longer sentence that i hope should be long enough to create some kind of long audio and could provoque a stack overrun."));
		}
	}

	@Test
	public void speakALotInThreads() throws IOException, InterruptedException {
		Thread[] threads = new Thread[4];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {

					try{
						for(int t = 0; t < 100 ; ++t){
							speakCycle(SSML("this is a test with a longer sentence that i hope should be long enough to create some kind of long audio and could provoque a stack overrun : " + j + " - " + t + "." ));
						}
					} catch (IOException e){

					}
				}
			};
		}
		for (int i = 0; i < threads.length; ++i)
			threads[i].start();
		for (Thread t : threads)
			t.join();
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
		Voice[] voices = Onecore.getVoices();
		Assert.assertTrue(voices.length > 0);

		String text = SSML("small test");
		NativeSynthesisResult res = Onecore.speak(voices[0].getEngine(), voices[0].getName(), text);


		int spoken1 = res.getStreamData().length;
		res = Onecore.speak(voices[0].getEngine(), voices[0].getName(), text);
		int spoken2 =  res.getStreamData().length;
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
		NativeSynthesisResult res = speakCycle(SSML("this is <mark name=\"" + bookmark + "\"/> a bookmark"));

		String[] names = res.getMarksNames();
		long[] pos = res.getMarksPositions();
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
		NativeSynthesisResult res = speakCycle(SSML("this is an ending mark <mark name=\"" + bookmark
		        + "\"/> "));
		String[] names = res.getMarksNames();
		long[] pos = res.getMarksPositions();
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
		NativeSynthesisResult res = speakCycle(SSML("one two three four <mark name=\"" + b1
		                                  + "\"/> five six <mark name=\"" + b2 + "\"/> seven"));
		String[] names = res.getMarksNames();
		long[] pos = res.getMarksPositions();

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
	static private int[] findSize(final String[] sentences, int startShift) throws InterruptedException, IOException {
		final  Voice[] voices = Onecore.getVoices();
		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {

					try{
						NativeSynthesisResult res = Onecore.speak(voices[0].getEngine(), voices[0].getName(), sentences[j]);
						foundSize[j] = res.getStreamData().length;
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
