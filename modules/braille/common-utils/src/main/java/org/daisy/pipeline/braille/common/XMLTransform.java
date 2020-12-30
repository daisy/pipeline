package org.daisy.pipeline.braille.common;

import java.util.Map;

import javax.xml.namespace.QName;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.StringWithNamespaceContext;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.transform.XMLTransformer;

public interface XMLTransform extends Transform {

	/**
	 * <p>Should return a transformer with:</p>
	 * <ul>
	 *   <li>a "source" input port that accepts a sequence of XML documents ({@link
	 *     XMLInputValue})</li>
	 *   <li>a "parameter" input port that accepts a map of parameter name/value pairs ({@link
	 *     InputValue}{@code <}{@link Map}{@code <}{@link QName}{@code ,}{@link
	 *     StringWithNamespaceContext}{@code >>})</li>
	 *   <li>a "result" output port that returns a sequence of XML documents ({@link
	 *     XMLOutputValue})</li>
	 * </ul>
	 */
	public XMLTransformer fromXmlToXml();

}