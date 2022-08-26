package org.daisy.pipeline.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

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
	default AudioInputStream decode(File inputFile) throws UnsupportedAudioFileException, Throwable {
		return decode(new FileInputStream(inputFile));
	}

	AudioInputStream decode(InputStream input) throws UnsupportedAudioFileException, Throwable;
}
