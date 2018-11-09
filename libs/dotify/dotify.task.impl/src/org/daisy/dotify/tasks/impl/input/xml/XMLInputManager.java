package org.daisy.dotify.tasks.impl.input.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.common.io.ResourceLocator;
import org.daisy.dotify.common.io.ResourceLocatorException;
import org.daisy.dotify.tasks.impl.FeatureSwitch;
import org.daisy.dotify.tasks.impl.input.DuplicatorTask;
import org.daisy.dotify.tasks.tools.XsltTask;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.option.UserOptionValue;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskSystemException;

/**
 * <p>Provides a method to determine the input format and load the 
 * appropriate settings based on the detected input format.</p>
 * 
 * <p>The InputDetectorTaskSystem is specifically designed to aid 
 * the process of selecting and executing the correct validation rules 
 * and transformation for a given input document and locale.</p>
 * 
 * <p>Note that, input format must be well-formed XML.</p>
 * 
 * <p>Resources are located in the following order:</p>
 * <ul> 
 * <li>localBase/[output format]/[input format].properties</li>
 * <li>localBase/[output format]/xml.properties</li>
 * <li>commonBase/[output format]/[input format].properties</li>
 * <li>commonBase/[output format]/xml.properties</li>
 * </ul>
 * <p>The properties file for the format should contain two entries:</p>
 * <ul>
 * <li>&lt;entry key="validation"&gt;path/to/schema/file&lt;/entry&gt;</li>
 * <li>&lt;entry key="transformation"&gt;path/to/xslt/file&lt;/entry&gt;</li>
 * </ul>
 * <p>Paths in the properties file are relative to the resource base url.</p>
 * <p>Whitespace normalization of the OBFL file is added last in the chain.</p>
 * 
 * @author Joel Håkansson
 *
 */
public class XMLInputManager implements TaskGroup {
	/**
	 * Specifies a location where the intermediary obfl output should be stored
	 */
	static final String OBFL_OUTPUT_LOCATION = "obfl-output-location";
	private static final String TEMPLATE_KEY = "template";
	static final String PROCESS_EDITING_INSTRUCTIONS = "process-editing-instructions";
	private static final String LOCALIZATION_PROPS = "localization.xml";
	private final ResourceLocator localLocator;
	private final ResourceLocator commonLocator;
	private final String name;
	private final Logger logger;

	/**
	 * Creates a new xml input manager with the specified options. 
	 * @param localLocator a locator for local resources
	 * @param commonLocator a locator for common resources
	 */
	public XMLInputManager(ResourceLocator localLocator, ResourceLocator commonLocator) {
		this(localLocator, commonLocator, "XMLInputManager");
	}
	
	/**
	 * Creates a new xml input manager with the specified options. 
	 * @param localLocator a locator for local resources
	 * @param commonLocator a locator for common resources
	 * @param name a name for the task group
	 */
	public XMLInputManager(ResourceLocator localLocator, ResourceLocator commonLocator, String name) {
		this.localLocator = localLocator;
		this.commonLocator = commonLocator;
		this.name = name;
		this.logger = Logger.getLogger(XMLInputManager.class.getCanonicalName());
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters)
			throws TaskSystemException {
		String template;
		if (parameters.get(TEMPLATE_KEY)==null) {
			logger.info("No template set, using default.");
			template = "default";
		} else {
			template = parameters.get(TEMPLATE_KEY).toString().toLowerCase();
		}
		
		List<InternalTask> ret = new ArrayList<>();
		Map<String, Object> params = makeXSLTParams(parameters);

		if ("true".equalsIgnoreCase(parameters.getOrDefault(PROCESS_EDITING_INSTRUCTIONS, "false").toString())) {
			ret.add(new XsltTask("Editing instructions processor", this.getClass().getResource("resource-files/editing-instructions.xsl"), params));
		}

		ret.add(new XMLExpandingTask(template, params, localLocator, commonLocator));

		String keep = (String)parameters.get(OBFL_OUTPUT_LOCATION);
		if (keep!=null && !"".equals(keep)) {
			ret.add(new DuplicatorTask("OBFL archiver", new File(keep)));
		}
		return ret;
	}
	
	private Map<String, Object> makeXSLTParams(Map<String, Object> parameters) {
		Map<String, Object> xsltParams = new HashMap<>();
		{
			Properties p2 = new Properties();
			try {
				p2.loadFromXML(localLocator.getResource(LOCALIZATION_PROPS).openStream());
			} catch (InvalidPropertiesFormatException e) {
				logger.log(Level.FINE, "", e);
			} catch (ResourceLocatorException e) {
				logger.log(Level.FINE, "", e);
			} catch (IOException e) {
				logger.log(Level.FINE, "", e);
			}
			
			for (Object key3 : p2.keySet()) {
				xsltParams.put(key3.toString(), p2.get(key3).toString());
			}
		}
		for (String key2 : parameters.keySet()) {
			xsltParams.put(key2, parameters.get(key2));
		}

		return Collections.unmodifiableMap(xsltParams);
	}

	@Override
	public List<UserOption> getOptions() {
		List<UserOption> ret = new ArrayList<>();
		ret.add(new UserOption.Builder(OBFL_OUTPUT_LOCATION).description("Path to store intermediary OBFL-file.").build());
		if (FeatureSwitch.ENABLE_EDITING_INSTRUCTIONS.isOn()) {
			ret.add(new UserOption.Builder(PROCESS_EDITING_INSTRUCTIONS)
					.description("Process editing instructions")
					.addValue(new UserOptionValue.Builder("true").build())
					.addValue(new UserOptionValue.Builder("false").build())
					.defaultValue("false")
					.build());
		}
		return ret;
	}

}
