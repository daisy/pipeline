package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.Context;


class ConditionalBlock {
	private final Condition condition;
	private final FormatterCoreImpl sequence;
	
	public ConditionalBlock(FormatterCoreImpl sequence, Condition condition) {
		this.sequence = sequence;
		this.condition = condition;
	}
	
	public FormatterCoreImpl getSequence() {
		return sequence;
	}
	
	public boolean appliesTo(Context context) {
		if (condition==null) {
			return true;
		}
		return condition.evaluate(context);
	}

}