package org.daisy.pipeline.tts;

import org.daisy.pipeline.audio.AudioBuffer;

public interface AudioBufferAllocator {
	public class MemoryException extends Exception {
		public MemoryException(String message) {
			super(message);
		}

		public MemoryException(int bytes) {
			super("tried to allocate " + bytes + " bytes (" + bytes / 1000000.0 + "MB)");
		}
	}

	AudioBuffer allocateBuffer(int size) throws MemoryException;

	void releaseBuffer(AudioBuffer b);
}
