package org.daisy.pipeline.webservice.request;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.restlet.data.Parameter;

import org.w3c.dom.Attr;
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
	public static ClientRequest fromXML(String xml) throws IllegalArgumentException {
		return fromXML(parseXML(xml));
	}

	public static ClientRequest fromXML(Document xml) throws IllegalArgumentException {
		return fromXML(xml, null);
	}

	/**
	 * @param id If not <code>null</code>, will be used as the client ID. If <code>null</code>, the
	 * ID must be provided through in the request XML.
	 */
	public static ClientRequest fromXML(String xml, String id) throws IllegalArgumentException {
		return fromXML(parseXML(xml), id);
	}

	public static ClientRequest fromXML(Document xml, String id) throws IllegalArgumentException {
		if (!XmlValidator.validate(xml, XmlValidator.CLIENT_SCHEMA_URL))
			throw new IllegalArgumentException("Supplied XML is not a valid client request");
		try {
			ClientRequest req = new ClientRequest();
			Element root = xml.getDocumentElement();
			Attr attr = root.getAttributeNode("id");
			req.id = attr != null ? attr.getValue() : null;
			req.secret = root.getAttribute("secret");
			req.role = Client.Role.valueOf(root.getAttribute("role").toUpperCase());
			req.contact = root.getAttribute("contact");
			attr = root.getAttributeNode("priority");
			if (attr != null)
				req.priority = Priority.valueOf(attr.getValue().toUpperCase());
			req.validate(id);
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied XML is not a valid client request: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse a query string
	 */
	public static ClientRequest fromQuery(Iterable<Parameter> query) throws IllegalArgumentException {
		return fromQuery(query, null);
	}

	/**
	 * @param id If not <code>null</code>, will be used as the client ID. If <code>null</code>, the
	 * ID must be provided through in the request XML.
	 */
	public static ClientRequest fromQuery(Iterable<Parameter> query, String id) throws IllegalArgumentException {
		try {
			ClientRequest req = fromQuery(query, ClientRequest.class, GSON);
			req.validate(id);
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied query is not a valid client request: " + e.getMessage(), e);
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
			JsonElement j = JsonParser.parseString(json);
			checkNoUnexistingFields(j, ClientRequest.class);
			ClientRequest req = GSON.fromJson(j, ClientRequest.class);
			req.validate(id);
			return req;
		} catch (JsonParseException|IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied JSON is not a valid client request: " + e.getMessage(), e);
		}
	}

	private String id;
	private String href;
	@JsonRequired
	private String secret;
	@JsonRequired
	private Client.Role role;
	@JsonRequired
	private String contact;
	private Priority priority = Priority.MEDIUM;

	private void validate(String id) throws IllegalArgumentException {
		checkRequiredFields();
		if (id != null) {
			if (this.id != null && !id.equals(this.id))
				throw new IllegalArgumentException("id mismatch");
			this.id = id;
		} else if (this.id == null)
			throw new IllegalArgumentException("no id was provided");
	}

	private final static Gson GSON
		= new GsonBuilder().registerTypeAdapter(Client.Role.class, new RoleDeserializer())
		                   .registerTypeAdapter(Priority.class, new PriorityDeserializer())
		                   .create();

	private static class RoleDeserializer implements JsonDeserializer<Client.Role> {
		public Client.Role deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonPrimitive value = json.getAsJsonPrimitive();
				if (!value.isString())
					throw new JsonParseException("invalid role: not a string");
				return Client.Role.valueOf(value.getAsString().toUpperCase());
			} catch (IllegalStateException e) {
				throw new JsonParseException("invalid role: not a string");
			} catch (IllegalArgumentException e) {
				throw new JsonParseException("invalid role: " + e.getMessage(), e);
			}
		}
	}

	private static class PriorityDeserializer implements JsonDeserializer<Priority> {
		public Priority deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonPrimitive value = json.getAsJsonPrimitive();
				if (!value.isString())
					throw new JsonParseException("invalid priority: not a string");
				return Priority.valueOf(value.getAsString().toUpperCase());
			} catch (IllegalStateException e) {
				throw new JsonParseException("invalid priority: not a string", e);
			} catch (IllegalArgumentException e) {
				throw new JsonParseException("invalid priority: " + e.getMessage(), e);
			}
		}
	}
}
