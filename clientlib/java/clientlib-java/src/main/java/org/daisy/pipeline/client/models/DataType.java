package org.daisy.pipeline.client.models;

import java.util.HashMap;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XPath;
import org.daisy.pipeline.client.models.datatypes.EnumType;
import org.daisy.pipeline.client.models.datatypes.RegexType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;


/**
 * A representation of a Pipeline 2 data type.
 */
public class DataType implements Comparable<DataType> {

	public Node dataTypeXml;
	public String id;

	public DataType(Node dataTypeXml) {
		try {
			// select root element if the node is a document node
			if (dataTypeXml instanceof Document)
				dataTypeXml = XPath.selectNode("/*", dataTypeXml, XPath.dp2ns);

			this.dataTypeXml = dataTypeXml;
			this.id = XPath.selectText("@id", dataTypeXml, XPath.dp2ns);

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse datatype XML", e);
		}
	}

	/** Get a key/value map of all datatypes available. Datatype ID as key, datatype URL as value.
	 * 
	 *  @param dataTypesXml The XML
	 *  @return The key/value map of all the datatypes
	 */
	public static Map<String,String> getDataTypes(Node dataTypesXml) {
		Map<String,String> dataTypes = new HashMap<String,String>();

		try {
			// select root element if the node is a document node
			if (dataTypesXml instanceof Document)
				dataTypesXml = XPath.selectNode("/*", dataTypesXml, XPath.dp2ns);

			for (Node dataTypeNode : XPath.selectNodes("d:datatype", dataTypesXml, XPath.dp2ns)) {
				String id = XPath.selectText("@id", dataTypeNode, XPath.dp2ns);
				String href = XPath.selectText("@href", dataTypeNode, XPath.dp2ns);
				dataTypes.put(id, href);
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse datatype XML", e);
		}

		return dataTypes;
	}

	/** Parse the datatype and return as an EnumType or RegexType instance if possible.
	 * 
	 *  @param dataTypeXml The XML
	 *  @return The DataType
	 */
	public static DataType getDataType(Node dataTypeXml) {
		try {
			// select root element if the node is a document node
			if (dataTypeXml instanceof Document)
				dataTypeXml = XPath.selectNode("/*", dataTypeXml, XPath.dp2ns);

			if ("choice".equals(dataTypeXml.getLocalName())) {
				return new EnumType(dataTypeXml);

			} else if ("data".equals(dataTypeXml.getLocalName()) && "pattern".equals(XPath.selectText("*[local-name()='param']/@name", dataTypeXml, XPath.dp2ns))) {
				return new RegexType(dataTypeXml);

			} else {
				return new DataType(dataTypeXml);
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse datatype XML", e);
		}

		return null;
	}

	public int compareTo(DataType other) {
		if (id == null) return 1;
		if (other.id == null) return -1;
		return id.compareTo(other.id);
	}

}
