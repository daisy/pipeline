package org.daisy.pipeline.client.http;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.utils.XML;
import org.w3c.dom.Document;

public class WSResponse {
	
	public String url;
	public int status;
	public String statusName;
	public String statusDescription;
	public String contentType;
	public Long size;
	private InputStream bodyStream;
	private String bodyText;
	private Document bodyXml;
	
	/**
	 * Creates a new Pipeline2WSResponse with the given HTTP status code, status name, status description and content body.
	 * 
	 * @param url the URL
	 * @param status the HTTP status
	 * @param statusName the HTTP status name
	 * @param statusDescription the HTTP status description
	 * @param contentType the content type
	 * @param size the size of the body
	 * @param bodyStream the body as a InputStream
	 */
	public WSResponse(String url, int status, String statusName, String statusDescription, String contentType, Long size, InputStream bodyStream) {
		this.status = status;
		this.statusName = statusName;
		this.statusDescription = statusDescription;
		this.contentType = contentType;
		this.bodyStream = bodyStream;
		this.bodyXml = null;
	}
	
	/**
	 * Returns the response body as a String.
	 * 
	 * @return the response body as a String.
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public String asText() throws Pipeline2Exception {
		if (bodyText != null)
			return bodyText;
		
		if (bodyStream != null) {
            Writer writer = new StringWriter();
 
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(bodyStream, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } catch (UnsupportedEncodingException e) {
				// unable to open stream
				e.printStackTrace();
			} catch (IOException e) {
				// unable to read buffer
				e.printStackTrace();
			} finally {
            	try {
					bodyStream.close();
					bodyStream = null;
				} catch (IOException e) {
					throw new Pipeline2Exception("Unable to close stream while reading response body", e);
				}
            }
            bodyText = writer.toString();
        }
		
		else if (bodyXml != null) {
	    	try {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				StreamResult result = new StreamResult(new StringWriter());
				DOMSource source = new DOMSource(bodyXml);
				transformer.transform(source, result);
				bodyText = result.getWriter().toString();
				
			} catch (TransformerException e) {
				throw new Pipeline2Exception("Unable to serialize body XML Document as string", e);
			}
		}
		
		return bodyText;
	}
	
	/**
	 * Returns the response body as a InputStream.
	 * 
	 * @return the response body as a InputStream.
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public InputStream asStream() throws Pipeline2Exception {
		if (bodyStream != null)
			return bodyStream;
		
		if (bodyText == null)
			asText();
		
		if (bodyText != null) {
			try {
				return new ByteArrayInputStream(bodyText.getBytes("utf-8"));
	        } catch(UnsupportedEncodingException e) {
	            throw new Pipeline2Exception("Unable to open body string as stream", e);
	        }
		}
		
		return null;
	}
	
	/**
	 * Returns the response body as an XML Document.
	 * 
	 * @return the response body as an XML Document.
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public Document asXml() throws Pipeline2Exception {
		if (bodyXml != null)
			return bodyXml;
		
		if (bodyText == null)
			asText();
		
		bodyXml = XML.getXml(bodyText);
		
		return bodyXml;
	}
	
}
