package org.daisy.braille.utils.impl.provider.indexbraille;

import org.daisy.dotify.api.paper.Length;

final class Margin {
    private final Length top;
    private final Length bottom;
    private final Length left;
    private final Length right;

    static class Builder {
        private Length top = Length.newMillimeterValue(0);
        private Length bottom = Length.newMillimeterValue(0);
        private Length left = Length.newMillimeterValue(0);
        private Length right = Length.newMillimeterValue(0);

        Builder() {
        }

        Builder top(Length value) {
            this.top = value;
            return this;
        }

        Builder bottom(Length value) {
            this.bottom = value;
            return this;
        }

        Builder left(Length value) {
            this.left = value;
            return this;
        }

        Builder right(Length value) {
            this.right = value;
            return this;
        }

        Margin build() {
            return new Margin(this);
        }

    }

    private Margin(Builder builder) {
        this.top = builder.top;
        this.bottom = builder.bottom;
        this.left = builder.left;
        this.right = builder.right;
    }

    public Length getTop() {
        return top;
    }

    public Length getBottom() {
        return bottom;
    }

    public Length getLeft() {
        return left;
    }

    public Length getRight() {
        return right;
    }

}
