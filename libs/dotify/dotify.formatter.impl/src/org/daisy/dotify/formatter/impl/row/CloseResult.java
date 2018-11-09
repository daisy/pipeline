package org.daisy.dotify.formatter.impl.row;

import java.util.Optional;

import org.daisy.dotify.common.text.StringTools;

class CloseResult implements CurrentResult {
	private final SegmentProcessorContext spc;
	private Optional<CurrentResult> cr;
	private boolean doFlush;
	private boolean doUnderline;
	
	CloseResult(SegmentProcessorContext spc, Optional<CurrentResult> cr) {
		this.spc = spc;
		this.cr = cr;
		this.doFlush = true;
		this.doUnderline = spc.getRdp().getUnderlineStyle()!=null;
	}
	
	private CloseResult(CloseResult template) {
		this.spc = template.spc;
		if (template.cr.isPresent()) {
			this.cr = Optional.of(template.cr.get().copy());
		} else {
			this.cr = Optional.empty();
		}
		this.doFlush = template.doFlush;
		this.doUnderline = template.doUnderline;
	}

	@Override
	public boolean hasNext(SegmentProcessing spi) {
		return cr.isPresent() && cr.get().hasNext(spi) || doFlush || (!spi.isEmpty() && doUnderline);
	}

	@Override
	public Optional<RowImpl> process(SegmentProcessing spi, boolean wholeWordsOnly) {
		if (cr.isPresent() && cr.get().hasNext(spi)) {
			return cr.get().process(spi, wholeWordsOnly);
		} else if (doFlush) {
			doFlush = false;
			if (spi.hasCurrentRow()) {
				return Optional.of(spi.flushCurrentRow());
			}
		} else if (!spi.isEmpty() && doUnderline) {
			doUnderline = false;
			if (spi.getUnusedLeft() < spc.getMargins().getLeftMargin().getContent().length() || spi.getUnusedRight() < spc.getMargins().getRightMargin().getContent().length()) {
				throw new RuntimeException("coding error");
			}
			return Optional.of(new RowImpl.Builder(StringTools.fill(spc.getSpaceCharacter(), spi.getUnusedLeft() - spc.getMargins().getLeftMargin().getContent().length())
						+ StringTools.fill(spc.getRdp().getUnderlineStyle(), spc.getFlowWidth() - spi.getUnusedLeft() - spi.getUnusedRight()))
						.leftMargin(spc.getMargins().getLeftMargin())
						.rightMargin(spc.getMargins().getRightMargin())
						.adjustedForMargin(true)
						.build());
		}
		return Optional.empty();
	}

	@Override
	public CurrentResult copy() {
		return new CloseResult(this);
	}
}