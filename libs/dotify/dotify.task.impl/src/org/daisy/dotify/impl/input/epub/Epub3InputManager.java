package org.daisy.dotify.impl.input.epub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskOption;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.common.io.ResourceLocator;
import org.daisy.dotify.impl.input.xml.XMLInputManager;

public class Epub3InputManager implements TaskGroup {
	private final XMLInputManager xml;
	
	public Epub3InputManager(ResourceLocator localLocator, ResourceLocator commonLocator) {
		this.xml = new XMLInputManager(localLocator, commonLocator);
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<>();
		ret.add(new Epub3Task("Epub to HTML converter", (String)parameters.get("opf-path")));
		ret.addAll(xml.compile(parameters));
		return ret;
	}

	@Override
	public List<TaskOption> getOptions() {
		return null;
	}

}