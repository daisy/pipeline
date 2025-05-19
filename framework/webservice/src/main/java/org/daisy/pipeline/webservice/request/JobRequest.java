package org.daisy.pipeline.webservice.request;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.sax.SAXSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.SerializedName;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.daisy.common.priority.Priority;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.webservice.Callback.CallbackType;
import org.daisy.pipeline.webservice.xml.XmlUtils;
import org.daisy.pipeline.webservice.xml.XmlValidator;

import org.restlet.data.Parameter;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

public class JobRequest extends Request {

	public String getScriptId() { return scriptId; }
	public String getNiceName() { return nicename; }
	public JobBatchId getBatchId() { return batchId; }
	public Priority getPriority() { return priority; }
	public Map<String,List<SAXSource>> getInputs() { return inputs.map; }
	public Map<String,List<String>> getOptions() { return options.map; }
	public List<Callback> getCallbacks() { return callbacks.list; }
	public boolean isOutputElementUsed() { return outputElementUsed; }

	/**
	 * Parse an XML document
	 */
	public static JobRequest fromXML(String xml) throws IllegalArgumentException {
		return fromXML(parseXML(xml));
	}

	public static JobRequest fromXML(Document xml) throws IllegalArgumentException {
		if (!XmlValidator.validate(xml, XmlValidator.JOB_REQUEST_SCHEMA_URL))
			throw new IllegalArgumentException("Supplied XML is not a valid job request");
		try {
			JobRequest req = new JobRequest();
			Element elem = (Element)xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "script").item(0);
			req.script = elem.getAttribute("href");
			NodeList elems = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "nicename");
			if (elems.getLength() != 0)
				req.nicename = elems.item(0).getTextContent();
			elems = xml.getElementsByTagName("batchId");
			if (elems.getLength() != 0)
				req.batchId = JobIdFactory.newBatchIdFromString(elems.item(0).getTextContent());
			elems = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "priority");
			if (elems.getLength() != 0)
				req.priority = Priority.valueOf(elems.item(0).getTextContent().toUpperCase());
			NodeList nodes = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "input");
			if (nodes.getLength() > 0) {
				Map<String,List<SAXSource>> inputs = new HashMap<>();
				for (int i = 0; i < nodes.getLength(); i++) {
					elem = (Element)nodes.item(i);
					List<SAXSource> files = new ArrayList<>();
					NodeList fileNodes = elem.getElementsByTagNameNS(XmlUtils.NS_DAISY, "item");
					if (fileNodes.getLength() > 0) {
						for (int j = 0; j < fileNodes.getLength(); j++) {
							URI file = URI.create(((Element)fileNodes.item(j)).getAttribute("value"));
							files.add(new SAXSource(new InputSource(file.toASCIIString())));
						}
					} else {
						NodeList docwrapperNodes = elem.getElementsByTagNameNS(XmlUtils.NS_DAISY, "docwrapper");
						for (int j = 0; j < docwrapperNodes.getLength(); j++) {
							Element docwrapper = (Element)docwrapperNodes.item(j);
							Node content = null; {
								// find the first element child
								for (int k = 0; k < docwrapper.getChildNodes().getLength(); k++) {
									if (docwrapper.getChildNodes().item(k).getNodeType() == Node.ELEMENT_NODE) {
										content = docwrapper.getChildNodes().item(k);
										break;
									}
								}
							}
							SAXSource source = new SAXSource();
							InputSource is = new InputSource(new StringReader(XmlUtils.nodeToString(content)));
							source.setInputSource(is);
							files.add(source);
						}
					}
					if (files.size() > 0)
						inputs.put(elem.getAttribute("name"), files);
				}
				req.inputs = new Inputs(inputs);
			}
			nodes = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "option");
			if (nodes.getLength() > 0) {
				Map<String,List<String>> options = new HashMap<>();
				for (int i = 0; i < nodes.getLength(); i++) {
					elem = (Element)nodes.item(i);
					List<String> values = new ArrayList<>();
					NodeList itemNodes = elem.getElementsByTagNameNS(XmlUtils.NS_DAISY, "item");
					if (itemNodes.getLength() > 0) {
						// accept <item> children even if it is not a sequence option but at most
						// one (this is verified in BoundScript.Builder)
						for (int j = 0; j < itemNodes.getLength(); j++) {
							values.add(((Element)itemNodes.item(j)).getAttribute("value"));
						}
					} else
						// accept text node even if it is a sequence option
						values.add(elem.getTextContent());
					if (values.size() > 0)
						options.put(elem.getAttribute("name"), values);
				}
				req.options = new Options(options);
			}
			if (xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "output").getLength() > 0)
				req.outputElementUsed = true;
			nodes = xml.getElementsByTagNameNS(XmlUtils.NS_DAISY, "callback");
			if (nodes.getLength() > 0) {
				List<Callback> callbacks = new ArrayList<>();
				for (int i = 0; i < nodes.getLength(); i++) {
					Callback callback = new Callback();
					elem = (Element)nodes.item(i);
					callback.type = CallbackType.valueOf(elem.getAttribute("type").toUpperCase());
					try {
						callback.href = new URI(elem.getAttribute("href"));
					} catch (URISyntaxException e) {
						throw new IllegalArgumentException("invalid href: not a URI");
					}
					Attr attr = elem.getAttributeNode("frequency");
					if (attr != null)
						try {
							callback.frequency = Integer.parseInt(attr.getValue());
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException("invalid frequency: not an integer");
						}
					callbacks.add(callback);
				}
				req.callbacks = new Callbacks(callbacks);
			}
			req.validate();
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied XML is not a valid job request: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse a query string
	 */
	public static JobRequest fromQuery(Iterable<Parameter> query) throws IllegalArgumentException {
		try {
			JobRequest req = fromQuery(query, JobRequest.class, GSON);
			req.validate();
			return req;
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied query is not a valid job request: " + e.getMessage(), e);
		}
	}

	/**
	 * Parse a JSON string
	 */
	public static JobRequest fromJSON(String json) throws IllegalArgumentException {
		try {
			JsonElement j = JsonParser.parseString(json);
			checkNoUnexistingFields(j, JobRequest.class);
			JobRequest req = GSON.fromJson(j, JobRequest.class);
			req.validate();
			return req;
		} catch (JsonParseException|IllegalArgumentException e) {
			throw new IllegalArgumentException(
				"Supplied JSON is not a valid job request: " + e.getMessage(), e);
		}
	}

	public String toJSON() {
		return GSON.toJson(this);
	}

	private static final JobBatchId EMPTY_BATCH_ID = JobIdFactory.newBatchIdFromString("");
	@JsonRequired
	private String script;
	private transient String scriptId;
	private String nicename = "";
	private JobBatchId batchId = EMPTY_BATCH_ID;
	private Priority priority = Priority.MEDIUM;
	@SerializedName("input")
	private Inputs inputs = Inputs.EMPTY;
	@SerializedName("option")
	private Options options = Options.EMPTY;
	@SerializedName("callback")
	private Callbacks callbacks = Callbacks.EMPTY;
	private transient boolean outputElementUsed = false;

	private void validate() throws IllegalArgumentException {
		checkRequiredFields();
		scriptId = script;
		// TODO eventually we might want to have an href-script ID lookup table
		// but for now, we'll get the script ID from the last part of the URL
		if (scriptId.endsWith("/"))
			scriptId = scriptId.substring(0, scriptId.length() - 1);
		scriptId = scriptId.substring(scriptId.lastIndexOf('/') + 1);
	}

	private static class Inputs {
		private static final Inputs EMPTY = new Inputs(ImmutableMap.of());
		private final ImmutableMap<String,List<SAXSource>> map;
		private Inputs(Map<String,List<SAXSource>> map) {
			this.map = ImmutableMap.copyOf(Maps.transformValues(map, ImmutableList::copyOf));
		}
	}

	private static class Options {
		private static final Options EMPTY = new Options(ImmutableMap.of());
		private final ImmutableMap<String,List<String>> map;
		private Options(Map<String,List<String>> map) {
			this.map = ImmutableMap.copyOf(Maps.transformValues(map, ImmutableList::copyOf));
		}
	}

	private static class Callbacks {
		private static final Callbacks EMPTY = new Callbacks(ImmutableList.of());
		private final ImmutableList<Callback> list;
		private Callbacks(List<Callback> list) {
			this.list = ImmutableList.copyOf(list);
		}
	}

	public static class Callback {
		@JsonRequired
		private URI href;
		@JsonRequired
		private CallbackType type;
		private int frequency = 1;
		public URI getHref() { return href; }
		public CallbackType getType() { return type; }
		public int getFrequency() { return frequency; }
	}

	private final static Gson GSON
		= new GsonBuilder().registerTypeAdapter(Priority.class, new PriorityAdapter())
		                   .registerTypeAdapter(JobBatchId.class, new JobBatchIdAdapter())
		                   .registerTypeAdapter(CallbackType.class, new CallbackTypeAdapter())
		                   .registerTypeAdapter(Inputs.class, new InputsAdapter())
		                   .registerTypeAdapter(Options.class, new OptionsAdapter())
		                   .registerTypeAdapter(Callbacks.class, new CallbacksAdapter())
		                   .registerTypeAdapter(SAXSource.class, new SAXSourceSerializer())
		                   .setPrettyPrinting()
		                   .create();

	private static class PriorityAdapter implements JsonDeserializer<Priority>, JsonSerializer<Priority> {

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

		public JsonElement serialize(Priority priority, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(priority.name().toLowerCase());
		}
	}

	private static class JobBatchIdAdapter implements JsonDeserializer<JobBatchId>, JsonSerializer<JobBatchId> {

		public JobBatchId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonPrimitive value = json.getAsJsonPrimitive();
				if (!value.isString())
					throw new JsonParseException("invalid batch ID: not a string");
				return JobIdFactory.newBatchIdFromString(value.getAsString());
			} catch (IllegalStateException e) {
				throw new JsonParseException("invalid batch ID: not a string", e);
			}
		}

		public JsonElement serialize(JobBatchId id, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(id.toString());
		}
	}

	private static class CallbackTypeAdapter implements JsonDeserializer<CallbackType>, JsonSerializer<CallbackType> {

		public CallbackType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			try {
				JsonPrimitive value = json.getAsJsonPrimitive();
				if (!value.isString())
					throw new JsonParseException("invalid callback type: not a string");
				return CallbackType.valueOf(value.getAsString().toUpperCase());
			} catch (IllegalStateException e) {
				throw new JsonParseException("invalid callback type: not a string", e);
			} catch (IllegalArgumentException e) {
				throw new JsonParseException("invalid callback type: " + e.getMessage(), e);
			}
		}

		public JsonElement serialize(CallbackType t, Type typeOfSrc, JsonSerializationContext context) {
			return new JsonPrimitive(t.name().toLowerCase());
		}
	}

	private static class InputsAdapter implements JsonDeserializer<Inputs>, JsonSerializer<Inputs> {

		public Inputs deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (!json.isJsonObject())
				throw new JsonParseException("invalid inputs: not an object");
			return new Inputs(
				Maps.transformValues(
					json.getAsJsonObject().asMap(),
					j -> {
						if (j.isJsonPrimitive()) {
							JsonPrimitive v = j.getAsJsonPrimitive();
							if (!v.isString())
								throw new JsonParseException("invalid input: not a string or an array");
							return ImmutableList.of(parseInput(v)); }
						else if (j.isJsonArray())
							return Lists.transform(
								j.getAsJsonArray().asList(),
								jj -> {
									try {
										JsonPrimitive v = jj.getAsJsonPrimitive();
										return parseInput(v); }
									catch (IllegalStateException e) {
										throw new JsonParseException("invalid input: not a string"); }});
						else
							throw new JsonParseException("invalid input: not a string or an array"); }));
		}

		public JsonElement serialize(Inputs inputs, Type typeOfSrc, JsonSerializationContext context) {
			return GSON.toJsonTree(inputs.map);
		}

		private static SAXSource parseInput(JsonPrimitive json) throws JsonParseException {
			if (!json.isString())
				throw new JsonParseException("invalid input: not a string");
			try {
				return new SAXSource(new InputSource(new URI(json.getAsString()).toASCIIString()));
			} catch (URISyntaxException e) {
				throw new JsonParseException("invalid input: not a URI");
			}
		}
	}

	private static class OptionsAdapter implements JsonDeserializer<Options>, JsonSerializer<Options> {

		public Options deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (!json.isJsonObject())
				throw new JsonParseException("invalid options: not an object");
			return new Options(
				Maps.transformValues(
					json.getAsJsonObject().asMap(),
					j -> {
						if (j.isJsonPrimitive()) {
							JsonPrimitive v = j.getAsJsonPrimitive();
							if (!v.isString())
								throw new JsonParseException("invalid option: not a string or an array");
							return ImmutableList.of(v.getAsString()); }
						else if (j.isJsonArray())
							return Lists.transform(
								j.getAsJsonArray().asList(),
								jj -> {
									try {
										JsonPrimitive v = jj.getAsJsonPrimitive();
										if (!v.isString())
											throw new JsonParseException("invalid option: not a string");
										return v.getAsString(); }
									catch (IllegalStateException e) {
										throw new JsonParseException("invalid option: not a string"); }});
						else
							throw new JsonParseException("invalid option: not a string or an array"); }));
		}

		public JsonElement serialize(Options options, Type typeOfSrc, JsonSerializationContext context) {
			return GSON.toJsonTree(options.map);
		}
	}

	private static class SAXSourceSerializer implements JsonSerializer<SAXSource> {
		public JsonElement serialize(SAXSource src, Type typeOfSrc, JsonSerializationContext context) {
			InputSource is = src.getInputSource();
			if (is.getCharacterStream() != null || is.getByteStream() != null)
				throw new UnsupportedOperationException("toJSON() not supported for job request that was created from XML");
			else
				return new JsonPrimitive(src.getSystemId());
		}
	}

	private static class CallbacksAdapter implements JsonDeserializer<Callbacks>, JsonSerializer<Callbacks> {
		public Callbacks deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			if (!json.isJsonArray())
				throw new JsonParseException("invalid callbacks: not an array");
			return new Callbacks(
				Lists.transform(
					json.getAsJsonArray().asList(),
					j -> {
						if (!j.isJsonObject())
							throw new JsonParseException("invalid callback: not an object");
						checkNoUnexistingFields(j, Callback.class);
						Callback c = GSON.fromJson(j, Callback.class);
						checkRequiredFields(c);
						return c; }));
		}

		public JsonElement serialize(Callbacks callbacks, Type typeOfSrc, JsonSerializationContext context) {
			return GSON.toJsonTree(callbacks.list);
		}
	}
}
