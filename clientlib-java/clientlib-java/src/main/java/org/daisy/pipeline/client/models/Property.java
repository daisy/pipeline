package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "/admin/properties" response from the Pipeline 2 Web Service.
 * 
 * Example XML:
 * {@code
 * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * <properties href="http://localhost:8181/ws/admin/properties" xmlns="http://www.daisy.org/ns/pipeline/data">
 *     <property bundleId="36" bundleName="org.daisy.pipeline.persistence-derby" name="javax.persistence.jdbc.driver" value="org.apache.derby.jdbc.EmbeddedDriver"/>
 *     <property bundleId="14" bundleName="org.daisy.pipeline.webservice" name="org.daisy.pipeline.ws.host" value="localhost"/>
 *     <property bundleId="14" bundleName="org.daisy.pipeline.webservice" name="org.daisy.pipeline.ws.path" value="/ws"/>
 *     <property bundleId="14" bundleName="org.daisy.pipeline.webservice" name="org.daisy.pipeline.ws.port" value="8181"/>
 * </properties>
 * }
 */
public class Property {

	public String name;
	public String value;
	public long bundleId;
	public String bundleName;

	public static List<Property> parsePropertiesXml(Node propertiesNode) {
		List<Property> properties = new ArrayList<Property>();

		try {
			// select root element if the node is a document node
			if (propertiesNode instanceof Document)
				propertiesNode = XPath.selectNode("/d:jobs", propertiesNode, XPath.dp2ns);

			List<Node> propertyNodes = XPath.selectNodes("d:job", propertiesNode, XPath.dp2ns);
			for (Node propertyNode : propertyNodes) {
				Property property = new Property();
				property.name = XPath.selectText("@name", propertyNode, XPath.dp2ns);
				property.value = XPath.selectText("@value", propertyNode, XPath.dp2ns);
				property.bundleId = Long.valueOf(XPath.selectText("@bundleId", propertyNode, XPath.dp2ns));
				property.bundleName = XPath.selectText("@bundleName", propertyNode, XPath.dp2ns);
				properties.add(property);
			}

		} catch (Exception e) {
			Pipeline2Logger.logger().error("Failed to parse the properties XML", e);
		}
		
		return properties;
	}

}
