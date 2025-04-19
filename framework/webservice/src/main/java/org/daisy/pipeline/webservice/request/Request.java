package org.daisy.pipeline.webservice.request;

import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

/**
 * Abstract class for object that represent a POST or PUT request (jobs, clients, properties or
 * stylesheet-parameters), originating from either a XML document or a query string.
 */
public abstract class Request {

	protected Request() {}

	/* Helper functions */

	/**
	 * Convert query string to JSON ± according to <a href="https://httpie.io/docs/cli/json">the rules
	 * of HTTPie</a>, then deserialize the JSON.
	 */
	protected static <R extends Request> R fromQuery(String query, Class<R> type, Gson gson)
			throws IllegalArgumentException, JsonSyntaxException {
		JsonElement json = new JsonElement();
		for (String param : queryParams.keys()) {
			if ("authid".equals(param) ||
			    "time".equals(param) ||
			    "nonce".equals(param) ||
			    "sign".equals(param))
				// ignore authentication parameters
				continue;
			List<String> keys = new ArrayList<>();
			StringBuilder kb = new StringBuilder();
			boolean openBracket = false;
			boolean esc = false;
			for (char p : param) {
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
						if (kb != null)
							// this is the first bracket
							if (kb.length() > 0)
								keys.add(kb.build());
						kb = new StringBuilder();
					}
				} else if (p == ']') {
					if (!openBracket)
						throw new IllegalArgumentException("malformed query parameter");
					else {
						openBracket = false;
						keys.add(kb.build());
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
					keys.add(kb.build());
			if (keys.size() == 0)
				throw new IllegalArgumentException("malformed query parameter");
			Function<Function<JsonElement,JsonElement>> key = x -> json;
			for (String k : keys) {
				if (k.equals("")) {
					JsonArray nested; {
						try {
							nested = key(x -> x != null && !x.isJsonNull() ? x : new JsonArray()).asJsonArray();
						} catch (ClassCastException e) {
							throw new IllegalArgumentException("can't perform index operation on object");
						}
					}
					key = x -> {
						JsonElement e = x.apply(null);
						nested.add(e);
						return e;
					}
				} else {
					try {
						int index = Integer.parseInt(k);
						JsonArray nested; {
							try {
								nested = key(x -> x != null && !x.isJsonNull() ? x : new JsonArray()).asJsonArray();
							} catch (ClassCastException e) {
								throw new IllegalArgumentException("can't perform index operation on object");
							}
						}
						key = x -> {
							while (index >= nested.size())
								nested.add(JsonNull.INSTANCE);
							JsonElement e = x.apply(nested.get(index));
							nested.set(index, e);
							return e;
						};
					} catch (NumberFormatException e) {
						JsonObject nested; {
							try {
								nested = key(x -> x != null && !x.isJsonNull() ? x : new JsonObject()).asJsonObject();
							} catch (ClassCastException e) {
								throw new IllegalArgumentException("can't perform key operation on array");
							}
						}
						key = x -> {
							JsonElement e = x.apply(nested.get(k));
							nested.add(k, e);
							return e;
						};
					}
				}
			}
			JsonPrimitive val = new JsonPrimitive(queryParams.get(param));
			key(
				x -> {
					if (x == null)
						return val;
					// the following is not according to the HTTPie rules
					else if (x.isJsonPrimitive()) {
						JsonArray a = new JsonArray<>();
						a.add(val);
						return a;
					} else if (x.isJsonArray()) {
						JsonArray a = x.getAsJsonArray();
						a.add(val);
						return a;
					} else
						throw new IllegalArgumentException("value already set");
				}
			);
		}
		return gson.fromJson(json, type);
	}
}
