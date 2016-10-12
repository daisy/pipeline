package org.daisy.dotify.impl.system.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.tasks.CompiledTaskSystem;
import org.daisy.dotify.api.tasks.DefaultCompiledTaskSystem;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskGroupSpecification.Type;
import org.daisy.dotify.api.tasks.TaskOption;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.dotify.impl.input.Keys;


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
	private static final Logger logger = Logger.getLogger(DotifyTaskSystem.class.getCanonicalName());
	private final String outputFormat;
	private final String context;
	private final String name;
	private final TaskGroupFactoryMakerService imf;
	
	public DotifyTaskSystem(String name, String outputFormat, String context,
			TaskGroupFactoryMakerService imf, PagedMediaWriterFactoryMakerService pmw, FormatterEngineFactoryService fe) {
		this.context = context;
		this.outputFormat = outputFormat;
		this.name = name;
		this.imf = imf;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public CompiledTaskSystem compile(Map<String, Object> pa) throws TaskSystemException {
		RunParameters p = RunParameters.fromMap(pa);
		HashMap<String, Object> h = new HashMap<>();
		for (Object key : p.getKeys()) {
			if (p.getProperty(key)!=null) {
				h.put(key.toString(), p.getProperty(key));
			}
		}
		
		DefaultCompiledTaskSystem setup = new DefaultCompiledTaskSystem(name, getOptions());
		String inputFormat = h.get(Keys.INPUT_FORMAT).toString();

		logger.info("Finding path...");
		for (TaskGroupSpecification spec : getPath(imf, new TaskGroupSpecification(inputFormat, outputFormat, context), pa)) {
			TaskGroup g = imf.newTaskGroup(spec);
			if (spec.getType()==Type.ENHANCE) {
				// For enhance, only include the options required to enable the task group. Once enabled,
				// additional options may be presented
				for (TaskOption o : spec.getRequiredOptions()) {
					setup.addOption(o);
				}
			}
			if (spec.getType()==Type.CONVERT || matchesRequiredOptions(spec, pa, false)) {
				//TODO: these options should be on the group level instead of on the system level
				List<TaskOption> opts = g.getOptions();
				if (opts!=null) {
					for (TaskOption o : opts) {
						setup.addOption(o);
					}
				}
				setup.addAll(g.compile(h));				
			}
		}
		return setup;
	}

	@Override
	public List<TaskOption> getOptions() {
		return Collections.emptyList();
	}
	
	/**
	 * Finds a path for the given specifications
	 * @param input the input format
	 * @param output the output format
	 * @param locale the target locale
	 * @param parameters the parameters
	 * @return returns a list of task groups
	 */
	static List<TaskGroupSpecification> getPath(TaskGroupFactoryMakerService imf, TaskGroupSpecification def, Map<String, Object> parameters) {
		Set<TaskGroupSpecification> specs = imf.listSupportedSpecifications();
		Map<String, List<TaskGroupSpecification>> byInput = byInput(specs);

		return getPathSpecifications(def.getInputFormat(), def.getOutputFormat(), def.getLocale(), byInput);
	}
	
	/**
	 * Gets the shortest path that matches the specification (breadth-first search)
	 * @param input the input format
	 * @param output the output format
	 * @param locale the locale
	 * @param inputs a list of specifications ordered by input format
	 * @return returns the shortest path
	 */
	static List<TaskGroupSpecification> getPathSpecifications(String input, String output, String locale, Map<String, List<TaskGroupSpecification>> inputs) {
		// queue root
		List<QueueInfo> queue = new ArrayList<>();
		queue.add(new QueueInfo(locale, new HashMap<>(inputs).remove(input), new ArrayList<TaskGroupSpecification>()));
		
		while (!queue.isEmpty()) {
			QueueInfo current = queue.remove(0);
			for (TaskGroupSpecification candidate : current.getCandidates().getConvert()) {
				logger.info("Evaluating " + candidate.getInputFormat() + " -> " + candidate.getOutputFormat());
				if (candidate.getOutputFormat().equals(output)) {
					List<TaskGroupSpecification> ret = new ArrayList<>(current.getSpecs());
					ret.addAll(current.getCandidates().getEnhance());
					ret.add(candidate);
					return ret;
				} else {
					// add for later evaluation
					QueueInfo info = new QueueInfo(locale, new HashMap<>(inputs).remove(candidate.getOutputFormat()), current.getSpecs());
					info.getSpecs().addAll(current.getCandidates().getEnhance());
					info.getSpecs().add(candidate);
					queue.add(info);
				}
			}
		}
		return Collections.emptyList();
	}
	
	static boolean matchesRequiredOptions(TaskGroupSpecification candidate, Map<String, Object> parameters, boolean emptyReturn) {
		if (candidate.getRequiredOptions().isEmpty()) {
			return emptyReturn;
		}
		for (TaskOption option : candidate.getRequiredOptions()) {
			String key = option.getKey();
			if (!parameters.containsKey(key)) {
				return false;
			} else {
				Object value = parameters.get(key);
				if (!option.acceptsValue(value.toString())) {
					return false;
				}
			}
		}
		return true;
	}
	
	static Map<String, List<TaskGroupSpecification>> byInput(Set<TaskGroupSpecification> specs) {
		Map<String, List<TaskGroupSpecification>> ret = new HashMap<>();
		for (TaskGroupSpecification spec : specs) {
			List<TaskGroupSpecification> group = ret.get(spec.getInputFormat());
			if (group==null) {
				group = new ArrayList<>();
				ret.put(spec.getInputFormat(), group);
			}
			group.add(spec);
		}
		return ret;
	}

}