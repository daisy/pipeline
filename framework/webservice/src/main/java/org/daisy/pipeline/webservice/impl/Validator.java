package org.daisy.pipeline.webservice.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.webservice.xml.XmlUtils;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

// TODO: Auto-generated Javadoc
/**
 * The Class Validator.
 */
public class Validator {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(Validator.class.getName());

	public static boolean validateXmlAgainstSchema(Document document, URL schema) {
		return XmlValidator.validate(document, schema);
	}
        public static String NS_DAISY = "http://www.daisy.org/ns/pipeline/data";

	/**
	 * Validate job request.
	 *
	 * @param doc the doc
	 * @param application the application
	 * @return true, if successful
	 */
	public static ValidationStatus validateJobRequest(Document doc, PipelineWebService application) {

		// validate against the schema
		boolean xmlValid = validateXmlAgainstSchema(doc, XmlValidator.JOB_REQUEST_SCHEMA_URL);

		if (xmlValid == false) {
			return new ValidationStatus(false,"The request is not valid");
		}

		return validateJobRequestArguments(doc, application);
	}

	// check that there is a value for each required argument
	// check data of the argument value to the fullest extent possible
	/**
	 * Validate arguments.
	 *
	 * @param doc the doc
	 * @param application the application
	 * @return true, if successful
	 */
	private static ValidationStatus validateJobRequestArguments(Document doc, PipelineWebService application) {
		
		Element scriptElm = (Element)doc.getElementsByTagNameNS(NS_DAISY,"script").item(0);
		// get the ID from the href attr value
				String scriptId = scriptElm.getAttribute("href");
				if (scriptId.endsWith("/")) {
				    scriptId = scriptId.substring(0, scriptId.length() - 1);
				}
				int idx = scriptId.lastIndexOf('/');
				scriptId = scriptId.substring(idx+1);

		ScriptRegistry scriptRegistry = application.getScriptRegistry();
		ScriptService<?> scriptService = scriptRegistry.getScript(scriptId);

		if (scriptService == null) {
                        String message="Script not found";
		        logger.error(message);
			return new ValidationStatus(false,message);
		}

		Script script = scriptService.load();

		// inputs
		ValidationStatus hasAllRequiredInputs = validateJobRequestInputPortData(
			doc.getElementsByTagNameNS(NS_DAISY, "input"), script);
                if (!hasAllRequiredInputs.isValid()){
                        return hasAllRequiredInputs;
                }
		// options
		ValidationStatus hasAllRequiredOptions = validateJobRequestOptionData(
			doc.getElementsByTagNameNS(NS_DAISY,"option"), script);
		
		return hasAllRequiredOptions;
	}

	/**
	 * Validate option data.
	 *
	 * @param options the options
	 * @param nodes the nodes
	 * @param script the script
	 * @return true, if successful
	 */
	private static ValidationStatus validateJobRequestOptionData(NodeList nodes, Script script) {
		boolean hasAllRequiredArgs = true;
		List<String> missingArgs = new ArrayList<String>();
		
		for (ScriptOption option : script.getOptions()) {
			// skip optional arguments
			if (option.isRequired() == false) {
				continue;
			}

			boolean validArg = false;
			for (int i=0; i<nodes.getLength(); i++) {
				Element elm = (Element)nodes.item(i);
				if (elm.getAttribute("name").equals(option.getName().toString())) {
					if(!option.isSequence()){
						validArg = validateOptionType(elm.getTextContent(), option.getMediaType());
					}else{//otherwise is been validated by the schema
						validArg=true;
					}
					break;
				}
			}
			hasAllRequiredArgs &= validArg;
			
			if (!validArg) {
				missingArgs.add(option.getName().toString());
			}
		}

		if (hasAllRequiredArgs == false) {
			// TODO: be more specific
			String missingArgsStr = "";
			for (String s : missingArgs){
			    missingArgsStr += s + ",";
			}

                        String message="Required JobRequest option(s) missing: " + missingArgsStr;
		        logger.error(message);
			return new ValidationStatus(false,message);
		}
		return new ValidationStatus(true);
	}

	/**
	 * Validate input port data.
	 *
	 * @param ports the ports
	 * @param nodes the nodes
	 * @param script the script
	 * @return true, if successful
	 */
	private static ValidationStatus validateJobRequestInputPortData(NodeList nodes, Script script) {

		boolean hasAllRequiredArgs = true;
		List<String> missingArgs = new ArrayList<String>();
		
		for (ScriptPort port : script.getInputPorts()) {
                        //if it's not required is valid unless otherwise proven (i.e. empty item)
                        //if it is required is not valid unless proven (it exists and the values/items are well formed
			boolean validArg = !port.isRequired();
			// input elements should be of one of two forms:
			// <input name="in1">
			//   <item value="./path/to/file/book.xml/>
			//   ...
			// </input>
			//
			// OR
			//
			// <input name="in">
			//   <docwrapper>
			//     <xml data../>
			//  </docwrapper>
			// </input>
			//
			//
			for (int i=0; i<nodes.getLength(); i++) {
				Element elm = (Element)nodes.item(i);

				// find the <input> XML element that matches this input arg name
				if (elm.getAttribute("name").equals(port.getName())) {
					// <input> elements will have either <item> element children or <docwrapper> element children
					NodeList itemNodes = elm.getElementsByTagNameNS(NS_DAISY,"item");
					NodeList docwrapperNodes = elm.getElementsByTagNameNS(NS_DAISY,"docwrapper");

					if (itemNodes.getLength() == 0 && docwrapperNodes.getLength() == 0) {
						validArg = false;
					}
					else {
						if (itemNodes.getLength() > 0) {
							validArg = validateItemElements(itemNodes);
						}
						else {
							validArg = validateDocwrapperElements(docwrapperNodes, port.getMediaType());
						}
					}
					break;
				}
			}
			hasAllRequiredArgs &= validArg;
			
			if (!validArg) {
				missingArgs.add(port.getName());
			}
		}

		if (hasAllRequiredArgs == false) {
			String missingArgsStr = "";
			for (String s : missingArgs){
			    missingArgsStr += s + ",";
			}
                        String message="Required jobRequest input port arg(s) missing: " + missingArgsStr;
		        logger.error(message);
			return new ValidationStatus(false,message);
		}
		return new ValidationStatus(true);
	}

	// make sure these nodes contain well-formed XML
	// nodes must contain at least one item
	// nodes must be <docwrapper> elements
	// TODO incorporate media type
	/**
	 * Validate docwrapper elements.
	 *
	 * @param nodes the nodes
	 * @param mediaType the media type
	 * @return true, if successful
	 */
	private static boolean validateDocwrapperElements(NodeList nodes, String mediaType) {
		boolean isValid = true;

		for (int i = 0; i<nodes.getLength(); i++) {
			Node docwrapper = nodes.item(i);
			Node content = null;
			// find the first element child of docwrapper
			for (int q = 0; q < docwrapper.getChildNodes().getLength(); q++) {
				if (docwrapper.getChildNodes().item(q).getNodeType() == Node.ELEMENT_NODE) {
					content = docwrapper.getChildNodes().item(q);
					break;
				}
			}
			String xml = XmlUtils.nodeToString(content);
			isValid &= validateWellFormedXml(xml);
		}

		return isValid;
	}

	// make sure these @value attributes are non-empty
	// nodes must contain at least one item
	// all nodes must be <item> elements
	/**
	 * Validate item elements.
	 *
	 * @param nodes the nodes
	 * @return true, if successful
	 */
	private static boolean validateItemElements(NodeList nodes) {
		boolean isValid = true;

		for (int i = 0; i<nodes.getLength(); i++) {
			Element elm = (Element)nodes.item(i);
			isValid &= elm.getAttribute("value").trim().length() > 0;
		}
		return isValid;
	}

	/**
	 * Validate option type.
	 *
	 * @param value the value
	 * @param mediaType the media type
	 * @return true, if successful
	 */
	private static boolean validateOptionType(String value, String mediaType) {
		// TODO validate XSD types
		// for now, just check that the string is non-empty
		return value.trim().length() > 0;
	}

	// just validate whether the xml is well-formed or not.
	// we don't verify flavor of xml is expected;
	// that's expected to be handled by the xproc script itself
	/**
	 * Validate well formed xml.
	 *
	 * @param xml the xml
	 * @return true, if successful
	 */
	private static boolean validateWellFormedXml(String xml){

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setValidating(false);
	    DocumentBuilder db;
		try {
			db = factory.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(xml.getBytes());
		    db.parse(is);
		    is.close();
		} catch (ParserConfigurationException e) {
			logger.error(e.getMessage());
			return false;
		} catch (SAXException e) {
			logger.error(e.getMessage());
			return false;
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}

}
