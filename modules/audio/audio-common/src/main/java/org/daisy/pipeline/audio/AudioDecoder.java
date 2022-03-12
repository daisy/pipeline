package org.daisy.pipeline.audio;

import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

public interface AudioDecoder {

	/**
	 * Decode an audio file into raw (PCM encoded) audio.
	 *
	 * @param inputFile is the input audio file
	 * @return the raw output audio
	 * @throws UnsupportedAudioFileException if this decoder does not recognize
	 *                                       the file format of <code>inputFile</code>
	 * @throws Throwable when some other error happens
	 */
	AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, Throwable;

}
