package org.daisy.pipeline.mathml.tts.saxon.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import com.xmlcalabash.core.XProcRuntime;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

import org.daisy.common.transform.InputValue;
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

		private MathMLToSSML(XProcRuntime runtime, XProcMonitor monitor, Map<String,String> properties) {
			this.runtime = runtime;
			this.monitor = monitor;
			this.properties = properties;
		}

		public void transform(XMLInputValue<?> mathml, XMLOutputValue<?> ssml, Locale language) {
			mathml = mathml.ensureSingleItem();
			TTSInputProcessor processor = null; {
				for (TTSInputProcessor p : processors)
					if (p.supportsInputMediaType(MIME_MATHML)
					    && (processor == null || p.getPriority() > processor.getPriority()))
						processor = p.forInputMediaType(MIME_MATHML);
			}
			if (processor != null) {
				SingleInSingleOutXMLTransformer transformer = null; {
					if (processor instanceof XProcStepProvider) {
						if (runtime != null)
							transformer = SingleInSingleOutXMLTransformer.from(
								((XProcStepProvider)processor).newStep(runtime, null, monitor, properties));
					} else if (processor instanceof XMLTransformer)
						transformer = SingleInSingleOutXMLTransformer.from((XMLTransformer)processor);
					else
						; // should not happen
				}
				if (transformer != null) {
					transformer.transform(
						mathml,
						ssml,
						new InputValue<>(ImmutableMap.of(new QName("language"), new InputValue<>(language)))
					).run();
					return;
				}
			}
			throw new UnsupportedOperationException("No MathML to SSML processor found");
		}
	}
}
