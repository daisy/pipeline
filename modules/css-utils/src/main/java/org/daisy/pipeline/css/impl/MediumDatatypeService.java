package org.daisy.pipeline.css.impl;

import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.ValidationResult;

import org.osgi.service.component.annotations.Component;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Component(
	name = "org.daisy.pipeline.css.impl.MediumDatatypeService",
	service = {
		DatatypeService.class
	}
)
public class MediumDatatypeService extends DatatypeService {

	private final Pattern pattern;
	private Document xmlDefinition = null;

	public MediumDatatypeService() {
		super("medium");
		pattern = MediumParser.asRegex();
	}

	public ValidationResult validate(String content) {
		if (pattern.matcher(content).matches())
			return ValidationResult.valid();
		else
			return ValidationResult.notValid(
				"'" + content + "' does not match the regular expression pattern /" + pattern + "/");
	}

	public Document asDocument() throws Exception {
		if (xmlDefinition == null) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
			                                     .getDOMImplementation().createDocument(null, "data", null);
			Element data = doc.getDocumentElement();
			Attr type = doc.createAttribute("type");
			type.setValue("string");
			data.setAttributeNode(type);
			Element documentation = (Element)data.appendChild(
				doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0",
				                    "documentation"));
			Attr lang = doc.createAttributeNS(XMLConstants.XML_NS_URI, "lang");
			lang.setValue("en");
			documentation.setAttributeNode(lang);
			Attr space = doc.createAttributeNS(XMLConstants.XML_NS_URI, "space");
			space.setValue("preserve");
			documentation.setAttributeNode(space);
			documentation.appendChild(doc.createTextNode(
				"A medium specification"                                                             + "\n" +
				                                                                                       "\n" +
				"A medium is specified using a resticted version of the "                            + "\n" +
				"[media query syntax](https://www.w3.org/TR/mediaqueries-4/), that does not allow "  + "\n" +
				"the `not` keyword or `min-` and `max-` prefixes."));
			Element param = (Element)data.appendChild(doc.createElement("param"));
			Attr name = doc.createAttribute("name");
			name.setValue("pattern");
			param.setAttributeNode(name);
			param.appendChild(doc.createTextNode("^(" + pattern.pattern().replaceAll("\\(\\?<[^>]+>", "(") + ")$"));
			xmlDefinition = doc;
		}
		return xmlDefinition;
	}
}
