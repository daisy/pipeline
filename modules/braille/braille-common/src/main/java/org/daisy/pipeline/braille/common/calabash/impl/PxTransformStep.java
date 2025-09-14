package org.daisy.pipeline.braille.common.calabash.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableMap;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XMLCalabashParameterInputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.XMLTransform;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PxTransformStep implements XProcStep {
	
	private final XProcMonitor monitor;
	private final Map<String,String> properties;
	private final TransformProvider<Transform> provider;
	private final XProcRuntime runtime;
	private final XAtomicStep step;
	private ReadablePipe source = null;
	private WritablePipe result = null;
	private final Hashtable<QName,RuntimeValue> params = new Hashtable<>();
	private final Hashtable<QName,RuntimeValue> options = new Hashtable<>();
	
	private static final QName _query = new QName("query");
	
	/**
	 * @param step is optional
	 */
	public PxTransformStep(XProcRuntime runtime,
	                       XAtomicStep step,
	                       XProcMonitor monitor,
	                       Map<String,String> properties,
	                       TransformProvider<Transform> provider) {
		if (runtime == null)
			throw new NullPointerException("runtime may not be null");
		else
			this.runtime = runtime;
		this.step = step;
		this.monitor = monitor;
		this.properties = properties;
		this.provider = provider;
	}

	@Override
	public void setInput(String port, ReadablePipe pipe) {
		source = pipe;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}

	@Override
	public void reset() {
		source.resetReader();
		result.resetWriter();
	}
	
	@Override
	public void setParameter(QName name, RuntimeValue value) {
		params.put(name, value);
	}
	
	@Override
	public void setParameter(String port, QName name, RuntimeValue value) {
		setParameter(name, value);
	}

	@Override
	public void setOption(QName name, RuntimeValue value) {
		options.put(name, value);
	}

	@Override
	public void run() throws SaxonApiException {
		try {
			Query query = query(options.get(_query).getString());
			SingleInSingleOutXMLTransformer xmlTransformer = null;
			try {
				for (Transform t : logSelect(query, provider, logger))
					if (t instanceof XProcStepProvider) {
						// if the transform is a XProcStepProvider, it is assumed to be declared as a p:pipeline
						xmlTransformer = SingleInSingleOutXMLTransformer.from(
							((XProcStepProvider)t).newStep(runtime, step, monitor, properties));
						break; }
					else if (t instanceof XMLTransform) {
						// fromXmlToXml() is assumed to have only a "source", a "parameters" and a "result" port
						// (i.e. the signature of px:transform without the "query" option)
						xmlTransformer = SingleInSingleOutXMLTransformer.from(
							((XMLTransform)t).fromXmlToXml());
						break; }}
			catch (NoSuchElementException e) {}
			if (xmlTransformer == null)
				throw new XProcException(step, "Could not find a Transform for query: " + query);
			xmlTransformer.transform(
				XMLCalabashInputValue.of(source),
				XMLCalabashOutputValue.of(result, runtime),
				XMLCalabashParameterInputValue.of(params)
			).run();
		} catch (Throwable e) {
			if (e instanceof TransformerException && e.getCause() instanceof XProcException)
				// assuming xmlTransformer is cx:eval based
				throw (XProcException)e.getCause();
			else
				throw XProcStep.raiseError(e, step);
		}
	}
	
	@Component(
		name = "px:transform",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}transform" }
	)
	public static class StepProvider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new PxTransformStep(runtime, step, monitor, properties, provider);
		}
		
		@Reference(
			name = "TransformProvider",
			unbind = "-",
			service = TransformProvider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.STATIC
		)
		@SuppressWarnings(
			"unchecked" // safe cast to TransformProvider<Transform>
		)
		public void bindTransformProvider(TransformProvider<?> provider) {
			providers.add((TransformProvider<Transform>)provider);
			logger.debug("Adding Transform provider: {}", provider);
		}
		
		private List<TransformProvider<Transform>> providers = new ArrayList<>();
		private TransformProvider<Transform> provider = dispatch(providers).withContext(logger);
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(PxTransformStep.class);
	
}
