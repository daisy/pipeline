package org.daisy.dotify.formatter.impl.common;

import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.Row;
import org.daisy.dotify.formatter.impl.row.RowImpl;

import java.io.Closeable;
import java.io.IOException;

/**
 * Provides a method for writing pages to a PagedMediaWriter.
 *
 * @author Joel Håkansson
 */
public class WriterHandler implements Closeable {
    private final PagedMediaWriter writer;

    /**
     * Creates a new writer handler.
     *
     * @param writer the PagedMediaWriter to write to
     */
    public WriterHandler(PagedMediaWriter writer) {
        this.writer = writer;
    }

    /**
     * Writes this structure to the suppled PagedMediaWriter.
     *
     * @param volumes the volumes to write
     */
    public void write(Iterable<? extends Volume> volumes) {
        for (Volume v : volumes) {
            writeVolume(v);
        }
    }

    private void writeVolume(Volume v) {
        boolean firstInVolume = true;
        for (Section s : v.getSections()) {
            if (firstInVolume) {
                firstInVolume = false;
                writer.newVolume(s.getSectionProperties());
            }
            writeSection(s);
        }
    }

    private void writeSection(Section s) {
        writer.newSection(s.getSectionProperties());
        for (Page p : s.getPages()) {
            writePage(p);
        }
    }

    private void writePage(Page p) {
        writer.newPage();
        for (Row r : p.getRows()) {
            /*
            This implementation is specific for the RowImpl. If someone would create an another
            implementation of Row it's not likely that they would implement this function.
            */
            if (r instanceof RowImpl && ((RowImpl) r).isInvisible()) {
                continue;
            }
            writer.newRow(r);
        }
    }

    @Override
    public void close() throws IOException {
        writer.close();
    }

}
