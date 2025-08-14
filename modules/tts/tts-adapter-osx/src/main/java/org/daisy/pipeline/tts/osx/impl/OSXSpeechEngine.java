package org.daisy.pipeline.tts.osx.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IllformedLocaleException;
import java.util.Locale;

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

import org.rococoa.contrib.appkit.NSSpeechSynthesizer;
import org.rococoa.contrib.appkit.NSVoice;

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
			try {
				sentence = transformSsmlNodeToString(ssml, ssmlTransformer, null);
			} catch (IOException | SaxonApiException e) {
				throw new SynthesisException(e);
			}
		}
		File waveOut = null;
		try {
			waveOut = File.createTempFile("pipeline", ".wav");
			new CommandRunner(mSayPath, "--data-format=LEI16@22050", "-o",
			                  waveOut.getAbsolutePath(), "-v", voice.getName())
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
			for (NSVoice voice : NSSpeechSynthesizer.availableVoices()) {
				try {
					Locale locale = (new Locale.Builder()).setLanguageTag(voice.getLocaleIdentifier().replace("_", "-")).build();
					Gender gender; {
						switch (voice.getGender()) {
						case Male:
							gender = Gender.MALE_ADULT;
							break;
						case Female:
							gender = Gender.FEMALE_ADULT;
							break;
						case Neuter:
						default:
							gender = Gender.ANY;
							break;
						}
					}
					result.add(new Voice(getProvider().getName(), voice.getName(), locale, gender));
				} catch (IllformedLocaleException e) {
					mLogger.debug("Could not parse locale: " + voice.getLocaleIdentifier());
					result.add(new Voice(getProvider().getName(), voice.getName()));
				}
			}
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
