package org.daisy.pipeline.webservice.request;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import org.restlet.data.Parameter;

import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Abstract class for object that represent a POST or PUT request (jobs, clients, properties or
 * stylesheet-parameters), originating from either a XML document or a query string.
 */
public abstract class Request {

	protected Request() {}

	/* Helper functions */

	protected static Document parseXML(String xml) throws IllegalArgumentException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(xml));
			return builder.parse(is);
		} catch (IOException|ParserConfigurationException|SAXException e) {
			throw new IllegalArgumentException(e);
		}
	}

	protected void checkRequiredFields() throws IllegalArgumentException {
		checkRequiredFields(this);
	}

	protected static <T> void checkRequiredFields(T object) throws IllegalArgumentException {
		checkRequiredFields(object, (Class<T>)object.getClass());
	}

	protected static <T> void checkRequiredFields(T object, Class<T> type) throws IllegalArgumentException {
		for (Field f : type.getDeclaredFields()) {
			if (f.getAnnotation(JsonRequired.class) != null) {
				try {
					f.setAccessible(true);
					if (f.get(object) == null) {
						String fieldName = f.getName();
						SerializedName serializedName = f.getAnnotation(SerializedName.class);
						if (serializedName != null)
							fieldName = serializedName.value();
						throw new IllegalArgumentException("no " + fieldName + " was provided");
					}
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	protected static <T> void checkNoUnexistingFields(JsonElement json, Class<T> type) throws JsonParseException {
		if (json.isJsonObject()) {
			Set<String> existingFields = new HashSet<>(); {
				for (Field f : type.getDeclaredFields()) {
					if (!Modifier.isTransient(f.getModifiers())) {
						String fieldName = f.getName();
						SerializedName serializedName = f.getAnnotation(SerializedName.class);
						if (serializedName != null)
							fieldName = serializedName.value();
						existingFields.add(fieldName);
					}
				}
			}
			for (String key : json.getAsJsonObject().keySet())
				if (!existingFields.contains(key))
					throw new JsonParseException("unknown key: " + key);
		}
	}

	/**
	 * Convert query string to JSON ± according to <a href="https://httpie.io/docs/cli/json">the rules
	 * of HTTPie</a>, then deserialize the JSON.
	 */
	protected static <R extends Request> R fromQuery(Iterable<Parameter> query, Class<R> type, Gson gson)
			throws IllegalArgumentException {
		JsonObject json = new JsonObject();
		for (Parameter param : query) {
			String paramName = param.getName();
			if ("authid".equals(paramName) ||
			    "time".equals(paramName) ||
			    "nonce".equals(paramName) ||
			    "sign".equals(paramName))
				// ignore authentication parameters
				continue;
			List<String> keys = new ArrayList<>();
			StringBuilder kb = new StringBuilder();
			boolean openBracket = false;
			boolean esc = false;
			for (int i = 0; i < paramName.length(); i++) {
				char p = paramName.charAt(i); 
				if (esc) {
					esc = false;
					kb.append(p);
					continue;
				} else if (p == '\\') {
					esc = true;
					continue;
				} else if (p == '[') {
					if (openBracket)
						throw new IllegalArgumentException("malformed query parameter");
					else {
						openBracket = true;
						if (kb != null)
							// this is the first bracket
							if (kb.length() > 0)
								keys.add(kb.toString());
						kb = new StringBuilder();
					}
				} else if (p == ']') {
					if (!openBracket)
						throw new IllegalArgumentException("malformed query parameter");
					else {
						openBracket = false;
						keys.add(kb.toString());
						kb = null;
					}
				} else if (kb == null)
					throw new IllegalArgumentException("malformed query parameter");
				else
					kb.append(p);
			}
			if (esc)
				throw new IllegalArgumentException("malformed query parameter");
			else if (openBracket)
				throw new IllegalArgumentException("malformed query parameter");
			else if (kb != null)
				if (kb.length() == 0)
					throw new IllegalArgumentException("malformed query parameter");
				else
					keys.add(kb.toString());
			if (keys.size() == 0)
				throw new IllegalArgumentException("malformed query parameter");
			Function<Function<JsonElement,JsonElement>,JsonElement> key = j -> json;
			for (String k : keys) {
				if (k.equals("")) {
					JsonArray nested; {
						try {
							nested = key.apply(j -> j != null && !j.isJsonNull() ? j : new JsonArray()).getAsJsonArray();
						} catch (ClassCastException e) {
							throw new IllegalArgumentException("can't perform index operation on object");
						}
					}
					key = f -> {
						JsonElement j = f.apply(null);
						nested.add(j);
						return j;
					};
				} else {
					try {
						int index = Integer.parseInt(k);
						JsonArray nested; {
							try {
								nested = key.apply(j -> j != null && !j.isJsonNull() ? j : new JsonArray()).getAsJsonArray();
							} catch (ClassCastException e) {
								throw new IllegalArgumentException("can't perform index operation on object");
							}
						}
						key = f -> {
							while (index >= nested.size())
								nested.add(JsonNull.INSTANCE);
							JsonElement j = f.apply(nested.get(index));
							nested.set(index, j);
							return j;
						};
					} catch (NumberFormatException e) {
						JsonObject nested; {
							try {
								nested = key.apply(j -> j != null && !j.isJsonNull() ? j : new JsonObject()).getAsJsonObject();
							} catch (ClassCastException ee) {
								throw new IllegalArgumentException("can't perform key operation on array");
							}
						}
						key = f -> {
							JsonElement j = f.apply(nested.get(k));
							nested.add(k, j);
							return j;
						};
					}
				}
			}
			JsonElement val; {
				String v = param.getValue();
				if (v.startsWith("{") || v.startsWith("[")) // support raw JSON
					try {
						val = JsonParser.parseString(v);
					} catch (JsonSyntaxException e) {
						throw new IllegalArgumentException(e.getMessage(), e);
					}
				else
					val = new JsonPrimitive(v);
			}
			key.apply(
				j -> {
					if (j == null)
						return val;
					// the following is not according to the HTTPie rules
					else if (j.isJsonPrimitive()) {
						JsonArray a = new JsonArray();
						a.add(j);
						a.add(val);
						return a;
					} else if (j.isJsonArray()) {
						JsonArray a = j.getAsJsonArray();
						a.add(val);
						return a;
					} else
						throw new IllegalArgumentException("value already set");
				}
			);
		}
		try {
			checkNoUnexistingFields(json, type);
			return gson.fromJson(json, type);
		} catch (JsonParseException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}
}
