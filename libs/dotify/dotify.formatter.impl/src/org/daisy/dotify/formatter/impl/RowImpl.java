package org.daisy.dotify.formatter.impl;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.formatter.FormattingTypes.Alignment;
import org.daisy.dotify.api.formatter.Marker;
import org.daisy.dotify.api.writer.Row;



/**
 * Row represents a single row of text
 * @author Joel Håkansson
 */
class RowImpl implements Row {
	private String chars;
	private List<Marker> markers;
	private List<String> anchors;
	private MarginProperties leftMargin;
	private MarginProperties rightMargin;
	private Alignment alignment;
	private Float rowSpacing;
	private boolean adjustedForMargin = false;
	private boolean allowsBreakAfter = true;
	private int leaderSpace;
	
	/**
	 * Create a new Row
	 * @param chars the characters on this row
	 */
	public RowImpl(String chars) {
		this(chars, new MarginProperties(), new MarginProperties());
	}
	public RowImpl(String chars, MarginProperties leftMargin, MarginProperties rightMargin) {
		this.chars = chars;
		this.markers = new ArrayList<>();
		this.anchors = new ArrayList<>();
		this.leftMargin = leftMargin;
		this.rightMargin = rightMargin;
		this.alignment = Alignment.LEFT;
		this.rowSpacing = null;
		this.leaderSpace = 0;
	}
	
	/**
	 * Creates a deep copy of the supplied instance
	 * @param template the instance to copy
	 */
	RowImpl(RowImpl template) {
		this.chars = template.chars;
		this.leftMargin = template.leftMargin;
		this.rightMargin = template.rightMargin;
		this.markers = new ArrayList<>(template.markers);
		this.anchors = new ArrayList<>(template.anchors);
		this.leftMargin = template.leftMargin;
		this.rightMargin = template.rightMargin;
		this.alignment = template.alignment;
		this.rowSpacing = template.rowSpacing;
		this.adjustedForMargin = template.adjustedForMargin;
		this.leaderSpace = template.leaderSpace;
	}

	@Deprecated
	static RowImpl withRow(RowImpl r) {
		RowImpl ret = new RowImpl(r.chars);
		ret.markers = r.markers;
		ret.anchors = r.anchors;
		ret.leftMargin = r.leftMargin;
		ret.rightMargin = r.rightMargin;
		ret.alignment = r.alignment;
		ret.rowSpacing = r.rowSpacing;
		ret.adjustedForMargin = r.adjustedForMargin;
		ret.leaderSpace = r.leaderSpace;
		return ret;
	}

	/**
	 * Create a new empty Row
	 */
	public RowImpl() {
		this("");
	}

	/**
	 * Get the characters on this row
	 * @return returns the characters on the row
	 */
	@Override
	public String getChars() {
		return chars;
	}

	public void setChars(String chars) {
		this.chars = chars;
	}
	
	public void setLeaderSpace(int value) {
		this.leaderSpace = value;
	}
	
	public int getLeaderSpace() {
		return leaderSpace;
	}

	public int getWidth() {
		return chars.length()+leftMargin.getContent().length()+rightMargin.getContent().length();
	}
	
	/**
	 * Add a marker to the Row
	 * @param marker
	 */
	public void addMarker(Marker marker) {
		markers.add(marker);
	}

	/**
	 * Add an anchor to the Row
	 * @param ref
	 */
	public void addAnchor(String ref) {
		anchors.add(ref);
	}
	
	public void addAnchors(List<String> refs) {
		anchors.addAll(refs);
	}
	public void addAnchors(int index, List<String> refs) {
		anchors.addAll(index, refs);
	}

	/**
	 * Add a collection of markers to the Row
	 * @param list
	 */
	public void addMarkers(List<Marker> list) {
		markers.addAll(list);
	}
	
	/**
	 * Add a collection of markers to the Row
	 * @param list
	 */
	public void addMarkers(int index, List<Marker> list) {
		markers.addAll(0, list);
	}

	/**
	 * Get all markers on this Row
	 * @return returns the markers
	 */
	public List<Marker> getMarkers() {
		return markers;
	}
	
	public boolean hasMarkerWithName(String name) {
		for (Marker m : markers) {
			if (m.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Get all anchors on this Row
	 * @return returns an ArrayList of anchors
	 */
	public List<String> getAnchors() {
		return anchors;
	}

	/**
	 * Set the left margin
	 * @param value the left margin, in characters
	 */
	public void setLeftMargin(MarginProperties value) {
		leftMargin = value;
	}

	/**
	 * Get the left margin value for the Row, in characters
	 * @return returns the left margin
	 */
	public MarginProperties getLeftMargin() {
		return leftMargin;
	}

	public MarginProperties getRightMargin() {
		return rightMargin;
	}

	public void setRightMargin(MarginProperties rightMargin) {
		this.rightMargin = rightMargin;
	}

	/**
	 * Gets the alignment value for the row
	 * @return returns the alignment
	 */
	public Alignment getAlignment() {
		return alignment;
	}

	/**
	 * Sets the alignment value for the row
	 * @param alignment the new value
	 */
	public void setAlignment(Alignment alignment) {
		this.alignment = alignment;
	}

	@Override
	public Float getRowSpacing() {
		return rowSpacing;
	}
	
	public void setRowSpacing(Float value) {
		this.rowSpacing = value;
	}
	
	
	boolean shouldAdjustForMargin() {
		return adjustedForMargin;
	}
	void setAdjustedForMargin(boolean value) {
		this.adjustedForMargin = value;
	}
	
	boolean allowsBreakAfter() {
		return allowsBreakAfter;
	}
	
	void setAllowsBreakAfter(boolean value) {
		this.allowsBreakAfter = value;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (adjustedForMargin ? 1231 : 1237);
		result = prime * result + ((alignment == null) ? 0 : alignment.hashCode());
		result = prime * result + (allowsBreakAfter ? 1231 : 1237);
		result = prime * result + ((anchors == null) ? 0 : anchors.hashCode());
		result = prime * result + ((chars == null) ? 0 : chars.hashCode());
		result = prime * result + leaderSpace;
		result = prime * result + ((leftMargin == null) ? 0 : leftMargin.hashCode());
		result = prime * result + ((markers == null) ? 0 : markers.hashCode());
		result = prime * result + ((rightMargin == null) ? 0 : rightMargin.hashCode());
		result = prime * result + ((rowSpacing == null) ? 0 : rowSpacing.hashCode());
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
		RowImpl other = (RowImpl) obj;
		if (adjustedForMargin != other.adjustedForMargin) {
			return false;
		}
		if (alignment != other.alignment) {
			return false;
		}
		if (allowsBreakAfter != other.allowsBreakAfter) {
			return false;
		}
		if (anchors == null) {
			if (other.anchors != null) {
				return false;
			}
		} else if (!anchors.equals(other.anchors)) {
			return false;
		}
		if (chars == null) {
			if (other.chars != null) {
				return false;
			}
		} else if (!chars.equals(other.chars)) {
			return false;
		}
		if (leaderSpace != other.leaderSpace) {
			return false;
		}
		if (leftMargin == null) {
			if (other.leftMargin != null) {
				return false;
			}
		} else if (!leftMargin.equals(other.leftMargin)) {
			return false;
		}
		if (markers == null) {
			if (other.markers != null) {
				return false;
			}
		} else if (!markers.equals(other.markers)) {
			return false;
		}
		if (rightMargin == null) {
			if (other.rightMargin != null) {
				return false;
			}
		} else if (!rightMargin.equals(other.rightMargin)) {
			return false;
		}
		if (rowSpacing == null) {
			if (other.rowSpacing != null) {
				return false;
			}
		} else if (!rowSpacing.equals(other.rowSpacing)) {
			return false;
		}
		return true;
	}

}