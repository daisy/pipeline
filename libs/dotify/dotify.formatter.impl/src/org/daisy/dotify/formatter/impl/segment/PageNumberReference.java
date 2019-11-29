package org.daisy.dotify.formatter.impl.segment;

import java.util.function.Function;

import org.daisy.dotify.api.formatter.NumeralStyle;


/**
 * Provides a page number reference event object.
 * 
 * @author Joel Håkansson
 */
public class PageNumberReference implements Segment {
	private final String refid;
	private final NumeralStyle style;
	private final boolean markCapitalLetters;
	private Function<PageNumberReference,String> v = (x)->"";
	private String resolved;
	
	public PageNumberReference(String refid, NumeralStyle style, boolean markCapitalLetters) {
		this(refid, style, null, markCapitalLetters);
	}
	
	public PageNumberReference(String refid, NumeralStyle style, MarkerValue marker, boolean markCapitalLetters) {
		this.refid = refid;
		this.style = style;
		this.markCapitalLetters = markCapitalLetters;
	}
	
	/**
	 * Gets the identifier to the reference location.
	 * @return returns the reference identifier
	 */
	public String getRefId() {
		return refid;
	}
	
	/**
	 * Gets the numeral style for this page number reference
	 * @return returns the numeral style
	 */
	public NumeralStyle getNumeralStyle() {
		return style;
	}
	
	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Reference;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((refid == null) ? 0 : refid.hashCode());
		result = prime * result + ((style == null) ? 0 : style.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PageNumberReference other = (PageNumberReference) obj;
		if (refid == null) {
			if (other.refid != null) {
				return false;
			}
		} else if (!refid.equals(other.refid)) {
			return false;
		}
		if (style != other.style) {
			return false;
		}
		return true;
	}

	@Override
	public String peek() {
		return resolved==null?"00":resolved;
	}

	@Override
	public String resolve() {
		if (resolved==null) {
			resolved = v.apply(this);
			if (resolved == null) {
				resolved = "";
			}
		}
		return resolved;
	}
	
	public void setResolver(Function<PageNumberReference,String> v) {
		this.resolved = null;
		this.v = v;
	}
	
	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public boolean shouldMarkCapitalLetters() {
		return markCapitalLetters;
	}


}
