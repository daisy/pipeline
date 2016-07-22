package models;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * A push notification
 * @author jostein
 */
public class Notification {
	Map<String,Object> notification;

	public Notification(String kind, Object data) {
		notification = new HashMap<String,Object>();
		notification.put("kind", kind);
		notification.put("data", data);
		notification.put("time", new Date());
	}

	public JsonNode toJson() {
		return play.libs.Json.toJson(notification);
	}

	public String toString() {
		return toJson().toString();
	}
	
	public Date getTime() {
		return (Date)notification.get("time");
	}
	
}