package org.daisy.pipeline.client.models.datatypes;

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
	
	public RegexType(Node dataTypeXml) {
		super(dataTypeXml);
		
		try {
			// select root element if the node is a document node
			if (dataTypeXml instanceof Document)
				dataTypeXml = XPath.selectNode("/*", dataTypeXml, XPath.dp2ns);
			
			this.regex = XPath.selectText("/*/*[local-name()='param' and @name='pattern']/text()", dataTypeXml, XPath.dp2ns);
			this.pattern = Pattern.compile(regex);
			
		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse datatype (regex) XML", e);
		}
	}

}
