package org.daisy.pipeline.audio.lame;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.audio.AudioEncoder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.io.Files;

/**
 * Pre-requisites:
 * 
 * - Lame installed
 * 
 * - avconv installed
 * 
 * - Read and write permissions on the OS' tmp directory
 */
public class LameTest {

	static class AudioBufferTest extends AudioBuffer {
		public AudioBufferTest(int size) {
			data = new byte[size];
			this.size = size;
		}

		public AudioBufferTest(byte[] data, int size) {
			this.data = data;
			this.size = size;
		}
	}

	@Before
	public void cleanProperties() {
		System.setProperty("host.protection", "true");
	}

	private static byte[] mp3ToPCM(AudioFormat originalFormat, String mp3File)
	        throws IOException, InterruptedException {

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

	private static AudioBuffer ref;
	private static AudioFormat refFormat;
	private static String mp3ref;
	private static LameEncoder lame;

	@BeforeClass
	public static void buildReference() throws Throwable {
		refFormat = new AudioFormat(8000, 8, 1, true, true); //8 bits signed big-endian (for easy comparisons)

		ref = new AudioBufferTest(1024 * 100);
		Random r = new Random();
		r.setSeed(0);
		ref.data[0] = (byte) r.nextInt(256);
		for (int i = 1; i < ref.size; ++i) {
			int next = (r.nextInt(3) - 1) + ref.data[i - 1];
			if (next > 127)
				next = 127;
			else if (next < -127)
				next = -127;
			ref.data[i] = (byte) next;
		}

		//dump the reference on the disk using Lame
		lame = new LameEncoder();
		AudioEncoder.EncodingOptions opts = lame.parseEncodingOptions(Collections.EMPTY_MAP);
		Optional<String> uri = lame.encode(Arrays.asList(ref), refFormat, new File(System
		        .getProperty("java.io.tmpdir")), "mp3ref", opts);
		if (!uri.isPresent())
			throw new RuntimeException("Could not encode the reference mp3");
		mp3ref = uri.get();
	}

	private boolean isValid(AudioFormat sourceFormat) throws Throwable {
		AudioEncoder.EncodingOptions opts = lame.parseEncodingOptions(Collections.EMPTY_MAP);

		//use avconv to create a new version of the PCM reference encoded with sourceFormat
		byte[] audio = mp3ToPCM(sourceFormat, mp3ref);

		//use lame to convert it to MP3
		AudioBuffer b = new AudioBufferTest(audio, audio.length);
		Optional<String> lameMp3 = lame.encode(Arrays.asList(b), sourceFormat, new File(System
		        .getProperty("java.io.tmpdir")), "lametest", opts);

		if (!lameMp3.isPresent()) {
			System.err.println("Lame could not encode the data");
			return false;
		}

		//convert it back to PCM in order to compare them
		byte[] lameAudio = mp3ToPCM(refFormat, lameMp3.get());

		//compare
		//TODO: proper convolution or frequency-based comparison (after FFT)
		byte[] small = ref.data;
		byte[] big = lameAudio;
		if (ref.data.length > lameAudio.length) {
			big = ref.data;
			small = lameAudio;
		}
		int diff = big.length - small.length;
		if (diff > 3 * ref.data.length / 100) {
			System.err.println("size differs too much");
			return false;
		}
		int window = Math.min(ref.data.length, lameAudio.length);
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
	public void noHostProtection() throws Throwable {
		System.setProperty("host.protection", "false");
		boolean valid = isValid(new AudioFormat(8000, 8, 1, true, true));
		Assert.assertTrue(valid);
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
		boolean valid = isValid(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 4, 8000, true));
		Assert.assertTrue(valid);
	}

	@Test
	public void floatingpoint64littleEndian() throws Throwable {
		boolean valid = isValid(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 4, 8000,
		        false));
		Assert.assertTrue(valid);
	}

}
