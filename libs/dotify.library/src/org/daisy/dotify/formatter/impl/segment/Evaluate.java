package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.DynamicContent;
import org.daisy.dotify.api.formatter.TextProperties;

import java.util.Optional;
import java.util.function.Function;


/**
 * Provides an evaluate event object.
 *
 * @author Joel Håkansson
 */
public class Evaluate implements Segment {
    private final DynamicContent expression;
    private final TextProperties props;
    private Function<Evaluate, String> value = (x) -> "";
    private String resolved;

    /**
     * @param expression         the expression
     * @param props              the text properties
     */
    public Evaluate(DynamicContent expression, TextProperties props) {
        this.expression = expression;
        this.props = props;
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
        return resolved == null ? value.apply(this) : resolved;
    }

    @Override
    public String resolve() {
        if (resolved == null) {
            resolved = value.apply(this);
            if (resolved == null) {
                resolved = "";
            }
        }
        return resolved;
    }

    public void setResolver(Function<Evaluate, String> v) {
        this.resolved = null;
        this.value = v;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public Optional<String> getLocale() {
        return Optional.ofNullable(props.getLocale());
    }

    @Override
    public boolean shouldHyphenate() {
        return props.isHyphenating();
    }

    @Override
    public boolean shouldMarkCapitalLetters() {
        return props.shouldMarkCapitalLetters();
    }

}
