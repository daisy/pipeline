package org.daisy.pipeline.tts.onecore.impl;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.onecore.OnecoreLib;
import org.daisy.pipeline.tts.onecore.OnecoreLibResult;
import org.daisy.pipeline.tts.SimpleTTSEngine;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.Voice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;

public class OnecoreEngine extends SimpleTTSEngine {

	private Logger Logger = LoggerFactory.getLogger(OnecoreEngine.class);

	private AudioFormat mAudioFormat;
	private int mOverallPriority;
	private Map<String, Voice> mVoiceFormatConverter = null;
	private final static int MIN_CHUNK_SIZE = 2048;

	private static class ThreadResource extends TTSResource {
		long connection;
	}

	public OnecoreEngine(OnecoreService service, int priority) {
		super(service);
		mOverallPriority = priority;
	}

	// @Override
	// public String endingMark() {
	// 	return "ending-mark";
	// }

	@Override
	public Collection<AudioBuffer> synthesize(String ssml, Voice voice,
	        TTSResource resource, List<Mark> marks, AudioBufferAllocator bufferAllocator,
	        boolean retry) throws SynthesisException, InterruptedException, MemoryException {

		return speak(ssml, voice, resource, marks, bufferAllocator);
	}

	public Collection<AudioBuffer> speak(String ssml, Voice voice, TTSResource resource,
	        List<Mark> marks, AudioBufferAllocator bufferAllocator) throws SynthesisException,
	        MemoryException {

		Collection<AudioBuffer> result = new ArrayList<AudioBuffer>();
		try {
			voice = mVoiceFormatConverter.get(voice.name.toLowerCase());

			ThreadResource tr = (ThreadResource) resource;
			int res = OnecoreLib.speak(tr.connection, voice.engine, voice.name, ssml);
			if (res != OnecoreLibResult.SAPINATIVE_OK.value()) {
				throw new SynthesisException("SAPI-Onecore speak error " + res + " raised with voice "
						+ voice + ": " +  OnecoreLibResult.valueOfCode(res));
			}

			int size = OnecoreLib.getStreamSize(tr.connection);
			// onecore returns the full 
			//AudioBuffer result = bufferAllocator.allocateBuffer(size);
			byte[] waveOutput = new byte[size];
			OnecoreLib.readStream(tr.connection, waveOutput, 0);

			// read the wave on the standard output
			BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(waveOutput));
			AudioInputStream fi = AudioSystem.getAudioInputStream(in);

			if (mAudioFormat == null)
				mAudioFormat = fi.getFormat();

			String[] names = OnecoreLib.getBookmarkNames(tr.connection);
			long[] pos = OnecoreLib.getBookmarkPositions(tr.connection);
			
			float sampleRate = mAudioFormat.getSampleRate();
			int bytesPerSample = mAudioFormat.getSampleSizeInBits() / 8;
			for (int i = 0; i < names.length; ++i) {
				int offset = (int) ((pos[i] * sampleRate * bytesPerSample) / 1000);
				marks.add(new Mark(names[i], offset));
			}

			while (true) {
				AudioBuffer b = bufferAllocator
				        .allocateBuffer(MIN_CHUNK_SIZE + fi.available());
				int ret = fi.read(b.data, 0, b.size);
				if (ret == -1) {
					//note: perhaps it would be better to call allocateBuffer()
					//somewhere else in order to avoid this extra call:
					bufferAllocator.releaseBuffer(b);
					break;
				}
				b.size = ret;
				result.add(b);
			}

			fi.close();
		} catch (MemoryException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			throw e;
		} catch (Throwable e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new SynthesisException(e);
		} finally {
		}
		return result;
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException {
		long connection = OnecoreLib.openConnection();

		if (connection == 0) {
			throw new SynthesisException("could not open SAPI-Onecore context.");
		}

		ThreadResource tr = new ThreadResource();
		tr.connection = connection;
		return tr;
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException {
		if (mVoiceFormatConverter == null) {
			mVoiceFormatConverter = new HashMap<String, Voice>();
			String[] names = OnecoreLib.getVoiceNames();
			String[] vendors = OnecoreLib.getVoiceVendors();
			String[] locale = OnecoreLib.getVoiceLocales();
			String[] gender = OnecoreLib.getVoiceGenders();
			String[] age = OnecoreLib.getVoiceAges();
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
								selected = Gender.FEMALE_ADULT;
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

		List<Voice> voices = new ArrayList<Voice>();
		
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
	public AudioFormat getAudioOutputFormat() {
		return mAudioFormat;
	}

	@Override
	public int getOverallPriority() {
		return mOverallPriority;
	}

}
