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
package org.daisy.braille.utils.impl.provider;

import org.daisy.braille.utils.impl.provider.BrailleEditorsTableProvider.TableType;
import org.daisy.braille.utils.impl.tools.embosser.AbstractEmbosserWriter;
import org.daisy.braille.utils.impl.tools.embosser.SimpleEmbosserProperties;
import org.daisy.dotify.api.embosser.LineBreaks;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.table.BrailleConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author Bert Frees
 */
public class MicroBrailleFileFormatWriter extends AbstractEmbosserWriter {

    private OutputStream os = null;
    private BrailleConverter table = null;
    private LineBreaks breaks;
    private Padding padding;
    private byte[] header;

    public MicroBrailleFileFormatWriter(OutputStream os) {

        int cols = 25; // ??? examine PEF file => Contract?
        int rows = 40; // ??? examine PEF file

        this.os = os;
        header = ("$" + rows).getBytes();
        breaks = new StandardLineBreaks(StandardLineBreaks.Type.DOS);
        pagebreaks = new NoPageBreaks();
        padding = Padding.AFTER;
        table = new BrailleEditorsTableProvider().newTable(TableType.MICROBRAILLE);

        SimpleEmbosserProperties props = SimpleEmbosserProperties.with(cols, rows)
                .supportsDuplex(false)
                .supportsAligning(false)
                .build();
        init(props);
    }

    @Override
    public LineBreaks getLinebreakStyle() {
        return breaks;
    }

    @Override
    public Padding getPaddingStyle() {
        return padding;
    }

    @Override
    public byte[] getBytes(String braille) throws UnsupportedEncodingException {
        return String.valueOf(table.toText(braille)).getBytes(table.getPreferredCharset().name());
    }

    @Override
    protected void addAll(byte[] bytes) throws IOException {
        os.write(bytes);
    }

    @Override
    protected void formFeed() throws IOException {
        super.formFeed();
        if (currentPage() == 1) {
            byte[] pageBreak = "----|---|---------------------------|+-".getBytes();
            if (getMaxWidth() < 40) {
                pageBreak[getMaxWidth() - 1] = '>';
            }
            addAll(pageBreak);
        }
        addAll(getLinebreakStyle().getString().getBytes());
    }

    @Override
    public void open(boolean duplex)
            throws IOException {

        super.open(duplex);
        addAll(header);
        addAll(getLinebreakStyle().getString().getBytes());
    }

    @Override
    public void close() throws IOException {
        os.close();
        super.close();
    }

}
