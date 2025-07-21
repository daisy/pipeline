package org.daisy.common.saxon.xslt;

import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Destination;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltTransformer;

import org.daisy.common.saxon.SaxonHelper;

/**
 * Transform a given XML tree or sub-tree, with optional XSLT parameters. The
 * methods must not be called within multiple threads.
 */
public class ThreadUnsafeXslTransformer {

	private final XsltTransformer transformer;
	private final Processor processor;

	ThreadUnsafeXslTransformer(XsltTransformer transformer, Processor processor) {
		this.transformer = transformer;
		this.processor = processor;
	}

	public XdmNode transform(XdmNode xml) throws SaxonApiException {
		return transform(xml, null, null);
	}

	/**
	 * The node '<xsl:output method="text"/>' should be specified in the XSLT to
	 * output regular text. However this method can be used for outputting
	 * serialized XML as well.
	 */
	public String transformToString(XdmNode xml) throws SaxonApiException {
		return transformToString(xml, null);
	}

	public XdmNode transform(XdmNode xml, URI outputBaseURI) throws SaxonApiException {
		return transform(xml, null, outputBaseURI);
	}

	public XdmNode transform(XdmNode xml, Map<String,Object> parameters)
	        throws SaxonApiException {
		return transform(xml, parameters, null);
	}

	/**
	 * Note that when {@code outputBaseURI} is not set, the output does not automatically
	 * inherit the base URI of the input, as one might expect.
	 */
	public XdmNode transform(XdmNode xml, Map<String,Object> parameters, URI outputBaseURI)
	        throws SaxonApiException {

		XdmDestination dest = new XdmDestination();
		if (outputBaseURI != null)
			dest.setBaseURI(outputBaseURI);
		genericTransform(xml, parameters, dest);
		return dest.getXdmNode();
	}

	public String transformToString(XdmNode xml, Map<String,Object> parameters)
	        throws SaxonApiException {
		Serializer dest = processor.newSerializer();
		StringWriter sw = new StringWriter();
		dest.setOutputWriter(sw);
		genericTransform(xml, parameters, dest);

		return sw.toString();
	}

	public void genericTransform(XdmNode input, Map<String,Object> parameters, Destination dest)
			throws SaxonApiException {

		try {
			if (parameters != null)
				for (Map.Entry<String,Object> param : parameters.entrySet())
					this.transformer.setParameter(new QName(null, param.getKey()),
					                              XdmValue.wrap(SaxonHelper.sequenceFromObject(param.getValue())));
			transformer.setSource(input.asSource());
			transformer.setDestination(dest);
			transformer.transform();
		} finally {
			if (parameters != null)
				transformer.clearParameters();
		}
	}

	public void setURIResolver(URIResolver uriResolver) {
		this.transformer.setURIResolver(uriResolver);
	}
}
