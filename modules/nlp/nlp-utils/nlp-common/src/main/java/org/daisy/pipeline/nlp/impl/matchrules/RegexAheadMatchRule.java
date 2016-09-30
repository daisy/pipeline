package org.daisy.pipeline.nlp.impl.matchrules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;
import org.daisy.pipeline.nlp.impl.MatchRule;

/**
 * Match text with one regex but return the text matched by another regex.
 * Better use the Negative Lookahead syntax when it is possible.
 */
public class RegexAheadMatchRule extends MatchRule {

	private Pattern mPattern;
	private Pattern mPatternForward;

	public RegexAheadMatchRule(Category category, int priority, boolean caseSensitive,
	        MatchMode matchMode) {
		super(category, priority, caseSensitive, matchMode);
	}

	public void init(String regexp, String regexpForward) {
		int flags = Pattern.UNICODE_CASE | Pattern.MULTILINE;

		// java7: flags |= Pattern.UNICODE_CHARACTER_CLASS;

		if (!mCaseSensitive) {
			flags |= Pattern.CASE_INSENSITIVE;
		}

		mPattern = Pattern.compile(regexp, flags);
		mPatternForward = Pattern.compile(regexpForward, flags);
	}

	@Override
	protected String match(String input) {
		Matcher m = mPattern.matcher(input);
		switch (mMatchMode) {
		case FULL_MATCH:
			if (m.matches()) {
				return m.group(0);
			}
			break;
		case PREFIX_MATCH:
			if (m.lookingAt()) {
				Matcher m2 = mPatternForward.matcher(input);
				if (m2.lookingAt())
					return m2.group(0);
				else {
					System.err.println("** critical error **: " + mPatternForward.pattern()
					        + " not included in " + mPattern.pattern());
				}
			}
			break;
		}

		return null;
	}

	@Override
	public boolean threadsafe() {
		return true;
	}
}
