package org.daisy.pipeline.tts.osx.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

public class OSXSpeechEngine extends TTSEngine {

	private final String mSayPath;
	private final int mPriority;

	private final static URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", OSXSpeechEngine.class);
	private final static Logger mLogger = LoggerFactory.getLogger(OSXSpeechEngine.class);

	public OSXSpeechEngine(OSXSpeechService service, String osxPath, int priority) {
		super(service);
		mPriority = priority;
		mSayPath = osxPath;
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource threadResources)
			throws SynthesisException, InterruptedException {
		
		String sentence; {
			Map<String,Object> xsltParams = new HashMap<>(); {
				xsltParams.put("voice", voice.name);
			}
			try {
				sentence = transformSsmlNodeToString(ssml, ssmlTransformer, xsltParams);
			} catch (IOException | SaxonApiException e) {
				throw new SynthesisException(e);
			}
		}
		File waveOut = null;
		try {
			waveOut = File.createTempFile("pipeline", ".wav");
			new CommandRunner(mSayPath, "--data-format=LEI16@22050", "-o",
			                  waveOut.getAbsolutePath(), "-v", voice.name)
				.feedInput(sentence.getBytes("utf-8"))
				.consumeError(mLogger)
				.run();
			
			// read the wave on the standard output
			return new SynthesisResult(
				createAudioStream(
					new FileInputStream(waveOut)));
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new SynthesisException(e);
		} finally {
			if (waveOut != null)
				waveOut.delete();
		}
	}

		@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException {

		Collection<Voice> result = new ArrayList<Voice>();
		try {
			new CommandRunner(mSayPath, "-v", "?")
				.consumeOutput(stream -> {
						Matcher mr = Pattern.compile("(?<name>.*?)\\s+(?<locale>\\w{2}_\\w{2})\\s*(#.*)?").matcher("");
						try (Scanner scanner = new Scanner(stream)) {
							while (scanner.hasNextLine()) {
								String line = scanner.nextLine();
								mr.reset(line);
								if (mr.find()) {
									String name = mr.group("name").trim();
									try {
										Locale locale = VoiceInfo.tagToLocale(mr.group("locale"));
										// Note that we could also maintain (hard-code) a mapping from voice to gender
										Gender unknownGender = Gender.ANY;
										result.add(new Voice(getProvider().getName(), name, locale, unknownGender));
									} catch (VoiceInfo.UnknownLanguage e) {
										mLogger.debug("Could not parse line from `say -v ?' output: " + line);
										mLogger.debug("Reason: could not parse locale: " + mr.group("locale"));
										result.add(new Voice(getProvider().getName(), name));
									}
								} else {
									mLogger.debug("Could not parse line from `say -v ?' output: " + line);
								}
							}
						}
					}
				)
				.consumeError(mLogger)
				.run();
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

}
