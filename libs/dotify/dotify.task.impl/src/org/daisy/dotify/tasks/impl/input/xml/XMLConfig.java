package org.daisy.dotify.tasks.impl.input.xml;

import java.util.Map;

import org.daisy.dotify.common.io.ResourceLocator;

class XMLConfig {
	private final String rootElement;
	private final String rootNS;
	private final ResourceLocator localLocator;
	private final ResourceLocator commonLocator;
	private final String template;
	private final Map<String, Object> xsltParams;

	public XMLConfig(String rootElement, String rootNS, String template, Map<String, Object> xsltParams, ResourceLocator localLocator, ResourceLocator commonLocator) {
		this.rootElement = rootElement;
		this.rootNS = rootNS;
		this.localLocator = localLocator;
		this.commonLocator = commonLocator;
		this.template = template;
		this.xsltParams = xsltParams;
	}

	String getRootElement() {
		return rootElement;
	}

	String getRootNS() {
		return rootNS;
	}

	ResourceLocator getLocalLocator() {
		return localLocator;
	}

	ResourceLocator getCommonLocator() {
		return commonLocator;
	}

	String getTemplate() {
		return template;
	}

	Map<String, Object> getXsltParams() {
		return xsltParams;
	}

}
