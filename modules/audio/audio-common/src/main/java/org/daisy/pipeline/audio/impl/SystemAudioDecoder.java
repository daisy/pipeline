package org.daisy.pipeline.audio.impl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.daisy.pipeline.audio.AudioDecoder;
import org.daisy.pipeline.audio.AudioDecoderService;
import org.daisy.pipeline.audio.AudioFileTypes;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "system-audio-decoder",
	immediate = true,
	service = { AudioDecoderService.class }
)
public class SystemAudioDecoder implements AudioDecoderService {

	@Override
	public boolean supportsFileType(AudioFileFormat.Type fileType) {
		return AudioFileTypes.WAVE.equals(fileType);
	}

	@Override
	public Optional<AudioDecoder> newDecoder(Map<String,String> params) {
		return Optional.of(getInstance());
	}

	static AudioDecoder instance = null;

	static AudioDecoder getInstance() {
		if (instance == null)
			instance = new AudioDecoder() {
					@Override
					public AudioInputStream decode(InputStream input) throws UnsupportedAudioFileException, Throwable {
						/* Javadoc of AudioSystem.getAudioInputStream: The implementation of this
						 * method may require multiple parsers to examine the stream to determine
						 * whether they support it. These parsers must be able to mark the stream,
						 * read enough data to determine whether they support the stream, and, if
						 * not, reset the stream's read pointer to its original position. If the
						 * input stream does not support these operation, this method may fail with
						 * an IOException. */
						if (!input.markSupported())
							input = new BufferedInputStream(input);
						return AudioSystem.getAudioInputStream(input);
					}
				};
		return instance;
	}
}
