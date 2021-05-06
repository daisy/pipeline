package org.daisy.dotify.hyphenator.impl;

import org.daisy.dotify.api.hyphenator.HyphenatorConfigurationException;

class JHyphenatorConfigurationException extends
        HyphenatorConfigurationException {

    /**
     *
     */
    private static final long serialVersionUID = 2975917855861633456L;

    JHyphenatorConfigurationException() {
        super();
    }

    JHyphenatorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    JHyphenatorConfigurationException(String message) {
        super(message);
    }

    JHyphenatorConfigurationException(Throwable cause) {
        super(cause);
    }


}
