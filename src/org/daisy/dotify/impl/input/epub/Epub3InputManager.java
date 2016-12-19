package org.daisy.dotify.impl.input.epub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskSystemException;

public class Epub3InputManager implements TaskGroup {

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<>();
		ret.add(new Epub3Task("Epub to HTML converter", (String)parameters.get("opf-path")));
		return ret;
	}

}