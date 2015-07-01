package org.daisy.dotify.impl.input.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.cr.InputManager;
import org.daisy.dotify.api.cr.InternalTask;
import org.daisy.dotify.api.cr.TaskSystemException;

class TextInputManager implements InputManager {
	private final String rootLang;

	TextInputManager(String rootLang) {
		this.rootLang = rootLang;
	}

	public String getName() {
		return "TextInputManager";
	}

	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<InternalTask>();
		ret.add(new Text2ObflTask("Text to OBFL converter", rootLang, parameters));
		return ret;
	}

}