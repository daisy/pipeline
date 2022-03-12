package org.daisy.pipeline.audio.lame.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.audio.AudioEncoder;
import static org.daisy.pipeline.audio.AudioFileTypes.MP3;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

/**
 * Pre-requisites:
 * 
 * - avconv installed
 * - Read and write permissions on the OS' tmp directory
 */
public class LameTest {

	private static byte[] mp3ToPCM(AudioFormat originalFormat, String mp3File)
	        throws IOException, InterruptedException {

		Assume.assumeTrue("Test can not be run because avconv not present",
		                  BinaryFinder.find("avconv").isPresent());
		
		String tmp = System.getProperty("java.io.tmpdir");
		File pcmFile = new File(tmp, "converted.pcm");
		try {
			pcmFile.delete();
		} catch (Exception e) {
			//file already exists
		}
		String f = "";
		if (originalFormat.getEncoding() == AudioFormat.Encoding.PCM_SIGNED) {
			f += "s";
		} else if (originalFormat.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
			f += "u";
		} else if (originalFormat.getEncoding() == AudioFormat.Encoding.PCM_FLOAT) {
			f += "f";
		}

		f += originalFormat.getSampleSizeInBits();

		if (originalFormat.getSampleSizeInBits() > 8) {
			if (originalFormat.isBigEndian())
				f += "be";
			else
				f += "le";
		}

		//convert to PCM file with AVCONV
		//TODO: check that it does not output WAV instead of PCM (the difference of size is rather suspicious)
		ProcessBuilder ps = new ProcessBuilder("avconv", "-loglevel", "warning", "-i",
		        mp3File, "-f", f, "-ar", String
		                .valueOf((int) (originalFormat.getSampleRate())), "-acodec", "pcm_"
		                + f, pcmFile.toString());
		ps.redirectErrorStream(true);
		Process p = ps.start();
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((line = in.readLine()) != null) {
			System.out.println("avconv output: " + line);
		}
		in.close();
		p.waitFor();

		return Files.toByteArray(pcmFile);
	}

	private static byte[] ref;
	private static AudioFormat refFormat;
	private static String mp3ref;
	private static AudioEncoder lame;

	@BeforeClass
	public static void buildReference() throws Throwable {
		
		refFormat = new AudioFormat(8000, 8, 1, true, true); //8 bits signed big-endian (for easy comparisons)

		ref = new byte[1024 * 100];
		Random r = new Random();
		r.setSeed(0);
		ref[0] = (byte) r.nextInt(256);
		for (int i = 1; i < ref.length; ++i) {
			int next = (r.nextInt(3) - 1) + ref[i - 1];
			if (next > 127)
				next = 127;
			else if (next < -127)
				next = -127;
			ref[i] = (byte) next;
		}

		//dump the reference on the disk using Lame
		lame = new LameEncoderService().newEncoder(new HashMap<String,String>()).orElse(null);
		Assume.assumeTrue("Test can not be run because lame not present", lame != null);

		AudioInputStream audioStream = new AudioInputStream(
			new ByteArrayInputStream(ref),
			refFormat,
			ref.length / refFormat.getFrameSize());
		File encodedFile = new File(new File(System.getProperty("java.io.tmpdir")), "mp3ref.mp3");
		lame.encode(audioStream, MP3, encodedFile);
		mp3ref = encodedFile.toURI().toString();
	}

	private boolean isValid(AudioFormat sourceFormat) throws Throwable {

		//use avconv to create a new version of the PCM reference encoded with sourceFormat
		byte[] audio = mp3ToPCM(sourceFormat, mp3ref);

		//use lame to convert it to MP3
		AudioInputStream audioStream = new AudioInputStream(
			new ByteArrayInputStream(audio),
			sourceFormat,
			audio.length / sourceFormat.getFrameSize());
		File encodedFile = new File(new File(System.getProperty("java.io.tmpdir")), "lametest.mp3");
		lame.encode(audioStream, MP3, encodedFile);
		String lameMp3 = encodedFile.toURI().toString();

		//convert it back to PCM in order to compare them
		byte[] lameAudio = mp3ToPCM(refFormat, lameMp3);

		//compare
		//TODO: proper convolution or frequency-based comparison (after FFT)
		byte[] small = ref;
		byte[] big = lameAudio;
		if (ref.length > lameAudio.length) {
			big = ref;
			small = lameAudio;
		}
		int diff = big.length - small.length;
		if (diff > 3 * ref.length / 100) {
			System.err.println("size differs too much");
			return false;
		}
		int window = Math.min(ref.length, lameAudio.length);
		long minerror = Long.MAX_VALUE;
		for (int d = 0; d < diff; d += 2) {
			long e = 0;
			for (int i = 0; i < window; ++i) {
				e += Math.abs(small[i] - big[i + d]);
			}
			minerror = Math.min(minerror, e);
		}
		double error = ((double) minerror) / window;

		return (error < 10.0);
	}

	@Test
	public void sampleRate8Khz() throws Throwable {
		boolean valid = isValid(new AudioFormat(8000, 8, 1, true, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void sampleRate16Khz() throws Throwable {
		boolean valid = isValid(new AudioFormat(16000, 8, 1, true, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void sampleRate32Khz() throws Throwable {
		boolean valid = isValid(new AudioFormat(32000, 8, 1, true, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void sampleRate48Khz() throws Throwable {
		boolean valid = isValid(new AudioFormat(48000, 8, 1, true, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void width16bits() throws Throwable {
		boolean valid = isValid(new AudioFormat(16000, 16, 1, true, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void width32bits() throws Throwable {
		boolean valid = isValid(new AudioFormat(16000, 32, 1, true, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void litteEndian16bits() throws Throwable {
		boolean valid = isValid(new AudioFormat(16000, 16, 1, true, false));
		Assert.assertTrue(valid);
	}

	@Test
	public void unsigned8bits() throws Throwable {
		boolean valid = isValid(new AudioFormat(8000, 8, 1, false, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void unsigned16bitsBigEndian() throws Throwable {
		boolean valid = isValid(new AudioFormat(8000, 16, 1, false, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void unsigned16bitsLittleEndian() throws Throwable {
		boolean valid = isValid(new AudioFormat(8000, 16, 1, false, false));
		Assert.assertTrue(valid);
	}

	@Test
	public void floatingpoint32bigEndian() throws Throwable {
		boolean valid = isValid(new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void floatingpoint32littleEndian() throws Throwable {
		boolean valid = isValid(new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000,
		        false));
		Assert.assertTrue(valid);
	}

	@Test
	public void floatingpoint64bigEndian() throws Throwable {
		boolean valid = isValid(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void floatingpoint64littleEndian() throws Throwable {
		boolean valid = isValid(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000,
		        false));
		Assert.assertTrue(valid);
	}
}
