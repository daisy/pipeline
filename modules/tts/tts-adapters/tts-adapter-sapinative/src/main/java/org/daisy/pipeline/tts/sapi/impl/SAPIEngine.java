package org.daisy.pipeline.tts.sapi.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.onecore.SAPI;
import org.daisy.pipeline.tts.onecore.SAPIResult;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.Voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SAPIEngine extends TTSEngine {

	private static final Logger Logger = LoggerFactory.getLogger(SAPIEngine.class);
	private static final URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", SAPIEngine.class);

	private final AudioFormat mAudioFormat;
	private final int mOverallPriority;
	private Map<String, Voice> mVoiceFormatConverter = null;

	private static class ThreadResource extends TTSResource {
		long connection;
	}

	public SAPIEngine(SAPIservice service, AudioFormat audioFormat, int priority) {
		super(service);
		mAudioFormat = audioFormat;
		mOverallPriority = priority;
	}

	// @Override
	// public String endingMark() {
	// 	return "ending-mark";
	// }

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource resource)
		throws SynthesisException {

		Map<String,Object> xsltParams = new HashMap<>(); {
			xsltParams.put("voice", voice.name);
		}
		try {
			List<Integer> marks = new ArrayList<>();
			AudioInputStream audio = speak(transformSsmlNodeToString(ssml, ssmlTransformer, xsltParams),
			             voice, resource, marks);
			return new SynthesisResult(audio, marks);
		} catch (IOException | SaxonApiException e) {
			throw new SynthesisException(e);
		}
	}

	public AudioInputStream speak(String ssml, Voice voice, TTSResource resource,
	        List<Integer> marks) throws SynthesisException {

		voice = mVoiceFormatConverter.get(voice.name.toLowerCase());

		ThreadResource tr = (ThreadResource) resource;
		int res = SAPI.speak(tr.connection, voice.engine, voice.name, ssml);
		if (res != SAPIResult.SAPINATIVE_OK.value()) {
			throw new SynthesisException("SAPI speak error " + res + " raised with voice "
			        + voice + ": " +  SAPIResult.valueOfCode(res));
		}

		int size = SAPI.getStreamSize(tr.connection);

		byte[] data = new byte[size];
		SAPI.readStream(tr.connection, data, 0);

		String[] names = SAPI.getBookmarkNames(tr.connection);
		long[] bookmarksPositions = SAPI.getBookmarkPositions(tr.connection);

		float sampleRate = mAudioFormat.getSampleRate();
		int bytesPerSample = mAudioFormat.getSampleSizeInBits() / 8;
		for (long position : bookmarksPositions) {
			int offset = (int) ((position * sampleRate * bytesPerSample) / 1000);
			marks.add(offset);
		}
		return createAudioStream(mAudioFormat, data);
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException {
		long connection = SAPI.openConnection();

		if (connection == 0) {
			throw new SynthesisException("could not open SAPI context.");
		}

		ThreadResource tr = new ThreadResource();
		tr.connection = connection;
		return tr;
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException {
		if (mVoiceFormatConverter == null) {
			mVoiceFormatConverter = new HashMap<>();
			String[] names = SAPI.getVoiceNames();
			String[] vendors = SAPI.getVoiceVendors();
			String[] locale = SAPI.getVoiceLocales();
			String[] gender = SAPI.getVoiceGenders();
			String[] age = SAPI.getVoiceAges();
			for (int i = 0; i < names.length; ++i) {
				String currentGender = gender[i];
				String currentAge = age[i];
				Gender selected = Gender.FEMALE_ADULT;
				switch (currentGender.toLowerCase()) {
					case "male":
						switch(currentAge.toLowerCase()){
							 // i have no example of child and elderly voice attribute for now
							case "child" :
								selected = Gender.MALE_CHILD;
								break;
							case "elderly" :
								selected = Gender.MALE_ELDERY;
								break;
							case "adult" : // default to adult
							default:
								selected = Gender.MALE_ADULT;
								break;
						}
						break;
					case "female": // default to female
					default:
						switch(currentAge.toLowerCase()){
								// i have no example of child and elderly voice attribute for now
							case "child" :
								selected = Gender.FEMALE_CHILD;
								break;
							case "elderly" :
								selected = Gender.FEMALE_ELDERY;
								break;
							case "adult" : // default to adult
							default:
								break;
						}
						break;
				}
				mVoiceFormatConverter.put(
					names[i].toLowerCase(),
					new Voice(
						vendors[i],
				        names[i],
						Locale.forLanguageTag(locale[i]),
						selected
					)
				);
			}
		}
		List<Voice> voices = new ArrayList<>();
		for (String sapiVoice : mVoiceFormatConverter.keySet()) {
			Voice original = mVoiceFormatConverter.get(sapiVoice);
			voices.add(
				new Voice(
					getProvider().getName(),
					sapiVoice,
					original.getLocale().get(),
					original.getGender().get()
				)
			);
		}
		return voices;
	}

	@Override
	public int getOverallPriority() {
		return mOverallPriority;
	}
}
