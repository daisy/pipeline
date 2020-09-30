package org.daisy.pipeline.tts.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * REST Request to communicate with remote services (mainly tts services like Google and Amazon).
 *
 * @author Nicolas Pavie @ braillenet.org
 */
public class Request<ContentType> {

	private static List<String> httpMethods =
		Arrays.asList(
			"GET","POST","PUT","DELETE","HEAD",
			"CONNECT","OPTIONS","TRACE","PATCH");

	private HashMap<String,String> headers = new HashMap<String,String>();
	private ContentType content = null;
	private URL requestURL;
	private String method = "GET";
	private HttpURLConnection connection;

	/**
	 * Fully initialized request
	 *
	 * @param httpMethod possible values: "GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"
	 * @param url a string of the complete request URL (including URL parameters like "?voice=smtg)"
	 * @param headers HTTP headers of the request
	 *                <p>Use {@code null} to unset all headers.</p>
	 * @param content content to send with the request (often associated with POST requests)
	 *                <p>Use {@code null} for requests without content</p>
	 * @throws Exception if the http method is not one of the list above
	 * @throws MalformedURLException if the url is not valid
	 */
	public Request(String httpMethod, String url, HashMap<String,String> headers, ContentType content) throws Exception, MalformedURLException {
		this.setMethod(httpMethod);
		this.setRequestUrl(url);
		this.headers = headers;
		this.content = content;
	}

	/**
	 * Add a new header field with specified value to the request
	 *
	 * @param name name of the header field
	 * @param value value set for the field
	 */
	public void addHeader(String name, String value) {
		this.headers.put(name, value);
	}

	/**
	 * Set the request content, to be sent through the request connection output stream
	 *
	 * @param content content of the request
	 */
	public void setContent(ContentType content) {
		this.content = content;
	}

	/**
	 * Set the request URL.
	 *
	 * <p>If a previous connection was opened for the current request, close and destroy it.</p>
	 */
	public void setRequestUrl(String url) throws MalformedURLException {
		this.requestURL = new URL(url);
		if (this.connection != null) {
			this.connection.disconnect();
			this.connection = null;
		}
	}

	/**
	 * Set the HTTP method to use with the request
	 *
	 * @param httpMethod one of the following values: "GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"
	 * @throws Exception if the HTTP method value is not a valid one
	 */
	public void setMethod(String httpMethod) throws Exception {
		if (!httpMethods.contains(httpMethod.toUpperCase())) {
			throw new Exception(httpMethod + " is not a valid HTTP method (valid methods : " + httpMethods.toString() + ")");
		}
		this.method = httpMethod.toUpperCase();
	}

	/**
	 * Send the request to the requested url and retrieve the server answer as an input stream.
	 *
	 * @return the input stream, through which data are send back by the server
	 * @throws IOException if an error occured during the connection or while sending data to the server
	 */
	public InputStream send() throws IOException {
		connection = (HttpURLConnection) requestURL.openConnection();
		connection.setRequestMethod(method);
		if (headers != null) headers.forEach((String key, String value) -> {
				connection.setRequestProperty(key, value);
			});
		if (content != null) {
			connection.setDoOutput(true);
			try (OutputStream os = connection.getOutputStream()) {
				byte[] input = content.toString().getBytes("utf-8");
				os.write(input, 0, input.length);
			}
		} else {
			connection.setDoOutput(false);
		}
		return connection.getInputStream();
	}

	/**
	 * Get the current connection object
	 */
	public HttpURLConnection getConnection() {
		return connection;
	}

	/**
	 * Cancel the request, disconnecting it from the server
	 */
	public void cancel() {
		if (this.connection != null) {
			this.connection.disconnect();
			this.connection = null;
		}
	}
}
