package org.daisy.pipeline.webservice;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.RequestLog;
import org.daisy.pipeline.clients.RequestLogEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Authenticator {

	private static Logger logger = LoggerFactory.getLogger(Authenticator.class.getName());
	private RequestLog requestLog;
	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	public Authenticator(RequestLog requestLog) {
		this.requestLog = requestLog;
	}
	
	public boolean authenticate(Client client, String hash, String timestamp, String nonce, String URI, long maxRequestTime) {
		// rules for hashing: use the whole URL string, minus the hash part (&sign=<some value>)
		// important!  put the sign param last so we can easily strip it out

		int idx = URI.indexOf("&sign=", 0);

		if (idx > 1) {
			String hashuri = URI.substring(0, idx);
			String clientSecret = client.getSecret();
			String serverHash = "";
			try {
				serverHash = calculateRFC2104HMAC(hashuri, clientSecret);

				SimpleDateFormat UTC_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
				UTC_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));

				Date serverTimestamp = new Date(System.currentTimeMillis());
				Date clientTimestamp;
				try {
					clientTimestamp = UTC_FORMATTER.parse(timestamp);
				} catch (ParseException e) {
					logger.error(String.format("Could not parse timestamp: %s", timestamp));
					e.printStackTrace();
					return false;
				}
				if(!hash.equals(serverHash)) {
					logger.error("Hash values do not match");
					return false;
				}
				if (serverTimestamp.getTime() - clientTimestamp.getTime() > maxRequestTime) {
					logger.error("Request expired");
					return false;
				}
				if (!checkValidNonce(client, nonce, timestamp)) {
					logger.error("Invalid nonce");
					return false;
				}
				return true;

			} catch (SignatureException e) {
				logger.error("Could not generate hash");
				e.printStackTrace();
				return false;
			}
		} else {
			return false;
		}
	}

	//  The uri param includes all parameters except id, timestamp, and hash"""
	public static URI createUriWithCredentials(String uri, Client client) {
		String uristring = "";
		String timestamp = getCurrentTimestamp();
		String nonce = generateNonce();
		String params = "authid=%s&time=%s&nonce=%s";
		params = String.format(params, client.getId(), timestamp, nonce);
		if (uri.indexOf("?") == -1) {
			uristring = uri + "?" + params;
		}
		else {
			uristring = uri + "&" + params;
		}

		String hash;
		URI newUri =  null;
		try {
			hash = calculateRFC2104HMAC(uristring, client.getSecret());
			String authUri = uristring + "&sign=" + hash;
			newUri = new URI(authUri);
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return newUri;
	}

	// nonces, along with timestamps, protect against replay attacks
	private boolean checkValidNonce(Client client, String nonce, String timestamp) {
		if (client == null) {
			throw new IllegalArgumentException("Client is null");
		}

		RequestLogEntry entry = new SimpleRequestLogEntry(client.getId(), nonce, timestamp);

		// if this nonce was already used with this timestamp, don't accept it again
		if (requestLog.contains(entry)) {
			logger.warn("Duplicate nonce detected.");
			return false;
		} else {
			// else, it is unique and therefore ok
			requestLog.add(entry);
			return true;
		}
	}

	// adapted slightly from
	// http://docs.amazonwebservices.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/index.html?AuthJavaSampleHMACSignature.html
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
		String result;
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingSecret = new SecretKeySpec(secret.getBytes(), HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingSecret);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			// base64-encode the hmac
			result = Base64.encodeBase64String(rawHmac);

			} catch (Exception e) {
				throw new SignatureException("Failed to generate HMAC : " + e.getMessage());
		}
		return result;
	}

	private static String generateNonce() {
		long range = (long) Math.pow(10, 30);
		long num = (long)(new Random().nextDouble() * range);
		String nonce = String.format("%-30d", num).replace(' ', '0');
		return nonce;
	}
	
	private static String getCurrentTimestamp() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String now = dateFormat.format(new Date()) + 'Z';
        return now;
	}



}
