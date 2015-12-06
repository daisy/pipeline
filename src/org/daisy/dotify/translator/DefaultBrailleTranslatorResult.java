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

	@Override
	public String nextTranslatedRow(int limit, boolean force) {
		if (finalizer!=null) {
			return finalizer.finalizeBraille(bph.nextRow(limit, force).getHead());
		} else {
			return bph.nextRow(limit, force).getHead();
		}
	}
	
	@Override
	public boolean hasNext() {
		return bph.hasNext();
	}

	@Override
	public String getTranslatedRemainder() {
		if (finalizer!=null) {
			return finalizer.finalizeBraille(bph.getRemaining());
		} else {
			return bph.getRemaining();
		}
	}

	@Override
	public int countRemaining() {
		return getTranslatedRemainder().length();
	}

}
