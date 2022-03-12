package org.daisy.pipeline.audio.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioEncoderService;

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
					public void encode(AudioInputStream pcm, AudioFileFormat.Type outputFileType, File outputFile) throws Throwable {
						AudioFormat format = pcm.getFormat();
						if (outputFileType == AudioFileFormat.Type.WAVE) {
							if (format.getEncoding() == Encoding.PCM_FLOAT) {
								if (format.isBigEndian())
									// FIXME: AudioSystem stores this as big-endian which is not according to the WAV standard
									throw new IllegalArgumentException(
										"Can not store {PCM_FLOAT, big-endian} audio as WAV");

								// AudioSystem creates the wrong file header. We fix the wrong bytes here.
								// see http://www-mmsp.ece.mcgill.ca/Documents/AudioFormats/WAVE/WAVE.html
								File tmpFile = new File(outputFile.getAbsolutePath() + ".tmp");
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
							} else if (format.getEncoding() == Encoding.PCM_UNSIGNED
							           && format.getFrameSize() > 1
							           && !format.isBigEndian()) {

								// FIXME: AudioSystem stores this as signed little-endian (according to the WAV standard)
								// but does not convert the data
								throw new IllegalArgumentException(
									"Can not store {PCM_UNSIGNED, " + format.getFrameSize() + " bytes/frame, little-endian} audio as WAV");
							} else
								AudioSystem.write(pcm, outputFileType, outputFile);
						} else
							AudioSystem.write(pcm, outputFileType, outputFile);
					}
				};
		return Optional.of(instance);
	}

	private static AudioEncoder instance = null;

}
