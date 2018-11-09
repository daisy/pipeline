package org.daisy.dotify.formatter.impl.segment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.MarkerProcessor;
import org.daisy.dotify.api.translator.MarkerProcessorConfigurationException;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

/**
 * Associates a text style with a group of segments.
 */
public class StyledSegmentGroup extends SegmentGroup {
	private final FormatterCoreContext fc;
	private final StyledSegmentGroup parentStyle;
	private final int idx;
	private final String name;
	private MarkerProcessor markerProcessorCache;
	private SegmentGroup processAttributes;
	
	public StyledSegmentGroup(String name, FormatterCoreContext fc) {
		this(name, null, fc);
	}
	
	public StyledSegmentGroup(String name, StyledSegmentGroup parentStyle, FormatterCoreContext fc) {
		super();
		this.fc = fc;
		this.parentStyle = parentStyle;
		if (parentStyle != null)
			idx = parentStyle.add(this);
		else
			idx = -1;
		this.name = name;
	}

	@Override
	int add(TextSegment segment) {
		// remove cached value
		processAttributes = null;
		return super.add(segment);
	}

	@Override
	int add(SegmentGroup group) {
		// remove cached value
		processAttributes = null;
		return super.add(group);
	}

	SegmentGroup processAttributes() {
		if (parentStyle != null) {
			return parentStyle.processAttributes().getGroupAt(idx);
		} else {
			if (processAttributes == null) {
				List<String> text = extractText(segments);
				TextAttribute attributes = makeTextAttribute(name, segments);
				String[] processedText = getMarkerProcessor().processAttributesRetain(attributes, text.toArray(new String[text.size()]));
				processAttributes = updateSegments(segments, Arrays.asList(processedText).iterator());
			}
			return processAttributes;
		}
	}
	
	private MarkerProcessor getMarkerProcessor() {
		if (markerProcessorCache == null) {
			try {
				String locale = fc.getConfiguration().getLocale();
				String mode = fc.getTranslatorMode();
				markerProcessorCache = fc.getMarkerProcessorFactoryMakerService().newMarkerProcessor(locale, mode);
			} catch (MarkerProcessorConfigurationException e) {
				throw new IllegalArgumentException(e);
			}
		}
		return markerProcessorCache;
	}
	
	/**
	 * Extracts the text from all segments into a list. 
	 * @param segments the segments
	 * @return returns a list of strings
	 */
	private static List<String> extractText(List<Object> segments) {
		List<String> l = new ArrayList<String>();
		for (Object o : segments) {
			if (o instanceof TextSegment) {
				TextSegment s = (TextSegment)o;
				l.add(s.getText());
			} else {
				SegmentGroup g = (SegmentGroup)o;
				l.addAll(extractText(g.segments));
			}
		}
		return l;
	}
	
	/**
	 * Creates a text attribute with the specified name and content. If the content
	 * contains styled segments, their styles are also added to the text attribute.
	 * @param name the name of the text attribute
	 * @param segments the children of the text attribute
	 * @return returns a new text attribute
	 */
	private static TextAttribute makeTextAttribute(String name, List<Object> segments) {
		DefaultTextAttribute.Builder b = new DefaultTextAttribute.Builder(name);
		int w = 0;
		for (Object o : segments) {
			if (o instanceof TextSegment) {
				TextSegment s = (TextSegment)o;
				TextAttribute a = new DefaultTextAttribute.Builder().build(s.getText().length());
				b.add(a);
				w += a.getWidth();
			} else if (o instanceof StyledSegmentGroup) {
				StyledSegmentGroup s = (StyledSegmentGroup)o;
				TextAttribute a = makeTextAttribute(s.name, s.segments);
				b.add(a);
				w += a.getWidth();
			} else {
				SegmentGroup g = (SegmentGroup)o;
				TextAttribute a = makeTextAttribute(null, g.segments);
				b.add(a);
				w += a.getWidth();
			}
		}
		return b.build(w);
	}
	
	/**
	 * Updates text segments with the processed text and replaces any {@link StyledSegmentGroup} with
	 * unstyled {@link SegmentGroup}s. 
	 * @param segments the segments
	 * @param processedText the processed text pieces
	 * @return returns a new SegmentGroup containing the updated segments
	 */
	private static SegmentGroup updateSegments(List<Object> segments, Iterator<String> processedText) {
		SegmentGroup processedGroup = new SegmentGroup();
		for (Object o : segments) {
			if (o instanceof TextSegment) {
				processedGroup.add(new TextSegment(processedText.next(), ((TextSegment)o).getTextProperties()));
			} else {
				processedGroup.add(updateSegments(((StyledSegmentGroup)o).segments, processedText));
			}
		}
		return processedGroup;
	}

	public String getName() {
		return name;
	}

	StyledSegmentGroup getParentStyle() {
		return parentStyle;
	}
}