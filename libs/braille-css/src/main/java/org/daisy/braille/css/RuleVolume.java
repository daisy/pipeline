package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class RuleVolume extends AbstractRuleBlock<Rule<?>> implements PrettyOutput {
	
	private static final Set<String> validPseudoIdents = new HashSet<String>(Arrays.asList("first", "last", "only"));
	private static final Set<String> validPseudoFuncNames = new HashSet<String>(Arrays.asList("nth", "nth-last"));
	
	protected String pseudo = null;
	
	public RuleVolume(String pseudo, String pseudoFuncArg) {
		this(pseudo, pseudoFuncArg, true);
	}
	
	RuleVolume(String pseudo, String pseudoFuncArg, boolean allowOnly) {
		super();
		replaceAll(new ArrayList<Rule<?>>());
		if (pseudo != null) {
			pseudo = pseudo.toLowerCase();
			if (validPseudoIdents.contains(pseudo) && (allowOnly || !"only".equals(pseudo)))
				this.pseudo = pseudo;
			else if (validPseudoFuncNames.contains(pseudo) && pseudoFuncArg != null)
				this.pseudo = pseudo + "(" + pseudoFuncArg + ")";
			else
				throw new IllegalArgumentException(
					"@volume"
					+ (pseudo != null
					     ? (":" + pseudo + (pseudoFuncArg != null
					                          ? ("(" + pseudoFuncArg + ")")
					                          : ""))
					     : "")
					+ " not allowed");
		}
	}
	
	public String getPseudo() {
		return pseudo;
	}
	
	@Override
	public boolean add(Rule<?> element) {
		if (element instanceof Declaration || element instanceof RuleVolumeArea)
			return super.add(element);
		else
			throw new IllegalArgumentException("Rule must be either a Declaration or a RuleVolumeArea");
	}
	
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb.append("@volume");
		if (pseudo != null)
			sb.append(":").append(pseudo);
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
