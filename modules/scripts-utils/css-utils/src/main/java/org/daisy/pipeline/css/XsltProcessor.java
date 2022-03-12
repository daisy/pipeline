package org.daisy.pipeline.css;

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.XMLInputValue;

/**
 * Processor of @xslt rules
 */
public interface XsltProcessor {

	/**
	 * @param stylesheetURI XSLT style sheet as absolute URI
	 */
	public XMLInputValue<Void> transform(URI stylesheetURI,
	                                     XMLInputValue<?> source,
	                                     Map<QName,InputValue<?>> parameters);

}
