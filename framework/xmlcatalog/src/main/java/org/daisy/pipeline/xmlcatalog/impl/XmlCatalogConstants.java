package org.daisy.pipeline.xmlcatalog.impl;

import javax.xml.namespace.QName;


/**
 * Useful constants for parsing catalogs
 */
public final class XmlCatalogConstants {

	/** The Constant XML_CATALOG_NS. */
	public static final String XML_CATALOG_NS="urn:oasis:names:tc:entity:xmlns:xml:catalog";

	/** The Constant XML_NS. */
	public static final String XML_NS="http://www.w3.org/XML/1998/namespace";

	/**
	 *Elements
	 */
	public static final class Elements{

		/** The Constant E_CATALOG. */
		public static final QName E_CATALOG = new QName(XML_CATALOG_NS,"catalog");

		/** The Constant E_GROUP. */
		public static final QName E_GROUP = new QName(XML_CATALOG_NS,"group");

		/** The Constant E_PUBLIC. */
		public static final QName E_PUBLIC = new QName(XML_CATALOG_NS,"public");

		/** The Constant E_SYSTEM. */
		public static final QName E_SYSTEM = new QName(XML_CATALOG_NS,"system");

		/** The Constant E_URI. */
		public static final QName E_URI = new QName(XML_CATALOG_NS,"uri");

		/** The Constant E_URI. */
		public static final QName E_REWRITE = new QName(XML_CATALOG_NS,"rewriteURI");
	}

	/**
	 * Attributes.
	 */
	public static final class Attributes{
		/*
		public static final QName A_XML_BASE= new QName(XML_NS, "base");
		public static final QName A_PUBLIC_ID= new QName(XML_CATALOG_NS, "publicId");
		public static final QName A_SYSTEM_ID= new QName(XML_CATALOG_NS, "systemId");
		public static final QName A_NAME= new QName(XML_CATALOG_NS, "name");
		public static final QName A_URI= new QName(XML_CATALOG_NS, "uri");
		*/
		//getAttributeByName is not working with the namespace
		/** The Constant A_XML_BASE. */
		public static final QName A_XML_BASE= new QName(XML_NS, "base","xml");

		/** The Constant A_PUBLIC_ID. */
		public static final QName A_PUBLIC_ID= new QName( "publicId");

		/** The Constant A_SYSTEM_ID. */
		public static final QName A_SYSTEM_ID= new QName( "systemId");

		/** The Constant A_NAME. */
		public static final QName A_NAME= new QName( "name");

		/** The Constant A_URI. */
		public static final QName A_URI= new QName( "uri");

		/** The Constant A_START_STRING. */
		public static final QName A_START_STRING= new QName( "uriStartString");

		/** The Constant A_REWRITE_PREFIX. */
		public static final QName A_REWRITE_PREFIX= new QName( "rewritePrefix");
	}



}
