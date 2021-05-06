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
package org.daisy.braille.utils.impl.tools.embosser;

import org.daisy.dotify.api.paper.Dimensions;

/**
 * Provides tools related to embosser communication.
 *
 * @author Joel Håkansson
 */
public class EmbosserTools {
    /**
     * Number of mm/inch.
     */
    public static final double INCH_IN_MM = 25.4;

    /**
     * Converts an integer to bytes and padding the value with zeros
     * to the required size.
     *
     * @param val  the value to convert
     * @param size the number of bytes to output
     * @return returns a zero padded byte array containing the value
     * @throws IllegalArgumentException if the integer value requires more bytes than
     *                                  specified by <code>size</code>.
     */
    public static byte[] toBytes(int val, int size) {
        StringBuffer sb = new StringBuffer();
        String s = "" + val;
        if (s.length() > size) {
            throw new IllegalArgumentException("Number is too big.");
        }
        for (int i = 0; i < size - s.length(); i++) {
            sb.append('0');
        }
        sb.append(s);
        return sb.toString().getBytes();
    }

    /**
     * Get width, in units.
     *
     * @param dim  the dimensions
     * @param unit unit in mm
     * @return returns width in units
     */
    public static int getWidth(Dimensions dim, double unit) {
        return (int) Math.floor(dim.getWidth() / unit);
    }

    /**
     * Get height, in units.
     *
     * @param dim  the dimensions
     * @param unit unit in mm
     * @return returns width in units
     */
    public static int getHeight(Dimensions dim, double unit) {
        return (int) Math.floor(dim.getHeight() / unit);
    }

}
