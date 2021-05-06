package org.daisy.dotify.common.xml;

/**
 * Provides an exception for situations where detection of the XML encoding fails.
 *
 * @author Joel Håkansson
 */
public class XmlEncodingDetectionException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 6374491859837845172L;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public XmlEncodingDetectionException(String message) {
        super(message);
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
    public XmlEncodingDetectionException(String message, Throwable cause) {
        super(message, cause);
    }


}
