package org.daisy.pipeline.tts.attnative.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.LoadBalancer;
import org.daisy.pipeline.tts.LoadBalancer.Host;
import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.SimpleTTSEngine;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSServiceUtil;
import org.daisy.pipeline.tts.Voice;

public class ATTEngine extends SimpleTTSEngine implements ATTLibListener {

	private AudioFormat mAudioFormat;
	private LoadBalancer mLoadBalancer;
	private int mPriority;
	
	private static class ThreadResource extends TTSResource {
		long connection;
		List<AudioBuffer> audioBuffers;
		List<Mark> marks;
		byte[] utf8text;
		int offset;
		int outOfMemBytes;
		AudioBufferAllocator bufferAllocator;
	}
	
	protected ATTEngine(TTSService provider, LoadBalancer lb, int priority) {
		super(provider);
		mLoadBalancer = lb;
		ATTLib.setListener(this);
		mPriority = priority;
		mAudioFormat = new AudioFormat(16000, 16, 1, true, false);
	}

	@Override
	public Collection<AudioBuffer> synthesize(String sentence,
			Voice voice, TTSResource threadResources,
			List<Mark> marks, AudioBufferAllocator bufferAllocator,
			boolean retry) throws SynthesisException, InterruptedException, MemoryException {
		if (retry) {
			//If the synthesis has failed once, it's likely because the connection is dead,
			//therefore we open a new connection.
			ThreadResource old = (ThreadResource) threadResources;
			releaseThreadResources(threadResources);
			ThreadResource tr = (ThreadResource) allocateThreadResources();
			old.connection = tr.connection;
		}

		return synthesize(sentence, threadResources, marks, bufferAllocator);
	}

	private Collection<AudioBuffer> synthesize(String ssml, TTSResource resource,
	        List<Mark> marks, AudioBufferAllocator bufferAllocator) throws SynthesisException,
	        MemoryException {

		ThreadResource tr = (ThreadResource) resource;
		tr.audioBuffers = new ArrayList<AudioBuffer>();
		tr.marks = marks;
		tr.offset = 0;
		tr.outOfMemBytes = 0;
		tr.bufferAllocator = bufferAllocator;

		UTF8Converter.UTF8Buffer utf8Buffer = UTF8Converter.convertToUTF8(ssml, tr.utf8text);
		tr.utf8text = utf8Buffer.buffer;

		ATTLib.speak(tr, tr.connection, tr.utf8text);

		if (tr.outOfMemBytes > 0) {
			throw new MemoryException(tr.outOfMemBytes);
		}

		return tr.audioBuffers;
	}

	@Override
	public AudioFormat getAudioOutputFormat() {
		return mAudioFormat;
	}

	@Override
	public void onRecvAudio(Object handler, ByteBuffer audio, int size) {
		ThreadResource tr = (ThreadResource) handler;
		if (tr.outOfMemBytes == 0) {
			try {
				AudioBuffer buffer = tr.bufferAllocator.allocateBuffer(size);
				audio.get(buffer.data, 0, size);
				tr.audioBuffers.add(buffer);
				tr.offset += size;
			} catch (MemoryException e) {
				tr.outOfMemBytes = size;
			}
		}
	}

	@Override
	public void onRecvMark(Object handler, String name) {
		ThreadResource tr = (ThreadResource) handler;
		tr.marks.add(new Mark(name, tr.offset));
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException,
	        InterruptedException {
		return allocateThreadResources(mLoadBalancer.selectHost());
	}

	private ThreadResource allocateThreadResources(Host h) throws SynthesisException,
	        InterruptedException {
		long connection = ATTLib.openConnection(h.address, h.port, (int) mAudioFormat
		        .getSampleRate(), mAudioFormat.getSampleSizeInBits());
		if (connection == 0) {
			throw new SynthesisException("cannot open connections with ATTServer on " + h,
			        null);
		}

		ThreadResource tr = new ThreadResource();
		tr.connection = connection;
		tr.utf8text = new byte[8];
		return tr;
	}

	@Override
	public void releaseThreadResources(TTSResource resource) {
		ThreadResource tr = (ThreadResource) resource;
		ATTLib.closeConnection(tr.connection);
	}

	@Override
	public int getOverallPriority() {
		return mPriority;
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException {
		ThreadResource tr = (ThreadResource) allocateThreadResources(mLoadBalancer.getMaster());
		String[] voices = ATTLib.getVoiceNames(tr.connection);
		ATTLib.closeConnection(tr.connection);

		Voice[] result = new Voice[voices.length];
		for (int i = 0; i < voices.length; ++i) {
			result[i] = new Voice(getProvider().getName(), voices[i]);
		}

		return Arrays.asList(result);
	}

	@Override
	public String endingMark() {
		return "ending-mark";
	}

	
}
