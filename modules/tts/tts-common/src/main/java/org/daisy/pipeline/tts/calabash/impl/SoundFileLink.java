package org.daisy.pipeline.tts.calabash.impl;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

import org.daisy.pipeline.audio.AudioClip;

/**
 * Mapping between a fragment in the XML document and a fragment in an audio file.
 */
class SoundFileLink {

	/** ID of fragment in XML document */
	private final String xmlID;
	/** Time position where the audio fragment starts within <code>clipBase</code> */
	private final Duration clipBegin;
	/** Time position where the audio fragment ends within <code>clipBase</code> */
	private final Duration clipEnd;

	/**
	 * Audio clip that is used as the base for <code>clipBegin</code> and <code>clipEnd</code>. The
	 * purpose of the reference is so that it can be easily shared between {@link TextToPcmThread}
	 * and {@link EncodingThread} (via {@link ContiguousPCM}), because {@link AudioClip} is
	 * immutable.
	 */
	public AtomicReference<AudioClip> clipBase;

	public SoundFileLink(String xmlID, Duration clipBegin, Duration clipEnd) {
		if (clipBegin == null || clipEnd == null || xmlID == null)
			throw new NullPointerException();
		if (clipBegin.compareTo(Duration.ZERO) < 0)
			throw new IllegalArgumentException("Clip begin can not be negative: " + clipBegin);
		if (clipEnd.compareTo(clipBegin) < 0)
			throw new IllegalArgumentException("Clip end can not come before clip begin: " + clipEnd + " < " + clipBegin);
		this.xmlID = xmlID;
		this.clipBegin = clipBegin;
		this.clipEnd = clipEnd;
	}

	public String getTextFragment() {
		return xmlID;
	}

	private AudioClip clip;

	public AudioClip getAudioFragment(TTSLog log) {
		if (clip == null) {
			if (clipBase == null)
				throw new IllegalStateException(); // should not happen
			AudioClip base = clipBase.get();
			if (base == null)
				return null; // was not set in EncodingThread due to an error
			Duration begin = base.clipBegin.plus(clipBegin);
			Duration end = base.clipBegin.plus(clipEnd);
			if (begin.compareTo(base.clipEnd) > 0) {
				if (begin.minus(base.clipEnd).toMillis() > 1)
					// this can not be a rounding error due to sampling
					log.getOrCreateEntry(getTextFragment()).addError(
						new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, "clip not encoded"));
				begin = base.clipEnd;
				end = base.clipEnd;
			} else if (end.compareTo(base.clipEnd) > 0) {
				if (end.minus(base.clipEnd).toMillis() > 1)
					// this can not be a rounding error due to sampling
					log.getOrCreateEntry(getTextFragment()).addError(
						new TTSLog.Error(TTSLog.ErrorCode.AUDIO_MISSING, "part of clip not encoded"));
				end = base.clipEnd;
			}
			clip = new AudioClip(base.src, begin, end);
		}
		return clip;
	}
}
