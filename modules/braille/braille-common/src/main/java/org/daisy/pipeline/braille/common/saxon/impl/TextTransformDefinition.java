package org.daisy.pipeline.braille.common.saxon.impl;

import java.util.Iterator;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator.FromStyledTextToBraille;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.css.CSSStyledText;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "TextTransform",
	service = { ExtensionFunctionProvider.class }
)
public class TextTransformDefinition extends ReflexiveExtensionFunctionProvider {

	public TextTransformDefinition() {
		super();
		addExtensionFunctionDefinitionsFromClass(TextTransform.class, new TextTransform());
	}

	private BrailleTranslatorRegistry translatorRegistry;

	@Reference(
		name = "BrailleTranslatorRegistry",
		unbind = "-",
		service = BrailleTranslatorRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindBrailleTranslatorRegistry(BrailleTranslatorRegistry registry) {
		translatorRegistry = registry.withContext(logger);
		logger.debug("Binding BrailleTranslator registry: {}", registry);
	}

	public class TextTransform {

		public Iterator<CSSStyledText> transform(String query, Iterable<CSSStyledText> styledText) {
			return transform(query(query), styledText);
		}

		private Iterator<CSSStyledText> transform(Query query, Iterable<CSSStyledText> styledText) {
			for (BrailleTranslator t : translatorRegistry.getWithHyphenator(query)) {
				FromStyledTextToBraille fsttb;
				try {
					fsttb = t.fromStyledTextToBraille();
				} catch (UnsupportedOperationException e) {
					logger.trace("Translator does not implement the FromStyledTextToBraille interface: " + t);
					continue;
				}
				try {
					return fsttb.transform(styledText).iterator();
				} catch (Exception e) {
					logger.debug("Failed to translate string with translator " + t);
					throw e; }
			}
			throw new RuntimeException("Could not find a BrailleTranslator for query: " + query);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TextTransformDefinition.class);

}
