package org.daisy.pipeline.tts.acapela.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.junit.TestRunner;
import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.xml.sax.InputSource;

/**
 * Before running those tests, you must start an Acapela server and have
 * libscube.so in your library path. Run mvn test with -Djna.nosys=true and the
 * adequate profile (probably -Ptester).
 */
@RunWith(TestRunner.class)
public class AcapelaTest {
	TTSEngine tts;

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

	@Inject
	public TTSService service;
	private Voice voice;

	@Before
	public void setUp() throws Throwable {
		tts = service.newEngine(Collections.emptyMap());
		voice = tts.getAvailableVoices().iterator().next();
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
	public void speakEasy() throws SynthesisException, InterruptedException, IOException, SaxonApiException {
		TTSResource r = tts.allocateThreadResources();
		int size = getSize(tts.synthesize(parseSSML(format("this is a test")), voice, r).audio);
		tts.releaseThreadResources(r);
		Assert.assertTrue("audio output must be big enough", size > 2000);
	}

	@Test
	public void oneBookmark() throws SynthesisException, InterruptedException, IOException, SaxonApiException {
		String bookmark = "bmark";
		TTSResource r = tts.allocateThreadResources();
		String text = "A piece of text long enough.";
		SynthesisResult result = tts.synthesize(
			parseSSML(format(text + "<mark name=\"" + bookmark + "\"></mark>" + text)),
			voice,
			r);
		int size = getSize(result.audio);
		tts.releaseThreadResources(r);
		Assert.assertTrue("audio output must be big enough", size > 2000);
		Assert.assertEquals("one bookmark should be found", 1, result.marks.size());
		Assert.assertTrue("the mark is around the middle", Math.abs(size / 2 - result.marks.get(0)) < 5000);
	}

	@Test
	public void twoBookmarks() throws SynthesisException, InterruptedException, IOException, SaxonApiException {
		TTSResource r = tts.allocateThreadResources();
		String bmark1 = "1";
		String bmark2 = "2";
		SynthesisResult result = tts.synthesize(
			parseSSML(format("one two three four five six <mark name=\"" + bmark1 + "\"/> seven <mark name=\"" + bmark2 + "\"/>")),
			voice,
			r);
		int size = getSize(result.audio);
		tts.releaseThreadResources(r);
		Assert.assertTrue("audio output must be big enough", size > 200);
		Assert.assertEquals("2 booksmarks should be found", 2, result.marks.size());
		Assert.assertTrue("marks' offset should be realistic",
		                  result.marks.get(1) - result.marks.get(0) < result.marks.get(0));
	}

	private int[] findSize(final String[] sentences, int startShift) throws InterruptedException, SaxonApiException {
		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					TTSResource r;
					try {
						r = tts.allocateThreadResources();
						foundSize[j] = getSize(tts.synthesize(parseSSML(format("this is a test")), voice, r).audio) / 4;
						tts.releaseThreadResources(r);
					} catch (SynthesisException | InterruptedException | SaxonApiException e) {
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
	public void multithreadedSpeak() throws InterruptedException, SaxonApiException {
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
	public void multiVoices() throws SynthesisException, InterruptedException, IOException, SaxonApiException {
		Collection<Voice> voices = tts.getAvailableVoices();
		Assert.assertTrue(voices.size() > 0);
		Set<Integer> sizes = new HashSet<Integer>();
		TTSResource r = tts.allocateThreadResources();
		for (Voice v : voices) {
			int size = getSize(tts.synthesize(parseSSML(format("this is a test", v.getName())), v, r).audio);
			sizes.add(size / 4);
		}
		tts.releaseThreadResources(r);
		float diversity = Float.valueOf(sizes.size()) / voices.size();
		Assert.assertTrue(diversity >= 0.6);
	}

	@Test
	public void accents() throws SynthesisException, IOException, InterruptedException, SaxonApiException {
		String withAccents = "à Noël, la tèrre est bèlle vûe du cièl";
		String withoutAccents = "a Noel, la terre est belle vue du ciel";
		//When the accents are not properly handled, the processor pronounces the accents separately.
		//As a result, the output audio buffer will be longer than expected. 
		TTSResource r = tts.allocateThreadResources();
		int size1 = getSize(
			tts.synthesize(parseSSML(format("<s xml:lang=\"fr\">" + withAccents + "</s>")), voice, r).audio);
		tts.releaseThreadResources(r);
		r = tts.allocateThreadResources();
		int size2 = getSize(
			tts.synthesize(parseSSML(format("<s xml:lang=\"fr\">" + withoutAccents + "</s>")),
			               voice, r).audio);
		tts.releaseThreadResources(r);
		Assert.assertTrue(size1 > 2000);
		Assert.assertTrue(Math.abs(size1 - size2) < 2000);
	}

	@Test
	public void utf8chars() throws SynthesisException, InterruptedException, IOException, SaxonApiException {
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
			SynthesisResult result = tts.synthesize(parseSSML(format(begin + c + end, "alice")), voice, r);
			int size = getSize(result.audio);
			tts.releaseThreadResources(r);
			Assert.assertTrue(1 == result.marks.size());
			if (refSize == null) {
				refSize = new Integer(size);
			}
			Assert.assertTrue(2 * refSize / 3 - size < 0);
		}
	}

	private static final Processor proc = new Processor(false);

	private static XdmNode parseSSML(String ssml) throws SaxonApiException {
		return proc.newDocumentBuilder().build(new SAXSource(new InputSource(new StringReader(ssml))));
	}
}
