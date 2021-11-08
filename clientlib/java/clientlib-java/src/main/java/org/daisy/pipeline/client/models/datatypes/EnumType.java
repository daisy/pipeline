package org.daisy.pipeline.client.models.datatypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.models.DataType;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EnumType extends DataType {
	
	public List<Value> values = new ArrayList<Value>();
	
	public EnumType(Node dataTypeXml) {
		super(dataTypeXml);
		
		try {
			// select root element if the node is a document node
			if (dataTypeXml instanceof Document)
				dataTypeXml = XPath.selectNode("/*", dataTypeXml, XPath.dp2ns);
			
			List<Node> valueNodes = XPath.selectNodes("*[local-name()='value']", dataTypeXml, XPath.dp2ns);
			for (int i = 0; i < valueNodes.size(); i++) {
				Value value = new Value();
				
				Node valueNode = valueNodes.get(i);
				value.name = XPath.selectText("text()", valueNode, XPath.dp2ns);
				if (value.name == null) {
					value.name = "";
				}
				
				List<Node> precedingDocNodes = XPath.selectNodes("/*/*[local-name()='documentation' and not(preceding-sibling::*[local-name()='value'])]/*[local-name()='value' and count(preceding-sibling::*[local-name()='value'])="+i+"]", dataTypeXml, XPath.dp2ns);
				List<Node> followingDocNodes = XPath.selectNodes("/*/*[local-name()='documentation' and count(preceding-sibling::*[local-name()='value'])="+(i+1)+"]", dataTypeXml, XPath.dp2ns);
				List<Node> docNodes = new ArrayList<Node>();
				docNodes.addAll(precedingDocNodes);
				docNodes.addAll(followingDocNodes);
				for (Node documentation : docNodes) {
					Node languageNode = XPath.selectNode("ancestor-or-self::*[@xml:lang | @lang | @*[local-name()='lang']]", documentation, XPath.dp2ns);
					String language = XPath.selectText("(@xml:lang | @lang | @*[local-name()='lang'])[1]", languageNode, XPath.dp2ns);
					String text = XPath.selectText("text()", documentation, XPath.dp2ns);
					String[] textSplit = text.split("\n+", 2);
					String nicename = textSplit[0];
					String description = textSplit.length == 2 ? textSplit[1] : "";
					value.nicenames.put(language, nicename);
					value.descriptions.put(language, description);
				}
				
				values.add(value);
			}
			
		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse datatype (enum) XML", e);
		}
	}
	
	public class Value {
		public String name;
		public Map<String,String> nicenames = new HashMap<String,String>();
		public Map<String,String> descriptions = new HashMap<String,String>();
		
		/** Use to get the default nicename (if multiple are available)
		 *  
		 *  @return The nicename
		 */
		public String getNicename() {
			if (nicenames.containsKey("")) {
				return nicenames.get("");
				
			} else if (nicenames.containsKey("en")) {
				return nicenames.get("en");
				
			} else if (!nicenames.isEmpty()) {
				for (String lang : nicenames.keySet()) {
					return nicenames.get(lang);
				}
			}
			
			return name;
		}

		public String getDescription() {
			if (descriptions.containsKey("")) {
				return descriptions.get("");
				
			} else if (descriptions.containsKey("en")) {
				return descriptions.get("en");
				
			} else if (!descriptions.isEmpty()) {
				for (String lang : descriptions.keySet()) {
					return descriptions.get(lang);
				}
			}
			
			return "";
		}
	}
	
}
