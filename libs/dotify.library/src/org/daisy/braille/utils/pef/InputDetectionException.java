/*
 * Braille Utils (C) 2010-2011 Daisy Consortium
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.daisy.braille.utils.pef;

import java.io.IOException;

/**
 * Provides an input detection exception.
 *
 * @author Joel Håkansson
 */
public class InputDetectionException extends IOException {

    private static final long serialVersionUID = -3021561730679367063L;

    /**
     * Constructs an {@code InputDetectionException} with {@code null}
     * as its error detail message.
     */
    public InputDetectionException() {
        super();
    }

    /**
     * Constructs an {@code InputDetectionException} with the specified detail message
     * and cause.
     *
     * <p> Note that the detail message associated with {@code cause} is
     * <i>not</i> automatically incorporated into this exception's detail
     * message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     * @param cause   The cause (which is saved for later retrieval by the
     *                {@link #getCause()} method).  (A null value is permitted,
     *                and indicates that the cause is nonexistent or unknown.)
     */
    public InputDetectionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code InputDetectionException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval
     *                by the {@link #getMessage()} method)
     */
    public InputDetectionException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code InputDetectionException} with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())}
     * (which typically contains the class and detail message of {@code cause}).
     *
     * @param cause The cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A null value is permitted,
     *              and indicates that the cause is nonexistent or unknown.)
     */
    public InputDetectionException(Throwable cause) {
        super(cause);
    }

}
