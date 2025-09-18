package org.daisy.pipeline.script;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobResultSet;

/**
 * Script description.
 *
 * Contains scripts metadata and descriptions of inputs, outputs and options.
 */
public abstract class Script {

	/**
	 * Run the script with an input
	 *
	 * @param properties Properties that may influence the behavior of the script
	 * @param resultBuilder for storing the results
	 * @param resultDir must exist
	 */
	public abstract Status run(ScriptInput input,
	                           Map<String,String> properties,
	                           MessageAppender messages,
	                           JobResultSet.Builder resultBuilder,
	                           File resultDir) throws IOException;

	/**
	 * Builder for {@link Script} objects.
	 */
	public static abstract class Builder {

		protected String id;
		protected String version;
		protected String shortName;
		protected String description;
		protected String homepage;
		protected List<String> inputFilesets = Lists.newLinkedList();
		protected List<String> outputFilesets = Lists.newLinkedList();
		protected final Map<String,ScriptPort> inputPorts = new HashMap<>();
		protected final Map<String,ScriptPort> outputPorts = new HashMap<>();
		protected final Map<String,ScriptOption> options = new LinkedHashMap<>();

		public Builder(ScriptService<?> descriptor) {
			this(descriptor.getId(), descriptor.getVersion());
		}

		public Builder(String id, String version) {
			this.id = id;
			this.version = version;
		}

		public Builder withInputFileset(String fileset){
			if (fileset!=null && !fileset.isEmpty()){
				this.inputFilesets.add(fileset);
			}
			return this;
		}

		public Builder withOutputFileset(String fileset){
			if (fileset!=null && !fileset.isEmpty()){
				this.outputFilesets.add(fileset);
			}
			return this;
		}

		/**
		 * With nice name.
		 */
		public Builder withShortName(String shortName){
			if(shortName!=null) {
				this.shortName=shortName;
			}
			return this;
		}

		/**
		 * With description.
		 */
		public Builder withDescription(String description){
			if (description!=null) {
				this.description=description;
			}
			return this;
		}

		/**
		 * With homepage.
		 */
		public Builder withHomepage(String homepage) {
			if (homepage != null) {
				this.homepage = homepage;
			}
			return this;
		}

		/**
		 * With input port
		 */
		public Builder withInputPort(String name, ScriptPort port) {
			inputPorts.put(name, port);
			return this;
		}

		/**
		 * With output port
		 */
		public Builder withOutputPort(String name, ScriptPort port) {
			outputPorts.put(name, port);
			return this;
		}

		/**
		 * With option.
		 */
		public Builder withOption(String name, ScriptOption option) {
			options.put(name, option);
			return this;
		}

		/**
		 * Builds the {@link Script} instance.
		 *
		 * @return the {@link Script}
		 */
		public abstract Script build();

	}

	private final String id;
	private final String version;
	private final String name;
	private final String description;
	private final String homepage;
	private final Map<String,ScriptPort> inputPorts;
	private final Map<String,ScriptPort> outputPorts;
	private final Map<String,ScriptOption> options;
	private final List<String> inputFilesets;
	private final List<String> outputFilesets;

	protected Script(String id, String version, String name, String description, String homepage,
	                 Map<String,ScriptPort> inputPorts, Map<String,ScriptPort> outputPorts, Map<String,ScriptOption> options,
	                 List<String> inputFilesets, List<String> outputFilesets) {
		this.id = id;
		this.version = version;
		this.name = name;
		this.description = description;
		this.homepage = homepage;
		this.inputPorts = ImmutableMap.copyOf(inputPorts);
		this.outputPorts = ImmutableMap.copyOf(outputPorts);
		this.options = ImmutableMap.copyOf(options);
		this.inputFilesets = ImmutableList.copyOf(inputFilesets);
		this.outputFilesets = ImmutableList.copyOf(outputFilesets);
	}

	/**
	 * The ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * The version.
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * The name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * The description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * The homepage.
	 */
	public String getHomepage() {
		return homepage;
	}

	/**
	 * Get all the input ports.
	 */
	public Iterable<ScriptPort> getInputPorts() {
		return inputPorts.values();
	}

	/**
	 * Get the input port for the given name.
	 */
	public ScriptPort getInputPort(String name) {
		return inputPorts.get(name);
	}

	/**
	 * Get all the input ports.
	 */
	public Iterable<ScriptPort> getOutputPorts() {
		return outputPorts.values();
	}

	/**
	 * Get the output port for the given name.
	 */
	public ScriptPort getOutputPort(String name) {
		return outputPorts.get(name);
	}

	/**
	 * Get all the options.
	 */
	public Iterable<ScriptOption> getOptions() {
		return options.values();
	}

	/**
	 * Get the option for the given name.
	 */
	public ScriptOption getOption(String name) {
		return options.get(name);
	}

	/**
	 * The input media types.
	 */
	public Iterable<String> getInputFilesets() {
		return inputFilesets;
	}

	/**
	 * The output media types.
	 */
	public Iterable<String> getOutputFilesets() {
		return outputFilesets;
	}

	@Override
	public String toString() {
		return String.format("Script[name=%s]",this.getName());
	}
}
