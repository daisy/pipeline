package org.daisy.pipeline.client.models;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "alive" response from the Pipeline 2 Web Service.
 */
public class Alive {
	
	public Boolean error = null;
	public Boolean authentication = null;
	public Boolean localfs = null;
	public String version = null;
	
	// ---------- Constructors ----------
	
	/**
	 * Parse the "alive"-XML described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/alive.xml
	 * 
	 * @param aliveXml the XML
	 * @throws Pipeline2Exception thrown when an error occurs
	 */
	public Alive(Node aliveXml) throws Pipeline2Exception {
		parseAliveXml(aliveXml);
	}
	
	private void parseAliveXml(Node aliveXml) throws Pipeline2Exception {
		if (XPath.selectNode("/d:error", aliveXml, XPath.dp2ns) != null) {
			error = true;
			return;
		}
		error = false;
		
		// select root element if the node is a document node
		if (aliveXml instanceof Document)
			aliveXml = XPath.selectNode("/d:alive", aliveXml, XPath.dp2ns);
		
		this.authentication = "true".equals(XPath.selectText("@authentication", aliveXml, XPath.dp2ns));
		this.localfs = "true".equals(XPath.selectText("@localfs", aliveXml, XPath.dp2ns));
		this.version = XPath.selectText("@version", aliveXml, XPath.dp2ns);
	}
	
}
