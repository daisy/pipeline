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

	class ServiceDisabledException extends Exception {

		public ServiceDisabledException() {
			super();
		}

		public ServiceDisabledException(Throwable t) {
			super(t);
		}

		public ServiceDisabledException(String message) {
			super(message);
		}

		public ServiceDisabledException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	/**
	 * Allocate a new TTSEngine
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
	 * @throws ServiceDisabledException if the service is not available, e.g. because
	 *         it was disabled by the user, or because required configuration is
	 *         missing.
	 * @throws Throwable if an engine could not be allocated for another reason.
	 */
	public TTSEngine newEngine(Map<String, String> params) throws ServiceDisabledException, Throwable;

	/**
	 * @return the same name as in the CSS voice-family property. If several TTS
	 *         services share the same name, then the one with the highest
	 *         priority will be chosen. Must be thread-safe.
	 */
	public String getName();

	/**
	 * @return a nice name for displaying in user interfaces.
	 */
	public default String getDisplayName() {
		return getName();
	}
}
