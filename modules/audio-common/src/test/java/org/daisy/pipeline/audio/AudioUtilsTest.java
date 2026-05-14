package org.daisy.pipeline.audio;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;

import com.google.common.io.ByteStreams;

import org.junit.Assert;
import org.junit.Test;

public class AudioUtilsTest {

	private static final boolean SIGNED = true;
	private static final boolean UNSIGNED = false;
	private static final boolean BIG_ENDIAN = true;
	private static final boolean LITTLE_ENDIAN = false;

	@Test
	public void test8BitSignedToUnsignedAndVisaVersa() throws IOException {
		AudioFormat signed = new AudioFormat(8000, 8, 1, SIGNED, LITTLE_ENDIAN);
		AudioFormat unsigned = new AudioFormat(8000, 8, 1, UNSIGNED, LITTLE_ENDIAN);
		byte[] signedData = new byte[]{(byte)0xbb,  // 0xbb =  -69 <-->  59 = 0x3b
		                               (byte)0xd4,  // 0xd4 =  -44 <-->  84 = 0x54
		                               (byte)0x3d,  // 0x3d =   61 <--> 189 = 0xbd
		                               (byte)0x9b}; // 0x9b = -101 <-->  27 = 0x1b
		byte[] unsignedData = new byte[]{(byte)0x3b,
		                                 (byte)0x54,
		                                 (byte)0xbd,
		                                 (byte)0x1b};
		AudioInputStream unsignedStream = AudioUtils.convertAudioStream(
			unsigned,
			AudioUtils.createAudioStream(signed, signedData));
		Assert.assertTrue(unsigned.matches(unsignedStream.getFormat()));
		Assert.assertArrayEquals(
			unsignedData,
			ByteStreams.toByteArray(unsignedStream));
		AudioInputStream signedStream = AudioUtils.convertAudioStream(
			signed,
			AudioUtils.createAudioStream(unsigned, unsignedData));
		Assert.assertTrue(signed.matches(signedStream.getFormat()));
		Assert.assertArrayEquals(
			signedData,
			ByteStreams.toByteArray(signedStream));
	}

	@Test
	public void test16BitLittleEndianSignedToUnsignedAndVisaVersa() throws IOException {
		AudioFormat signed = new AudioFormat(8000, 16, 1, SIGNED, LITTLE_ENDIAN);
		AudioFormat unsigned = new AudioFormat(8000, 16, 1, UNSIGNED, LITTLE_ENDIAN);
		byte[] signedData = new byte[]{(byte)0xbb, (byte)0xd4,  // bb d4 = 0xd4bb = -11077 <--> 21691 = 0x54bb = bb 54
		                               (byte)0x3d, (byte)0x9b}; // 3d 9b = 0x9b3d = -25795 <-->  6973 = 0x1b3d = 3d 1b
		byte[] unsignedData = new byte[]{(byte)0xbb, (byte)0x54,
		                                 (byte)0x3d, (byte)0x1b};
		AudioInputStream unsignedStream = AudioUtils.convertAudioStream(
			unsigned,
			AudioUtils.createAudioStream(signed, signedData));
		Assert.assertTrue(unsigned.matches(unsignedStream.getFormat()));
		Assert.assertArrayEquals(
			unsignedData,
			ByteStreams.toByteArray(unsignedStream));
		AudioInputStream signedStream = AudioUtils.convertAudioStream(
			signed,
			AudioUtils.createAudioStream(unsigned, unsignedData));
		Assert.assertTrue(signed.matches(signedStream.getFormat()));
		Assert.assertArrayEquals(
			signedData,
			ByteStreams.toByteArray(signedStream));
	}

	@Test
	public void test32BitFloatingPointBigEndianToLittleEndianAndVisaVersa() throws IOException {
		AudioFormat bigEndian = new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, BIG_ENDIAN);
		AudioFormat littleEndian = new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, LITTLE_ENDIAN);
		byte[] bigEndianData = new byte[]{(byte)0x3e, (byte)0xec, (byte)0x82, (byte)0xd0,  // 0x3eec82d0 = 0.461935520172
		                                  (byte)0x3f, (byte)0x29, (byte)0xb2, (byte)0xa2}; // 0x3f29b2a2 = 0.662881970406
		byte[] littleEndianData = new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e,
		                                     (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f};
		AudioInputStream littleEndianStream = AudioUtils.convertAudioStream(
			littleEndian,
			AudioUtils.createAudioStream(bigEndian, bigEndianData));
		Assert.assertTrue(littleEndian.matches(littleEndianStream.getFormat()));
		Assert.assertArrayEquals(
			littleEndianData,
			ByteStreams.toByteArray(littleEndianStream));
		AudioInputStream bigEndianStream = AudioUtils.convertAudioStream(
			bigEndian,
			AudioUtils.createAudioStream(littleEndian, littleEndianData));
		Assert.assertTrue(bigEndian.matches(bigEndianStream.getFormat()));
		Assert.assertArrayEquals(
			bigEndianData,
			ByteStreams.toByteArray(bigEndianStream));
	}

	@Test
	public void test64BitFloatingPointBigEndianToLittleEndianAndVisaVersa() throws IOException {
		AudioFormat bigEndian = new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, BIG_ENDIAN);
		AudioFormat littleEndian = new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, LITTLE_ENDIAN);
		// 0x3f29b2a23eec82d0 = 0.0001960585645628593921829807555923252948559820652008056640625
		byte[] bigEndianData = new byte[]{(byte)0x3f, (byte)0x29, (byte)0xb2, (byte)0xa2, (byte)0x3e, (byte)0xec, (byte)0x82, (byte)0xd0};
		byte[] littleEndianData = new byte[]{(byte)0xd0, (byte)0x82, (byte)0xec, (byte)0x3e, (byte)0xa2, (byte)0xb2, (byte)0x29, (byte)0x3f};
		AudioInputStream littleEndianStream = AudioUtils.convertAudioStream(
			littleEndian,
			AudioUtils.createAudioStream(bigEndian, bigEndianData));
		Assert.assertTrue(littleEndian.matches(littleEndianStream.getFormat()));
		Assert.assertArrayEquals(
			littleEndianData,
			ByteStreams.toByteArray(littleEndianStream));
		AudioInputStream bigEndianStream = AudioUtils.convertAudioStream(
			bigEndian,
			AudioUtils.createAudioStream(littleEndian, littleEndianData));
		Assert.assertTrue(bigEndian.matches(bigEndianStream.getFormat()));
		Assert.assertArrayEquals(
			bigEndianData,
			ByteStreams.toByteArray(bigEndianStream));
	}
}
