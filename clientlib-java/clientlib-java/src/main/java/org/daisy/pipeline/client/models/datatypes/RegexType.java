package org.daisy.pipeline.client.models.datatypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.models.DataType;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class RegexType extends DataType {

	public String regex;
	public Pattern pattern;
	public Map<String,String> descriptions = new HashMap<String,String>();
	
	public RegexType(Node dataTypeXml) {
		super(dataTypeXml);
		
		try {
			// select root element if the node is a document node
			if (dataTypeXml instanceof Document)
				dataTypeXml = XPath.selectNode("/*", dataTypeXml, XPath.dp2ns);
			
			this.regex = XPath.selectText("/*/*[local-name()='param' and @name='pattern']/text()", dataTypeXml, XPath.dp2ns);
			this.pattern = Pattern.compile(regex);
			
			List<Node> docNodes = XPath.selectNodes("/*/*[local-name()='documentation']", dataTypeXml, XPath.dp2ns);
			for (Node documentation : docNodes) {
				Node languageNode = XPath.selectNode("ancestor-or-self::*[@xml:lang | @lang | @*[local-name()='lang']]", documentation, XPath.dp2ns);
				String language = XPath.selectText("(@xml:lang | @lang | @*[local-name()='lang'])[1]", languageNode, XPath.dp2ns);
				String description = XPath.selectText("text()", documentation, XPath.dp2ns);
				this.descriptions.put(language, description);
			}
			
		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse datatype (regex) XML", e);
		}
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
