package org.daisy.pipeline.braille.common;

import java.util.LinkedHashMap;
import java.util.Map;
import java.net.URI;
import java.net.URL;

import static com.google.common.collect.Iterables.filter;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermURI;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.LanguageRange;
import org.daisy.braille.css.RuleHyphenationResource;
import org.daisy.common.file.URLs;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;

public class HyphenationResourceParser {

	/**
	 * Parse <code>@hyphenation-resource</code> rules and return them as an ordered map of language
	 * ranges to queries.
	 */
	public static Map<LanguageRange,Query> getHyphenatorQueries(String style, URI baseURI, Query baseQuery) {
		if (style != null && !"".equals(style)) {
			Map<LanguageRange,Query> queries = null;
		  rules: for (RuleHyphenationResource rule : filter(new InlineStyle(style), RuleHyphenationResource.class)) {
				String system = null; {
					for (Declaration d : rule)
						if (d.getProperty().equals("system")) {
							if (d.size() == 1
							    && d.get(0) instanceof TermIdent) {
								system = ((TermIdent)d.get(0)).getValue();
								break;
							} else
								continue rules; }}
				MutableQuery query = mutableQuery(baseQuery); // baseQuery should normally only contain "document-locale" and "hyphenator"
				                                              // features
				if (system != null) {
					if (query.containsKey("hyphenator"))
						query.removeAll("hyphenator");
					query.add("hyphenator", system);
				}
				for (Declaration d : rule)
					if (d.getProperty().equals("system")) continue;
					else if (d.size() == 1
					         && (d.get(0) instanceof TermIdent
					             || d.get(0) instanceof TermURI
					             || d.get(0) instanceof TermInteger)) {
						String key = d.getProperty();
						if (key.equals("document-locale")) {
							// document-locale is an internal feature and not supported in @hyphenation-resource rules
							query = null;
							break;
						}
						String value;
						if (d.get(0) instanceof TermURI) {
							URI uri = URLs.asURI(((TermURI)d.get(0)).getValue());
							if (!uri.isAbsolute() && !uri.getSchemeSpecificPart().startsWith("/")) {
								// relative URI
								URI cssBase; {
									URL b = ((TermURI)d.get(0)).getBase(); // this is always null because the style is provided as a string
									cssBase = b != null ? URLs.asURI(b) : baseURI;
								}
								uri = URLs.resolve(cssBase, uri);
							}
							value = uri.toASCIIString();
						} else {
							if (d.get(0) instanceof TermInteger)
								value = "" + ((TermInteger)d.get(0)).getIntValue();
							else
								value = "" + d.get(0).getValue();
						}
						if (query.containsKey(key))
							// features in base query are overridden by descriptors in @hyphenation-resource rule
							query.removeAll(key);
						if (system != null && key.equals("hyphenator") && !system.equals(value)) {
							query = null;
							break; }
						query.add(key, value);
					} else {
						query = null;
						break;
					}
				if (query != null) {
					if (queries == null) queries = new LinkedHashMap<>(); // iteration order = insertion order
					for (LanguageRange l : rule.getLanguageRanges())
						queries.put(l, query);
				}
			}
			return queries;
		} else
			return null;
	}
}
