package org.daisy.pipeline.webservice.impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.daisy.common.messaging.Message;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.webservice.Authenticator;
import org.daisy.pipeline.webservice.Callback;
import org.daisy.pipeline.webservice.xml.JobXmlWriter;
import org.daisy.pipeline.webservice.xml.XmlUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

/**
 * Callback that posts notification messages to a HTTP server.
 */
public class PosterCallback extends Callback {

	private final URI url;
	private final Client client;
	private final String requestRootUrl;
	private static Logger logger = LoggerFactory.getLogger(PosterCallback.class.getName());

	/**
	 * @param requestRootUrl Root URL of the /jobs POST request that declared the callback.
	 */
	public PosterCallback(Job job, CallbackType type, int frequency, URI url, Client client, String requestRootUrl) {
		super(job, type, frequency, 0);
		this.url = url;
		this.client = client;
		this.requestRootUrl = requestRootUrl;
	}

	public boolean postMessages(List<Message> messages, int newerThan, BigDecimal progress) {
		logger.debug("Posting messages to " + url);
		JobXmlWriter writer = new JobXmlWriter(getJob(), requestRootUrl);
		writer.withMessages(messages, newerThan);
		writer.withProgress(progress);
		Document doc = writer.getXmlDocument();
		return postXml(doc, url, client);
	}

	public boolean postStatusUpdate(Status status) {
		logger.debug("Posting status '" + status + "' to " + url);
		JobXmlWriter writer = new JobXmlWriter(getJob(), requestRootUrl);
		writer.overwriteStatus(status);
		Document doc = writer.getXmlDocument();
		return postXml(doc, url, client);
	}

	private static boolean postXml(Document doc, URI url, Client client) {
		URI requestUri = url;
		if (client != null) {
			requestUri = Authenticator.createUriWithCredentials(url.toString(), client);
		}

		// from http://code.geek.sh/2009/10/simple-post-in-java/
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) requestUri.toURL().openConnection();
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}

		connection.setDoInput (true);
		connection.setDoOutput (true);
		connection.setUseCaches (false);

		try {
			connection.setRequestMethod("POST");
		} catch (ProtocolException e) {
			logger.error(e.getMessage());
			return false;
		}

		try {
			connection.connect();
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}

		DataOutputStream output = null;

		try {
			output = new DataOutputStream(connection.getOutputStream());
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}

		// Send the request data.
		try {
			logger.debug("Posting XML: "+XmlUtils.nodeToString(doc));
			output.writeBytes(XmlUtils.nodeToString(doc));
			output.flush();
			output.close();
		} catch (IOException e) {
			logger.error(e.getMessage());
			return false;
		}

		// Get response data. We're not doing anything with it but if we don't retrieve it, the callback doesn't appear to work.
		try {
			logger.debug("Got response: " + connection.getResponseMessage() + " (" + connection.getResponseCode() + ")");
			try {
				DataInputStream input = new DataInputStream(connection.getInputStream());
				try {
					logger.debug((new BufferedReader(new InputStreamReader(input))).lines().collect(Collectors.joining("\n")));
				} finally { input.close(); }
			} catch (Exception e) {
			}
			return true;
		} catch (IOException e) {
			logger.warn("No response");
			logger.debug("No response", e);
			return false;
		}
	}
}
