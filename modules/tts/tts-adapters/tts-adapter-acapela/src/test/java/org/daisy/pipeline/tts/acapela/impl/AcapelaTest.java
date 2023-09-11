package org.daisy.pipeline.tts.acapela.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Before running those tests, you must start an Acapela server and have
 * libscube.so in your library path. Run mvn test with -Djna.nosys=true and the
 * adequate profile (probably -Ptester).
 */
public class AcapelaTest {
	AcapelaEngine tts;

	private String format(String str) {
		return str;
	}

	private String format(String str, String speakerName) {
		return "\\voice{" + speakerName + "}" + str;
	}

	private static int getSize(AudioInputStream audio) {
		return Math.toIntExact(
			audio.getFrameLength() * audio.getFormat().getFrameSize());
	}

	@Before
	public void setUp() throws SynthesisException, InterruptedException {
		tts = new AcapelaEngine(new AcapelaService(),
		        new AudioFormat(22050, 16, 1, true, true), new RoundRobinLoadBalancer(
		                "localhost:0", this), 300, 3, 10);
	}

	@Test
	public void simpleConnection() throws SynthesisException, InterruptedException {
		tts.releaseThreadResources(tts.allocateThreadResources());
	}

	@Test
	public void getVoiceNames() throws SynthesisException, InterruptedException, IOException {
		Collection<Voice> voices = tts.getAvailableVoices();
		Assert.assertTrue("some voices must be found", voices.size() > 0);
	}

	@Test
	public void speakEasy() throws SynthesisException, InterruptedException, IOException {
		TTSResource r = tts.allocateThreadResources();

		int size = getSize(tts.speak(format("this is a test"), r, null));
		tts.releaseThreadResources(r);

		Assert.assertTrue("audio output must be big enough", size > 2000);
	}

	@Test
	public void oneBookmark() throws SynthesisException, InterruptedException, IOException {
		String bookmark = "bmark";
		TTSResource r = tts.allocateThreadResources();

		List<Integer> marks = new ArrayList<>();

		String text = "A piece of text long enough.";
		int size = getSize(tts.speak(format(text + "<mark name=\"" + bookmark + "\"></mark>"
		        + text), r, marks));
		tts.releaseThreadResources(r);

		Assert.assertTrue("audio output must be big enough", size > 2000);
		Assert.assertEquals("one bookmark should be found", 1, marks.size());
		Assert.assertTrue("the mark is around the middle", Math.abs(size / 2 - marks.get(0)) < 5000);
	}

	@Test
	public void twoBookmarks() throws SynthesisException, InterruptedException, IOException {
		TTSResource r = tts.allocateThreadResources();
		String bmark1 = "1";
		String bmark2 = "2";
		List<Integer> marks = new ArrayList<>();
		
		int size = getSize(tts.speak(format("one two three four five six <mark name=\""
		        + bmark1 + "\"/> seven <mark name=\"" + bmark2 + "\"/>"), r, marks));
		tts.releaseThreadResources(r);

		Assert.assertTrue("audio output must be big enough", size > 200);
		Assert.assertEquals("2 booksmarks should be found", 2, marks.size());
		Assert.assertTrue("marks' offset should be realistic",
					marks.get(1) - marks.get(0) < marks.get(0));
	}

	private int[] findSize(final String[] sentences, int startShift)
	        throws InterruptedException {

		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];

		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					TTSResource r;
					try {
						r = tts.allocateThreadResources();
						foundSize[j] = getSize(tts.speak(format("this is a test"), r, null)) / 4;
						tts.releaseThreadResources(r);
					} catch (SynthesisException | InterruptedException | IOException e) {
						return;
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
	public void multithreadedSpeak() throws InterruptedException {
		final String[] sentences = new String[]{
		        format("short"), format("regular size"), format("a bit longer size"),
		        format("very much longer sentence")
		};

		int[] size1 = findSize(sentences, 0);
		int[] size2 = findSize(sentences, 2);
		for (int i = 0; i < sentences.length; ++i) {
			Assert.assertTrue(0 != size1[i]);
		}

		Assert.assertArrayEquals(size1, size2);
	}

	@Test
	public void multiVoices() throws SynthesisException, InterruptedException, IOException {
		Collection<Voice> voices = tts.getAvailableVoices();
		Assert.assertTrue(voices.size() > 0);

		Set<Integer> sizes = new HashSet<Integer>();

		TTSResource r = tts.allocateThreadResources();
		for (Voice v : voices) {
			int size = getSize(tts.speak(format("this is a test", v.getName()), r, null));
			sizes.add(size / 4);
		}
		tts.releaseThreadResources(r);

		float diversity = Float.valueOf(sizes.size()) / voices.size();
		Assert.assertTrue(diversity >= 0.6);
	}

	@Test
	public void workingVoice() throws SynthesisException {
		String v = tts.findWorkingVoice(null);
		Assert.assertNotNull(v);
	}

	@Test
	public void accents() throws SynthesisException, IOException, InterruptedException {
		String withAccents = "à Noël, la tèrre est bèlle vûe du cièl";
		String withoutAccents = "a Noel, la terre est belle vue du ciel";

		//When the accents are not properly handled, the processor pronounces the accents separately.
		//As a result, the output audio buffer will be longer than expected. 

		TTSResource r = tts.allocateThreadResources();
		int size1 = getSize(tts.speak(format("<s xml:lang=\"fr\">" + withAccents + "</s>"), r,
		        null));
		tts.releaseThreadResources(r);

		r = tts.allocateThreadResources();
		int size2 = getSize(tts.speak(format("<s xml:lang=\"fr\">" + withoutAccents + "</s>"),
		        r, null));
		tts.releaseThreadResources(r);

		Assert.assertTrue(size1 > 2000);
		Assert.assertTrue(Math.abs(size1 - size2) < 2000);
	}

	@Test
	public void utf8chars() throws SynthesisException, InterruptedException, IOException {
		List<Character> chars = new ArrayList<Character>();
		chars.add("a".charAt(0)); //for the reference test

		//get a list of 'dangerous' characters
		InputStream is = getClass().getResourceAsStream("/decimal_chars.txt");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		while (true) {
			try {
				String line = br.readLine();
				if (line == null)
					break;
				int i = Integer.parseInt(line);
				chars.add((char) i);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

		}

		//test every character individually
		String begin = "<s>begin ";
		String end = " this is the end of the sentence long enough for tests<mark name=\"end\"></s>";
		Integer refSize = null;

		for (Character c : chars) {
			TTSResource r = tts.allocateThreadResources();
			List<Integer> marks = new ArrayList<>();

			int size = getSize(tts.speak(format(begin + c + end, "alice"), r, marks));
			tts.releaseThreadResources(r);

			Assert.assertTrue(1 == marks.size());

			if (refSize == null) {
				refSize = new Integer(size);
			}

			Assert.assertTrue(2 * refSize / 3 - size < 0);
		}

	}
}
