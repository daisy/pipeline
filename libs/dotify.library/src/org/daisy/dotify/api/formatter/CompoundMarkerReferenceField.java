package org.daisy.dotify.api.formatter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A CompoundMarkerReferenceField holds a sequence of {@link
 * MarkerReferenceField}. Only the first MarkerReferenceField that
 * resolves to a value is rendered.
 *
 * @author bert
 */
public class CompoundMarkerReferenceField implements Field, Iterable<MarkerReferenceField> {

    private final List<MarkerReferenceField> children;

    public CompoundMarkerReferenceField(List<MarkerReferenceField> fields) {
        children = new ArrayList<>();
        children.addAll(fields);
    }

    public Iterator<MarkerReferenceField> iterator() {
        return children.iterator();
    }

    public String getTextStyle() {
        return null;
    }
}
