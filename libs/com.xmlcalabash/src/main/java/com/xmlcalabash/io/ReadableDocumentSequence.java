package com.xmlcalabash.io;

import com.xmlcalabash.model.Log;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

public interface ReadableDocumentSequence {
	public XdmNode get(int count) throws SaxonApiException;
	public int size() throws SaxonApiException;
	public void setLogger(Log log);
}
