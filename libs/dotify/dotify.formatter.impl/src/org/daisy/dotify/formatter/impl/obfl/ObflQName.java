package org.daisy.dotify.formatter.impl.obfl;

import javax.xml.namespace.QName;

interface ObflQName {
	static final String OBFL_NS = "http://www.daisy.org/ns/2011/obfl";
	 static final QName OBFL = new QName(OBFL_NS, "obfl");
	static final QName META = new QName(OBFL_NS, "meta");
	 static final QName LAYOUT_MASTER = new QName(OBFL_NS, "layout-master");
	 static final QName TEMPLATE = new QName(OBFL_NS, "template");
	 static final QName DEFAULT_TEMPLATE = new QName(OBFL_NS, "default-template");
	 static final QName HEADER = new QName(OBFL_NS, "header");
	 static final QName FOOTER = new QName(OBFL_NS, "footer");
	 static final QName MARGIN_REGION = new QName(OBFL_NS, "margin-region");
	 static final QName INDICATORS = new QName(OBFL_NS, "indicators");
	 static final QName MARKER_INDICATOR = new QName(OBFL_NS, "marker-indicator");
	 static final QName FIELD = new QName(OBFL_NS, "field");
	 static final QName STRING = new QName(OBFL_NS, "string");
	 static final QName EVALUATE = new QName(OBFL_NS, "evaluate");
	 static final QName CURRENT_PAGE = new QName(OBFL_NS, "current-page");
	 static final QName MARKER_REFERENCE = new QName(OBFL_NS, "marker-reference");
	 static final QName XML_DATA = new QName(OBFL_NS, "xml-data");
	 static final QName XML_PROCESSOR_RESULT = new QName(OBFL_NS, "xml-processor-result");
	 static final QName BLOCK = new QName(OBFL_NS, "block");
	 static final QName SPAN = new QName(OBFL_NS, "span");
	static final QName STYLE = new QName(OBFL_NS, "style");
	 static final QName TOC_ENTRY = new QName(OBFL_NS, "toc-entry");
	 static final QName LEADER = new QName(OBFL_NS, "leader");
	 static final QName MARKER = new QName(OBFL_NS, "marker");
	 static final QName ANCHOR = new QName(OBFL_NS, "anchor");
	 static final QName BR = new QName(OBFL_NS, "br");
	 static final QName PAGE_NUMBER = new QName(OBFL_NS, "page-number");
	
	 static final QName SEQUENCE = new QName(OBFL_NS, "sequence");
	 static final QName PAGE_AREA = new QName(OBFL_NS, "page-area");
	 static final QName FALLBACK = new QName(OBFL_NS, "fallback");
	 static final QName RENAME = new QName(OBFL_NS, "rename");
	 static final QName BEFORE = new QName(OBFL_NS, "before");
	 static final QName AFTER = new QName(OBFL_NS, "after");
	 static final QName VOLUME_TEMPLATE = new QName(OBFL_NS, "volume-template");
	 static final QName PRE_CONTENT = new QName(OBFL_NS, "pre-content");
	 static final QName POST_CONTENT = new QName(OBFL_NS, "post-content");
	 
	 static final QName VOLUME_TRANSITION = new QName(OBFL_NS, "volume-transition");

	 static final QName TOC_SEQUENCE = new QName(OBFL_NS, "toc-sequence");
	 static final QName ON_TOC_START = new QName(OBFL_NS, "on-toc-start");
	 static final QName ON_VOLUME_START = new QName(OBFL_NS, "on-volume-start");
	 static final QName ON_VOLUME_END = new QName(OBFL_NS, "on-volume-end");
	 static final QName ON_TOC_END = new QName(OBFL_NS, "on-toc-end");

	 static final QName DYNAMIC_SEQUENCE = new QName(OBFL_NS, "dynamic-sequence");
	 static final QName INSERT_REFS_LIST = new QName(OBFL_NS, "list-of-references");

	 static final QName ON_COLLECTION_START = new QName(OBFL_NS, "on-collection-start");
	 static final QName ON_PAGE_START = new QName(OBFL_NS, "on-page-start");
	 static final QName ON_PAGE_END = new QName(OBFL_NS, "on-page-end");
	 static final QName ON_COLLECTION_END = new QName(OBFL_NS, "on-collection-end");
	 
	 static final QName BLOCK_INTERRUPTED = new QName(OBFL_NS, "block-interrupted");
	 static final QName BLOCK_RESUMED = new QName(OBFL_NS, "block-resumed");
	 static final QName SEQUENCE_INTERRUPTED = new QName(OBFL_NS, "sequence-interrupted");
	 static final QName SEQUENCE_RESUMED = new QName(OBFL_NS, "sequence-resumed");
	
	 static final QName TABLE_OF_CONTENTS = new QName(OBFL_NS, "table-of-contents");
	 
	 static final QName COLLECTION = new QName(OBFL_NS, "collection");
	 static final QName ITEM = new QName(OBFL_NS, "item");
	 
	 static final QName XML_PROCESSOR = new QName(OBFL_NS, "xml-processor");
	 static final QName RENDERER = new QName(OBFL_NS, "renderer");
	 static final QName RENDERING_SCENARIO = new QName(OBFL_NS, "rendering-scenario");
	 static final QName FILE_REFERENCE = new QName(OBFL_NS, "file-reference");
	 static final QName PARAMETER = new QName(OBFL_NS, "parameter");
	
	 static final QName ATTR_XML_LANG = new QName("http://www.w3.org/XML/1998/namespace", "lang", "xml");
	 static final QName ATTR_HYPHENATE = new QName("hyphenate");
	 static final QName ATTR_TRANSLATE = new QName("translate");
	 static final QName ATTR_PAGE_WIDTH = new QName("page-width");
	 static final QName ATTR_PAGE_HEIGHT = new QName("page-height");
	 static final QName ATTR_NAME = new QName("name");
	 static final QName ATTR_ID = new QName("id");
	 static final QName ATTR_USE_WHEN = new QName("use-when");
	 static final QName ATTR_COLLECTION = new QName("collection");
	 static final QName ATTR_MAX_HEIGHT = new QName("max-height");
	 static final QName ATTR_ALIGN = new QName("align");
	 static final QName ATTR_TEXT_STYLE = new QName("text-style");
	 static final QName ATTR_ALLOW_TEXT_FLOW = new QName("allow-text-flow");
	 static final QName ATTR_START_OFFSET = new QName("start-offset");
	 static final QName ATTR_MARKER = new QName("marker");
	 static final QName ATTR_INITIAL_PAGE_NUMBER = new QName("initial-page-number");
	 static final QName ATTR_WIDTH = new QName("width");
	 static final QName ATTR_PROCESSOR = new QName("processor");
	 static final QName ATTR_QUALIFIER = new QName("qualifier");
	 static final QName ATTR_COST = new QName("cost");
	 static final QName ATTR_URI = new QName("uri");
	 static final QName ATTR_VALUE = new QName("value");
	 
	static final QName TABLE = new QName(OBFL_NS, "table");
	static final QName THEAD = new QName(OBFL_NS, "thead");
	static final QName TBODY = new QName(OBFL_NS, "tbody");
	static final QName TR = new QName(OBFL_NS, "tr");
	static final QName TD = new QName(OBFL_NS, "td");
	
	static final QName ATTR_TABLE_COL_SPACING = new QName("table-col-spacing");
	static final QName ATTR_TABLE_ROW_SPACING = new QName("table-row-spacing");
	static final QName ATTR_TABLE_PREFERRED_EMPTY_SPACE = new QName("preferred-empty-space");
	static final QName ATTR_COL_SPAN = new QName("col-span");
	static final QName ATTR_ROW_SPAN = new QName("row-span");
	 
}
