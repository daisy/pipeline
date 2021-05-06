package org.daisy.braille.utils.pef;

import java.io.Serializable;

/**
 * Provides an identifier for a specific section.
 *
 * @author Joel Håkansson
 */
public final class SectionIdentifier implements Serializable {

    private static final long serialVersionUID = -4123255922859073531L;
    private final int volume;
    private final int section;

    /**
     * Creates a new section identifier with the specified volume and
     * the implied section number of 1.
     *
     * @param volume the volume number, one based
     */
    public SectionIdentifier(int volume) {
        this(volume, 1);
    }

    /**
     * Creates a new section identifier with the specified volume and section.
     *
     * @param volume  the volume number, one based
     * @param section the section number, one based
     */
    public SectionIdentifier(int volume, int section) {
        this.volume = volume;
        this.section = section;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + section;
        result = prime * result + volume;
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
        SectionIdentifier other = (SectionIdentifier) obj;
        if (section != other.section) {
            return false;
        }
        if (volume != other.volume) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "SectionIdentifier [volume=" + volume + ", section=" + section + "]";
    }

}
