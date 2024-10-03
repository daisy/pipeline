package org.daisy.pipeline.tts.espeak.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.common.shell.CommandRunner;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ESpeakEngine extends TTSEngine {

	private final String[] mCmd;
	private final String mESpeakPath;
	private final int mPriority;

	private final static URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", ESpeakEngine.class);
	private final static Logger mLogger = LoggerFactory.getLogger(ESpeakEngine.class);

	public ESpeakEngine(ESpeakService eSpeakService, File eSpeakPath, int priority) {
		super(eSpeakService);
		mESpeakPath = eSpeakPath.getAbsolutePath();
		mPriority = priority;
		mCmd = new String[]{
		        mESpeakPath, "-m", "--stdout", "--stdin"
		};
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource threadResources)
			throws SynthesisException,InterruptedException {

		String sentence; {
			Map<String,Object> xsltParams = new HashMap<>(); {
				if (voice != null) xsltParams.put("voice", voice.getName());
			}
			try {
				sentence = transformSsmlNodeToString(ssml, ssmlTransformer, xsltParams);
			} catch (IOException | SaxonApiException e) {
				throw new SynthesisException(e);
			}
		}
		ArrayList<AudioInputStream> result = new ArrayList<>();
		try {
			new CommandRunner(mCmd)
				.feedInput(sentence.getBytes("utf-8"))
				// read the wave on the standard output
				.consumeOutput(stream -> {
						result.add(
							createAudioStream(
								stream)); })
				.consumeError(mLogger)
				.run();
			return new SynthesisResult(result.get(0));
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new SynthesisException(e);
		}
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException {
		Collection<Voice> result;
		try {
			// First: get the list of all the available languages
			Set<String> languages = new HashSet<String>();
			new CommandRunner(mESpeakPath, "--voices")
				// parse output
				.consumeOutput(stream -> {
						try (Scanner scanner = new Scanner(stream)) {
							Matcher mr = Pattern.compile("\\s*[0-9]+\\s+([-a-z]+)").matcher("");
							scanner.nextLine(); //headers
							while (scanner.hasNextLine()) {
								String line = scanner.nextLine();
								mr.reset(line);
								if (mr.find()) {
									languages.add(mr.group(1).split("-")[0]);
								} else {
									mLogger.debug("Could not parse line from `espeak --voices' output: " + line);
								}
							}
						}
					}
				)
				.consumeError(mLogger)
				.run();
			
			// Second: get the list of the voices for the found languages.
			// White spaces are not allowed in voice names
			result = new ArrayList<Voice>();
			Matcher mr = Pattern.compile(
				"^\\s*[0-9]+\\s+(?<locale>[-a-zA-Z0-9]+)\\s+((--/)?(?<gender>[FfMm-])\\s+)?(?<name>[^ ]+)"
			).matcher("");
			for (String lang : languages) {
				new CommandRunner(mESpeakPath, "--voices=" + lang)
					.consumeOutput(stream -> {
							try (Scanner scanner = new Scanner(stream)) {
								scanner.nextLine(); // headers
								while (scanner.hasNextLine()) {
									String line = scanner.nextLine();
									mr.reset(line);
									if (mr.find()) {
										String name = mr.group("name");
										try {
											Locale locale = (new Locale.Builder()).setLanguageTag(mr.group("locale").replace("_", "-")).build();
											Gender gender = "f".equals(Optional.ofNullable(mr.group("gender")).orElse("m").toLowerCase())
												? Gender.FEMALE_ADULT
												: Gender.MALE_ADULT;
											result.add(new Voice(getProvider().getName(), name, locale, gender));
										} catch (IllformedLocaleException e) {
											mLogger.debug("Could not parse line from `espeak --voices' output: " + line);
											mLogger.debug("Reason: could not parse locale: " + mr.group("locale"));
											result.add(new Voice(getProvider().getName(), name));
										}
									} else {
										mLogger.debug("Could not parse line from `espeak --voices' output: " + line);
									}
								}
							}
						}
					)
					.consumeError(mLogger)
					.run();
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			throw new SynthesisException(e);
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
}
