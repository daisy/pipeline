package org.daisy.common.xslt;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.XsltExecutable;

// FIXME: this class should be moved to org.daisy.common.saxon

/**
 * Allocate distinct ThreadUnsafeXslTransformer instances so that the same
 * transformation can be applied within multiple threads by invoking
 * newTransformer() in each thread.
 */
public class CompiledStylesheet {

	private XsltExecutable sheet;
	private URIResolver uriResolver;

	public CompiledStylesheet(XsltExecutable exec) {
		this.sheet = exec;
	}

	public void setURIResolver(URIResolver uriResolver) {
		this.uriResolver = uriResolver;
	}

	public ThreadUnsafeXslTransformer newTransformer() {
		ThreadUnsafeXslTransformer res = new ThreadUnsafeXslTransformer(this.sheet.load(), this.sheet.getProcessor());
		if (this.uriResolver != null)
			res.setURIResolver(this.uriResolver);
		return res;
	}
}
