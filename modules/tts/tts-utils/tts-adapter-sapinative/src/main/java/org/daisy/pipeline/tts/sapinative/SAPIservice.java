package org.daisy.pipeline.tts.sapinative;

import java.util.Map;

import javax.naming.directory.InvalidAttributeValueException;
import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.ComponentContext;

@Component(
	name = "sapinative-tts-service",
	service = { TTSService.class }
)
public class SAPIservice extends AbstractTTSService {

	private boolean mFirstLoad = true;
	private AudioFormat mAudioFormat = null;

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		int sampleRate = convertToInt(params, "org.daisy.pipeline.tts.sapi.samplerate", 22050);
		int bytesPerSample = convertToInt(params, "org.daisy.pipeline.tts.sapi.bytespersample", 2);
		int priority = convertToInt(params, "org.daisy.pipeline.tts.sapi.priority", 7);

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

	@Activate
	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml.xsl", SAPIservice.class);
	}

	//OSGi callback
	@Deactivate
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
