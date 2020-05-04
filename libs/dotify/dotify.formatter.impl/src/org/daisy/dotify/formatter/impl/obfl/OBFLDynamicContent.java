package org.daisy.dotify.formatter.impl.obfl;

import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.obfl.ExpressionFactory;

/**
 * TODO: Write java doc.
 */
public class OBFLDynamicContent extends OBFLExpressionBase implements DynamicContent {

    public OBFLDynamicContent(String exp, ExpressionFactory ef, OBFLVariable... variables) {
        super(exp, ef, variables);
    }

    @Override
    public String render() {
        if (exp == null) {
            return "";
        } else {
            return ef.newExpression().evaluate(exp).toString();
        }
    }

    @Override
    public String render(Context context) {
        if (exp == null) {
            return "";
        } else {
            return ef.newExpression().evaluate(ExpressionTools.resolveVariables(exp, buildArgs(context))).toString();
        }
    }

}
