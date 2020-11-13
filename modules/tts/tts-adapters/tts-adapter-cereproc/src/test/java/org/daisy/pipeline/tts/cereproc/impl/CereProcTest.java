package org.daisy.pipeline.tts.cereproc.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.tts.StraightBufferAllocator;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.Voice;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class CereProcTest extends AbstractTest {

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
			Collection<AudioBuffer> audio = engine.synthesize("<s>Hi, my name is William</s>",
			                                                  null, // xml
			                                                  new Voice(null, "William"),
			                                                  resource,
			                                                  null, // marks,
			                                                  null, // expectedMarks
			                                                  new StraightBufferAllocator(),
			                                                  false // retry
			                                                  );
			Assert.assertTrue(audio.stream().mapToInt(x -> x.size).sum() > 10000);
		} finally {
			engine.releaseThreadResources(resource);
		}
	}
}
