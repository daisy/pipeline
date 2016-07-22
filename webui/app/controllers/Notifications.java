package controllers;

import models.NotificationConnection;
import models.User;

import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.WebSocket;

public class Notifications extends Controller {

	/**
	 * Handle WebSocket pushing.
	 */
	public static WebSocket<JsonNode> websocket(Long browserId) {
		User user = User.authenticate(request(), session());
		return NotificationConnection.createWebSocket(user==null?null:user.getId(), browserId);
	}
	
	/**
	 * Handle XHR polling.
	 * @return
	 */
	public static Result xhr(Long browserId) {
		User user = User.authenticate(request(), session());
		return ok(NotificationConnection.pullJson(user==null?null:user.getId(), browserId));
	}
	
	/**
	 * Send browserId to browser.
	 * @return
	 */
	public static Result getBrowserId() {
		User user = User.authenticate(request(), session());
		Map<String,Long> browserId = new HashMap<String,Long>();
		browserId.put("value", User.getBrowserId(user));
		return ok(play.libs.Json.toJson(browserId));
	}
	
}
