package org.daisy.pipeline.webservice;

public enum Properties {

	MAX_REQUEST_TIME("org.daisy.pipeline.ws.maxrequesttime"),
	TMPDIR("org.daisy.pipeline.ws.tmpdir"),
	AUTHENTICATION("org.daisy.pipeline.ws.authentication"),
	LOCALFS("org.daisy.pipeline.ws.localfs"),
	RELEASE_DESCRIPTOR("org.daisy.pipeline.updater.releaseDescriptor"),
	PORT("org.daisy.pipeline.ws.port"),
	WEBSOCKET_PORT("org.daisy.pipeline.ws.websocket.port"),
	PATH("org.daisy.pipeline.ws.path"),
	HOST("org.daisy.pipeline.ws.host"),
	SSL("org.daisy.pipeline.ws.ssl"),
	SSL_KEYSTORE("org.daisy.pipeline.ws.ssl.keystore"),
	SSL_KEYSTOREPASSWORD("org.daisy.pipeline.ws.ssl.keystorepassword"),
	CLEAN_UP_ON_START_UP("org.daisy.pipeline.ws.cleanuponstartup"),
	SSL_KEYPASSWORD("org.daisy.pipeline.ws.ssl.keypassword"),
	CLIENT_KEY("org.daisy.pipeline.ws.authentication.key"),
	CLIENT_SECRET("org.daisy.pipeline.ws.authentication.secret"),
	CORS("org.daisy.pipeline.ws.cors");

	private final String key;
	
	private Properties(String key) {
		this.key = key;
	}

	public String get() {
		return org.daisy.common.properties.Properties.getProperty(key);
	}

	public String get(String defaultValue) {
		return org.daisy.common.properties.Properties.getProperty(key, defaultValue);
	}

	public String getName() {
		return key;
	}

	@Override
	public String toString() {
		return key;
	}
}
