package org.daisy.braille.css;

import java.util.List;
import java.util.ArrayList;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.css.Rule;
import cz.vutbr.web.css.RulePage;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class RuleVolumeArea extends AbstractRuleBlock<Rule<?>> implements PrettyOutput {
	
	public enum VolumeArea {
		
		BEGIN("begin"),
		END("end");
		
		public final String value;
		
		private VolumeArea(String value) {
			this.value = value;
		}
	}
	
	public RuleVolumeArea(String area) {
		super();
		for (VolumeArea a : VolumeArea.values()) {
			if (a.value.equals(area)) {
				volumeArea = a;
				break; }}
		if (volumeArea == null)
			throw new IllegalArgumentException("Illegal value for volume area: " + area);
		replaceAll(new ArrayList<Rule<?>>());
	}
	
	private VolumeArea volumeArea;
	
	public VolumeArea getVolumeArea() {
		return volumeArea;
	}

	@Override
	public boolean add(Rule<?> element) {
		if (element instanceof Declaration || element instanceof RulePage)
			return super.add(element);
		else
			throw new IllegalArgumentException("Element must be either a Declaration or a RulePage");
	}
	
	@Override
	public String toString() {
		return this.toString(0);
	}
	
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb = OutputUtil.appendTimes(sb, OutputUtil.DEPTH_DELIM, depth);
		sb.append('@').append(volumeArea.value);
		sb.append(OutputUtil.RULE_OPENING);
		List<PrettyOutput> rules = (List)list;
		sb = OutputUtil.appendList(sb, rules, OutputUtil.RULE_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
}
