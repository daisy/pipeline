package org.daisy.common.transform;

import java.util.function.Supplier;
import java.util.Iterator;

import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;

/*
 * Note that this interface does not extend javax.xml.transform.Transformer.
 */
public interface XMLStreamToXMLStreamTransformer {
	
	/**
	 * Transform a sequence of input documents to a sequence of output documents.
	 *
	 * @param input A sequence of XMLStreamReaders. Allowed to throw TransformerException.
	 * @param output A supplier of XMLStreamWriters. Allowed to throw TransformerException.
	 * @throws TransformerException
	 */
	public void transform(Iterator<BaseURIAwareXMLStreamReader> input, Supplier<BaseURIAwareXMLStreamWriter> output)
		throws TransformerException;
	
}
