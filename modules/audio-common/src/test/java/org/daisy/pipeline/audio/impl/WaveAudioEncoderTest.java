package org.daisy.pipeline.audio.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;

import com.google.common.io.ByteStreams;
import com.google.common.primitives.Bytes;

import org.daisy.pipeline.audio.AudioDecoder;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioFileTypes;
import org.daisy.pipeline.audio.AudioUtils;

import org.junit.Assert;
import org.junit.Test;

public class WaveAudioEncoderTest {

	private static final byte[] header_ChunkID = "RIFF".getBytes(US_ASCII);
	private static final byte[] header_Format = "WAVE".getBytes(US_ASCII);
	private static final byte[] header_Subchunk1ID = "fmt ".getBytes(US_ASCII);
	private static final byte[] header_Subchunk1Size = getIntegerBytesLittleEndian(16);
	private static final byte[] header_AudioFormat_PCM = getShortBytesLittleEndian((short)1);
	private static final byte[] header_AudioFormat_IEEE_FLOAT = getShortBytesLittleEndian((short)3);
	private static final byte[] header_Subchunk2ID = "data".getBytes(US_ASCII);

	private static byte[] getIntegerBytesLittleEndian(int value) {
		byte[] bytes = new byte[4];
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putInt(value);
		return bytes;
	}

	private static byte[] getShortBytesLittleEndian(short value) {
		byte[] bytes = new byte[2];
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		buf.order(ByteOrder.LITTLE_ENDIAN);
		buf.putShort(value);
		return bytes;
	}

	private static byte[] header_Subchunk1(AudioFormat fmt) {
		if (!AudioUtils.isPCM(fmt)) throw new IllegalArgumentException();
		byte[] header_AudioFormat = fmt.getEncoding() == Encoding.PCM_FLOAT
			? header_AudioFormat_IEEE_FLOAT
			: header_AudioFormat_PCM;
		byte[] header_NumChannels = getShortBytesLittleEndian((short)fmt.getChannels());
		byte[] header_SampleRate = getIntegerBytesLittleEndian((int)Math.round(fmt.getSampleRate()));
		byte[] header_ByteRate = getIntegerBytesLittleEndian(
			(int)Math.round(fmt.getSampleRate()) * fmt.getChannels() * fmt.getSampleSizeInBits() / 8);
		byte[] header_BlockAlign = getShortBytesLittleEndian((short)(fmt.getChannels() * fmt.getSampleSizeInBits() / 8));
		byte[] header_BitsPerSample = getShortBytesLittleEndian((short)fmt.getSampleSizeInBits());
		return Bytes.concat(
			header_Subchunk1ID,    // bytes 12-15
			header_Subchunk1Size,  // bytes 16-19
			header_AudioFormat,    // bytes 20-21
			header_NumChannels,    // bytes 22-23
			header_SampleRate,     // bytes 24-27
			header_ByteRate,       // bytes 28-31
			header_BlockAlign,     // bytes 32-33
			header_BitsPerSample); // bytes 34-35
	}

	private static byte[] header_Subchunk2(byte[] data) {
		byte[] header_Subchunk2Size = getIntegerBytesLittleEndian(data.length);
		return Bytes.concat(
			header_Subchunk2ID,   // bytes 36-39
			header_Subchunk2Size, // bytes 40-43
			data);                // bytes 44-...
	}

	/**
	 * Test WAVE encoder by writing an audio stream to file and checking the contents of the file.
	 */
	private void testEncode(AudioInputStream input, byte[] expectedDataBytes) {
		AudioEncoder encoder = new SystemAudioEncoder().newEncoder(new HashMap<String,String>()).get();
		File wav; {
			try {
				wav = File.createTempFile("test", ".wav");
				wav.deleteOnExit();
			} catch (IOException e) {
				throw new RuntimeException(e); // failed to create file
			}
		}
		try {
			encoder.encode(input, AudioFileTypes.WAVE, wav);
		} catch (Throwable e) {
			throw new RuntimeException("Encoder threw exception", e);
		}
		byte[] header_ChunkSize = getIntegerBytesLittleEndian(36 + expectedDataBytes.length);
		byte[] expectedBytes = Bytes.concat(header_ChunkID,                       // bytes 0-3
		                                    header_ChunkSize,                     // bytes 4-7
		                                    header_Format,                        // bytes 8-11
		                                    header_Subchunk1(input.getFormat()),  // bytes 12-35
		                                    header_Subchunk2(expectedDataBytes)); // bytes 36-...
		try {
			Assert.assertArrayEquals(expectedBytes, ByteStreams.toByteArray(new FileInputStream(wav)));
		} catch (IOException e) {
			throw new RuntimeException(e); // failed to read file
		}
	}

	/**
	 * Test WAVE encoder by writing a (generated) audio stream to file, reading it again, converting
	 * it to the original audio format, and comparing the result with the input audio stream.
	 */
	private void testEncodeAndDecode(AudioFormat format) throws AssertionError, RuntimeException {
		if (format.getChannels() != 1)
			throw new IllegalArgumentException();
		byte[] pcm = new byte[1024 * 100]; {
			Random r = new Random();
			r.setSeed(0);
			if (format.getEncoding() == Encoding.PCM_FLOAT) {
				// PCM floating point samples are IEEE 754 and vary in the interval (-1.0...1.0)
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
		AudioInputStream audioStream = AudioUtils.createAudioStream(format, pcm);
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
		AudioDecoder decoder = new SystemAudioDecoder().newDecoder(new HashMap<String,String>()).get();
		try {
			audioStream = decoder.decode(wav);
		} catch (Throwable e) {
			Assert.fail("WAV file can not be read");
		}
		// audio format is not automatically the same as before the encoding
		if (!format.matches(audioStream.getFormat())) {
			audioStream = AudioUtils.convertAudioStream(format, audioStream);
			if (!format.matches(audioStream.getFormat()))
				throw new AssertionError(
					"audio stream is expected to have the same audio format after encoding and decoding");
		}
		byte[] comparePcm = new byte[pcm.length];
		try {
			Assert.assertEquals(pcm.length, audioStream.read(comparePcm));
		} catch (IOException e) {
			throw new RuntimeException(e); // should not happen
		}
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
	}

	private static final boolean SIGNED = true;
	private static final boolean UNSIGNED = false;
	private static final boolean BIG_ENDIAN = true;
	private static final boolean LITTLE_ENDIAN = false;

	@Test
	public void test8Khz() {
		AudioFormat fmt = new AudioFormat(8000, 8, 1, UNSIGNED, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 8-bit samples are stored as unsigned bytes
		           // => no conversion needed
		           new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void test16Khz() {
		AudioFormat fmt = new AudioFormat(16000, 8, 1, UNSIGNED, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 8-bit samples are stored as unsigned bytes
		           // => no conversion needed
		           new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void test32Khz() {
		AudioFormat fmt = new AudioFormat(32000, 8, 1, UNSIGNED, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 8-bit samples are stored as unsigned bytes
		           // => no conversion needed
		           new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void test48Khz() {
		AudioFormat fmt = new AudioFormat(48000, 8, 1, UNSIGNED, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 8-bit samples are stored as unsigned bytes
		           // => no conversion needed
		           new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void testSigned8bits() {
		AudioFormat fmt = new AudioFormat(8000, 8, 1, SIGNED, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 8-bit samples are stored as unsigned bytes
		           // sign encoding of PCM is always 2's complement
		           // range of 8-bit unsigned integer is 0..255
		           // range of 8-bit 2's complement is -128..127
		           // => conversion needed:
		           //    - 0xbb =  -69 -->  59 = 0x3b
		           //    - 0xd4 =  -44 -->  84 = 0x54
		           //    - 0x3d =   61 --> 189 = 0xbd
		           //    - 0x9b = -101 -->  27 = 0x1b
		           new byte[]{(byte)0x3b, (byte)0x54, (byte)0xbd, (byte)0x1b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void test16bits() {
		AudioFormat fmt = new AudioFormat(16000, 16, 1, SIGNED, LITTLE_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 16-bit samples are stored as 2's-complement signed integers
		           // the byte ordering in WAVE is little endian
		           // sign encoding of PCM is always 2's complement
		           // => no conversion needed
		           new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void test32bits() {
		AudioFormat fmt = new AudioFormat(16000, 32, 1, SIGNED, LITTLE_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 32-bit samples are stored as 2's-complement signed integers
		           // the byte ordering in WAVE is little endian
		           // sign encoding of PCM is always 2's complement
		           // => no conversion needed
		           new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void test64bits() {
		AudioFormat fmt = new AudioFormat(16000, 64, 1, SIGNED, LITTLE_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b,
		                          (byte)0xa3, (byte)0x4f, (byte)0x8c, (byte)0x1d}),
		           // 64-bit samples are stored as 2's-complement signed integers
		           // the byte ordering in WAVE is little endian
		           // sign encoding of PCM is always 2's complement
		           // => no conversion needed
		           new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b,
		                      (byte)0xa3, (byte)0x4f, (byte)0x8c, (byte)0x1d});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void testUnsigned16bits() {
		AudioFormat fmt = new AudioFormat(8000, 16, 1, UNSIGNED, LITTLE_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 16-bit samples are stored as 2's-complement signed integers
		           // the byte ordering in WAVE is little endian
		           // range of 8-bit unsigned integer is 0..65535
		           // range of 16-bit 2's complement is -32768..32767
		           // => conversion needed:
		           //    - bb d4 = 0xd4bb = 54459 --> 21691 = 0x54bb = bb 54
		           //    - 3d 9b = 0x9b3d = 39741 -->  6973 = 0x1b3d = 3d 1b
		           new byte[]{(byte)0xbb, (byte)0x54, (byte)0x3d, (byte)0x1b});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void test16bitsBigEndian() {
		AudioFormat fmt = new AudioFormat(16000, 16, 1, SIGNED, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 16-bit samples are stored as 2's-complement signed integers
		           // the byte ordering in WAVE is little endian
		           // sign encoding of PCM is always 2's complement
		           // => conversion needed from big endian to little endian
		           new byte[]{(byte)0xd4, (byte)0xbb, (byte)0x9b, (byte)0x3d});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void testUnsigned16bitsBigEndian() {
		AudioFormat fmt = new AudioFormat(8000, 16, 1, UNSIGNED, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               new byte[]{(byte)0xbb, (byte)0xd4, (byte)0x3d, (byte)0x9b}),
		           // 16-bit samples are stored as 2's-complement signed integers
		           // the byte ordering in WAVE is little endian
		           // range of 8-bit unsigned integer is 0..65535
		           // range of 16-bit 2's complement is -32768..32767
		           // => conversion needed:
		           //    - bb d4 = 0xbbd4 = 48084 -->  15316 = 0x3bd4 = d4 3b
		           //    - 3d 9b = 0x3d9b = 15771 --> -16997 = 0xbd9b = 9b bd
		           new byte[]{(byte)0xd4, (byte)0x3b, (byte)0x9b, (byte)0xbd});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void testFloatingPoint32bits() {
		AudioFormat fmt = new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, LITTLE_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               // d0 82 ec 3e = 0x3eec82d0 = 0.461935520172
		               // a2 b2 29 3f = 0x3f29b2a2 = 0.662881970406
		               new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e, (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f}),
		           // WAVE supports storing floating point (IEEE 754) samples
		           // the byte ordering in WAVE is little endian
		           // => no conversion needed
		           new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e, (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void testFloatingPoint64bits() {
		AudioFormat fmt = new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, LITTLE_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               // d0 82 ec 3e a2 b2 29 3f = 0x3f29b2a23eec82d0 = 0.0001960585645628593921829807555923252948559820652008056640625
		               new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e, (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f}),
		           // WAVE supports storing floating point (IEEE 754) samples
		           // the byte ordering in WAVE is little endian
		           // => no conversion needed
		           new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e, (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void testFloatingPoint32bitsBigEndian() {
		AudioFormat fmt = new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               // 3e ec 82 d0 = 0x3eec82d0 = 0.461935520172
		               // 3f 29 b2 a2 = 0x3f29b2a2 = 0.662881970406
		               new byte[]{(byte)0x3e, (byte)0xec, (byte)0x82, (byte)0xd0, (byte)0x3f, (byte)0x29, (byte)0xb2, (byte)0xa2}),
		           // WAVE supports storing floating point (IEEE 754) samples
		           // the byte ordering in WAVE is little endian
		           // => conversion needed from big endian to little endian
		           new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e, (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f});
		testEncodeAndDecode(fmt);
	}

	@Test
	public void testFloatingPoint64bitsBigEndian() {
		AudioFormat fmt = new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, BIG_ENDIAN);
		testEncode(AudioUtils.createAudioStream(
		               fmt,
		               // 3f 29 b2 a2 3e ec 82 d0 = 0x3f29b2a23eec82d0 = 0.0001960585645628593921829807555923252948559820652008056640625
		               new byte[]{(byte)0x3f, (byte)0x29, (byte)0xb2, (byte)0xa2, (byte)0x3e, (byte)0xec, (byte)0x82, (byte)0xd0}),
		           // WAVE supports storing floating point (IEEE 754) samples
		           // the byte ordering in WAVE is little endian
		           // => conversion needed from big endian to little endian:
		           new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e, (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f});
		testEncodeAndDecode(fmt);
	}
}
