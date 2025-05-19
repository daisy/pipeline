package org.daisy.pipeline.webservice.request;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import org.daisy.pipeline.webservice.xml.XmlUtils;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.restlet.data.Parameter;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PropertyRequest extends Request {

	public String getName() { return name; }
	public String getValue() { return value; }

	/**
	 * Parse an XML document
	 */
	public static PropertyRequest fromXML(String xml) throws IllegalArgumentException {
		return fromXML(parseXML(xml));
	}

	public static PropertyRequest fromXML(Document xml) throws IllegalArgumentException {
		return fromXML(xml, null);
	}

	/**
	 * @param name If not <code>null</code>, will be used as the property name. If <code>null</code>, the
	 * name must be provided through in the request XML.
	 */
	public static PropertyRequest fromXML(String xml, String name) throws IllegalArgumentException {
		return fromXML(parseXML(xml), name);
	}

	public static PropertyRequest fromXML(Document xml, String name) throws IllegalArgumentException {
		if (!XmlValidator.validate(xml, XmlValidator.PROPERTY_SCHEMA_URL))
			throw new IllegalArgumentException("Supplied XML is not a valid property request");
		try {
			PropertyRequest req = new PropertyRequest();
			Element root = xml.getDocumentElement();
			Attr attr = root.getAttributeNode("name");
			req.name = attr != null ? attr.getValue() : null;
			attr = root.getAttributeNode("value");
			if (attr != null)
				req.value = attr.getValue();
			else {
				Element xmlContent = null; {
					NodeList children = root.getChildNodes();
					for (int i = 0; i < children.getLength(); i++) {
						Node n = children.item(i);
						switch (n.getNodeType()) {
						case Node.ELEMENT_NODE:
							if (xmlContent != null)
								throw new IllegalArgumentException(
									"'property' element has more than one child element");
							xmlContent = (Element)n;
							break;
						case Node.TEXT_NODE:
							if (!n.getTextContent().trim().isEmpty())
								throw new IllegalArgumentException(
									"'property' element contains text");
							break;
						case Node.COMMENT_NODE:
							break;
						default:
							throw new IllegalArgumentException(
								"'property' contains unexpected nodes");
						}
					}
				}
				if (xmlContent == null)
					throw new IllegalArgumentException(
						"'property' element missing 'value' attribute or child element");
				req.value = XmlUtils.nodeToString(xmlContent);
			}
			req.validate(name);
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied XML is not a valid property request: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse a query string
	 */
	public static PropertyRequest fromQuery(Iterable<Parameter> query) throws IllegalArgumentException {
		return fromQuery(query, null);
	}

	/**
	 * @param id If not <code>null</code>, will be used as the property name. If <code>null</code>, the
	 * name must be provided through in the request XML.
	 */
	public static PropertyRequest fromQuery(Iterable<Parameter> query, String name) throws IllegalArgumentException {
		try {
			PropertyRequest req = fromQuery(query, PropertyRequest.class, GSON);
			req.validate(name);
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied query is not a valid property request: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse a JSON string
	 */
	public static PropertyRequest fromJSON(String json) throws IllegalArgumentException {
		return fromJSON(json, null);
	}

	/**
	 * @param name If not <code>null</code>, will be used as the property name. If <code>null</code>, the
	 * name must be provided through in the request XML.
	 */
	public static PropertyRequest fromJSON(String json, String name) throws IllegalArgumentException {
		try {
			JsonElement j = JsonParser.parseString(json);
			checkNoUnexistingFields(j, PropertyRequest.class);
			PropertyRequest req = GSON.fromJson(j, PropertyRequest.class);
			req.validate(name);
			return req;
		} catch (JsonParseException|IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied JSON is not a valid property request: " + e.getMessage(), e);
		}
	}

	private String name;
	private String href;
	private String description;
	@JsonRequired
	private String value;

	private void validate(String name) throws IllegalArgumentException {
		checkRequiredFields();
		if (name != null) {
			if (this.name != null && !name.equals(this.name))
				throw new IllegalArgumentException("name mismatch");
			this.name = name;
		} else if (this.name == null)
			throw new IllegalArgumentException("no name was provided");
	}

	private final static Gson GSON = new GsonBuilder().create();

}
