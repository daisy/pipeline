package org.daisy.dotify.tasks.impl.input;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadOnlyTask;
import org.daisy.dotify.api.validity.ValidationReport;
import org.daisy.dotify.api.validity.ValidatorMessage;
import org.daisy.dotify.api.validity.ValidatorMessage.Type;
import org.daisy.dotify.common.xml.EntityResolverCache;
import org.daisy.dotify.common.xml.XMLInfo;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.prop.rng.RngProperty;

/**
 * <p>This task validates the input file against the given schema. The 
 * task throws an exception if anything goes wrong.</p>
 * 
 * <p>Input file type requirement: XML</p>
 * 
 * @author Joel Håkansson
 */
public class ValidatorTask extends ReadOnlyTask {
	private URL schema;

	/**
	 * Creates a new validator task with the specified name and schema
	 * @param name the name of the task
	 * @param schema the validation schema
	 */
	public ValidatorTask(String name, URL schema) {
		super(name);
		this.schema = schema;
	}
	
	/**
	 * Validates the specified file against the specified validation schema
	 * @param input the input file
	 * @param schema the schema
	 * @return returns true if the file is valid, false otherwise
	 * @throws ValidatorException if validation fails
	 */
	public static boolean validate(File input, URL schema) throws ValidatorException {
		URL url;
		try {
			url = input.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new ValidatorException("Validation failed.", e);
		}
		return validate(url, true, schema).isValid();
	}

	/**
	 * Validates the contents of the specified URL against the specified validation schema.
	 * @param input the input URL
	 * @param schemas the schemas
	 * @param javaLogging true if validation messages should be written to java logging, false otherwise
	 * @return returns true if the file is valid, false otherwise
	 * @throws ValidatorException if validation fails
	 */
	public static ValidationReport validate(URL input, boolean javaLogging, URL ... schemas) throws ValidatorException {
		XMLInfo info;
		try {
			info = XMLTools.parseXML(input.toURI(), true);
		} catch (XMLToolsException | URISyntaxException e1) {
			throw new ValidatorException(e1);
		}
		ValidatorTaskErrorHandler errorHandler = new ValidatorTaskErrorHandler(input, javaLogging);
		if (info.getSystemId()!=null || info.getPublicId()!=null) {
			runDTDValidation(input, errorHandler);
		}
		for (URL schema : schemas) {
			runSchemaValidation(input, schema, errorHandler);
		}
		return errorHandler.buildReport();
	}

	private static boolean runDTDValidation(URL url, ValidatorTaskErrorHandler errorHandler) {
		SAXParser saxParser = null;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		try {
			saxParser = factory.newSAXParser();
			saxParser.getXMLReader().setErrorHandler(errorHandler);
			saxParser.getXMLReader().setContentHandler(new DefaultHandler());
			saxParser.getXMLReader().setEntityResolver(new EntityResolverCache());
			saxParser.getXMLReader().parse(new InputSource(url.openStream()));
			return true;
		} catch (SAXException | ParserConfigurationException | IOException e) {
			errorHandler.addMessage(
					new ValidatorMessage.Builder(Type.FATAL_ERROR)
					.exception(e)
					.build()
				);
		}
		return false;
	}

	private static boolean runSchemaValidation(URL url, URL schema, ValidatorTaskErrorHandler errorHandler) {
		PropertyMapBuilder propertyBuilder = new PropertyMapBuilder();
    	RngProperty.CHECK_ID_IDREF.add(propertyBuilder);
		propertyBuilder.put(ValidateProperty.ERROR_HANDLER, errorHandler);
		propertyBuilder.put(ValidateProperty.ENTITY_RESOLVER, new EntityResolverCache());
		PropertyMap map = propertyBuilder.toPropertyMap();
        ValidationDriver vd = new ValidationDriver(map);
        try {
			vd.loadSchema(configureInputSource(schema));
        } catch (SAXException | IOException | URISyntaxException e) {
			errorHandler.addMessage(
					new ValidatorMessage.Builder(Type.FATAL_ERROR)
					.message("Failed to load schema: " + schema)
					.exception(e)
					.build()
			);
			return false;
		}
        try {
			return vd.validate(configureInputSource(url));
		} catch (SAXException | IOException | URISyntaxException e) {
			errorHandler.addMessage(
					new ValidatorMessage.Builder(Type.FATAL_ERROR)
					.exception(e)
					.build()
			);
			return false;
		}
	}
	
	private static InputSource configureInputSource(URL url) throws IOException, URISyntaxException {
		InputSource is = new InputSource(url.openStream());
		is.setSystemId(url.toURI().toString());
		return is;
	}

	@Override
	public void execute(File input) throws InternalTaskException {
		try {
			boolean ret = validate(input, schema);
			//FileUtils.copy(input, output);
			if (ret) {
				return;
			}
		} /*catch (IOException e) {
			throw new InternalTaskException("Failed to copy file.", e);
		} */catch (ValidatorException e) {
			throw new InternalTaskException("Validation failed.", e);
		}
		throw new InternalTaskException("Validation failed.");
	}
	
	private static class ValidatorTaskErrorHandler implements ErrorHandler {
		private final Logger logger;
		private final boolean doJavaLogging;
		private final ValidationReport.Builder report;
		
		public ValidatorTaskErrorHandler(URL source, boolean doJavaLogging) {
			this.logger = Logger.getLogger(ValidatorTaskErrorHandler.class.getCanonicalName());
			this.doJavaLogging = doJavaLogging;
			this.report = new ValidationReport.Builder(source);
		}
		
		public ValidationReport buildReport() {
			return report.build();
		}

		public void addMessage(ValidatorMessage message) {
			if (doJavaLogging) {
				// log accepts null exceptions
				logger.log(Level.WARNING, message.getMessage().orElse(""), message.getException().orElse(null));
			}
			report.addMessage(message);
		}
	
		@Override
		public void error(SAXParseException exception) throws SAXException {
			if (doJavaLogging) {
				logger.log(Level.WARNING, "Validation error" + getLineColumn(exception) + ": " + exception.getMessage());
			}
			report.addMessage(
					setLineColumn(new ValidatorMessage.Builder(ValidatorMessage.Type.ERROR).exception(exception), exception)
					.build()
					);
		}
	
		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			throw new SAXException(exception);
		}
	
		@Override
		public void warning(SAXParseException exception) throws SAXException {
			if (doJavaLogging) {
				logger.log(Level.INFO, "Parse warning " + getLineColumn(exception), exception);
			}
			report.addMessage(
					setLineColumn(new ValidatorMessage.Builder(ValidatorMessage.Type.WARNING).exception(exception), exception)
					.build()
					);
		}
		
		private ValidatorMessage.Builder setLineColumn(ValidatorMessage.Builder b, SAXParseException ex) {
			if (ex.getLineNumber()>=0) {
				b.lineNumber(ex.getLineNumber());
			}
			if (ex.getColumnNumber()>=0) {
				b.columnNumber(ex.getColumnNumber());
			}
			return b;
		}
		
		private String getLineColumn(SAXParseException e) {
			if (e.getLineNumber()<0 && e.getColumnNumber()<0) {
				return "";
			} else { 
				boolean both = (e.getLineNumber()>=0 && e.getColumnNumber()>=0);
				return " (at "+(e.getLineNumber()>=0?"line: "+e.getLineNumber():"")+
						(both?" ":"")+
						(e.getColumnNumber()>=0?"column: "+e.getColumnNumber():"")
						+")";
			}
		}
	}

	@Override
	public void execute(AnnotatedFile input) throws InternalTaskException {
		execute(input.getFile());
	}

}
