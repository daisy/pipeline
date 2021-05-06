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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * <p>Provides a utility to map unicode braille patterns to a braille
 * graphics mode. This can be useful when embossing 8-dot files on an embosser using
 * a 6-dot table, or when converting 6-dot graphics into 8-dot graphics.</p>
 *
 * <p>Data is added via {@link #write(String)}, {@link #newLine(int)} and {@link #flush()}.
 * The patterns are then mapped internally using a mapping configuration. The result
 * is extracted using {@link #readLine()} and {@link #readLine(boolean)}. To check if
 * there is more data use {@link #hasMoreFullLines()} and {@link #hasMoreLines()}.
 *
 * <p>Note that in order to create the same appearance as the the original patterns,
 * the resulting patterns should be embossed without any row spacing.</p>
 *
 * @author Joel Håkansson
 */
public class DotMapper {
    static final int[] UNICODE_BIT_MAP = {0x01, 0x08, 0x02, 0x10, 0x04, 0x20, 0x40, 0x80};
    private final int width;
    private final DotMapperConfiguration config;
    private List<BitSet> bs;
    private StringBuilder sb;

    /**
     * Creates a new dot mapper with the specified line length and
     * default configuration.
     * The default configuration maps 8-dot unicode patterns to
     * 6-dot unicode patterns: Dots 7 and 8 of the first cell
     * are shifted to dots 1 and 4 of the first cell of the
     * following line. Dots 1 and 4 of the first cell of the
     * second line are subsequently shifted to dots 2 and 5 of
     * the first cell of the second line and so on.
     *
     * @param width the length of the lines, in characters
     */
    public DotMapper(int width) {
        this(width, DotMapperConfiguration.builder().build());
    }

    /**
     * Creates a new dot mapper with the specified line length and
     * configuration.
     *
     * @param width  the width
     * @param config the configuration
     */
    public DotMapper(int width, DotMapperConfiguration config) {
        this.width = width;
        this.config = config;
        this.bs = new ArrayList<>();
        sb = new StringBuilder();
    }

    /**
     * Writes a string of braille. Values must be between 0x2800 and 0x28FF.
     *
     * @param braille characters in the range 0x2800 to 0x28FF
     * @throws IllegalArgumentException if the number of characters exceeds the line width
     */
    public void write(String braille) {
        if (sb.length() + braille.length() > width) {
            throw new IllegalArgumentException("The maximum number of characters on a line was exceeded.");
        }
        sb.append(braille);
    }

    /**
     * Starts a new line.
     *
     * @param rowgap the row gap following the line currently in the buffer
     */
    public void newLine(int rowgap) {
        flush();
        for (int i = 0; i < rowgap; i++) {
            bs.add(new BitSet(width * 2));
        }
    }

    /**
     * Flushes the last line of characters. This will empty the input buffer
     * and add them to the output buffer that can be read with {@link #readLine()}
     * or {@link #readLine(boolean)}. This is equal to calling newLine(0).
     */
    public void flush() {
        flushToBitSet();
    }

    private void flushToBitSet() {
        String t = sb.toString();
        for (int i = 0; i < config.getInputCellHeight(); i++) {
            BitSet s = new BitSet(width * 2);
            int j = 0;
            for (char c : t.toCharArray()) {
                s.set(j, (c & UNICODE_BIT_MAP[i * 2]) == UNICODE_BIT_MAP[i * 2]);
                s.set(j + 1, (c & UNICODE_BIT_MAP[i * 2 + 1]) == UNICODE_BIT_MAP[i * 2 + 1]);
                j = j + 2;
            }
            bs.add(s);
        }
        sb = new StringBuilder();
    }

    /**
     * Returns true if there are at least one full height line to extract
     * with {@link #readLine()} or {@link #readLine(boolean)}.
     *
     * @return true if there are at least one full height line, false otherwise
     */
    public boolean hasMoreFullLines() {
        return bs.size() >= config.getCellHeight();
    }

    /**
     * Returns true if there is more data to extract with {@link #readLine()}
     * or {@link #readLine(boolean)}.
     *
     * @return returns true if there is more data, false otherwise
     */
    public boolean hasMoreLines() {
        return bs.size() > 0;
    }

    /**
     * Reads a line from the output buffer. When the last line is read, the grid alignment resets (the
     * characters are padded to their full cell height).
     *
     * @return returns the line or null if the buffer is empty
     */
    public String readLine() {
        return readLine(false);
    }

    /**
     * Reads a line from the output buffer. When the last line is read, the grid alignment resets (the
     * characters are padded to their full cell height).
     *
     * @param trimTrailing when true, trailing base characters are trimmed
     * @return returns the line or null if the buffer is empty
     */
    public String readLine(boolean trimTrailing) {
        if (bs.size() == 0) {
            return null;
        }
        String res = getFirstRow();
        removeRow();
        return trimTrailing ? trimTrailing(res) : res;
    }

    String trimTrailing(String s) {
        int i = s.length();
        for (; i > 0; i--) {
            if (s.charAt(i - 1) != config.getBaseCharacter()) {
                break;
            }
        }
        if (config.getCellWidth() == 1 && i % 2 == 1) {
            i++;
        }
        return i >= s.length() ? s : s.substring(0, i);
    }

    /**
     * Converts the upper part of the bit set to a row of characters.
     *
     * @return returns the top row as characters
     */
    String getFirstRow() {
        StringBuilder res = new StringBuilder();
        BitSet s;
        // make a row
        for (int j = 0; j < width * (3 - config.getCellWidth()); j++) {
            char c = config.getBaseCharacter();
            for (int i = 0; i < config.getCellHeight(); i++) {
                if (bs.size() > i) {
                    s = bs.get(i);
                    for (int k = 0; k < config.getCellWidth(); k++) {
                        if (s.get(j * config.getCellWidth() + k)) {
                            c |= config.getBitMap()[i * config.getCellWidth() + k];
                        }
                    }
                }
            }
            res.append(c);
        }
        return res.toString();
    }

    void removeRow() {
        for (int i = 0; i < config.getCellHeight(); i++) {
            if (bs.size() > 0) {
                bs.remove(0);
            }
        }
    }

}
