package org.daisy.dotify.obfl;

import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.obfl.ExpressionFactory;

public class OBFLDynamicContent extends OBFLExpressionBase implements DynamicContent {

	public OBFLDynamicContent(String exp, ExpressionFactory ef, boolean extended) {
		super(exp, ef, extended);
	}

	@Override
	public String render() {
		if (exp==null) {
			return "";
		} else {
			return ef.newExpression().evaluate(exp).toString();
		}
	}

	@Override
	public String render(Context context) {
		if (exp==null) {
			return "";
		} else {
			return ef.newExpression().evaluate(exp, buildArgs(context)).toString();
		}
	}

}
