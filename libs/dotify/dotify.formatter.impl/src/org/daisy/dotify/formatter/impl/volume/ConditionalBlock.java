package org.daisy.dotify.formatter.impl.volume;

import org.daisy.dotify.api.formatter.Condition;
import org.daisy.dotify.api.formatter.Context;
import org.daisy.dotify.formatter.impl.core.FormatterCoreImpl;


class ConditionalBlock {
	private final Condition condition;
	private final FormatterCoreImpl sequence;
	
	ConditionalBlock(FormatterCoreImpl sequence, Condition condition) {
		this.sequence = sequence;
		this.condition = condition;
	}
	
	FormatterCoreImpl getSequence() {
		return sequence;
	}
	
	boolean appliesTo(Context context) {
		if (condition==null) {
			return true;
		}
		return condition.evaluate(context);
	}

}