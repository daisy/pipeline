package cz.vutbr.web.csskit;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.RuleMargin;

/**
 * Implementation of RuleMargin
 * 
 * @author Bert Frees, 2012-2015
 */
public class RuleMarginImpl extends AbstractRuleBlock<Declaration> implements RuleMargin {
	
	private enum MarginArea {
		
		TOPLEFTCORNER("top-left-corner"),
		TOPLEFT("top-left"),
		TOPCENTER("top-center"),
		TOPRIGHT("top-right"),
		TOPRIGHTCORNER("top-right-corner"),
		BOTTOMLEFTCORNER("bottom-left-corner"),
		BOTTOMLEFT("bottom-left"),
		BOTTOMCENTER("bottom-center"),
		BOTTOMRIGHT("bottom-right"),
		BOTTOMRIGHTCORNER("bottom-right-corner"),
		LEFTTOP("left-top"),
		LEFTMIDDLE("left-middle"),
		LEFTBOTTOM("left-bottom"),
		RIGHTTOP("right-top"),
		RIGHTMIDDLE("right-middle"),
		RIGHTBOTTOM("right-bottom");
		
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

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + getMarginArea().hashCode();
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
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
