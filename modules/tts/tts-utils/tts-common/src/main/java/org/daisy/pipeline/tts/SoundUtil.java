package org.daisy.pipeline.tts;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.daisy.pipeline.audio.AudioBuffer;

public class SoundUtil {
	private static int MinRiffHeaderSize = 44;

	static public void readWave(File soundFile, AudioBuffer audioBuffer)
	        throws UnsupportedAudioFileException, IOException {
		int maxLength = (int) (soundFile.length() - MinRiffHeaderSize);
		if (maxLength < 0) {
			throw new IOException(soundFile.getAbsolutePath()
			        + " is too small to be a WAV file");
		}

		if (maxLength > (audioBuffer.data.length - audioBuffer.size)) {
			realloc(audioBuffer, maxLength);
		}

		AudioInputStream fi = AudioSystem.getAudioInputStream(soundFile);
		int read = 0;
		while (audioBuffer.size + read != audioBuffer.data.length && read != -1) {
			audioBuffer.size += read;
			read = fi.read(audioBuffer.data, audioBuffer.size, audioBuffer.data.length
			        - audioBuffer.size);
		}
		fi.close();
	}

	static public void realloc(AudioBuffer buffer, int extra) {
		byte[] newBuffer = new byte[buffer.size + extra];
		System.arraycopy(buffer.data, 0, newBuffer, 0, buffer.size);
		buffer.data = newBuffer;
	}

	static public void cancelFootPrint(Iterable<AudioBuffer> buffers,
	        AudioBufferAllocator allocator) {
		for (AudioBuffer buff : buffers) {
			allocator.releaseBuffer(buff);
		}
	}
}
