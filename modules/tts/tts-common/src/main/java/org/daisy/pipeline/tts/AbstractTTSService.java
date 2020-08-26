package org.daisy.pipeline.tts;

import java.net.URL;

import org.daisy.common.file.URLs;

public abstract class AbstractTTSService implements TTSService {

	@Override
	public String getVersion() {
		return "";
	}

	/**
	 * @param resource Path to the SSML adapter XSLT within the bundle
	 * @param context A class contained in the same bundle as the SSML adapter XSLT
	 */
	protected void loadSSMLadapter(String resource, Class<?> context) {
		mXSLTresource = URLs.getResourceFromJAR(resource, context);
	}

	@Override
	public URL getSSMLxslTransformerURL() {
		return mXSLTresource;
	}

	protected URL mXSLTresource;
}
