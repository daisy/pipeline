package org.daisy.pipeline.nlp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.daisy.pipeline.nlp.impl.MatchRule;

public abstract class RuleBasedTextCategorizer extends TextCategorizer {

	@Override
	public CategorizedWord categorize(String fullcase, String lowercase) {
		CategorizedWord res = null;
		int k;
		// find the first match in the priority-sorted list
		for (k = 0; res == null && k < mRules.size(); ++k) {
			// k < mRules.size() is not tested because raising an exception
			// is the best we can do here
			res = mRules.get(k).match(fullcase, lowercase);
		}

		if (res == null) {
			res = new CategorizedWord();
			res.category = Category.UNKNOWN;
			res.word = fullcase.substring(0, 1);
			return res;
		}

		// select the longest match with the same priority
		int priority = mRules.get(k - 1).getPriority();
		for (; k < mRules.size() && mRules.get(k).getPriority() == priority; ++k) {
			CategorizedWord m = mRules.get(k).match(fullcase, lowercase);
			if (m != null && m.word.length() > res.word.length()) {
				res = m;
			}
		}

		return res;
	}

	@Override
	public void resetContext() {
		// this categorizer does not need any context
	}

	@Override
	public boolean threadsafe() {
		return mThreadsafe;
	}

	// ///// internal helpers //////
	protected ArrayList<MatchRule> mRules = new ArrayList<MatchRule>();

	protected void addRule(MatchRule rule) {
		mRules.add(rule);
	}

	/**
	 * Should only be called once.
	 */
	@Override
	public void compile() {
		Collections.sort(mRules, new Comparator<MatchRule>() {
			@Override
			public int compare(MatchRule r1, MatchRule r2) {
				if (r1.getPriority() == r2.getPriority())
					return (r1 == r2 ? 0 : -1);
				return (r1.getPriority() > r2.getPriority() ? -1 : 1);
			}
		});
		for (MatchRule rule : mRules) {
			if (!rule.threadsafe()) {
				return;
			}
		}
		mThreadsafe = true;
	}

	private boolean mThreadsafe = false;
}
