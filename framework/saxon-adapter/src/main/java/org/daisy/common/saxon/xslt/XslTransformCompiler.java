package org.daisy.common.saxon.xslt;

import java.io.InputStream;
import java.util.Map;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;

import org.daisy.common.saxon.SaxonHelper;

/**
 * Immutable XSLT compiler with an optional URIResolver.
 */
public class XslTransformCompiler {

	private final URIResolver uriResolver;
	private final XsltCompiler compiler;

	public XslTransformCompiler(Configuration config) {
		this(config, null);
	}

	public XslTransformCompiler(Configuration config, URIResolver uriResolver) {
		this.uriResolver = uriResolver;
		compiler = new Processor(config).newXsltCompiler();
		if (uriResolver != null)
			compiler.setURIResolver(uriResolver);
	}

	public CompiledStylesheet compileStylesheet(InputStream stylesheet) throws SaxonApiException {
		return compileStylesheet(stylesheet, null);
	}

	public CompiledStylesheet compileStylesheet(InputStream stylesheet, Map<String,Object> parameters)
			throws SaxonApiException {
		try {
			if (parameters != null)
				for (Map.Entry<String,Object> param : parameters.entrySet())
					compiler.setParameter(
						new QName(null, param.getKey()),
						XdmValue.wrap(SaxonHelper.sequenceFromObject(param.getValue())));
			CompiledStylesheet cs = new CompiledStylesheet(
				compiler.compile(new StreamSource(stylesheet)));
			if (uriResolver != null)
				cs.setURIResolver(uriResolver);
			return cs;
		} finally {
			if (parameters != null)
				compiler.clearParameters();
		}
	}
}
