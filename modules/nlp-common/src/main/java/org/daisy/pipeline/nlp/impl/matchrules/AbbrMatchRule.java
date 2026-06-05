package org.daisy.pipeline.nlp.impl.matchrules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;

/**
 * Match abbreviations such as "i.e." and "i.e".
 */
public class AbbrMatchRule extends CompactAbbrMatchRule {

	public AbbrMatchRule(Category category, int priority, boolean caseSensitive,
	        MatchMode matchMode, boolean capitalSensitive, Locale locale) {
		super(category, priority, caseSensitive, matchMode, capitalSensitive, locale);
	}

	public void init(Collection<String> rawAbbrs) {
		List<String> abbrs = new ArrayList<String>();
		for (String rawAbbr : rawAbbrs) {
			StringBuilder sb = new StringBuilder();
			sb.append(rawAbbr.charAt(0));
			for (int n = 1; n < rawAbbr.length(); ++n) {
				if (rawAbbr.charAt(n - 1) != '-')
					sb.append(".");
				sb.append(rawAbbr.charAt(n));
			}
			String extended = sb.toString();
			abbrs.add(rawAbbr);
			abbrs.add(extended);
		}

		super.init(abbrs);
	}
}
