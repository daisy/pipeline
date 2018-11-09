package org.daisy.dotify.formatter.impl.row;

import java.util.Optional;

import org.daisy.dotify.common.text.StringTools;

class NewLineResult implements CurrentResult {
	private final SegmentProcessorContext spc;
	private boolean newLine;
	private Optional<CurrentResult> cr;

	NewLineResult(SegmentProcessorContext spc, Optional<CurrentResult> cr) {
		this.spc = spc;
		this.cr = cr;
		this.newLine = true;
	}
	
	private NewLineResult(NewLineResult template) {
		this.spc = template.spc;
		if (template.cr.isPresent()) {
			this.cr = Optional.of(template.cr.get().copy());
		}
		this.newLine = template.newLine;
	}

	@Override
	public boolean hasNext(SegmentProcessing spi) {
		return cr.isPresent() && cr.get().hasNext(spi) || newLine;
	}

	@Override
	public Optional<RowImpl> process(SegmentProcessing spi, boolean wholeWordsOnly) {
		if (cr.isPresent() && cr.get().hasNext(spi)) {
			return cr.get().process(spi, wholeWordsOnly);
		} else if (newLine) {
			newLine = false;
			try {
				if (spi.hasCurrentRow()) {
					return Optional.of(spi.flushCurrentRow());
				}
			} finally {
				MarginProperties ret = new MarginProperties(spc.getMargins().getLeftMargin().getContent()+StringTools.fill(spc.getSpaceCharacter(), spc.getRdp().getTextIndent()), spc.getMargins().getLeftMargin().isSpaceOnly());
				spi.newCurrentRow(ret, spc.getMargins().getRightMargin());
			}
		}
		return Optional.empty();
	}

	@Override
	public CurrentResult copy() {
		return new NewLineResult(this);
	}		
}