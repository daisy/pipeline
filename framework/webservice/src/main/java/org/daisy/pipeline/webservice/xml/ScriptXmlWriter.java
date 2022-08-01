package org.daisy.pipeline.webservice.xml;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcOptionMetadata.Output;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.webservice.Routes;

import org.restlet.Request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class ScriptXmlWriter {
	
	private final String baseUrl;
	XProcScript script = null;
	boolean details = false;
	private static Logger logger = LoggerFactory.getLogger(ScriptXmlWriter.class.getName());


	/**
	 * @param baseUrl Prefix to be included at the beginning of <code>href</code>
	 *                attributes (the resource paths). Set this to {@link Request#getRootRef()}
	 *                to get fully qualified URLs. Set this to {@link Routes#getPath()} to get
	 *                absolute paths relative to the domain name.
	 */
	public ScriptXmlWriter(XProcScript script, String baseUrl) {
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
	
	private Document scriptToXmlDocument(XProcScript script) {
		Document doc = XmlUtils.createDom("script");
		Element scriptElm = doc.getDocumentElement();
		addElementData(script, scriptElm);
		
		// for debugging only
		if (!XmlValidator.validate(doc, XmlValidator.SCRIPT_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}

		return doc;
	}

	// element is <script> but it's empty
	private void addElementData(XProcScript script, Element element) {
		Document doc = element.getOwnerDocument();
		String scriptHref = baseUrl + Routes.SCRIPT_ROUTE.replaceFirst("\\{id\\}", script.getDescriptor().getId());

		element.setAttribute("id", script.getDescriptor().getId());
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
			
			addInputPorts(script.getXProcPipelineInfo().getInputPorts(), element);
			addOptions(script.getXProcPipelineInfo().getOptions(), element);
			addOutputPorts(script.getXProcPipelineInfo().getOutputPorts(), element);
		}
	}
	
	private void addInputPorts(Iterable<XProcPortInfo> inputs, Element parent) {
		Document doc = parent.getOwnerDocument();
		for (XProcPortInfo input : script.getXProcPipelineInfo().getInputPorts()) {
			XProcPortMetadata meta = script.getPortMetadata(input.getName());

			Element inputElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "input");
			inputElm.setAttribute("name", input.getName());
	                inputElm.setAttribute("nicename", meta.getNiceName());
			inputElm.setAttribute("required", Boolean.toString(meta.isRequired()));
			inputElm.setAttribute("sequence", Boolean.toString(input.isSequence()));
			if (meta.getMediaType() != null && !meta.getMediaType().isEmpty()) {
				inputElm.setAttribute("mediaType", meta.getMediaType());
			}
			inputElm.setAttribute("desc", meta.getDescription());

			parent.appendChild(inputElm);
		}
	}

	private void addOptions(Iterable<XProcOptionInfo> options, Element parent) {
		Document doc = parent.getOwnerDocument();
		for (XProcOptionInfo option : options) {
			XProcOptionMetadata meta = script.getOptionMetadata(option.getName());
			
			Element optionElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "option");
			optionElm.setAttribute("name", option.getName().toString());
	                optionElm.setAttribute("nicename", meta.getNiceName());
			optionElm.setAttribute("required", Boolean.toString(option.isRequired()));
			
			optionElm.setAttribute("type", meta.getType());
			if (meta.getMediaType() != null && !meta.getMediaType().isEmpty()) {
				optionElm.setAttribute("mediaType", meta.getMediaType());
			}
			optionElm.setAttribute("desc", meta.getDescription());
			optionElm.setAttribute("ordered", Boolean.toString(meta.isOrdered()));
			optionElm.setAttribute("sequence", Boolean.toString(meta.isSequence()));
                        setDefault(option,optionElm);
			
			if (meta.getOutput() != Output.NA) {
				optionElm.setAttribute("outputType", meta.getOutput().toString().toLowerCase());
			}
			parent.appendChild(optionElm);
		}
	}

        private void setDefault(XProcOptionInfo info,Element element ){
                
                String select=info.getSelect();
                if (Strings.isNullOrEmpty(select)){
                        return;
                }
                char quote=select.charAt(0);
                //check that those are quotes
                if (quote!='"' && quote!='\''){
                        return;//no quote
                }
                //starts and ends by quote
                if (select.charAt(select.length()-1)!=quote){
                        return;
                }

                String def=select.substring(1,select.length()-1);
                //set whatever is in the select
                element.setAttribute("default",def);
        }
	
	private void addOutputPorts(Iterable<XProcPortInfo> outputs, Element parent) {
		logger.debug("Adding output ports");
		Document doc = parent.getOwnerDocument();
		for (XProcPortInfo output : outputs) {
			logger.debug("Adding output port"+output.getName());
			XProcPortMetadata meta = script.getPortMetadata(output.getName());
			Element outputElm = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "output");
			outputElm.setAttribute("name", output.getName());
	                outputElm.setAttribute("nicename", meta.getNiceName());
			outputElm.setAttribute("sequence", Boolean.toString(output.isSequence()));
			if (meta.getMediaType() != null && !meta.getMediaType().isEmpty()) {
				outputElm.setAttribute("mediaType", meta.getMediaType());
			}
			outputElm.setAttribute("desc", meta.getDescription());
			
			parent.appendChild(outputElm);
		}
	}
}
