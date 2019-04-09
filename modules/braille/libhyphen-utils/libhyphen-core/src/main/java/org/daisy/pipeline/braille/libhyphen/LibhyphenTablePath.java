package org.daisy.pipeline.braille.libhyphen;

import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.google.common.base.Functions;
import static com.google.common.base.Functions.toStringFunction;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import static com.google.common.collect.Iterables.filter;

import static org.daisy.common.file.URIs.asURI;
import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.BundledResourcePath;
import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.util.Predicates.matchesGlobPattern;

public class LibhyphenTablePath extends BundledResourcePath implements LibhyphenTableProvider {
	
	@Override
	protected void activate(Map<?,?> properties, Class<?> context) throws IllegalArgumentException {
		if (properties.get(UNPACK) != null)
			throw new IllegalArgumentException(UNPACK + " property not supported");
		Map<Object,Object> props = new HashMap<Object,Object>(properties);
		props.put(BundledResourcePath.UNPACK, true);
		super.activate(props, context);
	}
	
	/**
	 * Lookup a table based on a locale, using the fact that table names are
	 * always in the form `hyph_language[_COUNTRY[_variant]].dic`
	 */
	public Iterable<URI> get(Locale locale) {
		return provider.get(locale);
	}
	
	private Provider<Locale,URI> provider = new Provider.util.VaryLocale<Locale,URI>() {
		public Iterable<URI> _get(Locale locale) {
			String language = locale.getLanguage().toLowerCase();
			String country = locale.getCountry().toUpperCase();
			String variant = locale.getVariant().toLowerCase();
			URI resource = null;
			if (!"".equals(variant))
				resource = asURI(String.format("hyph_%s_%s_%s.dic", language, country, variant));
			else if (!"".equals(country))
				resource = asURI(String.format("hyph_%s_%s.dic", language, country));
			else
				resource = asURI(String.format("hyph_%s.dic", language));
			if (containsResource(resource))
				return Optional.of(canonicalize(resource)).asSet();
			return Optional.<URI>absent().asSet();
		}
		@Override
		public Iterable<URI> fallback(Locale locale) {
			return filter(
				listResources(),
				Predicates.compose(
					matchesGlobPattern(String.format("hyph_%s_*.dic", locale.getLanguage().toLowerCase())),
					Functions.compose(URLs::decode, toStringFunction())));
		}
	};
}
