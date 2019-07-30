package org.daisy.pipeline.tts.qfrency.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.StraightBufferAllocator;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/*
 * Unit test for the Qfrency adapter.
 * You must have a speech server running on localhost to run these tests. You also nead the client program in your path as "qfrency-client". 
 */


public class QfrencyTest {
	QfrencyEngine tts;

	static AudioBufferAllocator BufferAllocator = new StraightBufferAllocator();

	private String format(String str) {
		return str;
	}

	private String format(String str, String speakerName) {
		return "\\voice{" + speakerName + "}" + str;
	}

	static private int getSize(Collection<AudioBuffer> buffers) {
		if (buffers == null)
			return -1;
		int size = 0;
		for (AudioBuffer b : buffers) {
			size += b.size;
		}
		return size;
	}

	@Before
	public void setUp() throws SynthesisException, InterruptedException {
		tts = new QfrencyEngine(new QfrencyService(), "qfrency-client", "localhost", 0);
	}

	@Test
	public void getVoiceNames() throws SynthesisException, InterruptedException {
		Collection<Voice> voices = tts.getAvailableVoices();
		Assert.assertTrue("some voices must be found", voices.size() > 0);
	}

	public Voice getVoice() throws SynthesisException, InterruptedException {
		Collection<Voice> voices = tts.getAvailableVoices();
		if (voices.size()>0)
			return voices.iterator().next();
		return null;
	}
	
	@Test
	public void simpleSpeak() throws SynthesisException, InterruptedException, MemoryException {
		TTSResource r = tts.allocateThreadResources();
		Voice voice = getVoice();
		Assert.assertTrue("At least one voice must be available", voice!=null);
		int size = getSize(tts.synthesize(format("this is a test"), null, voice, r, null, null, BufferAllocator, true));
		tts.releaseThreadResources(r);

		Assert.assertTrue("audio output must be big enough", size > 2000);
	}

}
