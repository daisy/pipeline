package org.daisy.braille.css;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.PrettyOutput;
import cz.vutbr.web.csskit.AbstractRuleBlock;
import cz.vutbr.web.csskit.OutputUtil;

public class RuleHyphenationResource extends AbstractRuleBlock<Declaration> implements PrettyOutput {

	private final List<LanguageRange> languageRanges;

	public RuleHyphenationResource(LanguageRange language) {
		super();
		languageRanges = Collections.singletonList(language);
	}

	public List<LanguageRange> getLanguageRanges() {
		return languageRanges;
	}

	public boolean matches(Locale language) {
		for (LanguageRange l : languageRanges)
			if (l.matches(language))
				return true;
		return false;
	}

	@Override
	public String toString() {
		return this.toString(0);
	}

	@Override
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		sb = OutputUtil.appendTimes(sb, OutputUtil.DEPTH_DELIM, depth);
		sb.append("@hyphenation-resource:lang(");
		sb = OutputUtil.appendList(sb, languageRanges, OutputUtil.SELECTOR_DELIM);
		sb.append(")");
		sb.append(OutputUtil.RULE_OPENING);
		sb = OutputUtil.appendList(sb, list, OutputUtil.RULE_DELIM, depth + 1);
		sb.append(OutputUtil.RULE_CLOSING);
		return sb.toString();
	}
}
