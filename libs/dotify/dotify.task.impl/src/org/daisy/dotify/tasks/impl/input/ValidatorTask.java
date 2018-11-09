package org.daisy.dotify.tasks.impl.input;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.dotify.common.xml.EntityResolverCache;
import org.daisy.dotify.common.xml.XMLInfo;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.daisy.streamline.api.media.AnnotatedFile;
import org.daisy.streamline.api.media.DefaultAnnotatedFile;
import org.daisy.streamline.api.media.InputStreamSupplier;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadOnlyTask;
import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.ValidatorMessage;
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
		return validate(new UrlInputStreamSupplier(input), javaLogging, schemas);
	}

	/**
	 * Validates the contents of the specified resource against the specified validation schema.
	 * @param input the input
	 * @param schemas the schemas
	 * @param javaLogging true if validation messages should be written to java logging, false otherwise
	 * @return returns true if the resource is valid, false otherwise
	 * @throws ValidatorException if validation fails
	 */
	public static ValidationReport validate(InputStreamSupplier input, boolean javaLogging, URL ... schemas) throws ValidatorException {
		URL inputUrl;
		try {
			inputUrl = new URL(input.getSystemId());
		} catch (MalformedURLException e1) {
			throw new ValidatorException(e1);
		}
		XMLInfo info;
		try (InputStream is = input.newInputStream()) {
			InputSource source = new InputSource(is);
			source.setSystemId(input.getSystemId());
			info = XMLTools.parseXML(source, true);
		} catch (XMLToolsException | IOException e1) {
			throw new ValidatorException(e1);
		}
		ValidatorTaskErrorHandler errorHandler = new ValidatorTaskErrorHandler(inputUrl, javaLogging);
		if (info.getSystemId()!=null || info.getPublicId()!=null) {
			try (InputStream is = input.newInputStream()) {
				InputSource source = new InputSource(is);
				source.setSystemId(input.getSystemId());
				runDTDValidation(source, errorHandler);
			} catch (IOException e) {
				errorHandler.addMessage(
						new ValidatorMessage.Builder(ValidatorMessage.Type.FATAL_ERROR)
						.exception(e)
						.build()
				);
			}
		}
		for (URL schema : schemas) {
			runSchemaValidation(input, schema, errorHandler);
		}
		return errorHandler.buildReport();
	}

	private static boolean runDTDValidation(InputSource input, ValidatorTaskErrorHandler errorHandler) {
		SAXParser saxParser = null;
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
		try {
			saxParser = factory.newSAXParser();
			saxParser.getXMLReader().setErrorHandler(errorHandler);
			saxParser.getXMLReader().setContentHandler(new DefaultHandler());
			saxParser.getXMLReader().setEntityResolver(new EntityResolverCache());
			saxParser.getXMLReader().parse(input);
			return true;
		} catch (SAXException | ParserConfigurationException | IOException e) {
			errorHandler.addMessage(
					new ValidatorMessage.Builder(ValidatorMessage.Type.FATAL_ERROR)
					.exception(e)
					.build()
				);
		}
		return false;
	}

	private static boolean runSchemaValidation(InputStreamSupplier input, URL schema, ValidatorTaskErrorHandler errorHandler) {
		PropertyMapBuilder propertyBuilder = new PropertyMapBuilder();
    	RngProperty.CHECK_ID_IDREF.add(propertyBuilder);
		propertyBuilder.put(ValidateProperty.ERROR_HANDLER, errorHandler);
		propertyBuilder.put(ValidateProperty.ENTITY_RESOLVER, new EntityResolverCache());
		PropertyMap map = propertyBuilder.toPropertyMap();
        ValidationDriver vd = new ValidationDriver(map);
        try (InputStream is = schema.openStream()) {
    		InputSource source = new InputSource();
    		source.setSystemId(schema.toString());
			vd.loadSchema(source);
        } catch (SAXException | IOException e) {
			errorHandler.addMessage(
					new ValidatorMessage.Builder(ValidatorMessage.Type.FATAL_ERROR)
					.message("Failed to load schema: " + schema)
					.exception(e)
					.build()
			);
			return false;
		}
        try (InputStream is = input.newInputStream()) {
			InputSource source = new InputSource(is);
			source.setSystemId(input.getSystemId());
			return vd.validate(source);
		} catch (SAXException | IOException e) {
			errorHandler.addMessage(
					new ValidatorMessage.Builder(ValidatorMessage.Type.FATAL_ERROR)
					.exception(e)
					.build()
			);
			return false;
		}
	}
	
	@Override
	public void execute(AnnotatedFile input) throws InternalTaskException {
		try {
			boolean ret = validate(input.getPath().toFile(), schema);
			if (ret) {
				return;
			}
		} catch (ValidatorException e) {
			throw new InternalTaskException("Validation failed.", e);
		}
		throw new InternalTaskException("Validation failed.");
	}
	
	private static class UrlInputStreamSupplier implements InputStreamSupplier {
		private final URL url;
		
		public UrlInputStreamSupplier(URL url) {
			this.url = url;
		}

		@Override
		public InputStream newInputStream() throws IOException {
			return url.openStream();
		}

		@Override
		public String getSystemId() {
			return url.toString();
		}
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
	@Deprecated
	public void execute(File input) throws InternalTaskException {
		execute(DefaultAnnotatedFile.with(input).build());
	}

}
