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
     * @param message the detail message.
     */
    public IntegerOutOfRange(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * message of <code>(cause==null ? null : cause.toString())</code> (which
     * typically contains the class and detail message of <code>cause</code>).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <code>null</code> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     */
    public IntegerOutOfRange(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message the detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A <code>null</code> value is
     *                permitted, and indicates that the cause is nonexistent or
     *                unknown.)
     */
    public IntegerOutOfRange(String message, Throwable cause) {
        super(message, cause);
    }

}
