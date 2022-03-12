package org.daisy.pipeline.audio;

import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;

public interface AudioEncoderService {

	/**
	 * Whether this encoder supports encoding to a certain file format.
	 */
	boolean supportsFileType(AudioFileFormat.Type fileType);

	/**
	 * @param params contains various key-value pairs. Some of them might be parameters for the
	 *               audio encoder. It can also contain parameters which have nothing to do with
	 *               audio encoding. Such parameters must be ignored.
	 */
	Optional<AudioEncoder> newEncoder(Map<String,String> params);

}
