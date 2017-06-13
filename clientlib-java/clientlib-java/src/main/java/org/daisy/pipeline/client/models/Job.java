package org.daisy.pipeline.client.models;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.filestorage.JobValidator;
import org.daisy.pipeline.client.utils.XML;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A representation of a Pipeline 2 job.
 * 
 * This represents a job both before (job request) and after (job response) it is submitted to the engine.
 */
public class Job implements Comparable<Job> {

	public enum Status { IDLE, RUNNING, DONE, ERROR, VALIDATION_FAIL };
	public enum Priority { high, medium, low };

	private String id;
	private String href; // xs:anyURI
	private Status status;
	private Priority priority;
	private String scriptHref; // for job request xml documents
	private Script script;
	private String nicename;
	private String description;
	private String batchId;
	private List<Argument> argumentInputs; // used when there's no script given
	private List<Argument> argumentOutputs; // used when there's no script given
	private List<Callback> callback;
	private JobMessages messages;
	private Node messagesNode;
	private String logHref;
	private Result result; // "all results"-zip
	private SortedMap<Result,List<Result>> results; // "individual results"-zips as keys, individual files as values
	private Node resultsNode;

	private boolean lazyLoaded = false;
	private Node jobNode = null;

	private JobStorage storage;

	private static final Map<String,String> ns;
	static {
		ns = new HashMap<String,String>();
		ns.put("d", "http://www.daisy.org/ns/pipeline/data");
	}

	/** Create an empty representation of a job. */
	public Job() {}

	/**
	 * Parse the job described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/job.xml
	 * 
	 * @param jobXml The XML
	 * @throws Pipeline2Exception thrown when an error occurs
	 */
	public Job(Node jobXml) throws Pipeline2Exception {
		this();
		setJobXml(jobXml);
	}

	/**
	 * Given the absolute href for the result, get the result without parsing the entire jobXml.
	 * 
	 * @param resultsXml The XML
	 * @param href The href
	 * @return The Result
	 */
	public static Result getResultFromHref(Node resultsXml, String href) {
		if (href == null) {
			href = "";
		}

		try {
			if (resultsXml instanceof Document) {
				resultsXml = XPath.selectNode("/*", resultsXml, XPath.dp2ns);
			}
			
			Node resultNode;
			if ("".equals(href)) {
				resultNode = XPath.selectNode("./descendant-or-self::d:results", resultsXml, XPath.dp2ns);
				
			} else {
				resultNode = XPath.selectNode("./descendant-or-self::*[@href='"+href.replaceAll("'", "''").replaceAll(" ", "%20")+"']", resultsXml, XPath.dp2ns);
			}
			
			if (resultNode != null) {
				return Result.parseResultXml(resultNode);
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Could not parse job XML for finding the job result '"+href.replaceAll("'", "''").replaceAll(" ", "%20")+"'", e);
		}
		Pipeline2Logger.logger().debug("No result was found for href="+href);
		return null;
	}

	private void lazyLoad() {
		if (storage != null) {
			storage.lazyLoad();
		}

		if (lazyLoaded || jobNode == null) {
			return;
		}

		try {
			// select root element if the node is a document node
			if (jobNode instanceof Document)
				jobNode = XPath.selectNode("/*", jobNode, XPath.dp2ns);

			String id = XPath.selectText("@id", jobNode, XPath.dp2ns);
			if (id != null) {
				this.id = id;
			}
			this.href = XPath.selectText("@href", jobNode, XPath.dp2ns);
			String status = XPath.selectText("@status", jobNode, XPath.dp2ns);
			for (Status s : Status.values()) {
				if (s.toString().equals(status)) {
					this.status = s;
					break;
				}
			}
			String priority = XPath.selectText("@priority", jobNode, XPath.dp2ns);
			for (Priority p : Priority.values()) {
				if (p.toString().equals(priority)) {
					this.priority = p;
					break;
				}
			}
			if (this.priority == null) {
				// in the job XML, the priority is a attribute, but in the job request xml, it is an element
				priority = XPath.selectText("priority", jobNode, XPath.dp2ns);
				for (Priority p : Priority.values()) {
					if (p.toString().equals(priority)) {
						this.priority = p;
						break;
					}
				}
			}

			if (XPath.selectNode("d:script/*", jobNode, XPath.dp2ns) == null) {
				scriptHref = XPath.selectText("d:script/@href", jobNode, XPath.dp2ns);

			} else {
				Node scriptNode = XPath.selectNode("d:script", jobNode, XPath.dp2ns);
				if (scriptNode != null) {
					this.script = new Script(scriptNode);
				}
			}
			this.nicename = XPath.selectText("d:nicename", jobNode, XPath.dp2ns);
			this.description = XPath.selectText("d:description", jobNode, XPath.dp2ns);
			this.batchId = XPath.selectText("d:batchId", jobNode, XPath.dp2ns);
			this.logHref = XPath.selectText("d:log/@href", jobNode, XPath.dp2ns);
			this.callback = new ArrayList<Callback>();
			for (Node callbackNode : XPath.selectNodes("d:callback", jobNode, XPath.dp2ns)) {
				this.callback.add(new Callback(callbackNode));
			}
			this.messagesNode = XPath.selectNode("d:messages", jobNode, XPath.dp2ns);
			this.resultsNode = XPath.selectNode("d:results", jobNode, XPath.dp2ns);

			// Arguments are both part of the script XML and the jobRequest XML.
			// We could keep a separate copy of the arguments here in the Job instance, but that seems a bit unneccessary.
			// We could keep the argument values here in the Job instance and the argument definitions in the Script instance,
			// but that will probably just complicate validation and separates things that are closely related.
			// Instead, we just say that everything is stored in the Script instance (if there is one).
			// However, if there is no script defined, we keep the arguments her in the job instance.
			if (this.script == null) {
				argumentInputs = new ArrayList<Argument>();
				argumentOutputs = new ArrayList<Argument>();
				for (Node node : XPath.selectNodes("d:input | d:option", jobNode, XPath.dp2ns)) {
					argumentInputs.add(new Argument(node));
				}
				for (Node node : XPath.selectNodes("d:output", jobNode, XPath.dp2ns)) {
					argumentOutputs.add(new Argument(node));
				}
			}


		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("Unable to parse job XML", e);
		}

		lazyLoaded = true;
	}

	/**
	 * Get a list of all messages for the job.
	 * 
	 * @return The list of messages
	 */
	public List<Message> getMessages() {
		return getMessages(-1);
	}
	
	/**
	 * Get a list of all messages for the job.
	 * 
	 * @param maxDepth Don't return messages deeper than this level
	 * @return The list of messages
	 */
	public List<Message> getMessages(int maxDepth) {
		lazyLoad();
		
		if (messages == null && messagesNode != null) {
			try {

				messages = new JobMessages();
				List<Node> messageNodes = XPath.selectNodes("d:message", this.messagesNode, XPath.dp2ns);

				for (Node messageNode : messageNodes) {
					Message m = new Message();
					m.text = XPath.selectText(".", messageNode, XPath.dp2ns);
					if (XPath.selectText("@level", messageNode, XPath.dp2ns) != null) {
						m.level = Message.Level.valueOf(XPath.selectText("@level", messageNode, XPath.dp2ns));
						m.inferredLevel = m.level;
					}
					if (XPath.selectText("@sequence", messageNode, XPath.dp2ns) != null) {
						m.sequence = Integer.valueOf(XPath.selectText("@sequence", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@line", messageNode, XPath.dp2ns) != null) {
						m.line = Integer.valueOf(XPath.selectText("@line", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@column", messageNode, XPath.dp2ns) != null) {
						m.column = Integer.valueOf(XPath.selectText("@column", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@timeStamp", messageNode, XPath.dp2ns) != null) {
						m.setTimeStamp(XPath.selectText("@timeStamp", messageNode, XPath.dp2ns));
					}
					if (XPath.selectText("@file", messageNode, XPath.dp2ns) != null) {
						m.file = XPath.selectText("@file", messageNode, XPath.dp2ns);
					}
					messages.add(m);
				}
				Collections.sort(messages);

			} catch (Exception e) {
				Pipeline2Logger.logger().error("Unable to parse messages XML", e);
			}
		}
		
		if (maxDepth >= 0) {
			ArrayList<Message> filteredMessages = new ArrayList<Message>();
			for (Message m : messages) {
				if (m.depth <= maxDepth) {
					filteredMessages.add(m);
				}
			}
			return filteredMessages;
			
		} else {
			return messages;
		}
	}

	private void lazyLoadResults() {
		lazyLoad();
		if (results == null && resultsNode != null) {
			try {
				result = Result.parseResultXml(this.resultsNode, href);
				results = new TreeMap<Result,List<Result>>();

				List<Node> resultNodes = XPath.selectNodes("d:result", this.resultsNode, XPath.dp2ns);
				for (Node resultPortOrOptionNode : resultNodes) {
					Result resultPortOrOption = Result.parseResultXml(resultPortOrOptionNode, result.href);
					List<Result> portOrOptionResults = new ArrayList<Result>();

					List<Node> fileNodes = XPath.selectNodes("d:result", resultPortOrOptionNode, XPath.dp2ns);
					for (Node fileNode : fileNodes) {
						Result file = Result.parseResultXml(fileNode, resultPortOrOption.href);
						portOrOptionResults.add(file);
					}
					Collections.sort(portOrOptionResults);

					results.put(resultPortOrOption, portOrOptionResults);
				}

			} catch (Pipeline2Exception e) {
				Pipeline2Logger.logger().error("Unable to parse results XML", e);
			}
		}
	}

	/**
	 * Get the main Result object.
	 * 
	 * @return The returned Result represents "all results".
	 */
	public Result getResult() {
		lazyLoadResults();
		return result;
	}

	/**
	 * Get the Result representing the argument with the given name.
	 * 
	 * @param argumentName The name of the argument
	 * @return The result
	 */
	public Result getResult(String argumentName) {
		lazyLoadResults();
		if (argumentName == null) {
			return null;
		}
		for (Result result : getResults().keySet()) {
			if (argumentName.equals(result.name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Get the Result representing the file with the given name from the argument with the given name.
	 * 
	 * @param argumentName The name of the argument
	 * @param href filename with path relative to the argument.
	 * @return The result
	 */
	public Result getResult(String argumentName, String href) {
		lazyLoadResults();
		if (argumentName == null || href == null || results == null) {
			return null;
		}

		href = href.replaceAll(" ", "%20");

		Result argument = getResult(argumentName);
		if (!results.containsKey(argument)) {
			return null;
		}

		for (Result r : getResults(argumentName)) {
			if (href.equals(r.href) || href.equals(r.prettyRelativeHref)) {
				return r;
			}
		}

		return null;
	}

	/**
	 * Get the list of Results for the argument with the given name.
	 * 
	 * @param argumentName The name of the argument
	 * @return The list of results
	 */
	public List<Result> getResults(String argumentName) {
		if (argumentName == null) {
			return null;
		}
		for (Result result : getResults().keySet()) {
			if (argumentName.equals(result.name)) {
				return results.get(result);
			}
		}
		return null;
	}

	/**
	 * Get a map of all the Result objects.
	 * 
	 * The keys are the result objects for each named output.
	 * 
	 * Each key is associated with a list of Result objects,
	 * each representing a file in the named output.
	 * 
	 * @return The returned map contains all named outputs and associated files.
	 */
	public SortedMap<Result,List<Result>> getResults() {
		lazyLoad();
		lazyLoadResults();
		return results;
	}

	/**
	 * Parse the list of jobs described by the provided XML document/node.
	 * Example: http://daisy-pipeline.googlecode.com/hg/webservice/samples/xml-formats/jobs.xml
	 * 
	 * @param jobsXml The XML
	 * @return The list of jobs
	 * @throws Pipeline2Exception thrown when an error occurs
	 */
	public static List<Job> parseJobsXml(Node jobsXml) throws Pipeline2Exception {
		List<Job> jobs = new ArrayList<Job>();

		// select root element if the node is a document node
		if (jobsXml instanceof Document)
			jobsXml = XPath.selectNode("/d:jobs", jobsXml, XPath.dp2ns);

		List<Node> jobNodes = XPath.selectNodes("d:job", jobsXml, XPath.dp2ns);
		for (Node jobNode : jobNodes) {
			jobs.add(new Job(jobNode));
		}

		return jobs;
	}

	/**
	 * Check that all the inputs and options is filled out properly.
	 * 
	 * The script associated with this job must be defined.
	 * 
	 * @return a message describing the first error, or null if there is no error
	 */
	public String validate() {
		for (Argument argument : getInputs()) {
			String error = JobValidator.validate(argument, storage);
			if (error != null) {
				return error;
			}
		}
		return null;
	}

	/**
	 * Check that the argument with then provided name is filled out properly.
	 * 
	 * The script associated with this job must be defined.
	 * 
	 * @param name The name of the parameter to validate
	 * @return a message describing the error, or null if there is no error
	 */
	public String validate(String name) {
		for (Argument argument : getInputs()) {
			if (argument.getName().equals(name)) {
				return JobValidator.validate(argument, storage);
			}
		}
		return null;
	}

	// simple getters and setters (to ensure lazy loading is performed)
	public String getId() { lazyLoad(); return id; }
	public String getHref() { lazyLoad(); return href; }
	public String getScriptHref() { lazyLoad(); if (script != null) return script.getHref(); else return scriptHref; }
	public Script getScript() { lazyLoad(); return script; }
	public Status getStatus() { lazyLoad(); return status; }
	public String getLogHref() { lazyLoad(); return logHref; }
	public String getNicename() { lazyLoad(); return nicename; }
	public String getDescription() { lazyLoad(); return description; }
	public String getBatchId() { lazyLoad(); return batchId; }
	public Priority getPriority() { lazyLoad(); return priority; }
	public List<Callback> getCallback() { lazyLoad(); return callback; }
	public JobStorage getJobStorage() { lazyLoad(); return storage; }
	public void setId(String id) { lazyLoad(); this.id = id; }
	public void setNicename(String nicename) { lazyLoad(); this.nicename = nicename; }
	public void setDescription(String description) { lazyLoad(); this.description = description; }
	public void setBatchId(String batchId) { lazyLoad(); this.batchId = batchId; }
	public void setPriority(Priority priority) { lazyLoad(); this.priority = priority; }
	public void setCallback(List<Callback> callback) { lazyLoad(); this.callback = callback; }
	public void setJobStorage(JobStorage storage) { lazyLoad(); this.storage = storage; }
	public void setHref(String href) { lazyLoad(); this.href = href; }
	public void setStatus(Status status) { lazyLoad(); this.status = status; }
	public void setScriptHref(String scriptHref) { lazyLoad(); this.scriptHref = scriptHref; }
	public void setScript(Script script) { lazyLoad(); this.script = script; }
	public void setLogHref(String logHref) { lazyLoad(); this.logHref = logHref; }
	
	public void setMessages(List<Message> messages) {
		lazyLoad();
		
		if (this.messages == messages) {
			return; // same instance
		}
		
		if (messages == null) {
			this.messages = null;
			return;
		}
		
		if (this.messages == null) {
			this.messages = new JobMessages();
		}
		
		Collections.sort(messages);
		int startSequence = messages.get(0).sequence;
		for (int m = this.messages.size()-1; m >= 0; m--) {
			if (this.messages.get(m).sequence >= startSequence) {
				this.messages.remove(m);
			}
		}
		this.messages.addAll(messages);
		Collections.sort(this.messages);
	}
	
	public void setResults(Result result, SortedMap<Result, List<Result>> results) {
		lazyLoad();
		lazyLoadResults();
		
		if (this.results != results) {
			if (results == null) {
				this.results = null;
				
			} else {
				this.results = new TreeMap<Result,List<Result>>();
				
				for (Result outputPortOrOption : results.keySet()) {
					this.results.put(outputPortOrOption, new ArrayList<Result>());
					this.results.get(outputPortOrOption).addAll(results.get(outputPortOrOption));
					Collections.sort(this.results.get(outputPortOrOption));
				}
			}
		}
		
		this.result = result;
	}

	/** Set job XML and re-enable lazy loading for the new XML.
	 * 
	 *  @param jobXml The XML
	 */
	public void setJobXml(Node jobXml) {
		this.jobNode = jobXml;
		this.lazyLoaded = false;
	}

	public List<Argument> getInputs() {
		lazyLoad();
		if (script == null && argumentInputs == null) {
			return new ArrayList<Argument>();
			
		} else if (script == null) {
			return argumentInputs;
			
		} else {
			return script.getInputs();
		}
	}

	public List<Argument> getOutputs() {
		lazyLoad();
		return script == null ? argumentOutputs : script.getOutputs();
	}
	
	public void setInputs(List<Argument> argumentInputs) {
		lazyLoad();
		if (script == null) {
			this.argumentInputs = argumentInputs;
			
		} else {
			script.setInputs(argumentInputs);
		}
	}
	
	public void setOutputs(List<Argument> argumentOutputs) {
		lazyLoad();
		if (script == null) {
			this.argumentOutputs = argumentOutputs;
		} else {
			script.setOutputs(argumentOutputs);
		}
	}

	public Argument getArgument(String name) {
		lazyLoad();
		List<Argument> inputs = getInputs();
		List<Argument> outputs = getOutputs();
		if (inputs != null) {
			for (Argument arg : getInputs()) {
				if (arg.getName().equals(name)) {
					return arg;
				}
			}
		}
		if (outputs != null) {
			for (Argument arg : getOutputs()) {
				if (arg.getName().equals(name)) {
					return arg;
				}
			}
		}
		return null;
	}

	@Override
	public int compareTo(Job o) {
		if (id == null && o.id == null)
			return 0;

		if (id == null && o.id != null)
			return -1;

		if (id != null && o.id == null)
			return 1;

		return id.compareTo(o.id);
	}

	/**
	 * Get a result object for the result with the given absolute or relative href.
	 * 
	 * @param href if relative, must be relative to the top-level `…/results/` URL segment.
	 * @return The result
	 */
	public Result getResultFromHref(String href) {
		if (results == null && resultsNode != null && (href == null || !href.startsWith("http"))) {
			return getResultFromHref(resultsNode, href);
		}
		
		lazyLoadResults();
		if (href == null || "".equals(href) || href.equals(result.href) || href.equals(result.relativeHref)) {
			return result;
		}
		for (Result key : results.keySet()) {
			if (href.equals(key.href) || href.equals(key.relativeHref)) {
				return key;
			}
			for (Result value : results.get(key)) {
				if (value != null && (href.equals(value.href) || href.equals(value.relativeHref))) {
					return value;
				}
			}
		}
		return null;
	}

	public Document toXml() {
		lazyLoad();

		Document jobDocument = XML.getXml("<job xmlns=\"http://www.daisy.org/ns/pipeline/data\"/>");
		Element jobElement = jobDocument.getDocumentElement();

		if (id != null) {
			jobElement.setAttribute("id", id);
		}

		if (href != null) {
			jobElement.setAttribute("href", href);
		}

		if (status != null) {
			jobElement.setAttribute("status", status.toString());
		}

		if (priority != null) {
			jobElement.setAttribute("priority", priority.toString());
		}

		if (script != null) {
			XML.appendChildAcrossDocuments(jobElement, script.toXml().getDocumentElement());
		}

		if (nicename != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "nicename");
			e.setTextContent(nicename);
			jobElement.appendChild(e);
		}

		if (description != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "description");
			e.setAttribute("xml:space", "preserve");
			e.setTextContent(description);
			jobElement.appendChild(e);
		}

		if (batchId != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "batchId");
			e.setTextContent(batchId);
			jobElement.appendChild(e);
		}

		if (logHref != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "log");
			e.setAttribute("href", logHref);
			jobElement.appendChild(e);
		}

		if (callback != null) {
			for (Callback c : callback) {
				XML.appendChildAcrossDocuments(jobElement, c.toXml().getDocumentElement());
			}
		}
		
		if (messages != null) {
			Element e = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "messages");
			for (Message m : messages) {
				Element mElement = jobElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "message");
				if (m.level != null) {
					mElement.setAttribute("level", m.level.toString());
				}
				if (m.sequence != null) {
					mElement.setAttribute("sequence", ""+m.sequence);
				}
				if (m.line != null) {
					mElement.setAttribute("line", ""+m.sequence);
				}
				if (m.column != null) {
					mElement.setAttribute("column", ""+m.sequence);
				}
				if (m.timeStamp != null) {
					mElement.setAttribute("timeStamp", m.formatTimeStamp());
				}
				if (m.file != null) {
					mElement.setAttribute("file", m.file);
				}
				if (m.text != null) {
					mElement.setTextContent(m.text);
				}
				e.appendChild(mElement);
			}
			jobElement.appendChild(e);
		}

		if (results != null) {
			Element resultsElement = jobDocument.createElementNS(XPath.dp2ns.get("d"), "results");
			result.toXml(resultsElement);
			for (Result r : results.keySet()) {
				Element resultElement = jobDocument.createElementNS(XPath.dp2ns.get("d"), "result");
				r.toXml(resultElement);
				for (Result fileResult : results.get(r)) {
					Element fileElement = jobDocument.createElementNS(XPath.dp2ns.get("d"), "result");
					fileResult.toXml(fileElement);
					resultElement.appendChild(fileElement);
				}
				resultsElement.appendChild(resultElement);
			}
			jobElement.appendChild(resultsElement);
		}

		// input and option values are stored in /job/script/* instead of here; no need to mirror those values here if the script is defined
		if (script == null) {
			if (argumentInputs != null) {
				for (Argument arg : argumentInputs) {
					XML.appendChildAcrossDocuments(jobElement, arg.toXml().getDocumentElement());
				}
			}
			if (argumentOutputs != null) {
				for (Argument arg : argumentOutputs) {
					XML.appendChildAcrossDocuments(jobElement, arg.toXml().getDocumentElement());
				}
			}
		}

		return jobDocument;
	}

	/**
	 * Create jobRequest XML.
	 * 
	 * If absoluteUris is true, file hrefs will be absolute file:/ URLs.
	 *  
	 * @param absoluteUris base directory for files
	 * @return jobRequest XML document
	 */
	public Document toJobRequestXml(boolean absoluteUris) {
		lazyLoad();

		Document jobRequestDocument = XML.getXml("<jobRequest xmlns=\"http://www.daisy.org/ns/pipeline/data\"/>");
		Element jobRequestElement = jobRequestDocument.getDocumentElement();

		if (script != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "script");
			e.setAttribute("href", script.getHref());
			jobRequestElement.appendChild(e);

		} else if (scriptHref != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "script");
			e.setAttribute("href", scriptHref);
			jobRequestElement.appendChild(e);
		}

		if (nicename != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "nicename");
			e.setTextContent(nicename);
			jobRequestElement.appendChild(e);
		}

		if (priority != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "priority");
			e.setTextContent(priority.toString());
			jobRequestElement.appendChild(e);
		}

		if (batchId != null) {
			Element e = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "batchId");
			e.setTextContent(batchId);
			jobRequestElement.appendChild(e);
		}

		List<Argument> options = new ArrayList<Argument>();
		List<Argument> inputs = new ArrayList<Argument>();
		List<Argument> outputs = getOutputs();
		for (Argument inputOrOption : getInputs()) {
			if (inputOrOption.getKind() == Argument.Kind.input) {
				inputs.add(inputOrOption);
			} else {
				options.add(inputOrOption);
			}
		}
		File contextDir = storage == null ? null : storage.getContextDir();
		URI contextDirUri = contextDir == null ? null : contextDir.toURI();
		for (Argument input : inputs) {
			if (input.isDefined()) {
				Element arg = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "input");
				arg.setAttribute("name", input.getName());

				if (contextDirUri != null) {
					for (File file : input.getAsFileList(storage)) {
						Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "item");
						String value;
						if (file == null) {
							Pipeline2Logger.logger().warn("File referenced from '"+input.getName()+"' not found in context: '"+input.get()+"'");
							value = input.get();
							
						} else if (absoluteUris) {
							value = file.toURI().toString();

						} else {
							value = file.toURI().relativize(contextDirUri).toString();
						}
						item.setAttribute("value", value);
						arg.appendChild(item);
					}

				} else {
					for (String value : input.getAsList()) {
						Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "item");
						item.setAttribute("value", value);
						arg.appendChild(item);
					}
				}
				jobRequestElement.appendChild(arg);
			}
		}
		for (Argument option : options) {
			if (option.isDefined()) {
				Element arg = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "option");
				arg.setAttribute("name", option.getName());

				if (option.getSequence()) {
					if (contextDirUri != null && ("anyFileURI".equals(option.getType()) || "anyDirURI".equals(option.getType()) || "anyURI".equals(option.getType()))) {
						for (File file : option.getAsFileList(storage)) {
							Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "item");
							String value;
							if (file == null) {
								Pipeline2Logger.logger().warn("File referenced from '"+option.getName()+"' not found in context: '"+option.get()+"'");
								value = option.get();
								
							} else if (absoluteUris) {
								value = file.toURI().toString();

							} else {
								value = file.toURI().relativize(contextDirUri).toString();
							}
							item.setAttribute("value", value);
							arg.appendChild(item);
						}

					} else {
						for (String value : option.getAsList()) {
							Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "item");
							item.setAttribute("value", value);
							arg.appendChild(item);
						}
					}

				} else {
					String value;
					if (contextDirUri != null && ("anyFileURI".equals(option.getType()) || "anyDirURI".equals(option.getType()) || "anyURI".equals(option.getType()))) {
						File file = option.getAsFile(storage);
						if (file == null) {
							Pipeline2Logger.logger().warn("File referenced from '"+option.getName()+"' not found in context: '"+option.get()+"'");
							value = option.get();
							
						} else if (absoluteUris) {
							value = file.toURI().toString();

						} else {
							value = file.toURI().relativize(contextDirUri).toString();
						}
						
					} else {
						value = option.get();
					}
					
					if ("".equals(value)) {
						// create d:item to avoid creating a self-closing d:option in the serialized jobRequest XML
						// (using a text node instead of a d:item is just for the aesthetics anyway...)
						Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "item");
						item.setAttribute("value", value);
						arg.appendChild(item);
						
					} else {
						arg.setTextContent(value);
					}
				}

				jobRequestElement.appendChild(arg);
			}
		}
		for (Argument output : outputs) {
			if (output.isDefined()) {
				Element arg = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "output");
				arg.setAttribute("name", output.getName());
				for (String value : output.getAsList()) {
					Element item = jobRequestElement.getOwnerDocument().createElementNS(XPath.dp2ns.get("d"), "item");
					item.setAttribute("value", value);
					arg.appendChild(item);
				}
				jobRequestElement.appendChild(arg);
			}
		}

		if (callback != null) {
			for (Callback c : callback) {
				XML.appendChildAcrossDocuments(jobRequestElement, c.toXml());
			}
		}

		return jobRequestDocument;
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
	
	/**
	 * Get the start of the current progress interval as a percentage.
	 * 
	 * The progress interval might be for instance `[20,30]` which
	 * would mean that the last progress update we got indicated
	 * we were 20% done with the job, and that the next progress update
	 * are expected to come at 30%.
	 * 
	 * For the interval `[20,30]` this method would return `20`.
	 * 
	 * @return the start of the current progress interval.
	 */
	public double getProgressFrom() {
		if (messages == null) {
			return 0.0;
		} else {
			return messages.getProgressFrom();
		}
	}
	
	/**
	 * Get the time of the start of the current progress interval.
	 * 
	 * @return the time as UNIX time.
	 */
	public Long getProgressFromTime() {
		if (messages == null) {
			return null;
		} else {
			return messages.getProgressFromTime();
		}
	}

	/**
	 * Get the end of the current progress interval as a percentage.
	 * 
	 * For the interval `[20,30]` this method would return `30`.
	 * 
	 * @return the end of the current progress interval.
	 */
	public double getProgressTo() {
		if (messages == null) {
			return 100.0;
		} else {
			return messages.getProgressTo();
		}
	}
	

	/**
	 * Get an estimate of the job progress as a percentage. 
	 * 
	 * This returns a percentage within the current progress
	 * interval, interpolated based on the current progress
	 * interval as well as the job duration. It follows this formula:
	 * 
	 * P(t) = P_{N+1} - P_{N} e^{-\frac{t-t_{N}}{T})}
	 * 
	 * where:
	 * - `P` is the percentage
	 * - `t` is the current time, in seconds
	 * - `t_{0}` is the time when the job started (i.e. the first progress message arrived)
	 * - `t_{N}` is the time when information about the current progress interval arrived, in seconds 
	 * - `P_{N+1}` is the percentage at the end of the current progress interval
	 * - `P_{N}` is the percentage at the beginning of the current progress interval
	 * - `T` is the time constant which determines how slowly the estimated progress approaches P_{N+1}.
	 *   20 is chosen as the initial value so that after 60 seconds the progress will be 95 % through the current
	 *   progress interval. When `P_{N} &gt; 0` and `t_{N} - t_{0} &gt; 0` then T is dynamically calculated so that it
	 *   adapts to the speed of the conversion. T is set using this formula:
	 *   
	 *   T = \frac{-t_{N} (\frac{P_{N+1}}{P_{N}} - 1)}{\ln 0.05}
	 *   
	 * 
	 * @return the estimated progress as a percentage.
	 */
	public double getProgressEstimate() {
		return getProgressEstimate(new Date().getTime());
	}
	
	/**
	 * Get an estimate of the job progress for the given time as a percentage.
	 * 
	 * Same as getProgressEstimate() but instead of calculating the progress for
	 * "now", use the UNIX time `now` to calculate instead.
	 * 
	 * This is mostly only useful for testing. It does not give a history of what
	 * the estimates were previously, since only the current progress interval is used
	 * as input to the calculations. 
	 * 
	 * @param now the UNIX time to use as "now".
	 * @return the estimated progress as a percentage.
	 */
	public double getProgressEstimate(Long now) {
		if (status == null || status == Status.IDLE) {
			return 0.0;
		} else if (status != Status.RUNNING) {
			return 100.0;
		} else if (messages == null) {
			return 0.0;
		} else {
			return messages.getProgressEstimate(now);
		}
	}
	
	/**
	 * The progress stack contains information about the progress at each progress "depth".
	 * @return 
	 */
	public Stack<JobMessages.Progress> getProgressStack() {
		return messages.getProgressStack();
	}
}
