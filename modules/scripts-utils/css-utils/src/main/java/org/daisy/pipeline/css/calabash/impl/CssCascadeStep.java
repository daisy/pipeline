package org.daisy.pipeline.css.calabash.impl;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import com.google.common.collect.ImmutableMap;
import static com.google.common.collect.Iterators.forArray;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.saxon.SaxonBuffer;
import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XMLCalabashParameterInputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.css.CssCascader;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.sass.SassCompiler;
import org.daisy.pipeline.css.XsltProcessor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssCascadeStep extends DefaultStep implements XProcStep {

	private ReadablePipe sourcePipe = null;
	private ReadablePipe contextPipe = null;
	private WritablePipe resultPipe = null;
	private Map<String,String> sassVariables = new HashMap<>();
	private final InMemoryURIResolver inMemoryResolver;
	private final URIResolver cssURIResolver;
	private final Iterable<CssCascader> inliners;

	private static final QName _default_stylesheet = new QName("default-stylesheet");
	private static final QName _type = new QName("type");
	private static final QName _media = new QName("media");
	private static final QName _attribute_name = new QName("attribute-name");

	private static final String DEFAULT_MEDIUM = "embossed";
	private static final String DEFAULT_TYPES = "text/css text/x-scss";
	private static final QName DEFAULT_ATTRIBUTE_NAME = new QName("style");

	private CssCascadeStep(XProcRuntime runtime, XAtomicStep step, Iterable<CssCascader> inliners, final URIResolver resolver) {
		super(runtime, step);
		this.inliners = inliners;
		// URI resolver that can resolve in-memory documents
		inMemoryResolver = new InMemoryURIResolver();
		// URI resolver for CSS files
		// first check memory and fall back to the resolver from module-registry
		cssURIResolver = fallback(inMemoryResolver, resolver);
	}

	@Override
	public void setInput(String port, ReadablePipe pipe) {
		if ("source".equals(port))
			sourcePipe = pipe;
		else
			contextPipe = pipe;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		resultPipe = pipe;
	}

	@Override
	public void setParameter(String port, QName name, RuntimeValue value) {
		if ("parameters".equals(port))
			if ("".equals(name.getNamespaceURI())) {
				sassVariables.put(name.getLocalName(), value.getString());
				return; }
		super.setParameter(port, name, value);
	}

	@Override
	public void setParameter(QName name, RuntimeValue value) {
		// Calabash calls this function and never setParameter(String port,
		// ...) so I just have to assume that port is "parameters"
		setParameter("parameters", name, value);
	}

	@Override
	public void reset() {
		sourcePipe.resetReader();
		resultPipe.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			Medium medium = Medium.parse(getOption(_media, DEFAULT_MEDIUM));
			List<String> types = Arrays.asList(getOption(_type, DEFAULT_TYPES).trim().split("\\s+"));
			if (!types.contains("text/css"))
				throw new IllegalArgumentException("'type' option must contain 'text/css'");
			boolean enableSass = types.contains("text/x-scss");
			for (CssCascader inliner : inliners)
				if (inliner.supportsMedium(medium)) {
					QName attributeName = getOption(_attribute_name, DEFAULT_ATTRIBUTE_NAME);
					inMemoryResolver.setContext(contextPipe);
					inliner.newInstance(
						medium,
						getOption(_default_stylesheet, ""),
						cssURIResolver,
						enableSass
							? new SassCompiler(cssURIResolver, Collections.unmodifiableMap(sassVariables))
							: null,
						new XSLT(runtime, step),
						SaxonHelper.jaxpQName(attributeName)
					).transform(
						new XMLCalabashInputValue(sourcePipe, runtime),
						new XMLCalabashOutputValue(resultPipe, runtime)
					).run();
					return; }
			throw new RuntimeException("No CSS inliner implementation found for medium " + medium); }
		catch (Exception e) {
			logger.error("px:css-cascade failed", e);
			throw new XProcException(step.getNode(), e); }
	}

	private static URIResolver fallback(final URIResolver... resolvers) {
		return new URIResolver() {
			public Source resolve(String href, String base) throws javax.xml.transform.TransformerException {
				Iterator<URIResolver> iterator = forArray(resolvers);
				while (iterator.hasNext()) {
					Source source = iterator.next().resolve(href, base);
					if (source != null)
						return source; }
				return null;
			}
		};
	}

	// extend XSLT to make it implement XMLTransformer
	private class XSLT extends com.xmlcalabash.library.XSLT implements XProcStep, XsltProcessor {
		public XSLT(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}
		public void setParameter(String port, QName name, RuntimeValue value) {
			if ("parameters".equals(port))
				setParameter(name, value);
			else
				super.setParameter(port, name, value);
		}
		public XMLInputValue<Void> transform(URI stylesheetURI,
		                                     XMLInputValue<?> source,
		                                     Map<String,String> parameters) {
			Configuration conf = runtime.getProcessor().getUnderlyingConfiguration();
			XMLInputValue<Void> stylesheet = new SaxonInputValue(runtime.parse(stylesheetURI.toASCIIString(), null), conf);
			Map<javax.xml.namespace.QName,InputValue<?>> params = new HashMap<>(); {
				for (String p : parameters.keySet())
					params.put(new javax.xml.namespace.QName(p), new InputValue(parameters.get(p))); }
			SaxonBuffer buf = new SaxonBuffer(conf);
			transform(
				ImmutableMap.of(
					new javax.xml.namespace.QName("source"), source,
					new javax.xml.namespace.QName("stylesheet"), stylesheet,
					new javax.xml.namespace.QName("parameters"), XMLCalabashParameterInputValue.of(new InputValue(params))),
				ImmutableMap.of(
					new javax.xml.namespace.QName("result"), buf.asOutput())
			).run();
			buf.done();
			return buf.asInput();
		}
	}

	@Component(
		name = "pxi:css-cascade",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}css-cascade" }
	)
	public static class Provider implements XProcStepProvider {

		private URIResolver resolver;
		private final List<CssCascader> inliners = new ArrayList<>();

		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new CssCascadeStep(runtime, step, inliners, resolver);
		}

		@Reference(
			name = "URIResolver",
			unbind = "-",
			service = URIResolver.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		public void setUriResolver(URIResolver resolver) {
			this.resolver = resolver;
		}

		@Reference(
			name = "CssCascader",
			unbind = "unbindCssCascader",
			service = CssCascader.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		public void bindCssCascader(CssCascader inliner) {
			this.inliners.add(inliner);
		}

		public void unbindCssCascader(CssCascader inliner) {
			this.inliners.remove(inliner);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(CssCascadeStep.class);

}
