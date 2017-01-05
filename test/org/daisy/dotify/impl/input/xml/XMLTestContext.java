package org.daisy.dotify.impl.input.xml;

import java.net.URL;

import javax.xml.namespace.NamespaceContext;

@SuppressWarnings("javadoc")
public interface XMLTestContext {

	public NamespaceContext getNamespaceContext();
	
	public URL getNormalizationResource();
}
