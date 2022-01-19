package controllers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.http.Pipeline2HttpClient;
import org.daisy.pipeline.client.http.WSResponse;

import play.mvc.*;

public class SystemStatus extends Controller {

	public static Map<String,EngineAttempt> engineAttempts = new HashMap<String,EngineAttempt>();

	public static class EngineAttempt {
		public Date lastAliveTime = null;
		public WSResponse aliveResponse = null;
		public org.daisy.pipeline.client.models.Alive alive = null;
		public String aliveError = "The endpoint is not responding...";

		public Date lastAuthTime = null;
		public WSResponse authResponse = null;
		public String authError = "The endpoint is not responding...";
	}

	/**
	 * GET /alive
	 * Allows the browser to ping the Web UI, as well as retrieve the local server time.
	 * @return
	 */
	public static Result alive() {
		return ok(play.libs.Jsonp.jsonp("alive", play.libs.Json.toJson(new Date())));
	}

	public static Map<String,Object> statusMap() {
		Map<String,Object> status = new HashMap<String,Object>();
		status.put("time", new Date());
		status.put("engine.state", Application.pipeline2EngineAvailable());
		status.put("engine", Application.getAlive());
		status.put("theme", Application.themeName());
		status.put("version", Application.version);
		return status;
	}

	/**
	 * GET /status
	 * @return
	 */
	public static Result status() {
		return ok(play.libs.Json.toJson(statusMap()));
	}

	/**
	 * GET /status/engine?url=...
	 * @return 
	 */
	public static Result engineStatus() {
		String urls[] = request().queryString().get("url");
		String url = urls==null?null : urls[0];
		EngineAttempt attempt = engineAttempt(url, null, null);
		return ok(play.libs.Json.toJson(attempt.alive == null ? attempt.aliveError+"" : attempt.alive));
	}

	public static EngineAttempt engineAttempt(String url, String authid, String secret) {
		EngineAttempt attempt = null;
		
		if (engineAttempts.containsKey(url)) {
			attempt = engineAttempts.get(url);
		} else {
			attempt = new EngineAttempt();
			engineAttempts.put(url, attempt);
		}
		
		// Ping endpoint
		if (attempt.lastAliveTime == null || new Date().getTime() - attempt.lastAliveTime.getTime() > 5000) {
			attempt.lastAliveTime = new Date();
			
			// Check URL
			boolean urlError = false;
			try {
				new URL(url);
			} catch (MalformedURLException malformedURLException) {
				// Note that not *all* URLs are caught here, but it should be good enough for our purposes:
				// http://stackoverflow.com/a/5965755/281065
				attempt.aliveError = "Invalid URL";
				urlError = true;
			}
			
			if (!urlError) {
				try {
					attempt.aliveResponse = Pipeline2HttpClient.get(url, "/alive", null, null, null);
	
					if (attempt.aliveResponse.status == 200) {
						attempt.alive = new org.daisy.pipeline.client.models.Alive(attempt.aliveResponse.asXml());
						attempt.aliveError = null;
						attempt.authError = null;
						
					} else {
						attempt.aliveError = attempt.aliveResponse.status+" - "+attempt.aliveResponse.statusName+": "+attempt.aliveResponse .statusDescription;
					}
	
				} catch (Pipeline2Exception e) {
					attempt.aliveError = "Something unexpected occured while communicating with the Pipeline 2 framework";
				}
			}
			
			if (attempt.aliveResponse == null && attempt.aliveError == null)
				attempt.aliveError = "Something unexpected occured while communicating with the Pipeline 2 framework";
		}
		if (attempt.aliveError != null)
			attempt.alive = null;

		// Test authentication
		if (attempt.aliveError != null) {
			attempt.authError = "Invalid endpoint; cannot authenticate.";
		} else if (attempt.lastAuthTime == null || new Date().getTime() - attempt.lastAuthTime.getTime() > 500) {
			attempt.lastAuthTime = new Date();
			if (attempt.alive != null && authid != null && secret != null && !attempt.alive.error && attempt.alive.authentication) {
				try {
					attempt.authResponse = Pipeline2HttpClient.get(url, "/jobs", authid, secret, null);
					//attempt.authResponse = org.daisy.pipeline.client.Scripts.get(url, authid, secret);
					if (attempt.authResponse.status == 401) {
						attempt.authError = "Invalid authentication ID or secret text";

					} else if (attempt.authResponse.status != 200) {
						attempt.authError = "An error occured while authenticating.";
						
					} else {
						attempt.authError = null;
					}

				} catch (Pipeline2Exception e) {
					attempt.authError = "An error occured while authenticating; could not reach the Pipeline 2 Engine.";
				}
			}
		}
		if (attempt.authError != null)
			attempt.authResponse = null;

		return attempt;
	}

}
