package org.daisy.pipeline.tts;

import java.net.URL;
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
			super(message
			        + (cause != null && cause.getMessage() != null ? ": " + cause.getMessage()
			                : ""), cause);
			if (cause != null) {
				setStackTrace(cause.getStackTrace());
			}

		}

		public SynthesisException(Throwable t) {
			super(t);
		}

		public SynthesisException(String message) {
			super(message);
		}
	}

	/**
	 * Java counterpart of SSML's marks and TTS processors' bookmarks.
	 */
	class Mark {
		public Mark(String name, int offset) {
			this.offsetInAudio = offset;
			this.name = name;
		}

		public int offsetInAudio; //in bytes
		public String name;
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

	/**
	 * @return the version or type (binary, in-memory) of the TTS service. Used
	 *         only for printing information. Must be thread-safe.
	 */
	public String getVersion();

	/**
	 * Must be thread safe.
	 * 
	 * @return the URL of the XSLT stylesheet used for transforming SSML into
	 *         whatever language the TTS processor takes as input. If the TTS
	 *         processor does not add any extra ending pauses, the XSLT might
	 *         produce a silent break at the end (around 250ms). It should also
	 *         add an ending SSML mark if it can handle it. The XSLT must take
	 *         as parameters an optional voice name and an ending-mark (even if
	 *         it doesn't use it).
	 */
	public URL getSSMLxslTransformerURL();

}
