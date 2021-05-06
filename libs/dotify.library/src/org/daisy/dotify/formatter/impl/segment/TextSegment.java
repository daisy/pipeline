package org.daisy.dotify.formatter.impl.segment;

import org.daisy.dotify.api.formatter.TextProperties;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;

import java.util.Objects;
import java.util.Optional;

/**
 * TODO: Write java doc.
 */
public class TextSegment implements Segment {
    private final String chars;
    private final TextProperties tp;
    private BrailleTranslatorResult cache;

    public TextSegment(String chars, TextProperties tp) {
        this.chars = Objects.requireNonNull(chars);
        this.tp = Objects.requireNonNull(tp);
    }

    public boolean canMakeResult() {
        return cache != null;
    }

    public BrailleTranslatorResult newResult() {
        return cache.copy();
    }

    public void storeResult(BrailleTranslatorResult template) {
        this.cache = template.copy();
    }

    public String getText() {
        return chars;
    }

    public TextProperties getTextProperties() {
        return tp;
    }

    @Override
    public SegmentType getSegmentType() {
        return SegmentType.Text;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((chars == null) ? 0 : chars.hashCode());
        result = prime * result + ((tp == null) ? 0 : tp.hashCode());
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
        TextSegment other = (TextSegment) obj;
        if (chars == null) {
            if (other.chars != null) {
                return false;
            }
        } else if (!chars.equals(other.chars)) {
            return false;
        }
        if (tp == null) {
            if (other.tp != null) {
                return false;
            }
        } else if (!tp.equals(other.tp)) {
            return false;
        }
        return true;
    }

    @Override
    public String peek() {
        return chars;
    }

    @Override
    public String resolve() {
        return chars;
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    @Override
    public Optional<String> getLocale() {
        return Optional.ofNullable(tp.getLocale());
    }

    @Override
    public boolean shouldHyphenate() {
        return tp.isHyphenating();
    }

    @Override
    public boolean shouldMarkCapitalLetters() {
        return tp.shouldMarkCapitalLetters();
    }

}
