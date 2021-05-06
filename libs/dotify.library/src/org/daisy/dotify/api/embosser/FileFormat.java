package org.daisy.dotify.api.embosser;

import org.daisy.dotify.api.factory.Factory;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

import java.io.OutputStream;


/**
 * @author Bert Frees
 */
public interface FileFormat extends Factory, FileFormatProperties {

    /**
     * Returns true if the table is supported, false otherwise.
     *
     * @param table the table
     * @return returns true if the table is supported, false otherwise
     */
    public boolean supportsTable(Table table);

    /**
     * Gets the table filter.
     *
     * @return returns the table filter
     */
    public TableFilter getTableFilter();

    /**
     * Creates a new embosser writer.
     *
     * @param os the output stream
     * @return returns a new embosser writer
     */
    public EmbosserWriter newEmbosserWriter(OutputStream os);

}
