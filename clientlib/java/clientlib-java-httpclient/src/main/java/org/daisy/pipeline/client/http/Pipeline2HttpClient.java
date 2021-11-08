package org.daisy.pipeline.client.http;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.w3c.dom.Document;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.utils.XML;

/** Implementation of DP2HttpClient that uses Apache HTTP Client as the underlying HTTP client. */
public class Pipeline2HttpClient {
	
	// TODO: implement PUT support put(...) ?
	
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
	
	public static DateFormat iso8601;
	static {
		iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		iso8601.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	/**
	 * Send a GET request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parameters URL query string parameters
	 * @return The return body.
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public static WSResponse get(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		return getDelete("GET", endpoint, path, username, secret, parameters);
	}
	
	/**
	 * Send a DELETE request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parameters URL query string parameters
	 * @return The return body.
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public static WSResponse delete(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		return getDelete("DELETE", endpoint, path, username, secret, parameters);
	}
	
	private static WSResponse getDelete(String method, String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		String url = url(endpoint, path, username, secret, parameters);
		Pipeline2Logger.logger().debug(method.toUpperCase()+": ["+url+"]");
		if (endpoint == null) {
			return new WSResponse(url, 503, "Endpoint is not set", "Please provide a Pipeline 2 endpoint.", null, null, null);
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpRequestBase http;
		
		if ("DELETE".equals(method)) {
			http = new HttpDelete(url);
		} else { // "GET"
			http = new HttpGet(url);
		}
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(http);
		} catch (ClientProtocolException e) {
			throw new Pipeline2Exception("Error while "+method+"ing.", e);
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while "+method+"ing.", e);
		}
		HttpEntity resEntity = response.getEntity();
		
		InputStream bodyStream = null;
		if (resEntity != null) {
			try {
				bodyStream = resEntity.getContent();
			} catch (IOException e) {
				throw new Pipeline2Exception("Error while reading response body", e);
			}
		}
		
		int status = response.getStatusLine() != null ? response.getStatusLine().getStatusCode() : 204; // Not sure if it's ok to default to 204, but let's try!
		String statusName = response.getStatusLine() != null ? response.getStatusLine().getReasonPhrase() : "";
		String statusDescription = null;
		String contentType = response.getFirstHeader("Content-Type") != null ? response.getFirstHeader("Content-Type").getValue() : "application/octet-stream";
		Long size = (resEntity != null && resEntity.getContentLength() >= 0) ? resEntity.getContentLength() : null;
		
		return new WSResponse(url, status, statusName, statusDescription, contentType, size, bodyStream);
	}
	
	/**
	 * POST an XML document.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param xml The XML document to post.
	 * @return The return body.
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public static WSResponse postXml(String endpoint, String path, String username, String secret, Document xml) throws Pipeline2Exception {
		String url = url(endpoint, path, username, secret, null);
		
		if (Pipeline2Logger.logger().logsLevel(Pipeline2Logger.LEVEL.DEBUG)) {
			Pipeline2Logger.logger().debug("POST: ["+url+"]");
			Pipeline2Logger.logger().debug(XML.toString(xml));
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
		StringEntity entity;
		try {
			entity = new StringEntity(XML.toString(xml), "application/xml", HTTP.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new Pipeline2Exception("Error while serializing XML for POSTing.", e);
		}
		
		httppost.setEntity(entity);
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		}
		HttpEntity resEntity = response.getEntity();
		
		InputStream bodyStream = null;
		try {
			bodyStream = resEntity.getContent();
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while reading response body", e); 
		}
		
		return new WSResponse(url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), null,
				response.getFirstHeader("Content-Type").getValue(),
				resEntity.getContentLength()>=0?resEntity.getContentLength():null,
				bodyStream);
	}
	
	/**
	 * POST a multipart request.
	 * @param endpoint WS endpoint, for instance "http://localhost:8182/ws".
	 * @param path Path to resource, for instance "/scripts".
	 * @param username Robot username. Can be null. If null, then the URL will not be signed.
	 * @param secret Robot secret. Can be null.
	 * @param parts A map of all the parts.
	 * @return The return body.
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public static WSResponse postMultipart(String endpoint, String path, String username, String secret, Map<String,File> parts) throws Pipeline2Exception {
		String url = url(endpoint, path, username, secret, null);
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		
		MultipartEntity reqEntity = new MultipartEntity();
		for (String partName : parts.keySet()) { 
			reqEntity.addPart(partName, new FileBody(parts.get(partName)));
		}
		httppost.setEntity(reqEntity);
		
		HttpResponse response = null;
		try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while POSTing.", e);
		}
		HttpEntity resEntity = response.getEntity();
		
		InputStream bodyStream = null;
		try {
			bodyStream = resEntity.getContent();
		} catch (IOException e) {
			throw new Pipeline2Exception("Error while reading response body", e); 
		}
		
		return new WSResponse(url, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), null,
				response.getFirstHeader("Content-Type").getValue(),
				resEntity.getContentLength()>=0?resEntity.getContentLength():null,
				bodyStream);
	}
	
	/**
	 * Sign a URL for communication with a Pipeline 2 Web Service running in authenticated mode.
	 * 
	 * @param endpoint the Pipeline 2 endpoint
	 * @param path the URL path component
	 * @param username the username
	 * @param secret the secret
	 * @param parameters a map of parameters to encode in the URL
	 * @return the signed URL as a String
	 * @throws Pipeline2Exception thrown if an error occurs
	 */
	public static String url(String endpoint, String path, String username, String secret, Map<String,String> parameters) throws Pipeline2Exception {
		boolean hasAuth = !(username == null || "".equals(username) || secret == null || "".equals(secret));
		
		String url = endpoint + path;
		if (parameters != null && parameters.size() > 0 || hasAuth)
			url += "?";
		
		if (parameters != null) {
			for (String name : parameters.keySet()) {
				try {
					url += URLEncoder.encode(name, "UTF-8")
						+ "=" + URLEncoder.encode(parameters.get(name), "UTF-8")
						+ "&"; }
				catch (UnsupportedEncodingException e) {
					throw new Pipeline2Exception("Unsupported encoding: UTF-8", e); }
			}
		}
		
		if (hasAuth) {

			// add parameters "authid", "time" and "nonce"
			parameters = new HashMap<>();
			parameters.put("authid", username);
			String time = iso8601.format(new Date());
			parameters.put("time", time);
			String nonce = "";
			while (nonce.length() < 30)
				nonce += (Math.random()+"").substring(2);
			nonce = nonce.substring(0, 30);
			parameters.put("nonce", nonce);
			for (String name : parameters.keySet()) {
				try {
					url += name
						+ "=" + URLEncoder.encode(parameters.get(name), "UTF-8")
						+ "&"; }
				catch (UnsupportedEncodingException e) {
					throw new Pipeline2Exception("Unsupported encoding: UTF-8", e); }
			}
			url = url.substring(0, url.length() - 1);

			// add parameter "sign"
			try {
				String hash = calculateRFC2104HMAC(url, secret);
				try {
					url += "&sign"
						+ "=" + URLEncoder.encode(hash, "UTF-8"); }
				catch (UnsupportedEncodingException e) {
					throw new Pipeline2Exception("Unsupported encoding: UTF-8", e); }
			} catch (SignatureException e) {
				throw new Pipeline2Exception("Could not sign request.");
			}
		}
		
		return url;
	}
	
	// adapted slightly from
    // http://docs.amazonwebservices.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/index.html?AuthJavaSampleHMACSignature.html
	// (copied from Pipeline 2 fwk code)
    /**
    * Computes RFC 2104-compliant HMAC signature.
    * * @param data
    * The data to be signed.
    * @param secret
    * The signing secret.
    * @return
    * The Base64-encoded RFC 2104-compliant HMAC signature.
    * @throws
    * java.security.SignatureException when signature generation fails
    */
    private static String calculateRFC2104HMAC(String data, String secret) throws java.security.SignatureException {
        byte[] result;
        try {
            // get an hmac_sha1 key from the raw key bytes
            SecretKeySpec signingSecret = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);

            // get an hmac_sha1 Mac instance and initialize with the signing key
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingSecret);

            // compute the hmac on input data bytes
            byte[] rawHmac = mac.doFinal(data.getBytes());

            // base64-encode the hmac
            result = Base64.getEncoder().encode(rawHmac);

        } catch (Exception e) {
            throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
        }
        return new String(result);
    }
	
}