package org.daisy.dotify.impl.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskOption;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;

public class LayoutEngine implements TaskGroup {

	private final TaskGroupSpecification spec;
	private final PagedMediaWriterFactoryMakerService pmw;
	private final FormatterEngineFactoryService fe;

	public LayoutEngine(TaskGroupSpecification spec, PagedMediaWriterFactoryMakerService pmw, FormatterEngineFactoryService fe) {
		this.spec = spec;
		this.pmw = pmw;
		this.fe = fe;
	}

	@Override
	public String getName() {
		return "Layout Engine";
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		
		ArrayList<InternalTask> ret = new ArrayList<>();
		Properties p2 = new Properties();
		p2.putAll(parameters);
		// Layout FLOW as PEF

		// Customize which parameters are sent to the PEFMediaWriter, as it
		// outputs all parameters for future reference
		// System file paths should be concealed for security reasons
		p2.remove(Keys.INPUT);
		p2.remove(Keys.INPUT_URI);
		p2.remove("output");
		p2.remove("obfl-output-location");
		p2.remove(Keys.TEMP_FILES_DIRECTORY);
		ret.add(new LayoutEngineTask(p2, spec, pmw, fe));
		return ret;
	}

	@Override
	public List<TaskOption> getOptions() {
		return null;
	}

}