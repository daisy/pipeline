package org.daisy.dotify.api.factory;

import java.io.Serializable;
import java.util.Objects;

/**
 * Provides an abstract class for Factories.
 *
 * @author Joel Håkansson
 */
public abstract class AbstractFactory implements Factory, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 3248048759239384586L;
    private final String name;
    private final String desc;
    private final String identifier;

    /**
     * Creates a new AbstractFactory with the supplied values.
     * It is currently possible to supply a null identifier, however this will
     * throw a null pointer exception in future versions.
     *
     * @param name       the factory name
     * @param desc       the factory description
     * @param identifier the factory identifier
     */
    public AbstractFactory(String name, String desc, String identifier) {
        this.name = name;
        this.desc = desc;
        this.identifier = Objects.requireNonNull(identifier);
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "AbstractFactory [name=" + name + ", desc=" + desc
                + ", identifier=" + identifier + "]";
    }

}
