package org.daisy.pipeline.tts.calabash.impl;

import java.util.concurrent.Semaphore;
import java.util.Map;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.TTSRegistry;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "ssml-to-audio",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}ssml-to-audio" }
)
public class SynthesizeProvider implements XProcStepProvider {
	private TTSRegistry mRegistry;
	private AudioServices mAudioServices;
	private Semaphore mStartSemaphore; //counter to limit the number of simultaneous text-to-speech steps
	private AudioFootprintMonitor mAudioFootprintMonitor;

	static final Property ENABLE_LOG = Properties.getProperty("org.daisy.pipeline.tts.log",
	                                                          true,
	                                                          "Whether or not to make the TTS log available",
	                                                          false,
	                                                          "false");

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		if (mStartSemaphore == null) {
			mStartSemaphore = new Semaphore(3, true);
		}

		if (mAudioFootprintMonitor == null) {
			mAudioFootprintMonitor = new AudioFootprintMonitor();
		}

		boolean error = false;
		if (mRegistry == null) {
			runtime.error(new RuntimeException("Registry of TTS engines is missing."));
			error = true;
		}

		if (mAudioServices == null) {
			runtime.error(new RuntimeException("Registry of audio encoders is missing."));
			error = true;
		}

		if (error)
			return null;

		//warning: a reference is kept on the audio encoder during all the synthesizing process,
		//even if it is unregistered.

		return new SynthesizeStep(runtime, step, mRegistry, mAudioServices, mStartSemaphore,
		                          mAudioFootprintMonitor, properties);
	}

	@Reference(
		name = "TTSRegistry",
		unbind = "-",
		service = TTSRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setTTSRegistry(TTSRegistry registry) {
		mRegistry = registry;
	}

	protected void unsetTTSRegistry(TTSRegistry registry) {
		mRegistry = null;
	}

	@Reference(
		name = "AudioServices",
		unbind = "-",
		service = AudioServices.class,
		cardinality = ReferenceCardinality.OPTIONAL,
		policy = ReferencePolicy.STATIC
	)
	protected void setAudioServices(AudioServices audioServices) {
		mAudioServices = audioServices;
	}

	protected void unsetAudioServices(AudioServices audioServices) {
		mAudioServices = null;
	}
}
