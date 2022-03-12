package org.daisy.pipeline.tts;

import java.util.Map;

/**
 * Classes that implements the TTSService interface are meant to be allocated
 * only once, as opposed to TTSEngines. The main role of TTSService is to
 * allocate TTSEngines depending on given parameters.
 * 
 */
public interface TTSService {

	class SynthesisException extends Exception {

		public SynthesisException(String message, Throwable cause) {
			super(message, cause);
		}

		public SynthesisException(Throwable t) {
			super(t);
		}

		public SynthesisException(String message) {
			super(message);
		}
	}

	/**
	 * Allocate a new TTSEngine (e.g. eSpeak or SAPI).
	 * 
	 * @param params contains various key-value pairs. Some of them might be options
	 *         for the TTS processor under consideration, e.g. server IPs,
	 *         priorities or sound quality. It can also contain options which
	 *         have nothing to do with the returned TTSEngine. Such options must
	 *         be ignored. This method is allowed to perform heavy
	 *         initializations as long as they can be interrupted with the
	 *         regular Java thread interruption mechanism. In particular, it is
	 *         not recommended to test a full text-to-speech step in this
	 *         method.
	 */
	public TTSEngine newEngine(Map<String, String> params) throws Throwable;

	/**
	 * @return the same name as in the CSS voice-family property. If several TTS
	 *         services share the same name, then the one with the highest
	 *         priority will be chosen. Must be thread-safe.
	 */
	public String getName();

}
