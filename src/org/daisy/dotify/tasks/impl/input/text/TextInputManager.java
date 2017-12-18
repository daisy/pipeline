package org.daisy.dotify.tasks.impl.input.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskSystemException;

class TextInputManager implements TaskGroup {
	private final String rootLang;

	TextInputManager(String rootLang) {
		this.rootLang = rootLang;
	}

	@Override
	public String getName() {
		return "TextInputManager";
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<>();
		ret.add(new Text2ObflTask("Text to OBFL converter", rootLang, parameters));
		return ret;
	}

}