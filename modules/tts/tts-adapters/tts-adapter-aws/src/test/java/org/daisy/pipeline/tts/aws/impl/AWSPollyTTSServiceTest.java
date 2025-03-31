package org.daisy.pipeline.tts.aws.impl;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.Voice;

import org.xml.sax.InputSource;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

public class AWSPollyTTSServiceTest {

	@Before
	public void checkConfig() {
		String prop = "org.daisy.pipeline.tts.aws.accesskey";
		if (System.getProperty(prop) == null) {
			System.out.println("No access key provided, please set the " + prop + " property");
		}
		Assume.assumeTrue(System.getProperty(prop) != null);
		prop = "org.daisy.pipeline.tts.aws.secretkey";
		if (System.getProperty(prop) == null) {
			System.out.println("No secret key provided, please set the " + prop + " property");
		}
		Assume.assumeTrue(System.getProperty(prop) != null);
		prop = "org.daisy.pipeline.tts.aws.region";
		if (System.getProperty(prop) == null) {
			System.out.println("No region provided, please set the " + prop + " property");
		}
		Assume.assumeTrue(System.getProperty(prop) != null);
	}

	private static AWSPollyTTSEngine allocateEngine() throws Throwable {
		Map<String,String> params = new HashMap<>(); {
			params.put("org.daisy.pipeline.tts.aws.accesskey", System.getProperty("org.daisy.pipeline.tts.aws.accesskey"));
			params.put("org.daisy.pipeline.tts.aws.secretkey", System.getProperty("org.daisy.pipeline.tts.aws.secretkey"));
			params.put("org.daisy.pipeline.tts.aws.region", System.getProperty("org.daisy.pipeline.tts.aws.region"));
		}
		return new AWSPollyTTSService().newEngine(params);
	}

	private static int getSize(AudioInputStream audio) {
		return Math.toIntExact(audio.getFrameLength() * audio.getFormat().getFrameSize());
	}

	private static final Processor proc = new Processor(false);
	private static XdmNode parseSSML(String ssml) throws SaxonApiException {
		return proc.newDocumentBuilder().build(new SAXSource(new InputSource(new StringReader(ssml))));
	}

	@Test
	public void testAvailableVoices() throws Throwable {
		TTSEngine engine = allocateEngine();
		Collection<Voice> voices = engine.getAvailableVoices();
		Assert.assertTrue(voices.size() > 0);
	}

	@Test
	public void testSpeak() throws Throwable {
		TTSEngine engine = allocateEngine();
		TTSResource resource = engine.allocateThreadResources();
		try {
			TTSEngine.SynthesisResult res = engine.synthesize(
					// con este texto falla. Es lo que le llega desde la aplicación para probar el engine y está fallando.
//					"<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s xmlns:tmp=\"http://\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\" >small sentence</s:s><ssml:break time=\"250ms\"/></ssml:speak>",
					// con esto funciona. Parece que no le gustan los namespaces.
				parseSSML("<ssml:speak xmlns:ssml=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s xmlns=\"http://www.w3.org/2001/10/synthesis\">García, que de humor iba justito cuando era el inocente, entró una vez a un trapo que no ayudó a la fluidez de su relación tempestuosa con Gil.</s><ssml:break time=\"250ms\"/></ssml:speak>"),
				new Voice("polly", "Lucia"), resource
			);

			AudioFormat format = res.audio.getFormat();
			Assert.assertTrue(getSize(res.audio) > 10000);
		} finally {
			engine.releaseThreadResources(resource);
		}
	}
}
