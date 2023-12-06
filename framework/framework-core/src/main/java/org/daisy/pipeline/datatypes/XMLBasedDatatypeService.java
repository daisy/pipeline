package org.daisy.pipeline.datatypes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import com.google.common.collect.ImmutableList;

import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.rng.CompactSchemaReader;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltTransformer;

import org.daisy.common.file.URLs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class XMLBasedDatatypeService extends DatatypeService {

	private static final Logger logger = LoggerFactory.getLogger(XMLBasedDatatypeService.class);

	private static final URL TYPE_DECL_SCHEMA_URL
		= XMLBasedDatatypeService.class.getResource("/org/daisy/pipeline/datatypes/resources/data-type.rnc");
	private static final String TYPE_DECL_XSLT_PATH = "/org/daisy/pipeline/datatypes/resources/data-type.normalize-space.xsl";

	private boolean typeDeclRead = false;
	private Document typeDecl;
	private boolean typeDeclValid;
	private List<String> enumerationValues = null;
	private Pattern pattern = null;
	private Document document = null;

	public XMLBasedDatatypeService() {
		super();
	}

	public XMLBasedDatatypeService(String id) {
		super(id);
	}

	protected abstract Node readDocument() throws Exception;

	public static Document readDocument(URL url) throws ParserConfigurationException, SAXException, IOException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
	}

	public static Document readDocument(String xml) throws ParserConfigurationException, SAXException, IOException {
		return DocumentBuilderFactory.newInstance()
		                             .newDocumentBuilder()
		                             .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
	}

	@Override
	public Document asDocument() throws Exception {
		if (document == null) {
			Node doc = readDocument();

			// normalize space inside documentation elements
			StreamSource xslt = new StreamSource(XMLBasedDatatypeService.class.getResourceAsStream(TYPE_DECL_XSLT_PATH));
			if (doc instanceof NodeOverNodeInfo) {
				NodeInfo node = ((NodeOverNodeInfo)doc).getUnderlyingNodeInfo();
				Processor proc = (Processor)node.getConfiguration().getProcessor();
				XsltTransformer transformer = proc.newXsltCompiler().compile(xslt).load();
				XdmDestination result = new XdmDestination();
				transformer.setSource(new XdmNode(node).asSource());
				transformer.setDestination(result);
				transformer.transform();
				document = (Document)DocumentOverNodeInfo.wrap(result.getXdmNode().getUnderlyingNode());
			} else {
				DOMResult result = new DOMResult();
				TransformerFactory.newInstance().newTransformer(xslt).transform(new DOMSource(doc), result);
				document = (Document)result.getNode();
			}
		}
		return document;
	}

	@Override
	public ValidationResult validate(String content) {
		readTypeDecl();
		if (enumerationValues != null) {
			if (enumerationValues.contains(content)) {
				return ValidationResult.valid();
			} else {
				return ValidationResult.notValid("'" + content + "' is not in the list of allowed values.");
			}
		} else if (pattern != null) {
			if (pattern.matcher(content).matches())
				return ValidationResult.valid();
			else
				return ValidationResult.notValid(
					"'" + content + "' does not match the regular expression pattern /" + pattern + "/");
		} else if (typeDecl == null) {
			return ValidationResult.notValid("Could not parse type declaration");
		} else if (!typeDeclValid) {
			return ValidationResult.notValid("Don't know how to read custom type declaration.");
		} else {
			return ValidationResult.notValid("Not implemented");
		}
	}

	/**
	 * Whether the datatype is an enumeration (defined with the "choice" element)
	 *
	 * <pre>
	 *    &lt;choice&gt;
	 *       &lt;value&gt;a&lt;/value&gt;
	 *       &lt;value&gt;b&lt;/value&gt;
	 *       &lt;value&gt;c&lt;/value&gt;
	 *    &lt;/choice&gt;
	 * </pre>
	 */
	public boolean isEnumeration() {
		readTypeDecl();
		return enumerationValues != null;
	}

	/**
	 * Whether the datatype is a regular expression pattern
	 *
	 * <pre>
	 &lt;data type="string"&gt;
	 &lt;param name="pattern"&gt;^a|b|c$&lt;/param&gt;
	 &lt;/data&gt;
	 * </pre>
	 */
	public boolean isPattern() {
		readTypeDecl();
		return pattern != null;
	}

	/**
	 * Get all the values of the enumeration.
	 *
	 * @throws UnsupportedOperationException if the datatype is not an enumeration.
	 */
	public List<String> getEnumerationValues() throws UnsupportedOperationException {
		readTypeDecl();
		if (enumerationValues == null) {
			throw new RuntimeException("Datatype is not an enumeration");
		}
		return enumerationValues;
	}

	/**
	 * Get the regular expression pattern
	 *
	 * @throws UnsupportedOperationException if the datatype is not an pattern.
	 */
	public Pattern getPattern() throws UnsupportedOperationException {
		readTypeDecl();
		if (pattern == null) {
			throw new RuntimeException("Datatype is not a pattern");
		}
		return pattern;
	}

	private void readTypeDecl() {
		if (typeDeclRead)
			return;
		try {
			typeDecl = asDocument();
			typeDeclValid = validateTypeDecl(typeDecl);
			if (typeDeclValid) {
				Element elem = typeDecl.getDocumentElement();
				if (elem.getTagName().equals("choice")) { // <choice><value>...</value><value>...</value>...</choice>
					ImmutableList.Builder<String> b = ImmutableList.builder();
					for (Node n : iterateNodeList(elem.getElementsByTagName("value"))) {
						b.add(((Element)n).getTextContent());
					}
					enumerationValues = b.build();
				// ignoring other child elements than <value>
				} else if (elem.getTagName().equals("data")) { // <data type="string"><param name="pattern">...</param></data>
					// assuming the type attribute is "string" and ignoring other attributes
					for (Node n : iterateNodeList(elem.getElementsByTagName("param"))) {
						// assuming the name attribute is "pattern" and ignoring other attributes
						pattern = Pattern.compile(((Element)n).getTextContent());
						// assuming there is only one <param>
						break;
					}
					if (pattern == null)
						throw new RuntimeException("Expected 'param' child element");
				// ignoring other child elements than <param>
				} else {
					throw new RuntimeException("Unexpected element: " + elem.getTagName());
				}
			}
		} catch (Exception e) {
			logger.error("Error while parsing type declaration", e);
		}
		typeDeclRead = true;
	}

	private static boolean validateTypeDecl(Document document) {
		ErrorHandlerImpl errorHandler = new ErrorHandlerImpl();
		ValidationDriver typeDeclValidator; {
			PropertyMapBuilder properties = new PropertyMapBuilder(); {
				properties.put(ValidateProperty.ERROR_HANDLER, errorHandler);
				properties.put(ValidateProperty.RESOLVER, BasicResolver.getInstance());
			}
			typeDeclValidator = new ValidationDriver(properties.toPropertyMap(), CompactSchemaReader.getInstance());
			try {
				InputSource schemaInputSource = new InputSource(TYPE_DECL_SCHEMA_URL.toURI().toString());
				if (!typeDeclValidator.loadSchema(schemaInputSource)) {
					logger.error("Could not load schema " + TYPE_DECL_SCHEMA_URL.toString());
					throw new RuntimeException("Could not load schema " + TYPE_DECL_SCHEMA_URL.toString());
				}
			} catch (URISyntaxException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (SAXException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			InputSource documentInputSource = DOMtoInputSource(document);
			typeDeclValidator.validate(documentInputSource);
		} catch (TransformerConfigurationException e) {
			logger.error(e.getMessage());
			return false;
		} catch (TransformerException e) {
			logger.error(e.getMessage());
			return false;
		} catch (TransformerFactoryConfigurationError e) {
			logger.error(e.getMessage());
			return false;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		} catch (SAXException e) {
			logger.error(e.getMessage());
			return false;
		}
		if (errorHandler.hasErrors()) {
			for (SAXParseException e : errorHandler.errors) {
				logger.error(e.getMessage() + "(" + e.getLineNumber() + ", " + e.getColumnNumber() + ")");
			}
		}
		return !errorHandler.hasErrors();
	}

	private static Iterable<Node> iterateNodeList(NodeList nodeList) {
		List<Node> list = new ArrayList<Node>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			list.add(nodeList.item(i));
		}
		return list;
	}

	/*
	 * The following functions and classes are copied from org.daisy.pipeline.webservice.xml.XmlValidator
	 * TODO: move to common package(s)
	 */

	private static InputSource DOMtoInputSource(Document doc)
		throws TransformerConfigurationException, TransformerException, TransformerFactoryConfigurationError {

		DOMSource source = new DOMSource(doc);
		StringWriter xmlAsWriter = new StringWriter();
		StreamResult result = new StreamResult(xmlAsWriter);
		TransformerFactory.newInstance().newTransformer().transform(source, result);
		StringReader xmlReader = new StringReader(xmlAsWriter.toString());
		InputSource is = new InputSource(xmlReader);
		return is;
	}

	private static class ErrorHandlerImpl implements ErrorHandler {

		List<SAXParseException> errors = new ArrayList<SAXParseException>();

		public boolean hasErrors() {
			return errors.size() > 0;
		}

		@Override
		public void error(SAXParseException exception) throws SAXException {
			errors.add(exception);
		}

		@Override
		public void fatalError(SAXParseException exception) throws SAXException {
			errors.add(exception);
		}

		@Override
		public void warning(SAXParseException exception) throws SAXException {
			errors.add(exception);
		}
	}

	/**
	 * Basic Resolver from Jing modified to add support for resolving zip and
	 * jar relative locations.
	 *
	 * @author george@oxygenxml.com
	 */
	private static class BasicResolver implements Resolver {

		static private final BasicResolver theInstance = new BasicResolver();

		protected BasicResolver() {
		}

		public static BasicResolver getInstance() {
			return theInstance;
		}

		@Override
		public void resolve(Identifier id, Input input) throws IOException,
		                                                       ResolverException {
			if (!input.isResolved()) {
				input.setUri(resolveUri(id));
			}
		}

		@Override
		public void open(Input input) throws IOException, ResolverException {
			if (!input.isUriDefinitive()) {
				return;
			}
			URI uri;
			try {
				uri = URLs.asURI(input.getUri());
			} catch (IllegalArgumentException e) {
				throw new ResolverException(e);
			}
			if (!uri.isAbsolute()) {
				throw new ResolverException("cannot open relative URI: " + uri);
			}
			URL url = URLs.asURL(uri);
			// XXX should set the encoding properly
			// XXX if this is HTTP and we've been redirected, should do
			// input.setURI with the new URI
			input.setByteStream(url.openStream());
		}

		public static String resolveUri(Identifier id) throws ResolverException {
			try {
				String uriRef = id.getUriReference();
				URI uri = new URI(uriRef);
				if (!uri.isAbsolute()) {
					String base = id.getBase();
					if (base != null) {
						// OXYGEN PATCH START
						// Use class URL in order to resolve protocols like zip
						// and jar.
						URI baseURI = new URI(base);
						if ("zip".equals(baseURI.getScheme())
						    || "jar".equals(baseURI.getScheme())) {
							uriRef = new URL(new URL(base), uriRef)
								.toExternalForm();
							// OXYGEN PATCH END
						} else {
							uriRef = baseURI.resolve(uri).toString();
						}
					}
				}
				return uriRef;
			} catch (URISyntaxException e) {
				throw new ResolverException(e);
			} catch (MalformedURLException e) {
				throw new ResolverException(e);
			}
		}
	}
}
