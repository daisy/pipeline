package org.daisy.pipeline.audio;

import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;

public interface AudioDecoderService {

	/**
	 * Whether this decoder supports reading from a certain file format.
	 */
	boolean supportsFileType(AudioFileFormat.Type fileType);

	/**
	 * @param params   contains various key-value pairs. Some of them might be parameters for the
	 *                 audio decoder. It can also contain parameters which have nothing to do with
	 *                 audio decoding. Such parameters must be ignored.
	 */
	Optional<AudioDecoder> newDecoder(Map<String,String> params);

}
