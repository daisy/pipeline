package org.daisy.dotify.translator;

/**
 * Provides an exception indicating that no matching marker
 * is found in the given context.
 *
 * @author Joel Håkansson
 */
public class MarkerNotFoundException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = 6301795989858272482L;

    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public MarkerNotFoundException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public MarkerNotFoundException(String message) {
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
    public MarkerNotFoundException(Throwable cause) {
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
    public MarkerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

}
