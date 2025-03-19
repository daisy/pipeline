package org.daisy.pipeline.audio.lame.impl;

import static java.util.Collections.emptyMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.sound.sampled.AudioFormat;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.audio.AudioEncoder;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.io.Files;

/**
 * Pre-requisites:
 * 
 * - Lame installed
 * 
 * - ffmpeg installed
 * 
 * - Read and write permissions on the OS' tmp directory
 */
public class LameResamplingTest {

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
		System.setProperty("org.daisy.pipeline.tts.host.protection", "true");
	}

	private static byte[] mp3ToPCM(AudioFormat originalFormat, String mp3File)
	        throws IOException, InterruptedException {

		Assume.assumeTrue("Test can not be run because ffmpeg not present",
		                  BinaryFinder.find("ffmpeg").isPresent());
		
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

		//convert to PCM file with ffmpeg
		//TODO: check that it does not output WAV instead of PCM (the difference of size is rather suspicious)
		ProcessBuilder ps = new ProcessBuilder("ffmpeg", "-loglevel", "warning", "-i",
		        mp3File, "-f", f, "-ar", String
		                .valueOf((int) (originalFormat.getSampleRate())), "-acodec", "pcm_"
		                + f, pcmFile.toString());
		ps.redirectErrorStream(true);
		Process p = ps.start();
		
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		while ((line = in.readLine()) != null) {
			System.out.println("ffmpeg output: " + line);
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
		
		Assume.assumeTrue("Test can not be run because lame not present",
		                  BinaryFinder.find("lame").isPresent());
		
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
		AudioEncoder.EncodingOptions opts = lame.parseEncodingOptions(Map.of("org.daisy.pipeline.tts.lame.minfreq", "0"));
		Optional<String> uri = lame.encode(Arrays.asList(ref), refFormat, new File(System
		        .getProperty("java.io.tmpdir")), "mp3ref", opts);
		if (!uri.isPresent())
			throw new RuntimeException("Could not encode the reference mp3");
		mp3ref = uri.get();
	}

	private void checkValid(AudioFormat sourceFormat, int expectedFreq, int expectedRate) throws Throwable {
		checkValid(sourceFormat, emptyMap(), expectedFreq, expectedRate);
	}

	private void checkValid(AudioFormat sourceFormat, float minFreq, int expectedFreq, int expectedRate) throws Throwable {
		checkValid(sourceFormat, Map.of("org.daisy.pipeline.tts.lame.minfreq", Float.toString(minFreq)), expectedFreq, expectedRate);
	}

	private void checkValid(AudioFormat sourceFormat, Map<String, String> options, int expectedFreq, int expectedRate) throws Throwable {
		AudioEncoder.EncodingOptions opts = lame.parseEncodingOptions(options);

		//use ffmpeg to create a new version of the PCM reference encoded with sourceFormat
		byte[] audio = mp3ToPCM(sourceFormat, mp3ref);

		//use lame to convert it to MP3
		AudioBuffer b = new AudioBufferTest(audio, audio.length);
		Optional<String> lameMp3 = lame.encode(Arrays.asList(b), sourceFormat, new File(System
		        .getProperty("java.io.tmpdir")), "lametest", opts);

		Assert.assertTrue("Lame could not encode the data", lameMp3.isPresent());

		// check mp3 file information
		ProcessBuilder ps = new ProcessBuilder("ffprobe", "-i", lameMp3.get());
		ps.redirectErrorStream(true);
		Process p = ps.start();
		int freq = 0;
		int rate = 0;
		{
			String text = new BufferedReader(
				      new InputStreamReader(p.getInputStream()))
				        .lines()
				        .filter(s -> s.contains("Stream #0:0: Audio: mp3,"))
				        .findFirst()
				        .get();
			String freqStr = text.replaceFirst(".*mp3, ([0-9]*) Hz.*", "$1");
			freq = Integer.parseInt(freqStr);
			String rateStr = text.replaceFirst(".*, ([0-9]*) kb/s.*", "$1");
			rate = Integer.parseInt(rateStr);
		}
		Assert.assertEquals("The frequency did not match", expectedFreq, freq);
		Assert.assertEquals("The bitrate did not match", expectedRate, rate);
		
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
		Assert.assertTrue("Size differs too much (" + diff + " from " + (3 * ref.data.length / 100) + ")", diff <= 3 * ref.data.length / 100);
		int window = Math.min(ref.data.length, lameAudio.length);
		long minerror = Long.MAX_VALUE;
		System.out.println("Diff is " + diff);
		for (int d = 0; d < diff; d += 2) {
			long e = 0;
			for (int i = 0; i < window; ++i) {
				e += Math.abs(small[i] - big[i + d]);
			}
			minerror = Math.min(minerror, e);
		}
		double error = ((double) minerror) / window;

		Assert.assertTrue("Error is too big (" + error + ")", error < 10.0);
	}

	@Test
	public void sampleRate8Khz() throws Throwable {
		checkValid(new AudioFormat(8000, 8, 1, true, true), 44100, 64);
	}

	@Test
	public void sampleRate16Khz() throws Throwable {
		checkValid(new AudioFormat(16000, 8, 1, true, true), 44100, 64);
	}

	@Test
	public void sampleRate32Khz() throws Throwable {
		checkValid(new AudioFormat(32000, 8, 1, true, true), 44100, 64);
	}

	@Test
	public void sampleRate44Khz() throws Throwable {
		checkValid(new AudioFormat(44100, 8, 1, true, true), 44100, 64);
	}

	@Test
	public void sampleRate48Khz() throws Throwable {
		checkValid(new AudioFormat(48000, 8, 1, true, true), 48000, 64);
	}

	@Test
	public void sampleRate8KhzMin44() throws Throwable {
		checkValid(new AudioFormat(8000, 8, 1, true, true), 44.1f, 44100, 64);
	}

	@Test
	public void sampleRate16KhzMin44() throws Throwable {
		checkValid(new AudioFormat(16000, 8, 1, true, true), 44.1f, 44100, 64);
	}

	@Test
	public void sampleRate32KhzMin44() throws Throwable {
		checkValid(new AudioFormat(32000, 8, 1, true, true), 44.1f, 44100, 64);
	}

	@Test
	public void sampleRate44KhzMin44() throws Throwable {
		checkValid(new AudioFormat(44100, 8, 1, true, true), 44.1f, 44100, 64);
	}

	@Test
	public void sampleRate48KhzMin44() throws Throwable {
		checkValid(new AudioFormat(48000, 8, 1, true, true), 44.1f, 48000, 64);
	}

	@Test
	public void sampleRate8KhzMin32() throws Throwable {
		checkValid(new AudioFormat(8000, 8, 1, true, true), 32f, 32000, 48);
	}

	@Test
	public void sampleRate16KhzMin32() throws Throwable {
		checkValid(new AudioFormat(16000, 8, 1, true, true), 32f, 32000, 48);
	}

	@Test
	public void sampleRate32KhzMin32() throws Throwable {
		checkValid(new AudioFormat(32000, 8, 1, true, true), 32f, 32000, 48);
	}

	@Test
	public void sampleRate44KhzMin32() throws Throwable {
		checkValid(new AudioFormat(44100, 8, 1, true, true), 32f, 44100, 64);
	}

	@Test
	public void sampleRate48KhzMin32() throws Throwable {
		checkValid(new AudioFormat(48000, 8, 1, true, true), 32f, 48000, 64);
	}

	@Test
	public void sampleRate8KhzMin16() throws Throwable {
		checkValid(new AudioFormat(8000, 8, 1, true, true), 16f, 16000, 24);
	}

	@Test
	public void sampleRate16KhzMin16() throws Throwable {
		checkValid(new AudioFormat(16000, 8, 1, true, true), 16f, 16000, 24);
	}

	@Test
	public void sampleRate32KhzMin16() throws Throwable {
		checkValid(new AudioFormat(32000, 8, 1, true, true), 16f, 32000, 48);
	}

	@Test
	public void sampleRate44KhzMin16() throws Throwable {
		checkValid(new AudioFormat(44100, 8, 1, true, true), 16f, 44100, 64);
	}

	@Test
	public void sampleRate48KhzMin16() throws Throwable {
		checkValid(new AudioFormat(48000, 8, 1, true, true), 16f, 48000, 64);
	}

}
