package org.daisy.dotify.tasks.impl.input.text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskSystemException;

class TextInputManager implements TaskGroup {
	enum Type {
		OBFL,
		HTML;
	}
	private final String rootLang;
	private final Type type;

	TextInputManager(String rootLang, Type type) {
		this.rootLang = rootLang;
		this.type = type;
	}

	@Override
	public String getName() {
		return "TextInputManager";
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		List<InternalTask> ret = new ArrayList<>();
		switch (type) {
			case OBFL:
				ret.add(new Text2ObflTask("Text to OBFL converter", rootLang, parameters));
				break;
			case HTML:
				ret.add(new Text2HtmlTask("Text to HTML converter", rootLang, parameters));
				break;
			default:
				throw new RuntimeException("Coding error");
		}
		return ret;
	}

}