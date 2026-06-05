package org.daisy.pipeline.nlp.impl.matchrules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;
import org.daisy.pipeline.nlp.impl.FullMatchStringFinder;
import org.daisy.pipeline.nlp.impl.IStringFinder;
import org.daisy.pipeline.nlp.impl.MatchRule;
import org.daisy.pipeline.nlp.impl.PrefixMatchStringFinder;

/**
 * Match the input strings with a list of strings.
 */
public class WordListMatchRule extends MatchRule {
	private boolean mCapitalSensitive;
	private IStringFinder mStringFinder;
	private Locale mLocale;

	/**
	 * @param locale can be null if caseSensitive = capitalSensitive = true
	 */
	public WordListMatchRule(Category category, int priority, boolean caseSensitive,
	        MatchMode matchMode, boolean capitalSensitive, Locale locale) {
		super(category, priority, caseSensitive, matchMode);
		mLocale = locale;
		mCapitalSensitive = capitalSensitive;
		switch (matchMode) {
		case FULL_MATCH:
			mStringFinder = new FullMatchStringFinder();
		default:
			mStringFinder = new PrefixMatchStringFinder();
		}
	}

	public void init(Collection<String> prefixes) {
		Collection<String> tocompile;
		if (mCaseSensitive) {
			if (!mCapitalSensitive) {
				tocompile = new ArrayList<String>();
				tocompile.addAll(prefixes);
				for (String prefix : prefixes) {
					String lw = prefix.toLowerCase(mLocale);
					tocompile.add(lw.substring(0, 1) + prefix.substring(1));
				}
			} else
				tocompile = prefixes;
		} else {
			String[] lowerCasePrefixes = new String[prefixes.size()];
			int k = 0;
			for (String prefix : prefixes) {
				lowerCasePrefixes[k++] = prefix.toLowerCase(mLocale);
			}
			tocompile = Arrays.asList(lowerCasePrefixes);
		}
		mStringFinder.compile(tocompile);
	}

	@Override
	protected String match(String input) {
		return mStringFinder.find(input);
	}

	@Override
	public boolean threadsafe() {
		return mStringFinder.threadsafe();
	}

}
