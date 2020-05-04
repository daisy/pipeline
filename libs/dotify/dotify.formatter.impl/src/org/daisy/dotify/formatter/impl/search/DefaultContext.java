package org.daisy.dotify.formatter.impl.search;

import org.daisy.dotify.api.formatter.Context;

/**
 * TODO: Write java doc.
 */
public class DefaultContext implements Context {

    private final Integer currentVolume, currentPage, metaVolume, metaPage;
    private final PageId currentPageId;
    private final Space space;
    protected final CrossReferenceHandler crh;

    /**
     * TODO: Write java doc.
     */
    public static class Builder {
        private Integer currentVolume = null, currentPage = null,
                metaVolume = null, metaPage = null;
        private PageId currentPageId = null;
        private Space space = null;
        private final CrossReferenceHandler crh;


        public Builder(CrossReferenceHandler crh) {
            this.crh = crh;
        }

        protected Builder(DefaultContext base) {
            this.currentVolume = base.getCurrentVolume();
            this.currentPage = base.getCurrentPage();
            this.metaVolume = base.getMetaVolume();
            this.metaPage = base.getMetaPage();
            this.currentPageId = base.getCurrentPageId();
            this.space = base.space;
            this.crh = base.crh;
        }

        public Builder currentVolume(Integer value) {
            this.currentVolume = value;
            return this;
        }

        public Builder currentPage(Integer value) {
            this.currentPage = value;
            return this;
        }

        public Builder currentPage(PageId index, Integer number) {
            this.currentPageId = index;
            this.currentPage = number;
            return this;
        }

        public Builder metaVolume(Integer value) {
            this.metaVolume = value;
            return this;
        }

        public Builder metaPage(Integer value) {
            this.metaPage = value;
            return this;
        }

        public Builder space(Space value) {
            this.space = value;
            return this;
        }

        public DefaultContext build() {
            return new DefaultContext(this);
        }
    }

    public static DefaultContext.Builder from(DefaultContext base) {
        return new DefaultContext.Builder(base);
    }

    protected DefaultContext(Builder builder) {
        this.currentVolume = builder.currentVolume;
        this.currentPage = builder.currentPage;
        this.metaVolume = builder.metaVolume;
        this.metaPage = builder.metaPage;
        this.currentPageId = builder.currentPageId;
        this.space = builder.space;
        this.crh = builder.crh;
    }

    @Override
    public Integer getCurrentVolume() {
        return currentVolume;
    }

    @Override
    public Integer getVolumeCount() {
        return (crh == null ? null : crh.getVolumeCount());
    }

    @Override
    public Integer getCurrentPage() {
        return currentPage;
    }

    @Override
    public Integer getMetaVolume() {
        return metaVolume;
    }

    @Override
    public Integer getMetaPage() {
        return metaPage;
    }

    @Override
    public Integer getPagesInVolume() {
        return (crh == null || currentVolume == null ? null : crh.getPagesInVolume(currentVolume));
    }

    @Override
    public Integer getPagesInDocument() {
        return (crh == null ? null : crh.getPagesInDocument());
    }

    @Override
    public Integer getSheetsInVolume() {
        return (crh == null || currentVolume == null ? null : crh.getSheetsInVolume(currentVolume));
    }

    @Override
    public Integer getSheetsInDocument() {
        return (crh == null ? null : crh.getSheetsInDocument());
    }

    public CrossReferenceHandler getRefs() {
        return crh;
    }

    public Space getSpace() {
        return space;
    }

    public PageId getCurrentPageId() {
        return currentPageId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((crh == null) ? 0 : crh.hashCode());
        result = prime * result + ((currentPage == null) ? 0 : currentPage.hashCode());
        result = prime * result + ((currentPageId == null) ? 0 : currentPageId.hashCode());
        result = prime * result + ((currentVolume == null) ? 0 : currentVolume.hashCode());
        result = prime * result + ((metaVolume == null) ? 0 : metaVolume.hashCode());
        result = prime * result + ((metaPage == null) ? 0 : metaPage.hashCode());
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
        DefaultContext other = (DefaultContext) obj;
        if (crh == null) {
            if (other.crh != null) {
                return false;
            }
        } else if (!crh.equals(other.crh)) {
            return false;
        }
        if (currentPage == null) {
            if (other.currentPage != null) {
                return false;
            }
        } else if (!currentPage.equals(other.currentPage)) {
            return false;
        }
        if (currentPageId == null) {
            if (other.currentPageId != null) {
                return false;
            }
        } else if (!currentPageId.equals(other.currentPageId)) {
            return false;
        }
        if (currentVolume == null) {
            if (other.currentVolume != null) {
                return false;
            }
        } else if (!currentVolume.equals(other.currentVolume)) {
            return false;
        }
        if (metaVolume == null) {
            if (other.metaVolume != null) {
                return false;
            }
        } else if (!metaVolume.equals(other.metaVolume)) {
            return false;
        }
        if (metaPage == null) {
            if (other.metaPage != null) {
                return false;
            }
        } else if (!metaPage.equals(other.metaPage)) {
            return false;
        }
        return true;
    }
}
