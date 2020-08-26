package org.daisy.pipeline.tts.att;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.BasicSSMLAdapter;
import org.daisy.pipeline.tts.LoadBalancer.Host;
import org.daisy.pipeline.tts.RoundRobinLoadBalancer;
import org.daisy.pipeline.tts.SSMLAdapter;
import org.daisy.pipeline.tts.SSMLUtil;
import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

/**
 * This synthesizer uses directly the AT&T's client binary and intermediate WAV
 * files.
 * 
 * Before any conversion, run ATT/bin/TTSServer -m 40 -c {mPort} -config
 * your-tts-conf.cfg
 */
@Component(
	name = "attbin-tts-service",
	service = { TTSService.class }
)
public class ATTBin extends AbstractTTSService {
	private AudioFormat mAudioFormat;
	private String mATTPath;
	private int mSampleRate;
	private Pattern mMarkPattern;
	private RoundRobinLoadBalancer mLoadBalancer;
	private SSMLAdapter mSSMLAdapter;

	private static class ThreadResource extends TTSResource {
		Host host;
	}

	public void onBeforeOneExecution() throws SynthesisException {
		mSSMLAdapter = new BasicSSMLAdapter() {
			@Override
			public String getFooter() {
				return "</voice>";
			}

			@Override
			public String getHeader(String voiceName) {
				if (voiceName == null || voiceName.isEmpty()) {
					return "<voice>";
				}
				return "<voice name=\"" + voiceName + "\">";
			}
		};

		mLoadBalancer = new RoundRobinLoadBalancer(System.getProperty("org.daisy.pipeline.tts.att.servers",
		        "localhost:8888"), null);
		mMarkPattern = Pattern
		        .compile("([0-9]+)\\s+BOOKMARK:\\s+([^\\s]+)", Pattern.MULTILINE);
		mSampleRate = 16000;
		mAudioFormat = new AudioFormat(mSampleRate, 16, 1, true, false);

		final String property = "org.daisy.pipeline.tts.att.client.path";
		mATTPath = System.getProperty(property);
		if (mATTPath == null) {
			Optional<String> apath = BinaryFinder.find("TTSClientFile");
			if (!apath.isPresent()) {
				throw new SynthesisException("Cannot find AT&T's client in PATH and "
				        + property + " is not set");
			}
			mATTPath = apath.get();
		}

		//test the synthesizer so that the service won't be active if it fails
		Host host = mLoadBalancer.getMaster();
		RawAudioBuffer testBuffer = new RawAudioBuffer();
		testBuffer.offsetInOutput = 0;
		testBuffer.output = new byte[1];
		try {
			synthesize("test", testBuffer, host, new LinkedList<Map.Entry<String, Integer>>());
		} catch (InterruptedException e) {
			throw new SynthesisException(e);
		}
		if (testBuffer.offsetInOutput <= 0) {
			throw new SynthesisException("AT&T client binary did not produce any audio data");
		}
	}

	@Override
	public void synthesize(XdmNode ssml, Voice voice, RawAudioBuffer audioBuffer,
	        Object resources, List<Entry<String, Integer>> marks, boolean retry)
	        throws SynthesisException, InterruptedException {
		synthesize(SSMLUtil.toString(ssml, voice.name, mSSMLAdapter, endingMark()),
		        audioBuffer, resources, marks);
	}

	private void synthesize(String ssml, RawAudioBuffer audioBuffer, Object resources,
	        List<Entry<String, Integer>> marks) throws SynthesisException,
	        InterruptedException {
		ThreadResource th = (ThreadResource) resources;
		File dest;
		try {
			dest = File.createTempFile("attbin", ".wav");
		} catch (IOException e) {
			throw new SynthesisException(e.getMessage(), e.getCause());
		}

		Process p = null;
		String[] cmd = null;
		try {
			cmd = new String[]{
			        mATTPath, "-ssml", "-v0", "-s", th.host.address, "-p",
			        String.valueOf(th.host.port), "-r", String.valueOf(mSampleRate), "-o",
			        dest.getAbsolutePath()
			};

			p = Runtime.getRuntime().exec(cmd);
			p.getOutputStream().write(ssml.getBytes("UTF-8"));
			p.getOutputStream().close();

			InputStream is = p.getInputStream();
			Scanner scanner = new Scanner(is);
			while (scanner.findWithinHorizon(mMarkPattern, 0) != null) {
				MatchResult mr = scanner.match();
				int bytes = (mAudioFormat.getSampleSizeInBits() * Integer.valueOf(mr.group(1))) / 8;
				marks.add(new AbstractMap.SimpleEntry<String, Integer>(mr.group(2), bytes));
			}
			is.close();
			p.waitFor();
		} catch (InterruptedException e) {
			dest.delete();
			if (p != null)
				p.destroy();
			throw e;
		} catch (Exception e) {
			dest.delete();
			if (p != null)
				p.destroy();
			throw new SynthesisException(e.getMessage(), e.getCause());
		}

		// read the audio data from the resulting WAV file
		try {
			SoundUtil.readWave(dest, audioBuffer);
		} catch (Exception e) {
			throw new SynthesisException(e.getMessage(), e.getCause());
		} finally {
			dest.delete();
		}
	}

	@Override
	public AudioFormat getAudioOutputFormat() {
		return mAudioFormat;
	}

	@Override
	public String getName() {
		return "att";
	}

	@Override
	public TTSResource allocateThreadResources() {
		ThreadResource th = new ThreadResource();
		th.host = mLoadBalancer.selectHost();
		return th;
	}

	@Override
	public String getVersion() {
		return "command-line";
	}

	@Override
	public void onAfterOneExecution() {
		mLoadBalancer = null;
		mMarkPattern = null;
		mAudioFormat = null;
		mSSMLAdapter = null;
	}

	@Override
	public int getOverallPriority() {
		return Integer.valueOf(System.getProperty("org.daisy.pipeline.tts.att.bin.priority", "5"));
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException {
		Set<Voice> result = new HashSet<Voice>();
		//The client binary has no option to list all the voices, therefore we must
		//iterate over the possible voices and check if they are accepted.
		//WARNING: all the AT&T servers are assumed to be configured with the same voices.
		Host host = mLoadBalancer.getMaster();
		String[] cmd = null;
		Pattern voicePattern = Pattern.compile("VOICE:\\s([^;]+)");
		Matcher mr = voicePattern.matcher("");

		File dest;
		try {
			dest = File.createTempFile("atttest", ".wav");
		} catch (IOException e1) {
			return null;
		}

		try {
			for (VoiceInfo voiceInfo : TTSRegistry.getAllPossibleVoices()) {
				if (voiceInfo.voice.vendor.equalsIgnoreCase("att")) {
					cmd = new String[]{
					        mATTPath, "-ssml", "-v0", "-s", host.address, "-p",
					        String.valueOf(host.port), "-o", dest.getAbsolutePath()
					};

					Process p = Runtime.getRuntime().exec(cmd);
					p.getOutputStream().write(
					        ("<voice name=\"" + voiceInfo.voice.name + "\">t</voice>")
					                .getBytes());
					p.getOutputStream().close();
					InputStream is = p.getInputStream();
					Scanner scanner = new Scanner(is);
					while (scanner.hasNextLine()) {
						mr.reset(scanner.nextLine());
						if (mr.find()) {
							result.add(new Voice(getName(), mr.group(1)));
							break;
						}
					}
					is.close();
					p.waitFor();
				}
			}
		} catch (Exception e) {
			throw new SynthesisException(e.getMessage(), e.getCause());
		} finally {
			dest.delete();
		}

		return result;
	}

	@Override
	public String endingMark() {
		return "ending-mark";
	}
}
