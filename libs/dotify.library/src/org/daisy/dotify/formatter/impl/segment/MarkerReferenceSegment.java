package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.MarkerReference;
import org.daisy.dotify.api.formatter.TextProperties;

import java.util.Optional;
import java.util.function.Function;

/**
 * Provides a marker reference event object.
 */
public class MarkerReferenceSegment implements Segment {

    private final Iterable<? extends MarkerReference> ref;
    private final TextProperties props;
    private Function<Iterable<? extends MarkerReference>, String> value = (x) -> "";
    private String resolved;

    public MarkerReferenceSegment(Iterable<? extends MarkerReference> ref, TextProperties props) {
        if (ref == null) {
            throw new IllegalArgumentException();
        }
        this.ref = ref;
        this.props = props;
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.MarkerReference;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ref.hashCode();
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
        MarkerReferenceSegment other = (MarkerReferenceSegment) obj;
        if (!ref.equals(other.ref)) {
            return false;
        }
        return true;
    }

    @Override
    public String peek() {
        return resolved == null ? value.apply(ref) : resolved;
    }

    @Override
    public String resolve() {
        if (resolved == null) {
            resolved = value.apply(ref);
            if (resolved == null) {
                resolved = "";
            }
        }
        return resolved;
    }

    public void setResolver(Function<Iterable<? extends MarkerReference>, String> v) {
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
}
