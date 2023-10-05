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
import com.microsoft.cognitiveservices.speech.SpeechSynthesisBookmarkEventArgs;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisCancellationDetails;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisOutputFormat;
import com.microsoft.cognitiveservices.speech.SpeechSynthesisResult;
import com.microsoft.cognitiveservices.speech.SpeechSynthesizer;
import com.microsoft.cognitiveservices.speech.SynthesisVoicesResult;
import com.microsoft.cognitiveservices.speech.util.EventHandler;
import com.microsoft.cognitiveservices.speech.VoiceInfo;

import net.sf.saxon.dom.ElementOverNodeInfo;
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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
			BookmarkListener bookmarkListener = new BookmarkListener(getMarkNames(ssml));
			try (
				AudioConfig audioConfig = AudioConfig.fromStreamOutput(stream);
				SpeechSynthesizer synth = new SpeechSynthesizer(speechConfig, audioConfig)
			) {
				synth.BookmarkReached.addEventListener(bookmarkListener);

				// Retry after a delay if the number of parallel requests exceeds the number of allowed concurrent
				// transcriptions for the subscription. Increase the delay exponentially. See also:
				// - https://learn.microsoft.com/en-us/azure/cognitive-services/speech-service/speech-services-quotas-and-limits#general-best-practices-to-mitigate-throttling-during-autoscaling
				// - https://learn.microsoft.com/en-us/azure/architecture/patterns/retry
				retry.launch(() -> {
						bookmarkListener.reset();
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
									case BadRequest:
										throw new SynthesisException(
											"Synthesis failed: bad request: " + cancellation.getErrorDetails() + "\n"
											+ "Sentence was: " + sentence);
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
			List<Integer> marks = bookmarkListener.getMarks(pcm.length); // throws SynthesisException
			return new SynthesisResult(audio, marks);
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

	private static class BookmarkListener implements EventHandler<SpeechSynthesisBookmarkEventArgs> {

		private final List<String> markNames;
		private final List<Integer> marks;
		private final List<String> unprocessedMarkNames;
		private final Collection<String> processedMarkNames;
		private final Collection<String> skippedMarkNames;
		private IllegalStateException illegalState = null;

		/**
		 * @param markNames Marks in the input (name attributes)
		 */
		public BookmarkListener(List<String> markNames) {
			this.markNames = markNames;
			marks = new ArrayList<>();
			unprocessedMarkNames = new ArrayList<>();
			processedMarkNames = new ArrayList<>();
			skippedMarkNames = new ArrayList<>();
			unprocessedMarkNames.addAll(markNames);
		}

		public void reset() {
			marks.clear();
			unprocessedMarkNames.clear();
			processedMarkNames.clear();
			skippedMarkNames.clear();
			illegalState = null;
			unprocessedMarkNames.addAll(markNames);
		}

		public void onEvent(Object sender, SpeechSynthesisBookmarkEventArgs bookmark) {
			if (illegalState != null)
				return;
			try {
				String name = bookmark.getText();
				if (processedMarkNames.contains(name))
					throw new IllegalStateException("Mark duplicated in the output");
				if (skippedMarkNames.contains(name))
					throw new IllegalStateException("Marks do not have the same order as in the input");
				if (!unprocessedMarkNames.contains(name))
					throw new IllegalStateException("Encountered mark that does not exist in the input");
				long offset = bookmark.getAudioOffset(); // audio offset in ticks (1 tick = 100 ns)
				// convert to bytes
				// (1 sample = 1/22050 s = 45,35 µs = 453,5 tick)
				// (1 byte = 1/2 sample = 22,68 µs = 226,8 tick)
				offset *= bytesPerSample;
				offset /= 1000;
				offset *= sampleRate;
				offset /= 10000;
				while (true) {
					marks.add(Math.toIntExact(offset));
					String m = unprocessedMarkNames.remove(0);
					if (m.equals(name)) {
						processedMarkNames.add(m);
						break;
					} else
						// mark has not been encountered in the output. add a mark with the same
						// offset as the following one
						skippedMarkNames.add(m);
				}
			} catch (IllegalStateException e) {
				illegalState = e;
			}
		}

		/**
		 * @param totalLength Total length of the audio stream in bytes.
		 * @return Marks in the output (audio offsets in bytes)
		 * @throws SynthesisException if marks could not be processed correctly for some reason
		 */
		public List<Integer> getMarks(int totalLength) throws SynthesisException {
			if (illegalState != null)
				throw new SynthesisException("Synthesis failed: marks could not be processed", illegalState);
			for (String m : unprocessedMarkNames) {
				// marks were skipped from the output. add the same number of marks at the very end of the stream
				marks.add(totalLength);
			}
			return marks;
		}
	}

	/**
	 * @param ssml The sentence as an SSML node containing {@code mark} elements. It can not be
	 *             assumed that the node is a root element.
	 * @return The ordered list of mark names contained in the sentence
	 * @throws SynthesisException if the input SSML is invalid
	 */
	private static List<String> getMarkNames(XdmNode ssml) throws SynthesisException {
		List<String> marks = new ArrayList<>();
		Node ssmlElem = ElementOverNodeInfo.wrap(ssml.getUnderlyingNode());
		if (ssmlElem instanceof Element)
			;
		else if (ssmlElem instanceof Document)
			ssmlElem = ((Document)ssmlElem).getDocumentElement();
		else
			throw new IllegalArgumentException();
		NodeList markElems = ((Element)ssmlElem).getElementsByTagNameNS("http://www.w3.org/2001/10/synthesis", "mark");
		for (int i = 0; i < markElems.getLength(); i++) {
			Element markElem = (Element)markElems.item(i);
			Attr nameAttr = markElem.getAttributeNode("name");
			if (nameAttr == null)
				throw new SynthesisException("Invalid SSML: mark element does not have a name attribute");
			String name = nameAttr.getValue();
			if (marks.contains(name))
				throw new SynthesisException("Invalid SSML: mark elements do not have a unique name");
			marks.add(name);
		}
		return marks;
	}
}
