package org.daisy.pipeline.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class PCMAudioFormat extends AudioFormat {

	private PCMAudioFormat(AudioFormat format) {
		super(format.getEncoding(),
		      format.getSampleRate(),
		      format.getSampleSizeInBits(),
		      format.getChannels(),
		      format.getFrameSize(),
		      format.getFrameRate(),
		      format.isBigEndian());
	}

	/**
	 * @throws IllegalArgumentException if <code>format</code> is not PCM encoded, or has frame rate,
	 *                                  frame size, sample rate, sample size, or number of channels
	 *                                  not specified.
	 */
	public static PCMAudioFormat of(AudioFormat format) {
		if (!AudioUtils.isPCM(format))
			throw new IllegalArgumentException();
		if (format.getFrameRate() == AudioSystem.NOT_SPECIFIED)
			throw new IllegalArgumentException();
		if (format.getFrameSize() == AudioSystem.NOT_SPECIFIED)
			throw new IllegalArgumentException();
		if (format.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED)
			throw new IllegalArgumentException();
		if (format.getSampleRate() == AudioSystem.NOT_SPECIFIED)
			throw new IllegalArgumentException();
		if (format.getChannels() == AudioSystem.NOT_SPECIFIED)
			throw new IllegalArgumentException();
		return new PCMAudioFormat(format);
	}
}
