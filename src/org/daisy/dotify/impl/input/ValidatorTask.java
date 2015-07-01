package org.daisy.dotify.impl.input;

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

import org.daisy.dotify.api.cr.InternalTaskException;
import org.daisy.dotify.api.cr.ReadOnlyTask;
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

	public ValidatorTask(String name, URL schema) {
		super(name);
		this.schema = schema;
	}
	
	public static boolean validate(File input, URL schema) throws ValidatorException {
		URL url;
		try {
			url = input.toURI().toURL();
		} catch (MalformedURLException e) {
			throw new ValidatorException("Validation failed.", e);
		}
		boolean ret = true;
		XMLInfo info;
		try {
			info = XMLTools.parseXML(input, true);
		} catch (XMLToolsException e1) {
			throw new ValidatorException(e1);
		}
		ValidatorTaskErrorHandler errorHandler = new ValidatorTaskErrorHandler();
		if (info.getSystemId()!=null || info.getPublicId()!=null) {
			ret &= runDTDValidation(url, errorHandler);
		}
		ret &= runSchemaValidation(url, schema, errorHandler);
		return ret && !errorHandler.hasError();

	}

	private static boolean runDTDValidation(URL url, ErrorHandler errorHandler) {
		SAXParser saxParser = null;
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(true);
			saxParser = factory.newSAXParser();
			saxParser.getXMLReader().setErrorHandler(errorHandler);
			saxParser.getXMLReader().setContentHandler(new DefaultHandler());
			saxParser.getXMLReader().setEntityResolver(new EntityResolverCache());
			saxParser.getXMLReader().parse(new InputSource(url.openStream()));
			return true;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private static boolean runSchemaValidation(URL url, URL schema, ErrorHandler errorHandler) {
		
		PropertyMapBuilder propertyBuilder = new PropertyMapBuilder();

        try {
    		propertyBuilder.put(ValidateProperty.ERROR_HANDLER, errorHandler);
    		propertyBuilder.put(ValidateProperty.ENTITY_RESOLVER, new EntityResolverCache());
    		PropertyMap map = propertyBuilder.toPropertyMap();
            ValidationDriver vd = new ValidationDriver(map);
			vd.loadSchema(configureInputSource(schema));
			return vd.validate(configureInputSource(url));
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
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
		private boolean error = false;
		
		public ValidatorTaskErrorHandler() {
			this.logger = Logger.getLogger(ValidatorTaskErrorHandler.class.getCanonicalName());
		}
		
		public boolean hasError() {
			return error;
		}
	
		public void error(SAXParseException exception) throws SAXException {
			logger.log(Level.WARNING, "Validation error" + getLineColumn(exception) + ": " + exception.getMessage());
			error = true;
		}
	
		public void fatalError(SAXParseException exception) throws SAXException {
			throw new SAXException(exception);
		}
	
		public void warning(SAXParseException exception) throws SAXException {
			logger.log(Level.INFO, "Parse warning " + getLineColumn(exception), exception);
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

}
