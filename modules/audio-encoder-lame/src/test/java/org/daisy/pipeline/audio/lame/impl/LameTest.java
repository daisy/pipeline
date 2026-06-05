package org.daisy.pipeline.audio.lame.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;

import javax.inject.Inject;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.daisy.pipeline.audio.AudioDecoder;
import org.daisy.pipeline.audio.AudioEncoder;
import static org.daisy.pipeline.audio.AudioFileTypes.MP3;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.audio.AudioUtils;
import org.daisy.pipeline.junit.AbstractTest;

import com.google.common.io.ByteStreams;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Pre-requisites:
 *
 * - Read and write permissions on the OS' tmp directory
 */

public class LameTest extends AbstractTest {

	@Inject
	public AudioServices audioServices;

	/**
	 * Decode MP3 file to PCM data
	 */
	private AudioInputStream mp3ToPCM(AudioFormat pcmFormat, File mp3File) {
		Optional<AudioDecoder> decoder = audioServices.newDecoder(MP3, new HashMap<String,String>());
		if (!decoder.isPresent())
			throw new RuntimeException("No MP3 decoder found"); // should not happen
		try {
			AudioInputStream pcm = decoder.get().decode(mp3File);
			if (!pcmFormat.matches(pcm.getFormat())) {
				pcm = AudioUtils.convertAudioStream(pcmFormat, pcm);
			}
			return pcm;
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static byte[] ref;
	private static AudioFormat refFormat;
	private static File mp3ref;
	private static AudioEncoder lame;

	@BeforeClass
	public static void buildReference() throws Throwable {

		// initialize Lame
		lame = new LameEncoderService().newEncoder(new HashMap<String,String>()).orElse(null);
		Assume.assumeTrue("Test can not be run because lame not present", lame != null);

		// build reference (as PCM data and as MP3 file)
		refFormat = new AudioFormat(8000, 8, 1, true, true); //8 bits signed big-endian (for easy comparisons)
		// not using AudioDecoder because audioServices object is not static
		AudioInputStream stream = AudioSystem.getAudioInputStream(LameTest.class.getResource("/blah.wav"));
		if (!stream.getFormat().matches(refFormat))
			stream = AudioUtils.convertAudioStream(refFormat, stream);
		ref = ByteStreams.toByteArray(stream);
		mp3ref = File.createTempFile("ref", ".mp3");
		mp3ref.deleteOnExit();
		lame.encode(
			AudioUtils.createAudioStream(refFormat, ref),
			MP3,
			mp3ref);
	}

	private void test(AudioFormat sourceFormat) throws AssertionError, Throwable {

		//create a new version of the PCM reference encoded with sourceFormat
		AudioInputStream audio = mp3ToPCM(sourceFormat, mp3ref);

		//use lame to convert it to MP3
		File lameMp3 = File.createTempFile("lametest", ".mp3");
		lameMp3.deleteOnExit();
		lame.encode(audio, MP3, lameMp3);

		//convert it back to PCM in order to compare them
		byte[] lameAudio = ByteStreams.toByteArray(mp3ToPCM(refFormat, lameMp3));

		//compare
		//TODO: proper convolution or frequency-based comparison (after FFT)
		byte[] small = ref;
		byte[] big = lameAudio;

		// FIXME: There is a difference in length due to padding added by Lame (not by the MP3
		// decoder, this has been checked with ffmpeg). The exact amount of padding is not very
		// predictable. It seems that the padding at the beginning of the file is always bigger than
		// the padding at the end, and depends on the sampling rate.
		int diff = big.length - small.length;
		Assert.assertTrue(diff >= 0);
		if (diff > 3000) // 3000 bytes = 0.375 sec
			Assert.fail("size differs too much: " + big.length + " != " + small.length);
		int window = Math.min(ref.length, lameAudio.length);
		long minerror = Long.MAX_VALUE;
		long minerror_padding = Long.MAX_VALUE;
		for (int d = 0; d < diff; d += 2) {
			long e = 0;
			long e_padding = 0;
			for (int i = 0; i < d; i++)
				e_padding += Math.abs(big[i]);
			for (int i = d; i < d + window; i++)
				e += Math.abs(big[i] - small[i - d]);
			for (int i = d + window; i < big.length; i++)
				e_padding += Math.abs(big[i]);
			if (e < minerror) {
				minerror = e;
				minerror_padding = e_padding;
			}
		}
		double error = ((double)minerror) / window;
		double error_padding = ((double)minerror_padding) / diff;
		if (error >= 3.6) // 3.6 = 1.4 % of 256
			Assert.fail("data differs");
		if (error_padding >= 0.05)
			Assert.fail("data differs");
	}

	@Test
	public void sampleRate8Khz() throws Throwable {
		test(new AudioFormat(8000, 8, 1, true, true));
	}

	@Test
	public void sampleRate16Khz() throws Throwable {
		test(new AudioFormat(16000, 8, 1, true, true));
	}

	@Test
	public void sampleRate32Khz() throws Throwable {
		test(new AudioFormat(32000, 8, 1, true, true));
	}

	@Test
	public void sampleRate48Khz() throws Throwable {
		test(new AudioFormat(48000, 8, 1, true, true));
	}

	@Test
	public void width16bits() throws Throwable {
		test(new AudioFormat(16000, 16, 1, true, true));
	}

	@Test
	public void width32bits() throws Throwable {
		test(new AudioFormat(16000, 32, 1, true, true));
	}

	@Test
	public void litteEndian16bits() throws Throwable {
		test(new AudioFormat(16000, 16, 1, true, false));
	}

	@Test
	public void unsigned8bits() throws Throwable {
		test(new AudioFormat(8000, 8, 1, false, true));
	}

	@Test
	public void unsigned16bitsBigEndian() throws Throwable {
		test(new AudioFormat(8000, 16, 1, false, true));
	}

	@Test
	public void unsigned16bitsLittleEndian() throws Throwable {
		test(new AudioFormat(8000, 16, 1, false, false));
	}

	@Test
	public void floatingpoint32bigEndian() throws Throwable {
		test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, true));
	}

	@Test
	public void floatingpoint32littleEndian() throws Throwable {
		test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 32, 1, 4, 8000, false));
	}

	@Test
	public void floatingpoint64bigEndian() throws Throwable {
		test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, true));
	}

	@Test
	public void floatingpoint64littleEndian() throws Throwable {
		test(new AudioFormat(Encoding.PCM_FLOAT, 8000, 64, 1, 8, 8000, false));
	}
}
