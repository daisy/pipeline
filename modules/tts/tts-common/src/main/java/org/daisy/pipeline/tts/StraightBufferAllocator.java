package org.daisy.pipeline.tts;

import org.daisy.pipeline.audio.AudioBuffer;

public class StraightBufferAllocator implements AudioBufferAllocator {

	private static class AllocableAudioBuffer extends AudioBuffer {
		AllocableAudioBuffer(int size) {
			this.data = new byte[size];
			this.size = size;
		}
	}

	@Override
	public AudioBuffer allocateBuffer(int size) throws MemoryException {
		return new AllocableAudioBuffer(size);
	}

	@Override
	public void releaseBuffer(AudioBuffer b) {
	}

}
