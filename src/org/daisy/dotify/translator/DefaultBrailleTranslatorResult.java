package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.text.BreakPointHandler;
import org.daisy.dotify.text.StringFilter;

class DefaultBrailleTranslatorResult implements BrailleTranslatorResult {
	private final BreakPointHandler bph;
	private final UncontractedBrailleFilter filter;

	public DefaultBrailleTranslatorResult(BreakPointHandler bph, StringFilter filter) {
		super();
		if (UncontractedBrailleFilter.class.isInstance(filter)) {
			this.filter = (UncontractedBrailleFilter)filter;
		} else {
			this.filter = null;
		}
		this.bph = bph;
	}

	public String nextTranslatedRow(int limit, boolean force) {
		if (filter!=null) {
			return filter.finalize(bph.nextRow(limit, force).getHead());
		} else {
			return bph.nextRow(limit, force).getHead();
		}
	}
	
	public boolean hasNext() {
		return bph.hasNext();
	}

	public String getTranslatedRemainder() {
		if (filter!=null) {
			return filter.finalize(bph.getRemaining());
		} else {
			return bph.getRemaining();
		}
	}

	public int countRemaining() {
		return getTranslatedRemainder().length();
	}

}
