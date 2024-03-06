package org.daisy.pipeline.braille.liblouis.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.ValidationResult;

import org.liblouis.Louis;
import org.liblouis.Table;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisTableDatatypeService",
	service = {
		DatatypeService.class
	}
)
public class LiblouisTableDatatypeService extends DatatypeService {

	protected LiblouisTableDatatypeService() {
		super("liblouis-table-query");
	}

	private Document xmlDefinition = null;
	private List<String> enumerationValues = null;

	@Reference(
		name = "LiblouisTableJnaImplProvider",
		unbind = "-",
		service = LiblouisTableJnaImplProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindLiblouisTableJnaImplProvider(LiblouisTableJnaImplProvider provider) {
		// dependency needed because LiblouisTableJnaImplProvider.activate() initializes Louis
	}

	public Document asDocument() throws Exception {
		if (xmlDefinition == null)
			createDatatype();
		return xmlDefinition;
	}

	public ValidationResult validate(String content) {
		if (enumerationValues == null)
			try {
				createDatatype();
			} catch (Exception e) {
				return ValidationResult.notValid("Failed to determine allowed values");
			}
		if (enumerationValues.contains(content))
			return ValidationResult.valid();
		else
			return ValidationResult.notValid("'" + content + "' is not in the list of allowed values.");
	}

	private void createDatatype() throws ParserConfigurationException, DOMException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		                                     .getDOMImplementation().createDocument(null, "choice", null);
		List<String> values = new ArrayList<>();
		Element choice = doc.getDocumentElement();
		String defaultValue = "";
		values.add(defaultValue);
		choice.appendChild(doc.createElement("value"))
		      .appendChild(doc.createTextNode(defaultValue));
		choice.appendChild(doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0", "documentation"))
		      .appendChild(doc.createTextNode("-"));
		List<Table> tables = new ArrayList<>();
		tables.addAll(Louis.listTables());
		Collections.sort(tables,
		                 new Comparator<Table>() {
				public int compare(Table o1, Table o2) {
					String s1 = o1.getInfo().get("index-name");
					String s2 = o2.getInfo().get("index-name");
					if (s1 == null)
						if (s2 == null)
							return 0;
						else
							return -1;
					else if (s2 == null)
						return 1;
					else
						return s1.toLowerCase().compareTo(s2.toLowerCase()); }});
		for (Table table : tables) {
			// We can only do this because we know the identifier can be used to construct a new
			// Translator, but this is not an official feature. A more correct solution would be to
			// cache the Table objects and use Table.getTranslator() to get the Translator.
			String value = "(liblouis-table:\"" + table.getIdentifier().replace("http://www.liblouis.org/tables/", "") + "\")";
			values.add(value);
			choice.appendChild(doc.createElement("value"))
			      .appendChild(doc.createTextNode(value));
			String indexName = table.getInfo().get("index-name");
			if (indexName != null) {
				choice.appendChild(doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0", "documentation"))
				      .appendChild(doc.createTextNode(indexName));
			}
		}
		xmlDefinition = doc;
		enumerationValues = values;
	}
}
