package org.daisy.common.xproc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * The Class XProcInput maps ports and options with a value for the pipeline execution, this object is immutable.
 */
public final class XProcInput {

	/**
	 * The Class Builder builds XProcInput objects to make them immutable.
	 */
	public static final class Builder {

		/** The info. */
		private final XProcPipelineInfo info;

		/** The inputs. */
		private final HashMap<String, List<Supplier<Source>>> inputs = Maps
				.newHashMap();

		/** The parameters. */
		private final Map<String, Map<QName, String>> parameters = Maps
				.newHashMap();

		/** The options. */
		private final Map<QName, Object> options = Maps.newHashMap();

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
			info = null;
		}

		/**
		 * Instantiates a new builder.
		 *
		 * @param info the info
		 */
		public Builder(XProcPipelineInfo info) {
			this.info = info;
		}

		/**
		 * With input.
		 *
		 * @param port the port
		 * @param source the source
		 * @return the builder
		 */
		public Builder withInput(String port, Supplier<Source> source) {
			// TODO check if compatible with info
			if (inputs.containsKey(port)) {
				inputs.get(port).add(source);
			} else {
				List<Supplier<Source>> resources = Lists.newLinkedList();
				resources.add(source);
				inputs.put(port, resources);
			}
			return this;
		}

		/**
		 * With option.
		 *
		 * @param name the name
		 * @param value the value
		 * @return the builder
		 */
		public Builder withOption(QName name, Object value) {
			// TODO check if compatible with info
			options.put(name, value);
			return this;
		}

		/**
		 * With parameter.
		 *
		 * @param port the port
		 * @param name the name
		 * @param value the value
		 * @return the builder
		 */
		public Builder withParameter(String port, QName name, String value) {
			// TODO check if compatible with info
			if (parameters.containsKey(port)) {
				parameters.get(port).put(name, value);
			} else {
				Map<QName, String> params = new HashMap<QName, String>();
				params.put(name, value);
				parameters.put(port, params);
			}
			return this;
		}

		/**
		 * Builds the input object
		 *
		 * @return the x proc input
		 */
		public XProcInput build() {
			return new XProcInput(inputs, parameters, options);
		}
	}

	/** The Constant emptySources. */
	private final static List<Supplier<Source>> emptySources = ImmutableList
			.of();

	/** The Constant emptyParams. */
	private final static Map<QName, String> emptyParams = ImmutableMap.of();

	/** The inputs. */
	private final Map<String, List<Supplier<Source>>> inputs;

	/** The parameters. */
	private final Map<String, Map<QName, String>> parameters;

	/** The options. */
	private final Map<QName, Object> options;

	/**
	 * Instantiates a new x proc input.
	 *
	 * @param inputs the inputs
	 * @param parameters the parameters
	 * @param options the options
	 */
	private XProcInput(Map<String, List<Supplier<Source>>> inputs,
			Map<String, Map<QName, String>> parameters,
			Map<QName, Object> options) {
		ImmutableMap.Builder<String, List<Supplier<Source>>> inputsBuilder = ImmutableMap
				.builder();
		for (String key : inputs.keySet()) {
			inputsBuilder.put(key, ImmutableList.copyOf(inputs.get(key)));
		}
		this.inputs = inputsBuilder.build();
		ImmutableMap.Builder<String, Map<QName, String>> parametersBuilder = ImmutableMap
				.builder();
		for (String key : parameters.keySet()) {
			parametersBuilder
					.put(key, ImmutableMap.copyOf(parameters.get(key)));
		}
		this.parameters = parametersBuilder.build();
		this.options = ImmutableMap.copyOf(Maps.filterValues(options,Predicates.notNull()));
	}

	/**
	 * Gets the inputs.
	 *
	 * @param port the port
	 * @return the inputs
	 */
	public Iterable<Supplier<Source>> getInputs(String port) {
		return inputs.containsKey(port) ? ImmutableList
				.copyOf(inputs.get(port)) : emptySources;
	}

	/**
	 * Gets the parameters.
	 *
	 * @param port the port
	 * @return the parameters
	 */
	public Map<QName, String> getParameters(String port) {
		return parameters.containsKey(port) ? ImmutableMap.copyOf(parameters
				.get(port)) : emptyParams;
	}

	/**
	 * Gets the options.
	 *
	 * @return the options
	 */
	public Map<QName, Object> getOptions() {
		return options;
	}

}
