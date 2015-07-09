package org.daisy.dotify.impl.input.epub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.impl.input.XsltTask;

public class Epub3InputManager implements TaskGroup {

	public String getName() {
		return this.getClass().getName();
	}

	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		ArrayList<InternalTask> ret = new ArrayList<InternalTask>();
		ret.add(new Epub3Task("Epub to Epub converter"));
		ret.add(new XsltTask("Epub 3 to OBFL converter", null, null));
		return ret;
	}

}
