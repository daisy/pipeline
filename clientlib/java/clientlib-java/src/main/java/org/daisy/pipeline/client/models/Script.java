package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XML;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * A representation of a Pipeline 2 script.
 */
public class Script implements Comparable<Script> {
	
	private String id;
	private String href; // xs:anyURI
	private List<String> inputFilesets = new ArrayList<String>();
	private List<String> outputFilesets = new ArrayList<String>();
	private String nicename;
	private String description;
	private String version;
	private String homepage; // xs:anyURI
	private List<Argument> inputs = new ArrayList<Argument>();
	private List<Argument> outputs = new ArrayList<Argument>();
	
	// lazy load document; don't parse it until necessary
	private Node scriptNode = null;
	private boolean lazyLoaded = false;
	
	/**
	 * Parse the script described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/script.xml
	 * 
	 * @param scriptXml The XML
	 * @throws Pipeline2Exception thrown when an error occurs
	 */
	public Script(Node scriptXml) throws Pipeline2Exception {
		scriptNode = scriptXml;
	}
	
	private void lazyLoad() {
		if (lazyLoaded || scriptNode == null) {
			return;
		}
		
		try {
			// select root element if the node is a document node
			if (scriptNode instanceof Document)
				scriptNode = XPath.selectNode("/d:script", scriptNode, XPath.dp2ns);
			
			this.id = XPath.selectText("@id", scriptNode, XPath.dp2ns);
			this.href = XPath.selectText("@href", scriptNode, XPath.dp2ns);
			
			String inputFilesetsString = XPath.selectText("@input-filesets", scriptNode, XPath.dp2ns);
			String outputFilesetsString = XPath.selectText("@output-filesets", scriptNode, XPath.dp2ns);
			
			if (inputFilesetsString != null && !"".equals(inputFilesetsString)) {
				for (String fileset : inputFilesetsString.split("\\s+")) {
					if (!"".equals(fileset)) {
						inputFilesets.add(fileset);
					}
				}
			}

			if (outputFilesetsString != null && !"".equals(outputFilesetsString)) {
				for (String fileset : outputFilesetsString.split("\\s+")) {
					if (!"".equals(fileset)) {
						outputFilesets.add(fileset);
					}
				}
			}
			
			if ((inputFilesetsString == null || "".equals(inputFilesetsString)) && (outputFilesetsString == null || "".equals(outputFilesetsString))) {
				// try to infer input/output filesets from script id
				if (this.id.matches("^[a-zA-Z0-9]+-to-[a-zA-Z0-9]+$")) {
					String[] split = this.id.split("-");
					inputFilesets.add(split[0]);
					outputFilesets.add(split[2]);
				}
				else if (this.id.matches("^[a-zA-Z0-9]+-validator$")) {
					String[] split = this.id.split("-");
					inputFilesets.add(split[0]);
				}
			}
			
			this.nicename = XPath.selectText("d:nicename", scriptNode, XPath.dp2ns);
			this.description = XPath.selectText("d:description", scriptNode, XPath.dp2ns);
			this.version = XPath.selectText("d:version", scriptNode, XPath.dp2ns);
			this.homepage = XPath.selectText("d:homepage", scriptNode, XPath.dp2ns);
			
			List<Node> inputNodes = XPath.selectNodes("d:input | d:option", scriptNode, XPath.dp2ns);
			List<Node> outputNodes = XPath.selectNodes("d:output", scriptNode, XPath.dp2ns);
			
			for (Node inputNode : inputNodes) {
				Argument argument = new Argument(inputNode);
				this.inputs.add(argument);
			}
	
			for (Node outputNode : outputNodes) {
				Argument argument = new Argument(outputNode);
				this.outputs.add(argument);
			}
			
		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse script XML", e);
		}
		
		lazyLoaded = true;
	}
	
//	/**
//	 * Populate the script arguments with the values contained in the given jobRequest document.
//	 * 
//	 * @param jobRequest The job request
//	 * @throws Pipeline2WSException thrown when an error occurs 
//	 */
//	public void parseFromJobRequest(Node jobRequest) throws Pipeline2WSException {
//		if (jobRequest instanceof Document)
//			jobRequest = ((Document)jobRequest).getDocumentElement();
//		
//		for (Argument arg : inputs) {
//			arg.parseFromJobRequest(jobRequest);
//		}
//	}
	
	/**
	 * Get an argument (inputs, options or outputs) by its name.
	 * 
	 * @param name The name of the argument
	 * @return The argument
	 */
	public Argument getArgument(String name) {
		lazyLoad();
		for (Argument arg : inputs) {
			if (arg.getName().equals(name)) {
				return arg;
			}
		}
		for (Argument arg : outputs) {
			if (arg.getName().equals(name)) {
				return arg;
			}
		}
		return null;
	}
	
	/**
	 * Parse the list of scripts described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/scripts.xml
	 * 
	 * @param scriptsXml The XML
	 * @return A list of the scripts
	 * @throws Pipeline2Exception thrown when an error occurs
	 */
	public static List<Script> parseScriptsXml(Node scriptsXml) throws Pipeline2Exception {
		
		List<Script> scripts = new ArrayList<Script>();
		
		// select root element if the node is a document node
		if (scriptsXml instanceof Document)
			scriptsXml = XPath.selectNode("/d:scripts", scriptsXml, XPath.dp2ns);
		
		List<Node> scriptNodes = XPath.selectNodes("d:script", scriptsXml, XPath.dp2ns);
		for (Node scriptNode : scriptNodes) {
			scripts.add(new Script(scriptNode));
		}
		
		Collections.sort(scripts);
		
		return scripts;
	}

	public int compareTo(Script other) {
		if (getId() == null) return 1;
		if (other.getId() == null) return -1;
		return getId().compareTo(other.getId());
	}
	
	// getters and setters (to ensure lazy loading)
    public String getId() { lazyLoad(); return id; }
    public String getHref() { lazyLoad(); return href; }
    public List<String> getInputFilesets() { lazyLoad(); return inputFilesets; }
    public List<String> getOutputFilesets() { lazyLoad(); return outputFilesets; }
    public String getNicename() { lazyLoad(); return nicename; }
    public String getDescription() { lazyLoad(); return description; }
    public String getVersion() { lazyLoad(); return version; }
    public String getHomepage() { lazyLoad(); return homepage; }
    public List<Argument> getInputs() { lazyLoad(); return inputs; }
    public List<Argument> getOutputs() { lazyLoad(); return outputs; }
    public void setId(String id) { lazyLoad(); this.id = id; }
    public void setHref(String href) { lazyLoad(); this.href = href; }
    public void setInputFilesets(List<String> inputFilesets) { lazyLoad(); this.inputFilesets = inputFilesets; }
    public void setOutputFilesets(List<String> outputFilesets) { lazyLoad(); this.outputFilesets = outputFilesets; }
    public void setNicename(String nicename) { lazyLoad(); this.nicename = nicename; }
    public void setDescription(String description) { lazyLoad(); this.description = description; }
    public void setVersion(String version) { lazyLoad(); this.version = version; }
    public void setHomepage(String homepage) { lazyLoad(); this.homepage = homepage; }
    public void setInputs(List<Argument> inputs) { lazyLoad(); this.inputs = inputs; }
    public void setOutputs(List<Argument> outputs) { lazyLoad(); this.outputs = outputs; }

	public Document toXml() {
		lazyLoad();
		
		Document scriptDocument = XML.getXml("<script xmlns=\"http://www.daisy.org/ns/pipeline/data\"/>");
		Element scriptElement = scriptDocument.getDocumentElement();

		if (id != null) {
		    scriptElement.setAttribute("id", id);
		}
		if (href != null) {
		    scriptElement.setAttribute("href", href);
		}
		if (inputFilesets != null) {
		    String inputFilesetsJoined = "";
		    for (int i = 0; i < inputFilesets.size(); i++) {
		        if (i > 0) {
		            inputFilesetsJoined += " ";
		        }
		        inputFilesetsJoined += inputFilesets.get(i);
		    }
		    scriptElement.setAttribute("input-filesets", inputFilesetsJoined);
		}
		if (outputFilesets != null) {
		    String outputFilesetsJoined = "";
		    for (int i = 0; i < outputFilesets.size(); i++) {
		        if (i > 0) {
		            outputFilesetsJoined += " ";
		        }
		        outputFilesetsJoined += outputFilesets.get(i);
		    }
		    scriptElement.setAttribute("output-filesets", outputFilesetsJoined);
		}
		if (nicename != null) {
		    Element e = scriptDocument.createElementNS(XPath.dp2ns.get("d"), "nicename");
		    e.setTextContent(nicename);
		    scriptElement.appendChild(e);
		}
		if (description != null) {
		    Element e = scriptDocument.createElementNS(XPath.dp2ns.get("d"), "description");
		    e.setTextContent(description);
		    scriptElement.appendChild(e);
		}
		if (version != null) {
		    Element e = scriptDocument.createElementNS(XPath.dp2ns.get("d"), "version");
		    e.setTextContent(version);
		    scriptElement.appendChild(e);
		}
		if (homepage != null) {
		    Element e = scriptDocument.createElementNS(XPath.dp2ns.get("d"), "homepage");
		    e.setTextContent(homepage);
		    scriptElement.appendChild(e);
		}
		if (inputs != null) {
		    for (Argument arg : inputs) {
		        XML.appendChildAcrossDocuments(scriptElement, arg.toXml().getDocumentElement());
		    }
		}
		if (outputs != null) {
		    for (Argument arg : outputs) {
		    	XML.appendChildAcrossDocuments(scriptElement, arg.toXml().getDocumentElement());
		    }
		}
		
		return scriptDocument;
	}
	
	/**
	 * Get all arguments, both inputs and outputs.
	 * 
	 * See also getInputs and getOutputs.
	 * 
	 * @return a list of all arguments.
	 */
	public List<Argument> getArguments() {
		List<Argument> arguments = new ArrayList<Argument>();
		arguments.addAll(getInputs());
		arguments.addAll(getOutputs());
		return arguments;
	}
	
}
