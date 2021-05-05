package cz.vutbr.web.csskit;

import java.net.URL;

import cz.vutbr.web.css.Declaration;

public class RuleXslt extends AbstractRuleBlock<Declaration> {

	public final String uri;
	public final URL base;

	public RuleXslt(String uri, URL base) {
		this.uri = uri;
		this.base = base;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@xslt ");
		sb.append(OutputUtil.STRING_OPENING)
			.append(uri)
			.append(OutputUtil.STRING_CLOSING);
		if (isEmpty())
			sb.append(OutputUtil.LINE_CLOSING);
		else {
			sb.append(OutputUtil.RULE_OPENING);
			sb = OutputUtil.appendList( sb, this, OutputUtil.EMPTY_DELIM, 1);
			sb.append(OutputUtil.RULE_CLOSING);
		}
		return sb.toString();
	}
}
