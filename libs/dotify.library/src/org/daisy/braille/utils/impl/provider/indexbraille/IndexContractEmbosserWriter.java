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

import org.daisy.braille.utils.impl.tools.embosser.ConfigurableEmbosser;
import org.daisy.braille.utils.impl.tools.embosser.ContractEmbosserWriter;
import org.daisy.braille.utils.impl.tools.embosser.InternalContract;
import org.daisy.braille.utils.impl.tools.embosser.InternalContract.BrailleRange;
import org.daisy.braille.utils.impl.tools.embosser.InternalContractNotSupportedException;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.embosser.StandardLineBreaks;
import org.daisy.dotify.api.table.BrailleConverter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides an embosser writer that supports different configurations based on
 * the properties of an {@link InternalContract}.
 *
 * @author Joel Håkansson
 */
public class IndexContractEmbosserWriter implements ContractEmbosserWriter {
    private static final Logger logger = Logger.getLogger(IndexContractEmbosserWriter.class.getCanonicalName());
    private final OutputStream os;
    private final BrailleConverter bc;
    private final byte[] footer;
    private final IndexHeader.Builder propsBuilder;
    private IndexHeader props;
    private EmbosserWriter writer;

    IndexContractEmbosserWriter(OutputStream os, BrailleConverter bc, IndexHeader.Builder propsBuilder) {
        this.os = os;
        this.bc = bc;
        this.footer = new byte[0];
        this.propsBuilder = propsBuilder;
        this.props = propsBuilder.build();
        this.writer = null;
    }

    @Override
    public void open(boolean duplex) throws IOException {
        throw new IOException("Only use this with an internal contract.");
    }

    @Override
    public void open(
        boolean duplex,
        InternalContract contract
    ) throws IOException, InternalContractNotSupportedException {
        // At the moment only simple six and eight dot configurations are available.
        // Supporting rowgaps smaller than empty lines requires an update to the Index embosser firmware.
        // For details, see https://github.com/brailleapps/braille-utils.impl/issues/2
        if (contract.getBrailleRange() == BrailleRange.SIX_DOT) { // && contract.onlySimpleRowgaps()
            props = propsBuilder.transparentMode(false).build();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Using the configurable embosser writer (default).");
            }
            writer = new ConfigurableEmbosser.Builder(os, bc)
                    .breaks(new StandardLineBreaks(StandardLineBreaks.Type.DOS))
                    .padNewline(ConfigurableEmbosser.Padding.NONE)
                    .footer(footer)
                    .embosserProperties(props)
                    .header(props.getIndexHeader())
                    .build();
            writer.open(duplex);
        } else {
            if (contract.getRowGaps().contains(0)) {
                throw new InternalContractNotSupportedException(
                    "Due to firmware limitations, rowgap 0 cannot be supported in combination with 8-dot braille. " +
                    "Either change the rowgap to 1 or replace the 8-dot characters with 6-dot characters."
                );
            }
            props = propsBuilder.transparentMode(true).build();
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Using the transparent embosser writer.");
            }
            writer = new IndexTransparentEmbosserWriter(os, props.getIndexHeader(), footer, props);
            writer.open(duplex);
        }
    }

    @Override
    public void write(String braille) throws IOException {
        writer.write(braille);
    }

    @Override
    public void newLine() throws IOException {
        writer.newLine();
    }

    @Override
    public void setRowGap(int value) {
        writer.setRowGap(value);
    }

    @Override
    public int getRowGap() {
        return writer.getRowGap();
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

    @Override
    public void newPage() throws IOException {
        writer.newPage();
    }

    @Override
    public void newSectionAndPage(boolean duplex) throws IOException {
        writer.newSectionAndPage(duplex);
    }

    @Override
    public void newVolumeSectionAndPage(boolean duplex) throws IOException {
        writer.newVolumeSectionAndPage(duplex);
    }

    @Override
    public boolean isOpen() {
        return writer.isOpen();
    }

    @Override
    public boolean isClosed() {
        return writer.isClosed();
    }

    @Override
    public int getMaxWidth() {
        return props.getMaxWidth();
    }

    @Override
    public boolean supportsAligning() {
        return props.supportsAligning();
    }

}
