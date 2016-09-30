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
package org.daisy.braille.pef;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.daisy.braille.api.validator.Validator;
import org.daisy.braille.api.validator.ValidatorFactoryService;

/**
 * Merges several single volume PEF-files into one. Metadata is collected from
 * the first file. The order of the files is determined by the file names.
 * Two sorting algorithms are used:
 * <ol>
 * <li>standard, which sorts character by character in strict alphabetical order</li>
 * <li>numeral grouping, which sorts groups of digits as numbers at the position in the string
 * where they occur.
 * </ol>
 * 
 * @author Joel Håkansson
 */
public class PEFFileMerger {
	/**
	 * Defines sorting types
	 */
	public enum SortType {
		/**
		 * Sort groups of digits as numbers
		 */
		NUMERAL_GROUPING,
		/**
		 * Sort alphabetically
		 */
		STANDARD
		};

	private Logger logger;
	private final ValidatorFactoryService validatorFactory;

	/**
	 * Creates a new PEFFileMerger
	 */
	public PEFFileMerger(ValidatorFactoryService validatorFactory) {
		logger = Logger.getLogger(this.getClass().getCanonicalName());
		this.validatorFactory = validatorFactory;
	}

	/**
	 * Merges several PEF-files into one.
	 * @param input input directory
	 * @param os output file
	 * @param identifier identifier of the new publication
	 * @param sort sort type
	 * @return returns true if merge was successful, false otherwise
	 */
	public boolean merge(File input, OutputStream os, String identifier, SortType sort) {
		//progress(0);
		if (!input.isDirectory()) {
			throw new IllegalArgumentException("Input must be an existing directory " + input);
		}
		File[] files = input.listFiles(new FileFilter(){
                        @Override
			public boolean accept(File pathname) {
				return pathname.isFile();
			}});
        switch (sort) {
	    	case NUMERAL_GROUPING:
		        Arrays.sort(files, new Comparator<File>() {
                                        @Override
					public int compare(File o1, File o2) {
						NumeralSortString s1 = new NumeralSortString(o1.getName().toLowerCase());
						NumeralSortString s2 = new NumeralSortString(o2.getName().toLowerCase());
						return s1.compareTo(s2);
					}});
	    		break;
	    	case STANDARD:
	    		Arrays.sort(files);
	    		break;
        }

		try {
			Validator v = validatorFactory.newValidator(PEFValidator.class.getName());
			if (v!=null) {
				v.setFeature(PEFValidator.FEATURE_MODE, PEFValidator.Mode.FULL_MODE);
				sendMessage("Checking input files");
		        for (File f : files) {
		        	sendMessage("Examining " + f.getName(), Level.INFO);
		        	if (!v.validate(f.toURI().toURL())) {
		        		sendMessage("Validation of input file \"" + f.getName() + "\" failed.", Level.SEVERE);
		        		return false;
		        	}
		        	sendMessage(f.getName() + " ok!", Level.FINE);
		        }
		        sendMessage("Input files ok");
	        } else {
	        	sendMessage("Cannot find validator", Level.WARNING);
	        	return false;
	        }

	        sendMessage("Assembling files");
	        //progress(1);
	        if (!writeFile(files, os, identifier)) {
	        	sendMessage("Assemby failed");
	        	return false;
	        } else {
	        	sendMessage("Done!");
	        	return true;
	        }
		} catch (MalformedURLException e) {
			//throw new TransformerRunException("MalformedURLException", e);
			return false;
		} catch (XMLStreamException e) {
			// throw new TransformerRunException("XMLStreamException", e);
			return false;
		} catch (IOException e) {
			// throw new TransformerRunException("IOException", e);
			return false;
		}
	}
	
	private boolean writeFile(File[] volumes, OutputStream os, String identifier) throws XMLStreamException, IOException {
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
		inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);        
        inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);

		FileInputStream is = new FileInputStream(volumes[0]);
		XMLEventReader reader = inFactory.createXMLEventReader(is);
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
		XMLEventWriter writer = outputFactory.createXMLEventWriter(os, "UTF-8");
		QName volume = new QName("http://www.daisy.org/ns/2008/pef", "volume");
		QName body = new QName("http://www.daisy.org/ns/2008/pef", "body");
		QName dcIdentifier = new QName("http://purl.org/dc/elements/1.1/", "identifier");
		while (reader.hasNext()) {
			XMLEvent event = reader.nextEvent();
			if (event.getEventType()==XMLStreamConstants.END_ELEMENT && body.equals(event.asEndElement().getName())) {
				// start copying
				boolean first = true;
				for (File f : volumes) {
					if (first) {
						// skip first volume, it has already been processed
						first = false;
					} else {
						FileInputStream i2 = new FileInputStream(f);
						XMLEventReader r2 = inFactory.createXMLEventReader(i2);
						boolean copy = false;
						while (r2.hasNext()) {
							XMLEvent e2 = r2.nextEvent();
							if (e2.getEventType()==XMLStreamConstants.START_ELEMENT && volume.equals(e2.asStartElement().getName())) {
								copy = true;
							}
							if (copy) {
								writer.add(e2);
							}
							if (e2.getEventType()==XMLStreamConstants.END_ELEMENT && volume.equals(e2.asEndElement().getName())) {
								copy = false;
							}
						}
						r2.close();
						i2.close();
					}
				}
			}
			if (event.getEventType()==XMLStreamConstants.START_ELEMENT && dcIdentifier.equals(event.asStartElement().getName())) {
				while (!
						(event.getEventType()==XMLStreamConstants.END_ELEMENT && dcIdentifier.equals(event.asEndElement().getName()))) {
					if (event.getEventType()==XMLStreamConstants.CHARACTERS) {
						writer.add(eventFactory.createCharacters(identifier));
					} else {
						writer.add(event);
					}
					event = reader.nextEvent();
				}
				writer.add(event);
			} else {
				writer.add(event);
			}
		}
		writer.close();
		reader.close();
		os.close();
		is.close();

		return true;
	}

	private void sendMessage(String msg) {
		sendMessage(msg, Level.INFO);
	}
	
	private void sendMessage(String msg, Level level) {
		logger.log(level, msg);
	}


}
