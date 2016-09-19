package org.daisy.pipeline.tts;

import java.net.URL;

import org.osgi.service.component.ComponentContext;

public abstract class AbstractTTSService implements TTSService {

	@Override
	public String getVersion() {
		return "";
	}

	protected void loadSSMLadapter(ComponentContext context) {
		mXSLTresource = context.getBundleContext().getBundle().getEntry("/transform-ssml.xsl");
	}

	@Override
	public URL getSSMLxslTransformerURL() {
		return mXSLTresource;
	}

	protected URL mXSLTresource;
}
