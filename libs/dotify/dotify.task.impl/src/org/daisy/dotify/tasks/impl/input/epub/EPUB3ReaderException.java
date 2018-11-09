package org.daisy.dotify.tasks.impl.input.epub;

/**
 * Provides an exception for epub reading.
 * @author Joel Håkansson
 */
public class EPUB3ReaderException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1778396562990448014L;

	EPUB3ReaderException() {
		super();
	}

	EPUB3ReaderException(String message, Throwable cause) {
		super(message, cause);
	}

	EPUB3ReaderException(String message) {
		super(message);
	}

	EPUB3ReaderException(Throwable cause) {
		super(cause);
	}

}
