package org.daisy.dotify.impl.system.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.dotify.impl.input.DuplicatorTask;
import org.daisy.dotify.impl.input.Keys;
import org.daisy.dotify.impl.input.LayoutEngine;


/**
 * <p>Transforms XML into braille in PEF 2008-1 format.</p>
 * <p>Transforms documents into text format.</p>
 * 
 * <p>This TaskSystem consists of the following steps:</p>
 * <ol>
	 * <li>Input Manager. Validates and converts input to OBFL.</li>
	 * <li>OBFL to PEF converter.
	 * 		Translates all characters into braille, and puts the text flow onto pages.</li>
 * </ol>
 * <p>The result should be validated against the PEF Relax NG schema using int_daisy_validator.</p>
 * @author Joel Håkansson
 */
public class DotifyTaskSystem implements TaskSystem {
	/**
	 * Specifies a location where the intermediary obfl output should be stored
	 */
	final static String OBFL_OUTPUT_LOCATION = "obfl-output-location";
	private final String outputFormat;
	private final String context;
	private final String name;
	private final TaskGroupFactoryMakerService imf;
	private final PagedMediaWriterFactoryMakerService pmw;
	private final FormatterEngineFactoryService fe;
	
	public DotifyTaskSystem(String name, String outputFormat, String context,
			TaskGroupFactoryMakerService imf, PagedMediaWriterFactoryMakerService pmw, FormatterEngineFactoryService fe) {
		this.context = context;
		this.outputFormat = outputFormat;
		this.name = name;
		this.imf = imf;
		this.pmw = pmw;
		this.fe = fe;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> pa) throws TaskSystemException {
		RunParameters p = RunParameters.fromMap(pa);
		HashMap<String, Object> h = new HashMap<>();
		for (Object key : p.getKeys()) {
			h.put(key.toString(), p.getProperty(key));
		}
		
		ArrayList<InternalTask> setup = new ArrayList<>();

		TaskGroup idts = imf.newTaskGroup(new TaskGroupSpecification(h.get(Keys.INPUT_FORMAT).toString(), "obfl", context));
		setup.addAll(idts.compile(h));

		String keep = (String)h.get(OBFL_OUTPUT_LOCATION);
		if (keep!=null && !"".equals(keep)) {
			setup.add(new DuplicatorTask("OBFL archiver", new File(keep)));
		}

		if (!Keys.OBFL_FORMAT.equals(outputFormat)) {
			setup.addAll(new LayoutEngine(new TaskGroupSpecification("obfl", outputFormat, context), pmw, fe).compile(h));
		}
		return setup;
	}

}