package org.daisy.dotify.formatter.impl;

import org.daisy.dotify.api.formatter.PageAreaBuilder;
import org.daisy.dotify.api.formatter.PageAreaProperties;

class PageAreaBuilderImpl implements PageAreaBuilder {
	private final PageAreaProperties properties;
	private final FormatterCoreImpl beforeArea;
	private final FormatterCoreImpl afterArea;

	PageAreaBuilderImpl(FormatterCoreContext fc, PageAreaProperties properties) {
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
