package org.daisy.braille.pef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Provides functionality to check if files are equal. Both binary and a looser XML-file compare
 * are provided. 
 * @author Joel Håkansson
 */
public class XMLFileCompare extends FileCompare {
	//private final static String TRANSFORMER_FACTORY_KEY = "javax.xml.transform.TransformerFactory";
	private final TransformerFactory factory;

	/**
	 * Creates a new FileCompare object
	 * @param factory
	 */
	public XMLFileCompare(TransformerFactory factory) {
		this(factory, false);
	}
	
	/**
	 * Creates a new FileCompare object
	 * @param factory
	 * @param keepTempFiles
	 */
	public XMLFileCompare(TransformerFactory factory, boolean keepTempFiles) {
		super(keepTempFiles);
		this.factory = factory;
	}
	
	/**
	 * Compare the input streams as XML. THe files are considered equal if they are binary equal once
	 * transformed through the same transparent XSLT (whitespace is normalized on text nodes)
	 * using the same transformer implementation.
	 * @param f1 the first input stream
	 * @param f2 the second input stream
	 * @return returns true if the streams are equal, false otherwise
	 * @throws IOException if IO fails
	 * @throws TransformerException if transformation fails
	 */
	public boolean compareXML(InputStream f1, InputStream f2) throws IOException, TransformerException {
		//String originalTransformer = System.getProperty(TRANSFORMER_FACTORY_KEY);
		//System.setProperty(TRANSFORMER_FACTORY_KEY, "net.sf.saxon.TransformerFactoryImpl");
		try {
			factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
		} catch (IllegalArgumentException iae) { 
			iae.printStackTrace();
		}
        t1 = File.createTempFile("FileCompare", ".tmp");
        t2 = File.createTempFile("FileCompare", ".tmp");
        try {
	        StreamSource xml1 = new StreamSource(f1);
	        StreamSource xml2 = new StreamSource(f2);
	        Source xslt;
	        Transformer transformer;
	        
	        xslt = new StreamSource(this.getClass().getResourceAsStream("resource-files/normalize.xsl"));
	        transformer = factory.newTransformer(xslt);
	        transformer.transform(xml1, new StreamResult(t1));
	        
	        xslt = new StreamSource(this.getClass().getResourceAsStream("resource-files/normalize.xsl"));
	        transformer = factory.newTransformer(xslt);
	        transformer.transform(xml2, new StreamResult(t2));
	
	        return compareBinary(new FileInputStream(t1), new FileInputStream(t2));
        } finally {
        	if (!keepTempFiles) {
	        	if (!t1.delete()) {
	        		t1.deleteOnExit();
	        	}
	        	if (!t2.delete()) {
	        		t2.deleteOnExit();
	        	}
        	}
        	/*
        	if (originalTransformer!=null) {
        		System.setProperty(TRANSFORMER_FACTORY_KEY, originalTransformer);
        	} else {
        		System.clearProperty(TRANSFORMER_FACTORY_KEY);
        	}*/
        }
	}
}
