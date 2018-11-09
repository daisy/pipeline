package org.daisy.dotify.api.embosser;

import java.io.OutputStream;

import org.daisy.dotify.api.factory.Factory;
import org.daisy.dotify.api.paper.Area;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.Paper;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.TableFilter;

/**
 * Provides an interface for common properties of an Embosser.
 * @author Joel Håkansson
 */
public interface Embosser extends Factory, EmbosserFactoryProperties, EmbosserProperties {

	/**
	 * Returns true if dimension is supported
	 * @param printPage the dimension to test
	 * @return returns true if dimension is supported
	 */
	//TODO: check whether this method needs to be public / is needed altogether
	public boolean supportsPrintPage(PrintPage printPage);

	/**
	 * Returns true if the page format is supported.
	 * @param pageFormat the page format
	 * @return returns true if the page format is supported, false otherwise
	 */
	public boolean supportsPageFormat(PageFormat pageFormat);

	/**
	 * Returns thue if the paper is supported.
	 * @param paper the paper
	 * @return returns true if the paper is supported, false otherwise
	 */
	public boolean supportsPaper(Paper paper);

	/**
	 * Returns true if table is supported
	 * @param table the table to test
	 * @return returns true if table is supported
	 */
	public boolean supportsTable(Table table);

	/**
	 * Gets a table filter that returns true if supportsTable returns true
	 * @return returns a table filter
	 */
	public TableFilter getTableFilter();

	/**
	 * Gets a new EmbosserWriter that writes to the supplied OutputStream
	 * @param os the OutputStream that the EmbosserWriter should use
	 * @return returns a new EmbosserWriter
	 */
	public EmbosserWriter newEmbosserWriter(OutputStream os);

	/**
	 * Gets a new EmbosserWriter that writes to the supplied Device
	 * @param device the device that the EmbosserWriter should use
	 * @return returns a new EmbosserWriter
	 */
	public EmbosserWriter newEmbosserWriter(Device device);

	/**
	 * Gets the max width for the specified page format
	 * @param pageFormat the page format
	 * @return returns the max width for the specified page format
	 */
	public int getMaxWidth(PageFormat pageFormat);

	/**
	 * Gets the max height for the specified page format
	 * @param pageFormat the page format
	 * @return returns the max height for the specified page format
	 */
	public int getMaxHeight(PageFormat pageFormat);

	/**
	 * Gets the dimensions of one print page for the specified page format
	 * @param pageFormat the page format
	 * @return returns the dimensions of one print page for the specified page format
	 */
	public PrintPage getPrintPage(PageFormat pageFormat);

	/**
	 * Gets the printable area for the specified page format
	 * @param pageFormat the page format
	 * @return returns the printable area for the specified page format
	 */
	public Area getPrintableArea(PageFormat pageFormat);

}
