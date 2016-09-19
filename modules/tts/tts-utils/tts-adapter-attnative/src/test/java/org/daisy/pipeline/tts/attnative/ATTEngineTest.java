package org.daisy.pipeline.tts.attnative;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.StraightBufferAllocator;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.Voice;
import org.junit.Before;
import org.junit.Test;

public class ATTEngineTest extends AbstractTTSService {
	@Before
	public void setUp() {
		ATTHelpers.loadATT();
	}
	
	@Test
	public void simpleSentence() throws SynthesisException, InterruptedException, MemoryException{
		ATTEngine engine = new ATTEngine(this, new RoundRobinLoadBalancer("localhost:8888", this), 1);
		
		Collection<Voice> voices = engine.getAvailableVoices();
		Assert.assertNotSame("some voices are available", 0, voices.size());
		
		
		String txtsentence = "small sentence <mark name=\""+engine.endingMark()+"\"/><break time=\"250ms\"/>";
		
		
		Iterator<Voice> it = voices.iterator();
		
		while (it.hasNext()){
			Voice voice = it.next();
		
			ArrayList<Mark> marks = new ArrayList<Mark>();
			String sentence = ATTHelpers.SSML(txtsentence, voice.name);
			
			TTSResource r = engine.allocateThreadResources();
			Collection<AudioBuffer> res = engine.synthesize(sentence, voice, r, marks, new StraightBufferAllocator(), false);
			engine.releaseThreadResources(r);
			
			Assert.assertNotSame("AT&T returned audio using voice "+voice.name, 0, res.size());
			Assert.assertSame("one notification of SSML mark using voice "+voice.name, 1, marks.size());
			Assert.assertEquals("mark's name must be right using voice "+voice.name, engine.endingMark(), marks.get(0).name);
		}
	}

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		return null;
	}

	@Override
	public String getName() {
		return "att";
	}
}
