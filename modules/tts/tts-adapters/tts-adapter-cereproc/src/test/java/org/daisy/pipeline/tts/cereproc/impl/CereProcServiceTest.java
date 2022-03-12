package org.daisy.pipeline.tts.cereproc.impl;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import com.google.common.collect.ImmutableSet;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.Voice;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

public class CereProcServiceTest extends AbstractTest {

	@Inject
	public TTSService ttsService;

	private static final Map<String,String> params = new HashMap<>();
	static {
		params.put("org.daisy.pipeline.tts.cereproc.server", System.getProperty("org.daisy.pipeline.tts.cereproc.server"));
		params.put("org.daisy.pipeline.tts.cereproc.port", System.getProperty("org.daisy.pipeline.tts.cereproc.port"));
		params.put("org.daisy.pipeline.tts.cereproc.dnn.port", System.getProperty("org.daisy.pipeline.tts.cereproc.dnn.port"));
	}

	@Test
	public void testAvailableVoices() throws Throwable {
		TTSEngine engine = ttsService.newEngine(params);
		Collection<Voice> voices = engine.getAvailableVoices();
		Assert.assertEquals(2, voices.size());
		Assert.assertEquals(ImmutableSet.of("William", "Ylva"),
		                    voices.stream().map(v -> v.name).collect(Collectors.toSet()));
	}

	@Test
	public void testSpeak() throws Throwable {
		TTSEngine engine = ttsService.newEngine(params);
		Assert.assertTrue("cereproc".equals(ttsService.getName())
		                  || "cereproc-dnn".equals(ttsService.getName()));
		TTSResource resource = engine.allocateThreadResources();
		try {
			AudioInputStream audio = engine.synthesize(
				parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">Hi, my name is William</s>"),
				new Voice(null, "William"),
				resource
			).audio;
			Assert.assertTrue(audio.getFrameLength() * audio.getFormat().getFrameSize() > 10000);
		} finally {
			engine.releaseThreadResources(resource);
		}
	}

	private static final Processor proc = new Processor(false);

	private static XdmNode parseSSML(String ssml) throws SaxonApiException {
		return proc.newDocumentBuilder().build(new SAXSource(new InputSource(new StringReader(ssml))));
	}
}
