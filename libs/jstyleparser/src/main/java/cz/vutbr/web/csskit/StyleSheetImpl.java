package cz.vutbr.web.csskit;

import java.util.List;

import cz.vutbr.web.css.FeatureSpec;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.RuleBlock;
import cz.vutbr.web.css.RuleMedia;
import cz.vutbr.web.css.RuleSupports;
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

	@Override
	public StyleSheet filter(FeatureSpec userAgent) {
		StyleSheet filtered = new StyleSheetImpl();
		filtered.unlock();
		filtered.setOrigin(getOrigin());
		filtered.setMediaQueries(getMediaQueries());
		filter(this, userAgent, filtered);
		return filtered;
	}

	private static void filter(Iterable<RuleBlock<?>> unfiltered, FeatureSpec userAgent, List<RuleBlock<?>> filtered) {
		for (RuleBlock<?> rule : unfiltered) {
			if (rule instanceof RuleSupports) {
				if (((RuleSupports)rule).getCondition().isSatisfied(userAgent))
					filter((RuleSupports)rule, userAgent, filtered);
			} else
				filtered.add(rule);
		}
	}
}
