package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.common.text.BreakPointHandler;

class DefaultBrailleTranslatorResult implements BrailleTranslatorResult {
	private final BreakPointHandler bph;
	private final BrailleFinalizer finalizer;

	public DefaultBrailleTranslatorResult(BreakPointHandler bph, BrailleFinalizer finalizer) {
		super();
		this.finalizer = finalizer;
		this.bph = bph;
	}

	public String nextTranslatedRow(int limit, boolean force) {
		if (finalizer!=null) {
			return finalizer.finalizeBraille(bph.nextRow(limit, force).getHead());
		} else {
			return bph.nextRow(limit, force).getHead();
		}
	}
	
	public boolean hasNext() {
		return bph.hasNext();
	}

	public String getTranslatedRemainder() {
		if (finalizer!=null) {
			return finalizer.finalizeBraille(bph.getRemaining());
		} else {
			return bph.getRemaining();
		}
	}

	public int countRemaining() {
		return getTranslatedRemainder().length();
	}

}
