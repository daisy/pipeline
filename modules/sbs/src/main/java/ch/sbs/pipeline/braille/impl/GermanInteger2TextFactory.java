package ch.sbs.pipeline.braille.impl;

import org.daisy.dotify.api.text.Integer2Text;
import org.daisy.dotify.api.text.Integer2TextConfigurationException;
import org.daisy.dotify.api.text.Integer2TextFactory;

class GermanInteger2TextFactory implements Integer2TextFactory {

	@Override
	public Integer2Text newInteger2Text(String locale) throws Integer2TextConfigurationException {
		return new DeInt2TextLocalization();
	}

	@Override
	public Object getFeature(String key) {
		return null;
	}

	@Override
	public void setFeature(String key, Object value) throws Integer2TextConfigurationException {
		throw new GermanInteger2TextConfigurationException();
	}
	
	private class GermanInteger2TextConfigurationException extends Integer2TextConfigurationException {
	}
}
