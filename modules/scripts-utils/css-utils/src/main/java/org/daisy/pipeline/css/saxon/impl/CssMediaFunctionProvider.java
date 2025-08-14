package org.daisy.pipeline.css.saxon.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;
import org.daisy.pipeline.css.impl.MediumParser;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.MediumProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Attr;

@Component(
	name = "pf:css-parse-medium",
	service = { ExtensionFunctionProvider.class }
)
public class CssMediaFunctionProvider extends ReflexiveExtensionFunctionProvider {

	public CssMediaFunctionProvider() {
		super();
		addExtensionFunctionDefinitionsFromClass(CssMediaFunctions.class, new CssMediaFunctions());
	}

	private final List<MediumProvider> mediumProviders = new ArrayList<>();
	private final MediumProvider mediumProvider = MediumProvider.dispatch(mediumProviders, true);

	@Reference(
		name = "MediumProvider",
		unbind = "-",
		service = MediumProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void bindMediumProvider(MediumProvider provider) {
		mediumProviders.add(provider);
	}

	public class CssMediaFunctions {

		private CssMediaFunctions() {}

		// pf:css-parse-medium
		public Medium parse(Iterator<Object> args) throws IllegalArgumentException {
			MediumParser parser = new MediumParser(mediumProvider);
			boolean first = true;
			try {
				while (args.hasNext()) {
					Object arg = args.next();
					if (arg instanceof Medium) {
						if (!first || args.hasNext())
							throw new IllegalArgumentException("Medium object specified but not the only item in the sequence");
						return (Medium)arg;
					} else if (arg instanceof String) {
						parser.add((String)arg);
					} else if (arg instanceof Map) {
						Map<String,Object> features = new HashMap<>();
						for (Object k : ((Map)arg).keySet()) {
							if (!(k instanceof String))
								throw new IllegalArgumentException("Unexpected argument type: map with non-string key");
							Object v = ((Map)arg).get(k);
							if (v instanceof Attr)
								v = ((Attr)v).getNodeValue();
							else if (!(v instanceof String || v instanceof Integer || v instanceof Long || v instanceof Boolean || v instanceof Locale))
								throw new IllegalArgumentException("Unexpected argument type: map with value of type " + v.getClass());
							features.put((String)k, v);
						}
						parser.add(features);
					} else
						throw new IllegalArgumentException("Unexpected argument type: " + arg.getClass());
					first = false;
				}
				if (first)
					throw new IllegalArgumentException("Argument must not be an empty sequence");
				return parser.parse();
			} catch (IllegalArgumentException|NoSuchElementException e) {
				throw new IllegalArgumentException("Argument can not be resolved to a medium", e);
			}
		}

		// pf:media-query-matches
		public boolean matches(String mediaQuery, Iterator<Object> medium) throws IllegalArgumentException {
			return parse(medium).matches(mediaQuery);
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(CssMediaFunctions.class);

}
