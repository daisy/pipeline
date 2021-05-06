package org.daisy.dotify.api.translator;

/**
 * Provides an exception indicating a translation problem.
 *
 * @author Joel Håkansson
 */
public class TranslationException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -1834392372293939932L;

    protected TranslationException() {
        super();
    }

    protected TranslationException(String message, Throwable cause) {
        super(message, cause);
    }

    protected TranslationException(String message) {
        super(message);
    }

    protected TranslationException(Throwable cause) {
        super(cause);
    }

}
