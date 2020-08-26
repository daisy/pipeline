package org.daisy.pipeline.nlp.impl.matchrules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;
import org.daisy.pipeline.nlp.impl.MatchRule;

/**
 * MatchRule based on the Java core's Regexp engine. See dk.brics.automaton for
 * a future faster implementation.
 */
public class RegexMatchRule extends MatchRule {

	// warning: the Java Regexp engine always break the search if X matches in
	// "(X|Y)"
	// even if Y is longer than X.

	private Pattern mPattern;

	public RegexMatchRule(Category category, int priority, boolean caseSensitive,
	        MatchMode matchMode) {
		super(category, priority, caseSensitive, matchMode);
	}

	public void init(String regexp) {
		switch (mMatchMode) {
		case FULL_MATCH:
			regexp += "$";
			break;
		default:
			break;
		}

		int flags = Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.DOTALL;
		// java7: flags |= Pattern.UNICODE_CHARACTER_CLASS;

		if (!mCaseSensitive) {
			flags |= Pattern.CASE_INSENSITIVE;
		}

		mPattern = Pattern.compile(regexp, flags);
	}

	@Override
	protected String match(String input) {
		Matcher m = mPattern.matcher(input);

		if (m.lookingAt()) {
			return m.group(0);
		}
		return null;
	}

	@Override
	public boolean threadsafe() {
		return true;
	}
}
