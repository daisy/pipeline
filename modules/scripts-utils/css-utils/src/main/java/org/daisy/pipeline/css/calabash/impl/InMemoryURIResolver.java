package org.daisy.pipeline.css.calabash.impl;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.util.Base64;
import com.xmlcalabash.util.S9apiUtils;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * URI resolver that can resolve in-memory documents
 */
class InMemoryURIResolver implements URIResolver {

	private ReadablePipe documents = null;

	InMemoryURIResolver() {}

	void setContext(ReadablePipe documents) {
		this.documents = documents;
	}

	private static final QName c_encoding = new QName("c", XProcConstants.NS_XPROC_STEP, "encoding");
	private static final QName _content_type = new QName("content-type");

	public Source resolve(String href, String base) throws TransformerException {
		if (documents == null) return null;
		try {
			URI uri = (base != null) ?
				new URI(base).resolve(new URI(href)) :
				new URI(href);
			uri = normalizeUri(uri);
			documents.resetReader();
			while (documents.moreDocuments()) {
				XdmNode doc = documents.read();
				URI docUri = normalizeUri(doc.getBaseURI());
				if (docUri.equals(uri)) {
					XdmNode root = S9apiUtils.getDocumentElement(doc);
					if (XProcConstants.c_result.equals(root.getNodeName())
					    && root.getAttributeValue(_content_type) != null
					    && root.getAttributeValue(_content_type).startsWith("text/"))
						return new StreamSource(new ByteArrayInputStream(doc.getStringValue().getBytes()),
						                        uri.toASCIIString());
					else if ("base64".equals(root.getAttributeValue(c_encoding)))
						return new StreamSource(new ByteArrayInputStream(Base64.decode(doc.getStringValue())),
						                        uri.toASCIIString());
					else if (XProcConstants.c_data.equals(root.getNodeName()))
						return new StreamSource(new ByteArrayInputStream(doc.getStringValue().getBytes()),
						                        uri.toASCIIString());
					else
						return doc.asSource(); }}}
		catch (URISyntaxException e) {
			e.printStackTrace();
			throw new TransformerException(e); }
		catch (SaxonApiException e) {
			e.printStackTrace();
			throw new TransformerException(e); }
		return null;
	}

	private static URI normalizeUri(URI uri) {
		String s = uri.toASCIIString();

		// A URI that starts with "file:///" can be written as "file:/"
		if (s.startsWith("file:///"))
			s = "file:" + s.substring(7);

		// A URI that contains "!/" is a ZIP URI
		if (s.startsWith("file:") && s.contains("!/"))
			s = "zip:" + s;

		// The part of a ZIP URI after the "!" must start with "/"
		if (s.startsWith("zip:file:") && !s.contains("!/"))
			s = s.replaceFirst("!", "!/");
		try {
			return new URI(s); }
		catch (URISyntaxException e) {
			throw new RuntimeException(e); }
	}
}
