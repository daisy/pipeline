package org.daisy.dotify.formatter.impl.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.streamline.api.tasks.InternalTask;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.daisy.streamline.api.tasks.TaskSystemException;
import org.daisy.streamline.api.validity.ValidatorFactoryMakerService;

/**
 * Provides a task group for running the Dotify formatter.
 * 
 * @author Joel Håkansson
 */
public class LayoutEngine implements TaskGroup {

	private final TaskGroupSpecification spec;
	private final PagedMediaWriterFactoryMakerService pmw;
	private final FormatterEngineFactoryService fe;
	private final ValidatorFactoryMakerService vf;

	/**
	 * Creates a new layout engine with the specified parameters.
	 * @param spec the task group specification
	 * @param pmw a paged media writer factory maker service
	 * @param fe a formatter engine factory service
	 * @param vf a validator factory service
	 */
	public LayoutEngine(TaskGroupSpecification spec, PagedMediaWriterFactoryMakerService pmw, FormatterEngineFactoryService fe, ValidatorFactoryMakerService vf) {
		this.spec = spec;
		this.pmw = pmw;
		this.fe = fe;
		this.vf = vf;
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
		ret.add(new LayoutEngineTask(p2, spec, pmw, fe, vf));
		return ret;
	}

}