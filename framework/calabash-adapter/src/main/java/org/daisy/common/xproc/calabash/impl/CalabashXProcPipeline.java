package org.daisy.common.xproc.calabash.impl;

import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.xproc.XProcError;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcResult;
import org.daisy.common.xproc.calabash.CalabashXProcError;
import org.daisy.common.xproc.calabash.XProcConfigurationFactory;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcMessageListener;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.DeclareStep;
import com.xmlcalabash.model.Input;
import com.xmlcalabash.model.Option;
import com.xmlcalabash.model.Output;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.model.SequenceType;
import com.xmlcalabash.runtime.XPipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calabash piplines allow to define and run xproc pipelines using calabash. The
 * pipelines supplied by this class are reusable.
 */
public class CalabashXProcPipeline implements XProcPipeline {

	private static final Logger logger = LoggerFactory.getLogger(CalabashXProcPipeline.class);

	/** The uri. */
	private final URI uri;

	/** The config factory. */
	private final XProcConfigurationFactory configFactory;

	/** The uri resolver. */
	private final URIResolver uriResolver;

	/** The entity resolver. */
	private final EntityResolver entityResolver;

	private final boolean AUTO_NAME_STEPS = Boolean.parseBoolean(
		org.daisy.common.properties.Properties.getProperty(
			"org.daisy.pipeline.calabash.autonamesteps", "false"));

	/**
	 * The pipeline supplier returns a ready-to-go pipeline instance based on
	 * the XProcPipeline object
	 */
	private final Supplier<PipelineInstance> pipelineSupplier = new Supplier<PipelineInstance>() {
		/**
		 * configures the clone of the pipeline instance setting all the objects
		 * present in the XProcPipeline object
		 */
		@Override
		public PipelineInstance get() {
			XProcConfiguration config = configFactory.newConfiguration();
			XProcRuntime runtime = new XProcRuntime(config);
			runtime.setMessageListener(new slf4jXProcMessageListener());
			if (uriResolver != null) {
				runtime.setURIResolver(uriResolver);
			}
			if (entityResolver != null) {
				runtime.setEntityResolver(entityResolver);
			}

			XProcMessageListenerAggregator listeners = new XProcMessageListenerAggregator();
			listeners.add(new slf4jXProcMessageListener());
			// TODO: get rid of asAccessor as from now on it will be available
			// from the job monitor
			// listeners.addAsAccessor(new MessageListenerWrapper(
			// messageListenerFactory.createMessageListener()));
			runtime.setMessageListener(listeners);
			XPipeline xpipeline = null;

			try {
				xpipeline = runtime.load(new com.xmlcalabash.util.Input(uri.toString()));
                                
			} catch (SaxonApiException e) {
				throw new RuntimeException(e.getMessage(), e);
			}
			return new PipelineInstance(xpipeline, config,runtime);
		}
	};

	/** Suplies the current Pipeline info for this pipeline object */
	private final Supplier<XProcPipelineInfo> info = Suppliers
			.memoize(new Supplier<XProcPipelineInfo>() {

				@Override
				public XProcPipelineInfo get() {
					XProcPipelineInfo.Builder builder = new XProcPipelineInfo.Builder();
					builder.withURI(uri);
                                        PipelineInstance instance=pipelineSupplier.get();
					DeclareStep declaration = instance.xpipe.getDeclareStep();
					// input and parameter ports
					for (Input input : declaration.inputs()) {
						if (!input.getParameterInput()) {
							builder.withPort(XProcPortInfo.newInputPort(
									input.getPort(), input.getSequence(),
									input.getPrimary()));
						} else {
							builder.withPort(XProcPortInfo.newParameterPort(
									input.getPort(), input.getPrimary()));
						}
					}
					// output ports
					for (Output output : declaration.outputs()) {
						builder.withPort(XProcPortInfo.newOutputPort(
								output.getPort(), output.getSequence(),
								output.getPrimary()));
					}
					// options
					for (Option option : declaration.options()) {
						SequenceType sequenceType = option.getSequenceType();
						if (sequenceType == null) sequenceType = SequenceType.XS_STRING;
						builder.withOption(new CalabashXProcOptionInfo(option, sequenceType));
					}
                                        instance.runtime.close();

					return builder.build();
				}
			});



	/**
	 * Instantiates a new calabash x proc pipeline.
	 *
	 * @param uri
	 *            the uri to load the xpl file
	 * @param configFactory
	 *            the configuration factory
	 * @param uriResolver
	 *            the uri resolver
	 * @param entityResolver
	 *            the entity resolver
	 * @param messageListenerFactory
	 *            the message listener factory used to process pipeline
	 *            execution related messages
	 */
	public CalabashXProcPipeline(URI uri,
			XProcConfigurationFactory configFactory, URIResolver uriResolver,
			EntityResolver entityResolver) {
		this.uri = uri;
		this.configFactory = configFactory;
		this.uriResolver = uriResolver;
		this.entityResolver = entityResolver;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.daisy.common.xproc.XProcPipeline#getInfo()
	 */
	@Override
	public XProcPipelineInfo getInfo() {
		return info.get();
	}

	@Override
	public XProcResult run(XProcInput data) throws XProcErrorException {
		return run(data, null,null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.daisy.common.xproc.XProcPipeline#run(org.daisy.common.xproc.XProcInput
	 * )
	 */
	@Override
	public XProcResult run(XProcInput data, XProcMonitor monitor, Properties props) throws XProcErrorException {
		if (monitor != null) {
			MessageListenerImpl messageListener = new MessageListenerImpl(monitor.getMessageAppender(), AUTO_NAME_STEPS);
			try {
				return run(data, messageListener);
			} finally {
				messageListener.clean();
			}
		} else {
			return run(data, null);
		}
	}

	private XProcResult run(XProcInput data, XProcMessageListener messageListener) throws XProcErrorException {
		PipelineInstance pipeline = pipelineSupplier.get();
		if (messageListener != null)
			((XProcMessageListenerAggregator)pipeline.xpipe.getStep().getXProc().getMessageListener())
				.add(messageListener);
		// bind inputs
		for (String name : pipeline.xpipe.getInputs()) {
			boolean cleared = false;
			for (Supplier<Source> sourceProvider : data.getInputs(name)) {
				Source source = sourceProvider.get();
				// TODO hack to set the entity resolver
				if (source instanceof SAXSource) {
					XMLReader reader = ((SAXSource) source).getXMLReader();
					if (reader == null) {
						try {
							reader = XMLReaderFactory.createXMLReader();
							((SAXSource) source).setXMLReader(reader);
							reader.setEntityResolver(entityResolver);
						} catch (SAXException se) {
							// nop?
						}
					}
				}
				// remove possible default connection
				if (!cleared) pipeline.xpipe.clearInputs(name);
				pipeline.xpipe.writeTo(name,
						asXdmNode(pipeline.config.getProcessor(), source));
			}
		}
		// bind options
		for (QName optname : data.getOptions().keySet()) {
			XProcOptionInfo optionInfo = info.get().getOption(optname);
			if (optionInfo != null) {
				RuntimeValue value; {
					if (pipeline.runtime.getAllowGeneralExpressions()) {
						XdmValue xdmValue = ((CalabashXProcOptionInfo)optionInfo).sequenceType.cast(
							SaxonHelper.xdmValueFromObject(data.getOptions().get(optname)),
							// note that we're passing null as the "namespaceResolver" argument which could
							// lead to a NullPointerException if we're trying to cast a xs:string to a xs:QName
							null);
						// because value might be accessed as string or untyped atomic, e.g. by p:in-scope-names
						String stringValue = StreamSupport.stream(xdmValue.spliterator(), false)
						                                  .map(XdmItem::getStringValue)
						                                  .collect(Collectors.joining(""));
						value = new RuntimeValue(stringValue, xdmValue, null, null);
					} else {
						Object val = data.getOptions().get(optname);
						try {
							value = new RuntimeValue((String)val);
						} catch (ClassCastException e) {
							throw new RuntimeException("Expected string value for option " + optname + " but got: " + val.getClass());
						}
					}
				}
				pipeline.xpipe.passOption(new net.sf.saxon.s9api.QName(optname), value);
			} // else ignore the option
		}

		// bind parameters
		for (String port : info.get().getParameterPorts()) {
			for (QName name : data.getParameters(port).keySet()) {
				RuntimeValue value = new RuntimeValue(data.getParameters(port)
						.get(name), null, null);
				pipeline.xpipe.setParameter(port, new net.sf.saxon.s9api.QName(
						name), value);
			}
		}

		// run
		try {
			pipeline.xpipe.run();
			// make sure all lazy code is executed by accessing all output ports
			for (String port : pipeline.xpipe.getOutputs()) {
				pipeline.xpipe.readFrom(port).moreDocuments();
			}
		} catch (XProcException e) {

			// if multiple errors have been reported, log all except the last one (the last one
			// should normally contain the same info as the caught XProcException)
			List<XdmNode> errors = pipeline.xpipe.errors();
			for (int i = 0; i < errors.size() - 1; i++)
				try {
					XProcError err = XProcError.parse(
						new SaxonInputValue(errors.get(i).getUnderlyingNode()).asXMLStreamReader());
					logger.error(err.toString()); }
				catch (Throwable e1) {}

			XProcError err = CalabashXProcError.from(e);
			throw new XProcErrorException(err, e);
		} catch (Exception e) {
                        throw new RuntimeException(e);

		} catch (OutOfMemoryError e) {//this one needs it's own catch!
                        throw new RuntimeException(e);
		}finally{
                        pipeline.runtime.close();
                }
		return CalabashXProcResult.newInstance( pipeline.xpipe ,
				pipeline.config);
	}

	/**
	 * As xdm node.
	 *
	 * @param processor
	 *            the processor
	 * @param source
	 *            the source
	 * @return the xdm node
	 */
	private static XdmNode asXdmNode(Processor processor, Source source) {
		DocumentBuilder builder = processor.newDocumentBuilder();
		builder.setDTDValidation(false);
		builder.setLineNumbering(true);
		try {
			return builder.build(source);
		} catch (SaxonApiException sae) {
			// TODO better exception handling
			throw new RuntimeException(sae.getMessage(), sae);
		}
	}

	private static class CalabashXProcOptionInfo extends XProcOptionInfo {

		public final SequenceType sequenceType;

		public CalabashXProcOptionInfo(Option option, SequenceType sequenceType) {
			super(new QName(option.getName().getNamespaceURI(),
			                option.getName().getLocalName(),
			                option.getName().getPrefix()),
			      sequenceType.toString(),
			      option.getRequired(),
			      option.getSelect());
			this.sequenceType = sequenceType;
		}
	}

	/**
	 * The Class PipelineInstance is just a holder for various objects to
	 * connect with the suppliers .
	 */
	private static final class PipelineInstance {

		/** The xpipe. */
		private final XPipeline xpipe;

		/** The config. */
		private final XProcConfiguration config;

		/** The config. */
		private final XProcRuntime runtime;

		/**
		 * Instantiates a new pipeline instance.
		 *
		 * @param xpipe
		 *            the xpipe
		 * @param config
		 *            the config
		 */
		private PipelineInstance(XPipeline xpipe, XProcConfiguration config,XProcRuntime runtime) {
			this.xpipe = xpipe;
			this.config = config;
			this.runtime= runtime;

		}
	}

}
