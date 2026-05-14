package org.daisy.pipeline.audio.saxon.impl;

import javax.xml.namespace.QName;

public final class AudioErrors {
	private AudioErrors() {}

	private static final String ERR_NS = "http://www.daisy.org/ns/pipeline/errors";

	/**
	 * Invalid or unsupported media-type
	 */
	public static final QName ERR_AUDIO_001 = new QName(ERR_NS, "AUDIO001", "pe");

}
