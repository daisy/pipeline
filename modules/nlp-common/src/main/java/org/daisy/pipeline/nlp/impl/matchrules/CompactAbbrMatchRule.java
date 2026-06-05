package org.daisy.pipeline.nlp.impl.matchrules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;

/**
 * Match compact abbreviations such as "etc.", but "e.t.c" does not match.
 */
public class CompactAbbrMatchRule extends WordListMatchRule {
	public CompactAbbrMatchRule(Category category, int priority, boolean caseSensitive,
	        MatchMode matchMode, boolean capitalSensitive, Locale locale) {
		super(category, priority, caseSensitive, matchMode, capitalSensitive, locale);
	}

	public void init(Collection<String> rawAbbrs) {
		List<String> abbrs = new ArrayList<String>();
		for (String prefix : rawAbbrs) {
			abbrs.add(prefix + ".");
		}

		super.init(abbrs);
	}
}
