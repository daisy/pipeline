package org.daisy.dotify.obfl.impl;

import org.daisy.dotify.api.obfl.Expression;
import org.daisy.dotify.api.obfl.ExpressionFactory;
import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;
import org.daisy.dotify.formatter.impl.SPIHelper;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class ExpressionFactoryImpl implements ExpressionFactory {
	private Integer2TextFactoryMakerService itf;

	@Override
	public Expression newExpression() {
		return new ExpressionImpl(itf);
	}

	@Override
	@Reference
	public void setInteger2TextFactory(Integer2TextFactoryMakerService itf) {
		this.itf = itf;
	}

	public void unsetInteger2TextFactory(Integer2TextFactoryMakerService itf) {
		this.itf = null;
	}

	@Override
	public void setCreatedWithSPI() {
		setInteger2TextFactory(SPIHelper.getInteger2TextFactoryMaker());
	}

}
