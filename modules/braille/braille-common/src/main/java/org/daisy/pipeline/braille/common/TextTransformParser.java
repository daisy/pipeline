package org.daisy.pipeline.braille.common;

import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.URL;

import static com.google.common.collect.Iterables.filter;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermURI;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.RuleTextTransform;
import org.daisy.common.file.URLs;

import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;

public class TextTransformParser {

	/**
	 * Parse <code>@text-transform</code> rules with <code>system: braille-translator</code> and
	 * return them as a set of named queries.
	 */
	public static Map<String,Query> getBrailleTranslatorQueries(String style, URI baseURI, Query baseQuery) {
		if (style != null && !"".equals(style)) {
			Map<String,Query> queries = null;
			for (RuleTextTransform rule : filter(new InlineStyle(style), RuleTextTransform.class)) {
				// default system is braille
				String system = "braille";
				for (Declaration d : rule)
					if (d.getProperty().equals("system")) {
						if (d.size() == 1
						    && d.get(0) instanceof TermIdent)
							system = ((TermIdent)d.get(0)).getValue();
						else
							system = null;
						break; }
				if (system == null)
					continue;
				if ("braille-translator".equals(system))
					system = "braille";
				String translator = null;
				if ("liblouis".equals(system)) {
					system = "braille";
					translator = "liblouis"; }
				MutableQuery query = mutableQuery(baseQuery);
				// System could be "braille" or something else. Because at this point DAISY Pipeline
				// does not support any other systems than braille systems, we assume that
				// "something else" could be a feature that Liblouis or another translator provider
				// may support.
				if (!"braille".equals(system)) {
					if (query.containsKey("system"))
						query.removeAll("system");
					query.add("system", system); }
				if (translator != null)
					query.add("translator", translator);
				for (Declaration d : rule)
					if (d.getProperty().equals("system")) continue;
					else if (d.size() == 1
					         && (d.get(0) instanceof TermIdent
					             || d.get(0) instanceof TermURI
					             || d.get(0) instanceof TermInteger)) {
						String key = d.getProperty();
						if (key.equals("charset") || key.equals("braille-charset")) {
							// (silently) ignoring this feature because all braille translators
							// used in the whole conversion must use the same output character set
							continue;
						}
						String value;
						if (d.get(0) instanceof TermURI) {
							URL cssBase = ((TermURI)d.get(0)).getBase(); // this is always null because the source is a string
							value = URLs.resolve(cssBase != null ? URLs.asURI(cssBase) : baseURI,
							                     URLs.asURI(((TermURI)d.get(0)).getValue()))
							            .toASCIIString();
						} else {
							if (d.get(0) instanceof TermInteger)
								value = "" + ((TermInteger)d.get(0)).getIntValue();
							else
								value = "" + d.get(0).getValue();
							if (query.containsKey(key))
								query.removeAll(key);
						}
						if (translator != null && key.equals("translator") && !translator.equals(value)) {
							query = null;
							break; }
						else if (key.equals("contraction") && value.equals("no"))
							query.removeAll("grade");
						else if (key.equals("table") || key.equals("liblouis-table")) {
							query.removeAll("locale");
							query.removeAll("type");
							query.removeAll("contraction");
							query.removeAll("grade");
							query.removeAll("dots");
							query.removeAll("direction");
						}
						query.add(key, value);
					} else {
						query = null;
						break;
					}
				if (query != null) {
					if (queries == null) queries = new HashMap<>();
					String name = rule.getName();
					queries.put(name == null ? "auto" : name, query);
				}
			}
			return queries;
		} else
			return null;
	}
}
