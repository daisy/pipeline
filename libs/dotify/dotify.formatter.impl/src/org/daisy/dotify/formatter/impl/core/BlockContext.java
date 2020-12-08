package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.formatter.impl.search.DefaultContext;
import org.daisy.dotify.formatter.impl.search.PageId;
import org.daisy.dotify.formatter.impl.search.Space;

/**
 * TODO: Write java doc.
 */
public class BlockContext extends DefaultContext {
    private final int flowWidth;
    private final FormatterContext fcontext;
    private final boolean topOfPage;

    /**
     * TODO: Write java doc.
     */
    public static class Builder extends DefaultContext.Builder {
        private int flowWidth = 0;
        private FormatterContext fcontext = null;
        private boolean topOfPage;

        public Builder(BlockContext base) {
            super(base);
            this.flowWidth = base.flowWidth;
            this.fcontext = base.fcontext;
            this.topOfPage = base.topOfPage;
        }

        protected Builder(DefaultContext base) {
            super(base);
        }

        public Builder flowWidth(int value) {
            this.flowWidth = value;
            return this;
        }

        public Builder formatterContext(FormatterContext value) {
            this.fcontext = value;
            return this;
        }

        public Builder topOfPage(boolean value) {
            this.topOfPage = value;
            return this;
        }

        @Override
        public Builder currentVolume(Integer value) {
            super.currentVolume(value);
            return this;
        }

        @Override
        public Builder currentPage(Integer value) {
            super.currentPage(value);
            return this;
        }

        @Override
        public Builder currentPage(PageId index, Integer number) {
            super.currentPage(index, number);
            return this;
        }

        @Override
        public Builder metaVolume(Integer value) {
            super.metaVolume(value);
            return this;
        }

        @Override
        public Builder metaPage(Integer value) {
            super.metaPage(value);
            return this;
        }

        @Override
        public Builder space(Space value) {
            super.space(value);
            return this;
        }

        public BlockContext build() {
            return new BlockContext(this);
        }
    }

    protected BlockContext(Builder builder) {
        super(builder);
        this.flowWidth = builder.flowWidth;
        this.fcontext = builder.fcontext;
        this.topOfPage = builder.topOfPage;
    }

    public static BlockContext.Builder from(DefaultContext base) {
        return new BlockContext.Builder(base);
    }

    public static BlockContext.Builder from(BlockContext base) {
        return new BlockContext.Builder(base);
    }

    public int getFlowWidth() {
        return flowWidth;
    }

    public FormatterContext getFcontext() {
        return fcontext;
    }

    /**
     * This is the top of page boolean. It will be set to true during the rendering to signify that you
     * are currently at the top of the page in this context. When you try to write the first data to the
     * page you are no longer at the top of the page and this value will be changed to false.
     *
     * @return  boolean     Will be true if you are at the top of the page during rendering.
     */
    public boolean isTopOfPage() {
        return topOfPage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fcontext == null) ? 0 : fcontext.hashCode());
        result = prime * result + flowWidth;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BlockContext other = (BlockContext) obj;
        if (fcontext == null) {
            if (other.fcontext != null) {
                return false;
            }
        } else if (!fcontext.equals(other.fcontext)) {
            return false;
        }
        if (flowWidth != other.flowWidth) {
            return false;
        }
        return true;
    }

}
