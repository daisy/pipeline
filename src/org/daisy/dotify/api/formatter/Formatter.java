package org.daisy.dotify.api.formatter;

import java.io.Closeable;

import org.daisy.dotify.api.translator.BrailleTranslator;


/**
 * <p>Provides an entry point for formatting text.</p>
 * 
 * <p>The result can be passed on to a Paginator or used for displaying
 * on a refreshable braille display.</p>
 *  
 * @author Joel Håkansson
 */
public interface Formatter extends Closeable, FormatterCore {
	/*
	public FilterFactory getFilterFactory();
	*/
	public BrailleTranslator getTranslator();
	
	/*
	public StringFilter getDefaultFilter();
	*/
	/*
	public FilterLocale getFilterLocale();
	*/
	/*
	public void setLocale(FilterLocale locale);
	*/
	
	/**
	 * Opens the Formatter for writing.
	 */
	public void open();


	/**
	 * Start a new Sequence at the current position in the flow.
	 * @param props the SequenceProperties for the new sequence
	 */
	public void newSequence(SequenceProperties props);

	/**
	 * Add a LayoutMaster
	 * @param name The name of the LayoutMaster. This is the named used in when retrieving
	 * a master for a particular sequence from the {@link SequenceProperties}.
	 * @param master the LayoutMaster
	 */
	public void addLayoutMaster(String name, LayoutMaster master);
	
	//public BlockStruct getFlowStruct();

	public Iterable<Volume> getVolumes(VolumeContentFormatter vcf);
}
