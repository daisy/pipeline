package org.daisy.dotify.impl.input.text;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupInformation;
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
	public boolean supportsSpecification(TaskGroupInformation spec) {
		return listAll().contains(spec);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new TextInputManager(spec.getLocale());
	}

	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
	}

}
