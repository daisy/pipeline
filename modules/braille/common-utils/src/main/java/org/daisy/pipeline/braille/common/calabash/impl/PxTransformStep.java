package org.daisy.pipeline.braille.common.calabash.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XMLCalabashParameterInputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

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

public class PxTransformStep extends DefaultStep implements XProcStep {
	
	private final TransformProvider<Transform> provider;
	private ReadablePipe source = null;
	private WritablePipe result = null;
	private final Hashtable<net.sf.saxon.s9api.QName,RuntimeValue> params = new Hashtable<>();
	
	private static final net.sf.saxon.s9api.QName _query = new net.sf.saxon.s9api.QName("query");
	
	private PxTransformStep(XProcRuntime runtime, XAtomicStep step, TransformProvider<Transform> provider) {
		super(runtime, step);
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
	public void setParameter(net.sf.saxon.s9api.QName name, RuntimeValue value) {
		params.put(name, value);
	}
	
	@Override
	public void setParameter(String port, net.sf.saxon.s9api.QName name, RuntimeValue value) {
		setParameter(name, value);
	}
	
	@Override
	public void run() throws SaxonApiException {
		try {
			Query query = query(getOption(_query).getString());
			XMLTransformer xmlTransformer = null;
			try {
				for (Transform t : logSelect(query, provider, logger))
					if (t instanceof XProcStepProvider) {
						// if the transform is a XProcStepProvider, it is assumed to be declared as a p:pipeline
						xmlTransformer = ((XProcStepProvider)t).newStep(runtime, step);
						break; }
					else if (t instanceof XMLTransform) {
						// fromXmlToXml() is assumed to have only a "source", a "parameters" and a "result" port
						// (i.e. the signature of px:transform without the "query" option)
						xmlTransformer = ((XMLTransform)t).fromXmlToXml();
						break; }}
			catch (NoSuchElementException e) {}
			if (xmlTransformer == null)
				throw new XProcException(step, "Could not find a Transform for query: " + query);
			xmlTransformer.transform(
				ImmutableMap.of(
					new QName("source"), new XMLCalabashInputValue(source, runtime),
					new QName("parameters"), new XMLCalabashParameterInputValue(params)),
				ImmutableMap.of(
					new QName("result"), new XMLCalabashOutputValue(result, runtime))
			).run();
		} catch (Throwable e) {
			if (e instanceof TransformerException && e.getCause() instanceof XProcException)
				// assuming xmlTransformer is cx:eval based
				throw (XProcException)e.getCause();
			else
				throw XProcStep.raiseError(e, step);
		}
		super.run();
	}
	
	@Component(
		name = "px:transform",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}transform" }
	)
	public static class StepProvider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new PxTransformStep(runtime, step, provider);
		}
		
		@Reference(
			name = "XProcTransformProvider",
			unbind = "unbindXProcTransformProvider",
			service = TransformProvider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		@SuppressWarnings(
			"unchecked" // safe cast to TransformProvider<XProcTransform>
		)
		public void bindXProcTransformProvider(TransformProvider<?> provider) {
			providers.add((TransformProvider<Transform>)provider);
			logger.debug("Adding XProcTransform provider: {}", provider);
		}
		
		public void unbindXProcTransformProvider(TransformProvider<?> provider) {
			providers.remove(provider);
			logger.debug("Removing XProcTransform provider: {}", provider);
		}
		
		private List<TransformProvider<Transform>> providers = new ArrayList<>();
		private TransformProvider<Transform> provider = dispatch(providers);
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(PxTransformStep.class);
	
}
