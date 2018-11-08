package org.daisy.common.saxon;

import java.util.function.Supplier;
import java.util.Iterator;

import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.transform.TransformerException;

import net.sf.saxon.s9api.XdmNode;

public interface NodeToXMLStreamTransformer {
	
	public void transform(Iterator<XdmNode> input, Supplier<BaseURIAwareXMLStreamWriter> output) throws TransformerException;
	
}