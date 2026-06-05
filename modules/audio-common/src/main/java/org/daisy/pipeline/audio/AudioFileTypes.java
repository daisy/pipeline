package org.daisy.pipeline.audio;

import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioFileFormat;

/**
 * Supported audio file types
 */
public final class AudioFileTypes {

	public static final AudioFileFormat.Type MP3 = new AudioFileFormat.Type("MP3", "mp3");
	public static final AudioFileFormat.Type WAVE = AudioFileFormat.Type.WAVE;

	private static final Map<String,AudioFileFormat.Type> mediaTypes = new HashMap<>();
	static {
		mediaTypes.put("audio/mpeg", MP3);
		mediaTypes.put("audio/mp3", MP3);
		mediaTypes.put("audio/wav", WAVE);
		mediaTypes.put("audio/wave", WAVE);
		mediaTypes.put("audio/x-wav", WAVE);
		mediaTypes.put("audio/vnd.wave", WAVE);
		mediaTypes.put("audio/aiff", AudioFileFormat.Type.AIFF);
		mediaTypes.put("audio/x-aiff", AudioFileFormat.Type.AIFF);
	}
	
	public static AudioFileFormat.Type fromMediaType(String mediaType) {
		return mediaTypes.get(mediaType);
	}
}
