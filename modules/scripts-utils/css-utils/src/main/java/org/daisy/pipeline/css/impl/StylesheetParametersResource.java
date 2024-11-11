package org.daisy.pipeline.css.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.css.CssAnalyzer;
import org.daisy.pipeline.css.CssAnalyzer.SassVariable;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.css.UserAgentStylesheetRegistry;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.job.ZippedJobResources;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;
import org.daisy.pipeline.webservice.restlet.MultipartRequestData;
import org.daisy.pipeline.webservice.xml.XmlUtils;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class StylesheetParametersResource extends AuthenticatedResource {

	static final String URI_RESOLVER_KEY = "uri-resolver";
	static final String DATATYPE_REGISTRY_KEY = "datatype-registry";
	static final String USER_AGENT_STYLESHEET_REGISTRY_KEY = "user-agent-stylesheet-registry";

	private static final String STYLESHEET_PARAMETERS_DATA_FIELD = "stylesheet-parameters-data";
	private static final String STYLESHEET_PARAMETERS_REQUEST_FIELD = "stylesheet-parameters-request";
	private static final URL STYLESHEET_PARAMETERS_REQUEST_SCHEMA_URL
		= URLs.getResourceFromJAR("rnc/stylesheetParametersRequest.rnc", StylesheetParametersResource.class);
	private static final String NS_DAISY = "http://www.daisy.org/ns/pipeline/data";
	private static final Logger logger = LoggerFactory.getLogger(StylesheetParametersResource.class.getName());

	private URIResolver uriResolver;
	private DatatypeRegistry datatypeRegistry;
	private UserAgentStylesheetRegistry userAgentStylesheetRegistry;

	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated())
			return;
		uriResolver = (URIResolver)getContext().getAttributes().get(URI_RESOLVER_KEY);
		datatypeRegistry = (DatatypeRegistry)getContext().getAttributes().get(DATATYPE_REGISTRY_KEY);
		userAgentStylesheetRegistry
			= (UserAgentStylesheetRegistry)getContext().getAttributes().get(USER_AGENT_STYLESHEET_REGISTRY_KEY);
	}

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Post
	public Representation getResource(Representation representation) {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		if (representation == null) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation("POST request with no entity");
		}
		Document request = null;
		ZippedJobResources data; {
			if (MediaType.MULTIPART_FORM_DATA.equals(representation.getMediaType(), true)) {
				MultipartRequestData multi = null;
				try {
					multi = MultipartRequestData.processMultipart(getRequest(),
					                                              STYLESHEET_PARAMETERS_DATA_FIELD,
					                                              STYLESHEET_PARAMETERS_REQUEST_FIELD,
					                                              new File(getConfiguration().getTmpDir()));
				} catch (Exception e) {
					return badRequest(e);
				}
				if (multi == null) {
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation("Multipart data is empty");
				}
				request = multi.getXml();
				data = new ZippedJobResources(multi.getZipFile());
			} else {
				data = null;
				try {
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					factory.setNamespaceAware(true);
					DocumentBuilder builder = factory.newDocumentBuilder();
					request = builder.parse(new InputSource(new StringReader(representation.getText())));
				} catch (IOException|ParserConfigurationException|SAXException e) {
					return badRequest(e);
				}
			}
		}
		if (logger.isDebugEnabled())
			logger.debug(XmlUtils.nodeToString(request));
		if (!XmlValidator.validate(request, STYLESHEET_PARAMETERS_REQUEST_SCHEMA_URL)) {
			setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return getErrorRepresentation("The request is not valid");
		}

		// using ScriptInput.Builder to validate URIs and convert them to Source objects
		ScriptInput inputs; {
			ScriptInput.Builder builder = new ScriptInput.Builder(data);
			NodeList userStylesheets = request.getElementsByTagNameNS(NS_DAISY, "userStylesheets");
			if (userStylesheets.getLength() > 0) {
				NodeList files = ((Element)userStylesheets.item(0)).getElementsByTagNameNS(NS_DAISY, "file");
				for (int i = 0; i < files.getLength(); i++) {
					Element file = (Element)files.item(i);
					URI href = URI.create(file.getAttribute("href"));
					try {
						if ("file".equals(href.getScheme()) && !getConfiguration().isLocalFS())
							throw new FileNotFoundException(
								"WS does not allow local inputs but a href starting with 'file:' was found: " + href);
						else if ("http".equals(href.getScheme()) || "https".equals(href.getScheme()))
							builder.withInput("stylesheet",
							                  new ByteArrayInputStream(
								                  ("@import url(\"" + href + "\");").getBytes(StandardCharsets.UTF_8)));
						else
							builder.withInput("stylesheet", href);
					} catch (IllegalArgumentException|FileNotFoundException e) {
						return badRequest(e);
					}
				}
			}
			NodeList sourceDocument = request.getElementsByTagNameNS(NS_DAISY, "sourceDocument");
			if (sourceDocument.getLength() > 0) {
				NodeList file = ((Element)sourceDocument.item(0)).getElementsByTagNameNS(NS_DAISY, "file");
				URI href = URI.create(((Element)file.item(0)).getAttribute("href"));
				try {
					if ("file".equals(href.getScheme()) && !getConfiguration().isLocalFS())
						throw new FileNotFoundException(
							"WS does not allow local inputs but a href starting with 'file:' was found: " + href);
					builder.withInput("source", href);
				} catch (IllegalArgumentException|FileNotFoundException e) {
					return badRequest(e);
				}
			}
			inputs = builder.build();
		}
		List<Medium> media; {
			media = null;
			NodeList node = request.getElementsByTagNameNS(NS_DAISY, "media");
			if (node.getLength() > 0)
				try {
					media = Medium.parseMultiple(((Element)node.item(0)).getAttribute("value"));
				} catch (IllegalArgumentException e) {
					return badRequest(e);
				}
			if (media == null)
				media = Medium.parseMultiple("screen");
		}
		URI contextBase = URI.create("context:/");
		uriResolver = fallback(uriResolver, simpleURIResolver);
		URIResolver resolver = data == null
			? uriResolver
			: new URIResolver() {
					@Override
					public Source resolve(String href, String base) throws TransformerException {
						URI hrefURI = URLs.asURI(href);
						if (href.startsWith(contextBase.toString())
						    || (base != null && base.startsWith(contextBase.toString())
						        && !(hrefURI.isAbsolute() || hrefURI.getSchemeSpecificPart().startsWith("/")))) {
							if (!href.startsWith(contextBase.toString()))
								hrefURI = URLs.resolve(URLs.asURI(base), hrefURI);
							try {
								URL u = URLs.asURL(hrefURI);
								return new StreamSource(u.openStream(), // implemented in custom URLStreamHandler below
								                        hrefURI.toString());
							} catch (IOException e) {
								throw new TransformerException(e);
							}
						}
						return uriResolver.resolve(href, base);
					}
				};
		URLStreamHandlerFactory resetURLStreamHandlerFactory = null;
		if (data != null)
			resetURLStreamHandlerFactory = setURLStreamHandlerFactory(
				new URLStreamHandlerFactory() {
					@Override
					public URLStreamHandler createURLStreamHandler(String protocol) {
						if ("context".equals(protocol))
							return new URLStreamHandler() {
								@Override
								protected URLConnection openConnection(URL u) throws IOException {
									return new URLConnection(u) {
										@Override
										public void connect() throws IOException {
										}
										@Override
										public InputStream getInputStream() throws IOException {
											Supplier<InputStream> s = data.getResource(URLs.relativize(contextBase, URLs.asURI(u)).getPath());
											if (s == null)
												throw new FileNotFoundException();
											return s.get();
										}
									};
								}
							};
						else
							return null;
					}
				}
			);
		try {
			Function<Source,Source> handleZippedInput = s -> {
				if (s.getSystemId() == null)
					// this means we used a InputStream
					return s;
				URI u = URLs.asURI(s.getSystemId());
				if (u.getScheme() == null)
					// we know that u is a relative URI and that data contains the file
					u = URLs.resolve(contextBase, u);
				try {
					return resolver.resolve(u.toString(), "");
				} catch (TransformerException e) {
					throw new RuntimeException(e);
				}
			};
			List<Source> userAndUserAgentStylesheets = new ArrayList<>(); {
				NodeList userAgentStylesheet = request.getElementsByTagNameNS(NS_DAISY, "userAgentStylesheet");
				if (userAgentStylesheet.getLength() > 0) {
					for (URL u : userAgentStylesheetRegistry.get(
					                 Collections.singleton("text/x-scss"),
					                 Arrays.asList(((Element)userAgentStylesheet.item(0)).getAttribute("mediaType").trim().split("\\s+")),
					                 media))
						try {
							userAndUserAgentStylesheets.add(simpleURIResolver.resolve(u.toString(), null));
						} catch (TransformerException e) {
							throw new RuntimeException(e);
						}
				}
				for (Source s : Iterables.transform(inputs.getInput("stylesheet"), handleZippedInput))
					userAndUserAgentStylesheets.add(s);
			}
			Source sourceDocument = Iterables.getFirst(Iterables.transform(inputs.getInput("source"), handleZippedInput), null);
			Document parametersDoc; {
				parametersDoc = XmlUtils.createDom("parameters");
				Element parametersElem = parametersDoc.getDocumentElement();
				try {
					for (SassVariable v : new CssAnalyzer(media, resolver, datatypeRegistry)
						                      .analyze(userAndUserAgentStylesheets, sourceDocument)
					                          .getVariables()) {
						if (v.isDefault()) {
							Element parameterElem = parametersDoc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "parameter");
							parameterElem.setAttribute("name", v.getName());
							parameterElem.setAttribute("nicename", v.getNiceName());
							parameterElem.setAttribute("description", v.getDescription());
							parameterElem.setAttribute("default", v.getValue());
							parameterElem.setAttribute("type", v.getType().getId());
							parameterElem.setAttribute("required", "false");
							parameterElem.setAttribute("sequence", "false");
							parameterElem.setAttribute("ordered", "false");
							parametersElem.appendChild(parameterElem);
						}
					}
				} catch (IOException e) {
					return badRequest(e);
				} catch (DOMException e) { // should not happen
					setStatus(Status.SERVER_ERROR_INTERNAL);
					return getErrorRepresentation(e.getMessage());
				}
				DomRepresentation dom = new DomRepresentation(MediaType.APPLICATION_XML, parametersDoc);
				setStatus(Status.SUCCESS_OK);
				logResponse(dom);
				return dom;
			}
		} finally {
			if (data != null)
				setURLStreamHandlerFactory(resetURLStreamHandlerFactory);
		}
	}

	private Representation badRequest(Exception e) {
		logger.error("bad request:", e);
		setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		return getErrorRepresentation(e.getMessage());
	}

	private static URIResolver simpleURIResolver = new URIResolver() {
			@Override
			public Source resolve(String href, String base) throws TransformerException {
				try {
					URI uri; {
						if (base != null)
							uri = new URI(base).resolve(new URI(href));
						else
							uri = new URI(href);
					}
					return new SAXSource(new InputSource(uri.toASCIIString()));
				} catch (URISyntaxException e) {
					throw new TransformerException(e);
				}
			}
		};

	private static URIResolver fallback(URIResolver... resolvers) {
		return new URIResolver() {
			@Override
			public Source resolve(String href, String base) throws TransformerException {
				Source source = null;
				Iterator<URIResolver> iterator = Iterators.forArray(resolvers);
				while (iterator.hasNext()) {
					source = iterator.next().resolve(href, base);
					if (source != null)
						break; }
				return source;
			}
		};
	}

	// see https://stackoverflow.com/a/18018891
	private static URLStreamHandlerFactory setURLStreamHandlerFactory(URLStreamHandlerFactory factory) {
		try {
			Field f = URL.class.getDeclaredField("factory");
			f.setAccessible(true);
			URLStreamHandlerFactory curFactory = (URLStreamHandlerFactory)f.get(null);
			f.set(null, null);
			URL.setURLStreamHandlerFactory(factory);
			return curFactory;
		} catch (NoSuchFieldException|SecurityException|IllegalArgumentException|IllegalAccessException e) {
			throw new RuntimeException();
		}
	}
}
