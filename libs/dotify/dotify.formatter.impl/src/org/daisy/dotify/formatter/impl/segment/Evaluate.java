package org.daisy.dotify.formatter.impl.segment;

import java.util.Optional;
import java.util.function.Function;

import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.TextProperties;


/**
 * Provides an evaluate event object.
 * 
 * @author Joel Håkansson
 *
 */
public class Evaluate implements Segment {
	private final DynamicContent expression;
	private final TextProperties props;
	private final boolean markCapitalLetters;
	private Function<Evaluate,String> v = (x)->"";
	private String resolved;
	
	/**
	 * @param expression the expression
	 * @param props the text properties
	 * @param markCapitalLetters true if capital letters should be marked
	 */
	public Evaluate(DynamicContent expression, TextProperties props, boolean markCapitalLetters) {
		this(expression, props, markCapitalLetters, null);
	}

	public Evaluate(DynamicContent expression, TextProperties props, boolean markCapitalLetters, MarkerValue marker) {
		this.expression = expression;
		this.props = props;
		this.markCapitalLetters = markCapitalLetters;
	}
	
	public DynamicContent getExpression() {
		return expression;
	}

	@Override
	public SegmentType getSegmentType() {
		return SegmentType.Evaluate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		result = prime * result + ((props == null) ? 0 : props.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Evaluate other = (Evaluate) obj;
		if (expression == null) {
			if (other.expression != null) {
				return false;
			}
		} else if (!expression.equals(other.expression)) {
			return false;
		}
		if (props == null) {
			if (other.props != null) {
				return false;
			}
		} else if (!props.equals(other.props)) {
			return false;
		}
		return true;
	}

	@Override
	public String peek() {
		return resolved==null?v.apply(this):resolved;
	}

	@Override
	public String resolve() {
		if (resolved==null) {
			resolved = v.apply(this);
			if (resolved == null) {
				resolved = "";
			}
		}
		return resolved;
	}

	public void setResolver(Function<Evaluate,String> v) {
		this.resolved = null;
		this.v = v;
	}

	@Override
	public boolean isStatic() {
		return false;
	}
	
	@Override
	public Optional<String> getLocale() {
		return Optional.of(props.getLocale());
	}

	@Override
	public boolean shouldHyphenate() {
		return props.isHyphenating();
	}

	@Override
	public boolean shouldMarkCapitalLetters() {
		return markCapitalLetters;
	}

}
