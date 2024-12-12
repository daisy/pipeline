package org.daisy.pipeline.tts.google.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.common.rest.Request;
import org.daisy.pipeline.common.rest.Response;
import org.daisy.pipeline.common.rest.scheduler.ExponentialBackoffScheduler;
import org.daisy.pipeline.common.rest.scheduler.FatalError;
import org.daisy.pipeline.common.rest.scheduler.RecoverableError;
import org.daisy.pipeline.common.rest.scheduler.Schedulable;
import org.daisy.pipeline.common.rest.scheduler.Scheduler;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.Voice.MarkSupport;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.LanguageRange;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector class to synthesize audio using the google cloud tts engine.
 *
 * <p>This connector is based on their REST API.</p>
 *
 * @author Louis Caille @ braillenet.org
 */
public class GoogleRestTTSEngine extends TTSEngine {

	private final AudioFormat mAudioFormat;
	private final Scheduler<Schedulable> mRequestScheduler;
	private final int mPriority;
	private final GoogleRequestBuilder mRequestBuilder;
	private final float speechRate;

	private static final URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", GoogleRestTTSEngine.class);
	private static final Logger logger = LoggerFactory.getLogger(GoogleRestTTSEngine.class);

	public GoogleRestTTSEngine(GoogleTTSService googleService, String serverAddress, String apiKey, AudioFormat audioFormat, int priority,
	                           float speechRate) {
		super(googleService);
		mPriority = priority;
		mAudioFormat = audioFormat;
		this.speechRate = speechRate;
		mRequestScheduler = new ExponentialBackoffScheduler<Schedulable>();
		mRequestBuilder = new GoogleRequestBuilder(serverAddress, apiKey);
	}

	@Override
	public boolean handlesSpeakingRate() {
		return true;
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource threadResources)
			throws SynthesisException, InterruptedException {

		boolean ssmlSupported = !(voice != null && voice.getMarkSupport() == MarkSupport.MARK_NOT_SUPPORTED);
		String sentence; {
			try {
				Map<String,Object> params = new HashMap<>(); {
					params.put("speech-rate", speechRate);
					if (!ssmlSupported)
						params.put("mark-not-supported", true);
				}
				sentence = transformSsmlNodeToString(ssml, ssmlTransformer, params);
			} catch (IOException | SaxonApiException e) {
				throw new SynthesisException(e);
			}
		}

		if (sentence.length() > 5000) {
			throw new SynthesisException("The number of characters in the sentence must not exceed 5000.");
		}

		String languageCode;
		String name;

		if (voice != null) {
			name = voice.getName();
			// take first language code declared on voice (assume at least one is declared)
			// FIXME: take language of SSML
			languageCode = voice.getLocale().iterator().next().toString();
		} else {
			// by default the voice is set to English
			languageCode = "en-GB";
			name = "en-GB-Standard-A";
		}

		try {
			Request speechRequest; {
				GoogleRequestBuilder b = mRequestBuilder.newRequest()
					.withSampleRate((int)mAudioFormat.getSampleRate())
					.withAction(GoogleRestAction.SPEECH)
					.withLanguageCode(languageCode)
					.withVoice(name);
				b = ssmlSupported ? b.withSsml(sentence) : b.withText(sentence);
				speechRequest = b.build();
			}
			ArrayList<byte[]> result = new ArrayList<>();
			mRequestScheduler.launch(() -> {
				Response response;
				try {
					response = speechRequest.send();
				} catch (IOException e) {
					throw new FatalError(e);
				}
				if (response.status == 429)
					throw new RecoverableError("Exceeded quotas", response.exception);
				else if (response.status != 200)
					raiseFatalError(response, speechRequest);
				else if (response.body == null)
					throw new FatalError("Response body is null", response.exception);
				try {
					// assume response is JSON object with single "audioContent" string
					String audioContent = new JSONObject(response.body).getString("audioContent");
					// the answer is encoded in base 64
					byte[] decodedBytes = Base64.getDecoder().decode(audioContent);
					result.add(decodedBytes);
				} catch (JSONException e) {
					throw new FatalError("JSON could not be parsed:\n" + response.body, e);
				}
			});
			AudioInputStream audio = createAudioStream(new BufferedInputStream(new ByteArrayInputStream(result.get(0))));
			if (!audio.getFormat().matches(mAudioFormat))
				throw new IllegalStateException("Got unexpected WAV header");
			return new SynthesisResult(audio);
		} catch (InterruptedException e) {
			throw e;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			if (e instanceof FatalError)
				throw new SynthesisException(e.getMessage(), e.getCause());
			else
				throw new SynthesisException(e);
		}
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
		Collection<Voice> result = new ArrayList<Voice>();
		try {
			Request voicesRequest = mRequestBuilder.newRequest()
				.withAction(GoogleRestAction.VOICES)
				.build();
			mRequestScheduler.launch(() -> {
				Response response;
				try {
					response = voicesRequest.send();
				} catch (IOException e) {
					throw new FatalError(e);
				}
				if (response.status == 429)
					throw new RecoverableError("Exceeded quotas", response.exception);
				else if (response.status != 200)
					raiseFatalError(response, voicesRequest);
				else if (response.body == null)
					throw new FatalError("Response body is null", response.exception);
				try {
					// assume response is JSON object with single "voices" array
					JSONArray voices = new JSONObject(response.body).getJSONArray("voices");
					for (int i = 0; i < voices.length(); i++) {
						// assume elements are objects
						JSONObject voice = voices.getJSONObject(i);
						// assume "name" string is present
						String name = voice.getString("name");
						// simple regex to filter out voices that do not start with a language tag
						// for now because these voices "require a model name to be specified"
						if (!name.matches("^[a-z]{2,3}-.+"))
							continue;
						Gender gender; {
							// assume "ssmlGender" string is present
							String g = voice.getString("ssmlGender");
							if ("SSML_VOICE_GENDER_UNSPECIFIED".equals(g))
								gender = Gender.ANY;
							else {
								gender = Gender.of(g);
								if (gender == null)
									logger.debug("Could not parse gender: " + g);
							}
						}
						if (gender != null) {
							List<LanguageRange> locale = new ArrayList<>(); {
								JSONArray codes = voice.getJSONArray("languageCodes");
								if (codes != null)
									for (int j = 0; j < codes.length(); j++)
										try {
											locale.add(
												new LanguageRange(
													(new Locale.Builder()).setLanguageTag(codes.getString(j).replace("_", "-"))
													                      .build()));
										} catch (IllformedLocaleException e) {
											logger.debug("Could not parse locale: " + locale);
										}
							}
							if (locale.isEmpty())
								continue;
							result.add(new Voice(getProvider().getName(), name, locale, gender,
							                     isChirpVoice(name) ? MarkSupport.MARK_NOT_SUPPORTED
							                                        : MarkSupport.DEFAULT));
						}
					}
				} catch (JSONException e) {
					throw new FatalError("Voices list could not be parsed:\n" + response.body, e);
				}
			});
		} catch (InterruptedException e) {
			throw e;
		} catch (FatalError e) {
			throw new SynthesisException(e.getMessage(), e.getCause());
		} catch (Exception e) {
			throw new SynthesisException(e);
		}
		return result;
	}

	private static boolean isChirpVoice(String name) {
		return name.contains("-Chirp-") || name.contains("-Chirp3-");
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

	private static void raiseFatalError(Response response, Request request) throws FatalError {
		String message = "Response code " + response.status + " from " + request.getConnection().getURL();
		Throwable cause = response.exception;
		try {
			JSONObject errorJson = null; {
				if (response.error != null) {
					logger.debug("Error stream:\n" + response.error);
					errorJson = new JSONObject(response.error).getJSONObject("error");
				}
			}
			if (errorJson != null) {
				message = errorJson.getString("message");
				cause = null;
				if (message != null && message.length() > 80) {
					// provide a simplified message
					try {
						JSONArray details = errorJson.getJSONArray("details");
						if (details != null && details.length() > 0) {
							String reason = details.getJSONObject(0).getString("reason");
							if (reason != null) {
								cause = new Exception(message, cause);
								if ("API_KEY_INVALID".equals(reason))
									message = "API key not valid";
								else
									message = reason.replaceAll("_", " ");
							}
						}
					} catch (JSONException e) {
						try {
							String status = errorJson.getString("status");
							if (status != null) {
								cause = new Exception(message, cause);
								message = status.replaceAll("_", " ");
							}
						} catch (JSONException ee) {
						}
					}
				}
			}
		} catch (JSONException e) {
			logger.debug("Could not parse error", e);
		}
		throw new FatalError(message, cause);
	}
}
