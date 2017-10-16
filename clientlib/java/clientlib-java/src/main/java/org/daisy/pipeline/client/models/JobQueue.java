package org.daisy.pipeline.client.models;

import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * A representation of the "/queue" response from the Pipeline 2 Web Service.
 * 
 * Example XML:
 * {@code
 * <?xml version="1.0" encoding="UTF-8" standalone="no"?>
 * <queue xmlns="http://www.daisy.org/ns/pipeline/data" href="http://localhost:8181/ws/queue">
 *     <job clientPriority="medium" computedPriority="-43.333333333333336" href="http://localhost:8181/ws/jobs/464c72f9-ed59-44ff-8b7b-207abb4add7f" id="464c72f9-ed59-44ff-8b7b-207abb4add7f" jobPriority="medium" moveDown="http://localhost:8181/ws/queue/down/464c72f9-ed59-44ff-8b7b-207abb4add7f" moveUp="http://localhost:8181/ws/queue/up/464c72f9-ed59-44ff-8b7b-207abb4add7f" relativeTime="1.0" timestamp="1398930862759"/>
 *     <job clientPriority="medium" computedPriority="-40.452655526790615" href="http://localhost:8181/ws/jobs/29f567b4-c3da-492b-8471-8f95c82dfeac" id="29f567b4-c3da-492b-8471-8f95c82dfeac" jobPriority="medium" moveDown="http://localhost:8181/ws/queue/down/29f567b4-c3da-492b-8471-8f95c82dfeac" moveUp="http://localhost:8181/ws/queue/up/29f567b4-c3da-492b-8471-8f95c82dfeac" relativeTime="0.8559661096728642" timestamp="1398930863371"/>
 *     <job clientPriority="medium" computedPriority="-37.36957715540911" href="http://localhost:8181/ws/jobs/fdf37ff6-5532-4bb3-96f5-df5347d680fa" id="fdf37ff6-5532-4bb3-96f5-df5347d680fa" jobPriority="medium" moveDown="http://localhost:8181/ws/queue/down/fdf37ff6-5532-4bb3-96f5-df5347d680fa" moveUp="http://localhost:8181/ws/queue/up/fdf37ff6-5532-4bb3-96f5-df5347d680fa" relativeTime="0.7018121911037891" timestamp="1398930864026"/>
 *     <job clientPriority="medium" computedPriority="-30.106691770612695" href="http://localhost:8181/ws/jobs/28556164-af41-4a0b-aa3e-8718e97a209c" id="28556164-af41-4a0b-aa3e-8718e97a209c" jobPriority="medium" moveDown="http://localhost:8181/ws/queue/down/28556164-af41-4a0b-aa3e-8718e97a209c" moveUp="http://localhost:8181/ws/queue/up/28556164-af41-4a0b-aa3e-8718e97a209c" relativeTime="0.338667921863968" timestamp="1398930865569"/>
 *     <job clientPriority="medium" computedPriority="-25.983368635757433" href="http://localhost:8181/ws/jobs/b3a1f963-a29d-40cf-98c2-e523b859f926" id="b3a1f963-a29d-40cf-98c2-e523b859f926" jobPriority="medium" moveDown="http://localhost:8181/ws/queue/down/b3a1f963-a29d-40cf-98c2-e523b859f926" moveUp="http://localhost:8181/ws/queue/up/b3a1f963-a29d-40cf-98c2-e523b859f926" relativeTime="0.13250176512120498" timestamp="1398930866445"/>
 *     <job clientPriority="medium" computedPriority="-23.333333333333332" href="http://localhost:8181/ws/jobs/0bcb5bf1-e039-43f9-8218-b08c877f9204" id="0bcb5bf1-e039-43f9-8218-b08c877f9204" jobPriority="medium" moveDown="http://localhost:8181/ws/queue/down/0bcb5bf1-e039-43f9-8218-b08c877f9204" moveUp="http://localhost:8181/ws/queue/up/0bcb5bf1-e039-43f9-8218-b08c877f9204" relativeTime="0.0" timestamp="1398930867008"/>
 * </queue>
 * }
 */
public class JobQueue {
	
	public enum Priority { LOW, MEDIUM, HIGH };
	
	public class QueuedJob {
		String id; // "464c72f9-ed59-44ff-8b7b-207abb4add7f"
		String href; // "http://localhost:8181/ws/jobs/464c72f9-ed59-44ff-8b7b-207abb4add7f"
		Double computedPriority; // "-43.333333333333336"
		Priority jobPriority; // "medium"
		Priority clientPriority; // "medium"
		Double relativeTime; // "1.0"
		Long timestamp; // "1398930862759"
		String moveUp; // "http://localhost:8181/ws/queue/up/464c72f9-ed59-44ff-8b7b-207abb4add7f"
		String moveDown; // "http://localhost:8181/ws/queue/down/464c72f9-ed59-44ff-8b7b-207abb4add7f"
	}
	
	public String href;
	public List<QueuedJob> queue;
    
	public JobQueue(Node queueNode) {
		queue = new ArrayList<QueuedJob>();
		
		try {
			// select root element if the node is a document node
			if (queueNode instanceof Document)
				queueNode = XPath.selectNode("/d:jobs", queueNode, XPath.dp2ns);

			List<Node> queuedJobNodes = XPath.selectNodes("d:job", queueNode, XPath.dp2ns);
			for (Node queuedJobNode : queuedJobNodes) {
				QueuedJob queuedJob = new QueuedJob();
				queuedJob.id = XPath.selectText("@id", queuedJobNode, XPath.dp2ns);
				queuedJob.href = XPath.selectText("@href", queuedJobNode, XPath.dp2ns);
				queuedJob.computedPriority = Double.valueOf(XPath.selectText("@computedPriority", queuedJobNode, XPath.dp2ns));
				queuedJob.jobPriority = Priority.valueOf(XPath.selectText("@jobPriority", queuedJobNode, XPath.dp2ns));
				queuedJob.clientPriority = Priority.valueOf(XPath.selectText("@clientPriority", queuedJobNode, XPath.dp2ns));
				queuedJob.relativeTime = Double.valueOf(XPath.selectText("@relativeTime", queuedJobNode, XPath.dp2ns));
				queuedJob.timestamp = Long.valueOf(XPath.selectText("@timestamp", queuedJobNode, XPath.dp2ns));
				queuedJob.moveUp = XPath.selectText("@moveUp", queuedJobNode, XPath.dp2ns);
				queuedJob.moveDown = XPath.selectText("@moveDown", queuedJobNode, XPath.dp2ns);
				queue.add(queuedJob);
			}
			
		} catch (Exception e) {
			Pipeline2Logger.logger().error("Failed to parse the job queue XML", e);
		}
	}
	
}
