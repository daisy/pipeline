package cz.vutbr.web.csskit;

import java.util.List;

import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.StyleSheet;

/**
 * CSS style sheet, entry point.
 * Allows 
 * 
 * @author kapy
 * 
 */
public class StyleSheetImpl extends AbstractRule<RuleBlock<?>> implements StyleSheet {
	
	protected StyleSheetImpl() {
	}

	@Override
	public StyleSheet filter(MediaSpec medium) {
		StyleSheet filtered = new StyleSheetImpl();
		filtered.unlock();
		filtered.setOrigin(getOrigin());
		filtered.setMediaQueries(getMediaQueries());
		filter(this, medium, filtered);
		return filtered;
	}

	private static void filter(Iterable<RuleBlock<?>> unfiltered, MediaSpec medium, List<RuleBlock<?>> filtered) {
		for (RuleBlock<?> rule : unfiltered) {
			if (rule instanceof RuleMedia) {
				if (medium.matches(((RuleMedia)rule).getMediaQueries()))
					filter((RuleMedia)rule, medium, filtered);
			} else
				filtered.add(rule);
		}
	}
}
