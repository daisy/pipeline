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
	name = "org.daisy.pipeline.css.impl.StylesheetParametersDatatypeService",
	service = {
		DatatypeService.class
	}
)
public class StylesheetParametersDatatypeService extends DatatypeService {

	private final Pattern pattern;
	private Document xmlDefinition = null;

	protected StylesheetParametersDatatypeService() {
		super("stylesheet-parameters");
		pattern = StylesheetParametersParser.asRegex();
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
				"A Sass map"                                                                         + "\n" +
				                                                                                       "\n" +
				"A [Sass map](https://sass-lang.com/documentation/values/maps/) is a set of pairs"   + "\n" +
				"of keys and values written as:"                                                     + "\n" +
				                                                                                       "\n" +
				"    (<expression>: <expression>, <expression>: <expression>, ...)"                  + "\n" +
				                                                                                       "\n" +
				"where the"                                                                          + "\n" +
				"[expression](https://sass-lang.com/documentation/syntax/structure#expressions)"     + "\n" +
				"before the `:` is the key, and the expression after is the value associated"        + "\n" +
				"with that key. The keys must be unique. An empty map is written `()`."));
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
