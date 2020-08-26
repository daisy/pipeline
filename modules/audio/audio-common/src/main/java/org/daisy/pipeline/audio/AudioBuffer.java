package org.daisy.pipeline.audio;

public class AudioBuffer {
	protected AudioBuffer() {
		//must be allocated in subclasses
	}

	public byte[] data;
	public int size;
}
