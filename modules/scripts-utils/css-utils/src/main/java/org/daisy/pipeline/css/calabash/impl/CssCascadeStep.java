package org.daisy.pipeline.css.calabash.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;

import static com.google.common.collect.Iterators.forArray;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.css.CssCascader;
import org.daisy.pipeline.css.SassCompiler;

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
	private Map<String,String> sassVariables = new HashMap<String,String>();
	private final InMemoryURIResolver inMemoryResolver;
	private final URIResolver cssURIResolver;
	private final SassCompiler sassCompiler;
	private final Iterable<CssCascader> inliners;

	private static final QName _default_stylesheet = new QName("default-stylesheet");
	private static final QName _media = new QName("media");
	private static final QName _attribute_name = new QName("attribute-name");

	private static final String DEFAULT_MEDIUM = "embossed";
	private static final QName DEFAULT_ATTRIBUTE_NAME = new QName("style");

	private CssCascadeStep(XProcRuntime runtime, XAtomicStep step, Iterable<CssCascader> inliners, final URIResolver resolver) {
		super(runtime, step);
		this.inliners = inliners;
		// URI resolver that can resolve in-memory documents
		inMemoryResolver = new InMemoryURIResolver();
		// URI resolver for CSS files
		// first check memory and fall back to the resolver from module-registry
		cssURIResolver = fallback(inMemoryResolver, resolver);
		sassCompiler = new SassCompiler(cssURIResolver, Collections.unmodifiableMap(sassVariables));
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
		if ("sass-variables".equals(port))
			if ("".equals(name.getNamespaceURI())) {
				sassVariables.put(name.getLocalName(), value.getString());
				return; }
		super.setParameter(port, name, value);
	}

	@Override
	public void setParameter(QName name, RuntimeValue value) {
		// Calabash calls this function and never setParameter(String port,
		// ...) so I just have to assume that port is "sass-variables"
		setParameter("sass-variables", name, value);
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
			String medium = getOption(_media, DEFAULT_MEDIUM);
			for (CssCascader inliner : inliners)
				if (inliner.supportsMedium(medium)) {
					QName attributeName = getOption(_attribute_name, DEFAULT_ATTRIBUTE_NAME);
					inMemoryResolver.setContext(contextPipe);
					inliner.newInstance(
						medium,
						getOption(_default_stylesheet, ""),
						cssURIResolver,
						sassCompiler,
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
