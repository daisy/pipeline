package org.daisy.dotify.formatter.impl.writer;

import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterException;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.api.writer.SectionProperties;
import org.daisy.dotify.common.io.StateObject;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;


/**
 * PagedMediaWriter implementation that outputs plain text.
 *
 * @author Joel Håkansson
 */
class TextMediaWriter implements PagedMediaWriter {
    private PrintStream pst;
    //private Properties p;
    private boolean hasOpenVolume;
    private boolean hasOpenSection;
    private boolean hasOpenPage;
    /*
    private int cCols;
    private int cRows;
    private int cRowgap;
    private boolean cDuplex;*/
    private final String encoding;
    private final StateObject state;

    /**
     * Creates a new text media writer using with the specified encoding.
     *
     * @param encoding the encoding to use.
     */
    public TextMediaWriter(String encoding) {
        hasOpenVolume = false;
        hasOpenSection = false;
        hasOpenPage = false;
        this.encoding = encoding;
        this.state = new StateObject("Writer");
    }

    @Override
    public void prepare(List<MetaDataItem> meta) {
    }

    @Override
    public void open(OutputStream os) throws PagedMediaWriterException {
        open(os, null);
    }

    private void open(OutputStream os, List<MetaDataItem> meta) throws PagedMediaWriterException {
        state.assertUnopened();
        state.open();
        try {
            pst = new PrintStream(os, true, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new PagedMediaWriterException("Cannot open PrintStream with " + encoding, e);
        }
        hasOpenVolume = false;
        hasOpenSection = false;
        hasOpenPage = false;
    }

    @Override
    public void newPage() {
        state.assertOpen();
        closeOpenPage();
        hasOpenPage = true;
    }

    @Override
    public void newRow(Row row) {
        state.assertOpen();
        pst.println(row.getChars());
    }

    @Override
    public void newRow() {
        state.assertOpen();
        pst.println();
    }

    @Override
    public void newVolume(SectionProperties master) {
        state.assertOpen();
        closeOpenVolume();
        hasOpenVolume = true;
    }

    @Override
    public void newSection(SectionProperties master) {
        state.assertOpen();
        if (!hasOpenVolume) {
            newVolume(master);
        }
        closeOpenSection();
        hasOpenSection = true;
    }

    private void closeOpenVolume() {
        closeOpenSection();
        if (hasOpenVolume) {
            hasOpenVolume = false;
        }
    }

    private void closeOpenSection() {
        closeOpenPage();
        if (hasOpenSection) {
            hasOpenSection = false;
        }
    }

    private void closeOpenPage() {
        if (hasOpenPage) {
            hasOpenPage = false;
        }
    }

    @Override
    public void close() {
        if (state.isClosed()) {
            return;
        }
        state.assertOpen();
        closeOpenVolume();
        pst.close();
        state.close();
    }

}
