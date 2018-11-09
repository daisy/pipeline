package org.daisy.dotify.formatter.impl.row;

import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

/**
 * Provides immutable information about the segment processor's context.
 * 
 * @author Joel Håkansson
 */
class SegmentProcessorContext {
	private final FormatterCoreContext fcontext;
	private final RowDataProperties rdp;
	private final BlockMargin margins;
	private final int flowWidth;
	private final char spaceChar;
	private final int available;
	
	SegmentProcessorContext(FormatterCoreContext fcontext, RowDataProperties rdp, BlockMargin margins, int flowWidth, int available) {
		this.fcontext = fcontext;
		this.rdp = rdp;
		this.margins = margins;
		this.flowWidth = flowWidth;
		this.spaceChar = fcontext.getSpaceCharacter();
		this.available = available;
	}

	RowDataProperties getRdp() {
		return rdp;
	}

	BlockMargin getMargins() {
		return margins;
	}

	int getFlowWidth() {
		return flowWidth;
	}

	char getSpaceCharacter() {
		return spaceChar;
	}

	int getAvailable() {
		return available;
	}
	
	FormatterCoreContext getFormatterContext() {
		return fcontext;
	}

}
