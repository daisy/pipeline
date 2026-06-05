package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.daisy.dotify.api.table.Table;

import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.UnityBrailleTranslator;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.pef.TableRegistry;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * {@link BrailleTranslatorProvider} of PreTranslatedBrailleTranslator.
 */
@Component(
	name = "org.daisy.pipeline.braille.dotify.impl.PreTranslatedBrailleTranslatorProvider",
	service = {
		BrailleTranslatorProvider.class
	}
)
public class PreTranslatedBrailleTranslatorProvider extends AbstractTransformProvider<BrailleTranslator>
                                                    implements BrailleTranslatorProvider<BrailleTranslator>  {

	protected Iterable<BrailleTranslator> _get(Query query) {
		MutableQuery q = mutableQuery(query);
		String brailleCharset = q.containsKey("braille-charset")
			? q.removeOnly("braille-charset").getValue().get()
			: null;
		boolean isPreTranslatedQuery = false; {
			for (Query.Feature f : q) {
				String key = f.getKey();
				String val = f.getValue().orElse(null);
				if ("input".equals(key)) {
					if ("braille".equals(val))
						isPreTranslatedQuery = true;
					else if (!"text-css".equals(val)) {
						isPreTranslatedQuery = false;
						break; }}
				else if ("output".equals(key)) {
					if (!"braille".equals(val)) {
						isPreTranslatedQuery = false;
						break; }}
				else if ("document-locale".equals(key) && val != null && !isPreTranslatedQuery) {
					try {
						if ("Brai".equals(parseLocale(val).getScript()))
							isPreTranslatedQuery = true; }
					catch (IllegalArgumentException e) {}}
				else {
					isPreTranslatedQuery = false;
					break; }}}
		if (isPreTranslatedQuery) {
			try {
				return AbstractTransformProvider.util.Iterables.of(
					new UnityBrailleTranslator(
						brailleCharset != null
							? tableRegistry.get(mutableQuery().add("id", brailleCharset))
							               .iterator().next()
							               .newBrailleConverter()
							: null,
						true));
			} catch (NoSuchElementException e) {
				// should not happen
			}
		}
		return AbstractTransformProvider.util.Iterables.<BrailleTranslator>empty();
	}

	private TableRegistry tableRegistry;

	@Reference(
		name = "TableRegistry",
		unbind = "-",
		service = TableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableRegistry(TableRegistry registry) {
		tableRegistry = registry;
	}
}
