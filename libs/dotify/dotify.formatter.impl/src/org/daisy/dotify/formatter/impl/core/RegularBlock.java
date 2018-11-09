package org.daisy.dotify.formatter.impl.core;

import java.util.Stack;

import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.formatter.impl.row.AbstractBlockContentManager;
import org.daisy.dotify.formatter.impl.row.BlockContentManager;
import org.daisy.dotify.formatter.impl.row.RowDataProperties;
import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.segment.ConnectedTextSegment;
import org.daisy.dotify.formatter.impl.segment.Segment;
import org.daisy.dotify.formatter.impl.segment.Segment.SegmentType;
import org.daisy.dotify.formatter.impl.segment.TextSegment;

class RegularBlock extends Block {
	private final Stack<Segment> segments;

	RegularBlock(String blockId, RowDataProperties rdp, RenderingScenario scenario) {
		super(blockId, rdp, scenario);
		this.segments = new Stack<>();
	}
	
	RegularBlock(RegularBlock template) {
		super(template);
		this.segments = template.segments;
	}
	
	public RegularBlock copy() {
		return new RegularBlock(this);
	}

	@Override
	public void addSegment(Segment s) {
		super.addSegment(s);
		segments.add(s);
	}
	
	@Override
	public void addSegment(TextSegment s) {
		super.addSegment(s);
		addSegment(s, segments);
	}
	
	private static void addSegment(TextSegment s, Stack<Segment> segments) {
		if (segments.size() > 0 && segments.peek().getSegmentType() == SegmentType.Text) {
			TextSegment ts = ((TextSegment) segments.peek());
			if (ts.getTextProperties().equals(s.getTextProperties())
			    && ts.getTextAttribute() == null && s.getTextAttribute() == null) {
				// Appending chars to existing text segment
				segments.pop();
				segments.push(new TextSegment(ts.getText() + "" + s.getText(), ts.getTextProperties()));
				return;
			}
		}
		segments.push(s);
	}
	
	@Override
	boolean isEmpty() {
		return segments.isEmpty();
	}

	@Override
	protected AbstractBlockContentManager newBlockContentManager(BlockContext context) {
		return new BlockContentManager(getIdentifier(), context.getFlowWidth(), processAttributes(segments), rdp, context.getRefs(),
				DefaultContext.from(context).metaVolume(metaVolume).metaPage(metaPage).build(),
				context.getFcontext());
	}
	
	/**
	 * Process non-null text attributes of text segments. "Connected" segments are processed
	 * together.
	 */
	private static Stack<Segment> processAttributes(Stack<Segment> segments) {
		Stack<Segment> processedSegments = new Stack<Segment>();
		for (Segment s : segments) {
			if (s instanceof ConnectedTextSegment) {
				s = ((ConnectedTextSegment)s).processAttributes();
			}
			if (s instanceof TextSegment) {
				// cast to TextSegment in order to enable merging
				addSegment((TextSegment)s, processedSegments);
			} else {
				processedSegments.push(s);
			}
		}
		return processedSegments;
	}

}
