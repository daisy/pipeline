package org.daisy.pipeline.webservice.xml;

import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;

public class ScriptXmlWriter {
	
	private final String baseUrl;
	private Script script = null;
	private boolean details = false;
	private static Logger logger = LoggerFactory.getLogger(ScriptXmlWriter.class.getName());


	/**
	 * @param baseUrl Prefix to be included at the beginning of <code>href</code>
	 *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
	 *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
	 *                absolute paths relative to the domain name.
	 */
	public ScriptXmlWriter(Script script, String baseUrl) {
		this.script = script;
		this.baseUrl = baseUrl;
	}
	
	public ScriptXmlWriter withDetails() {
		details = true;
		return this;
	}
	
	public Document getXmlDocument() {
		if (script == null) {
			logger.warn("Could not create XML for null script");
			return null;
		}
		return scriptToXmlDocument(script);
	}
	
	// instead of creating a standalone XML document, add an element to an existing document
	public void addAsElementChild(Element parent) {
		Document doc = parent.getOwnerDocument();
		Element scriptElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "script");
		addElementData(script, scriptElm);
		parent.appendChild(scriptElm);
	}
	
	private Document scriptToXmlDocument(Script script) {
		Document doc = XmlUtils.createDom("script");
		Element scriptElm = doc.getDocumentElement();
		addElementData(script, scriptElm);
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.SCRIPT_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.nodeToString(doc));
		}

		return doc;
	}

	// element is <script> but it's empty
	private void addElementData(Script script, Element element) {
		Document doc = element.getOwnerDocument();
		String scriptHref = baseUrl + Routes.SCRIPT_ROUTE.replaceFirst("\\{id\\}", script.getId());

		element.setAttribute("id", script.getId());
		element.setAttribute("href", scriptHref);
		Joiner joiner = Joiner.on(" ");
		if(!Iterables.isEmpty(script.getInputFilesets())){
			element.setAttribute("input-filesets",joiner.join(script.getInputFilesets()));
		}


		if(!Iterables.isEmpty(script.getOutputFilesets())){
			element.setAttribute("output-filesets",joiner.join(script.getOutputFilesets()));
		}

		Element nicenameElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "nicename");
		nicenameElm.setTextContent(script.getName());
		element.appendChild(nicenameElm);

		Element descriptionElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "description");
		descriptionElm.setTextContent(script.getDescription());
		element.appendChild(descriptionElm);

		Element versionElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "version");
		versionElm.setTextContent(script.getVersion());
		element.appendChild(versionElm);

		if (details) {
			String homepage = script.getHomepage();
			if (homepage != null && homepage.trim().length() > 0) {
				Element homepageElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "homepage");
				homepageElm.setTextContent(homepage);
				element.appendChild(homepageElm);
			}
			
			addInputPorts(script.getInputPorts(), element);
			addOptions(script.getOptions(), element);
		}
	}
	
	private static void addInputPorts(Iterable<ScriptPort> ports, Element parent) {
		Document doc = parent.getOwnerDocument();
		for (ScriptPort port : ports) {
			Element inputElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "input");
			inputElm.setAttribute("name", port.getName());
	                inputElm.setAttribute("nicename", port.getNiceName());
			inputElm.setAttribute("required", Boolean.toString(port.isRequired()));
			inputElm.setAttribute("sequence", Boolean.toString(port.isSequence()));
			if (port.getMediaType() != null && !port.getMediaType().isEmpty()) {
				inputElm.setAttribute("mediaType", port.getMediaType());
			}
			inputElm.setAttribute("desc", port.getDescription());

			parent.appendChild(inputElm);
		}
	}

	private static void addOptions(Iterable<ScriptOption> options, Element parent) {
		Document doc = parent.getOwnerDocument();
		for (ScriptOption option : options) {
			Element optionElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "option");
			optionElm.setAttribute("name", option.getName().toString());
			optionElm.setAttribute("nicename", option.getNiceName());
			optionElm.setAttribute("required", Boolean.toString(option.isRequired()));
			
			optionElm.setAttribute("type", option.getType().getId());
			if (option.getMediaType() != null && !option.getMediaType().isEmpty()) {
				optionElm.setAttribute("mediaType", option.getMediaType());
			}
			optionElm.setAttribute("desc", option.getDescription());
			optionElm.setAttribute("ordered", Boolean.toString(option.isOrdered()));
			optionElm.setAttribute("sequence", Boolean.toString(option.isSequence()));
			if (!option.isRequired())
				optionElm.setAttribute("default", option.getDefault());
			if (option.getRole() != null)
				optionElm.setAttribute("role", option.getRole().toString());
			parent.appendChild(optionElm);
		}
	}
}
