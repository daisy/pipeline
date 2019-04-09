package org.daisy.pipeline.braille.tex.impl;

import java.net.URI;
import java.util.Locale;

import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import static org.daisy.pipeline.braille.common.Provider.util.varyLocale;
import org.daisy.pipeline.braille.common.ResourceRegistry;

import org.daisy.pipeline.braille.tex.TexHyphenatorTablePath;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "org.daisy.pipeline.braille.tex.impl.TexHyphenatorTableRegistry",
	service = {
		TexHyphenatorTableRegistry.class
	}
)
public class TexHyphenatorTableRegistry extends ResourceRegistry<TexHyphenatorTablePath> implements Provider<Locale,URI> {
	
	@Reference(
		name = "TexHyphenatorTablePath",
		unbind = "_unregister",
		service = TexHyphenatorTablePath.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void _register(TexHyphenatorTablePath path) {
		register(path);
		provider.invalidateCache();
	}
	
	protected void _unregister (TexHyphenatorTablePath path) {
		unregister(path);
		provider.invalidateCache();
	}
	
	/**
	 * Try to find a table based on the given locale.
	 * An automatic fallback mechanism is used: if nothing is found for
	 * language-COUNTRY-variant, then language-COUNTRY is searched, then language.
	 */
	public Iterable<URI> get(Locale locale) {
		return provider.get(locale);
	}
	
	private final Provider.util.MemoizingProvider<Locale,URI> provider
		= memoize(
			varyLocale(
				dispatch(paths.values())));
	
}
