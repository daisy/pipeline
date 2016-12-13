package org.daisy.dotify.impl.input.xml;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroupInformation;
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
	private final Set<TaskGroupInformation> supportedTaskGroupInformations;
	private final Set<String> supportedLocales;
	
	public XMLInputManagerFactory() {
		this.locator = XMLL10nResourceLocator.getInstance();
		DefaultInputUrlResourceLocator p = DefaultInputUrlResourceLocator.getInstance();
		Set<String> supportedFormats = p.listFileFormats();
		supportedFormats.add("xml");
		supportedSpecifications = new HashSet<>();
		this.supportedLocales = locator.listSupportedLocales();
		Set<TaskGroupInformation> tmp = new HashSet<>();
		for (String format : supportedFormats) {
			for (String locale : supportedLocales) {
				if ("obfl".equals(format)) {
					tmp.add(TaskGroupInformation.newEnhanceBuilder(format).locale(locale).build());
				} else {
					tmp.add(TaskGroupInformation.newConvertBuilder(format, "obfl").locale(locale).build());
				}
				supportedSpecifications.add(new TaskGroupSpecification(format, "obfl", locale));
			}
		}
		supportedTaskGroupInformations = Collections.unmodifiableSet(tmp);
	}

	@Override
	public boolean supportsSpecification(TaskGroupSpecification spec) {
		//TODO: move this to default implementation after move to java 8
		for (TaskGroupInformation i : listAll()) {
			if (spec.matches(i)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Deprecated
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(supportedSpecifications);
	}
	
	@Override
	public Set<TaskGroupInformation> listAll() {
		return supportedTaskGroupInformations;
	}

	@Override
	public Set<TaskGroupInformation> list(String locale) {
		//TODO: move this to default implementation after move to java 8 (and use streams)
		Objects.requireNonNull(locale);
		Set<TaskGroupInformation> ret = new HashSet<>();
		for (TaskGroupInformation info : listAll()) {
			if (info.matchesLocale(locale)) {
				ret.add(info.newCopyBuilder().locale(locale).build());
			}
		}
		return ret;
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
        return new XMLInputManager(locator.getResourceLocator(spec.getLocale()), new CommonResourceLocator("resource-files/common"));
	}

	@Override
	public void setCreatedWithSPI() {
		//TODO: remove after move to java 8
	}
	
}
