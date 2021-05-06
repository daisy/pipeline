package org.daisy.dotify.formatter.impl.core;

import org.daisy.dotify.api.formatter.PageArea;
import org.daisy.dotify.api.formatter.PageAreaProperties;
import org.daisy.dotify.formatter.impl.common.FormatterCoreContext;

/**
 * TODO: Write java doc.
 */
public class PageAreaImpl implements PageArea {
    private final PageAreaProperties properties;
    private final FormatterCoreImpl beforeArea;
    private final FormatterCoreImpl afterArea;

    PageAreaImpl(FormatterCoreContext fc, PageAreaProperties properties) {
        this.properties = properties;
        this.beforeArea = new FormatterCoreImpl(fc);
        this.afterArea = new FormatterCoreImpl(fc);
    }

    PageAreaProperties getProperties() {
        return properties;
    }

    @Override
    public FormatterCoreImpl getBeforeArea() {
        return beforeArea;
    }

    @Override
    public FormatterCoreImpl getAfterArea() {
        return afterArea;
    }

}
