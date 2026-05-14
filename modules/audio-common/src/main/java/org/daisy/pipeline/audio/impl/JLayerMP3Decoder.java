package org.daisy.pipeline.audio.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;

import javazoom.jl.converter.Converter;

import com.google.common.io.ByteStreams;

import org.daisy.pipeline.audio.AudioDecoder;
import org.daisy.pipeline.audio.AudioDecoderService;
import org.daisy.pipeline.audio.AudioFileTypes;

import org.osgi.service.component.annotations.Component;

/*
 * Note that it is in theory also possibly to support MP3 in SystemAudioDecoder, through the MP3SPI
 * library (com.googlecode.soundlibs:mp3spi). When this library is on the class path,
 * AudioSystem.getAudioInputStream() applied on an MP3 file will return an MPEG encoded audio
 * stream, which we could then convert to a PCM encoded audio stream. Unfortunately this conversion
 * does not work for some reason. The resulting PCM encoded stream contains all zeros. Another issue
 * is that when MP3SPI is on the class path, AudioSystem.getAudioInputStream() may incorrectly read
 * a WAV file as MPEGL1.
 */
@Component(
	name = "jlayer-mp3-decoder",
	immediate = true,
	service = { AudioDecoderService.class }
)
public class JLayerMP3Decoder implements AudioDecoderService {

	@Override
	public boolean supportsFileType(AudioFileFormat.Type fileType) {
		return AudioFileTypes.MP3.equals(fileType);
	}

	@Override
	public Optional<AudioDecoder> newDecoder(Map<String,String> params) {
		if (instance == null)
			instance = new AudioDecoder() {
					@Override
					public AudioInputStream decode(InputStream input) throws Throwable {
						File tmpMp3 = File.createTempFile("mp3-to-wav-", ".mp3");
						ByteStreams.copy(input, new FileOutputStream(tmpMp3));
						try {
							return decode(tmpMp3);
						} finally {
							tmpMp3.delete();
						}
					}
					@Override
					public AudioInputStream decode(File inputFile) throws Throwable {
						File tmpWav = File.createTempFile("mp3-to-wav-", ".wav");
						tmpWav.deleteOnExit();
						new Converter().convert(inputFile.getAbsolutePath(), tmpWav.getAbsolutePath());
						try {
							return SystemAudioDecoder.getInstance().decode(tmpWav);
						} finally {
							tmpWav.delete();
						}
					}
				};
		return Optional.of(instance);
	}

	private static AudioDecoder instance = null;

}
