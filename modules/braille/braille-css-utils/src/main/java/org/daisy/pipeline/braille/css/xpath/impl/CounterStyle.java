package org.daisy.pipeline.braille.css.xpath.impl;

import org.daisy.pipeline.braille.css.impl.BrailleCssStyle;

public class CounterStyle extends Stylesheet implements org.daisy.pipeline.css.CounterStyle {

	private final org.daisy.pipeline.css.CounterStyle counterStyle;

	public CounterStyle(BrailleCssStyle style) {
		super(style);
		if (style.underlyingObject == null || !(style.underlyingObject instanceof org.daisy.pipeline.css.CounterStyle))
			throw new IllegalArgumentException();
		counterStyle = (org.daisy.pipeline.css.CounterStyle)style.underlyingObject;
	}

	public String format(int counterValue) {
		return counterStyle.format(counterValue);
	}

	public String format(int counterValue, boolean withPrefixAndSuffix) {
		return counterStyle.format(counterValue, withPrefixAndSuffix);
	}

	@Deprecated
	public String getTextTransform(int counterValue) {
		return counterStyle.getTextTransform(counterValue);
	}
}
