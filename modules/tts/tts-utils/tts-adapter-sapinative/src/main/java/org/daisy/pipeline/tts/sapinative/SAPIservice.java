package org.daisy.pipeline.tts.sapinative;

import java.util.Map;

import javax.naming.directory.InvalidAttributeValueException;
import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;
import org.osgi.service.component.ComponentContext;

public class SAPIservice extends AbstractTTSService {

	private boolean mFirstLoad = true;
	private AudioFormat mAudioFormat = null;

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		int sampleRate = convertToInt(params, "sapi.samplerate", 22050);
		int bytesPerSample = convertToInt(params, "sapi.bytespersample", 2);
		int priority = convertToInt(params, "sapi.priority", 7);

		AudioFormat audioFormat = new AudioFormat(sampleRate, 8 * bytesPerSample, 1, true,
		        false);

		synchronized (this) {
			if (mAudioFormat != null && !mAudioFormat.matches(audioFormat)) {
				throw new InvalidAttributeValueException(
				        "SAPI's audio properties cannot change at runtime.");
			}

			if (mFirstLoad) {
				System.loadLibrary("sapinative");
				mFirstLoad = false;
			}

			if (mAudioFormat == null) {
				int res = SAPILib.initialize(sampleRate, 8 * bytesPerSample);
				if (res != 0) {
					throw new SynthesisException(
					        "SAPI initialization failed with error code '" + res + "'");
				}
				mAudioFormat = audioFormat;
			}
		}

		//allocate the engine
		return new SAPIengine(this, audioFormat, priority);
	}

	@Override
	public String getName() {
		return "sapi";
	}

	@Override
	public String getVersion() {
		return "native";
	}

	//OSGi callback
	protected void deactivate(ComponentContext context) {
		SAPILib.dispose();
	}

	private static int convertToInt(Map<String, String> params, String prop, int defaultVal)
	        throws SynthesisException {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property "
				        + prop);
			}
		}
		return defaultVal;
	}

}
