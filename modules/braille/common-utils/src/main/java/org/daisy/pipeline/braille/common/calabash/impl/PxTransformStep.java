package org.daisy.pipeline.braille.common.calabash.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.extensions.Eval;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.QName;

import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.daisy.pipeline.braille.common.calabash.JobContextImpl;
import org.daisy.pipeline.braille.common.JobContext;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.Transform.XProc;
import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.logSelect;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PxTransformStep extends Eval {
	
	private final JobContext context;
	private final TransformProvider<Transform> provider;
	private final ReadableDocument pipeline;
	
	private static final QName _query = new QName("query");
	private static final QName _temp_dir = new QName("temp-dir");
	private static final QName _step = new QName("step");
	private static final QName _name = new QName("name");
	private static final QName _namespace = new QName("namespace");
	private static final QName _value = new QName("value");
	
	private PxTransformStep(XProcRuntime runtime, XAtomicStep step, TransformProvider<Transform> provider) {
		super(runtime, step);
		this.context = new JobContextImpl(runtime.getMessageListener());
		this.provider = provider;
		pipeline = new ReadableDocument(runtime);
		setInput("pipeline", pipeline);
	}
	
	private static class ReadableDocument extends com.xmlcalabash.io.ReadableDocument {
		private ReadableDocument(XProcRuntime runtime) {
			super(runtime);
		}
		private URI uri;
		private void setURI(URI uri) {
			this.uri = uri;
		}
		private boolean readDoc = false;
		@Override
		protected void readDoc() {
			if (readDoc) return;
			readDoc = true;
			documents.add(runtime.parse(uri.toASCIIString(), ""));
		}
	}
	
	private boolean setup = false;
	
	private void setup() {
		if (!setup) {
			Query query = query(getOption(_query).getString());
			XProc xproc = null;
			try {
				for (Transform t : logSelect(query, provider, context))
					try {
						xproc = t.asXProc();
						break; }
					catch (UnsupportedOperationException e) {}}
			catch (NoSuchElementException e) {}
			if (xproc == null)
				throw new RuntimeException("Could not find a Transform for query: " + query);
			RuntimeValue tempDir = getOption(_temp_dir);
			pipeline.setURI(xproc.getURI());
			if (xproc.getName() != null) {
				final QName step = new QName(xproc.getName());
				setOption(_step, new RuntimeValue() { public QName getQName() { return step; }});
				throw new RuntimeException("p:library not supported due to a bug in cx:eval"); }
			if (xproc.getOptions() != null || tempDir != null) {
				final Map<String,String> options = new HashMap<String,String>();
				if (xproc.getOptions() != null)
					options.putAll(xproc.getOptions());
				if (tempDir != null)
					options.put("temp-dir", tempDir.getString());
				setInput("options", new com.xmlcalabash.io.ReadableDocument(runtime) {
					private boolean readDoc = false;
					@Override
					protected void readDoc() {
						if (readDoc) return;
						readDoc = true;
						TreeWriter optionWriter = new TreeWriter(runtime);
						optionWriter.startDocument(step.getNode().getBaseURI());
						optionWriter.addStartElement(cx_options);
						optionWriter.startContent();
						for (String option : options.keySet()) {
							optionWriter.addStartElement(cx_option);
							optionWriter.addAttribute(_name, option);
							optionWriter.addAttribute(_namespace, "");
							optionWriter.addAttribute(_value, options.get(option));
							optionWriter.startContent();
							optionWriter.addEndElement(); }
						optionWriter.addEndElement();
						optionWriter.endDocument();
						documents.add(optionWriter.getResult()); }}); }}
		setup = true;
	}
	
	@Override
	public void setParameter(String port, QName name, RuntimeValue value) {
		if ("parameters".equals(port))
			setParameter(name, value);
		else
			throw new XProcException("No parameters allowed on port '" + port + "'");
	}
	
	@Override
	public void run() throws SaxonApiException {
		try { setup(); }
		catch (Exception e) {
			logger.error("px:transform failed", e);
			throw new XProcException(step.getNode(), e); }
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
		
		private List<TransformProvider<Transform>> providers = new ArrayList<TransformProvider<Transform>>();
		private TransformProvider<Transform> provider = dispatch(providers);
		
	}
	
	private static final Logger logger = LoggerFactory.getLogger(PxTransformStep.class);
	
}
