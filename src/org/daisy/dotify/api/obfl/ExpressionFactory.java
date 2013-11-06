package org.daisy.dotify.api.obfl;

import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;

public interface ExpressionFactory {

	public Expression newExpression();

	public void setInteger2TextFactory(Integer2TextFactoryMakerService itf);

}
