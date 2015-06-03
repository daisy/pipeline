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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.daisy.factory.AbstractFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;

/**
 * Validates PEF-documents against the official Relax NG schema. Optionally performs additional
 * checks, see the different modes. 
 * @author Joel Håkansson
 */
public class PEFValidator extends AbstractFactory implements org.daisy.braille.api.validator.Validator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5109400956885804582L;
	/**
	 * Key for getFeature/setFeature,
	 * corresponding value should be a {@link Mode} value
	 */
	public final static String FEATURE_MODE = "validator mode";
	/**
	 * Defines the modes available to the validator.
	 */
	public enum Mode {
		/**
		 * Light mode validation only validates the document against the Relax NG schema
		 */
		LIGHT_MODE, 
		/**
		 * In addition to schema validation, performs other tests required by the PEF-specification.
		 */
		FULL_MODE
	};
	private File report;
	private Mode mode;
	
	/**
	 * Creates a new PEFValidator
	 */
	public PEFValidator() {
		this(PEFValidator.class.getCanonicalName());
	}

	PEFValidator(String id) {
		super("PEF Validator", "A validator for PEF 1.0 files.", id);
		this.report = null;
		this.mode = Mode.FULL_MODE;
	}

	public boolean validate(URL input) {
		return validate(input, mode);
	}
	
	private boolean validate(URL input, Mode modeLocal) {
		boolean hasSchematron = true;
		String schemaPath = "resource-files/pef-2008-1-full.rng";
		switch (modeLocal) {
			case FULL_MODE: 
				schemaPath = "resource-files/pef-2008-1-full.rng";
				hasSchematron = true;
				break;
			case LIGHT_MODE: 
				schemaPath = "resource-files/pef-2008-1-light.rng";
				hasSchematron = false;
				break;
		}
		PrintStream orErr = System.err;
		try {
			report = File.createTempFile("Validator", ".tmp");
			report.deleteOnExit();
		} catch (IOException e1) {
			report = null;
		}
		
		PrintStream ps = null;
		try {
			ps = new PrintStream(report);
		} catch (FileNotFoundException e) { }
		try {

			System.setErr(ps);

			boolean ok;
			ok = runValidation(input, this.getClass().getResource(schemaPath));
			if (hasSchematron) {
				try {
					File schematron = transformSchematron(this.getClass().getResource(schemaPath));
					ok &= runValidation(input, schematron.toURI().toURL());
				} catch (Exception e) {
					e.printStackTrace();
					ok = false;
				}
			}
			return ok;
		} finally {
			if (ps != null) {
				ps.close();
			}
			System.setErr(orErr);
		}
	}
	
	private boolean runValidation(URL url, URL schema) {
		TestError errorHandler = new TestError();
		PropertyMapBuilder propertyBuilder = new PropertyMapBuilder();

		propertyBuilder.put(ValidateProperty.ERROR_HANDLER, errorHandler);
		PropertyMap map = propertyBuilder.toPropertyMap();
        ValidationDriver vd = new ValidationDriver(map);
        try {
			vd.loadSchema(new InputSource(schema.openStream()));
			return vd.validate(new InputSource(url.openStream()));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private File transformSchematron(URL schema) throws IOException, SAXException, TransformerException {

        InputSource schSource;
        File schematronSchema = File.createTempFile("schematron", ".tmp");
        schematronSchema.deleteOnExit();

        // Use XSLT to strip out Schematron rules
        Source xml = new StreamSource(schema.toString());
        
        Source xslt = new StreamSource(this.getClass().getResourceAsStream("resource-files/RNG2Schtrn.xsl"));
        TransformerFactory factory = TransformerFactory.newInstance();
        System.err.println(factory.getClass().getName()); 
		try {
			factory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
		} catch (IllegalArgumentException iae) { }
        Transformer transformer = factory.newTransformer(xslt);

        transformer.transform(xml, new StreamResult(schematronSchema.toURI().toString()));
        schSource = new InputSource(schematronSchema.toURI().toString());
        schSource.setSystemId(schematronSchema.toURI().toString());

        return schematronSchema;
	}
	
	static class TestError implements ErrorHandler {
		private boolean hasErrors = false;

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			buildErrorMessage("Warning", exception);
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			hasErrors = true;
			buildErrorMessage("Error", exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			hasErrors = true;
			buildErrorMessage("Fatal error", exception);
		}
		
		public boolean hasErrors() {
			return hasErrors;
		}

		private void buildErrorMessage(String type, SAXParseException e) {
			int line = e.getLineNumber();
			int column = e.getColumnNumber();
			System.err.print(type);
			if (line > -1 || column > -1) {
				System.err.print(" at");
				if (line > -1) {
					System.err.print(" line " + line);
				}
				if (line > -1 && column > -1) {
					System.err.print(",");
				}
				if (column > -1) {
					System.err.print(" column " + column);
				}
			}
			System.err.println(": " + e.getMessage());
		}
	}

	public InputStream getReportStream() {
		if (report==null) {
			return null;
		}
		try {
			return new FileInputStream(report);
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public Object getFeature(String key) {
		if (FEATURE_MODE.equals(key)) {
			return mode;
		} else {
			throw new IllegalArgumentException("Unknown feature: '" + key +"'");
		}
	}

	public Object getProperty(String key) {
		return null;
	}

	public void setFeature(String key, Object value) {
		if (FEATURE_MODE.equals(key)) {
			try {
				mode = (Mode)value;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Unsupported value for " + FEATURE_MODE + " '" + value + "'");
			}
		} else {
			throw new IllegalArgumentException("Unknown feature: '" + key +"'");
		}
		
	}

}
