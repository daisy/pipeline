package org.daisy.pipeline.css;

import javax.xml.namespace.QName;
import javax.xml.transform.URIResolver;

import org.daisy.common.transform.XMLTransformer;

/**
 * Used by the <a
 * href="http://daisy.github.io/pipeline/modules/css-utils/src/main/resources/xml/css-cascade.xpl"
 * class="apidoc"><code>px:css-cascade</code></a> step to inline CSS style sheets in XML.
 *
 * <p>The known implementations are:</p>
 * <ul>
 *   <li><a href="http://daisy.github.io/pipeline/modules/braille/braille-css-utils/src/main/README.html"
 *          class="apidoc">Medium <b>embossed</b></a></li>
 * </ul>
 */
public interface CssCascader {

	public boolean supportsMedium(Medium medium);

	public XMLTransformer newInstance(Medium medium,
	                                  String defaultStylesheet,
	                                  URIResolver uriResolver,
	                                  CssPreProcessor preProcessor,
	                                  XsltProcessor xsltProcessor,
	                                  QName attributeName);

}
