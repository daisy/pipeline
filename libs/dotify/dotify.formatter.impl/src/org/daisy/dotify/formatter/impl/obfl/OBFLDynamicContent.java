package org.daisy.dotify.formatter.impl.obfl;

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
			return ef.newExpression().evaluate(ExpressionTools.resolveVariables(exp, buildArgs(context))).toString();
		}
	}

}
