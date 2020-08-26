package org.daisy.pipeline.nlp.impl.matchrules;

import java.math.BigInteger;

import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;
import org.daisy.pipeline.nlp.impl.MatchRule;

/**
 * Match ranges like "x-y" where x,y are integers such that x < y
 */
public class NumberRangeMatchRule extends MatchRule {

	public NumberRangeMatchRule(Category category, int priority, MatchMode matchMode) {
		super(category, priority, false, matchMode);
	}

	@Override
	protected String match(String input) {
		if (input.charAt(0) <= '0' || input.charAt(0) > '9')
			return null;
		int k = 1;
		for (; k < input.length() && input.charAt(k) >= '0' && input.charAt(k) <= '9'; ++k);
		if (k == input.length() || input.charAt(k) != '-')
			return null; //input is a regular number, not a range
		int prevk = k++;
		if (k >= input.length() || input.charAt(k) <= '0' || input.charAt(k) > '9')
			return null;
		for (++k; k < input.length() && input.charAt(k) >= '0' && input.charAt(k) <= '9'; ++k);
		if (k == prevk + 2
		        || !lowerThan(input.substring(0, prevk), input.substring(prevk + 1, k))
		        || (mMatchMode == MatchMode.FULL_MATCH && k != input.length()))
			return null;

		return input.substring(0, k);
	}
	
	private static boolean lowerThan(String x, String  y){
		return (new BigInteger(x).compareTo(new BigInteger(y)) == -1);
	}

	@Override
	public boolean threadsafe() {
		return true;
	}
}
