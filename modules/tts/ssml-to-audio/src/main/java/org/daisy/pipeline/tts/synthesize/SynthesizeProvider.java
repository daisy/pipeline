package org.daisy.pipeline.tts.synthesize;

import java.util.concurrent.Semaphore;

import javax.xml.transform.URIResolver;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioBufferTracker;
import org.daisy.pipeline.tts.TTSRegistry;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;

public class SynthesizeProvider implements XProcStepProvider {
	private TTSRegistry mRegistry;
	private AudioServices mAudioServices;
	private Semaphore mStartSemaphore; //counter to limit the number of simultaneous text-to-speech steps
	private AudioBufferTracker mAudioBufferTracker;
	private URIResolver mURIResolver;

	/**
	 * Service component callback
	 */
	public void setURIResolver(URIResolver uriResolver) {
		mURIResolver = uriResolver;
	}

	/**
	 * Service component callback
	 */
	public void unsetURIResolver(URIResolver uriResolver) {
		mURIResolver = null;
	}

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		if (mStartSemaphore == null) {
			mStartSemaphore = new Semaphore(3, true);
		}

		if (mAudioBufferTracker == null) {
			mAudioBufferTracker = new AudioBufferTracker();
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
		        mAudioBufferTracker, mURIResolver);
	}

	protected void setTTSRegistry(TTSRegistry registry) {
		mRegistry = registry;
	}

	protected void unsetTTSRegistry(TTSRegistry registry) {
		mRegistry = null;
	}

	protected void setAudioServices(AudioServices audioServices) {
		mAudioServices = audioServices;
	}

	protected void unsetAudioServices(AudioServices audioServices) {
		mAudioServices = null;
	}
}
