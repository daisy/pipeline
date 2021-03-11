package org.daisy.pipeline.audio;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFormat;

public interface AudioEncoder {
	interface EncodingOptions {

	}

	/**
	 * Encode raw audio data into a single file (mp3 for instance). This method
	 * must forward any exceptions (including InterruptionException).
	 * 
	 * @param pcm are the audio data. The method is allowed to modify the audio
	 *            buffers (both their content and their size).
	 * 
	 * @param audioFormat tells how the data must be interpreted
	 * 
	 * @param outputDir is the directory where the sound file will be stored
	 * 
	 * @param filePrefix is the prefix of the output sound filename.
	 * 
	 * @param options is the object returned by parseEncodingOptions()
	 * 
	 * @return the URI where the sound has been output. The extension (e.g.
	 *         'mp3') is up to the encoder. Returns an absent optional if an
	 *         error occurs that cannot be easily transformed into a throwable
	 *         exception.
	 * @throws Throwable
	 * 
	 */
	Optional<String> encode(Iterable<AudioBuffer> pcm, AudioFormat audioFormat,
	        File outputDir, String filePrefix, EncodingOptions options) throws Throwable;

	/**
	 * @param params stores the options in their raw format. Note that the map
	 *            can contain more options than necessary. In such cases, the
	 *            AudioEncoder must ignore them. In the particular case of the
	 *            DAISY Pipeline, parseEncodingOptions() is called by
	 *            px:ssml-to-audio with the TTS config file's properties as input.
	 * @return non-null object containing the ready-to-use options.
	 */
	EncodingOptions parseEncodingOptions(Map<String, String> params);

	/**
	 * Test the encoder with the current options.
	 */
	void test(EncodingOptions options) throws Exception;
}
