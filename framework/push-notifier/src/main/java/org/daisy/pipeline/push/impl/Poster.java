package org.daisy.pipeline.push.impl;

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
import org.daisy.pipeline.webserviceutils.Authenticator;
import org.daisy.pipeline.webserviceutils.callback.Callback;
import org.daisy.pipeline.webserviceutils.xml.JobXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlUtils;
import org.daisy.pipeline.webserviceutils.xml.XmlWriterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class Poster {

	/** The logger. */
	private static Logger logger = LoggerFactory.getLogger(Poster.class.getName());

	public static void postMessages(Job job, List<Message> messages, int newerThan, BigDecimal progress, Callback callback) {
		logger.debug("Posting messages to " + callback.getHref());
		URI url = callback.getHref();
		JobXmlWriter writer = XmlWriterFactory.createXmlWriterForJob(job);
		writer.withMessages(messages, newerThan);
		writer.withProgress(progress);
		Document doc = writer.getXmlDocument();
		postXml(doc, url, callback.getClient());
	}

	public static void postStatusUpdate(Job job, Status status,Callback callback) {
		logger.debug("Posting status '" + status + "' to " + callback.getHref());
		URI url = callback.getHref();
		JobXmlWriter writer = XmlWriterFactory.createXmlWriterForJob(job);
		writer.overwriteStatus(status);
		Document doc = writer.getXmlDocument();
		postXml(doc, url, callback.getClient());
	}

	private static void postXml(Document doc, URI url, Client client) {
		//System.out.println(XmlUtils.DOMToString(doc));

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
			return;
        }

        connection.setDoInput (true);
        connection.setDoOutput (true);
        connection.setUseCaches (false);

        try {
            connection.setRequestMethod("POST");
        } catch (ProtocolException e) {
        	logger.error(e.getMessage());
        	return;
        }

        try {
            connection.connect();
        } catch (IOException e) {
        	logger.error(e.getMessage());
        	return;
        }

        DataOutputStream output = null;

        try {
            output = new DataOutputStream(connection.getOutputStream());
        } catch (IOException e) {
        	logger.error(e.getMessage());
        	return;
        }

        // Send the request data.
        try {
	logger.debug("Posting XML: "+XmlUtils.DOMToString(doc));
            output.writeBytes(XmlUtils.DOMToString(doc));
            output.flush();
            output.close();
        } catch (IOException e) {
        	logger.error(e.getMessage());
        	return;
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
        } catch (IOException e) {
            logger.warn("No response");
            logger.debug("No response", e);
        }
    }

}
