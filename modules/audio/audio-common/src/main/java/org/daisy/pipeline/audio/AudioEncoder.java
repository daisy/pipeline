package org.daisy.pipeline.audio;

import java.io.File;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

public interface AudioEncoder {

	/**
	 * Encode raw audio data into a single file (mp3 for instance). This method
	 * must forward any exceptions (including InterruptionException).
	 *
	 * @param pcm is the raw input audio.
	 * @param outputFileType the file format the encoder should write in
	 * @param outputFile is the output audio file
	 * @throws IllegalArgumentException if this decoder does not support <code>outputFileType</code>
	 * @throws Throwable when some other error happens
	 */
	void encode(AudioInputStream pcm, AudioFileFormat.Type outputFileType, File outputFile) throws Throwable;

}
