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

	public static String P_NS = "http://www.w3.org/ns/xproc";

	public static String PX_NS = "http://www.daisy.org/ns/pipeline/xproc";

	public static String CX_NS = "http://xmlcalabash.com/ns/extensions";
	
	public static String XML_NS = "http://www.w3.org/XML/1998/namespace";

	public static final class Elements {

		public static QName P_DECLARE_STEP = new QName(P_NS, "declare-step");

		public static QName P_DOCUMENTATION = new QName(P_NS, "documentation");

		public static QName P_INPUT = new QName(P_NS, "input");

		public static QName P_OUTPUT = new QName(P_NS, "output");

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

		private Elements() {
			// no instantiations
		}
	}

	public static final class Attributes {

		public static final QName KIND = new QName("kind");

		public static final QName NAME = new QName("name");

		public static final QName PORT = new QName("port");

		public static final QName PRIMARY = new QName("primary");

		public static final QName REQUIRED = new QName("required");

		public static final QName SELECT = new QName("select");

		public static final QName SEQUENCE = new QName("sequence");

		public static final QName PX_DIR = new QName(PX_NS, "dir");

		public static final QName PX_MEDIA_TYPE = new QName(PX_NS, "media-type");

		public static final QName PX_TYPE = new QName(PX_NS, "type");

		public static final QName PX_HIDDEN = new QName(PX_NS, "hidden");

		public static final QName PX_INPUT_FILESETS = new QName(PX_NS, "input-filesets");

		public static final QName PX_OUTPUT_FILESETS = new QName(PX_NS, "output-filesets");

		public static final QName PX_ROLE = new QName(PX_NS, "role");

		public static final QName XML_SPACE = new QName(XML_NS, "space");

		public static final QName HREF = new QName("href");

		public static final QName PX_OUTPUT = new QName(PX_NS, "output");

		public static final QName PX_SEQUENCE = new QName(PX_NS, "sequence");

		public static final QName PX_ORDERED = new QName(PX_NS, "ordered");

		public static final QName PX_SEPARATOR = new QName(PX_NS, "separator");

		public static final QName PX_PRIMARY = new QName(PX_NS, "primary");

		public static final QName CX_AS = new QName(CX_NS, "as");

		private Attributes() {
			// no instantiations
		}
	}

	public static final class Values {

		public static final String PARAMETER = "parameter";

		public static final String TRUE = "true";

		public static final String NAME = "name";

		public static final String DESC = "desc";

		public static final String HOMEPAGE = "homepage";

		public static final String AUTHOR = "author";

		public static final String MAINTAINER = "maintainer";

		private Values() {
			// no instantiations
		}
	}
}
