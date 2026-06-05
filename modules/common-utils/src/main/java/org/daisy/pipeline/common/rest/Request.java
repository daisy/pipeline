package org.daisy.pipeline.common.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteStreams;

import org.daisy.common.file.Resource;

/**
 * REST request to communicate with remote services (such as TTS services like Google and Amazon).
 *
 * @author Nicolas Pavie @ braillenet.org
 */
public class Request {

	private static List<String> httpMethods =
		Arrays.asList(
			"GET","POST","PUT","DELETE","HEAD",
			"CONNECT","OPTIONS","TRACE","PATCH");

	private Map<String,String> headers = new HashMap<String,String>();
	private String contentType = null;
	private String content = null;
	private ByteArrayOutputStream formData = null;
	private String multipartBoundary = null;
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
	public Request(String httpMethod, String url, Map<String,String> headers, String content)
			throws IllegalArgumentException, MalformedURLException {
		this.setMethod(httpMethod);
		this.setRequestUrl(url);
		this.headers = headers;
		this.content = content;
	}

	public Request(String url) throws MalformedURLException {
		setRequestUrl(url);
	}

	public Request(URL url) {
		setRequestUrl(url);
	}

	/**
	 * Add a new header field with specified value to the request
	 *
	 * @param name name of the header field
	 * @param value value set for the field
	 */
	public Request addHeader(String name, String value) {
		if ("Content-Type".equals(name)) {
			if (contentType != null && !contentType.equals(value))
				throw new IllegalStateException("content type already set to: " + contentType);
			else
				contentType = value;
		} else
			headers.put(name, value);
		return this;
	}

	public Request setContentType(String contentType) {
		return addHeader("Content-Type", contentType);
	}

	/**
	 * Set the request body, to be sent through the request connection output stream
	 *
	 * @param content the content
	 */
	public Request setContent(String content) {
		if (this.content != null || formData != null)
			throw new IllegalStateException("content already set");
		else
			this.content = content;
		return this;
	}

	/**
	 * Append form data to the multipart body of the request
	 *
	 * @param name         the name of the subpart
	 * @param fileName     the optional file name associated with the subpart
	 * @param contentType  the optional mimetype associated with the subpart
	 * @param content      the content
	 */
	public Request setFormDataContent(String name, String fileName, String contentType, InputStream content)
			throws IOException {
		if (this.content != null)
			throw new IllegalStateException("content already set");
		if (formData == null) {
			formData = new ByteArrayOutputStream();
			multipartBoundary = "Boundary-" + new SecureRandom().nextLong();
			setContentType("multipart/form-data; boundary=" + multipartBoundary);
		}
		writeUTF8(formData, String.format("--%s\r\nContent-Disposition: form-data; name=\"%s\"", multipartBoundary, name));
		if (fileName != null)
			writeUTF8(formData, String.format("; filename=\"%s\"", fileName));
		writeUTF8(formData, "\r\n");
		if (contentType != null)
			writeUTF8(formData, String.format("Content-Type: %s\r\n", contentType));
		writeUTF8(formData, "\r\n");
		transferTo(content, formData);
		writeUTF8(formData, "\r\n");
		return this;
	}

	public Request setFormDataContent(String name, String content) throws IOException {
		return setFormDataContent(name, null, content);
	}

	public Request setFormDataContent(String name, String contentType, String content) throws IOException {
		return setFormDataContent(name, null, contentType, new ByteArrayInputStream(content.getBytes("utf-8")));
	}

	public Request setFormDataContent(String name, String contentType, File content) throws IOException {
		return setFormDataContent(name, content.getName(), contentType, new FileInputStream(content));
	}

	public Request setFormDataContent(String name, Resource content) throws IOException {
		return setFormDataContent(name,
		                          content.getPath().toASCIIString(),
		                          content.getMediaType().orElse(null),
		                          content.read());
	}

	/**
	 * Set the request URL.
	 *
	 * <p>If a previous connection was opened for the current request, close and destroy it.</p>
	 */
	public Request setRequestUrl(String url) throws MalformedURLException {
		return setRequestUrl(new URL(url));
	}

	public Request setRequestUrl(URL url) {
		requestURL = url;
		if (this.connection != null) {
			this.connection.disconnect();
			this.connection = null;
		}
		return this;
	}

	/**
	 * Set the HTTP method to use with the request
	 *
	 * @param httpMethod one of the following values: "GET", "POST", "PUT", "DELETE", "HEAD", "CONNECT", "OPTIONS", "TRACE", "PATCH"
	 * @throws IllegalArgumentException if the HTTP method value is not a valid one
	 */
	public Request setMethod(String httpMethod) {
		if (!httpMethods.contains(httpMethod.toUpperCase())) {
			throw new IllegalArgumentException(httpMethod + " is not a valid HTTP method (valid methods : " + httpMethods.toString() + ")");
		}
		method = httpMethod.toUpperCase();
		return this;
	}

	/**
	 * Send the request to the requested URL and retrieve the server answer
	 *
	 * @return the {@link Response} object
	 * @throws InterruptedException if the current thread is interrupted while getting the response from the server
	 * @throws IOException if the response code could not be retrieved, or the body could not be read
	 */
	public Response send() throws InterruptedException, IOException {
		int status = 0;
		InputStream body = null;
		InputStream error = null;
		IOException ioe = null;
		try {
			connection = (HttpURLConnection)requestURL.openConnection();
			connection.setRequestMethod(method);
			if (contentType != null)
				connection.setRequestProperty("Content-Type", contentType);
			if (headers != null)
				headers.forEach((String key, String value) -> {
						connection.setRequestProperty(key, value);
					});
			if (content != null || formData != null) {
				connection.setDoOutput(true);
				try (OutputStream os = connection.getOutputStream()) {
					if (formData != null) {
						writeUTF8(formData, String.format("--%s--\r\n", multipartBoundary));
						formData.flush();
						os.write(formData.toByteArray());
					} else
						writeUTF8(os, content);
				}
			} else {
				connection.setDoOutput(false);
			}
			// handle thread interrupts (fired by TTSTimeout e.g.)
			Thread currentThread = Thread.currentThread();
			Thread handleInterrupt = new Thread() {
					public void run() {
						while (true) {
							try {
								sleep(1000);
							} catch (InterruptedException e) {
								return;
							}
							if (currentThread.isInterrupted()) {
								connection.disconnect(); // unblocks connection.getInputStream() below
								connection = null;
								return;
							}
						}
					}
				};
			handleInterrupt.start();
			try {
				body = connection.getInputStream();
			} catch (IOException e) {
				handleInterrupt.interrupt();
				if (connection == null)
					// cancelled by interrupt handler
					throw new InterruptedException("request was interrupted");
				else
					throw e;
			} finally {
				handleInterrupt.interrupt();
			}
		} catch (IOException e) {
			ioe = e;
		}
		try {
			status = getConnection().getResponseCode();
		} catch (IOException e) {
			throw new IOException(
				"Could not retrieve response code for request. Do you have an internet connection?", e);
		}
		if (ioe != null || status > 299)
			error = getConnection().getErrorStream();
		return new Response(status, body, error, ioe);
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

	private static void writeUTF8(OutputStream os, String content) throws IOException {
		try {
			byte[] input = content.getBytes("utf-8");
			os.write(input, 0, input.length);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("coding error");
		}
	}

	/**
	 * Note that this function is part of Java 9
	 */
	private static long transferTo(InputStream in, OutputStream out) throws IOException {
		return ByteStreams.copy(in, out);
	}
}
