package org.daisy.pipeline.xmlcatalog.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.function.Predicate;
import java.util.LinkedList;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

import org.daisy.common.stax.EventProcessor;
import org.daisy.common.stax.StaxEventHelper;
import org.daisy.common.stax.StaxEventHelper.EventPredicates;
import org.daisy.pipeline.xmlcatalog.XmlCatalog;
import org.daisy.pipeline.xmlcatalog.XmlCatalogParser;
import org.daisy.pipeline.xmlcatalog.impl.XmlCatalogConstants.Attributes;
import org.daisy.pipeline.xmlcatalog.impl.XmlCatalogConstants.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stax based XmlCatalog parser implementation
 */
public class StaxXmlCatalogParser implements XmlCatalogParser {

	private static final String HTTP_WWW_OASIS_OPEN_ORG_COMMITTEES_ENTITY_RELEASE_1_0_CATALOG_DTD = "http://www.oasis-open.org/committees/entity/release/1.0/catalog.dtd";
	private static final String ORG_DAISY_PIPELINE_XMLCATALOG_RESOURCES_CATALOG_DTD = "org/daisy/pipeline/xmlcatalog/resources/catalog.dtd";
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory
			.getLogger(StaxXmlCatalogParser.class);
	/** The xmlinputfactory. */
	private XMLInputFactory mFactory;
	private final XMLResolver mResolver = new XMLResolver() {

		@Override
		public Object resolveEntity(String publicId, String systemId,
				String arg2, String arg3) throws XMLStreamException {
			if (systemId
					.equals(HTTP_WWW_OASIS_OPEN_ORG_COMMITTEES_ENTITY_RELEASE_1_0_CATALOG_DTD)) {
				return this.getClass().getClassLoader()
						.getResourceAsStream(ORG_DAISY_PIPELINE_XMLCATALOG_RESOURCES_CATALOG_DTD);
			} else {
				return null;

			}

		}
	};

	/*
	 * (non-Javadoc)
	 *
	 * @see org.daisy.pipeline.xmlcatalog.XmlCatalogParser#parse(java.net.URI)
	 */
	@Override
	public XmlCatalog parse(URI uri) {
		return new StatefulParser().parse(uri);
	}

	/**
	 * Sets the {@link XMLInputFactory}
	 *
	 * @param factory
	 *            the new factory
	 */
	public void setFactory(XMLInputFactory factory) {
		mFactory = factory;
		mFactory.setXMLResolver(mResolver);
	}

	/**
	 * Activate (OSGI).
	 */
	public void activate() {
		logger.trace("Activating XmlCatalogParser");
	}

	/**
	 * The Class StatefulParser.
	 */
	private class StatefulParser {

		/** The m catalog builder. */
		XmlCatalog.Builder mCatalogBuilder = new XmlCatalog.Builder();

		/** The m base. */
		LinkedList<URI> mBase = new LinkedList<URI>();

		/**
		 * Parses the.
		 *
		 * @param uri
		 *            the uri
		 * @return the xml catalog
		 */
		public XmlCatalog parse(URI uri) {
			if (mFactory == null) {
				throw new IllegalStateException();
			}
			if (uri == null) {
				throw new IllegalArgumentException("Uri is null");
			}
			XMLEventReader reader = null;
			InputStream is = null;
			mBase.add(URI.create(""));
			try {

				is = uri.toURL().openStream();

				reader = mFactory.createXMLEventReader(is);

				parseCatalog(reader);

			} catch (XMLStreamException e) {
				throw new RuntimeException("Parsing error: " + e.getMessage(),
						e);
			} catch (IOException e) {
				throw new RuntimeException(
						"Couldn't access package descriptor: " + e.getMessage(),
						e);

			}catch (Exception e){
				throw new RuntimeException(
						"Error while parsing descriptor: " + e.getMessage(),
						e);

			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
					if (is != null) {
						is.close();
					}
				} catch (Exception e) {
					// ignore;
				}
			}
			return mCatalogBuilder.build();
		}

		/**
		 * Parses the catalog.
		 *
		 * @param reader
		 *            the reader
		 * @throws XMLStreamException
		 *             the xML stream exception
		 */
		private void parseCatalog(XMLEventReader reader) throws XMLStreamException {
			Predicate<XMLEvent> pred =
				    EventPredicates.isStartOrStopElement(Elements.E_CATALOG)
				.or(EventPredicates.isStartOrStopElement(Elements.E_GROUP))
				.or(EventPredicates.isStartOrStopElement(Elements.E_PUBLIC))
				.or(EventPredicates.isStartOrStopElement(Elements.E_SYSTEM))
				.or(EventPredicates.isStartOrStopElement(Elements.E_URI))
				.or(EventPredicates.isStartOrStopElement(Elements.E_REWRITE));
			StaxEventHelper.loop(reader, pred,
					EventPredicates.getChildOrSiblingPredicate(),
					new EventProcessor() {
						@Override
						public void process(XMLEvent event)
								throws XMLStreamException {
							// catalog and group controlled for xml:base changes
							if (event.isStartElement()
									&& (event.asStartElement().getName()
											.equals(Elements.E_CATALOG) || event
											.asStartElement().getName()
											.equals(Elements.E_GROUP))) {
								Attribute base = event.asStartElement()
										.getAttributeByName(
												Attributes.A_XML_BASE);
								if (base != null) {
									mBase.push(URI.create(base.getValue()));
								} else {
									mBase.push(mBase.peek());
								}
								// xml:base pop
							} else if (event.isEndElement()
									&& (event.asEndElement().getName()
											.equals(Elements.E_CATALOG) || event
											.asEndElement().getName()
											.equals(Elements.E_GROUP))) {
								mBase.pop();
								// rest of interesting elements
							} else if (event.isStartElement()
									&& event.asStartElement().getName()
											.equals(Elements.E_PUBLIC)) {
								parsePublic(event);
							}
							if (event.isStartElement()
									&& event.asStartElement().getName()
											.equals(Elements.E_SYSTEM)) {
								parseSystem(event);
							}
							if (event.isStartElement()
									&& event.asStartElement().getName()
											.equals(Elements.E_URI)) {
								parseUri(event);
							}
							if (event.isStartElement()
									&& event.asStartElement().getName()
											.equals(Elements.E_REWRITE)) {
								parseRewrite(event);
							}

						}

					});

		}

		/**
		 * Parses the uri.
		 *
		 * @param event
		 *            the event
		 */
		protected void parseUri(XMLEvent event) {
			Attribute name = event.asStartElement().getAttributeByName(
					Attributes.A_NAME);
			Attribute uri = event.asStartElement().getAttributeByName(
					Attributes.A_URI);
			URI nameUri = null;
			URI uriUri = null;
			if (name != null) {
				nameUri = URI.create(name.getValue());
			} else {
				throw new IllegalStateException("name is null");
			}
			if (uri != null) {
				uriUri = addBase(uri.getValue(), mBase.peek());
			} else {
				throw new IllegalStateException("uri is null");
			}
			mCatalogBuilder.withUriMapping(nameUri, uriUri);
		}

		/**
		 * Parses the public.
		 *
		 * @param event
		 *            the event
		 */
		private void parsePublic(XMLEvent event) {
			Attribute publicId = event.asStartElement().getAttributeByName(
					Attributes.A_PUBLIC_ID);
			Attribute uri = event.asStartElement().getAttributeByName(
					Attributes.A_URI);
			String publicIdStr = "";
			URI uriUri = null;
			if (publicId != null) {
				publicIdStr = publicId.getValue();
			} else {
				throw new IllegalStateException("public id is null");
			}
			if (uri != null) {
				uriUri = addBase(uri.getValue(), mBase.peek());
			} else {
				throw new IllegalStateException("uri is null");
			}

			mCatalogBuilder.withPublicMapping(publicIdStr, uriUri);
		}

		/**
		 * Parses the system.
		 *
		 * @param event
		 *            the event
		 */
		private void parseSystem(XMLEvent event) {

			Attribute systemId = event.asStartElement().getAttributeByName(
					Attributes.A_SYSTEM_ID);
			Attribute uri = event.asStartElement().getAttributeByName(
					Attributes.A_URI);
			URI systemIdUri = null;
			URI uriUri = null;
			if (systemId != null) {
				systemIdUri = URI.create(systemId.getValue());
			} else {
				throw new IllegalStateException("system id is null");
			}
			if (uri != null) {
				uriUri = addBase(uri.getValue(), mBase.peek());
			} else {
				throw new IllegalStateException("uri is null");
			}
			mCatalogBuilder.withSystemIdMapping(systemIdUri, uriUri);
		}

		/**
		 * Parses the rewriteUri element 
		 *
		 * @param event
		 *            the event
		 */
		private void parseRewrite(XMLEvent event) {
			Attribute startString= event.asStartElement().getAttributeByName(
					Attributes.A_START_STRING);
			Attribute rewrite= event.asStartElement().getAttributeByName(
					Attributes.A_REWRITE_PREFIX);
			URI startStringUri= null;
			URI rewriteUri= null;
			if (startString!= null) {
                                if (startString.getValue().endsWith("/")){
                                        startStringUri= URI.create(startString.getValue());
                                }else{
                                        startStringUri= URI.create(startString.getValue()+"/");
                                }

			} else {
				throw new IllegalStateException("startString is null");
			}
			if (rewrite != null) {
				rewriteUri= addBase(rewrite.getValue(), mBase.peek());
			} else {
				throw new IllegalStateException("rewrite preffix is null");
			}
			mCatalogBuilder.withRewriteUri(startStringUri, rewriteUri);
		}
	}

	/**
	 * Adds the base.
	 *
	 * @param uri
	 *            the uri
	 * @param base
	 *            the base
	 * @return the uRI
	 */
	private URI addBase(String uri, URI base) {
		if (base.toString().isEmpty()) {
			return URI.create(uri);
		} else {
			if (base.toString().charAt(base.toString().length() - 1) == '/') {
				return URI.create(base.toString() + uri);
			} else {
				return URI.create(base.toString() + "/" + uri);
			}
		}
	}
}
