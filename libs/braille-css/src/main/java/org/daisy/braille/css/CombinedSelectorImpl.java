package org.daisy.braille.css;

import cz.vutbr.web.css.Selector;

public class CombinedSelectorImpl extends cz.vutbr.web.csskit.CombinedSelectorImpl {
	
	// Appends selectors to last selector if last selector contains custom pseudo class
	@Override
	public boolean add(Selector selector) {
		try {
			Selector last = getLastSelector();
			if (last instanceof SelectorImpl) {
				return ((SelectorImpl)last).add(selector);
			}
		} catch (UnsupportedOperationException e) {
		}
		return super.add(selector);
	}
}
