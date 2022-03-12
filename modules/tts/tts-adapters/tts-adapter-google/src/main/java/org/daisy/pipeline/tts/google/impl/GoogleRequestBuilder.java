package org.daisy.pipeline.tts.google.impl;

import java.util.HashMap;

import org.daisy.pipeline.tts.rest.Request;
import org.json.JSONObject;

/**
 * Google REST Request builder class.
 *
 * @author Louis Caille @ braillenet.org
 */
public class GoogleRequestBuilder {

	/**
	 * Google API address
	 */
	private final String serverAddress;
	/**
	 * Google API key
	 */
	private final String apiKey;

	/**
	 * Encoding sample rate (if null, uses the default encoding)
	 */
	private Integer sampleRate = null;

	private GoogleRestAction action = GoogleRestAction.VOICES;
	private String text = null;
	private String languageCode = null;
	private String voice = null;

	/**
	 * Create a new REST request builder for google services
	 *
	 * @param apiKey the API key to access google services (to create from your google cloud console API identifiers)
	 */
	public GoogleRequestBuilder(String serverAddress, String apiKey) {
		this.apiKey = apiKey;
		this.serverAddress = serverAddress;
	}

	/**
	 * Set the action to execute
	 *
	 * @param action
	 */
	public GoogleRequestBuilder withAction(GoogleRestAction action) {
		this.action = action;
		return this;
	}

	/**
	 * Mandatory - set the language code of the next requests
	 *
	 * @param languageCode language code of the voice
	 */
	public GoogleRequestBuilder withLanguageCode(String languageCode) {
		this.languageCode = languageCode;
		return this;
	}

	/**
	 * Set the voice name to use for the next requests
	 *
	 * @param voice
	 */
	public GoogleRequestBuilder withVoice(String voice) {
		this.voice = voice;
		return this;
	}

	/**
	 * Mandatory - Set the text to synthesise for the next requests
	 *
	 * @param text
	 */
	public GoogleRequestBuilder withText(String text) {
		this.text = text;
		return this;
	}

	/**
	 * Set the sample rate of the next requests
	 *
	 * @param sampleRateHertz
	 */
	public GoogleRequestBuilder withSampleRate(int sampleRateHertz) {
		this.sampleRate = Integer.valueOf(sampleRateHertz);
		return this;
	}

	/**
	 * Create a new builder instance with all value except the api key set to defaults.
	 *
	 * @return a new builder to use for building a request
	 */
	public GoogleRequestBuilder newRequest() {
		return new GoogleRequestBuilder(serverAddress, apiKey);
	}

	/**
	 * Build a rest request to send for google could text to speech
	 *
	 * @throws Exception
	 */
	public Request<JSONObject> build() throws Exception {

		HashMap<String, String> headers = new HashMap<String, String>();
		JSONObject parameters = null;

		headers.put("Accept", "application/json");
		headers.put("Content-Type", "application/json; utf-8");

		switch(action) {
		case VOICES:
			// No specific parameters
			break;
		case SPEECH:
			// speech synthesis errors handling
			if (this.text == null || text.length() == 0)
				throw new Exception("Speech request without text.");
			if (languageCode == null || languageCode.length() == 0)
				throw new Exception("Language code definition is mandatory, please set one (speech request for " + text + ")");
			parameters = new JSONObject()
				.put("input", new JSONObject().put("ssml", text))
				.put("voice", new JSONObject().put("languageCode", languageCode)
				                              .putOpt("name", voice))
				.put("audioConfig", new JSONObject().put("audioEncoding", "LINEAR16")
				                                    .putOpt("sampleRateHertz", sampleRate));
			break;
		}

		return new Request<JSONObject>(
			action.method,
			serverAddress + action.domain + "?key=" + apiKey,
			headers,
			parameters);
	}
}
