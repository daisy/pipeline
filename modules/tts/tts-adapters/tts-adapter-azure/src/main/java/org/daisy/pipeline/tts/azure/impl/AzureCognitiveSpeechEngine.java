package org.daisy.pipeline.tts.azure.impl;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import com.google.common.io.ByteStreams;

import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.audio.AudioOutputStream;
import com.microsoft.cognitiveservices.speech.audio.PullAudioOutputStream;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.SynthesisVoicesResult;
import com.microsoft.cognitiveservices.speech.VoiceInfo;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.scheduler.ExponentialBackoffScheduler;
import org.daisy.pipeline.tts.scheduler.FatalError;
import org.daisy.pipeline.tts.scheduler.RecoverableError;
import org.daisy.pipeline.tts.scheduler.Schedulable;
import org.daisy.pipeline.tts.scheduler.Scheduler;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import static org.daisy.pipeline.tts.VoiceInfo.tagToLocale;
import org.daisy.pipeline.tts.VoiceInfo.Gender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AzureCognitiveSpeechEngine extends TTSEngine {

	private static final Logger logger = LoggerFactory.getLogger(AzureCognitiveSpeechEngine.class);
	private static final URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", AzureCognitiveSpeechEngine.class);

	// 22,05 kHz sampling rate, 16-bit depth, mono (signed, little endian)
	private static final AudioFormat audioFormat = new AudioFormat(22050f, 16, 1, true, false);
	private static final SpeechSynthesisOutputFormat synthOutputFormat = SpeechSynthesisOutputFormat.Raw22050Hz16BitMonoPcm;
	private static final float sampleRate = audioFormat.getSampleRate();
	private static final int bytesPerSample = audioFormat.getSampleSizeInBits() / 8;
	private static final Scheduler<Schedulable> retry = new ExponentialBackoffScheduler<Schedulable>();

	private final int threads;
	private final int priority;
	private final SpeechConfig speechConfig;

	public AzureCognitiveSpeechEngine(AzureCognitiveSpeechService service, String key, String region, int threads, int priority) {
		super(service);
		this.threads = threads;
		this.priority = priority;
		speechConfig = SpeechConfig.fromSubscription(key, region);
		speechConfig.setSpeechSynthesisOutputFormat(synthOutputFormat);
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource threadResources) throws SynthesisException,
	                                                                                                 InterruptedException {
		String sentence; {
			try {
				Map<String,Object> params = new HashMap<>(); {
					params.put("voice", voice.getName());
				}
				sentence = transformSsmlNodeToString(ssml, ssmlTransformer, params);
			} catch (IOException|SaxonApiException e) {
				throw new SynthesisException(e);
			}
		}
		try (PullAudioOutputStream stream = AudioOutputStream.createPullStream()) {
			List<Integer> marks = new ArrayList<>();
			try (
				AudioConfig audioConfig = AudioConfig.fromStreamOutput(stream);
				SpeechSynthesizer synth = new SpeechSynthesizer(speechConfig, audioConfig)
			) {
				synth.BookmarkReached.addEventListener((sender, mark) -> {
						long offset = mark.getAudioOffset(); // audio offset in ticks (1 tick = 100 ns)
						// convert to bytes
						// (1 sample = 1/22050 s = 45,35 µs = 453,5 tick)
						// (1 byte = 1/2 sample = 22,68 µs = 226,8 tick)
						offset *= bytesPerSample;
						offset /= 1000;
						offset *= sampleRate;
						offset /= 10000;
						marks.add(Math.toIntExact(offset));
					});
				// Retry after a delay if the number of parallel requests exceeds the number of allowed concurrent
				// transcriptions for the subscription. Increase the delay exponentially. See also:
				// - https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-services-quotas-and-limits#general-best-practices-to-mitigate-throttling-during-autoscaling
				// - https://learn.microsoft.com/en-us/azure/architecture/patterns/retry
				retry.launch(() -> {
						try (
							SpeechSynthesisResult result = synth.SpeakSsmlAsync(sentence).get() // get() throws InterruptedException
						) {
							switch (result.getReason()) {
							case SynthesizingAudioCompleted:
								// success
								return;
							case Canceled:
								SpeechSynthesisCancellationDetails cancellation = SpeechSynthesisCancellationDetails.fromResult(result);
								switch (cancellation.getReason()) {
								case Error:
									switch (cancellation.getErrorCode()) {
									case TooManyRequests:
										throw new RecoverableError(
											new SynthesisException(
												"Synthesis failed: too many requests: " + cancellation.getErrorDetails()));
									default:
										throw new SynthesisException(
											"Synthesis failed: " + cancellation.getErrorCode() + ": " + cancellation.getErrorDetails());
									}
								default:
									throw new SynthesisException("Synthesis failed: " + cancellation.getReason());
								}
							default:
								throw new SynthesisException("Synthesis failed: " + result.getReason());
							}
						} catch (SynthesisException|ExecutionException e) {
							throw new FatalError(e);
						}
					}
				);
			}
			// synthesized successfully
			byte[] pcm = ByteStreams.toByteArray(new PullAudioOutputStreamAsInputStream(stream)); // raw PCM data
			AudioInputStream audio = createAudioStream(audioFormat, pcm);
			return new SynthesisResult(audio, marks.isEmpty() ? null : marks);
		} catch (InterruptedException e) {
			throw e;
		} catch (FatalError e) {
			if (e.getCause() instanceof SynthesisException)
				throw (SynthesisException)e.getCause();
			else
				throw new SynthesisException("Synthesis failed", e.getCause());
		} catch (Throwable e) {
			throw new SynthesisException("Synthesis failed", e);
		}
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
		try (SpeechSynthesizer synth = new SpeechSynthesizer(speechConfig, null);
		     SynthesisVoicesResult result = synth.getVoicesAsync("").get() // get() throws InterruptedException
		) {
			switch (result.getReason()) {
			case VoicesListRetrieved:
				Collection<Voice> voices = new ArrayList<Voice>();
				for (VoiceInfo voice : result.getVoices()) {
					String name = voice.getShortName();
					Gender gender; {
						switch (voice.getGender()) {
						case Female:
							gender = Gender.FEMALE_ADULT;
							break;
						case Male:
							gender = Gender.MALE_ADULT;
							break;
						case Unknown:
						default:
							gender = Gender.ANY;
						}
					}
					try {
						voices.add(new Voice(getProvider().getName(), name, tagToLocale(voice.getLocale()), gender));
					} catch (UnknownLanguage e) {
						logger.debug("Could not parse locale: " + voice.getLocale() + "; skipping " + name);
					}
				}
				return voices;
			default:
				throw new SynthesisException("Failed to retrieve voices list: " + result.getReason());
			}
		} catch (InterruptedException|SynthesisException e) {
			throw e;
		} catch (Throwable e) {
			throw new SynthesisException(e);
		}
	}

	@Override
	public boolean handlesMarks() {
		return true;
	}

	@Override
	public int getOverallPriority() {
		return priority;
	}

	@Override
	public int reservedThreadNum() {
		return threads;
	}

	@Override
	public TTSResource allocateThreadResources() {
		return new TTSResource();
	}

	private static class PullAudioOutputStreamAsInputStream extends InputStream {

		private final PullAudioOutputStream stream;

		PullAudioOutputStreamAsInputStream(PullAudioOutputStream stream) {
			this.stream = stream;
		}

		@Override
		public int read() {
			byte[] b = new byte[1];
			if (read(b) < 0)
				return -1;
			return Byte.toUnsignedInt(b[0]);
		}

		@Override
		public int read(byte[] b) {
			int r = (int)stream.read(b); // return value of PullAudioOutputStream.read() can not be
			                             // longer than int because arrays are indexed by int values
			if (r == 0 && b.length > 0)
				return -1;
			return r;
		}

		@Override
		public int read(byte[] b, int off, int len) {
			if (off < 0 || len < 0 || len > b.length - off)
				throw new IndexOutOfBoundsException();
			if (len == 0)
				return 0;
			byte[] data = new byte[len];
			int r = read(data);
			if (r < 0)
				return -1;
			System.arraycopy(data, 0, b, off, r);
			return r;
		}
	}
}
