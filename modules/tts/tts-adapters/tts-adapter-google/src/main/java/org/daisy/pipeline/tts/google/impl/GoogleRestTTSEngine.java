package org.daisy.pipeline.tts.google.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.MarklessTTSEngine;
import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.rest.Request;
import org.daisy.pipeline.tts.scheduler.ExponentialBackoffScheduler;
import org.daisy.pipeline.tts.scheduler.FatalError;
import org.daisy.pipeline.tts.scheduler.RecoverableError;
import org.daisy.pipeline.tts.scheduler.Schedulable;
import org.daisy.pipeline.tts.scheduler.Scheduler;

import org.json.JSONObject;

/**
 * Connector class to synthesize audio using the google cloud tts engine.
 *
 * <p>This connector is based on their REST Api.</p>
 *
 * @author Louis Caille @ braillenet.org
 */
public class GoogleRestTTSEngine extends MarklessTTSEngine {

	private AudioFormat mAudioFormat;
	private Scheduler<Schedulable> mRequestScheduler;
	private int mPriority;
	private GoogleRequestBuilder mRequestBuilder;

	public GoogleRestTTSEngine(GoogleTTSService googleService, String apiKey, AudioFormat audioFormat, int priority) {
		super(googleService);
		mPriority = priority;
		mAudioFormat = audioFormat;
		mRequestScheduler = new ExponentialBackoffScheduler<Schedulable>();
		mRequestBuilder = new GoogleRequestBuilder(apiKey);
	}

	@Override
	public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence, Voice voice, TTSResource threadResources,
	                                          AudioBufferAllocator bufferAllocator, boolean retry)
			throws SynthesisException,InterruptedException, MemoryException {
	
		if (sentence.length() > 5000) {
			throw new SynthesisException("The number of characters in the sentence must not exceed 5000.");
		}

		Collection<AudioBuffer> result = new ArrayList<AudioBuffer>();

		// the sentence must be in an appropriate format to be inserted in the json query
		// it is necessary to wrap the sentence in quotes and add backslash in front of the existing quotes

		String adaptedSentence = "";

		for (int i = 0; i < sentence.length(); i++) {
			if (sentence.charAt(i) == '"') {
				adaptedSentence = adaptedSentence + '\\' + sentence.charAt(i);
			}
			else {
				adaptedSentence = adaptedSentence + sentence.charAt(i);
			}
		}

		String languageCode;
		String name;
		int indexOfSecondHyphen;

		if (voice != null) {
			//recovery of the language code in the name of the voice
			indexOfSecondHyphen = voice.name.indexOf('-', voice.name.indexOf('-') + 1);
			languageCode = voice.name.substring(0, indexOfSecondHyphen);
			name = voice.name;
		} else {
			// by default the voice is set to English
			languageCode = "en-GB";
			name = "en-GB-Standard-A";
		}

		try {
			Request<JSONObject> speechRequest = mRequestBuilder.newRequest()
				.withSampleRate((int)mAudioFormat.getSampleRate())
				.withAction(GoogleRestAction.SPEECH)
				.withLanguageCode(languageCode)
				.withVoice(name)
				.withText(adaptedSentence)
				.build();

			mRequestScheduler.launch(() -> {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(speechRequest.send(), "utf-8"));
					StringBuilder response = new StringBuilder();
					String inputLine;
					while ((inputLine = br.readLine()) != null) {
						response.append(inputLine.trim());
					}
					br.close();

					// the answer is encoded in base 64, so it must be decoded
					byte[] decodedBytes = Base64.getDecoder().decode(response.toString().substring(18, response.length()-2));

					AudioBuffer b = bufferAllocator.allocateBuffer(decodedBytes.length);
					b.data = decodedBytes;
					result.add(b);
				} catch (IOException | MemoryException e) {
					try {
						if (speechRequest.getConnection().getResponseCode() == 429) {
							throw new RecoverableError("Exceeded quotas", e);
						} else {
							throw new FatalError(e);
						}
					} catch (IOException responseCodeError) {
						throw new FatalError("could not retrieve response code for request", responseCodeError);
					}
				}
			});
		} catch (Exception e) { // include FatalError
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new SynthesisException(e.getMessage(), e.getCause());
		}

		return result;
	}

	@Override
	public AudioFormat getAudioOutputFormat() {
		return mAudioFormat;
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {

		Collection<Voice> result = new ArrayList<Voice>();

		try {

			Request<JSONObject> voicesRequest = mRequestBuilder.newRequest()
				.withAction(GoogleRestAction.VOICES)
				.build();
			mRequestScheduler.launch(() -> {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(voicesRequest.send(), "utf-8"));
					StringBuilder response = new StringBuilder();
					String inputLine;
					while ((inputLine = br.readLine()) != null) {
						response.append(inputLine.trim());
					}
					br.close();

					// voice name pattern
					Pattern p = Pattern .compile("[a-z]+-[A-Z]+-[a-z A-Z]+-[A-Z]");

					// we retrieve the names of the voices in the response returned by the API
					Matcher m = p.matcher(response);

					while (m.find()) {
						result.add(new Voice(getProvider().getName(),response.substring(m.start(), m.end())));;
					}
				} catch (IOException e) {
					try {
						if (voicesRequest.getConnection().getResponseCode() == 429) {
							throw new RecoverableError("Exceeded quotas", e);
						} else {
							throw new FatalError(e);
						}
					} catch (IOException responseCodeError) {
						throw new FatalError("could not retrieve response code of a request", responseCodeError);
					}
				}
			});

		} catch (Exception e) { // Include FatalError
			throw new SynthesisException(e.getMessage(), e.getCause());
		}

		return result;
	}

	@Override
	public int getOverallPriority() {
		return mPriority;
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException,
	InterruptedException {
		return new TTSResource();
	}

	@Override
	public int expectedMillisecPerWord() {
		// Worst case scenario with quotas:
		// the thread can wait for a bit more than a minute for a anwser
		return 64000;
	}
}
