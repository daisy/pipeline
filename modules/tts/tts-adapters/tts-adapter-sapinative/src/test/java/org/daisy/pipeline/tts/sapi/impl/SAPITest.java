package org.daisy.pipeline.tts.sapi.impl;

import org.daisy.pipeline.tts.onecore.NativeSynthesisResult;
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

import javax.sound.sampled.AudioFormat;

public class SAPITest {

	private static final int sampleRate = 22050;
	private static final short bytesPerSample = 2;

	private static AudioFormat sapiAudioFormat = new AudioFormat(sampleRate, 8 * bytesPerSample, 1, true, false);

	@BeforeClass
	public static void load() throws SynthesisException {
		SAPIService.loadAndInitializeSAPI(sampleRate, bytesPerSample);
	}

	@AfterClass
	public static void dispose() {
		SAPIService.ReleaseSAPI();
	}

	static String SSML(String x) {
		return "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\"><s>" + x
		        + "</s></speak>";
	}

	static NativeSynthesisResult speakCycle(String text) throws IOException {
		Voice[] voices = SAPI.getVoices();

		Assert.assertTrue(voices.length > 0);

		return SAPI.speak(voices[0].getEngine(), voices[0].getName(), text, (int)sapiAudioFormat.getSampleRate(), (short)sapiAudioFormat.getSampleSizeInBits());

	}


	@Test
	public void getVoices() throws IOException {
		Voice[] voices = SAPI.getVoices();
		Assert.assertTrue(voices.length > 0);
	}

	@Test
	public void speakEasy() throws IOException{
		speakCycle(SSML("this is a test"));
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
		Voice[] voices = SAPI.getVoices();

		Assert.assertTrue(voices.length > 0);

		String text = SSML("small test");
		NativeSynthesisResult spoken1 = SAPI.speak(voices[0].getEngine(), voices[0].getName(), text,  (int)sapiAudioFormat.getSampleRate(), (short)sapiAudioFormat.getSampleSizeInBits());
		NativeSynthesisResult spoken2 = SAPI.speak(voices[0].getEngine(), voices[0].getName(), text,  (int)sapiAudioFormat.getSampleRate(), (short)sapiAudioFormat.getSampleSizeInBits());
		Assert.assertTrue(spoken1.getStreamData().length >= 200);
		Assert.assertTrue(spoken1.getStreamData().length >= 200);
		Assert.assertEquals(spoken1.getStreamData().length, spoken1.getStreamData().length);
	}

	@Test
	public void bookmarkReply() throws IOException {
		NativeSynthesisResult res = speakCycle(SSML("this is <mark name=\"t\"/> a bookmark"));
		Assert.assertSame(1, res.getMarksNames().length);
		Assert.assertSame(1, res.getMarksPositions().length);
	}

	@Test
	public void oneBookmark() throws IOException {
		String bookmark = "bmark";
		NativeSynthesisResult res =  speakCycle(SSML("this is <mark name=\"" + bookmark + "\"/> a bookmark"));
		String[] names = res.getMarksNames();
		long[] pos = res.getMarksPositions();
		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
		Assert.assertEquals(bookmark, names[0]);
	}

	 @Test
	public void endingBookmark() throws IOException {
		String bookmark = "endingmark";
		 NativeSynthesisResult res =  speakCycle(SSML("this is an ending mark <mark name=\"" + bookmark + "\"/> "));
		 String[] names = res.getMarksNames();
		 long[] pos = res.getMarksPositions();
		Assert.assertSame(1, names.length);
		Assert.assertSame(1, pos.length);
		Assert.assertEquals(bookmark, names[0]);
	}

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

	static private int[] findSize(final String[] sentences, int startShift) throws InterruptedException, IOException {
		Voice[] voices = SAPI.getVoices();
		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					try{
						NativeSynthesisResult res = SAPI.speak(voices[0].getEngine(), voices[0].getName(), sentences[j],  (int)sapiAudioFormat.getSampleRate(), (short)sapiAudioFormat.getSampleSizeInBits());
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
