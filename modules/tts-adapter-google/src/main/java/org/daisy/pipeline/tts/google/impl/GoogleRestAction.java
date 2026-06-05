package org.daisy.pipeline.tts.google.impl;

/**
 * Possible actions to request with REST and google API.
 *
 * @author  Louis Caille @ braillenet.org
 */
public enum GoogleRestAction {

	VOICES("GET","/v1/voices"),
	SPEECH("POST","/v1/text:synthesize");

	public String method;
	public String domain;

	/**
	 * @param method the HTTP method (usually GET or POST)
	 * @param domain the domain/endpoint of the requested action
	 */
	GoogleRestAction(String method, String domain) {
		this.method = method;
		this.domain = domain;
	}
}
