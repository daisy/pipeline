package org.daisy.pipeline.nlp.impl;

import org.daisy.pipeline.nlp.TextCategorizer.CategorizedWord;
import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;

/**
 * Mapping between a string matching method (e.g. some regex) and a word
 * category, associated to a priority so as to rank the match rules.
 */
public abstract class MatchRule {
	private final int mPriority;
	private final Category mCategory;
	protected final MatchMode mMatchMode;
	protected final boolean mCaseSensitive;

	/**
	 * 
	 * @param category the classification applied to the words that match the
	 *            rule
	 * @param priority allows us to select a rule when several rules match the
	 *            word. High value means it will be chosen over lower ones. When
	 *            two priorities are equal, the one that returns the longest
	 *            match is chosen.
	 */
	public MatchRule(Category category, int priority, boolean caseSensitive,
	        MatchMode matchMode) {
		mCategory = category;
		mPriority = priority;
		mCaseSensitive = caseSensitive;
		mMatchMode = matchMode;
	}

	public int getPriority() {
		return mPriority;
	}

	public CategorizedWord match(String input, String lowerCaseInput) {
		String res;
		if (mCaseSensitive)
			res = match(input);
		else
			res = match(lowerCaseInput);
		if (res == null)
			return null;
		CategorizedWord cw = new CategorizedWord();
		cw.category = mCategory;
		cw.word = res;
		return cw;
	}

	protected abstract String match(String input);

	/**
	 * Can return false before returning true, but not the other way around.
	 */
	public abstract boolean threadsafe();
}
