package org.daisy.dotify.impl.input.xml;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
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
	private final XMLL10nResourceLocator locator;

	private final Set<TaskGroupSpecification> supportedSpecifications;
	
	public XMLInputManagerFactory() {
		this.locator = XMLL10nResourceLocator.getInstance();
		DefaultInputUrlResourceLocator p = DefaultInputUrlResourceLocator.getInstance();
		Set<String> supportedFormats = p.listFileFormats();
		supportedFormats.add("xml");
		supportedSpecifications = new HashSet<>();
		for (String format : supportedFormats) {
			for (String locale : locator.listSupportedLocales()) {
				supportedSpecifications.add(new TaskGroupSpecification(format, "obfl", locale));
			}
		}
	}

	@Override
	public boolean supportsSpecification(TaskGroupSpecification spec) {
		return supportedSpecifications.contains(spec);
	}
	
	@Override
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(supportedSpecifications);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
        return new XMLInputManager(locator.getResourceLocator(spec.getLocale()), new CommonResourceLocator("resource-files/common"));
	}

	@Override
	public void setCreatedWithSPI() {
	}
	
}
