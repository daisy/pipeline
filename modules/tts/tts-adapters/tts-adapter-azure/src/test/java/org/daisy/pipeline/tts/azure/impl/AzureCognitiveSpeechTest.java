package org.daisy.pipeline.tts.azure.impl;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.Gender;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import org.xml.sax.InputSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public class AzureCognitiveSpeechTest {

	@Before
	public void checkConfig() {
		String prop = "org.daisy.pipeline.tts.azure.key";
		if (System.getProperty(prop) == null) {
			System.out.println("No key provided, please set the " + prop + " property");
		}
		Assume.assumeTrue(System.getProperty(prop) != null);
		prop = "org.daisy.pipeline.tts.azure.region";
		if (System.getProperty(prop) == null) {
			System.out.println("No region provided, please set the " + prop + " property");
		}
		Assume.assumeTrue(System.getProperty(prop) != null);
	}

	private static AzureCognitiveSpeechEngine allocateEngine() throws Throwable {
		Map<String,String> params = new HashMap<>(); {
			params.put("org.daisy.pipeline.tts.azure.key", System.getProperty("org.daisy.pipeline.tts.azure.key"));
			params.put("org.daisy.pipeline.tts.azure.region", System.getProperty("org.daisy.pipeline.tts.azure.region"));
		}
		return new AzureCognitiveSpeechService().newEngine(params);
	}

	private static final Voice testVoice = new Voice("azure", "en-GB-SoniaNeural", Locale.forLanguageTag("en"), Gender.of("female"));

	@Test
	public void speakEasy() throws Throwable {
		AzureCognitiveSpeechEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		AudioInputStream audio = engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">this is a test</s>"),
			testVoice,
			resource).audio;
		engine.releaseThreadResources(resource);
		Assert.assertTrue("the audio should be long enough", getSize(audio) > 50000);
	}

	@Test
	public void speakUnicode() throws Throwable {
		AzureCognitiveSpeechEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		AudioInputStream audio = engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">𝄞𝄞𝄞𝄞 水水水水水 𝄞水𝄞水𝄞水𝄞水 test 国Ø家Ť标准 ĜæŘ ß ŒÞ ๕</s>"),
			testVoice, resource).audio;
		engine.releaseThreadResources(resource);
		Assert.assertTrue("the audio should be long enough", getSize(audio) > 50000);
	}

	/**
	 * Tests concurrent requests and tests different voices.
	 */
	@Test
	public void multiSpeak() throws Throwable {
		AzureCognitiveSpeechEngine engine = allocateEngine();
		Collection<Voice> voices = allocateEngine().getAvailableVoices();
		Assert.assertTrue(voices.size() > 16);
		Iterator<Voice> voicesIterator = voices.iterator();
		Thread[] threads = new Thread[16];
		for (int i = 0; i < threads.length; i++) {
			Voice v = voicesIterator.next();
			threads[i] = new Thread() {
				public void run() {
					try {
						TTSResource resource = null;
						try {
							resource = engine.allocateThreadResources();
							int constantSize = 0;
							for (int j = 0; j < 16; j++) {
								AudioInputStream audio = engine.synthesize(
									parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">small test</s>"),
									v, resource).audio;
								int size = getSize(audio);
								if (j == 0) {
									Assert.assertTrue("the audio should be long enough", size > 50000);
									constantSize = size;
								} else
									Assert.assertEquals("the length of the audio should be the same for identical conversions",
									                    constantSize, size);
							}
						} finally {
							if (resource != null)
								engine.releaseThreadResources(resource);
						}
					} catch (SynthesisException|InterruptedException|SaxonApiException e) {
						throw new RuntimeException(e);
					}
				}
			};
		}
		AtomicReference<Throwable> uncaughtException = new AtomicReference<>();
		for (Thread th : threads)
			th.setUncaughtExceptionHandler((thread, e) -> { uncaughtException.getAndUpdate(ee -> ee != null ? ee : e ); });
		for (Thread th : threads)
			th.start();
		for (Thread th : threads)
			th.join();
		Throwable e = uncaughtException.get();
		if (e != null) throw e;
	}

	@Test
	public void twoBookmarks() throws Throwable {
		AzureCognitiveSpeechEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		String text = " One two three four five six seven eight nine ten. ";
		SynthesisResult result = engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">"
			          + text + text + "<mark name='1'/>"
			          + text + text + "<mark name='2'/></s>"),
			testVoice,
			resource);
		engine.releaseThreadResources(resource);
		int size = getSize(result.audio);
		Assert.assertTrue("the audio should be long enough", size > 50000);
		Assert.assertTrue("bookmarks should be found", result.marks != null);
		Assert.assertEquals("two bookmark should be found", 2, result.marks.size());
		Assert.assertTrue("mark 1 should be around the middle", Math.abs(result.marks.get(1) / 2 - result.marks.get(0)) < 20000);
		Assert.assertTrue("mark 2 should be near the end", size - result.marks.get(1) < 50000);
	}

	private static final Processor proc = new Processor(false);

	private static XdmNode parseSSML(String ssml) throws SaxonApiException {
		return proc.newDocumentBuilder().build(new SAXSource(new InputSource(new StringReader(ssml))));
	}

	/**
	 * Get size in bytes.
	 */
	private static int getSize(AudioInputStream audio) {
		return Math.toIntExact(audio.getFrameLength() * audio.getFormat().getFrameSize());
	}
}
