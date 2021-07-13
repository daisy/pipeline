package org.daisy.pipeline.braille.css;

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
			for (RuleTextTransform rule : filter(new InlineStyle(style), RuleTextTransform.class))
				for (Declaration d : rule)
					if (d.getProperty().equals("system")
					    && d.size() == 1
					    && d.get(0) instanceof TermIdent
					    && "braille-translator".equals(((TermIdent)d.get(0)).getValue())) {
						MutableQuery query = mutableQuery(baseQuery);
						for (Declaration dd : rule)
							if (dd.getProperty().equals("system")
							    && dd.size() == 1
							    && dd.get(0) instanceof TermIdent
							    && "braille-translator".equals(((TermIdent)dd.get(0)).getValue()))
								;
							else if (!dd.getProperty().equals("system")
							         && dd.size() == 1
							         && (dd.get(0) instanceof TermIdent
							             || dd.get(0) instanceof TermURI
							             || dd.get(0) instanceof TermInteger)) {
								String key = dd.getProperty();
								String value;
								if (dd.get(0) instanceof TermURI) {
									URL cssBase = ((TermURI)dd.get(0)).getBase(); // this is always null because the source is a string
									value = URLs.resolve(cssBase != null ? URLs.asURI(cssBase) : baseURI,
									                     URLs.asURI(((TermURI)dd.get(0)).getValue()))
										.toASCIIString();
								} else {
									if (dd.get(0) instanceof TermInteger)
										value = "" + ((TermInteger)dd.get(0)).getIntValue();
									else
										value = "" + dd.get(0).getValue();
									if (query.containsKey(key))
										query.removeAll(key);
								}
								if (key.equals("contraction") && value.equals("no"))
									query.removeAll("grade");
								// FIXME: support this in Liblouis translator
								if (key.equals("table") || key.equals("liblouis-table")) {
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
						break;
					}
			return queries;
		} else
			return null;
	}
}
