package org.daisy.pipeline.braille.common.saxon.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslator.FromStyledTextToBraille;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
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
		super(TextTransform.class);
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

		public Iterator<String> transform(String query, Iterator<String> text) {
			return transform(query, text, null);
		}

		public Iterator<String> transform(String query, Iterator<String> text, Iterator<String> style) {
			return transform(query, text, style, null);
		}

		public Iterator<String> transform(String query, Iterator<String> text, Iterator<String> style, Iterator<String> lang) {
			List<CSSStyledText> styledText = new ArrayList<>();
			while (text.hasNext()) {
				String t = text.next();
				if (style != null) {
					if (!style.hasNext())
						throw new IllegalArgumentException("Lengths of text and style sequences must match");
					if (lang != null) {
						if (!lang.hasNext())
							throw new IllegalArgumentException("Lengths of text and lang sequences must match");
						styledText.add(new CSSStyledText(t, style.next(), parseLocale(lang.next())));
					} else
						styledText.add(new CSSStyledText(t, style.next()));
				} else
					styledText.add(new CSSStyledText(t));
			}
			if (style != null && style.hasNext())
				throw new IllegalArgumentException("Lengths of text and style sequences must match");
			if (lang != null && lang.hasNext())
				throw new IllegalArgumentException("Lengths of text and lang sequences must match");
			return transform(query(query), styledText);
		}

		private Iterator<String> transform(Query query, List<CSSStyledText> styledText) {
			for (BrailleTranslator t : translatorRegistry.getWithHyphenator(query)) {
				FromStyledTextToBraille fsttb;
				try {
					fsttb = t.fromStyledTextToBraille();
				} catch (UnsupportedOperationException e) {
					logger.trace("Translator does not implement the FromStyledTextToBraille interface: " + t);
					continue;
				}
				try {
					// FIXME: don't ignore result style
					List<String> braille = new ArrayList<>();
					for (CSSStyledText b : fsttb.transform(styledText))
						braille.add(b.getText());
					return braille.iterator();
				} catch (Exception e) {
					logger.debug("Failed to translate string with translator " + t);
					throw e; }
			}
			throw new RuntimeException("Could not find a BrailleTranslator for query: " + query);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TextTransformDefinition.class);

}
