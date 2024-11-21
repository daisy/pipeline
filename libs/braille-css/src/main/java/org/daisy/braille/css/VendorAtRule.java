package org.daisy.braille.css;

import java.util.List;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class VendorAtRule<R extends Rule<?>> extends AbstractRuleBlock<R> implements PrettyOutput  {
	
	private final String name;
	
	public VendorAtRule(String name, List<R> content) {
		super();
		for (Rule<?> r : content)
			if (!(r instanceof Declaration || r instanceof VendorAtRule))
				throw new IllegalArgumentException("Rule must be either a Declaration or an at-rule");
		replaceAll(content);
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb.append("@").append(name);
		sb.append(" ");
		sb.append(OutputUtil.RULE_OPENING);
		sb = OutputUtil.appendList(sb, (List<PrettyOutput>)(List)list, OutputUtil.EMPTY_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
	
	@Override
	public String toString() {
		return this.toString(0);
	}
}
