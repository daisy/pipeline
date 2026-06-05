package org.daisy.pipeline.asciimath.saxon.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;

import net.sf.saxon.Configuration;

import org.daisy.common.saxon.SaxonHelper;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.tts.TTSInputProcessor;
import org.daisy.pipeline.asciimathml.ASCIIMathML;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

@Component(
	name = "pf:asciimath-to-ssml",
	service = { ExtensionFunctionProvider.class }
)
public class ASCIIMathToSSMLFunctionProvider extends ReflexiveExtensionFunctionProvider {

	public ASCIIMathToSSMLFunctionProvider() {
		super();
		addExtensionFunctionDefinitionsFromClass(ASCIIMathToSSML.class, new ASCIIMathToSSML());
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

	public class ASCIIMathToSSML {

		private final static String MIME_ASCIIMATH = "text/x-asciimath";
		private final static String MIME_MATHML = "application/mathml+xml";

		public void transform(XMLInputValue<?> asciimath, XMLOutputValue<?> ssml, Locale language, Configuration saxonConfig) {
			asciimath = asciimath.ensureSingleItem();
			TTSInputProcessor processor = null; {
				for (TTSInputProcessor p : processors)
					if ((p.supportsInputMediaType(MIME_ASCIIMATH) || p.supportsInputMediaType(MIME_MATHML))
					    && (processor == null || p.getPriority() > processor.getPriority()))
						processor = p.forInputMediaType(
							p.supportsInputMediaType(MIME_ASCIIMATH)
								? MIME_ASCIIMATH
								: MIME_MATHML);
			}
			if (processor != null) {
				SingleInSingleOutXMLTransformer transformer = null; {
					if (processor instanceof XMLTransformer)
						transformer = SingleInSingleOutXMLTransformer.from((XMLTransformer)processor);
					else
						; // could be a XProcStepProvider, but not supported for now
				}
				if (transformer != null) {
					XMLInputValue<?> math = asciimath;
					if (!processor.supportsInputMediaType(MIME_ASCIIMATH)) {
						// first convert to MathML
						Node n = asciimath.asNodeIterator().next();
						if (!(n instanceof Text))
							throw new IllegalArgumentException("Expected text node, but got;: " + n.getClass());
						Element mathml = ASCIIMathML.convert(n.getNodeValue());
						math = new SaxonInputValue(SaxonHelper.nodeInfoFromNode(mathml, saxonConfig));
					}
					transformer.transform(
						math,
						ssml,
						new InputValue<>(ImmutableMap.of(new QName("language"), new InputValue<>(language)))
					).run();
					return;
				}
			}
			throw new UnsupportedOperationException("No ASCIIMath to SSML processor found");
		}
	}
}
