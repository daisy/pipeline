package org.daisy.dotify.impl.input.epub;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.impl.input.xml.CommonResourceLocator;
import org.daisy.dotify.impl.input.xml.XMLL10nResourceLocator;

import aQute.bnd.annotation.component.Component;

@Component
public class Epub3InputManagerFactory implements TaskGroupFactory {
	private final XMLL10nResourceLocator locator;
	private final Set<TaskGroupSpecification> supportedSpecifications;

	public Epub3InputManagerFactory() {
		this.locator = XMLL10nResourceLocator.getInstance();
		this.supportedSpecifications = new HashSet<>();
		for (String locale : locator.listSupportedLocales()) {
			supportedSpecifications.add(new TaskGroupSpecification("epub", "obfl", locale));
		}
	}

	@Override
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(supportedSpecifications);
	}

	@Override
	public boolean supportsSpecification(TaskGroupSpecification spec) {
		return supportedSpecifications.contains(spec);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new Epub3InputManager(locator.getResourceLocator(spec.getLocale()), new CommonResourceLocator("resource-files/common"));
	}

	@Override
	public void setCreatedWithSPI() {
	}

}
