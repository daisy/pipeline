package org.daisy.dotify.api.translator;

import java.util.Optional;

/**
 * Provides an attribute for a list of items.
 *
 * @author Joel Håkansson
 */
public interface AttributeWithContext extends Iterable<AttributeWithContext> {

    /**
     * Gets the width of this attribute.
     *
     * @return the width, in items
     */
    public int getWidth();

    /**
     * Gets the name of this attribute.
     *
     * @return an optional containing the attribute name,
     * or an empty optional if it does not have any
     */
    public Optional<String> getName();

    /**
     * Returns true if this attribute has children.
     *
     * @return true if this attribute has children, false otherwise
     */
    public boolean hasChildren();

}
