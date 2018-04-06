package org.daisy.pipeline.client.http;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.models.*;
import org.daisy.pipeline.client.utils.XPath;
import org.w3c.dom.Document;

/**
 * Methods for communicating with the Pipeline 2 API.
 * 
 * @see <a href="http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI">http://code.google.com/p/daisy-pipeline/wiki/WebServiceAPI</a>
 */
public class WS implements WSInterface {

	private String endpoint;
	private String username;
	private String secret;
	private String shutDownKey;
	
	private boolean isLocal;

	public WS() {
		endpoint = "http://localhost:8181/ws";
		username = "clientid";
		secret = "supersecret";
		try {
			List<String> shutDownKeyFileLines = Files.readAllLines(new File(System.getProperty("java.io.tmpdir")+File.separator+"dp2key.txt").toPath());
			shutDownKey = shutDownKeyFileLines.get(0);

		} catch (IOException e) {/*ignore*/}
		checkIfLocal();
	}

	/** Set which Pipeline 2 Web API endpoint to use. Defaults to: "http://localhost:8181/ws" */
	@Override
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
		checkIfLocal();
	}

	@Override
	public String getEndpoint() {
		return endpoint;
	}

	/** Set the credentials to use for the Pipeline 2 Web API. Defaults to "clientid" and "supersecret". */
	@Override
	public void setCredentials(String username, String secret) {
		this.username = username;
		this.secret = secret;
	}

	@Override
	public String getUsername() {
		return username;
	}

	/** Set the key used when invoking /admin/halt to shut down the engine.
	 * 
	 * Defaults to the first line if the "dp2key.txt" file in the systems temporary directory. */
	@Override
	public void setShutDownKey(String shutDownKey) {
		this.shutDownKey = shutDownKey;
	}

	@Override
	public String getShutDownKey() {
		return shutDownKey;
	}

	@Override
	public Alive alive() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/alive", null, null, null);

			if (response.status >= 200 && response.status < 300) {
				return new Alive(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /alive response", e);
			return null;
		}
	}

	@Override
	public boolean halt() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/admin/halt/"+shutDownKey, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return true;

			} else {
				error(response);
				return false;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /admin/halt/"+shutDownKey+" response", e);
			return false;
		}
	}

	@Override
	public List<Property> getProperties() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/admin/properties", username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return Property.parsePropertiesXml(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /admin/properties response", e);
			return null;
		}
	}

	@Override
	public List<Script> getScripts() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/scripts", username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return Script.parseScriptsXml(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /scripts response", e);
			return null;
		}
	}

	@Override
	public Script getScript(String scriptId) {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/scripts/"+scriptId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return new Script(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /scripts/"+scriptId+" response", e);
			return null;
		}
	}

	@Override
	public Map<String,String> getDataTypes() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/datatypes", username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return DataType.getDataTypes(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /datatypes response", e);
			return null;
		}
	}

	@Override
	public DataType getDataType(String dataTypeId) {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/datatypes/"+dataTypeId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return DataType.getDataType(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /datatypes/"+dataTypeId+" response", e);
			return null;
		}
	}

	@Override
	public List<Job> getJobs() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/jobs", username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return Job.parseJobsXml(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /jobs response", e);
			return null;
		}
	}

	@Override
	public Job getJob(String jobId, long msgSeq) {
		try {
			WSResponse response;

			if (msgSeq <= 0) {
				response = Pipeline2HttpClient.get(endpoint, "/jobs/"+jobId, username, secret, null);

			} else {
				Map<String,String> parameters = new HashMap<String,String>();
				parameters.put("msgSeq", msgSeq+"");
				response = Pipeline2HttpClient.get(endpoint, "/jobs/"+jobId, username, secret, parameters);
			}

			if (response.status >= 200 && response.status < 300) {
				return new Job(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /jobs/"+jobId+"?msgSeq="+msgSeq+" response", e);
			return null;
		}
	}

	@Override
	public Job postJob(Job job) {
		try {
			WSResponse response = null;

			Document jobRequestDocument;
			
			if (isLocal) {
				jobRequestDocument = job.toJobRequestXml(true);
				
				response = Pipeline2HttpClient.postXml(endpoint, "/jobs", username, secret, jobRequestDocument);

			} else {
				File contextZipFile = job.getJobStorage().makeContextZip();
				jobRequestDocument = job.toJobRequestXml(false);
				
				try {
					File jobRequestFile = null;

					jobRequestFile = File.createTempFile("jobRequest", ".xml");
					BufferedWriter bw = new BufferedWriter(new FileWriter(jobRequestFile));
					StringWriter writer = new StringWriter();
					Transformer transformer = TransformerFactory.newInstance().newTransformer();
					transformer.transform(new DOMSource(jobRequestDocument), new StreamResult(writer));
					bw.write(writer.toString());
					bw.close();

					Map<String,File> parts = new HashMap<String,File>();
					parts.put("job-request", jobRequestFile);
					parts.put("job-data", contextZipFile);

					response = Pipeline2HttpClient.postMultipart(endpoint, "/jobs", username, secret, parts);

				} catch (IOException e) {
					Pipeline2Logger.logger().error("Could not create and/or write to temporary jobRequest file", e);

				} catch (TransformerConfigurationException e) {
					Pipeline2Logger.logger().error("Could not serialize jobRequest XML", e);

				} catch (TransformerFactoryConfigurationError e) {
					Pipeline2Logger.logger().error("Could not serialize jobRequest XML", new Exception(e));

				} catch (TransformerException e) {
					Pipeline2Logger.logger().error("Could not serialize jobRequest XML", e);
				}
			}

			if (response != null && response.status >= 200 && response.status < 300) {
				return new Job(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /jobs response", e);
			return null;
		}
	}

	@Override
	public boolean deleteJob(String jobId) {
		try {
			WSResponse response = Pipeline2HttpClient.delete(endpoint, "/jobs/"+jobId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return true;

			} else {
				error(response);
				return false;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /jobs/"+jobId+" response", e);
			return false;
		}
	}

	@Override
	public JobSizes getSizes() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/admin/sizes", username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return new JobSizes(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /admin/sizes response", e);
			return null;
		}
	}

	@Override
	public List<Job> getBatch(String batchId) {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/batch/"+batchId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return Job.parseJobsXml(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /batch/"+batchId+" response", e);
			return null;
		}
	}

	@Override
	public boolean deleteBatch(String batchId) {
		try {
			WSResponse response = Pipeline2HttpClient.delete(endpoint, "/batch/"+batchId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return true;

			} else {
				error(response);
				return false;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /batch/"+batchId+" response", e);
			return false;
		}
	}

	@Override
	public String getJobLog(String jobId) {
		try {
			Pipeline2Logger.logger().debug("getting log...");
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/jobs/"+jobId+"/log", username, secret, null);
			String responseText = response.asText();
			return responseText;

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /jobs/"+jobId+"/log response as text", e);
			return null;
		}
	}

	@Override
	public InputStream getJobResultAsStream(String jobId, String href) {
		try {
			if (href == null || "".equals(href))
				href = "";
			else
				href = "/"+href.replace(" ", "%20");
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/jobs/"+jobId+"/result"+href, username, secret, null);
			return response.asStream();

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to get /jobs/"+jobId+"/result"+href+" response as InputStream", e);
			return null;
		}
	}

	@Override
	public File getJobResultAsFile(String jobId, String href) {
		if (href == null) href = "";
		Job job = getJob(jobId, 0);
		if (job == null) {
			return null;
		}

		Result result = job.getResultFromHref(href);

		if (result == null) {
			Pipeline2Logger.logger().error("Could not find href in job results: "+href);
			return null;
		}

		File resultFile;
		
		if (isLocal) {
			if (result.file != null && result.file.length() > 0) {
				// single file download
				try {
					Pipeline2Logger.logger().debug("Reading file from disk: \""+result.file+"\" (href: \""+href+"\")");
					resultFile = new File(new URI(result.file));
	
				} catch (URISyntaxException e) {
					Pipeline2Logger.logger().error("Unable to parse result file path; please make sure that the Pipeline 2 engine is running on the same system as the the client (i.e. the Web UI).", e);
					return null;
	
				} catch (IllegalArgumentException e) {
					Pipeline2Logger.logger().error("Could not read file from disk: "+result.file, e);
					return null;
				}
			}
	
			else {
				// ZIP the results
				try {
					File tempDirForZip = File.createTempFile("webui-result-zip", null);
					Pipeline2Logger.logger().debug("creating temp dir for zip: "+tempDirForZip.getAbsolutePath());
					tempDirForZip.delete();
					tempDirForZip.mkdir();
	
					resultFile = new File(new URI(tempDirForZip.toURI().toString()+"/"+(result.name == null ? jobId : result.name)+".zip"));
					Pipeline2Logger.logger().debug("touching zip: "+resultFile.getAbsolutePath());
					resultFile.createNewFile();
	
					for (Result optionOrPort : job.getResults().keySet()) {
						if (result.from != null && result.from.length() > 0 && result.name != null && !result.name.equals(optionOrPort.name)) {
							continue;
						}
						for (Result optionOrPortFile : job.getResults().get(optionOrPort)) {
							String contextPath = optionOrPortFile.file.substring(0, optionOrPortFile.file.indexOf(jobId) + jobId.length() + "/output/".length());
							String optionOrPortPath = contextPath + optionOrPort.name + "/";
							File optionOrPortDir = new File(new URI(optionOrPortPath));
							for (File file : optionOrPortDir.listFiles()) {
								// NOTE: When downloading multiple output directories, all output directories will be merged into one.
								//       If there are file naming collisions, only the last one will appear in the resulting ZIP.
								org.daisy.pipeline.client.utils.Files.addDirectoryToZip(resultFile, file);
							}
						}
					}
	
				} catch (IOException e) {
					Pipeline2Logger.logger().error("Unable to create result ZIP archive for "+result.from+" "+result.name, e);
					return null;
	
				} catch (URISyntaxException e) {
					Pipeline2Logger.logger().error("Unable to create result ZIP archive for "+result.from+" "+result.name, e);
					return null;
				}
			}
			
		} else {
			// download the file through the Web API
			try {
				WSResponse response = Pipeline2HttpClient.get(endpoint, "/jobs/"+jobId+"/result"+("".equals(result.relativeHref) ? "" : "/"+result.relativeHref), username, secret, null);
				return response.asFile();
				
			} catch (Pipeline2Exception e) {
				Pipeline2Logger.logger().error("Unable to retrieve file from pipeline engine: '"+result.relativeHref+"'", e);
				return null;
			}
		}

		return resultFile;
	}

	@Override
	public JobQueue getQueue() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/queue", username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return new JobQueue(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /queue response", e);
			return null;
		}
	}

	@Override
	public JobQueue moveUpQueue(String jobId) {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/queue/up/"+jobId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return new JobQueue(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /queue/up/"+jobId+" response", e);
			return null;
		}
	}

	@Override
	public JobQueue moveDownQueue(String jobId) {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/queue/down/"+jobId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return new JobQueue(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /queue/down/"+jobId+" response", e);
			return null;
		}
	}

	@Override
	public List<Client> getClients() {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/admin/clients", username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return Client.parseClientsXml(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /admin/clients response", e);
			return null;
		}
	}

	@Override
	public Client getClient(String clientId) {
		try {
			WSResponse response = Pipeline2HttpClient.get(endpoint, "/admin/clients/"+clientId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return new Client(response.asXml());

			} else {
				error(response);
				return null;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /admin/clients/"+clientId+" response", e);
			return null;
		}
	}

	@Override
	public boolean deleteClient(String clientId) {
		try {
			WSResponse response = Pipeline2HttpClient.delete(endpoint, "/admin/clients/"+clientId, username, secret, null);

			if (response.status >= 200 && response.status < 300) {
				return true;

			} else {
				error(response);
				return false;
			}

		} catch (Pipeline2Exception e) {
			Pipeline2Logger.logger().error("failed to parse /admin/clients/"+clientId+" response", e);
			return false;
		}
	}

	// for parsing errors when they occur
	private String error(WSResponse response) {
		if (response == null) {
			return "An error occured while parsing the error recieved from the Pipeline 2 Web API.";
		}
		try {
			Document xml = response.asXml();
			String query = XPath.selectText("/d:error/@query", xml, XPath.dp2ns);
			String description = XPath.selectText("/d:error/d:description", xml, XPath.dp2ns);
			String trace = XPath.selectText("/d:error/d:trace", xml, XPath.dp2ns);
			Pipeline2Logger.logger().error(query+" failed: \n\n"+trace);
			return description;

		} catch (Pipeline2Exception e) {
			try {
				return response.asText();

			} catch (Pipeline2Exception ex) {
				return "An error occured while parsing the error recieved from the Pipeline 2 Web API.";
			}
		}
	}
	
	public void checkIfLocal() {
		Alive alive = alive();
		if (alive != null && alive.localfs == false) {
			isLocal = false;
		} else {
			// Check if the address is defined on any interface
			// Based on http://stackoverflow.com/a/2406819/281065
			try {
				URI url = new URI(endpoint);
				InetAddress addr = InetAddress.getByName(url.getHost());

				// Check if the address is a valid special local or loopback
				if (addr.isAnyLocalAddress() || addr.isLoopbackAddress())
					isLocal = true;

				isLocal = NetworkInterface.getByInetAddress(addr) != null;

			} catch (SocketException e) {
				isLocal = false;

			} catch (UnknownHostException e) {
				isLocal = false;

			} catch (URISyntaxException e) {
				isLocal = false;
			}
		}
	}

}
