package org.daisy.dotify.tasks.impl.input.xml;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupInformation;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;

import aQute.bnd.annotation.component.Component;

/**
 * Provides an XML input manager factory. This can be used when implementing
 * XML-formats in this package. It is specifically designed to inject the 
 * correct validation rules and XSLT stylesheet for any XML-format and locale
 * combination into the task chain. See the package documentation for information
 * on how to extend it.
 * 
 * @author Joel Håkansson
 *
 */
@Component
public class XMLInputManagerFactory implements TaskGroupFactory {
	private static final Logger logger = Logger.getLogger(XMLInputManagerFactory.class.getCanonicalName());
	private final XMLL10nResourceLocator locator;

	private final Set<TaskGroupSpecification> supportedSpecifications;
	private final Set<TaskGroupInformation> supportedTaskGroupInformations;
	private final Set<String> supportedLocales;
	
	/**
	 * Creates a new xml input manager factory.
	 */
	public XMLInputManagerFactory() {
		this.locator = XMLL10nResourceLocator.getInstance();
		DefaultInputUrlResourceLocator p = DefaultInputUrlResourceLocator.getInstance();
		Set<String> supportedFormats = p.listFileFormats();
		supportedFormats.add("xml");
		supportedSpecifications = new HashSet<>();
		this.supportedLocales = locator.listSupportedLocales();
		Set<TaskGroupInformation> tmp = new HashSet<>();
		for (String format : supportedFormats) {
			if ("obfl".equals(format)) {
				logger.info("Ignoring obfl.");
			} else {
				for (String locale : supportedLocales) {				
					tmp.add(TaskGroupInformation.newConvertBuilder(format, "obfl").locale(locale).build());
					supportedSpecifications.add(new TaskGroupSpecification(format, "obfl", locale));
				}
			}
		}
		supportedTaskGroupInformations = Collections.unmodifiableSet(tmp);
	}

	@Override
	public boolean supportsSpecification(TaskGroupInformation spec) {
		return listAll().contains(spec);
	}
	
	@Override
	public Set<TaskGroupInformation> listAll() {
		return supportedTaskGroupInformations;
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
        return new XMLInputManager(locator.getResourceLocator(spec.getLocale()), new CommonResourceLocator("resource-files/common"));
	}

}
