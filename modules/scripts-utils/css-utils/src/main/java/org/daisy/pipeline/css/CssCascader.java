package org.daisy.pipeline.css;

import javax.xml.namespace.QName;
import javax.xml.transform.URIResolver;

import org.daisy.common.transform.SingleInSingleOutXMLTransformer;

public interface CssCascader {

	public boolean supportsMedium(String medium);

	public SingleInSingleOutXMLTransformer newInstance(String medium,
	                                                   String defaultStylesheet,
	                                                   URIResolver uriResolver,
	                                                   SassCompiler sassCompiler,
	                                                   QName attributeName);

}
