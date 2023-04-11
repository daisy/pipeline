package org.daisy.pipeline.script.impl;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

/**
 * Constants used when parsing XProcScripts
 */
public final class XProcScriptConstants {

	private XProcScriptConstants() {
		// no instantiations
	}

	/** CD namespace. */
	public static String CD_NS = "http://www.daisy.org/ns/pipeline/converter";

	/** P namespace. */
	public static String P_NS = "http://www.w3.org/ns/xproc";

	/** PX namespace. */
	public static String PX_NS = "http://www.daisy.org/ns/pipeline/xproc";
	
	/** XML namespace. */
	public static String XML_NS = "http://www.w3.org/XML/1998/namespace";

	/**
	 * Elements.
	 */
	public static final class Elements {

		/** declare step. */
		public static QName P_DECLARE_STEP = new QName(P_NS, "declare-step");

		/** documentation. */
		public static QName P_DOCUMENTATION = new QName(P_NS, "documentation");

		/** input. */
		public static QName P_INPUT = new QName(P_NS, "input");

		/** output. */
		public static QName P_OUTPUT = new QName(P_NS, "output");

		/** option. */
		public static QName P_OPTION = new QName(P_NS, "option");

		public static QName P_PARAMS = new QName(P_NS, "params");

		public static QName P_EMPTY = new QName(P_NS, "empty");

		public static QName P_DOCUMENT = new QName(P_NS, "document");

		public static QName P_INLINE = new QName(P_NS, "inline");

		public static QName P_DATA = new QName(P_NS, "data");
		
		public static Set<QName> CONNECTIONS = new HashSet<QName>();
		static {
			CONNECTIONS.add(P_EMPTY);
			CONNECTIONS.add(P_DOCUMENT);
			CONNECTIONS.add(P_INLINE);
			CONNECTIONS.add(P_DATA);
		}

		/**
		 * Instantiates a new elements.
		 */
		private Elements() {
			// no instantiations
		}
	}

	/**
	 * Attributes.
	 */
	public static final class Attributes {

		/** The Constant KIND. */
		public static final QName KIND = new QName("kind");

		/** The Constant NAME. */
		public static final QName NAME = new QName("name");

		/** The Constant PORT. */
		public static final QName PORT = new QName("port");

		/** The Constant PRIMARY. */
		public static final QName PRIMARY = new QName("primary");

		/** The Constant REQUIRED. */
		public static final QName REQUIRED = new QName("required");

		/** The Constant SELECT. */
		public static final QName SELECT = new QName("select");

		/** The Constant SEQUENCE. */
		public static final QName SEQUENCE = new QName("sequence");

		/** The Constant PX_DIR. */
		public static final QName PX_DIR = new QName(PX_NS, "dir");

		/** The Constant PX_MEDIA_TYPE. */
		public static final QName PX_MEDIA_TYPE = new QName(PX_NS, "media-type");

		/** The Constant PX_TYPE. */
		public static final QName PX_TYPE = new QName(PX_NS, "type");

		/** The Constant PX_HIDDEN. */
		public static final QName PX_HIDDEN = new QName(PX_NS, "hidden");

		/** The Constant PX_TYPE. */
		public static final QName PX_INPUT_FILESETS = new QName(PX_NS,
				"input-filesets");

		/** The Constant PX_TYPE. */
		public static final QName PX_OUTPUT_FILESETS = new QName(PX_NS,
				"output-filesets");

		/** The Constant PX_ROLE */
		public static final QName PX_ROLE = new QName(PX_NS, "role");

		/** The Constant XML_SPACE */
		public static final QName XML_SPACE = new QName(XML_NS, "space");

		/** The Constant HREF */
		public static final QName HREF = new QName("href");

		/** The Constant PX_OUTPUT */
		public static final QName PX_OUTPUT = new QName(PX_NS, "output");

		/** The Constant PX_SEQUENCE */
		public static final QName PX_SEQUENCE = new QName(PX_NS, "sequence");

		/** The Constant PX_OUTPUT */
		public static final QName PX_ORDERED = new QName(PX_NS, "ordered");

		/** The Constant PX_SEPARATOR */
		public static final QName PX_SEPARATOR = new QName(PX_NS, "separator");

		public static final QName PX_PRIMARY = new QName(PX_NS, "primary");

		/**
		 * Instantiates a new attributes.
		 */
		private Attributes() {
			// no instantiations
		}
	}

	/**
	 * Values.
	 */
	public static final class Values {

		/** The Constant PARAMETER. */
		public static final String PARAMETER = "parameter";

		/** The Constant TRUE. */
		public static final String TRUE = "true";

		/** The Constant NAME */
		public static final String NAME = "name";

		/** The Constant DESC */
		public static final String DESC = "desc";

		/** The Constant HOMEPAGE */
		public static final String HOMEPAGE = "homepage";

		/** The Constant AUTHOR */
		public static final String AUTHOR = "author";

		/** The Constant MAINTAINER */
		public static final String MAINTAINER = "maintainer";

		/**
		 * Instantiates a new values.
		 */
		private Values() {
			// no instantiations
		}
	}
}
