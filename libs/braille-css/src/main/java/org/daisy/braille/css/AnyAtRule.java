package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class AnyAtRule extends AbstractRuleBlock<Rule<?>> implements PrettyOutput  {
	
	private final String name;
	
	public AnyAtRule(String name) {
		super();
		replaceAll(new ArrayList<Rule<?>>());
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public boolean add(Rule<?> element) {
		if (element instanceof Declaration || element instanceof AnyAtRule)
			return super.add(element);
		else
			throw new IllegalArgumentException("Rule must be either a Declaration or an at rule");
	}
	
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb.append("@").append(name);
		sb.append(" ");
		sb.append(OutputUtil.RULE_OPENING);
		List<PrettyOutput> rules = (List)list;
		sb = OutputUtil.appendList(sb, rules, OutputUtil.EMPTY_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return this.toString(0);
	}
}
