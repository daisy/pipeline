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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Stack;
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

import org.daisy.validator.Validator;
import org.daisy.validator.ValidatorFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Splits a PEF-file into several single volume files. The main purpose is to 
 * interact with software that operates on one volume at a time.
 * @author Joel Håkansson
 *
 */
public class PEFFileSplitter implements ErrorHandler  {
	/**
	 * Defines the default prefix for generated file names.
	 */
	public final static String PREFIX = "volume-";
	/**
	 * Defines the default postfix for generated file names.
	 */
	public final static String POSTFIX = ".pef";
	enum State {HEADER, BODY, FOOTER};
	private Logger logger;

	/**
	 * Creates a new PEFFileSplitter object.
	 */
	public PEFFileSplitter() {
		logger = Logger.getLogger(PEFFileSplitter.class.getCanonicalName());
	}
	
	/**
	 * Splits a PEF-file into several single volume PEF-files.
	 * @param input input PEF-file
	 * @param directory output directory
	 * @return returns true if split was successful, false otherwise
	 * @throws IllegalArgumentException if input is not a file
	 */
	public boolean split(File input, File directory) {
		if (!input.isFile()) {
			throw new IllegalArgumentException("Input is not a file: " + input);
		}
		String inputName = input.getName();
		String inputExt = ".pef";
		int index = inputName.lastIndexOf('.');
		if (index >= 0) {
			if (index < inputName.length()) {
				inputExt = inputName.substring(index);
			}
			inputName = inputName.substring(0, index);
			
		}
		try {
			return split(new FileInputStream(input), directory, inputName + "-", inputExt);
		} catch (FileNotFoundException e) {
			return false;
		}
	}

	/**
	 * Splits the PEF-document provided as an input stream into several single volume PEF-files using
	 * the default file name pre- and postfix.
	 * @param is the input stream to the PEF-document
	 * @param directory the output directory
	 * @return returns true if split was successful, false otherwise
	 */
	public boolean split(InputStream is, File directory) {
		return split(is, directory, PREFIX, POSTFIX);
	}

	/**
	 * Splits the PEF-document provided as an input stream into several single volume PEF-files using
	 * the supplied file name pre- and postfix.
	 * @param is the input stream to the PEF-document
	 * @param directory the output directory
	 * @param prefix the prefix to use
	 * @param postfix the postfix to use
	 * @return returns true if split was successful, false otherwise
	 */
	public boolean split(InputStream is, File directory, String prefix, String postfix) {
		//progress(0);

		directory.mkdirs();
        XMLInputFactory inFactory = XMLInputFactory.newInstance();
		inFactory.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);        
        inFactory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        inFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.TRUE);
        /*
    	try {
			inFactory.setXMLResolver(new StaxEntityResolver(CatalogEntityResolver.getInstance()));
		} catch (CatalogExceptionNotRecoverable e1) {
			e1.printStackTrace();
		}*/
		sendMessage("Splitting");
		try {
		    XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			XMLEventReader reader = inFactory.createXMLEventReader(is);
			XMLEventFactory eventFactory = XMLEventFactory.newInstance();
			ArrayList<XMLEvent> header = new ArrayList<XMLEvent>();
			Stack<File> files = new Stack<File>();
			Stack<XMLEventWriter> writers = new Stack<XMLEventWriter>();
			Stack<FileOutputStream> os = new Stack<FileOutputStream>();
			QName volume = new QName("http://www.daisy.org/ns/2008/pef", "volume");
			QName body = new QName("http://www.daisy.org/ns/2008/pef", "body");
			int i = 0;
			State state = State.HEADER;
	        while (reader.hasNext()) {
	            XMLEvent event = reader.nextEvent();
	            if (event.getEventType()==XMLStreamConstants.START_ELEMENT
	            		&& volume.equals(event.asStartElement().getName())) {
	            	state = State.BODY;
	        		i++;
	        		files.push(new File(directory, prefix + i + postfix));
	    			os.push(new FileOutputStream(files.peek()));
	    			writers.push(outputFactory.createXMLEventWriter(os.peek(), "UTF-8"));
	    			// output header information
	    			boolean ident = false;
	    			QName dcIdentifier = new QName("http://purl.org/dc/elements/1.1/", "identifier");
	    			for (XMLEvent e : header) {
	    				if (e.getEventType()==XMLStreamConstants.START_ELEMENT &&
	    						dcIdentifier.equals(e.asStartElement().getName())) {
	    					ident = true;
	    					writers.peek().add(e);
	    				} else if (ident==true && e.getEventType()==XMLStreamConstants.CHARACTERS) {
	    					ident = false;
	    					XMLEvent e2 = eventFactory.createCharacters(e.asCharacters().getData()+"-"+i);
	    					writers.peek().add(e2);
	    				} else {
	    					writers.peek().add(e);
	    				}
	    			}
		        } else if (event.getEventType()==XMLStreamConstants.END_ELEMENT &&
		            	body.equals(event.asEndElement().getName())) {
            		state = State.FOOTER;
            	}
	            switch (state) {
	            	case HEADER:
	            		//push header event
	            		header.add(event);
	            		break;
	            	case BODY:
	            		writers.peek().add(event);
	            		break;
	            	case FOOTER:
	            		// write footer to all files
	              		for (XMLEventWriter w : writers) {
	            			w.add(event);
	            		}
	            		break;
	            }
	        }
	        for (XMLEventWriter w : writers) {
	        	w.close();
	        }
	        for (FileOutputStream s : os) {
	        	s.close();
	        }
	        is.close();
	        sendMessage("Checking result for errors");
	        //progress(0.5);
	        ValidatorFactory vf = ValidatorFactory.newInstance();
	        Validator v = vf.newValidator(PEFValidator.class.getName());
	        if (v!=null) {
		        v.setFeature(PEFValidator.FEATURE_MODE, PEFValidator.Mode.FULL_MODE);
		        for (File f : files) {
		        	sendMessage("Examining " + f.getName(), Level.FINE);
		        	if (!v.validate(f.toURI().toURL())) {
		        		sendMessage("Validation of result file failed: " + f.getName(), Level.SEVERE);
		        		return false;
		        	}
		        	sendMessage(f.getName() + " ok!", Level.FINE);
		        }
		        sendMessage("All ok!");
	        } else {
	        	sendMessage("Cannot find validator", Level.WARNING);
	        	return false;
	        }
	        sendMessage("Done!");
	        //progress(1);
	        return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//throw new TransformerRunException("FileNotFoundException: ", e);
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
			//throw new TransformerRunException("IOException: ", e);
		} catch (XMLStreamException e) {
			e.printStackTrace();
			return false;
			//throw new TransformerRunException("XMLStreamException: ", e);
		}/* catch (ValidationException e) {
			throw new TransformerRunException("ValidationException: ", e);
		}*/
	}

	public void error(SAXParseException exception) throws SAXException {
		throw new SAXException(exception);
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		throw new SAXException(exception);
	}

	public void warning(SAXParseException exception) throws SAXException {
		sendMessage(exception.toString());
	}
	
	private void sendMessage(String msg) {
		sendMessage(msg, Level.INFO);
	}
	
	private void sendMessage(String msg, Level level) {
		logger.log(level, msg);
	}

}
