package org.daisy.braille.css;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleMargin;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class RuleMarginImpl extends AbstractRuleBlock<Declaration> implements RuleMargin {
	
	private enum MarginArea {
		
		TOPLEFT("top-left"),
		TOPCENTER("top-center"),
		TOPRIGHT("top-right"),
		BOTTOMLEFT("bottom-left"),
		BOTTOMCENTER("bottom-center"),
		BOTTOMRIGHT("bottom-right"),
		LEFT("left"),
		RIGHT("right"),
		FOOTNOTES("footnotes");
		
		public final String value;
		
		private MarginArea(String value) {
			this.value = value;
		}
	}
	
	private final MarginArea marginArea;
	
	protected RuleMarginImpl(String area) {
		for (MarginArea a : MarginArea.values()) {
			if (a.value.equals(area)) {
				marginArea = a;
				return; }}
		throw new IllegalArgumentException("Illegal value for margin area: " + area);
	}
	
	public String getMarginArea() {
		return marginArea.value;
	}
	
	@Override
	public String toString() {
		return this.toString(0);
	}
	
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb = OutputUtil.appendTimes(sb, OutputUtil.DEPTH_DELIM, depth);
		sb.append(OutputUtil.MARGIN_AREA_OPENING).append(getMarginArea());
		sb.append(OutputUtil.RULE_OPENING);
		sb = OutputUtil.appendList(sb, list, OutputUtil.RULE_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getMarginArea().hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof RuleMarginImpl))
			return false;
		RuleMarginImpl other = (RuleMarginImpl) obj;
		if (!getMarginArea().equals(other.getMarginArea()))
			return false;
		return true;
	}
}
