package org.daisy.dotify.api.text;

/**
 * Thrown to indicate that an integer cannot be converted to
 * text because it is out of range of the capabilities of
 * the implementation.
 * 
 * @author Joel Håkansson
 */
public class IntegerOutOfRange extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3260267525562596727L;

    /**
     * Constructs an <code>IntegerOutOfRange</code> with no
     * detail message.
     */
	public IntegerOutOfRange() {
		super();
	}

    /**
     * Constructs an <code>IntegerOutOfRange</code> class
     * with the specified detail message.
     *
     * @param   message   the detail message.
     */
	public IntegerOutOfRange(String message) {
		super(message);
	}

	public IntegerOutOfRange(Throwable cause) {
		super(cause);
	}

	public IntegerOutOfRange(String message, Throwable cause) {
		super(message, cause);
	}

}
