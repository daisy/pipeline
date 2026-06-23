package org.daisy.pipeline.mathml.tts.saxon.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import com.xmlcalabash.core.XProcRuntime;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.Mult;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.daisy.pipeline.tts.TTSInputProcessor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "pf:mathml-to-ssml",
	service = { ExtensionFunctionProvider.class }
)
public class MathMLToSSMLFunctionProvider implements ExtensionFunctionProvider {

	@Override
	public Collection<ExtensionFunctionDefinition> getDefinitions() {
		return getDefinitions(null);
	}

	@Override
	public Collection<ExtensionFunctionDefinition> getDefinitions(Collection<Object> context) {
		XProcRuntime runtime = context != null
			? Iterables.getOnlyElement(Iterables.filter(context, XProcRuntime.class), null)
			: null;
		XProcMonitor monitor = context != null
			? Iterables.getOnlyElement(Iterables.filter(context, XProcMonitor.class), null)
			: null;
		Map<String,String> properties = context != null
			? (Map<String,String>)Iterables.getOnlyElement(Iterables.filter(context, Map.class), null)
			: null;
		return new ReflexiveExtensionFunctionProvider() {{
			addExtensionFunctionDefinitionsFromClass(
				MathMLToSSML.class, new MathMLToSSML(runtime, monitor, properties));
		}}.getDefinitions();
	}

	private final List<TTSInputProcessor> processors = new ArrayList<>();

	@Reference(
		name = "TTSInputProcessor",
		unbind = "-",
		service = TTSInputProcessor.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void bindTTSInputProcessor(TTSInputProcessor processor) {
		processors.add(processor);
	}

	public class MathMLToSSML {

		private final static String MIME_MATHML = "application/mathml+xml";

		private final XProcRuntime runtime;
		private final XProcMonitor monitor;
		private final Map<String,String> properties;
		private final List<TTSInputProcessor> mathmlProcessorChain;

		private MathMLToSSML(XProcRuntime runtime, XProcMonitor monitor, Map<String,String> properties) {
			this.runtime = runtime;
			this.monitor = monitor;
			this.properties = properties;
			this.mathmlProcessorChain = new ArrayList<>(); {
				for (TTSInputProcessor p : processors)
					if (p.supportsInputMediaType(MIME_MATHML))
						mathmlProcessorChain.add(p.forInputMediaType(MIME_MATHML));
				Collections.sort(mathmlProcessorChain,
				                 Comparator.comparingInt(TTSInputProcessor::getPriority)
				                           .reversed());
			}
		}

		public void transform(XMLInputValue<?> mathml, XMLOutputValue<?> ssml, Locale language) {
			Mult<? extends XMLInputValue<?>> mmlCopies = mathml.ensureSingleItem().mult(mathmlProcessorChain.size());
			Iterator<TTSInputProcessor> pp = mathmlProcessorChain.iterator();
			if (!pp.hasNext())
				throw new UnsupportedOperationException("No MathML to SSML processor found");
			while (pp.hasNext()) {
				TTSInputProcessor p = pp.next();
				SingleInSingleOutXMLTransformer transformer = null; {
					if (p instanceof XProcStepProvider) {
						if (runtime != null)
							transformer = SingleInSingleOutXMLTransformer.from(
								((XProcStepProvider)p).newStep(runtime, null, monitor, properties));
					} else if (p instanceof XMLTransformer)
						transformer = SingleInSingleOutXMLTransformer.from((XMLTransformer)p);
					else
						; // should not happen
				}
				if (transformer != null) {
					try {
						transformer.transform(
							mmlCopies.get(),
							ssml,
							new InputValue<>(ImmutableMap.of(new QName("language"), new InputValue<>(language)))
						).run();
						return;
					} catch (Throwable e) {
						logger.warn(p.getDisplayName() + " failed to process MathML. (Please see detailed log for more info.)");
						logger.debug("Error stack trace:", e);
						if (pp.hasNext())
							logger.warn("Processing MathML with fallback...");
					}
				}
			}
			throw new UnsupportedOperationException("MathML could not be converted to SSML");
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(MathMLToSSMLFunctionProvider.class);
}
