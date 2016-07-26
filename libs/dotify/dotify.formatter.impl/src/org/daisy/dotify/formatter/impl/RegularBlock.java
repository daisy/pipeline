package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.RenderingScenario;
import org.daisy.dotify.formatter.impl.Segment.SegmentType;

public class RegularBlock extends Block {

	public RegularBlock(String blockId, RowDataProperties rdp, RenderingScenario scenario) {
		super(blockId, rdp, scenario);
	}
	
	public void addSegment(Segment s) {
		segments.add(s);
	}
	
	public void addSegment(TextSegment s) {
		if (segments.size() > 0 && segments.peek().getSegmentType() == SegmentType.Text) {
			TextSegment ts = ((TextSegment) segments.peek());
			if (ts.getTextProperties().equals(s.getTextProperties())
			    && ts.getTextAttribute() == null && s.getTextAttribute() == null) {
				// Logger.getLogger(this.getClass().getCanonicalName()).finer("Appending chars to existing text segment.");
				segments.pop();
				segments.push(new TextSegment(ts.getText() + "" + s.getText(), ts.getTextProperties()));
				return;
			}
		}
		segments.push(s);
	}

	@Override
	protected AbstractBlockContentManager newBlockContentManager(BlockContext context) {
		return new BlockContentManager(context.getFlowWidth(), segments, rdp, context.getRefs(),
				DefaultContext.from(context.getContext()).metaVolume(metaVolume).metaPage(metaPage).build(),
				context.getFcontext());
	}

}
