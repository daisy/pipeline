package org.daisy.pipeline.audio;

import java.io.File;
import java.net.URI;
import java.time.Duration;

/**
 * Object that identifies a specific fragment in an audio file.
 */
public final class AudioClip {

	public final URI src;
	public final Duration clipBegin;
	public final Duration clipEnd;

	public AudioClip(File src, Duration clipBegin, Duration clipEnd) {
		this(src == null ? null : src.toURI(), clipBegin, clipEnd);
	}

	public AudioClip(URI src, Duration clipBegin, Duration clipEnd) {
		if (clipBegin == null || clipEnd == null || src == null)
			throw new NullPointerException();
		if (clipBegin.compareTo(Duration.ZERO) < 0)
			throw new IllegalArgumentException("Clip begin can not be negative");
		if (clipEnd.compareTo(clipBegin) < 0)
			throw new IllegalArgumentException("Clip end can not come before clip begin");
		this.src = src;
		this.clipBegin = clipBegin;
		this.clipEnd = clipEnd;
	}

	@Override
	public String toString() {
		return "[src: " + src + ", clipBegin: " + clipBegin + ", clipEnd: " + clipEnd + "]";
	}
}
