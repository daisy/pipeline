package org.daisy.pipeline.webservice.request;

import java.net.URL;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ClientRequest extends Request {

	public String getId() { return id; }
	public String getSecret() { return secret; }
	public Client.Role getRole() { return role; }
	public String getContact() { return contact; }
	public Priority getPriority() { return priority; }

	/**
	 * Parse an XML document
	 */
	public static ClientRequest fromXML(Document xml) throws IllegalArgumentException {
		return fromXml(xml, null);
	}

	/**
	 * @param id If not <code>null</code>, will be used as the client ID. If <code>null</code>, the
	 * ID must be provided through in the request XML.
	 */
	public static ClientRequest fromXML(Document xml, String id) throws IllegalArgumentException {
		if (!XmlValidator.validate(xml, schema))
			throw new IllegalArgumentException("Supplied XML is not a valid client request");
		try {
			ClientRequest req = new ClientRequest();
			Element root = xml.getDocumentElement();
			req.id = root.getAttribute("id");
			req.secret = root.getAttribute("secret");
			req.role = root.getAttribute("role");
			req.contact = root.getAttribute("contact");
			req.priority = root.getAttribute("priority");
			req.validate(id);
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Supplied XML is not a valid client request", e);
		}
	}

	/**
	 * Parse a query string
	 */
	public static ClientRequest fromQuery(String query) throws IllegalArgumentException {
		return fromQuery(query, null);
	}

	/**
	 * @param id If not <code>null</code>, will be used as the client ID. If <code>null</code>, the
	 * ID must be provided through in the request XML.
	 */
	public static ClientRequest fromQuery(String query, String id) throws IllegalArgumentException {
		try {
			ClientRequest req = fromQuery(json, ClientRequest.class, newGson());
			req.validate(id);
			return req;
		} catch (JsonSyntaxException|IllegalArgumentException e) {
			throw new IllegalArgumentException("Supplied query is not a valid client request", e);
		}
	}

	/**
	 * Parse a JSON string
	 */
	public static ClientRequest fromJSON(String json) throws IllegalArgumentException {
		return fromJSON(json, null);
	}

	/**
	 * @param id If not <code>null</code>, will be used as the client ID. If <code>null</code>, the
	 * ID must be provided through in the request XML.
	 */
	public static ClientRequest fromJSON(String json, String id) throws IllegalArgumentException {
		try {
			ClientRequest req = newGson().fromJson(json, ClientRequest.class);
			req.validate(id);
			return req;
		} catch (JsonSyntaxException|IllegalArgumentException e) {
			throw new IllegalArgumentException("Supplied JSON is not a valid client request", e);
		}
	}

	private final static URL schema = XmlValidator.CLIENT_SCHEMA_URL;
	private String id;
	private String href;
	private String secret;
	private Client.Role role;
	private String contact;
	private Priority priority = Priority.MEDIUM;

	private void validate(String id) throws IllegalArgumentException {
		if (id != null) {
			if (req.id != null && !id.equals(req.id))
				throw new IllegalArgumentException("id mismatch");
			this.id = id;
		} else if (this.id == null)
			throw new IllegalArgumentException("no id was provided");
		if (secret == null)
			throw new IllegalArgumentException("no secret was provided");
		if (role == null)
			throw new IllegalArgumentException("no role was provided");
		if (contact == null)
			throw new IllegalArgumentException("no contact was provided");
	}

	private Gson newGson() {
		return new GsonBuilder().registerTypeAdapter(Client.Role.class, new RoleDeserializer())
		                        .registerTypeAdapter(Priority.class, new PriorityDeserializer())
		                        .create();
	}

	private static class RoleDeserializer implements JsonDeserializer<Client.Role> {
		public Client.Role deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonPrimitive value = json.getAsJsonPrimitive();
				if (!value.isString())
					throw new JsonParseException("invalid role: not a string");
				return Client.Role.valueOf(value.getAsString());
			} catch (IllegalArgumentException  e) {
				throw new JsonParseException("invalid role", e);
			}
		}
	}

	private static class PriorityDeserializer implements JsonDeserializer<Priority> {
		public Priority deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonPrimitive value = json.getAsJsonPrimitive();
				if (!value.isString())
					throw new JsonParseException("invalid priority: not a string");
				return Priority.valueOf(value.getAsString());
			} catch (IllegalArgumentException e) {
				throw new JsonParseException("invalid priority");
			}
		}
	}
}
