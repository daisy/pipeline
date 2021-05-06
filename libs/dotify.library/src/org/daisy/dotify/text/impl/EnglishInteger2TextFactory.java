package org.daisy.dotify.text.impl;

import org.daisy.dotify.api.text.Integer2Text;
import org.daisy.dotify.api.text.Integer2TextConfigurationException;
import org.daisy.dotify.api.text.Integer2TextFactory;

class EnglishInteger2TextFactory implements Integer2TextFactory {

    @Override
    public Integer2Text newInteger2Text(String locale) throws Integer2TextConfigurationException {
        return new EnInt2TextLocalization();
    }

    @Override
    public Object getFeature(String key) {
        return null;
    }

    @Override
    public void setFeature(String key, Object value) throws Integer2TextConfigurationException {
        throw new EnglishInteger2TextConfigurationException();
    }

    private class EnglishInteger2TextConfigurationException extends
            Integer2TextConfigurationException {

        /**
         *
         */
        private static final long serialVersionUID = -7090139699406930899L;

    }

}
