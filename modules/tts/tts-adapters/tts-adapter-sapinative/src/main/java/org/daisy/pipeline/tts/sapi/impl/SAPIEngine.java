package org.daisy.pipeline.tts.sapi.impl;

import java.io.ByteArrayInputStream;
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
import org.daisy.pipeline.tts.onecore.Onecore;
import org.daisy.pipeline.tts.onecore.OnecoreResult;
import org.daisy.pipeline.tts.onecore.SAPI;
import org.daisy.pipeline.tts.onecore.SAPIResult;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
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
	private final boolean onecoreIsReady;
	private final AudioFormat sapiAudioFormat;

	private Map<String, Voice> mVoiceFormatConverter = null;

	private static class ThreadResource extends TTSResource {
		long onecoreConnection;
		long SAPIConnection;
	}

	/**
	 * SAPI and Onecore TTS engine
	 * @param service is the service managing this engine
	 * @param priority is the priority level in the selection of this engine for TTS
	 * @param onecoreIsReady should be true if Onecore was correctly loaded and initialized before creating the engine.
	 * @param sapiAudioFormat should be the audio format set by the SAPI initialization.<br/>
	 *                        It should be null if SAPI could not be loaded and/or initialized.
	 */
	public SAPIEngine(SAPIService service, int priority, boolean onecoreIsReady, AudioFormat sapiAudioFormat) {
		super(service);
		this.onecoreIsReady = onecoreIsReady;
		this.sapiAudioFormat = sapiAudioFormat;
		mOverallPriority = priority;
	}

	@Override
	public boolean handlesMarks() {
		return true;
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice, TTSResource resource)
			throws SynthesisException {

		Map<String,Object> xsltParams = new HashMap<>(); {
			xsltParams.put("voice", voice.getName());
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

		voice = mVoiceFormatConverter.get(voice.getName().toLowerCase());
		ThreadResource tr = (ThreadResource)resource;
		if (voice.getEngine().equals("sapi")) {
			try {
				int res = SAPI.speak(tr.SAPIConnection, voice.getEngine(), voice.getName(), ssml);
				if (res != SAPIResult.SAPINATIVE_OK.value()) {
					throw new SynthesisException("SAPI-legacy speak error " + res + " raised with voice "
							+ voice + ": " +  SAPIResult.valueOfCode(res)+"\nFor text :"
							+ ssml);
				}
			} catch (RuntimeException e){
				Logger.error("SAPI-legacy raised a RUNTIME exception while speaking " + ssml + " with " + voice + " : " + e.getMessage());
				throw new SynthesisException("SAPI-legacy raised a RUNTIME exception while speaking " + ssml + " with " + voice, e);
			} catch (Exception e){
				Logger.error("SAPI-legacy raised an exception while speaking " + ssml + " with " + voice + " : " + e.getMessage());
				throw new SynthesisException("SAPI-legacy raised an exception while speaking " + ssml + " with " + voice, e);
			}

			int size = SAPI.getStreamSize(tr.SAPIConnection);
			byte[] data = new byte[size];
			SAPI.readStream(tr.SAPIConnection, data, 0);

			String[] names = SAPI.getBookmarkNames(tr.SAPIConnection);
			long[] positions = SAPI.getBookmarkPositions(tr.SAPIConnection);
			float sampleRate = sapiAudioFormat.getSampleRate();
			int bytesPerSample = sapiAudioFormat.getSampleSizeInBits() / 8;
			for (int i = 0; i < names.length; ++i) {
				int offset = (int) ((positions[i] * sampleRate * bytesPerSample) / 1000);
				// it happens that SAPI / OneCore sometimes make empty bookmarks (for unknown reason)
				if (names[i].length() > 0){
					marks.add(offset);
				}
			}
			return createAudioStream(sapiAudioFormat, data);
		} else { // use onecore engine
			try {
				int res = Onecore.speak(tr.onecoreConnection, voice.getEngine(), voice.getName(), ssml);
				if (res != OnecoreResult.SAPINATIVE_OK.value()) {
					throw new SynthesisException("SAPI-Onecore speak error " + res + " raised with voice "
							+ voice + ": " +  OnecoreResult.valueOfCode(res)+"\nFor text :"
							+ ssml);
				}
			} catch (IOException e) {
				Logger.error("SAPI-onecore raised an exception while speaking " + ssml + " with " + voice + " : " + e.getMessage());
				throw new SynthesisException("SAPI-Onecore raised an exception while speaking " + ssml + " with " + voice, e);
			}

			int size = Onecore.getStreamSize(tr.onecoreConnection);
			byte[] data = new byte[size];
			Onecore.readStream(tr.onecoreConnection, data, 0);
			String[] names = Onecore.getBookmarkNames(tr.onecoreConnection);
			long[] pos = Onecore.getBookmarkPositions(tr.onecoreConnection);
			AudioInputStream result;
			try {
				result = createAudioStream(new ByteArrayInputStream(data));
			} catch (Exception e) {
				throw new SynthesisException(e);
			}
			AudioFormat resultFormat = result.getFormat();
			float sampleRate = resultFormat.getSampleRate();
			int bytesPerSample = resultFormat.getSampleSizeInBits() / 8;
			for (int i = 0; i < names.length; ++i) {
				int offset = (int) ((pos[i] * sampleRate * bytesPerSample) / 1000);
				// it happens that SAPI / OneCore sometimes make empty bookmarks (for unknown reason)
				if (names[i].length() > 0){
					marks.add(offset);
				}
			}
			return result;
		}
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException {
		ThreadResource tr = new ThreadResource();
		if (this.onecoreIsReady){
			long connection = Onecore.openConnection();
			if (connection == 0) {
				throw new SynthesisException("could not open SAPI-Onecore context.");
			}
			tr.onecoreConnection = connection;
		}
		if (this.sapiAudioFormat != null){
			try {
				long connection = SAPI.openConnection();
				if (connection == 0) {
					throw new IOException("could not connect to SAPI-Legacy context.");
				}
				tr.SAPIConnection = connection;
			} catch (IOException e) {
				throw new SynthesisException("could not open SAPI-Legacy context.", e);
			}


		}
		return tr;
	}

	@Override
	public Collection<Voice> getAvailableVoices() {
		if (mVoiceFormatConverter == null) {
			mVoiceFormatConverter = new HashMap<>();
			ArrayList<String> names = new ArrayList<>();
			ArrayList<String> vendors = new ArrayList<>();
			ArrayList<String> locale = new ArrayList<>();
			ArrayList<String> gender = new ArrayList<>();
			ArrayList<String> age = new ArrayList<>();
			if (this.sapiAudioFormat != null){
				// first load sapi voices
				names.addAll(Arrays.asList(SAPI.getVoiceNames()));
				vendors.addAll(Arrays.asList(SAPI.getVoiceVendors()));
				locale.addAll(Arrays.asList(SAPI.getVoiceLocales()));
				gender.addAll(Arrays.asList(SAPI.getVoiceGenders()));
				age.addAll(Arrays.asList(SAPI.getVoiceAges()));
			}
			if (this.onecoreIsReady){
				// then load onecore voices
				names.addAll(Arrays.asList(Onecore.getVoiceNames()));
				vendors.addAll(Arrays.asList(Onecore.getVoiceVendors()));
				locale.addAll(Arrays.asList(Onecore.getVoiceLocales()));
				gender.addAll(Arrays.asList(Onecore.getVoiceGenders()));
				age.addAll(Arrays.asList(Onecore.getVoiceAges()));
			}
			for (int i = 0; i < names.size(); ++i) {
				String currentGender = gender.get(i);
				String currentAge = age.get(i);
				// Default selection
				Gender selected = Gender.FEMALE_ADULT;
				switch (currentGender.toLowerCase()) {
					case "male":
						switch(currentAge.toLowerCase()){
							 // i have no example of child and elderly voice attribute for now
							case "child" :
								selected = Gender.MALE_CHILD;
								break;
							case "elderly" :
								selected = Gender.MALE_ELDERLY;
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
								selected = Gender.FEMALE_ELDERLY;
								break;
							case "adult" : // default to adult
							default:
								break;
						}
						break;
				}
				// merge the sapi and onecore lists using the keys
				// Note that since onecore voice are added after sapi,
				// they are overwriting matching sapi voices to avoid duplicates
				try {
					// remove the "desktop" extension of SAPI legacy microsoft voices
					// So that onecore voices are used instead if available
					String key = names.get(i).toLowerCase();
					if (key.endsWith(" desktop")) {
						key = key.substring(0,key.length() - " desktop".length());
					}
					mVoiceFormatConverter.put(
						key,
						new Voice(
							vendors.get(i),
							names.get(i),
							VoiceInfo.tagToLocale(locale.get(i)),
							selected
						)
					);
				} catch (VoiceInfo.UnknownLanguage e) {
					Logger.debug("Not a valid locale: " + locale.get(i));
				}
			}
		}
		List<Voice> voices = new ArrayList<>();
		for (String sapiVoice : mVoiceFormatConverter.keySet()) {
			Voice original = mVoiceFormatConverter.get(sapiVoice);
			voices.add(
				new Voice(
					original.getEngine(), // should be sapi or onecore, required for speak interactions (see the native libs code)
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
