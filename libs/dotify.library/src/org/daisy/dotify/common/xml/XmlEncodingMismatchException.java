package org.daisy.dotify.common.xml;

/**
 * Provides an exception for situations where the XML file has a declared encoding which doesn't
 * match the detected encoding.
 *
 * @author Joel Håkansson
 */
public class XmlEncodingMismatchException extends XmlEncodingDetectionException {
    /**
     *
     */
    private static final long serialVersionUID = 4808515519997096568L;
    private final String detectedEncoding;
    private final String declaredEncoding;

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message  the detail message. The detail message is saved for
     *                 later retrieval by the {@link #getMessage()} method.
     * @param detected the detected encoding
     * @param declared the declared encoding
     */
    public XmlEncodingMismatchException(String message, String detected, String declared) {
        super(message);
        this.detectedEncoding = detected;
        this.declaredEncoding = declared;
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.  <p>Note that the detail message associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail message.
     *
     * @param message  the detail message (which is saved for later retrieval
     *                 by the {@link #getMessage()} method).
     * @param cause    the cause (which is saved for later retrieval by the
     *                 {@link #getCause()} method).  (A <code>null</code> value is
     *                 permitted, and indicates that the cause is nonexistent or
     *                 unknown.)
     * @param detected the detected encoding
     * @param declared the declared encoding
     */
    public XmlEncodingMismatchException(String message, Throwable cause, String detected, String declared) {
        super(message, cause);
        this.detectedEncoding = detected;
        this.declaredEncoding = declared;
    }

    /**
     * Gets the encoding detected from the XML-file.
     *
     * @return returns the detected encoding
     */
    public String getDetectedEncoding() {
        return detectedEncoding;
    }

    /**
     * Gets the encoding declared in the XML-file.
     *
     * @return returns the declared encoding
     */
    public String getDeclaredEncoding() {
        return declaredEncoding;
    }

}
