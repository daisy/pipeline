package org.daisy.pipeline.braille.pef.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.pipeline.braille.pef.TableRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.ValidationResult;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

@Component(
	name = "org.daisy.pipeline.braille.pef.impl.TableDatatypeService",
	service = {
		DatatypeService.class
	}
)
public class TableDatatypeService extends DatatypeService {

	public TableDatatypeService() {
		super("preview-table");
	}

	private TableRegistry registry;

	@Reference(
		name = "TableRegistry",
		unbind = "-",
		service = TableRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setTableRegistry(TableRegistry registry) {
		this.registry = registry;
	}

	public Document asDocument() throws Exception {
		// re-computing datatype every time because tables may have been added to the list
		return createDatatype(registry.list());
	}

	public ValidationResult validate(String content) {
		try {
			// re-computing datatype every time because tables may have been added to the list
			if (enumerateValues(registry.list()).contains(content))
				return ValidationResult.valid();
			else
				return ValidationResult.notValid("'" + content + "' is not in the list of allowed values.");
		} catch (Exception e) {
			return ValidationResult.notValid("Failed to determine allowed values");
		}
	}

	private static final String defaultValue = "";

	/**
	 * List all options, starting with the default (empty string), followed by the given tables
	 */
	private static List<String> enumerateValues(Collection<FactoryProperties> tables) {
		List<String> values = new ArrayList<>();
		values.add(defaultValue);
		for (FactoryProperties t : tables)
			values.add("(id:\"" + t.getIdentifier() + "\")");
		return values;
	}

	private static Document createDatatype(Collection<FactoryProperties> tables) throws ParserConfigurationException, DOMException {
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		                                     .getDOMImplementation().createDocument(null, "choice", null);
		Element choice = doc.getDocumentElement();
		choice.appendChild(doc.createElement("value"))
		      .appendChild(doc.createTextNode(defaultValue));
		choice.appendChild(doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0",
		                                       "documentation"))
		      .appendChild(doc.createTextNode("-"));
		// order tables by display name
		List<FactoryProperties> sortedTables = new ArrayList<>(tables);
		Collections.sort(sortedTables,
		                 new Comparator<FactoryProperties>() {
				@Override
				public int compare(FactoryProperties o1, FactoryProperties o2) {
					String s1 = o1.getDisplayName();
					String s2 = o2.getDisplayName();
					if (s1 == null)
						if (s2 == null)
							return 0;
						else
							return -1;
					else if (s2 == null)
						return 1;
					else
						return s1.toLowerCase().compareTo(s2.toLowerCase()); }});
		for (FactoryProperties table : tables) {
			choice.appendChild(doc.createElement("value"))
			      .appendChild(doc.createTextNode("(id:\"" + table.getIdentifier() + "\")"));
			String desc = table.getDisplayName();
			if (desc != null) {
				if (table.getDescription() != null && !"".equals(table.getDescription()))
					desc += ("\n" + table.getDescription());
				choice.appendChild(doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0",
				                                       "documentation"))
				      .appendChild(doc.createTextNode(desc));
			}
		}
		return doc;
	}
}
