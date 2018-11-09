package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextAttribute;


/**
 * Provides an evaluate event object.
 * 
 * @author Joel Håkansson
 *
 */
public class Evaluate implements Segment {
	private final DynamicContent expression;
	private final TextProperties props;
	private final String[] textStyle;
	
	public Evaluate(DynamicContent expression, TextProperties props) {
		this(expression, props, null);
	}
	
	/**
	 * @param expression the expression
	 * @param props the text properties
	 * @param textStyle Array of styles to apply (from outer to inner).
	 * 
	 */
	public Evaluate(DynamicContent expression, TextProperties props, String[] textStyle) {
		this.expression = expression;
		this.props = props;
		this.textStyle = textStyle;
	}
	
	public DynamicContent getExpression() {
		return expression;
	}

	public TextProperties getTextProperties() {
		return props;
	}

	/**
	 * Creates a new text attribute with the specified width.
	 * @param width The width of the evaluated expression.
	 * @return returns the new text attribute
	 */
	public TextAttribute getTextAttribute(int width) {
		if (textStyle == null || textStyle.length == 0) {
			return null;
		} else {
			TextAttribute a = new DefaultTextAttribute.Builder(textStyle[0]).build(width);
			for (int i = 1; i < textStyle.length; i++) {
				a = new DefaultTextAttribute.Builder(textStyle[i]).add(a).build(width);
			}
			return a;
		}
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Evaluate;
	}

}
