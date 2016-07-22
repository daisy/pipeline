package org.daisy.common.xslt;

import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

/**
 * Transform a given XML tree or sub-tree, with optional XSLT parameters. The
 * methods must not be called within multiple threads.
 */
public class ThreadUnsafeXslTransformer {

	private XsltTransformer transformer;

	public ThreadUnsafeXslTransformer(XsltTransformer transformer) {
		this.transformer = transformer;
	}

	public XdmNode transform(XdmNode xml) throws SaxonApiException {
		return transform(xml, null);
	}

	/**
	 * The node '<xsl:output method="text"/>' should be specified in the XSLT to
	 * output regular text. However this method can be used for outputting
	 * serialized XML as well.
	 */
	public String transformToString(XdmNode xml) throws SaxonApiException {
		return transformToString(xml, null);
	}

	public XdmNode transform(XdmNode xml, Map<String, Object> parameters)
	        throws SaxonApiException {

		XdmDestination dest = new XdmDestination();
		genericTransform(xml, parameters, dest);
		return dest.getXdmNode();
	}

	public String transformToString(XdmNode xml, Map<String, Object> parameters)
	        throws SaxonApiException {
		Serializer dest = new Serializer();
		StringWriter sw = new StringWriter();
		dest.setOutputWriter(sw);
		genericTransform(xml, parameters, dest);

		return sw.toString();
	}

	public void genericTransform(XdmNode input, Map<String, Object> parameters,
	        Destination dest) throws SaxonApiException {

		if (parameters != null) {
			for (Map.Entry<String, Object> param : parameters.entrySet()) {
				this.transformer.setParameter(new QName(null, param.getKey()),
				        new XdmAtomicValue(param.getValue().toString()));
			}
		}
		this.transformer.setSource(input.asSource());
		this.transformer.setDestination(dest);
		this.transformer.transform();

		if (parameters != null) {
			//cancel the parameters
			for (Map.Entry<String, Object> param : parameters.entrySet()) {
				this.transformer.setParameter(new QName(null, param.getKey()), null);
			}
		}
	}

	public void setURIResolver(URIResolver uriResolver) {
		this.transformer.setURIResolver(uriResolver);
	}
}
