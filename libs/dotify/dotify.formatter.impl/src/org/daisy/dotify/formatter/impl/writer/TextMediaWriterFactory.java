package org.daisy.dotify.formatter.impl.writer;

import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;

class TextMediaWriterFactory implements PagedMediaWriterFactory {
	private static final String FEATURE_ENCODING_KEY = "encoding";
	private String encoding = "utf-8";

	@Override
	public PagedMediaWriter newPagedMediaWriter()
			throws PagedMediaWriterConfigurationException {
		return new TextMediaWriter(encoding);
	}

	@Override
	public Object getFeature(String key) {
		if (FEATURE_ENCODING_KEY.equals(key)) {
			return encoding;
		} else {
			return null;
		}
	}

	@Override
	public void setFeature(String key, Object value)
			throws PagedMediaWriterConfigurationException {
		if (FEATURE_ENCODING_KEY.equals(key)) {
			encoding = value.toString();
		} else {
			throw new TextMediaWriterConfigurationException("Unknown feature: " + key);
		}
	}
	
	private class TextMediaWriterConfigurationException extends PagedMediaWriterConfigurationException {

		/**
		 * 
		 */
		private static final long serialVersionUID = -2673985749596696888L;

		public TextMediaWriterConfigurationException(String message) {
			super(message);
		}
		
	}

}
