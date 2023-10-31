package org.daisy.pipeline.tts.google.impl;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import org.daisy.pipeline.junit.OSGiLessRunner;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.Gender;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.xml.sax.InputSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

// We run with OSGiLessRunner in order to start the Google TTS server mock
@RunWith(OSGiLessRunner.class)
public class GoogleTTSTest {
	
	@Before
	public void checkForAPIKey() {
		if(System.getProperty("org.daisy.pipeline.tts.google.apikey") == null) {
			System.out.println("No API key provided for tests, please add one with the property org.daisy.pipeline.tts.google.apikey");
		}
		Assume.assumeTrue(System.getProperty("org.daisy.pipeline.tts.google.apikey") != null);
	}

	private static final Voice DefaultVoice
		= System.getProperty("org.daisy.pipeline.tts.google.address") != null
			// assume we're using MockGoogle
			? new Voice("google", "foo", Locale.forLanguageTag("en"), Gender.of("male"))
			// will default to "en-GB-Standard-A"
			: null;

	private static int getSize(AudioInputStream audio) {
		return Math.toIntExact(
			audio.getFrameLength() * audio.getFormat().getFrameSize());
	}

	/**
	 * Default engine allocator for tests.
	 * Developpers should pass their own API key as system property either with maven option 
	 * -Dorg.daisy.pipeline.tts.google.apikey=their_key
	 * or as either a property in the pom or a configuration file (for global pipeline build)
	 * @return
	 * @throws Throwable
	 */
	private static GoogleRestTTSEngine allocateEngine() throws Throwable {
		GoogleTTSService s = new GoogleTTSService();
		Map<String, String> params = new HashMap<>();
		params.put("org.daisy.pipeline.tts.google.address", System.getProperty("org.daisy.pipeline.tts.google.address"));
		params.put("org.daisy.pipeline.tts.google.apikey", System.getProperty("org.daisy.pipeline.tts.google.apikey"));
		return (GoogleRestTTSEngine) s.newEngine(params);
	}
	
	@Test
	public void convertToIntWithGivenParams() throws Throwable {
		Map<String, String> params = new HashMap<>();
		params.put("org.daisy.pipeline.tts.google.apikey", System.getProperty("org.daisy.pipeline.tts.google.apikey"));
		params.put("org.daisy.pipeline.tts.google.samplerate", "24000");
		GoogleTTSService s = new GoogleTTSService();
		s.newEngine(params);
	}
	
	@Test(expected=SynthesisException.class)
	public void convertToIntWithNotValidParams() throws Throwable {
		Map<String, String> params = new HashMap<>();
		params.put("org.daisy.pipeline.tts.google.apikey", System.getProperty("org.daisy.pipeline.tts.google.apikey"));
		params.put("org.daisy.pipeline.tts.google.samplerate", "240s0T0");
		GoogleTTSService s = new GoogleTTSService();
		s.newEngine(params);
	}

	@Test
	public void getVoiceInfo() throws Throwable {
		Collection<Voice> voices = allocateEngine().getAvailableVoices();
		if (System.getProperty("org.daisy.pipeline.tts.google.address") != null)
			// assume we're using MockGoogle
			Assert.assertEquals(2, voices.size()); // mock-tts has 2 voices with locale ≠ * and gender
		else
			Assert.assertTrue(voices.size() > 5);
	}

	@Test
	public void speakEasy() throws Throwable {
		GoogleRestTTSEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		AudioInputStream audio = engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">this is a test</s>"),
			DefaultVoice, resource).audio;
		engine.releaseThreadResources(resource);

		Assert.assertTrue(getSize(audio) > 2000);
	}
	
	@Test
	public void speakWithVoices() throws Throwable {
		GoogleRestTTSEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		Set<Integer> sizes = new HashSet<Integer>();
		int totalVoices = 0;
		Iterator<Voice> ite = engine.getAvailableVoices().iterator();
		while (ite.hasNext()) {
			Voice v = ite.next();
			AudioInputStream audio = engine.synthesize(
				parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">small test</s>"),
				v, resource).audio;

			sizes.add(getSize(audio) / 4); //div 4 helps being more robust to tiny differences
			totalVoices++;
		}
		engine.releaseThreadResources(resource);

		// this number will be very low if the voice names are not properly retrieved
		float diversity = Float.valueOf(sizes.size()) / totalVoices;
		Assert.assertTrue(diversity > 0.4);
	}

	@Test
	public void speakUnicode() throws Throwable {
		GoogleRestTTSEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		AudioInputStream audio = engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">𝄞𝄞𝄞𝄞 水水水水水 𝄞水𝄞水𝄞水𝄞水 test 国Ø家Ť标准 ĜæŘ ß ŒÞ ๕</s>"),
			DefaultVoice, resource).audio;
		engine.releaseThreadResources(resource);
		Assert.assertTrue(getSize(audio) > 2000);
	}

	@Test
	public void multiSpeak() throws Throwable {
		final GoogleRestTTSEngine engine = allocateEngine();
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
								DefaultVoice, resource).audio;
						} catch (SaxonApiException | SynthesisException | InterruptedException e) {
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
	
	@Test(expected=SynthesisException.class)
	public void tooBigSentence() throws Throwable {
		String sentence = "";
		for (int i = 0 ; i < 5001; i++) {
			sentence = sentence + 'a';
		}
		GoogleRestTTSEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">" + sentence + "</s>"),
			DefaultVoice, resource);
		engine.releaseThreadResources(resource);
	}
	
	@Test
	public void adaptedSentence() throws Throwable {
		String sentence = "I can pause <break time=\"3s\"/>.";
		GoogleRestTTSEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		engine.synthesize(
			parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">" + sentence + "</s>"),
			DefaultVoice, resource);
		engine.releaseThreadResources(resource);
	}

	private static final Processor proc = new Processor(false);

	private static XdmNode parseSSML(String ssml) throws SaxonApiException {
		return proc.newDocumentBuilder().build(new SAXSource(new InputSource(new StringReader(ssml))));
	}
}
