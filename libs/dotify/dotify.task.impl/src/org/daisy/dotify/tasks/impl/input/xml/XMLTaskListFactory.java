package org.daisy.dotify.tasks.impl.input.xml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.daisy.dotify.common.io.ResourceLocator;
import org.daisy.dotify.common.io.ResourceLocatorException;
import org.daisy.dotify.tasks.impl.input.ValidatorTask;
import org.daisy.dotify.tasks.tools.XsltTask;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.InternalTaskException;

enum XMLTaskListFactory {
	INSTANCE;
	private static final Logger logger = Logger.getLogger(XMLTaskListFactory.class.getCanonicalName());
	private static final String TEMPLATES_PATH = "templates/";
	private Map<String, Function<XMLConfig, List<InternalTask>>> props;

	private XMLTaskListFactory() {
		props = new HashMap<>();
		props.put("dtbook@http://www.daisy.org/z3986/2005/dtbook/", conf->createTaskList("dtbook.properties", conf));
		props.put("html@http://www.w3.org/1999/xhtml", conf->createTaskList("html.properties", conf));
	}

	static XMLTaskListFactory getInstance() {
		return INSTANCE;
	}
	
	private Function<XMLConfig, List<InternalTask>> getConfigFileName(String rootElement, String rootNS) {
		if (rootNS!=null) {
			return props.get(rootElement+"@"+rootNS);
		} else {
			return props.get(rootElement);
		}
	}

	List<InternalTask> createTaskList(XMLConfig config) throws InternalTaskException {
		try {
			Function<XMLConfig, List<InternalTask>> func = getConfigFileName(config.getRootElement(), config.getRootNS());
			if (func!=null) {
				return func.apply(config);
			} else {
				// Generic XML support
				return createTaskList(null, config);
			}
		} catch (TaskCreationException e) {
			// Wrap in checked exception
			throw new InternalTaskException(e);
		}
	}
	
	private static List<InternalTask> createTaskList(String inputformat, XMLConfig config) throws TaskCreationException {
		if (inputformat !=null && "".equals(inputformat)) {
			return new ArrayList<>();
		}
		String xmlformat = "xml.properties";
		String basePath = TEMPLATES_PATH + config.getTemplate() + "/";
		if (inputformat!=null) {
			try {
				return readConfiguration(config.getRootElement(), config.getLocalLocator(), basePath + inputformat, config.getXsltParams());
			} catch (ResourceLocatorException e) {
				logger.fine("Cannot find localized URL " + basePath + inputformat);
			}
		}
		try {
			return readConfiguration(config.getRootElement(), config.getLocalLocator(), basePath + xmlformat, config.getXsltParams());
		} catch (ResourceLocatorException e) {
			logger.fine("Cannot find localized URL " + basePath + xmlformat);
		}
		if (inputformat!=null) {
			try {
				return readConfiguration(config.getRootElement(), config.getCommonLocator(), basePath + inputformat, config.getXsltParams());
			} catch (ResourceLocatorException e) {
				logger.fine("Cannot find common URL " + basePath + inputformat);
			}
		}
		try {
			return readConfiguration(config.getRootElement(), config.getCommonLocator(), basePath + xmlformat, config.getXsltParams());
		} catch (ResourceLocatorException e) {
			logger.fine("Cannot find common URL " + basePath + xmlformat);
		}
		throw new TaskCreationException("Unable to open a configuration stream for the format.");
	}
	
	private static List<InternalTask> readConfiguration(String type, ResourceLocator locator, String path, Map<String, Object> xsltParams) throws TaskCreationException, ResourceLocatorException {
		URL t = locator.getResource(path);
		List<InternalTask> setup = new ArrayList<>();				
		try {
			Properties pa = new Properties();
			try {
				logger.fine("Opening stream: " + t.getFile());					
				pa.loadFromXML(t.openStream());
			} catch (IOException e) {
				logger.log(Level.FINE, "Cannot open stream: " + t.getFile(), e);
				throw new ResourceLocatorException("Cannot open stream");
			}
			addValidationTask(type, removeSchemas(pa, "validation"), setup, locator);
			addXsltTask(type, removeSchemas(pa, "transformation"), setup, locator, xsltParams); 
			for (Object key : pa.keySet()) {
				logger.info("Unrecognized key: " + key);							
			}
		} catch (IOException e) {
			throw new TaskCreationException("Unable to open settings file.", e);
		}
		
		return setup;
	}

	private static void addValidationTask(String type, String[] schemas, List<InternalTask> setup, ResourceLocator locator) throws ResourceLocatorException {
		if (schemas!=null) {
			for (String s : schemas) {
				if (s!=null && !s.equals("")) {
					setup.add(new ValidatorTask(type + " conformance checker: " + s, locator.getResource(s)));
				}
			}
		} 
	}
	
	private static void addXsltTask(String type, String[] schemas, List<InternalTask> setup, ResourceLocator locator, Map<String, Object> xsltParams) throws ResourceLocatorException {
		if (schemas!=null) {
			for (String s : schemas) {
				if (s!=null && s!="") {
					setup.add(new XsltTask(type + " to OBFL converter", locator.getResource(s), xsltParams));
				}
			}
		}
	}
	private static String[] removeSchemas(Properties p, String key) {
		Object o = p.remove(key);
		String value = (o instanceof String) ? (String)o : null;
		if (value==null) {
			return null;
		} else {
			return value.split("\\s*,\\s*");
		}
	}

	Set<String> listFileFormats() {
		return props.keySet().stream().map(s->{
			int inx;
			if ((inx = s.indexOf('@')) > -1) {
				return s.substring(0, inx);
			} else {
				return s;
			}
		}).collect(Collectors.toSet());
	}

}
