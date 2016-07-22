package org.daisy.braille.css;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class RuleVolumeArea extends AbstractRuleBlock<Declaration> implements PrettyOutput {
	
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
	}
	
	private VolumeArea volumeArea;
	
	public VolumeArea getVolumeArea() {
		return volumeArea;
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
		sb = OutputUtil.appendList(sb, list, OutputUtil.RULE_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
}
