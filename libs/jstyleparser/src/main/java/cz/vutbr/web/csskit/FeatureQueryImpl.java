package cz.vutbr.web.csskit;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.FeatureCondition;
import cz.vutbr.web.css.FeatureQuery;
import cz.vutbr.web.css.FeatureSpec;

// supports-condition> = not <supports-in-parens> | <supports-in-parens> [ and <supports-in-parens> ]* | <supports-in-parens> [ or <supports-in-parens> ]*
// <supports-in-parens> = ( <supports-condition> ) | ( <declaration> )

public class FeatureQueryImpl extends AbstractRule<FeatureCondition> implements FeatureQuery {

	private final boolean negative;

	@Override
	public boolean isSatisfied(FeatureSpec userAgent) {
		boolean satisfied = true;
		for (FeatureCondition condition : this) {
			if (!satisfied)
				break;
			satisfied = condition.isSatisfied(userAgent);
		}
		if (negative)
			satisfied = !satisfied;
		return satisfied;
	}

	FeatureQueryImpl() {
		this(false);
	}

	private FeatureQueryImpl(boolean negative) {
		this.negative = negative;
	}

	@Override
	public FeatureQueryImpl negate() {
		FeatureQueryImpl negated = new FeatureQueryImpl(!negative);
		negated.unlock();
		negated.addAll(this);
		return negated;
	}

	@Override
	public boolean add(FeatureCondition element) {
		if (element instanceof DeclarationImpl || element instanceof FeatureQueryImpl)
			return super.add(element);
		else
			throw new IllegalArgumentException(
				"FeatureCondition must be either a DeclarationImpl or a FeatureQueryImpl");
	}

	@Override
	public String toString(int depth) {
		StringBuilder sb = new StringBuilder();
		if (size() > 0) { // should always be the case
			if (negative) {
				sb.append("not ");
				if (size() > 1)
					sb.append("( ");
			}
			boolean first = true;
			for (FeatureCondition c : this) {
				if (!first)
					sb.append(" and ");
				else
					first = false;
				if (c instanceof Declaration)
					sb.append("( ").append(c).append(" )");
				else {
					FeatureQueryImpl q = (FeatureQueryImpl)c;
					if (!q.negative && q.size() == 1)
						// already in parens
						sb.append(c);
					else
						sb.append("( ").append(c).append(" )");
				}
				sb.append(c);
			}
			if (negative && size() > 1)
				sb.append(" )");
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return toString(0);
	}
}
