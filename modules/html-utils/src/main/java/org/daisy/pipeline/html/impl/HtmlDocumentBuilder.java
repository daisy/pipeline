package org.daisy.pipeline.html.impl;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;

import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import nu.validator.htmlparser.common.XmlViolationPolicy;

import org.daisy.common.file.URLs;

import org.daisy.common.saxon.xslt.XslTransformCompiler;
import org.daisy.common.xml.DocumentBuilder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component(
	name = "html-document-builder",
	service = { DocumentBuilder.class }
)
public class HtmlDocumentBuilder implements DocumentBuilder {

	private final static String MIME_HTML = "text/html";
	private final static String MIME_XHTML = "application/xhtml+xml";

	/**
	 * Test whether the given content media type is "text/html" or "application/xhtml+xml".
	 */
	@Override
	public boolean supportsContentType(String type) {
		if (MIME_HTML.equals(type))
			return true;
		else if (xmlParser != null && MIME_XHTML.equals(type))
			return true;
		else
			return false;
	}

	/**
	 * @param input an HTML file
	 */
	@Override
	public Document parse(InputSource input) throws IOException, SAXException {
		if (xmlParser != null)
			try {
				return xmlParser.parse(input);
			} catch (SAXException|IllegalArgumentException e) {
				// ignore
			}
		URI baseURI; {
			try {
				baseURI = getBaseURI(input);
			} catch (IllegalArgumentException e) {
				if (input.getCharacterStream() == null && input.getByteStream() == null)
					throw new IOException("Error reading input: invalid URI", e);
				else
					throw e;
			}
		}
		if (input.getCharacterStream() == null && input.getByteStream() == null) {
			// resolve URI
			if (baseURI == null)
				throw new IOException(
					"Error reading input: no character or byte stream available and no base URI");
			Source s; {
				try {
					s = uriResolver.resolve(baseURI.toASCIIString(), null);
				} catch (TransformerException e) {
					throw new IOException("Error reading input: " + baseURI, e);
				}
			}
			if (s != null) {
				input = SAXSource.sourceToInputSource(s);
				if (input == null)
					throw new IOException("Error reading input: " + baseURI);
			}
		}
		Reader reader = input.getCharacterStream();
		String text;
		byte[] bytes = null;
		boolean checkEncoding = false;;
		if (reader != null)
			text = CharStreams.toString(reader);
		else {
			checkEncoding = true;
			bytes = readBytes(input);
			text = new String(bytes, StandardCharsets.UTF_8);
		}
		text = removeDoctype(text);
		Document doc = readHtml(text);
		if (checkEncoding) {
			for (Node n : iterateNodeList(doc.getDocumentElement().getElementsByTagName("meta"))) {
				Element elem = (Element)n;
				Attr httpEquiv = elem.getAttributeNode("http-equiv");
				if (httpEquiv != null && "Content-Type".equals(httpEquiv.getValue())) {
					Attr contentType = elem.getAttributeNode("content");
					if (contentType != null) {
						String charset = contentType.getValue()
						                            .replaceAll(".*?charset\\s*=\\s*('(.*?)'|\"(.*?)\"|([^'\"\\s][^\\s;]*)|.).*",
						                                        "$2$3$4");
						if (!charset.equals(contentType.getValue())) {
							Charset encoding; {
								try {
									encoding = Charset.forName(charset.toLowerCase());
								} catch (IllegalCharsetNameException e) {
									throw new IOException("Invalid charset: " + charset, e);
								} catch (UnsupportedCharsetException e) {
									throw new IOException("Unsupported charset: " + charset, e);
								}
							}
							if (!StandardCharsets.UTF_8.equals(encoding)) {
								text = new String(bytes, encoding);
								text = removeDoctype(text);
								doc = readHtml(text);
							}
						}
						break;
					}
				}
			}
		}
		// use Saxon to apply namespace-fixup.xsl and set base URI
		try {
			XdmNode saxonDoc = saxonProcessor.newDocumentBuilder().build(new DOMSource(doc));
			saxonDoc = new XslTransformCompiler(saxonProcessor.getUnderlyingConfiguration())
			    .compileStylesheet(URLs.getResourceFromJAR("/xml/xslt/namespace-fixup.xsl", HtmlDocumentBuilder.class)
			                           .openStream())
			    .newTransformer()
			    .transform(saxonDoc);
			if (baseURI != null) {
				// for some reason setting the output base URI on the XSLT transformation doesn't work properly
				// as a workaround use XMLCalabash's TreeWriter to set the base URI
				TreeWriter fixbase = new TreeWriter(saxonProcessor);
				fixbase.startDocument(baseURI);
				fixbase.addSubtree(saxonDoc);
				fixbase.endDocument();
				saxonDoc = fixbase.getResult();
			}
			doc = (Document)DocumentOverNodeInfo.wrap(saxonDoc.getUnderlyingNode());
		} catch (IOException|SaxonApiException e) {
			throw new SAXException("Error parsing input", e);
		}
		return doc;
	}

	/**
	 * @return {@code null} if the input has no base URI
	 * @throws IllegalArgumentException if the input has an invalid base URI
	 */
	private static URI getBaseURI(InputSource input) throws IllegalArgumentException {
		String sysId = input.getSystemId();
		if (sysId == null || "".equals(sysId))
			return null;
		try {
			return URI.create(sysId);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid base URI: " + sysId, e);
		}
	}

	private static byte[] readBytes(InputSource input) throws IOException {
		if (input.getCharacterStream() != null)
			throw new IllegalArgumentException();
		InputStream s = input.getByteStream();
		if (s == null) {
			URI uri; {
				try {
					uri = getBaseURI(input);
					if (uri == null)
						throw new IOException(
							"Error reading input: no character or byte stream available and no base URI");
				} catch (IllegalArgumentException e) {
					throw new IOException("Error reading input: invalid URI", e);
				}
			}
			URL url; {
				try {
					url = uri.toURL();
				} catch (IllegalArgumentException e) {
					// not absolute
					throw new IOException( "Error reading input: not an absolute URI: " + uri, e);
				} catch (MalformedURLException e) {
					throw new IOException( "Error reading input: malformed URL: " + uri, e);
				}
			}
			s = url.openStream();
		}
		return ByteStreams.toByteArray(s);
	}

	private static String removeDoctype(String input) {
		return Pattern.compile("^<[!\\?].*?(<[^!\\?])", Pattern.DOTALL).matcher(input).replaceFirst("$1");
	}

	private Document readHtml(String input) throws SAXException, IOException {
		nu.validator.htmlparser.dom.HtmlDocumentBuilder builder
			= new nu.validator.htmlparser.dom.HtmlDocumentBuilder(XmlViolationPolicy.ALTER_INFOSET);
		builder.setEntityResolver(entityResolver);
		try {
			return builder.parse(new InputSource(new StringReader(input)));
		} catch (IOException e) {
			throw e;
		} catch (SAXException e) {
			throw e;
		}
	}

	private static Iterable<Node> iterateNodeList(NodeList nodeList) {
		int len = nodeList.getLength();
		return new Iterable<Node>() {
			@Override
			public Iterator<Node> iterator() {
				return new Iterator() {
					int i = 0;
					@Override
					public boolean hasNext() {
						return i < len;
					}
					@Override
					public Node next() throws NoSuchElementException {
						if (!hasNext())
							throw new NoSuchElementException();
						return nodeList.item(i++);
					}
				};
			}
		};
	}

	private DocumentBuilder xmlParser = null;
	private URIResolver uriResolver = null;
	private EntityResolver entityResolver = null;
	private Processor saxonProcessor = null;

	// FIXME: this is brittle: it will be a cyclic dependency if at least one other DocumentBuilder
	// implementation would bind DocumentBuilder
	@Reference(
		name = "xml-parser",
		unbind = "-",
		service = DocumentBuilder.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void setXmlParser(DocumentBuilder parser) {
		if (xmlParser == null && parser.supportsContentType(MIME_XHTML))
			xmlParser = parser;
	}

	@Reference(
		name = "uri-resolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setURIResolver(URIResolver resolver) {
		uriResolver = resolver;
	}

	@Reference(
		name = "entity-resolver",
		unbind = "-",
		service = EntityResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setEntityResolver(EntityResolver resolver) {
		entityResolver = resolver;
	}

	@Reference(
		name = "saxon-processor",
		unbind = "-",
		service = Processor.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setSaxonProcessor(Processor processor) {
	    saxonProcessor = processor;
	}
}
