package org.daisy.common.xslt;

import java.io.InputStream;

import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XsltCompiler;

import com.xmlcalabash.core.XProcConfiguration;

// FIXME: this class should be moved to org.daisy.common.saxon

/**
 * Immutable XSLT compiler with an optional URIResolver.
 */
public class XslTransformCompiler {

	private URIResolver uriResolver;
	private XsltCompiler xsltCompiler;

	public XslTransformCompiler(Configuration config) {
		setConfiguration(config);
	}

	public XslTransformCompiler(XProcConfiguration config) {
		setConfiguration(config);
	}

	public XslTransformCompiler(Configuration config, URIResolver uriResolver) {
		this.uriResolver = uriResolver;
		setConfiguration(config);
	}

	public XslTransformCompiler(XProcConfiguration config, URIResolver uriResolver) {
		this.uriResolver = uriResolver;
		setConfiguration(config);
	}

	public CompiledStylesheet compileStylesheet(InputStream stylesheet)
	        throws SaxonApiException {

		CompiledStylesheet cs = new CompiledStylesheet(this.xsltCompiler
		        .compile(new StreamSource(stylesheet)));

		if (this.uriResolver != null)
			cs.setURIResolver(this.uriResolver);

		return cs;
	}

	private void setConfiguration(Configuration config) {
		initCompiler(config);
	}

	private void setConfiguration(XProcConfiguration config) {
		initCompiler(config.getProcessor().getUnderlyingConfiguration());
	}

	private void initCompiler(Configuration config) {
		this.xsltCompiler = new Processor(config).newXsltCompiler();
		if (this.uriResolver != null) {
			//this resolver is used for xsl:include and xsl:import
			this.xsltCompiler.setURIResolver(this.uriResolver);
		}
	}
}
