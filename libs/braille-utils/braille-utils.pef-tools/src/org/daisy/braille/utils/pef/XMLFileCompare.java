package org.daisy.braille.utils.pef;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
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
	private final Templates templates;
	private final boolean keepTempFiles;
	private File t1;
	private File t2;

	/**
	 * Creates a new FileCompare object
	 * @param factory the transformer factory
	 */
	public XMLFileCompare(TransformerFactory factory) {
		this(factory, false);
	}

	/**
	 * Creates a new FileCompare object
	 * @param factory the transformer factory
	 * @param keepTempFiles true if temporary files should be kept, false otherwise
	 */
	public XMLFileCompare(TransformerFactory factory, boolean keepTempFiles) {
		super();
		this.templates = init(factory);
		this.keepTempFiles = keepTempFiles;
		this.t1 = null;
		this.t2 = null;
	}
	
	private static Templates init(TransformerFactory factory) {
		try {
			factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
		} catch (IllegalArgumentException iae) { 
			iae.printStackTrace();
		}
		try {
			return factory.newTemplates(new StreamSource(XMLFileCompare.class.getResourceAsStream("resource-files/normalize.xsl")));
		} catch (TransformerConfigurationException e) {
			return null;
		}
	}
	
	/**
	 * Gets the intermediary file created  
	 * from the first argument of the latest call to compareXML 
	 * (as base for the post normalization binary compare).
	 * @return returns the first file
	 * @throws IllegalStateException if temporary files are not kept
	 * or if compareXML has not been called.
	 */
	public File getFileOne() {
		if (!keepTempFiles || t1==null) {
			throw new IllegalStateException();
		}
		return t1;
	}

	/**
	 * Gets the intermediary file created  
	 * from the second argument of the latest call to compareXML 
	 * (as base for the post normalization binary compare).
	 * @return returns the second file
	 * @throws IllegalStateException if temporary files are not kept
	 * or if compareXML has not been called.
	 */
	public File getFileTwo() {
		if (!keepTempFiles || t2==null) {
			throw new IllegalStateException();
		}
		return t2;
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
		if (templates==null) {
			throw new TransformerException("No template.");
		}
		t1 = File.createTempFile("FileCompare", ".tmp");
		t2 = File.createTempFile("FileCompare", ".tmp");
		try {
			StreamSource xml1 = new StreamSource(f1);
			StreamSource xml2 = new StreamSource(f2);

			templates.newTransformer().transform(xml1, new StreamResult(t1));
			templates.newTransformer().transform(xml2, new StreamResult(t2));

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
		}
	}
}
