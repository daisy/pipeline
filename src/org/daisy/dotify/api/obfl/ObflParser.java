package org.daisy.dotify.api.obfl;

import java.util.List;

import javax.xml.stream.XMLEventReader;

import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterException;
import org.daisy.dotify.api.writer.MetaDataItem;


/**
 * Provides an OBFL-parser. The parser reads an OBFL-file and places
 * its contents in a formatter instance.
 * 
 * @author Joel Håkansson
 */
public interface ObflParser {
	
	/**
	 * Parses the input places its contents in the supplied formatter.
	 * @param input the OBFL
	 * @param formatter the formatter
	 * @throws FormatterException if there is a problem writing contents to the formatter 
	 * @throws ObflParserException if there is a problem reading the OBFL
	 */
	public void parse(XMLEventReader input, Formatter formatter) throws FormatterException, ObflParserException;
	
	/**
	 * Gets a list of meta data items collected by the latest call to {@link #parse(XMLEventReader, Formatter)}.
	 * @return returns a list of meta data items
	 */
	public List<MetaDataItem> getMetaData();
}
