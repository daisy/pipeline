package org.daisy.common.transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class StringWithNamespaceContext extends InputValue<String> implements NamespaceContext {

	private final String value;
	private final Map<String,String> namespaces;
	private Map<String,String> prefixes;

	public StringWithNamespaceContext(String value, Map<String,String> bindings) {
		super(value);
		this.value = value;
		namespaces = Collections.unmodifiableMap(bindings);
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public String getNamespaceURI(String prefix) {
		return namespaces != null ? namespaces.get(prefix) : XMLConstants.NULL_NS_URI;
	}

	@Override
	public String getPrefix(String namespaceURI) {
		if (prefixes == null && namespaces != null) {
			prefixes = new HashMap<String,String>();
			for (Map.Entry<String,String> e : namespaces.entrySet())
				prefixes.put(e.getValue(), e.getKey());
		}
		return prefixes != null ? prefixes.get(namespaceURI) : null;
	}

	@Override
	public Iterator<String> getPrefixes(String namespaceURI) {
		if (namespaces == null)
			return Collections.emptyIterator();
		List<String> prefixes = new ArrayList<String>();
		for (String prefix : namespaces.keySet())
			if (namespaces.get(prefix).equals(namespaceURI))
				prefixes.add(prefix);
		return prefixes.iterator();
	}

	public Iterator<String> getPrefixes() {
		return namespaces.keySet().iterator();
	}

	public Map<String,String> getNamespaceBindingsAsMap() {
		return namespaces;
	}
}
