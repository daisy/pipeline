package org.daisy.pipeline.tts.aws.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toList;

import javax.sound.sampled.AudioFormat;

import com.google.common.io.ByteStreams;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.core.exception.AbortedException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.DescribeVoicesResponse;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.Engine;
import software.amazon.awssdk.services.polly.model.TextType;
import software.amazon.awssdk.services.polly.model.VoiceId;
import software.amazon.awssdk.services.polly.model.OutputFormat;

/**
 * TTS adapter for Amazon Polly
 * @author mmartida - original author
 * @author Nicolas Pavie - updates and refactoring for DAISY Pipeline 2, version 1.15+
 */
public class AWSPollyTTSEngine extends TTSEngine {

	private final static URL SSML_TRANSFORMER = URLs.getResourceFromJAR("/transform-ssml.xsl", AWSPollyTTSEngine.class);

	private final static Logger LOGGER = LoggerFactory.getLogger(AWSPollyTTSEngine.class);

	private static final AudioFormat AF_PCM_POLLY = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 16000f, 16, 1, 2, 16000f, false);

	/**
	 * Client object provided by AWS SDK to interact with the polly engines
	 */
	private final PollyClient client;

	/**
	 * Map of AWS voices available to the user for each engine
	 */
	private final HashMap<Engine, DescribeVoicesResponse> awsVoicesDescription = new HashMap<>();

	private int mPriority;

	/**
	 * Constructor for the AWSPollyTTSEngine
	 *
	 * @param service the bound service
	 * @param accessKey the AWS account access key
	 * @param secretKey the AWS account secret key
	 * @param region the AWS region
	 * @throws SynthesisException
	 */
	public AWSPollyTTSEngine(AWSPollyTTSService service, String accessKey, String secretKey, String region, int priority)
			throws SynthesisException {

		super(service);
		this.client = PollyClient.builder()
			.credentialsProvider(() -> new AwsCredentials() {
				@Override
				public String accessKeyId() {
					return accessKey;
				}

				@Override
				public String secretAccessKey() {
					return secretKey;
				}
			})
			.region(Region.of(region))
			.build();
		this.mPriority = priority;
	}

	/**
	 * Get the list of available voices
	 *
	 * @throws SynthesisException
	 * @throws InterruptedException
	 */
	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
		synchronized (this.client){
			// Order of priorities based on
			Engine[] engines = new Engine[] {
				Engine.STANDARD,
				Engine.NEURAL,
				Engine.GENERATIVE,
				Engine.LONG_FORM,
			};
			Collection<Voice> voices = new ArrayList<>();
			if (awsVoicesDescription.isEmpty()) {
				// fetch all voices for all engines
				for (Engine engine : engines) {
					try {
						DescribeVoicesResponse awsvoices = this.client.describeVoices(b -> b.engine(engine));
						awsVoicesDescription.put(engine, awsvoices);
					} catch (Exception e) {
						LOGGER.error("Error fetching AWS voices for engine {}: {}", engine, e.getMessage());
					}
				}
			}
			for (Engine engine : engines) {
				if (awsVoicesDescription.containsKey(engine)) {
					voices.addAll(
						awsVoicesDescription.get(engine).voices()
							.stream()
							.map(i -> {
								try {
									// FIXME: also take into account additionalLanguageCodesAsStrings()
									return new Voice(getProvider().getName(),
									                 i.idAsString() + " (" + engine.toString() + ")",
									                 (new Locale.Builder()).setLanguageTag(i.languageCodeAsString().replace("_", "-")).build(),
									                 Gender.of(i.genderAsString().toLowerCase()));
								} catch (IllformedLocaleException e) {
									LOGGER.debug("Could not parse language tag: " + i.languageCodeAsString());
									return null;
								}
							})
							.filter(Objects::nonNull)
							.collect(toList())
					);
				}
			}
			return voices;
		}
	}

	private Pattern voiceIdAndEnginePattern = Pattern.compile(
		"([a-z0-9_]+)\\s*\\((standard|neural|generative|long-form)\\)",
		Pattern.CASE_INSENSITIVE);

	@Override
	public SynthesisResult synthesize(XdmNode pXdmNode, Voice pVoice, TTSRegistry.TTSResource pTTSResource)
			throws SynthesisException, InterruptedException {

		String sentence; {
			try {
				Map<String,Object> params = new HashMap<>(); {}
				sentence = transformSsmlNodeToString(pXdmNode, SSML_TRANSFORMER, params);
			} catch (IOException | SaxonApiException e) {
				throw new SynthesisException(e);
			}
		}
		try {
			// Search back engine and voice from voiceId
			Matcher m = voiceIdAndEnginePattern.matcher(pVoice.getName());
			if (!m.matches()) throw new SynthesisException(
				"AWS VoiceId and engine not found from voice name: " + pVoice.getName());
			VoiceId selectedVoice = VoiceId.fromValue(m.group(1));
			Engine selectedEngine = Engine.fromValue(m.group(2));
			if (selectedVoice == null) {
				throw new SynthesisException("Unknown Voice provided in : " + pVoice.getName());
			}
			if (selectedEngine == null) {
				throw new SynthesisException("Unknown Engine provided in " + pVoice.getName());
			}
			SynthesizeSpeechRequest synthReq = SynthesizeSpeechRequest.builder()
				.engine(selectedEngine)
				.text(sentence)
				.textType(TextType.SSML)
				.voiceId(selectedVoice)
				.outputFormat(OutputFormat.PCM)
				.build();
			return new SynthesisResult(
				createAudioStream(
					AF_PCM_POLLY, new ByteArrayInputStream(
						ByteStreams.toByteArray(this.client.synthesizeSpeech(synthReq))
					)
				)
			);
		} catch (IOException ex) {
			throw new SynthesisException("Could not synthesize sentence \"" + sentence + "\"", ex);
		} catch (AbortedException e) {
			if (Thread.currentThread().isInterrupted()) {
				InterruptedException ex = new InterruptedException();
				ex.initCause(e);
				throw ex;
			} else {
				throw e;
			}
		}
	}

	@Override
	public int getOverallPriority() {
		return mPriority;
	}

	@Override
	public int expectedMillisecPerWord() {
		// Worst case scenario with quotas:
		// the thread can wait for a bit more than a minute for a anwser
		return 10000;
	}
}
