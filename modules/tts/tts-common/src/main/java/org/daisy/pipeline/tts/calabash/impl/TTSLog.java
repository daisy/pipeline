package org.daisy.pipeline.tts.calabash.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.Voice;

/**
 * A class to log structured messages in a big list. TTSLog is meant to be
 * allocated for every new Pipeline job and written to an external file after
 * the TTS work of the job is done.
 */
public interface TTSLog {
	enum ErrorCode {
		UNEXPECTED_VOICE,
		AUDIO_MISSING,
		CRITICAL_ERROR,
		ERROR,
		WARNING
	}

	public static class Error {
		public Error(ErrorCode key, String message) {
			this.key = key;
			this.message = message;
		}

		public ErrorCode getErrorCode() {
			return key;
		}

		public String getMessage() {
			return message;
		}

		private ErrorCode key;
		private String message;
	}

	interface Entry {
		void addError(Error err);

		Collection<Error> getReadOnlyErrors();

		/**
		 * @param ssml is the SSML before being converted to 'ttsinput'
		 */
		void setSSML(XdmNode ssml);

		XdmNode getSSML();

		/**
		 * @param v is the voice selected by the top-level VoiceManager
		 */
		void setSelectedVoice(Voice v);

		Voice getSelectedVoice();

		/**
		 * @param v is the actual voice used by the TTS processor (the same as
		 *            selectedVoice in the general case, but can be different if
		 *            something went wrong)
		 */
		void setActualVoice(Voice v);

		Voice getActualVoice();

		/**
		 * @param soundfile is a path of a wave, mp3 or ogg file
		 */
		void setSoundfile(String soundfile);

		String getSoundFile();

		/**
		 * @param secs is the timeout value used while synthesizing the entry
		 */
		void setTimeout(float secs);

		float getTimeout();

		/**
		 * @param secs is the actual time elapsed while synthesizing the entry in seconds
		 */
		void setTimeElapsed(float secs);

		float getTimeElapsed();

		/**
		 * @param begin offset in seconds
		 * @param end offset in seconds
		 */
		void setPositionInFile(double begin, double end);

		double getBeginInFile();

		double getEndInFile();
	}

	/**
	 * Supposed to be called within a single-threaded context
	 */
	Entry getOrCreateEntry(String id);

	/**
	 * Can be called within a multi-threaded context once all the calls to
	 * getOrCreateEntry() are done.
	 */
	Entry getWritableEntry(String id);

	Set<Map.Entry<String, Entry>> getEntries();

	void addGeneralError(ErrorCode errcode, String message);

	Collection<Error> readonlyGeneralErrors();
}
