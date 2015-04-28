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
package org_daisy;

import java.io.IOException;
import java.io.OutputStream;

import org.daisy.braille.embosser.AbstractEmbosserWriter;
import org.daisy.braille.embosser.LineBreaks;
import org.daisy.braille.embosser.SimpleEmbosserProperties;
import org.daisy.braille.embosser.StandardLineBreaks;
import org.daisy.braille.table.BrailleConverter;
import org_daisy.BrailleEditorsTableProvider.TableType;

/**
 *
 * @author  Bert Frees
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

        SimpleEmbosserProperties props = new SimpleEmbosserProperties(cols, rows)
                       .supportsDuplex(false)
                       .supports8dot(false)
                       .supportsVolumes(false)
                       .supportsAligning(false);
        init(props);
    }

    //jvm1.6@Override
    public LineBreaks getLinebreakStyle() {
        return breaks;
    }

    //jvm1.6@Override
    public Padding getPaddingStyle() {
        return padding;
    }

    //jvm1.6@Override
    public BrailleConverter getTable() {
        return table;
    }

    //jvm1.6@Override
    protected void add(byte b) throws IOException {
        os.write(b);
    }

    //jvm1.6@Override
    protected void addAll(byte[] bytes) throws IOException {
        os.write(bytes);
    }

    @Override
    protected void formFeed() throws IOException {
        super.formFeed();
        if (currentPage()==1) {
            byte[] pageBreak = "----|---|---------------------------|+-".getBytes();
            if (getMaxWidth()<40) {
                pageBreak[getMaxWidth()-1] = '>';
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

	public boolean supportsZFolding() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean supportsPrintMode(PrintMode mode) {
		return mode == PrintMode.REGULAR;
	}
}
