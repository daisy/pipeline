package org.daisy.dotify.formatter.impl.segment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides a text style segment. This segment is a bit different than other segments
 * in that it can contain other segments.
 * @author Joel Håkansson
 *
 */
public class Style implements Segment {
	private final List<Segment> segments;
	private final String style;
	
	/**
	 * Creates a new style segment with the specified name.
	 * @param style the style name
	 */
	public Style(String style) {
		this.segments = new ArrayList<>();
		this.style = style;
	}
	
	/**
	 * Gets the name of this style.
	 * @return the name of this style
	 */
	public String getName() {
		return style;
	}
	
	/**
	 * Adds a segment to this style.
	 * @param segment the segment to add
	 * @return the index of segment inside the group
	 */
	public int add(Segment segment) {
		segments.add(segment);
		return segments.size()-1;
	}

	/**
	 * Gets the segments in the scope of this style.
	 * @return a list of segments
	 */
	public List<Segment> getSegments() {
		return Collections.unmodifiableList(segments);
	}
	
	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Style;
	}

	@Override
	public String peek() {
		// Style doesn't support this operation, since style segments are not meant to be sent to the translator.
		// Styles are removed in SegmentProcessor.
		throw new UnsupportedOperationException();
	}

	@Override
	public String resolve() {
		// Style doesn't support this operation, since style segments are not meant to be sent to the translator.
		// Styles are removed in SegmentProcessor.
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean isStatic() {
		// Style doesn't support this operation, since style segments are not meant to be sent to the translator.
		// Styles are removed in SegmentProcessor.
		throw new UnsupportedOperationException();
	}

}
