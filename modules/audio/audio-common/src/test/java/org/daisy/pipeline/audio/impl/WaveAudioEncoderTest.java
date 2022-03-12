package org.daisy.pipeline.audio.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioFileTypes;

import org.junit.Assert;
import org.junit.Test;

public class WaveAudioEncoderTest {
	
	private void test(AudioFormat format) throws AssertionError, RuntimeException {
		byte[] pcm = new byte[1024 * 100]; {
			Random r = new Random();
			r.setSeed(0);
			if (format.getEncoding() == Encoding.PCM_FLOAT) {
				if (format.getFrameSize() == 4) {
					ByteBuffer bytes = ByteBuffer.wrap(pcm);
					if (!format.isBigEndian())
						bytes = bytes.order(ByteOrder.LITTLE_ENDIAN);
					for (int i = 0; i < pcm.length / 4; i++)
						bytes.putFloat(r.nextFloat() * 2 - 1);
				} else if (format.getFrameSize() == 8) {
					ByteBuffer bytes = ByteBuffer.wrap(pcm);
					if (!format.isBigEndian())
						bytes = bytes.order(ByteOrder.LITTLE_ENDIAN);
					for (int i = 0; i < pcm.length / 8; i++)
						bytes.putDouble(r.nextDouble() * 2 - 1);
				} else
					throw new IllegalArgumentException();
			} else {
				for (int i = 0; i < pcm.length; i++)
					pcm[i] = (byte)r.nextInt(256);
			}
		}
		AudioEncoder encoder = new SystemAudioEncoder().newEncoder(new HashMap<String,String>()).get();
		AudioInputStream audioStream = new AudioInputStream(
			new ByteArrayInputStream(pcm),
			format,
			pcm.length / format.getFrameSize());
		File wav; {
			try {
				wav = File.createTempFile("ref", ".wav");
				wav.deleteOnExit();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			encoder.encode(audioStream, AudioFileTypes.WAVE, wav);
		} catch (Throwable e) {
			throw new RuntimeException("Encoder threw exception", e);
		}
		// read wav file and compare with original bytes
		try {
			audioStream = AudioSystem.getAudioInputStream(wav);
			// audio format is not automatically the same as before the encoding
			if (!format.matches(audioStream.getFormat())) {
				audioStream = AudioSystem.getAudioInputStream(format, audioStream);
				if (!format.matches(audioStream.getFormat()))
					throw new AssertionError(
						"audio stream is expected to have the same audio format after encoding and decoding");
			}
			byte[] comparePcm = new byte[pcm.length];
			Assert.assertEquals(pcm.length, audioStream.read(comparePcm));
			if (format.getEncoding() == Encoding.PCM_SIGNED) {
				if (format.getFrameSize() >= 4) {
					if (format.getFrameSize() == 4) {
						ByteBuffer expected = ByteBuffer.wrap(pcm);
						ByteBuffer actual = ByteBuffer.wrap(comparePcm);
						if (!format.isBigEndian()) {
							expected = expected.order(ByteOrder.LITTLE_ENDIAN);
							actual = actual.order(ByteOrder.LITTLE_ENDIAN);
						}
						while (expected.hasRemaining()) {
							int pos = expected.position();
							int e = expected.getInt();
							int a = actual.getInt();
							if (Math.abs(e - a) > 0x1p6)
								throw new AssertionError(
									"arrays first differed at integer at position " + pos + "; " +
									"expected:<" + e + "> " +
									"but was:<" + a + ">");
						}
					} else if (format.getFrameSize() == 8) {
						ByteBuffer expected = ByteBuffer.wrap(pcm);
						ByteBuffer actual = ByteBuffer.wrap(comparePcm);
						if (!format.isBigEndian()) {
							expected = expected.order(ByteOrder.LITTLE_ENDIAN);
							actual = actual.order(ByteOrder.LITTLE_ENDIAN);
						}
						while (expected.hasRemaining()) {
							int pos = expected.position();
							long e = expected.getLong();
							long a = actual.getLong();
							if (Math.abs(e - a) > 0x1p38)
								throw new AssertionError(
									"arrays first differed at long at position " + pos + "; " +
									"expected:<" + e + "> " +
									"but was:<" + a + ">");
						}
					} else
						throw new IllegalArgumentException();
				} else
					Assert.assertArrayEquals(pcm, comparePcm);
			} else
				Assert.assertArrayEquals(pcm, comparePcm);
		} catch (UnsupportedAudioFileException | IOException e) {
			Assert.fail("WAV file can not be read");
		}
	}

	@Test
	public void test8Khz() {
		test(new AudioFormat(8000, 8, 1, false, true));
	}

	@Test
	public void test16Khz() {
		test(new AudioFormat(16000, 8, 1, false, true));
	}

	@Test
	public void test32Khz() {
		test(new AudioFormat(32000, 8, 1, false, true));
	}

	@Test
	public void test48Khz() {
		test(new AudioFormat(48000, 8, 1, false, true));
	}

	@Test
	public void testSigned8bits() {
		test(new AudioFormat(8000, 8, 1, true, true));
	}

	@Test
	public void test16bits() {
		test(new AudioFormat(16000, 16, 1, true, false));
	}

	@Test
	public void test32bits() {
		test(new AudioFormat(16000, 32, 1, true, false));
	}

	@Test
	public void test64bits() {
		test(new AudioFormat(16000, 64, 1, true, false));
	}

	@Test
	public void testUnsigned16bits() {
		try {
			test(new AudioFormat(8000, 16, 1, false, false));
		} catch (RuntimeException e) {
			if ("Encoder threw exception".equals(e.getMessage())
			    && e.getCause() instanceof IllegalArgumentException)
					return;
		}
		Assert.fail("Expected encoder exception");
	}

	@Test
	public void test16bitsBigEndian() {
		test(new AudioFormat(16000, 16, 1, true, true));
	}

	@Test
	public void testUnsigned16bitsBigEndian() {
		test(new AudioFormat(8000, 16, 1, false, true));
	}

	@Test
	public void testFloatingPoint32bits() {
		test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, false));
	}

	@Test
	public void testFloatingPoint64bits() {
		test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, false));
	}

	@Test
	public void testFloatingPoint32bitsBigEndian() {
		try {
			test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, true));
		} catch (RuntimeException e) {
			if ("Encoder threw exception".equals(e.getMessage())
			    && e.getCause() instanceof IllegalArgumentException)
					return;
		}
		Assert.fail("Expected encoder exception");
	}

	@Test
	public void testFloatinPpoint64bitsBigEndian() {
		try {
			test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, true));
		} catch (RuntimeException e) {
			if ("Encoder threw exception".equals(e.getMessage())
			    && e.getCause() instanceof IllegalArgumentException)
					return;
		}
		Assert.fail("Expected encoder exception");
	}
}
