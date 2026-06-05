package org.daisy.pipeline.tts.sapi.impl;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.sapinative.NativeSynthesisResult;
import org.daisy.pipeline.tts.sapinative.SAPI;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unified SAPI/OneCore TTS engine
 */
public class SAPIEngine extends TTSEngine {

	private static final Logger Logger = LoggerFactory.getLogger(SAPIEngine.class);
	private static final URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", SAPIEngine.class);

	private final int mOverallPriority;
	private final AudioFormat sapiAudioFormat;
	private final float speechRate;

	private Map<String, Voice> mVoiceFormatConverter = null;

	/**
	 * SAPI and Onecore TTS engine
	 * @param service is the service managing this engine
	 * @param priority is the priority level in the selection of this engine for TTS
	 * @param sapiAudioFormat should be the audio format set by the SAPI initialization.<br/>
	 *                        It should be null if SAPI could not be loaded and/or initialized.
	 */
	public SAPIEngine(SAPIService service, int priority, AudioFormat sapiAudioFormat, float speechRate) {
		super(service);
		this.sapiAudioFormat = sapiAudioFormat;
		this.speechRate = speechRate;
		mOverallPriority = priority;
	}

	@Override
	public boolean handlesMarks() {
		return true;
	}

	@Override
	public boolean handlesSpeakingRate() {
		return true;
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource resource)
			throws SynthesisException {

		Map<String,Object> xsltParams = new HashMap<>(); {
			xsltParams.put("voice", voice.getName());
			xsltParams.put("speech-rate", speechRate);
		}
		try {
			List<Integer> marks = new ArrayList<>();
			String ssmlForEngine = transformSsmlNodeToString(ssml, ssmlTransformer, xsltParams);
			AudioInputStream audio = speak(ssmlForEngine, voice, resource, marks);
			return new SynthesisResult(audio, marks);
		} catch (IOException|SaxonApiException e) {
			throw new SynthesisException(e);
		}
	}

	public AudioInputStream speak(String ssml, Voice voice, TTSResource resource, List<Integer> marks)
			throws SynthesisException {

		String key = voice.getName().toLowerCase();
		// To avoid using a SAPI voice when a OneCore voice is available, the "desktop" suffix is
		// removed from keys in getAvailableVoices(), so need to do the same here.
		if (key.endsWith(" desktop"))
			key = key.substring(0, key.length() - " desktop".length());
		voice = mVoiceFormatConverter.get(key); // mVoiceFormatConverter was initialized in getAvailableVoices()
		NativeSynthesisResult res;
		// Speak
		try {
			res = SAPI.speak(voice.getEngine(), voice.getName(), ssml, (int)sapiAudioFormat.getSampleRate(), (short)sapiAudioFormat.getSampleSizeInBits());
		} catch (RuntimeException e){
			Logger.error("SAPI-legacy raised a RUNTIME exception while speaking " + ssml + " with " + voice + " : " + e.getMessage());
			throw new SynthesisException("SAPI-legacy raised a RUNTIME exception while speaking " + ssml + " with " + voice, e);
		} catch (Exception e){
			Logger.error("SAPI-legacy raised an exception while speaking " + ssml + " with " + voice + " : " + e.getMessage());
			throw new SynthesisException("SAPI-legacy raised an exception while speaking " + ssml + " with " + voice, e);
		}
		if(res == null){
			throw new SynthesisException("No synthesis result were returned while speaking " + ssml + " with " + voice);
		}
		try {
			AudioInputStream result;
			result = createAudioStream(sapiAudioFormat, res.getStreamData());
			AudioFormat resultFormat = result.getFormat();
			float sampleRate = resultFormat.getSampleRate();
			int bytesPerSample = resultFormat.getSampleSizeInBits() / 8;
			String[] names = res.getMarksNames();
			long[] positions = res.getMarksPositions();
			for (int i = 0; i < names.length; ++i) {
				// it happens that SAPI / OneCore sometimes make empty bookmarks (for unknown reason)
				if (names[i].length() > 0){
					marks.add((int)positions[i]);
				}
			}
			return result;
		} catch (Exception e){
			throw new SynthesisException("Exception raised while reconstructing AudioStream after speaking " + ssml + " with " + voice, e);
		}
	}

	/**
	 * Return voices form both SAPI and OneCore text-to-speech underlying engines.
	 *
	 * SAPI voices are listed first to prioritize third party voices that could have been installed
	 * before (like Acapela voices for Windows or other third parties). At this time, we are not
	 * aware of third party voices distributed for OneCore.
	 *
	 * SAPI voices that have a OneCore version installed are discarded in favor of their OneCore
	 * version.
	 *
	 * @return the list of all available voices through SAPI and OneCore engines
	 */
	@Override
	public Collection<Voice> getAvailableVoices() {
		if (mVoiceFormatConverter == null) {
			mVoiceFormatConverter = new HashMap<>();
			ArrayList<Voice> nativeVoices = new ArrayList<>();
			if (this.sapiAudioFormat != null) {
				try {
					// first load sapi voices
					nativeVoices.addAll(Arrays.asList(SAPI.getVoices()));
				} catch (Exception e){
					Logger.debug("Could not retrieve SAPI voices : " + e.getMessage());
				}
			}
			for (Voice v : nativeVoices) {
				String key = v.getName().toLowerCase();
				// To avoid using a SAPI voice when a OneCore voice is available, the "desktop"
				// suffix needs to be removed
				if (key.endsWith(" desktop"))
					key = key.substring(0, key.length() - " desktop".length());
				mVoiceFormatConverter.put(key, v);
			}
		}
		List<Voice> voices = new ArrayList<>();
		for (String sapiVoice : mVoiceFormatConverter.keySet()) {
			Voice original = mVoiceFormatConverter.get(sapiVoice);
			voices.add(
				new Voice(
					original.getEngine(), // should be sapi or onecore, required for speak interactions (see the native libs code)
					original.getName(),
					original.getLocale(),
					original.getGender().get(),
					original.getMarkSupport()
				)
			);
		}
		return voices;
	}

	@Override
	public int getOverallPriority() {
		return mOverallPriority;
	}

	@Override
	public int expectedMillisecPerWord() {
		return 1000;
	}
}
