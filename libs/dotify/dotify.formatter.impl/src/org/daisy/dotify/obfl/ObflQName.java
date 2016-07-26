package org.daisy.dotify.obfl;

import javax.xml.namespace.QName;

interface ObflQName {
	final static String OBFL_NS = "http://www.daisy.org/ns/2011/obfl";
	 final static QName OBFL = new QName(OBFL_NS, "obfl");
	final static QName META = new QName(OBFL_NS, "meta");
	 final static QName LAYOUT_MASTER = new QName(OBFL_NS, "layout-master");
	 final static QName TEMPLATE = new QName(OBFL_NS, "template");
	 final static QName DEFAULT_TEMPLATE = new QName(OBFL_NS, "default-template");
	 final static QName HEADER = new QName(OBFL_NS, "header");
	 final static QName FOOTER = new QName(OBFL_NS, "footer");
	 final static QName MARGIN_REGION = new QName(OBFL_NS, "margin-region");
	 final static QName INDICATORS = new QName(OBFL_NS, "indicators");
	 final static QName MARKER_INDICATOR = new QName(OBFL_NS, "marker-indicator");
	 final static QName FIELD = new QName(OBFL_NS, "field");
	 final static QName STRING = new QName(OBFL_NS, "string");
	 final static QName EVALUATE = new QName(OBFL_NS, "evaluate");
	 final static QName CURRENT_PAGE = new QName(OBFL_NS, "current-page");
	 final static QName MARKER_REFERENCE = new QName(OBFL_NS, "marker-reference");
	 final static QName XML_DATA = new QName(OBFL_NS, "xml-data");
	 final static QName XML_PROCESSOR_RESULT = new QName(OBFL_NS, "xml-processor-result");
	 final static QName BLOCK = new QName(OBFL_NS, "block");
	 final static QName SPAN = new QName(OBFL_NS, "span");
	final static QName STYLE = new QName(OBFL_NS, "style");
	 final static QName TOC_ENTRY = new QName(OBFL_NS, "toc-entry");
	 final static QName LEADER = new QName(OBFL_NS, "leader");
	 final static QName MARKER = new QName(OBFL_NS, "marker");
	 final static QName ANCHOR = new QName(OBFL_NS, "anchor");
	 final static QName BR = new QName(OBFL_NS, "br");
	 final static QName PAGE_NUMBER = new QName(OBFL_NS, "page-number");
	
	 final static QName SEQUENCE = new QName(OBFL_NS, "sequence");
	 final static QName PAGE_AREA = new QName(OBFL_NS, "page-area");
	 final static QName FALLBACK = new QName(OBFL_NS, "fallback");
	 final static QName RENAME = new QName(OBFL_NS, "rename");
	 final static QName BEFORE = new QName(OBFL_NS, "before");
	 final static QName AFTER = new QName(OBFL_NS, "after");
	 final static QName VOLUME_TEMPLATE = new QName(OBFL_NS, "volume-template");
	 final static QName PRE_CONTENT = new QName(OBFL_NS, "pre-content");
	 final static QName POST_CONTENT = new QName(OBFL_NS, "post-content");

	 final static QName TOC_SEQUENCE = new QName(OBFL_NS, "toc-sequence");
	 final static QName ON_TOC_START = new QName(OBFL_NS, "on-toc-start");
	 final static QName ON_VOLUME_START = new QName(OBFL_NS, "on-volume-start");
	 final static QName ON_VOLUME_END = new QName(OBFL_NS, "on-volume-end");
	 final static QName ON_TOC_END = new QName(OBFL_NS, "on-toc-end");

	 final static QName DYNAMIC_SEQUENCE = new QName(OBFL_NS, "dynamic-sequence");
	 final static QName INSERT_REFS_LIST = new QName(OBFL_NS, "list-of-references");

	 final static QName ON_COLLECTION_START = new QName(OBFL_NS, "on-collection-start");
	 final static QName ON_PAGE_START = new QName(OBFL_NS, "on-page-start");
	 final static QName ON_PAGE_END = new QName(OBFL_NS, "on-page-end");
	 final static QName ON_COLLECTION_END = new QName(OBFL_NS, "on-collection-end");
	
	 final static QName TABLE_OF_CONTENTS = new QName(OBFL_NS, "table-of-contents");
	 
	 final static QName COLLECTION = new QName(OBFL_NS, "collection");
	 final static QName ITEM = new QName(OBFL_NS, "item");
	 
	 final static QName XML_PROCESSOR = new QName(OBFL_NS, "xml-processor");
	 final static QName RENDERER = new QName(OBFL_NS, "renderer");
	 final static QName RENDERING_SCENARIO = new QName(OBFL_NS, "rendering-scenario");
	 final static QName FILE_REFERENCE = new QName(OBFL_NS, "file-reference");
	 final static QName PARAMETER = new QName(OBFL_NS, "parameter");
	
	 final static QName ATTR_XML_LANG = new QName("http://www.w3.org/XML/1998/namespace", "lang", "xml");
	 final static QName ATTR_HYPHENATE = new QName("hyphenate");
	 final static QName ATTR_TRANSLATE = new QName("translate");
	 final static QName ATTR_PAGE_WIDTH = new QName("page-width");
	 final static QName ATTR_PAGE_HEIGHT = new QName("page-height");
	 final static QName ATTR_NAME = new QName("name");
	 final static QName ATTR_ID = new QName("id");
	 final static QName ATTR_USE_WHEN = new QName("use-when");
	 final static QName ATTR_COLLECTION = new QName("collection");
	 final static QName ATTR_MAX_HEIGHT = new QName("max-height");
	 final static QName ATTR_ALIGN = new QName("align");
	 final static QName ATTR_TEXT_STYLE = new QName("text-style");
	 final static QName ATTR_START_OFFSET = new QName("start-offset");
	 final static QName ATTR_MARKER = new QName("marker");
	 final static QName ATTR_INITIAL_PAGE_NUMBER = new QName("initial-page-number");
	 final static QName ATTR_WIDTH = new QName("width");
	 final static QName ATTR_PROCESSOR = new QName("processor");
	 final static QName ATTR_QUALIFIER = new QName("qualifier");
	 final static QName ATTR_COST = new QName("cost");
	 final static QName ATTR_URI = new QName("uri");
	 final static QName ATTR_VALUE = new QName("value");
	 
	final static QName TABLE = new QName(OBFL_NS, "table");
	final static QName THEAD = new QName(OBFL_NS, "thead");
	final static QName TBODY = new QName(OBFL_NS, "tbody");
	final static QName TR = new QName(OBFL_NS, "tr");
	final static QName TD = new QName(OBFL_NS, "td");
	
	final static QName ATTR_TABLE_COL_SPACING = new QName("table-col-spacing");
	final static QName ATTR_TABLE_ROW_SPACING = new QName("table-row-spacing");
	final static QName ATTR_TABLE_PREFERRED_EMPTY_SPACE = new QName("preferred-empty-space");
	final static QName ATTR_COL_SPAN = new QName("col-span");
	final static QName ATTR_ROW_SPAN = new QName("row-span");
	 
}