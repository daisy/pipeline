package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import play.Logger;

public class ContentType {
	
	/**
	 * Matches the file extension of a filename to determine its content type.
	 * If the file is an XML file, further investigation into the file are executed
	 * using the input stream to read the document element of the XML. The namespace
	 * used for the document element are used to determine what kind of XML document
	 * it is.
	 * 
	 * @param filename The filename of the file
	 * @param in The input stream for the file
	 * @return The content type for the file
	 */
	public static String probe(String filename, InputStream in) {
		String contentType = null;
		String ext = filename.replaceAll("^.*\\.", "").toLowerCase();
		boolean isXML = false;
		
		if ("xml".equals(ext)) {
			isXML = true;
			
		} else if ("html".equals(ext) || "htm".equals(ext)) {
			isXML = true;
			
		} else if ("xhtml".equals(ext)) {
			isXML = true;
			contentType = "application/xhtml+xml";
		} else if ("opf".equals(ext)) {
			isXML = true;
			contentType = "application/oebps-package+xml";
		} else if ("smil".equals(ext)) {
			isXML = true;
			contentType = "application/smil+xml";
		} else if ("ncx".equals(ext)) {
			isXML = true;
			contentType = "application/x-dtbncx+xml";
		} else if ("svg".equals(ext)) {
			isXML = true;
			contentType = "image/svg+xml";
		} else if ("xpl".equals(ext) || "xproc".equals(ext)) {
			isXML = true;
			contentType = "application/xproc+xml";
		} else if ("xsl".equals(ext) || "xslt".equals(ext)) {
			isXML = true;
			contentType = "application/xslt+xml";
		}
		
		if (isXML && contentType == null) {
			char[] buf = new char[4 * 1024]; // 4 KiB
			StringBuilder headBuilder = new StringBuilder();
			try {
				Reader reader = new InputStreamReader(in, "UTF-8");
				int len = 0;
				len = reader.read(buf, 0, buf.length);
				if (len > 0)
					headBuilder.append(buf, 0, len);
			} catch (UnsupportedEncodingException e) {
				Logger.error("Encoding not supported (while reading filestream)", e);
			} catch (IOException e) {
				Logger.error("Could not read filestream", e);
			}
			String head = headBuilder.toString();
			
			Map<String,String> documentElement = parseDocument(head);
			
			if (documentElement.containsKey("")) {
				String name = documentElement.get("");
				String prefix = "";
				String namespace = "";
				
				if (name.contains(":")) {
					String[] split = name.split(":");
					prefix = split[0];
					name = split[1];
				}
				
				if ("".equals(prefix))
					namespace = documentElement.get("xmlns");
				else
					namespace = documentElement.get("xmlns:"+prefix);
				
				if (namespace != null) {
					contentType = namespaces.get(namespace);
				} else if ("html".equals(ext) || "htm".equals(ext)) {
					contentType = "text/html";
				}
				
				if (contentType == null)
					contentType = "application/xml";
			}
			
			if (contentType == null) {
				if ("html".equals(ext) || "htm".equals(ext))
					contentType = "application/xhtml+xml";
				else
					contentType = "application/xml";
			}
			
		} else {
			// Non-XML files
			
			if ("html".equals(ext) || "htm".equals(ext)) {
				
				if (contentType == null)
					contentType = "text/html";
				
			} else {
				contentType = extensions.get(ext);
			}
			
			if (contentType == null)
				contentType = "application/octet-stream";
		}
		
		return contentType;
	}
	
	private static final Pattern PI = Pattern.compile("^[^<]*?<\\?.*?\\?>", Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern DOCTYPE = Pattern.compile("^\\s*<!\\w.*?>", Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern COMMENT = Pattern.compile("^\\s*<!--.*?-->", Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern WHITESPACE = Pattern.compile("^\\s+", Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern FIX_EQUALS = Pattern.compile("\\s*=\\s*", Pattern.MULTILINE | Pattern.DOTALL);
	private static final Pattern ATTRIBUTE_VALUE = Pattern.compile("^\"([^\"]*)\".*", Pattern.MULTILINE | Pattern.DOTALL);
	
	/**
	 * Takes the start of an XML document as input, and then parses it to find the document element.
	 * The name and attributes of the document element are returned.
	 * 
	 * @param head The start of an XML document.
	 * @return A map of all the attributes found on the document element. The "" key contains the element name.
	 */
	public static Map<String, String> parseDocument(String head) {
		Map<String, String> map = new HashMap<String,String>();
		
		if (head == null) {
			Logger.warn("Document head string == null", new Exception());
			return map;
		}
		
		// Remove XML prolog
		int removed = 1;
		while (removed > 0) {
			removed = head.length();
			head = PI.matcher(head).replaceFirst("");
			head = DOCTYPE.matcher(head).replaceFirst("");
			head = COMMENT.matcher(head).replaceFirst("");
			head = WHITESPACE.matcher(head).replaceFirst("");
			removed -= head.length();
		}
		
		if (head.length() == 0) {
			Logger.warn("Document head empty", new Exception());
			return map;
		}
		
		// Read XML document element name and attributes
		if (head.charAt(0) == '<') {
			String[] elementNameSplit = head.split("\\s", 2);
			String elementName = elementNameSplit[0].substring(1);
			head = elementNameSplit.length > 1 ? elementNameSplit[1] : "";
			map.put("", elementName);
			
			removed = 1;
			while (true) {
				removed = head.length();
				String[] attribute = parseAttribute(head);
				head = attribute[0];
				removed -= head.length();
				
				if (removed <= 0) break;
				
				map.put(attribute[1], attribute[2]);
			}
		}
		
		return map;
	}
	
	/**
	 * Helper function for parseDocument(String)
	 * 
	 * @param element
	 * @return String[3] = [ remainder of the element , attribute name , attribute value ] or [ element , "" , "" ] if not found
	 */
	private static String[] parseAttribute(String element) {
		element = WHITESPACE.matcher(element).replaceFirst("");
		element = FIX_EQUALS.matcher(element).replaceFirst("=");
		String[] attributeNameSplit = element.split("=", 2);
		if (attributeNameSplit.length < 2 || attributeNameSplit[0].contains(">"))
			return new String[]{element, "", ""};
		
		String value = ATTRIBUTE_VALUE.matcher(attributeNameSplit[1]).replaceFirst("$1");
		if (value.length() == attributeNameSplit[1].length())
			return new String[]{element, "", ""};
		
		element = attributeNameSplit[1].substring(value.length()+2);
		
		return new String[]{element, attributeNameSplit[0], value};
	}
	
	/**
	 * A map of the content type commonly associated with a namespace.
	 */
	public static final Map<String, String> namespaces;
	static {
    	Map<String, String> nsMap = new HashMap<String, String>();
        
    	nsMap.put("http://www.w3.org/1999/xhtml", "application/xhtml+xml");
    	nsMap.put("http://www.idpf.org/2007/opf", "application/oebps-package+xml");
    	nsMap.put("http://www.daisy.org/z3986/2005/dtbook/", "application/x-dtbook+xml");
    	nsMap.put("http://www.w3.org/TR/REC-smil", "application/smil+xml"); // SMIL 1.0
    	nsMap.put("http://www.w3.org/2001/SMIL20/", "application/smil+xml"); // SMIL 2.0
    	nsMap.put("http://www.w3.org/2001/SMIL20/Language", "application/smil+xml"); // SMIL 2.0
    	nsMap.put("http://www.w3.org/2005/SMIL21/", "application/smil+xml"); // SMIL 2.1
    	nsMap.put("http://www.w3.org/2005/SMIL21/Language", "application/smil+xml"); // SMIL 2.1
    	nsMap.put("http://www.w3.org/2005/SMIL21/Mobile", "application/smil+xml"); // SMIL 2.1
    	nsMap.put("http://www.w3.org/2005/SMIL21/ExtendedMobile", "application/smil+xml"); // SMIL 2.1
    	nsMap.put("http://www.w3.org/2005/SMIL21/MobileProfile", "application/smil+xml"); // SMIL 2.1
    	nsMap.put("http://www.w3.org/2005/SMIL21/BasicExclTimeContainers", "application/smil+xml"); // SMIL 2.1
    	nsMap.put("http://www.w3.org/ns/SMIL", "application/smil+xml"); // SMIL 3.0
    	nsMap.put("http://www.daisy.org/z3986/2005/ncx/", "application/x-dtbncx+xml");
    	nsMap.put("http://www.w3.org/2000/svg", "image/svg+xml");
    	nsMap.put("http://www.w3.org/ns/xproc", "application/xproc+xml");
    	nsMap.put("http://www.w3.org/ns/xproc-step", "application/xproc+xml");
    	nsMap.put("http://www.w3.org/ns/xproc-error", "application/xproc+xml");
    	nsMap.put("http://www.w3.org/1999/XSL/Transform", "application/xslt+xml");
    	nsMap.put("http://www.w3.org/1999/XSL/Format", "text/xsl");
    	nsMap.put("http://www.daisy.org/ns/z3986/authoring/", "application/z3998-auth+xml");
    	nsMap.put("http://www.daisy.org/ns/z3998/authoring/", "application/z3998-auth+xml");
    	nsMap.put("http://www.w3.org/XML/1998/namespace", "application/xml");
    	nsMap.put("http://openebook.org/namespaces/oeb-package/1.0/", "application/oebps-package+xml");
    	nsMap.put("http://www.w3.org/2001/XML", "application/xml");
    	nsMap.put("http://www.w3.org/ns/xproc-step", "application/xproc+xml");
    	
    	namespaces = Collections.unmodifiableMap(nsMap);
	}
	
	/**
	 * A map of the content type commonly associated with a file extension.
	 * 
	 * Based on http://svn.apache.org/repos/asf/httpd/httpd/trunk/docs/conf/mime.types
	 */
	public static final Map<String, String> extensions;
	static {
    	Map<String, String> extMap = new HashMap<String, String>();
        
    	extMap.put("ez", "application/andrew-inset");
    	extMap.put("aw", "application/applixware");
    	extMap.put("atom", "application/atom+xml");
    	extMap.put("atomcat", "application/atomcat+xml");
    	extMap.put("atomsvc", "application/atomsvc+xml");
    	extMap.put("ccxml", "application/ccxml+xml");
    	extMap.put("cdmia", "application/cdmi-capability");
    	extMap.put("cdmic", "application/cdmi-container");
    	extMap.put("cdmid", "application/cdmi-domain");
    	extMap.put("cdmio", "application/cdmi-object");
    	extMap.put("cdmiq", "application/cdmi-queue");
    	extMap.put("cu", "application/cu-seeme");
    	extMap.put("davmount", "application/davmount+xml");
    	extMap.put("dbk", "application/docbook+xml");
    	extMap.put("dssc", "application/dssc+der");
    	extMap.put("xdssc", "application/dssc+xml");
    	extMap.put("ecma", "application/ecmascript");
    	extMap.put("emma", "application/emma+xml");
    	extMap.put("epub", "application/epub+zip");
    	extMap.put("exi", "application/exi");
    	extMap.put("pfr", "application/font-tdpfr");
    	extMap.put("gml", "application/gml+xml");
    	extMap.put("gpx", "application/gpx+xml");
    	extMap.put("gxf", "application/gxf");
    	extMap.put("stk", "application/hyperstudio");
    	extMap.put("ink", "application/inkml+xml");
    	extMap.put("inkml", "application/inkml+xml");
    	extMap.put("ipfix", "application/ipfix");
    	extMap.put("jar", "application/java-archive");
    	extMap.put("ser", "application/java-serialized-object");
    	extMap.put("class", "application/java-vm");
    	extMap.put("js", "application/javascript");
    	extMap.put("json", "application/json");
    	extMap.put("jsonml", "application/jsonml+json");
    	extMap.put("lostxml", "application/lost+xml");
    	extMap.put("hqx", "application/mac-binhex40");
    	extMap.put("cpt", "application/mac-compactpro");
    	extMap.put("mads", "application/mads+xml");
    	extMap.put("mrc", "application/marc");
    	extMap.put("mrcx", "application/marcxml+xml");
    	extMap.put("ma", "application/mathematica");
    	extMap.put("nb", "application/mathematica");
    	extMap.put("mb", "application/mathematica");
    	extMap.put("mathml", "application/mathml+xml");
    	extMap.put("mbox", "application/mbox");
    	extMap.put("mscml", "application/mediaservercontrol+xml");
    	extMap.put("metalink", "application/metalink+xml");
    	extMap.put("meta4", "application/metalink4+xml");
    	extMap.put("mets", "application/mets+xml");
    	extMap.put("mods", "application/mods+xml");
    	extMap.put("m21", "application/mp21");
    	extMap.put("mp21", "application/mp21");
    	extMap.put("mp4s", "application/mp4");
    	extMap.put("doc", "application/msword");
    	extMap.put("dot", "application/msword");
    	extMap.put("mxf", "application/mxf");
    	extMap.put("bin", "application/octet-stream");
    	extMap.put("dms", "application/octet-stream");
    	extMap.put("lrf", "application/octet-stream");
    	extMap.put("mar", "application/octet-stream");
    	extMap.put("so", "application/octet-stream");
    	extMap.put("dist", "application/octet-stream");
    	extMap.put("distz", "application/octet-stream");
    	extMap.put("pkg", "application/octet-stream");
    	extMap.put("bpk", "application/octet-stream");
    	extMap.put("dump", "application/octet-stream");
    	extMap.put("elc", "application/octet-stream");
    	extMap.put("deploy", "application/octet-stream");
    	extMap.put("oda", "application/oda");
    	extMap.put("opf", "application/oebps-package+xml");
    	extMap.put("ogx", "application/ogg");
    	extMap.put("omdoc", "application/omdoc+xml");
    	extMap.put("onetoc", "application/onenote");
    	extMap.put("onetoc2", "application/onenote");
    	extMap.put("onetmp", "application/onenote");
    	extMap.put("onepkg", "application/onenote");
    	extMap.put("oxps", "application/oxps");
    	extMap.put("xer", "application/patch-ops-error+xml");
    	extMap.put("pdf", "application/pdf");
    	extMap.put("pgp", "application/pgp-encrypted");
    	extMap.put("asc", "application/pgp-signature");
    	extMap.put("sig", "application/pgp-signature");
    	extMap.put("prf", "application/pics-rules");
    	extMap.put("p10", "application/pkcs10");
    	extMap.put("p7m", "application/pkcs7-mime");
    	extMap.put("p7c", "application/pkcs7-mime");
    	extMap.put("p7s", "application/pkcs7-signature");
    	extMap.put("p8", "application/pkcs8");
    	extMap.put("ac", "application/pkix-attr-cert");
    	extMap.put("cer", "application/pkix-cert");
    	extMap.put("crl", "application/pkix-crl");
    	extMap.put("pkipath", "application/pkix-pkipath");
    	extMap.put("pki", "application/pkixcmp");
    	extMap.put("pls", "application/pls+xml");
    	extMap.put("ai", "application/postscript");
    	extMap.put("eps", "application/postscript");
    	extMap.put("ps", "application/postscript");
    	extMap.put("cww", "application/prs.cww");
    	extMap.put("pskcxml", "application/pskc+xml");
    	extMap.put("rdf", "application/rdf+xml");
    	extMap.put("rif", "application/reginfo+xml");
    	extMap.put("rnc", "application/relax-ng-compact-syntax");
    	extMap.put("rl", "application/resource-lists+xml");
    	extMap.put("rld", "application/resource-lists-diff+xml");
    	extMap.put("rs", "application/rls-services+xml");
    	extMap.put("gbr", "application/rpki-ghostbusters");
    	extMap.put("mft", "application/rpki-manifest");
    	extMap.put("roa", "application/rpki-roa");
    	extMap.put("rsd", "application/rsd+xml");
    	extMap.put("rss", "application/rss+xml");
    	extMap.put("rtf", "application/rtf");
    	extMap.put("sbml", "application/sbml+xml");
    	extMap.put("scq", "application/scvp-cv-request");
    	extMap.put("scs", "application/scvp-cv-response");
    	extMap.put("spq", "application/scvp-vp-request");
    	extMap.put("spp", "application/scvp-vp-response");
    	extMap.put("sdp", "application/sdp");
    	extMap.put("setpay", "application/set-payment-initiation");
    	extMap.put("setreg", "application/set-registration-initiation");
    	extMap.put("shf", "application/shf+xml");
    	extMap.put("smi", "application/smil+xml");
    	extMap.put("smil", "application/smil+xml");
    	extMap.put("rq", "application/sparql-query");
    	extMap.put("srx", "application/sparql-results+xml");
    	extMap.put("gram", "application/srgs");
    	extMap.put("grxml", "application/srgs+xml");
    	extMap.put("sru", "application/sru+xml");
    	extMap.put("ssdl", "application/ssdl+xml");
    	extMap.put("ssml", "application/ssml+xml");
    	extMap.put("tei", "application/tei+xml");
    	extMap.put("teicorpus", "application/tei+xml");
    	extMap.put("tfi", "application/thraud+xml");
    	extMap.put("tsd", "application/timestamped-data");
    	extMap.put("plb", "application/vnd.3gpp.pic-bw-large");
    	extMap.put("psb", "application/vnd.3gpp.pic-bw-small");
    	extMap.put("pvb", "application/vnd.3gpp.pic-bw-var");
    	extMap.put("tcap", "application/vnd.3gpp2.tcap");
    	extMap.put("pwn", "application/vnd.3m.post-it-notes");
    	extMap.put("aso", "application/vnd.accpac.simply.aso");
    	extMap.put("imp", "application/vnd.accpac.simply.imp");
    	extMap.put("acu", "application/vnd.acucobol");
    	extMap.put("atc", "application/vnd.acucorp");
    	extMap.put("acutc", "application/vnd.acucorp");
    	extMap.put("air", "application/vnd.adobe.air-application-installer-package+zip");
    	extMap.put("fcdt", "application/vnd.adobe.formscentral.fcdt");
    	extMap.put("fxp", "application/vnd.adobe.fxp");
    	extMap.put("fxpl", "application/vnd.adobe.fxp");
    	extMap.put("xdp", "application/vnd.adobe.xdp+xml");
    	extMap.put("xfdf", "application/vnd.adobe.xfdf");
    	extMap.put("ahead", "application/vnd.ahead.space");
    	extMap.put("azf", "application/vnd.airzip.filesecure.azf");
    	extMap.put("azs", "application/vnd.airzip.filesecure.azs");
    	extMap.put("azw", "application/vnd.amazon.ebook");
    	extMap.put("acc", "application/vnd.americandynamics.acc");
    	extMap.put("ami", "application/vnd.amiga.ami");
    	extMap.put("apk", "application/vnd.android.package-archive");
    	extMap.put("cii", "application/vnd.anser-web-certificate-issue-initiation");
    	extMap.put("fti", "application/vnd.anser-web-funds-transfer-initiation");
    	extMap.put("atx", "application/vnd.antix.game-component");
    	extMap.put("mpkg", "application/vnd.apple.installer+xml");
    	extMap.put("m3u8", "application/vnd.apple.mpegurl");
    	extMap.put("swi", "application/vnd.aristanetworks.swi");
    	extMap.put("iota", "application/vnd.astraea-software.iota");
    	extMap.put("aep", "application/vnd.audiograph");
    	extMap.put("mpm", "application/vnd.blueice.multipass");
    	extMap.put("bmi", "application/vnd.bmi");
    	extMap.put("rep", "application/vnd.businessobjects");
    	extMap.put("cdxml", "application/vnd.chemdraw+xml");
    	extMap.put("mmd", "application/vnd.chipnuts.karaoke-mmd");
    	extMap.put("cdy", "application/vnd.cinderella");
    	extMap.put("cla", "application/vnd.claymore");
    	extMap.put("rp9", "application/vnd.cloanto.rp9");
    	extMap.put("c4g", "application/vnd.clonk.c4group");
    	extMap.put("c4d", "application/vnd.clonk.c4group");
    	extMap.put("c4f", "application/vnd.clonk.c4group");
    	extMap.put("c4p", "application/vnd.clonk.c4group");
    	extMap.put("c4u", "application/vnd.clonk.c4group");
    	extMap.put("c11amc", "application/vnd.cluetrust.cartomobile-config");
    	extMap.put("c11amz", "application/vnd.cluetrust.cartomobile-config-pkg");
    	extMap.put("csp", "application/vnd.commonspace");
    	extMap.put("cdbcmsg", "application/vnd.contact.cmsg");
    	extMap.put("cmc", "application/vnd.cosmocaller");
    	extMap.put("clkx", "application/vnd.crick.clicker");
    	extMap.put("clkk", "application/vnd.crick.clicker.keyboard");
    	extMap.put("clkp", "application/vnd.crick.clicker.palette");
    	extMap.put("clkt", "application/vnd.crick.clicker.template");
    	extMap.put("clkw", "application/vnd.crick.clicker.wordbank");
    	extMap.put("wbs", "application/vnd.criticaltools.wbs+xml");
    	extMap.put("pml", "application/vnd.ctc-posml");
    	extMap.put("ppd", "application/vnd.cups-ppd");
    	extMap.put("car", "application/vnd.curl.car");
    	extMap.put("pcurl", "application/vnd.curl.pcurl");
    	extMap.put("dart", "application/vnd.dart");
    	extMap.put("rdz", "application/vnd.data-vision.rdz");
    	extMap.put("uvf", "application/vnd.dece.data");
    	extMap.put("uvvf", "application/vnd.dece.data");
    	extMap.put("uvd", "application/vnd.dece.data");
    	extMap.put("uvvd", "application/vnd.dece.data");
    	extMap.put("uvt", "application/vnd.dece.ttml+xml");
    	extMap.put("uvvt", "application/vnd.dece.ttml+xml");
    	extMap.put("uvx", "application/vnd.dece.unspecified");
    	extMap.put("uvvx", "application/vnd.dece.unspecified");
    	extMap.put("uvz", "application/vnd.dece.zip");
    	extMap.put("uvvz", "application/vnd.dece.zip");
    	extMap.put("fe_launch", "application/vnd.denovo.fcselayout-link");
    	extMap.put("dna", "application/vnd.dna");
    	extMap.put("mlp", "application/vnd.dolby.mlp");
    	extMap.put("dpg", "application/vnd.dpgraph");
    	extMap.put("dfac", "application/vnd.dreamfactory");
    	extMap.put("kpxx", "application/vnd.ds-keypoint");
    	extMap.put("ait", "application/vnd.dvb.ait");
    	extMap.put("svc", "application/vnd.dvb.service");
    	extMap.put("geo", "application/vnd.dynageo");
    	extMap.put("mag", "application/vnd.ecowin.chart");
    	extMap.put("nml", "application/vnd.enliven");
    	extMap.put("esf", "application/vnd.epson.esf");
    	extMap.put("msf", "application/vnd.epson.msf");
    	extMap.put("qam", "application/vnd.epson.quickanime");
    	extMap.put("slt", "application/vnd.epson.salt");
    	extMap.put("ssf", "application/vnd.epson.ssf");
    	extMap.put("es3", "application/vnd.eszigno3+xml");
    	extMap.put("et3", "application/vnd.eszigno3+xml");
    	extMap.put("ez2", "application/vnd.ezpix-album");
    	extMap.put("ez3", "application/vnd.ezpix-package");
    	extMap.put("fdf", "application/vnd.fdf");
    	extMap.put("mseed", "application/vnd.fdsn.mseed");
    	extMap.put("seed", "application/vnd.fdsn.seed");
    	extMap.put("dataless", "application/vnd.fdsn.seed");
    	extMap.put("gph", "application/vnd.flographit");
    	extMap.put("ftc", "application/vnd.fluxtime.clip");
    	extMap.put("fm", "application/vnd.framemaker");
    	extMap.put("frame", "application/vnd.framemaker");
    	extMap.put("maker", "application/vnd.framemaker");
    	extMap.put("book", "application/vnd.framemaker");
    	extMap.put("fnc", "application/vnd.frogans.fnc");
    	extMap.put("ltf", "application/vnd.frogans.ltf");
    	extMap.put("fsc", "application/vnd.fsc.weblaunch");
    	extMap.put("oas", "application/vnd.fujitsu.oasys");
    	extMap.put("oa2", "application/vnd.fujitsu.oasys2");
    	extMap.put("oa3", "application/vnd.fujitsu.oasys3");
    	extMap.put("fg5", "application/vnd.fujitsu.oasysgp");
    	extMap.put("bh2", "application/vnd.fujitsu.oasysprs");
    	extMap.put("ddd", "application/vnd.fujixerox.ddd");
    	extMap.put("xdw", "application/vnd.fujixerox.docuworks");
    	extMap.put("xbd", "application/vnd.fujixerox.docuworks.binder");
    	extMap.put("fzs", "application/vnd.fuzzysheet");
    	extMap.put("txd", "application/vnd.genomatix.tuxedo");
    	extMap.put("ggb", "application/vnd.geogebra.file");
    	extMap.put("ggt", "application/vnd.geogebra.tool");
    	extMap.put("gex", "application/vnd.geometry-explorer");
    	extMap.put("gre", "application/vnd.geometry-explorer");
    	extMap.put("gxt", "application/vnd.geonext");
    	extMap.put("g2w", "application/vnd.geoplan");
    	extMap.put("g3w", "application/vnd.geospace");
    	extMap.put("gmx", "application/vnd.gmx");
    	extMap.put("kml", "application/vnd.google-earth.kml+xml");
    	extMap.put("kmz", "application/vnd.google-earth.kmz");
    	extMap.put("gqf", "application/vnd.grafeq");
    	extMap.put("gqs", "application/vnd.grafeq");
    	extMap.put("gac", "application/vnd.groove-account");
    	extMap.put("ghf", "application/vnd.groove-help");
    	extMap.put("gim", "application/vnd.groove-identity-message");
    	extMap.put("grv", "application/vnd.groove-injector");
    	extMap.put("gtm", "application/vnd.groove-tool-message");
    	extMap.put("tpl", "application/vnd.groove-tool-template");
    	extMap.put("vcg", "application/vnd.groove-vcard");
    	extMap.put("hal", "application/vnd.hal+xml");
    	extMap.put("zmm", "application/vnd.handheld-entertainment+xml");
    	extMap.put("hbci", "application/vnd.hbci");
    	extMap.put("les", "application/vnd.hhe.lesson-player");
    	extMap.put("hpgl", "application/vnd.hp-hpgl");
    	extMap.put("hpid", "application/vnd.hp-hpid");
    	extMap.put("hps", "application/vnd.hp-hps");
    	extMap.put("jlt", "application/vnd.hp-jlyt");
    	extMap.put("pcl", "application/vnd.hp-pcl");
    	extMap.put("pclxl", "application/vnd.hp-pclxl");
    	extMap.put("sfd-hdstx", "application/vnd.hydrostatix.sof-data");
    	extMap.put("mpy", "application/vnd.ibm.minipay");
    	extMap.put("afp", "application/vnd.ibm.modcap");
    	extMap.put("listafp", "application/vnd.ibm.modcap");
    	extMap.put("list3820", "application/vnd.ibm.modcap");
    	extMap.put("irm", "application/vnd.ibm.rights-management");
    	extMap.put("sc", "application/vnd.ibm.secure-container");
    	extMap.put("icc", "application/vnd.iccprofile");
    	extMap.put("icm", "application/vnd.iccprofile");
    	extMap.put("igl", "application/vnd.igloader");
    	extMap.put("ivp", "application/vnd.immervision-ivp");
    	extMap.put("ivu", "application/vnd.immervision-ivu");
    	extMap.put("igm", "application/vnd.insors.igm");
    	extMap.put("xpw", "application/vnd.intercon.formnet");
    	extMap.put("xpx", "application/vnd.intercon.formnet");
    	extMap.put("i2g", "application/vnd.intergeo");
    	extMap.put("qbo", "application/vnd.intu.qbo");
    	extMap.put("qfx", "application/vnd.intu.qfx");
    	extMap.put("rcprofile", "application/vnd.ipunplugged.rcprofile");
    	extMap.put("irp", "application/vnd.irepository.package+xml");
    	extMap.put("xpr", "application/vnd.is-xpr");
    	extMap.put("fcs", "application/vnd.isac.fcs");
    	extMap.put("jam", "application/vnd.jam");
    	extMap.put("rms", "application/vnd.jcp.javame.midlet-rms");
    	extMap.put("jisp", "application/vnd.jisp");
    	extMap.put("joda", "application/vnd.joost.joda-archive");
    	extMap.put("ktz", "application/vnd.kahootz");
    	extMap.put("ktr", "application/vnd.kahootz");
    	extMap.put("karbon", "application/vnd.kde.karbon");
    	extMap.put("chrt", "application/vnd.kde.kchart");
    	extMap.put("kfo", "application/vnd.kde.kformula");
    	extMap.put("flw", "application/vnd.kde.kivio");
    	extMap.put("kon", "application/vnd.kde.kontour");
    	extMap.put("kpr", "application/vnd.kde.kpresenter");
    	extMap.put("kpt", "application/vnd.kde.kpresenter");
    	extMap.put("ksp", "application/vnd.kde.kspread");
    	extMap.put("kwd", "application/vnd.kde.kword");
    	extMap.put("kwt", "application/vnd.kde.kword");
    	extMap.put("htke", "application/vnd.kenameaapp");
    	extMap.put("kia", "application/vnd.kidspiration");
    	extMap.put("kne", "application/vnd.kinar");
    	extMap.put("knp", "application/vnd.kinar");
    	extMap.put("skp", "application/vnd.koan");
    	extMap.put("skd", "application/vnd.koan");
    	extMap.put("skt", "application/vnd.koan");
    	extMap.put("skm", "application/vnd.koan");
    	extMap.put("sse", "application/vnd.kodak-descriptor");
    	extMap.put("lasxml", "application/vnd.las.las+xml");
    	extMap.put("lbd", "application/vnd.llamagraphics.life-balance.desktop");
    	extMap.put("lbe", "application/vnd.llamagraphics.life-balance.exchange+xml");
    	extMap.put("123", "application/vnd.lotus-1-2-3");
    	extMap.put("apr", "application/vnd.lotus-approach");
    	extMap.put("pre", "application/vnd.lotus-freelance");
    	extMap.put("nsf", "application/vnd.lotus-notes");
    	extMap.put("org", "application/vnd.lotus-organizer");
    	extMap.put("scm", "application/vnd.lotus-screencam");
    	extMap.put("lwp", "application/vnd.lotus-wordpro");
    	extMap.put("portpkg", "application/vnd.macports.portpkg");
    	extMap.put("mcd", "application/vnd.mcd");
    	extMap.put("mc1", "application/vnd.medcalcdata");
    	extMap.put("cdkey", "application/vnd.mediastation.cdkey");
    	extMap.put("mwf", "application/vnd.mfer");
    	extMap.put("mfm", "application/vnd.mfmp");
    	extMap.put("flo", "application/vnd.micrografx.flo");
    	extMap.put("igx", "application/vnd.micrografx.igx");
    	extMap.put("mif", "application/vnd.mif");
    	extMap.put("daf", "application/vnd.mobius.daf");
    	extMap.put("dis", "application/vnd.mobius.dis");
    	extMap.put("mbk", "application/vnd.mobius.mbk");
    	extMap.put("mqy", "application/vnd.mobius.mqy");
    	extMap.put("msl", "application/vnd.mobius.msl");
    	extMap.put("plc", "application/vnd.mobius.plc");
    	extMap.put("txf", "application/vnd.mobius.txf");
    	extMap.put("mpn", "application/vnd.mophun.application");
    	extMap.put("mpc", "application/vnd.mophun.certificate");
    	extMap.put("xul", "application/vnd.mozilla.xul+xml");
    	extMap.put("cil", "application/vnd.ms-artgalry");
    	extMap.put("cab", "application/vnd.ms-cab-compressed");
    	extMap.put("xls", "application/vnd.ms-excel");
    	extMap.put("xlm", "application/vnd.ms-excel");
    	extMap.put("xla", "application/vnd.ms-excel");
    	extMap.put("xlc", "application/vnd.ms-excel");
    	extMap.put("xlt", "application/vnd.ms-excel");
    	extMap.put("xlw", "application/vnd.ms-excel");
    	extMap.put("xlam", "application/vnd.ms-excel.addin.macroenabled.12");
    	extMap.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroenabled.12");
    	extMap.put("xlsm", "application/vnd.ms-excel.sheet.macroenabled.12");
    	extMap.put("xltm", "application/vnd.ms-excel.template.macroenabled.12");
    	extMap.put("eot", "application/vnd.ms-fontobject");
    	extMap.put("chm", "application/vnd.ms-htmlhelp");
    	extMap.put("ims", "application/vnd.ms-ims");
    	extMap.put("lrm", "application/vnd.ms-lrm");
    	extMap.put("thmx", "application/vnd.ms-officetheme");
    	extMap.put("cat", "application/vnd.ms-pki.seccat");
    	extMap.put("stl", "application/vnd.ms-pki.stl");
    	extMap.put("ppt", "application/vnd.ms-powerpoint");
    	extMap.put("pps", "application/vnd.ms-powerpoint");
    	extMap.put("pot", "application/vnd.ms-powerpoint");
    	extMap.put("ppam", "application/vnd.ms-powerpoint.addin.macroenabled.12");
    	extMap.put("pptm", "application/vnd.ms-powerpoint.presentation.macroenabled.12");
    	extMap.put("sldm", "application/vnd.ms-powerpoint.slide.macroenabled.12");
    	extMap.put("ppsm", "application/vnd.ms-powerpoint.slideshow.macroenabled.12");
    	extMap.put("potm", "application/vnd.ms-powerpoint.template.macroenabled.12");
    	extMap.put("mpp", "application/vnd.ms-project");
    	extMap.put("mpt", "application/vnd.ms-project");
    	extMap.put("docm", "application/vnd.ms-word.document.macroenabled.12");
    	extMap.put("dotm", "application/vnd.ms-word.template.macroenabled.12");
    	extMap.put("wps", "application/vnd.ms-works");
    	extMap.put("wks", "application/vnd.ms-works");
    	extMap.put("wcm", "application/vnd.ms-works");
    	extMap.put("wdb", "application/vnd.ms-works");
    	extMap.put("wpl", "application/vnd.ms-wpl");
    	extMap.put("xps", "application/vnd.ms-xpsdocument");
    	extMap.put("mseq", "application/vnd.mseq");
    	extMap.put("mus", "application/vnd.musician");
    	extMap.put("msty", "application/vnd.muvee.style");
    	extMap.put("taglet", "application/vnd.mynfc");
    	extMap.put("nlu", "application/vnd.neurolanguage.nlu");
    	extMap.put("ntf", "application/vnd.nitf");
    	extMap.put("nitf", "application/vnd.nitf");
    	extMap.put("nnd", "application/vnd.noblenet-directory");
    	extMap.put("nns", "application/vnd.noblenet-sealer");
    	extMap.put("nnw", "application/vnd.noblenet-web");
    	extMap.put("ngdat", "application/vnd.nokia.n-gage.data");
    	extMap.put("n-gage", "application/vnd.nokia.n-gage.symbian.install");
    	extMap.put("rpst", "application/vnd.nokia.radio-preset");
    	extMap.put("rpss", "application/vnd.nokia.radio-presets");
    	extMap.put("edm", "application/vnd.novadigm.edm");
    	extMap.put("edx", "application/vnd.novadigm.edx");
    	extMap.put("ext", "application/vnd.novadigm.ext");
    	extMap.put("odc", "application/vnd.oasis.opendocument.chart");
    	extMap.put("otc", "application/vnd.oasis.opendocument.chart-template");
    	extMap.put("odb", "application/vnd.oasis.opendocument.database");
    	extMap.put("odf", "application/vnd.oasis.opendocument.formula");
    	extMap.put("odft", "application/vnd.oasis.opendocument.formula-template");
    	extMap.put("odg", "application/vnd.oasis.opendocument.graphics");
    	extMap.put("otg", "application/vnd.oasis.opendocument.graphics-template");
    	extMap.put("odi", "application/vnd.oasis.opendocument.image");
    	extMap.put("oti", "application/vnd.oasis.opendocument.image-template");
    	extMap.put("odp", "application/vnd.oasis.opendocument.presentation");
    	extMap.put("otp", "application/vnd.oasis.opendocument.presentation-template");
    	extMap.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
    	extMap.put("ots", "application/vnd.oasis.opendocument.spreadsheet-template");
    	extMap.put("odt", "application/vnd.oasis.opendocument.text");
    	extMap.put("odm", "application/vnd.oasis.opendocument.text-master");
    	extMap.put("ott", "application/vnd.oasis.opendocument.text-template");
    	extMap.put("oth", "application/vnd.oasis.opendocument.text-web");
    	extMap.put("xo", "application/vnd.olpc-sugar");
    	extMap.put("dd2", "application/vnd.oma.dd2+xml");
    	extMap.put("oxt", "application/vnd.openofficeorg.extension");
    	extMap.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    	extMap.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
    	extMap.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
    	extMap.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
    	extMap.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    	extMap.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
    	extMap.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    	extMap.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
    	extMap.put("mgp", "application/vnd.osgeo.mapguide.package");
    	extMap.put("dp", "application/vnd.osgi.dp");
    	extMap.put("esa", "application/vnd.osgi.subsystem");
    	extMap.put("pdb", "application/vnd.palm");
    	extMap.put("pqa", "application/vnd.palm");
    	extMap.put("oprc", "application/vnd.palm");
    	extMap.put("paw", "application/vnd.pawaafile");
    	extMap.put("str", "application/vnd.pg.format");
    	extMap.put("ei6", "application/vnd.pg.osasli");
    	extMap.put("efif", "application/vnd.picsel");
    	extMap.put("wg", "application/vnd.pmi.widget");
    	extMap.put("plf", "application/vnd.pocketlearn");
    	extMap.put("pbd", "application/vnd.powerbuilder6");
    	extMap.put("box", "application/vnd.previewsystems.box");
    	extMap.put("mgz", "application/vnd.proteus.magazine");
    	extMap.put("qps", "application/vnd.publishare-delta-tree");
    	extMap.put("ptid", "application/vnd.pvi.ptid1");
    	extMap.put("qxd", "application/vnd.quark.quarkxpress");
    	extMap.put("qxt", "application/vnd.quark.quarkxpress");
    	extMap.put("qwd", "application/vnd.quark.quarkxpress");
    	extMap.put("qwt", "application/vnd.quark.quarkxpress");
    	extMap.put("qxl", "application/vnd.quark.quarkxpress");
    	extMap.put("qxb", "application/vnd.quark.quarkxpress");
    	extMap.put("bed", "application/vnd.realvnc.bed");
    	extMap.put("mxl", "application/vnd.recordare.musicxml");
    	extMap.put("musicxml", "application/vnd.recordare.musicxml+xml");
    	extMap.put("cryptonote", "application/vnd.rig.cryptonote");
    	extMap.put("cod", "application/vnd.rim.cod");
    	extMap.put("rm", "application/vnd.rn-realmedia");
    	extMap.put("rmvb", "application/vnd.rn-realmedia-vbr");
    	extMap.put("link66", "application/vnd.route66.link66+xml");
    	extMap.put("st", "application/vnd.sailingtracker.track");
    	extMap.put("see", "application/vnd.seemail");
    	extMap.put("sema", "application/vnd.sema");
    	extMap.put("semd", "application/vnd.semd");
    	extMap.put("semf", "application/vnd.semf");
    	extMap.put("ifm", "application/vnd.shana.informed.formdata");
    	extMap.put("itp", "application/vnd.shana.informed.formtemplate");
    	extMap.put("iif", "application/vnd.shana.informed.interchange");
    	extMap.put("ipk", "application/vnd.shana.informed.package");
    	extMap.put("twd", "application/vnd.simtech-mindmapper");
    	extMap.put("twds", "application/vnd.simtech-mindmapper");
    	extMap.put("mmf", "application/vnd.smaf");
    	extMap.put("teacher", "application/vnd.smart.teacher");
    	extMap.put("sdkm", "application/vnd.solent.sdkm+xml");
    	extMap.put("sdkd", "application/vnd.solent.sdkm+xml");
    	extMap.put("dxp", "application/vnd.spotfire.dxp");
    	extMap.put("sfs", "application/vnd.spotfire.sfs");
    	extMap.put("sdc", "application/vnd.stardivision.calc");
    	extMap.put("sda", "application/vnd.stardivision.draw");
    	extMap.put("sdd", "application/vnd.stardivision.impress");
    	extMap.put("smf", "application/vnd.stardivision.math");
    	extMap.put("sdw", "application/vnd.stardivision.writer");
    	extMap.put("vor", "application/vnd.stardivision.writer");
    	extMap.put("sgl", "application/vnd.stardivision.writer-global");
    	extMap.put("smzip", "application/vnd.stepmania.package");
    	extMap.put("sm", "application/vnd.stepmania.stepchart");
    	extMap.put("sxc", "application/vnd.sun.xml.calc");
    	extMap.put("stc", "application/vnd.sun.xml.calc.template");
    	extMap.put("sxd", "application/vnd.sun.xml.draw");
    	extMap.put("std", "application/vnd.sun.xml.draw.template");
    	extMap.put("sxi", "application/vnd.sun.xml.impress");
    	extMap.put("sti", "application/vnd.sun.xml.impress.template");
    	extMap.put("sxm", "application/vnd.sun.xml.math");
    	extMap.put("sxw", "application/vnd.sun.xml.writer");
    	extMap.put("sxg", "application/vnd.sun.xml.writer.global");
    	extMap.put("stw", "application/vnd.sun.xml.writer.template");
    	extMap.put("sus", "application/vnd.sus-calendar");
    	extMap.put("susp", "application/vnd.sus-calendar");
    	extMap.put("svd", "application/vnd.svd");
    	extMap.put("sis", "application/vnd.symbian.install");
    	extMap.put("sisx", "application/vnd.symbian.install");
    	extMap.put("xsm", "application/vnd.syncml+xml");
    	extMap.put("bdm", "application/vnd.syncml.dm+wbxml");
    	extMap.put("xdm", "application/vnd.syncml.dm+xml");
    	extMap.put("tao", "application/vnd.tao.intent-module-archive");
    	extMap.put("pcap", "application/vnd.tcpdump.pcap");
    	extMap.put("cap", "application/vnd.tcpdump.pcap");
    	extMap.put("dmp", "application/vnd.tcpdump.pcap");
    	extMap.put("tmo", "application/vnd.tmobile-livetv");
    	extMap.put("tpt", "application/vnd.trid.tpt");
    	extMap.put("mxs", "application/vnd.triscape.mxs");
    	extMap.put("tra", "application/vnd.trueapp");
    	extMap.put("ufd", "application/vnd.ufdl");
    	extMap.put("ufdl", "application/vnd.ufdl");
    	extMap.put("utz", "application/vnd.uiq.theme");
    	extMap.put("umj", "application/vnd.umajin");
    	extMap.put("unityweb", "application/vnd.unity");
    	extMap.put("uoml", "application/vnd.uoml+xml");
    	extMap.put("vcx", "application/vnd.vcx");
    	extMap.put("vsd", "application/vnd.visio");
    	extMap.put("vst", "application/vnd.visio");
    	extMap.put("vss", "application/vnd.visio");
    	extMap.put("vsw", "application/vnd.visio");
    	extMap.put("vis", "application/vnd.visionary");
    	extMap.put("vsf", "application/vnd.vsf");
    	extMap.put("wbxml", "application/vnd.wap.wbxml");
    	extMap.put("wmlc", "application/vnd.wap.wmlc");
    	extMap.put("wmlsc", "application/vnd.wap.wmlscriptc");
    	extMap.put("wtb", "application/vnd.webturbo");
    	extMap.put("nbp", "application/vnd.wolfram.player");
    	extMap.put("wpd", "application/vnd.wordperfect");
    	extMap.put("wqd", "application/vnd.wqd");
    	extMap.put("stf", "application/vnd.wt.stf");
    	extMap.put("xar", "application/vnd.xara");
    	extMap.put("xfdl", "application/vnd.xfdl");
    	extMap.put("hvd", "application/vnd.yamaha.hv-dic");
    	extMap.put("hvs", "application/vnd.yamaha.hv-script");
    	extMap.put("hvp", "application/vnd.yamaha.hv-voice");
    	extMap.put("osf", "application/vnd.yamaha.openscoreformat");
    	extMap.put("osfpvg", "application/vnd.yamaha.openscoreformat.osfpvg+xml");
    	extMap.put("saf", "application/vnd.yamaha.smaf-audio");
    	extMap.put("spf", "application/vnd.yamaha.smaf-phrase");
    	extMap.put("cmp", "application/vnd.yellowriver-custom-menu");
    	extMap.put("zir", "application/vnd.zul");
    	extMap.put("zirz", "application/vnd.zul");
    	extMap.put("zaz", "application/vnd.zzazz.deck+xml");
    	extMap.put("vxml", "application/voicexml+xml");
    	extMap.put("wgt", "application/widget");
    	extMap.put("hlp", "application/winhlp");
    	extMap.put("wsdl", "application/wsdl+xml");
    	extMap.put("wspolicy", "application/wspolicy+xml");
    	extMap.put("7z", "application/x-7z-compressed");
    	extMap.put("abw", "application/x-abiword");
    	extMap.put("ace", "application/x-ace-compressed");
    	extMap.put("dmg", "application/x-apple-diskimage");
    	extMap.put("aab", "application/x-authorware-bin");
    	extMap.put("x32", "application/x-authorware-bin");
    	extMap.put("u32", "application/x-authorware-bin");
    	extMap.put("vox", "application/x-authorware-bin");
    	extMap.put("aam", "application/x-authorware-map");
    	extMap.put("aas", "application/x-authorware-seg");
    	extMap.put("bcpio", "application/x-bcpio");
    	extMap.put("torrent", "application/x-bittorrent");
    	extMap.put("blb", "application/x-blorb");
    	extMap.put("blorb", "application/x-blorb");
    	extMap.put("bz", "application/x-bzip");
    	extMap.put("bz2", "application/x-bzip2");
    	extMap.put("boz", "application/x-bzip2");
    	extMap.put("cbr", "application/x-cbr");
    	extMap.put("cba", "application/x-cbr");
    	extMap.put("cbt", "application/x-cbr");
    	extMap.put("cbz", "application/x-cbr");
    	extMap.put("cb7", "application/x-cbr");
    	extMap.put("vcd", "application/x-cdlink");
    	extMap.put("cfs", "application/x-cfs-compressed");
    	extMap.put("chat", "application/x-chat");
    	extMap.put("pgn", "application/x-chess-pgn");
    	extMap.put("nsc", "application/x-conference");
    	extMap.put("cpio", "application/x-cpio");
    	extMap.put("csh", "application/x-csh");
    	extMap.put("deb", "application/x-debian-package");
    	extMap.put("udeb", "application/x-debian-package");
    	extMap.put("dgc", "application/x-dgc-compressed");
    	extMap.put("dir", "application/x-director");
    	extMap.put("dcr", "application/x-director");
    	extMap.put("dxr", "application/x-director");
    	extMap.put("cst", "application/x-director");
    	extMap.put("cct", "application/x-director");
    	extMap.put("cxt", "application/x-director");
    	extMap.put("w3d", "application/x-director");
    	extMap.put("fgd", "application/x-director");
    	extMap.put("swa", "application/x-director");
    	extMap.put("wad", "application/x-doom");
    	extMap.put("ncx", "application/x-dtbncx+xml");
    	extMap.put("dtb", "application/x-dtbook+xml");
    	extMap.put("res", "application/x-dtbresource+xml");
    	extMap.put("dvi", "application/x-dvi");
    	extMap.put("evy", "application/x-envoy");
    	extMap.put("eva", "application/x-eva");
    	extMap.put("bdf", "application/x-font-bdf");
    	extMap.put("gsf", "application/x-font-ghostscript");
    	extMap.put("psf", "application/x-font-linux-psf");
    	extMap.put("otf", "application/x-font-otf");
    	extMap.put("pcf", "application/x-font-pcf");
    	extMap.put("snf", "application/x-font-snf");
    	extMap.put("ttf", "application/x-font-ttf");
    	extMap.put("ttc", "application/x-font-ttf");
    	extMap.put("pfa", "application/x-font-type1");
    	extMap.put("pfb", "application/x-font-type1");
    	extMap.put("pfm", "application/x-font-type1");
    	extMap.put("afm", "application/x-font-type1");
    	extMap.put("woff", "application/x-font-woff");
    	extMap.put("arc", "application/x-freearc");
    	extMap.put("spl", "application/x-futuresplash");
    	extMap.put("gca", "application/x-gca-compressed");
    	extMap.put("ulx", "application/x-glulx");
    	extMap.put("gnumeric", "application/x-gnumeric");
    	extMap.put("gramps", "application/x-gramps-xml");
    	extMap.put("gtar", "application/x-gtar");
    	extMap.put("hdf", "application/x-hdf");
    	extMap.put("install", "application/x-install-instructions");
    	extMap.put("iso", "application/x-iso9660-image");
    	extMap.put("jnlp", "application/x-java-jnlp-file");
    	extMap.put("latex", "application/x-latex");
    	extMap.put("lzh", "application/x-lzh-compressed");
    	extMap.put("lha", "application/x-lzh-compressed");
    	extMap.put("mie", "application/x-mie");
    	extMap.put("prc", "application/x-mobipocket-ebook");
    	extMap.put("mobi", "application/x-mobipocket-ebook");
    	extMap.put("application", "application/x-ms-application");
    	extMap.put("lnk", "application/x-ms-shortcut");
    	extMap.put("wmd", "application/x-ms-wmd");
    	extMap.put("wmz", "application/x-ms-wmz");
    	extMap.put("xbap", "application/x-ms-xbap");
    	extMap.put("mdb", "application/x-msaccess");
    	extMap.put("obd", "application/x-msbinder");
    	extMap.put("crd", "application/x-mscardfile");
    	extMap.put("clp", "application/x-msclip");
    	extMap.put("exe", "application/x-msdownload");
    	extMap.put("dll", "application/x-msdownload");
    	extMap.put("com", "application/x-msdownload");
    	extMap.put("bat", "application/x-msdownload");
    	extMap.put("msi", "application/x-msdownload");
    	extMap.put("mvb", "application/x-msmediaview");
    	extMap.put("m13", "application/x-msmediaview");
    	extMap.put("m14", "application/x-msmediaview");
    	extMap.put("wmf", "application/x-msmetafile");
    	extMap.put("wmz", "application/x-msmetafile");
    	extMap.put("emf", "application/x-msmetafile");
    	extMap.put("emz", "application/x-msmetafile");
    	extMap.put("mny", "application/x-msmoney");
    	extMap.put("pub", "application/x-mspublisher");
    	extMap.put("scd", "application/x-msschedule");
    	extMap.put("trm", "application/x-msterminal");
    	extMap.put("wri", "application/x-mswrite");
    	extMap.put("nc", "application/x-netcdf");
    	extMap.put("cdf", "application/x-netcdf");
    	extMap.put("nzb", "application/x-nzb");
    	extMap.put("p12", "application/x-pkcs12");
    	extMap.put("pfx", "application/x-pkcs12");
    	extMap.put("p7b", "application/x-pkcs7-certificates");
    	extMap.put("spc", "application/x-pkcs7-certificates");
    	extMap.put("p7r", "application/x-pkcs7-certreqresp");
    	extMap.put("rar", "application/x-rar-compressed");
    	extMap.put("ris", "application/x-research-info-systems");
    	extMap.put("sh", "application/x-sh");
    	extMap.put("shar", "application/x-shar");
    	extMap.put("swf", "application/x-shockwave-flash");
    	extMap.put("xap", "application/x-silverlight-app");
    	extMap.put("sql", "application/x-sql");
    	extMap.put("sit", "application/x-stuffit");
    	extMap.put("sitx", "application/x-stuffitx");
    	extMap.put("srt", "application/x-subrip");
    	extMap.put("sv4cpio", "application/x-sv4cpio");
    	extMap.put("sv4crc", "application/x-sv4crc");
    	extMap.put("t3", "application/x-t3vm-image");
    	extMap.put("gam", "application/x-tads");
    	extMap.put("tar", "application/x-tar");
    	extMap.put("tcl", "application/x-tcl");
    	extMap.put("tex", "application/x-tex");
    	extMap.put("tfm", "application/x-tex-tfm");
    	extMap.put("texinfo", "application/x-texinfo");
    	extMap.put("texi", "application/x-texinfo");
    	extMap.put("obj", "application/x-tgif");
    	extMap.put("ustar", "application/x-ustar");
    	extMap.put("src", "application/x-wais-source");
    	extMap.put("der", "application/x-x509-ca-cert");
    	extMap.put("crt", "application/x-x509-ca-cert");
    	extMap.put("fig", "application/x-xfig");
    	extMap.put("xlf", "application/x-xliff+xml");
    	extMap.put("xpi", "application/x-xpinstall");
    	extMap.put("xz", "application/x-xz");
    	extMap.put("z1", "application/x-zmachine");
    	extMap.put("z2", "application/x-zmachine");
    	extMap.put("z3", "application/x-zmachine");
    	extMap.put("z4", "application/x-zmachine");
    	extMap.put("z5", "application/x-zmachine");
    	extMap.put("z6", "application/x-zmachine");
    	extMap.put("z7", "application/x-zmachine");
    	extMap.put("z8", "application/x-zmachine");
    	extMap.put("xaml", "application/xaml+xml");
    	extMap.put("xdf", "application/xcap-diff+xml");
    	extMap.put("xenc", "application/xenc+xml");
    	extMap.put("xhtml", "application/xhtml+xml");
    	extMap.put("xht", "application/xhtml+xml");
    	extMap.put("xml", "application/xml");
    	extMap.put("xsl", "application/xml");
    	extMap.put("dtd", "application/xml-dtd");
    	extMap.put("xop", "application/xop+xml");
    	extMap.put("xpl", "application/xproc+xml");
    	extMap.put("xslt", "application/xslt+xml");
    	extMap.put("xspf", "application/xspf+xml");
    	extMap.put("mxml", "application/xv+xml");
    	extMap.put("xhvml", "application/xv+xml");
    	extMap.put("xvml", "application/xv+xml");
    	extMap.put("xvm", "application/xv+xml");
    	extMap.put("yang", "application/yang");
    	extMap.put("yin", "application/yin+xml");
    	extMap.put("zip", "application/zip");
    	extMap.put("adp", "audio/adpcm");
    	extMap.put("au", "audio/basic");
    	extMap.put("snd", "audio/basic");
    	extMap.put("mid", "audio/midi");
    	extMap.put("midi", "audio/midi");
    	extMap.put("kar", "audio/midi");
    	extMap.put("rmi", "audio/midi");
    	extMap.put("mp4a", "audio/mp4");
    	extMap.put("mpga", "audio/mpeg");
    	extMap.put("mp2", "audio/mpeg");
    	extMap.put("mp2a", "audio/mpeg");
    	extMap.put("mp3", "audio/mpeg");
    	extMap.put("m2a", "audio/mpeg");
    	extMap.put("m3a", "audio/mpeg");
    	extMap.put("oga", "audio/ogg");
    	extMap.put("ogg", "audio/ogg");
    	extMap.put("spx", "audio/ogg");
    	extMap.put("s3m", "audio/s3m");
    	extMap.put("sil", "audio/silk");
    	extMap.put("uva", "audio/vnd.dece.audio");
    	extMap.put("uvva", "audio/vnd.dece.audio");
    	extMap.put("eol", "audio/vnd.digital-winds");
    	extMap.put("dra", "audio/vnd.dra");
    	extMap.put("dts", "audio/vnd.dts");
    	extMap.put("dtshd", "audio/vnd.dts.hd");
    	extMap.put("lvp", "audio/vnd.lucent.voice");
    	extMap.put("pya", "audio/vnd.ms-playready.media.pya");
    	extMap.put("ecelp4800", "audio/vnd.nuera.ecelp4800");
    	extMap.put("ecelp7470", "audio/vnd.nuera.ecelp7470");
    	extMap.put("ecelp9600", "audio/vnd.nuera.ecelp9600");
    	extMap.put("rip", "audio/vnd.rip");
    	extMap.put("weba", "audio/webm");
    	extMap.put("aac", "audio/x-aac");
    	extMap.put("aif", "audio/x-aiff");
    	extMap.put("aiff", "audio/x-aiff");
    	extMap.put("aifc", "audio/x-aiff");
    	extMap.put("caf", "audio/x-caf");
    	extMap.put("flac", "audio/x-flac");
    	extMap.put("mka", "audio/x-matroska");
    	extMap.put("m3u", "audio/x-mpegurl");
    	extMap.put("wax", "audio/x-ms-wax");
    	extMap.put("wma", "audio/x-ms-wma");
    	extMap.put("ram", "audio/x-pn-realaudio");
    	extMap.put("ra", "audio/x-pn-realaudio");
    	extMap.put("rmp", "audio/x-pn-realaudio-plugin");
    	extMap.put("wav", "audio/x-wav");
    	extMap.put("xm", "audio/xm");
    	extMap.put("cdx", "chemical/x-cdx");
    	extMap.put("cif", "chemical/x-cif");
    	extMap.put("cmdf", "chemical/x-cmdf");
    	extMap.put("cml", "chemical/x-cml");
    	extMap.put("csml", "chemical/x-csml");
    	extMap.put("xyz", "chemical/x-xyz");
    	extMap.put("bmp", "image/bmp");
    	extMap.put("cgm", "image/cgm");
    	extMap.put("g3", "image/g3fax");
    	extMap.put("gif", "image/gif");
    	extMap.put("ief", "image/ief");
    	extMap.put("jpeg", "image/jpeg");
    	extMap.put("jpg", "image/jpeg");
    	extMap.put("jpe", "image/jpeg");
    	extMap.put("ktx", "image/ktx");
    	extMap.put("png", "image/png");
    	extMap.put("btif", "image/prs.btif");
    	extMap.put("sgi", "image/sgi");
    	extMap.put("svg", "image/svg+xml");
    	extMap.put("svgz", "image/svg+xml");
    	extMap.put("tiff", "image/tiff");
    	extMap.put("tif", "image/tiff");
    	extMap.put("psd", "image/vnd.adobe.photoshop");
    	extMap.put("uvi", "image/vnd.dece.graphic");
    	extMap.put("uvvi", "image/vnd.dece.graphic");
    	extMap.put("uvg", "image/vnd.dece.graphic");
    	extMap.put("uvvg", "image/vnd.dece.graphic");
    	extMap.put("sub", "image/vnd.dvb.subtitle");
    	extMap.put("djvu", "image/vnd.djvu");
    	extMap.put("djv", "image/vnd.djvu");
    	extMap.put("dwg", "image/vnd.dwg");
    	extMap.put("dxf", "image/vnd.dxf");
    	extMap.put("fbs", "image/vnd.fastbidsheet");
    	extMap.put("fpx", "image/vnd.fpx");
    	extMap.put("fst", "image/vnd.fst");
    	extMap.put("mmr", "image/vnd.fujixerox.edmics-mmr");
    	extMap.put("rlc", "image/vnd.fujixerox.edmics-rlc");
    	extMap.put("mdi", "image/vnd.ms-modi");
    	extMap.put("wdp", "image/vnd.ms-photo");
    	extMap.put("npx", "image/vnd.net-fpx");
    	extMap.put("wbmp", "image/vnd.wap.wbmp");
    	extMap.put("xif", "image/vnd.xiff");
    	extMap.put("webp", "image/webp");
    	extMap.put("3ds", "image/x-3ds");
    	extMap.put("ras", "image/x-cmu-raster");
    	extMap.put("cmx", "image/x-cmx");
    	extMap.put("fh", "image/x-freehand");
    	extMap.put("fhc", "image/x-freehand");
    	extMap.put("fh4", "image/x-freehand");
    	extMap.put("fh5", "image/x-freehand");
    	extMap.put("fh7", "image/x-freehand");
    	extMap.put("ico", "image/x-icon");
    	extMap.put("sid", "image/x-mrsid-image");
    	extMap.put("pcx", "image/x-pcx");
    	extMap.put("pic", "image/x-pict");
    	extMap.put("pct", "image/x-pict");
    	extMap.put("pnm", "image/x-portable-anymap");
    	extMap.put("pbm", "image/x-portable-bitmap");
    	extMap.put("pgm", "image/x-portable-graymap");
    	extMap.put("ppm", "image/x-portable-pixmap");
    	extMap.put("rgb", "image/x-rgb");
    	extMap.put("tga", "image/x-tga");
    	extMap.put("xbm", "image/x-xbitmap");
    	extMap.put("xpm", "image/x-xpixmap");
    	extMap.put("xwd", "image/x-xwindowdump");
    	extMap.put("eml", "message/rfc822");
    	extMap.put("mime", "message/rfc822");
    	extMap.put("igs", "model/iges");
    	extMap.put("iges", "model/iges");
    	extMap.put("msh", "model/mesh");
    	extMap.put("mesh", "model/mesh");
    	extMap.put("silo", "model/mesh");
    	extMap.put("dae", "model/vnd.collada+xml");
    	extMap.put("dwf", "model/vnd.dwf");
    	extMap.put("gdl", "model/vnd.gdl");
    	extMap.put("gtw", "model/vnd.gtw");
    	extMap.put("mts", "model/vnd.mts");
    	extMap.put("vtu", "model/vnd.vtu");
    	extMap.put("wrl", "model/vrml");
    	extMap.put("vrml", "model/vrml");
    	extMap.put("x3db", "model/x3d+binary");
    	extMap.put("x3dbz", "model/x3d+binary");
    	extMap.put("x3dv", "model/x3d+vrml");
    	extMap.put("x3dvz", "model/x3d+vrml");
    	extMap.put("x3d", "model/x3d+xml");
    	extMap.put("x3dz", "model/x3d+xml");
    	extMap.put("appcache", "text/cache-manifest");
    	extMap.put("ics", "text/calendar");
    	extMap.put("ifb", "text/calendar");
    	extMap.put("css", "text/css");
        extMap.put("scss", "text/scss");
    	extMap.put("csv", "text/csv");
    	extMap.put("html", "text/html");
    	extMap.put("htm", "text/html");
    	extMap.put("n3", "text/n3");
    	extMap.put("txt", "text/plain");
    	extMap.put("text", "text/plain");
    	extMap.put("conf", "text/plain");
    	extMap.put("def", "text/plain");
    	extMap.put("list", "text/plain");
    	extMap.put("log", "text/plain");
    	extMap.put("in", "text/plain");
    	extMap.put("dsc", "text/prs.lines.tag");
    	extMap.put("rtx", "text/richtext");
    	extMap.put("sgml", "text/sgml");
    	extMap.put("sgm", "text/sgml");
    	extMap.put("tsv", "text/tab-separated-values");
    	extMap.put("t", "text/troff");
    	extMap.put("tr", "text/troff");
    	extMap.put("roff", "text/troff");
    	extMap.put("man", "text/troff");
    	extMap.put("me", "text/troff");
    	extMap.put("ms", "text/troff");
    	extMap.put("ttl", "text/turtle");
    	extMap.put("uri", "text/uri-list");
    	extMap.put("uris", "text/uri-list");
    	extMap.put("urls", "text/uri-list");
    	extMap.put("vcard", "text/vcard");
    	extMap.put("curl", "text/vnd.curl");
    	extMap.put("dcurl", "text/vnd.curl.dcurl");
    	extMap.put("scurl", "text/vnd.curl.scurl");
    	extMap.put("mcurl", "text/vnd.curl.mcurl");
    	extMap.put("sub", "text/vnd.dvb.subtitle");
    	extMap.put("fly", "text/vnd.fly");
    	extMap.put("flx", "text/vnd.fmi.flexstor");
    	extMap.put("gv", "text/vnd.graphviz");
    	extMap.put("3dml", "text/vnd.in3d.3dml");
    	extMap.put("spot", "text/vnd.in3d.spot");
    	extMap.put("jad", "text/vnd.sun.j2me.app-descriptor");
    	extMap.put("wml", "text/vnd.wap.wml");
    	extMap.put("wmls", "text/vnd.wap.wmlscript");
    	extMap.put("s", "text/x-asm");
    	extMap.put("asm", "text/x-asm");
    	extMap.put("c", "text/x-c");
    	extMap.put("cc", "text/x-c");
    	extMap.put("cxx", "text/x-c");
    	extMap.put("cpp", "text/x-c");
    	extMap.put("h", "text/x-c");
    	extMap.put("hh", "text/x-c");
    	extMap.put("dic", "text/x-c");
    	extMap.put("f", "text/x-fortran");
    	extMap.put("for", "text/x-fortran");
    	extMap.put("f77", "text/x-fortran");
    	extMap.put("f90", "text/x-fortran");
    	extMap.put("java", "text/x-java-source");
    	extMap.put("opml", "text/x-opml");
    	extMap.put("p", "text/x-pascal");
    	extMap.put("pas", "text/x-pascal");
    	extMap.put("nfo", "text/x-nfo");
    	extMap.put("etx", "text/x-setext");
    	extMap.put("sfv", "text/x-sfv");
    	extMap.put("uu", "text/x-uuencode");
    	extMap.put("vcs", "text/x-vcalendar");
    	extMap.put("vcf", "text/x-vcard");
    	extMap.put("3gp", "video/3gpp");
    	extMap.put("3g2", "video/3gpp2");
    	extMap.put("h261", "video/h261");
    	extMap.put("h263", "video/h263");
    	extMap.put("h264", "video/h264");
    	extMap.put("jpgv", "video/jpeg");
    	extMap.put("jpm", "video/jpm");
    	extMap.put("jpgm", "video/jpm");
    	extMap.put("mj2", "video/mj2");
    	extMap.put("mjp2", "video/mj2");
    	extMap.put("mp4", "video/mp4");
    	extMap.put("mp4v", "video/mp4");
    	extMap.put("mpg4", "video/mp4");
    	extMap.put("mpeg", "video/mpeg");
    	extMap.put("mpg", "video/mpeg");
    	extMap.put("mpe", "video/mpeg");
    	extMap.put("m1v", "video/mpeg");
    	extMap.put("m2v", "video/mpeg");
    	extMap.put("ogv", "video/ogg");
    	extMap.put("qt", "video/quicktime");
    	extMap.put("mov", "video/quicktime");
    	extMap.put("uvh", "video/vnd.dece.hd");
    	extMap.put("uvvh", "video/vnd.dece.hd");
    	extMap.put("uvm", "video/vnd.dece.mobile");
    	extMap.put("uvvm", "video/vnd.dece.mobile");
    	extMap.put("uvp", "video/vnd.dece.pd");
    	extMap.put("uvvp", "video/vnd.dece.pd");
    	extMap.put("uvs", "video/vnd.dece.sd");
    	extMap.put("uvvs", "video/vnd.dece.sd");
    	extMap.put("uvv", "video/vnd.dece.video");
    	extMap.put("uvvv", "video/vnd.dece.video");
    	extMap.put("dvb", "video/vnd.dvb.file");
    	extMap.put("fvt", "video/vnd.fvt");
    	extMap.put("mxu", "video/vnd.mpegurl");
    	extMap.put("m4u", "video/vnd.mpegurl");
    	extMap.put("pyv", "video/vnd.ms-playready.media.pyv");
    	extMap.put("uvu", "video/vnd.uvvu.mp4");
    	extMap.put("uvvu", "video/vnd.uvvu.mp4");
    	extMap.put("viv", "video/vnd.vivo");
    	extMap.put("webm", "video/webm");
    	extMap.put("f4v", "video/x-f4v");
    	extMap.put("fli", "video/x-fli");
    	extMap.put("flv", "video/x-flv");
    	extMap.put("m4v", "video/x-m4v");
    	extMap.put("mkv", "video/x-matroska");
    	extMap.put("mk3d", "video/x-matroska");
    	extMap.put("mks", "video/x-matroska");
    	extMap.put("mng", "video/x-mng");
    	extMap.put("asf", "video/x-ms-asf");
    	extMap.put("asx", "video/x-ms-asf");
    	extMap.put("vob", "video/x-ms-vob");
    	extMap.put("wm", "video/x-ms-wm");
    	extMap.put("wmv", "video/x-ms-wmv");
    	extMap.put("wmx", "video/x-ms-wmx");
    	extMap.put("wvx", "video/x-ms-wvx");
    	extMap.put("avi", "video/x-msvideo");
    	extMap.put("movie", "video/x-sgi-movie");
    	extMap.put("smv", "video/x-smv");
    	extMap.put("ice", "x-conference/x-cooltalk");
    	
    	extensions = Collections.unmodifiableMap(extMap);
    }
}
