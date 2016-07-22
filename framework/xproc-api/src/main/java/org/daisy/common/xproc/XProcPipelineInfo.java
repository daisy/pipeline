package org.daisy.common.xproc;

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * The Class XProcPipelineInfo gives access to the collection of ports and options from a pipeline.
 * This class is immutable, in order to give actual values to ports and options use an {@link XProcInput} object.
 */
public final class XProcPipelineInfo {

	/**
	 * The XProcPipelineInfo Builder allows to build the pipline info, this class is for internal use of the xproc implementation bundle.
	 */
	public static final class Builder {

		/** The uri. */
		private URI uri;

		/** The input ports. */
		private final Map<String, XProcPortInfo> inputPorts = Maps.newHashMap();

		/** The parameter ports. */
		private final Map<String, XProcPortInfo> parameterPorts = Maps.newHashMap();

		/** The output ports. */
		private final Map<String, XProcPortInfo> outputPorts = Maps.newHashMap();

		/** The options. */
		private final Map<QName, XProcOptionInfo> options = Maps.newLinkedHashMap();

		/**
		 * Instantiates a new builder.
		 */
		public Builder() {
		}

		/**
		 * With uri.
		 *
		 * @param uri the uri
		 * @return the builder
		 */
		public Builder withURI(URI uri) {
			this.uri = uri;
			return this;
		}

		/**
		 * With port.
		 *
		 * @param port the port
		 * @return the builder
		 */
		public Builder withPort(XProcPortInfo port) {
			switch (port.getKind()) {
			case INPUT:
				inputPorts.put(port.getName(), port);
				break;
			case OUTPUT:
				outputPorts.put(port.getName(), port);
				break;
			case PARAMETER:
				parameterPorts.put(port.getName(), port);
				break;
			}
			return this;
		}

		/**
		 * With option.
		 *
		 * @param option the option
		 * @return the builder
		 */
		public Builder withOption(XProcOptionInfo option) {
			options.put(option.getName(), option);
			return this;
		}

		/**
		 * Builds the XProcPipelineInfo object
		 *
		 * @return the x proc pipeline info
		 */
		public final XProcPipelineInfo build() {
			return new XProcPipelineInfo(uri, inputPorts, parameterPorts,
					outputPorts, options);
		}
	}

	/** The uri. */
	private final URI uri;

	/** The input ports. */
	private final Map<String, XProcPortInfo> inputPorts;

	/** The parameter ports. */
	private final Map<String, XProcPortInfo> parameterPorts;

	/** The output ports. */
	private final Map<String, XProcPortInfo> outputPorts;

	/** The options. */
	private final Map<QName, XProcOptionInfo> options;

	/**
	 * Instantiates a new x proc pipeline info.
	 *
	 * @param uri the uri
	 * @param inputPorts the input ports
	 * @param parameterPorts the parameter ports
	 * @param outputPorts the output ports
	 * @param options the options
	 */
	private XProcPipelineInfo(URI uri, Map<String, XProcPortInfo> inputPorts,
			Map<String, XProcPortInfo> parameterPorts,
			Map<String, XProcPortInfo> outputPorts,
			Map<QName, XProcOptionInfo> options) {
		this.uri = uri;
		this.inputPorts = ImmutableMap.copyOf(inputPorts);
		this.parameterPorts = ImmutableMap.copyOf(parameterPorts);
		this.outputPorts = ImmutableMap.copyOf(outputPorts);
		this.options = ImmutableMap.copyOf(options);
	}

	/**
	 * Gets the uRI.
	 *
	 * @return the uRI
	 */
	public URI getURI() {
		return uri;
	}

	/**
	 * Gets the input ports.
	 *
	 * @return the input ports
	 */
	public Iterable<XProcPortInfo> getInputPorts() {
		return inputPorts.values();
	}

	/**
	 * Gets the input port.
	 *
	 * @param name the name
	 * @return the input port
	 */
	public XProcPortInfo getInputPort(String name) {
		return inputPorts.get(name);
	}

	/**
	 * Gets the options.
	 *
	 * @return the options
	 */
	public Iterable<XProcOptionInfo> getOptions() {
		return options.values();
	}

	/**
	 * Gets the option.
	 *
	 * @param name the name
	 * @return the option
	 */
	public XProcOptionInfo getOption(QName name) {
		return options.get(name);
	}

	/**
	 * Gets the output ports.
	 *
	 * @return the output ports
	 */
	public Iterable<XProcPortInfo> getOutputPorts() {
		return outputPorts.values();
	}

	/**
	 * Gets the output port.
	 *
	 * @param name the name
	 * @return the output port
	 */
	public XProcPortInfo getOutputPort(String name) {
		return outputPorts.get(name);
	}

	/**
	 * Gets the parameter ports.
	 *
	 * @return the parameter ports
	 */
	public Iterable<String> getParameterPorts() {
		return parameterPorts.keySet();
	}
}
