package org.daisy.pipeline.css.impl;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.css.Medium;
import org.daisy.pipeline.webservice.request.Request;
import org.daisy.pipeline.webservice.xml.XmlUtils;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.restlet.data.Parameter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class StylesheetParametersRequest extends Request {

	public List<Medium> getMedia() { return media.list; }
	public List<String> getMediaTypes() { return mediaTypes.list; }
	public Optional<URI> getSourceDocument() { return Optional.ofNullable(sourceDocument); }
	public List<URI> getUserStylesheets() { return userStylesheets.list; }

	/**
	 * Parse an XML document
	 */
	public static StylesheetParametersRequest fromXML(String xml) throws IllegalArgumentException {
		return fromXML(parseXML(xml));
	}

	public static StylesheetParametersRequest fromXML(Document xml) throws IllegalArgumentException {
		if (!XmlValidator.validate(xml, STYLESHEET_PARAMETERS_REQUEST_SCHEMA_URL))
			throw new IllegalArgumentException("Supplied XML is not a valid stylesheet parameters request");
		try {
			StylesheetParametersRequest req = new StylesheetParametersRequest();
			NodeList nodes = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "userStylesheets");
			if (nodes.getLength() > 0) {
				List<URI> userStylesheets = new ArrayList<>();
				NodeList fileNodes = ((Element)nodes.item(0)).getElementsByTagNameNS(XmlUtils.NS_DAISY, "file");
				for (int i = 0; i < fileNodes.getLength(); i++) {
					try {
						userStylesheets.add(new URI(((Element)fileNodes.item(i)).getAttribute("href")));
					} catch (URISyntaxException e) {
						throw new IllegalArgumentException("invalid href: not a URI");
					}
				}
				req.userStylesheets = new UserStylesheets(userStylesheets);
			}
			nodes = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "sourceDocument");
			if (nodes.getLength() > 0) {
				NodeList fileNode = ((Element)nodes.item(0)).getElementsByTagNameNS(XmlUtils.NS_DAISY, "file");
				try {
					req.sourceDocument = new URI(((Element)fileNode.item(0)).getAttribute("href"));
				} catch (URISyntaxException e) {
					throw new IllegalArgumentException("invalid href: not a URI");
				}
			}
			nodes = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "media");
			if (nodes.getLength() > 0)
				req.media = new Media(Medium.parseMultiple(((Element)nodes.item(0)).getAttribute("value")));
			nodes = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "userAgentStylesheet");
			if (nodes.getLength() > 0)
				req.mediaTypes = new MediaTypes(
					Arrays.asList(((Element)nodes.item(0)).getAttribute("mediaType").trim().split("\\s+")));
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied XML is not a valid stylesheet parameters request: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse a query string
	 */
	public static StylesheetParametersRequest fromQuery(Iterable<Parameter> query) throws IllegalArgumentException {
		try {
			return fromQuery(query, StylesheetParametersRequest.class, GSON);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied query is not a valid stylesheet parameters request: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse a JSON string
	 */
	public static StylesheetParametersRequest fromJSON(String json) throws IllegalArgumentException {
		try {
			JsonElement j = JsonParser.parseString(json);
			checkNoUnexistingFields(j, StylesheetParametersRequest.class);
			return GSON.fromJson(j, StylesheetParametersRequest.class);
		} catch (JsonParseException|IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied JSON is not a valid stylesheet parameters request: " + e.getMessage(), e);
		}
	}

	private static final URL STYLESHEET_PARAMETERS_REQUEST_SCHEMA_URL
		= URLs.getResourceFromJAR("rnc/stylesheetParametersRequest.rnc", StylesheetParametersRequest.class);
	private Media media = Media.SCREEN;
	@SerializedName("userAgentStylesheet")
	private MediaTypes mediaTypes = MediaTypes.EMPTY;
	private URI sourceDocument = null;
	private UserStylesheets userStylesheets = UserStylesheets.EMPTY;

	private static class Media {
		private static final Media SCREEN = new Media(Medium.parseMultiple("screen"));
		private final ImmutableList<Medium> list;
		private Media(List<Medium> list) {
			this.list = ImmutableList.copyOf(list);
		}
	}

	private static class MediaTypes {
		private static final MediaTypes EMPTY = new MediaTypes(ImmutableList.of());
		private final ImmutableList<String> list;
		private MediaTypes(List<String> list) {
			this.list = ImmutableList.copyOf(list);
		}
	}

	private static class UserStylesheets {
		private static final UserStylesheets EMPTY = new UserStylesheets(ImmutableList.of());
		private final ImmutableList<URI> list;
		private UserStylesheets(List<URI> list) {
			this.list = ImmutableList.copyOf(list);
		}
	}

	private final static Gson GSON
		= new GsonBuilder().registerTypeAdapter(Media.class, new MediaDeserializer())
		                   .registerTypeAdapter(MediaTypes.class, new MediaTypesDeserializer())
		                   .registerTypeAdapter(UserStylesheets.class, new UserStylesheetsDeserializer())
		                   .create();

	private static class MediaDeserializer implements JsonDeserializer<Media> {
		public Media deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonPrimitive j = json.getAsJsonPrimitive();
				if (!j.isString())
					throw new JsonParseException("invalid media: not a string");
				return new Media(Medium.parseMultiple(j.getAsString()));
			} catch (IllegalStateException e) {
				throw new JsonParseException("invalid media: not a string", e);
			} catch (IllegalArgumentException e) {
				throw new JsonParseException("invalid media: " + e.getMessage(), e);
			}
		}
	}

	private static class MediaTypesDeserializer implements JsonDeserializer<MediaTypes> {
		public MediaTypes deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (!json.isJsonObject())
				throw new JsonParseException("invalid userAgentStylesheet: not an object");
			JsonObject o = json.getAsJsonObject();
			if (!o.has("mediaType"))
				throw new JsonParseException("no mediaType was provided");
			if (o.size() > 0)
				for (String k : o.keySet())
					if (!"mediaType".equals(k))
						throw new JsonParseException("unknown key: " + k);
			JsonElement j = o.get("mediaType");
			if (j.isJsonPrimitive()) {
				try {
					j = j.getAsJsonPrimitive();
					if (!((JsonPrimitive)j).isString())
						throw new JsonParseException("invalid mediaType: not a string");
					return new MediaTypes(Arrays.asList(j.getAsString().trim().split("\\s+")));
				} catch (IllegalStateException e) {
					throw new JsonParseException("invalid mediaType: not a string");
				}
			} else if (j.isJsonArray()) {
				List<String> mediaTypes = new ArrayList<>();
				for (JsonElement jj : j.getAsJsonArray().asList()) {
					try {
						jj = jj.getAsJsonPrimitive();
						if (!((JsonPrimitive)jj).isString())
							throw new JsonParseException("invalid mediaType: not a string");
						if (jj.getAsString().contains(" "))
							throw new JsonParseException("invalid mediaType: should contain no spaces");
						mediaTypes.add(jj.getAsString());
					} catch (IllegalStateException e) {
						throw new JsonParseException("invalid mediaType: not a string");
					}
				}
				return new MediaTypes(mediaTypes);
			} else
				throw new JsonParseException("invalid mediaType: not a string or an array");
		}
	}

	private static class UserStylesheetsDeserializer implements JsonDeserializer<UserStylesheets> {
		public UserStylesheets deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {

			if (!json.isJsonObject())
				throw new JsonParseException("invalid user stylesheets: not an object");
			return new UserStylesheets(
				Lists.transform(
					json.getAsJsonArray().asList(),
					j -> GSON.fromJson(j, URI.class)));
		}
	}
}
