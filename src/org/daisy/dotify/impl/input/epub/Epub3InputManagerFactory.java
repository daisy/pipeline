package org.daisy.dotify.impl.input.epub;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupInformation;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.impl.input.xml.XMLL10nResourceLocator;

import aQute.bnd.annotation.component.Component;

@Component
public class Epub3InputManagerFactory implements TaskGroupFactory {
	private final XMLL10nResourceLocator locator;
	private final Set<TaskGroupSpecification> supportedSpecifications;
	private final Set<TaskGroupInformation> information;

	public Epub3InputManagerFactory() {
		this.locator = XMLL10nResourceLocator.getInstance();
		this.supportedSpecifications = new HashSet<>();
		for (String locale : locator.listSupportedLocales()) {
			supportedSpecifications.add(new TaskGroupSpecification("epub", "html", locale));
		}
		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder("epub", "html").build());
		information = Collections.unmodifiableSet(tmp);
	}

	@Override
	@Deprecated
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(supportedSpecifications);
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
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new Epub3InputManager();
	}
	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
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
	public void setCreatedWithSPI() {
		//TODO: remove after move to java 8
	}

}