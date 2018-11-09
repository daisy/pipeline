package org.daisy.dotify.formatter.impl.obfl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;

import org.w3c.dom.Node;

final class RendererInfo {
	private final NamespaceContext namespaceContext;
	private final Node processor;
	private final String qualifier;
	private final String cost;
	private final Map<String, String> params;
	static class Builder {
		private final NamespaceContext namespaceContext;
		private final Node processor;
		private final String qualifier;
		private final String cost;
		private final Map<String, String> params;

		Builder(Node p, NamespaceContext nc, String qualifier, String cost) {
			this.processor = p;
			this.namespaceContext = nc;
			this.qualifier = qualifier;
			this.cost = cost;
			this.params = new HashMap<>();
		}
		
		Builder addParameter(String name, String value) {
			if (name!=null) {
				params.put(name, value);
			}
			return this;
		}
		
		RendererInfo build() {
			return new RendererInfo(this);
		}
	}
	private RendererInfo(Builder builder) {
		this.processor = builder.processor;
		this.namespaceContext = builder.namespaceContext;
		this.qualifier = builder.qualifier;
		this.cost = builder.cost;
		this.params = Collections.unmodifiableMap(new HashMap<>(builder.params));
	}

	Node getProcessor() {
		return processor;
	}

	String getQualifier() {
		return qualifier;
	}

	String getCost() {
		return cost;
	}

	NamespaceContext getNamespaceContext() {
		return namespaceContext;
	}
	
	Map<String, String> getParams() {
		return params;
	}
	
}
