package cz.vutbr.web.csskit;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.FeatureCondition;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleSupports;

public class RuleSupportsImpl extends AbstractRuleBlock<RuleBlock<?>> implements RuleSupports {

	private final FeatureCondition condition;

	RuleSupportsImpl(FeatureCondition condition) {
		this.condition = condition;
	}

	@Override
	public FeatureCondition getCondition() {
		return condition;
	}

	@Override
	public String toString() {
		return this.toString(0);
	}

	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb = OutputUtil.appendTimes(sb, OutputUtil.DEPTH_DELIM, depth);
		sb.append(OutputUtil.SUPPORTS_KEYWORD);
		sb.append(condition);
		sb = OutputUtil.appendTimes(sb, OutputUtil.DEPTH_DELIM, depth);
		sb.append(OutputUtil.RULE_OPENING);
		List<PrettyOutput> prettyPrintableList = new ArrayList<>();
		for (RuleBlock<?> r : list)
			if (r instanceof PrettyOutput)
				prettyPrintableList.add((PrettyOutput)r);
			else
				throw new IllegalStateException("Rule does not implement PrettyOutput: " + r);
		sb = OutputUtil.appendList(sb, prettyPrintableList, OutputUtil.RULE_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
}
