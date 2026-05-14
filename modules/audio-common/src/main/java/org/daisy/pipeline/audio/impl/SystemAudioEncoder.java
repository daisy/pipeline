package org.daisy.pipeline.audio.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioEncoderService;
import org.daisy.pipeline.audio.AudioUtils;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "system-audio-encoder",
	immediate = true,
	service = { AudioEncoderService.class }
)
public class SystemAudioEncoder implements AudioEncoderService {

	@Override
	public boolean supportsFileType(AudioFileFormat.Type fileType) {
		return AudioSystem.isFileTypeSupported(fileType);
	}

	@Override
	public Optional<AudioEncoder> newEncoder(Map<String,String> params) {
		if (instance == null)
			instance = new AudioEncoder() {
					@Override
					public AudioClip encode(AudioInputStream pcm, AudioFileFormat.Type outputFileType, File outputFile) throws Throwable {
						AudioFormat format = pcm.getFormat();
						if (outputFileType == AudioFileFormat.Type.WAVE) {
							if (format.getEncoding() == Encoding.PCM_FLOAT) {
								if (format.isBigEndian()) {

									// AudioSystem stores this as big-endian which is not according
									// to the WAV standard. To work around this issue first convert
									// the samples to little-endian.
									return encode(
										AudioUtils.convertAudioStream(
											new AudioFormat(format.getEncoding(),
											                format.getSampleRate(),
											                format.getSampleSizeInBits(),
											                format.getChannels(),
											                format.getFrameSize(),
											                format.getFrameRate(),
											                false), // little endian
											pcm),
										outputFileType,
										outputFile);
								} else {

									// AudioSystem creates the wrong file header. We fix the wrong bytes here.
									// see http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
									File tmpFile = File.createTempFile("wrong-header-", ".wav.tmp");
									tmpFile.deleteOnExit();
									AudioSystem.write(pcm, outputFileType, tmpFile);
									try (FileInputStream wrongFile = new FileInputStream(tmpFile);
									     FileOutputStream correctedFile = new FileOutputStream(outputFile)) {
										byte[] header = new byte[44];
										wrongFile.read(header);
										// format = WAVE_FORMAT_IEEE_FLOAT
										header[20] = (byte)3;
										correctedFile.write(header);
										byte[] buf = new byte[1024];
										while (true) {
											int bytesRead = wrongFile.read(buf);
											if (bytesRead == -1)
												break;
											correctedFile.write(buf, 0, bytesRead);
										}
									}
									tmpFile.delete();
								}
							} else if (format.getEncoding() == Encoding.PCM_UNSIGNED
							           && format.getSampleSizeInBits() > 8
							           && !format.isBigEndian()) {

								// AudioSystem writes this as signed little-endian (according to the WAV standard)
								// but does not convert the data. This is probably because com.sun.media.sound.PCMtoPCMCodec
								// does not correctly convert unsigned little-endian to signed little-endian.
								// To work around this issue first convert the samples to big-endian.
								return encode(
									AudioUtils.convertAudioStream(
										new AudioFormat(format.getSampleRate(),
										                format.getSampleSizeInBits(),
										                format.getChannels(),
										                false, // unsigned
										                true), // big endian
										pcm),
									outputFileType,
									outputFile);
							} else
								AudioSystem.write(pcm, outputFileType, outputFile);
						} else
							AudioSystem.write(pcm, outputFileType, outputFile);
						return new AudioClip(outputFile, Duration.ZERO, AudioUtils.getDuration(pcm));
					}
				};
		return Optional.of(instance);
	}

	private static AudioEncoder instance = null;

}
