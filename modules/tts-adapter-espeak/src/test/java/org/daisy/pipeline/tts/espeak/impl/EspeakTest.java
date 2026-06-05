package org.daisy.pipeline.tts.espeak.impl;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import org.xml.sax.InputSource;

public class EspeakTest {

	static private int getSize(AudioInputStream audio) {
		return Math.toIntExact(
			audio.getFrameLength() * audio.getFormat().getFrameSize());
	}

	private static ESpeakEngine allocateEngine() throws Throwable {
		Assume.assumeTrue("Test can not be run because espeak not present",
		                  BinaryFinder.find("espeak").isPresent());
		ESpeakService s = new ESpeakService();
		s.activate();
		return (ESpeakEngine) s.newEngine(new HashMap<String, String>());
	}

	@Test
	public void getVoiceInfo() throws Throwable {
		Collection<Voice> voices = allocateEngine().getAvailableVoices();
		Assert.assertTrue(voices.size() > 5);
	}

	@Test
	public void speakEasy() throws Throwable {
		ESpeakEngine engine = allocateEngine();

		TTSResource resource = engine.allocateThreadResources();
		AudioInputStream audio = engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">this is a test</s>"),
			null, resource).audio;
		engine.releaseThreadResources(resource);

		Assert.assertTrue(getSize(audio) > 2000);
	}

	@Test
	public void speakWithVoices() throws Throwable {
		ESpeakEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();

		Set<Integer> sizes = new HashSet<Integer>();
		int totalVoices = 0;
		Iterator<Voice> ite = engine.getAvailableVoices().iterator();
		while (ite.hasNext()) {
			Voice v = ite.next();
			AudioInputStream audio = engine.synthesize(
				parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">"
				          + "<voice name=\"" + v.getName() + "\">small test</voice></s>"),
				null, resource).audio;

			sizes.add(getSize(audio) / 4); // div 4 helps being more robust to tiny differences
			totalVoices++;
		}
		engine.releaseThreadResources(resource);

		//this number will be very low if the voice names are not properly retrieved
		float diversity = Float.valueOf(sizes.size()) / totalVoices;

		Assert.assertTrue(diversity > 0.2);
	}

	@Test
	public void speakUnicode() throws Throwable {
		ESpeakEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		AudioInputStream audio = engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">"
			          + "𝄞𝄞𝄞𝄞 水水水水水 𝄞水𝄞水𝄞水𝄞水 test 国Ø家Ť标准 ĜæŘ ß ŒÞ ๕</s>"),
			null, resource).audio;
		engine.releaseThreadResources(resource);

		Assert.assertTrue(getSize(audio) > 2000);
	}

	@Test
	public void multiSpeak() throws Throwable {
		final ESpeakEngine engine = allocateEngine();

		final int[] sizes = new int[16];
		Thread[] threads = new Thread[sizes.length];
		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					TTSResource resource = null;
					try {
						resource = engine.allocateThreadResources();
					} catch (SynthesisException | InterruptedException e) {
						return;
					}

					AudioInputStream audio = null;
					for (int k = 0; k < 16; ++k) {
						try {
							audio = engine.synthesize(
								parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">small test</s>"),
								null, resource).audio;
						} catch (SynthesisException | InterruptedException | SaxonApiException e) {
							e.printStackTrace();
							break;
						}
						sizes[j] += getSize(audio);
					}
					try {
						engine.releaseThreadResources(resource);
					} catch (SynthesisException | InterruptedException e) {
					}
				}
			};
		}

		for (Thread th : threads)
			th.start();

		for (Thread th : threads)
			th.join();

		for (int size : sizes) {
			Assert.assertEquals(sizes[0], size);
		}
	}

	private static final Processor proc = new Processor(false);

	private static XdmNode parseSSML(String ssml) throws SaxonApiException {
		return proc.newDocumentBuilder().build(new SAXSource(new InputSource(new StringReader(ssml))));
	}
}
