package org.daisy.pipeline.client.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "/admin/sizes" response from the Pipeline 2 Web Service.
 * 
 * Example:
 * {@code
 * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * <jobSizes href="http://localhost:8181/ws/admin/sizes" total="10057458" xmlns="http://www.daisy.org/ns/pipeline/data">
 *     <jobSize context="0" id="0eb84973-4e5e-43c8-837c-7ea773317050" log="53404" output="178154"/>
 *     <jobSize context="0" id="9d463718-a1e6-4bcf-94b8-9a41a5bdbe2f" log="38850" output="178154"/>
 *     <jobSize context="0" id="280b1060-c028-4187-b767-c88eda4d6f8a" log="115902" output="654189"/>
 *     <jobSize context="0" id="9b3b882b-12f0-408a-8471-410f5e915572" log="230744" output="1735172"/>
 *     <jobSize context="0" id="eb919247-3cf9-4bca-856c-526f2526b20e" log="268776" output="1914980"/>
 *     <jobSize context="0" id="02c48af5-b72b-4944-8c7f-02c0e72458b4" log="7694" output="0"/>
 *     <jobSize context="0" id="09a41422-727e-44e2-8b08-ff66614cc016" log="6260" output="0"/>
 *     <jobSize context="0" id="6113de44-c0fd-48b3-9934-09f1b0d983da" log="6262" output="0"/>
 *     <jobSize context="0" id="673eb63e-0a03-4fab-9d72-9f125e96d166" log="41627" output="177289"/>
 *     <jobSize context="0" id="2e3f63d2-bdfb-4adc-9324-bea8f19b418f" log="31507" output="177289"/>
 *     <jobSize context="0" id="d0f3693c-8ce8-47cf-b5b4-d391accf342c" log="41624" output="177289"/>
 *     <jobSize context="0" id="85bdd727-f453-4775-94fd-b62dbbaa75ec" log="35198" output="177289"/>
 *     <jobSize context="0" id="de4ae40e-231c-41a6-aacb-e56652cedec5" log="41640" output="177289"/>
 *     <jobSize context="0" id="d05f1d64-020c-48e2-a465-dc88226b0c73" log="63992" output="177289"/>
 *     <jobSize context="0" id="b94e9977-65e4-4719-afb7-9af301d5024e" log="64562" output="177289"/>
 *     <jobSize context="0" id="f4735643-c570-494c-a8ac-6d33fbb9e31d" log="64563" output="177289"/>
 *     <jobSize context="0" id="d775b9db-6c97-4e81-9612-b68bb1742069" log="8968" output="0"/>
 *     <jobSize context="0" id="27b7a369-930f-4eba-877c-a1359acbcbf2" log="55743" output="1108947"/>
 *     <jobSize context="0" id="51487a3f-76a8-400c-b5ea-35d348694a16" log="143129" output="703443"/>
 *     <jobSize context="0" id="0655e182-5094-4be3-8ac3-28843b729062" log="142219" output="703443"/>
 * </jobSizes>
 * }
 */
public class JobSizes {
	
	public class JobSize {
		public String id;
		public Long context;
		public Long log;
		public Long output;
	}
    
	public String href;
	public Long total;
	public Map<String, JobSize> jobSizes;
	
	public JobSizes(Node jobSizesNode) {
		jobSizes = new HashMap<String, JobSize>();
		
		try {
			// select root element if the node is a document node
			if (jobSizesNode instanceof Document)
				jobSizesNode = XPath.selectNode("/d:jobs", jobSizesNode, XPath.dp2ns);
			
			this.href = XPath.selectText("@href", jobSizesNode, XPath.dp2ns);
			this.total = Long.valueOf(XPath.selectText("@total", jobSizesNode, XPath.dp2ns));

			List<Node> jobSizeNodes = XPath.selectNodes("d:job", jobSizesNode, XPath.dp2ns);
			for (Node jobSizeNode : jobSizeNodes) {
				JobSize jobSize = new JobSize();
				jobSize.id = XPath.selectText("@id", jobSizeNode, XPath.dp2ns);
				jobSize.context = Long.valueOf(XPath.selectText("@context", jobSizeNode, XPath.dp2ns));
				jobSize.log = Long.valueOf(XPath.selectText("@log", jobSizeNode, XPath.dp2ns));
				jobSize.output = Long.valueOf(XPath.selectText("@output", jobSizeNode, XPath.dp2ns));
				jobSizes.put(jobSize.id, jobSize);
			}
			
		} catch (Exception e) {
			Pipeline2Logger.logger().error("Failed to parse the job sizes XML", e);
		}
	}
	
}
