package org.daisy.dotify.api.hyphenator;

/**
 * Provides an exception that indicates that a feature is not supported.
 *
 * @author Joel Håkansson
 */
public class HyphenatorConfigurationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -7784339351020337103L;

    protected HyphenatorConfigurationException() {
        super();
    }

    protected HyphenatorConfigurationException(String message) {
        super(message);
    }

    protected HyphenatorConfigurationException(Throwable cause) {
        super(cause);
    }

    protected HyphenatorConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

}
