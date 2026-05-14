package org.daisy.pipeline.common.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Response {

	/**
	 * The response code
	 */
	public final int status;

	/**
	 * The response body
	 */
	public final String body;

	/**
	 * The error stream (response body when request failed but server sent useful data nonetheless)
	 */
	public final String error;

	/**
	 * Error that occured while making the connection or sending data to the server.
	 */
	public final IOException exception;

	Response(int status, InputStream body, InputStream error, IOException exception) throws IOException {
		this.status = status;
		try {
			this.body = body != null ? readStream(body) : null;
			this.error = error != null ? readStream(error) : null;
		} catch (IOException e) {
			throw new IOException("Could not read body", e);
		}
		this.exception = exception;
	}

	/**
	 * Read InputStream as a UTF-8 encoded string.
	 */
	private static String readStream(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line.trim());
		}
		br.close();
		return sb.toString();
	}
}
