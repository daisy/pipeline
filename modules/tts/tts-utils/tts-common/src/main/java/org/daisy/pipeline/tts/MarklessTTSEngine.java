package org.daisy.pipeline.tts;

import java.util.Collection;
import java.util.List;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;

public abstract class MarklessTTSEngine extends TTSEngine {

	public abstract Collection<AudioBuffer> synthesize(String sentence,
			XdmNode xmlSentence, Voice voice, TTSResource threadResources,
			AudioBufferAllocator bufferAllocator, boolean retry)
			throws SynthesisException, InterruptedException, MemoryException;
	
	
	protected MarklessTTSEngine(TTSService provider) {
		super(provider);
	}
	
	@Override
	public Collection<AudioBuffer> synthesize(String sentence,
			XdmNode xmlSentence, Voice voice, TTSResource threadResources,
			List<Mark> marks, List<String> expectedMarks,
			AudioBufferAllocator bufferAllocator, boolean retry)
			throws SynthesisException, InterruptedException, MemoryException {
		return synthesize(sentence, xmlSentence, voice, threadResources, bufferAllocator, retry);
	}
}
