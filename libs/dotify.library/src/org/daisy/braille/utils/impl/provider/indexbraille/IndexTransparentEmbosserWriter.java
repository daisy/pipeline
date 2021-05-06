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
package org.daisy.braille.utils.impl.provider.indexbraille;

import org.daisy.braille.utils.impl.tools.embosser.AbstractEmbosserWriter;
import org.daisy.braille.utils.impl.tools.embosser.InternalEmbosserWriterProperties;
import org.daisy.dotify.api.embosser.LineBreaks;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.table.BrailleConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an embosser writer that uses the transparent mode of index embossers.
 *
 * @author Bert Frees
 * @author Joel Håkansson
 */
public class IndexTransparentEmbosserWriter extends AbstractEmbosserWriter {
    private final OutputStream os;
    private final BrailleConverter bc;
    private final boolean eightDot;
    private final List<Byte> buf;
    private int charsOnRow;
    private final byte[] header;
    private final byte[] footer;

    /**
     * Creates a new transparent embosser writer using V4/5 transparent mode mapping.
     *
     * @param os     the output stream
     * @param header the header
     * @param footer the footer
     * @param props  the properties
     */
    public IndexTransparentEmbosserWriter(
        OutputStream os,
        byte[] header,
        byte[] footer,
        InternalEmbosserWriterProperties props
    ) {
        this(os, null, true, header, footer, props);
    }

    /**
     * Creates a new transparent embosser writer using the specified braille table.
     *
     * @param os       the output stream
     * @param bc       the braille converter
     * @param eightDot true for eight dot transparent mode, false for six dot transparent mode
     * @param header   the header
     * @param footer   the footer
     * @param props    the properties
     */
    public IndexTransparentEmbosserWriter(
        OutputStream os,
        BrailleConverter bc,
        boolean eightDot,
        byte[] header,
        byte[] footer,
        InternalEmbosserWriterProperties props
    ) {
        init(props);
        if (header != null) {
            this.header = header;
        } else {
            this.header = new byte[0];
        }
        if (footer != null) {
            this.footer = footer;
        } else {
            this.footer = new byte[0];
        }
        this.os = os;
        this.bc = bc;
        this.eightDot = eightDot;
        this.buf = new ArrayList<>();
        charsOnRow = 0;
    }

    @Override
    public LineBreaks getLinebreakStyle() {
        return new StandardLineBreaks(StandardLineBreaks.Type.DOS);
    }

    @Override
    public Padding getPaddingStyle() {
        return Padding.NONE;
    }

    @Override
    public byte[] getBytes(String braille) throws UnsupportedEncodingException {
        if (bc != null) {
            // Joel:    I'll leave the old behavior intact, although the name "transparent mode"
            //          seems to indicate that this is incorrect.
            return String.valueOf(bc.toText(braille)).getBytes(bc.getPreferredCharset().name());
        } else {
            char[] chars = braille.toCharArray();
            byte[] ret = new byte[chars.length];
            for (int i = 0; i < chars.length; i++) {
                ret[i] = mapUnicode2Transparent(chars[i]);
            }
            return ret;
        }
    }

    /**
     * Maps unicode braille pattern bit order to index transparent bit order.
     *
     * @param c the unicode braille pattern
     * @return returns the corresponding transparent byte
     */
    static byte mapUnicode2Transparent(char c) {
        int v = 0xff & c;
        int b1238 = v & 0b1000_0111; // bits 1-3 and 8 are unchanged
        int b456 = v & 0b0011_1000; // bits 4-6
        int b7 = v & 0b0100_0000; // bit 7
        int ret = b1238 | b456 << 1 | b7 >> 3;
        return (byte) ret;
    }

    @Override
    protected void addAll(byte[] b) throws IOException {
        for (byte bi : b) {
            buf.add(bi);
        }
    }

    private void flush() throws IOException {
        if (charsOnRow > 0) {
            // The number of characters = y * 256 + x
            int y = charsOnRow / 256;
            int x = charsOnRow - y * 256;
            byte[] preamble = new byte[]{0x1b, 0x5c, (byte) x, (byte) y};
            os.write(preamble);
        }
        for (byte b : buf) {
            os.write(b);
        }
        charsOnRow = 0;
        buf.clear();
    }

    @Override
    public void newLine() throws IOException {
        if (eightDot) {
            // whole lines are 1, 6, 11 etc in 8-dot mode
            for (int i = 0; i < ((Math.max(getRowGap() - 1, 0) / 5) + 1); i++) {
                lineFeed();
            }
        } else {
            super.newLine();
        }
    }

    @Override
    protected void lineFeed() throws IOException {
        super.lineFeed();
        flush();
    }

    @Override
    protected void formFeed() throws IOException {
        super.formFeed();
        flush();
    }

    @Override
    public void write(String braille) throws IOException {
        charsOnRow += braille.length();
        super.write(braille);
    }

    @Override
    public void open(boolean duplex) throws IOException {
        super.open(duplex);
        os.write(header);
    }

    @Override
    public void close() throws IOException {
        flush();
        os.write(footer);
        os.close();
        super.close();
    }
}
