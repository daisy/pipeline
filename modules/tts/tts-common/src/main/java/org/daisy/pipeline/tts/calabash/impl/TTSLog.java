package org.daisy.pipeline.tts.calabash.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.tts.Voice;

import org.slf4j.Logger;

/**
 * A class to log structured messages in a big list. TTSLog is meant to be
 * allocated for every new Pipeline job and written to an external file after
 * the TTS work of the job is done.
 */
public class TTSLog {

	private final Logger slf4jLogger;

	public TTSLog() {
		this(null);
	}

	public TTSLog(Logger slf4jLogger) {
		this.slf4jLogger = slf4jLogger;
	}

	public enum ErrorCode {
		UNEXPECTED_VOICE,
		AUDIO_MISSING,
		CRITICAL_ERROR,
		ERROR,
		WARNING
	}

	public static class Error {

		private final ErrorCode key;
		private String message;
		private Throwable cause;

		public Error(ErrorCode key, String message) {
			this(key, message, null);
		}

		public Error(ErrorCode key, String message, Throwable cause) {
			this.key = key;
			this.message = message;
			this.cause = cause;
		}

		public ErrorCode getErrorCode() {
			return key;
		}

		public String getMessage() {
			return message;
		}

		public Throwable getCause() {
			return cause;
		}
	}

	public class Entry {

		private final String sentenceId;

		private Entry(String sentenceId) {
			this.sentenceId = sentenceId;
		}

		public void addError(Error err) {
			errors.add(err);
			String msg = err.getMessage();
			if (err.getCause() != null)
				msg += " (Please see detailed log and TTS log for more info.)";
			else
				msg += " (Please see TTS log for more info.)";
			switch (err.getErrorCode()) {
			case WARNING:
			case ERROR:
				break;
			default:
				msg = err.getErrorCode() + ": " + msg;
			}
			msg = "While processing sentence with ID " + sentenceId + ": " + msg;
			switch (err.getErrorCode()) {
			case WARNING:
			case AUDIO_MISSING:
				if (slf4jLogger != null)
					slf4jLogger.warn(msg);
				break;
			default:
				if (slf4jLogger != null)
					slf4jLogger.error(msg);
			}
			if (err.getCause() != null)
				if (slf4jLogger != null)
					slf4jLogger.debug("Error stack trace:", err.getCause());
		}

		public Collection<Error> getReadOnlyErrors() {
			return errors;
		}

		/**
		 * @param ssml is the SSML before being converted to 'ttsinput'
		 */
		public void setSSML(XdmNode ssml) {
			this.ssml = ssml;
		}

		public XdmNode getSSML() {
			return ssml;
		}

		/**
		 * @param v is the voice selected by the top-level VoiceManager
		 */
		public void setSelectedVoice(Voice v) {
			this.selectedVoice = v;
		}

		public Voice getSelectedVoice() {
			return selectedVoice;
		}

		/**
		 * @param v is the actual voice used by the TTS processor (the same as
		 *            selectedVoice in the general case, but can be different if
		 *            something went wrong)
		 */
		public void setActualVoice(Voice v) {
			this.actualVoice = v;
		}

		public Voice getActualVoice() {
			return actualVoice;
		}

		/**
		 * @param clip is a path of a wave, mp3 or ogg file, with begin and end offsets
		 */
		public void setClip(AudioClip clip) {
			this.clip = clip;
		}

		public AudioClip getClip() {
			return clip;
		}

		/**
		 * @param secs is the timeout value used while synthesizing the entry
		 */
		public void setTimeout(float secs) {
			this.timeout = secs;
		}

		public float getTimeout() {
			return timeout;
		}

		/**
		 * @param secs is the actual time elapsed while synthesizing the entry in seconds
		 */
		public void setTimeElapsed(float secs) {
			this.timeElapsed = secs;
		}

		public float getTimeElapsed() {
			return this.timeElapsed;
		}

		private List<Error> errors = new ArrayList<Error>();
		private XdmNode ssml; //SSML
		private Voice selectedVoice;
		private Voice actualVoice;
		private AudioClip clip;
		private float timeout;
		private float timeElapsed;
	}

	/**
	 * Supposed to be called within a single-threaded context
	 */
	public Entry getOrCreateEntry(String sentenceId) {
		Entry res = mLog.get(sentenceId);
		if (res != null)
			return res;
		res = new Entry(sentenceId);
		mLog.put(sentenceId, res);
		return res;
	}

	/**
	 * Can be called within a multi-threaded context once all the calls to
	 * getOrCreateEntry() are done.
	 */
	public Entry getWritableEntry(String sentenceId) {
		return mLog.get(sentenceId);
	}

	public Set<Map.Entry<String, Entry>> getEntries() {
		return mLog.entrySet();
	}

	public void addGeneralError(ErrorCode errcode, String message) {
		addGeneralError(errcode, message, null);
	}

	public void addGeneralError(ErrorCode errcode, String message, Throwable cause) {
		synchronized (generalErrors) {
			generalErrors.add(new Error(errcode, message, cause));
		}
		if (cause != null)
			message += " (Please see detailed log for more info.)";
		switch (errcode) {
		case WARNING:
		case ERROR:
			break;
		default:
			message = "" + errcode + ": " + message;
		}
		switch (errcode) {
		case WARNING:
		case AUDIO_MISSING:
			if (slf4jLogger != null)
				slf4jLogger.warn(message);
			break;
		default:
			if (slf4jLogger != null)
				slf4jLogger.error(message);
		}
		if (cause != null)
			if (slf4jLogger != null)
				slf4jLogger.debug("Error stack trace:", cause);
	}

	public Collection<Error> readonlyGeneralErrors() {
		return generalErrors;
	}

	private List<Error> generalErrors = new ArrayList<Error>();
	private Map<String, Entry> mLog = new HashMap<String, Entry>();
}

