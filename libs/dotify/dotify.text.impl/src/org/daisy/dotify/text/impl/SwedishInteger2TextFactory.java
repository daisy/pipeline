package org.daisy.dotify.text.impl;

import org.daisy.dotify.api.text.Integer2Text;
import org.daisy.dotify.api.text.Integer2TextConfigurationException;
import org.daisy.dotify.api.text.Integer2TextFactory;

class SwedishInteger2TextFactory implements Integer2TextFactory {

	@Override
	public Integer2Text newInteger2Text(String locale) throws Integer2TextConfigurationException {
		return new SvInt2TextLocalization();
	}

	@Override
	public Object getFeature(String key) {
		return null;
	}

	@Override
	public void setFeature(String key, Object value) throws Integer2TextConfigurationException {
		throw new SwedishInteger2TextConfigurationException();
	}
	
	private class SwedishInteger2TextConfigurationException extends Integer2TextConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7090139699406930899L;
		
	}

}
