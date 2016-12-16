package org.daisy.dotify.impl.input.text;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroupInformation;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;

import aQute.bnd.annotation.component.Component;

@Component
public class TextInputManagerFactory implements TaskGroupFactory {
	private final Set<TaskGroupSpecification> specs;
	private final Set<TaskGroupInformation> information;

	public TextInputManagerFactory() {
		this.specs = new HashSet<>();
		String text = "text";
		String txt = "txt";
		String obfl = "obfl";
		String sv = "sv-SE";
		String en = "en-US";
		this.specs.add(new TaskGroupSpecification(text, obfl, sv));
		this.specs.add(new TaskGroupSpecification(text, obfl, en));
		this.specs.add(new TaskGroupSpecification(txt, obfl, sv));
		this.specs.add(new TaskGroupSpecification(txt, obfl, en));
		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder(text, obfl).build());
		tmp.add(TaskGroupInformation.newConvertBuilder(txt, obfl).build());
		information = Collections.unmodifiableSet(tmp);
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
	public boolean supportsSpecification(TaskGroupInformation spec) {
		return listAll().contains(spec);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new TextInputManager(spec.getLocale());
	}

	@Override
	@Deprecated
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(specs);
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
	}

}
