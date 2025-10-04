package org.daisy.pipeline.pipeline1.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.EventObject;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.Location;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;

import com.google.common.base.Joiner;

// Pipeline 1
import org.daisy.pipeline.core.event.BusListener;
import org.daisy.pipeline.core.event.EventBus;
import org.daisy.pipeline.core.event.JobStateChangeEvent;
import org.daisy.pipeline.core.event.MessageEvent;
import org.daisy.pipeline.core.event.StateChangeEvent;
import org.daisy.pipeline.core.event.TaskMessageEvent;
import org.daisy.pipeline.core.event.TaskStateChangeEvent;
import org.daisy.pipeline.core.script.datatype.BooleanDatatype;
import org.daisy.pipeline.core.script.datatype.Datatype;
import org.daisy.pipeline.core.script.datatype.DatatypeException;
import org.daisy.pipeline.core.script.datatype.EnumDatatype;
import org.daisy.pipeline.core.script.datatype.EnumItem;
import org.daisy.pipeline.core.script.datatype.FileBasedDatatype;
import org.daisy.pipeline.core.script.datatype.FilesDatatype;
import org.daisy.pipeline.core.script.datatype.IntegerDatatype;
import org.daisy.pipeline.core.script.datatype.StringDatatype;
import org.daisy.pipeline.core.script.Job;
import org.daisy.pipeline.core.script.ScriptParameter;
import org.daisy.pipeline.core.script.Task;
import org.daisy.pipeline.exception.JobFailedException;
import org.daisy.util.xml.stax.ExtendedLocationImpl;

// Pipeline 2
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.ValidationResult;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobResourcesDir;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

import org.xml.sax.InputSource;

public class Pipeline1Script extends Script {

	private static final Logger logger = LoggerFactory.getLogger(Pipeline1Script.class);
	private static final Logger detailedLog
	= LoggerFactory.getLogger(org.daisy.pipeline.job.Job.class); // choose logger that is not
	                                                             // included by JobProgressAppender

	@Override
	public Status run(ScriptInput input, Map<String,String> properties, MessageAppender messages,
	                  JobResultSet.Builder resultBuilder, File resultDir) throws IOException {
		if (provider.closed)
			throw new IllegalStateException("script provider is closed");
		Job job = null;
		EventBus bus = null;
		BusListener busListener = null;
		AtomicReference<MessageAppender> currentTaskMessages = new AtomicReference<>();
		try (ThreadLocalEnvironment _env = new ThreadLocalEnvironment(getClass().getClassLoader(),
		                                                              System.getProperties())) {
			for (Map.Entry<String,String> prop : properties.entrySet())
				if (prop.getKey().startsWith("org.daisy.pipeline.pipeline1."))
					System.setProperty(
						prop.getKey().substring("org.daisy.pipeline.pipeline1.".length()),
						prop.getValue());
			job = new Job(script);
			// handle messages
			busListener = new BusListener() {
					private Task currentTask = null;
					@Override
					public void received(EventObject event) {
						if (event instanceof JobStateChangeEvent) {
							if (((StateChangeEvent)event).getState() == StateChangeEvent.Status.STOPPED && currentTask != null) {
								currentTask = null;
								currentTaskMessages.getAndSet(null).close();
							}
						} else if (event instanceof TaskStateChangeEvent) {
							StateChangeEvent se = (StateChangeEvent)event;
							MessageBuilder message = new MessageBuilder().withLevel(Level.INFO);
							Task task = (Task)se.getSource();
							if (currentTask != null) {
								currentTask = null;
								currentTaskMessages.getAndSet(null).close();
							}
							String transformerName = task.getTransformerInfo() != null
								? task.getTransformerInfo().getNiceName()
								: task.getName();
							if (se.getState() == StateChangeEvent.Status.STARTED) {
								message = message.withText("Running " + transformerName);
								currentTask = task;
								currentTaskMessages.set(messages.append(message));
							} else {
								// message = message.withText(transformerName + " done");
								// messages.append(message).close();
							}
						} else if (event instanceof MessageEvent) {
							MessageEvent me = (MessageEvent)event;
							MessageAppender appender = (currentTask != null && me instanceof TaskMessageEvent)
								? currentTaskMessages.get()
								: messages;
							MessageBuilder message = new MessageBuilder();
							switch (me.getType()) {
							case INFO:
							case INFO_FINER:
								message = message.withLevel(Level.INFO);
								break;
							case WARNING:
								message = message.withLevel(Level.WARNING);
								break;
							case ERROR:
								message = message.withLevel(Level.ERROR);
								break;
							case DEBUG:
								message = message.withLevel(Level.DEBUG);
								break;
							}
							String details = null;
							if (me.getLocation() != null) {
								Location loc = me.getLocation();
								String sysId = loc.getSystemId();
								if (sysId != null && sysId.length() > 0) {
									File file = new File(sysId);
									message = message.withFile(file.getPath());
									if (loc.getLineNumber() > -1) {
										message = message.withLine(loc.getLineNumber());
										if (loc.getColumnNumber() > -1)
											message = message.withColumn(loc.getColumnNumber());
									}
								}
								if (loc instanceof ExtendedLocationImpl) {
									ExtendedLocationImpl eLoc = (ExtendedLocationImpl)loc;
									StringBuilder detailsBuilder = new StringBuilder();
									detailsBuilder.append(me.getMessage());
									if (sysId != null)
										detailsBuilder.append("\n\tat ").append(sysId);
									detailsBuilder.append("Location details:\n");
									for (ExtendedLocationImpl.InformationType type : ExtendedLocationImpl.InformationType.values()) {
										detailsBuilder.append("\n\t").append(type.toString()).append(": ");
										String value = eLoc.getExtendedLocationInfo(type);
										detailsBuilder.append(value == null ? "N/A" : value);
									}
									details = detailsBuilder.toString();
								}
							}
							if (details != null) {
								message = message.withText(me.getMessage() + " (Please see detailed log for more info.)");
								switch (me.getType()) {
								case INFO:
								case INFO_FINER:
									detailedLog.info(details);
									break;
								case WARNING:
									detailedLog.warn(details);
									break;
								case ERROR:
									detailedLog.error(details);
									break;
								case DEBUG:
									detailedLog.debug(details);
									break;
								}
							} else {
								message = message.withText(me.getMessage());
							}
							appender.append(message).close();
						} else {
							logger.debug("failed to handle message: " + event);
						}
					}
				};
			bus = new EventBus();
			bus.subscribe(busListener, MessageEvent.class);
			bus.subscribe(busListener, StateChangeEvent.class);
			EventBus.REGISTRY.put(job, bus);
			// store everything to disk just in case it hasn't been done before
			input = input.storeToDisk();
			for (ScriptPort port : getInputPorts()) {
				ScriptParameter param = ((Pipeline1ScriptPort)port).param;
				List<File> files = new ArrayList<>();
				for (Source src : input.getInput(port.getName())) {
					InputSource is = SAXSource.sourceToInputSource(src);
					// make sure documents on input ports have a non-empty base URI
					if (src.getSystemId() == null
					    || "".equals(src.getSystemId())
					    || (is != null && (is.getByteStream() != null || is.getCharacterStream() != null)))
						throw new IllegalStateException(); // should not happen because ScripInput.storeToDisk() was called
					// get file where input was stored
					URI baseURI = resolveRelativePath(URI.create(src.getSystemId()), input);
					files.add(new File(baseURI));
				}
				job.setParameterValue(param.getName(),
				                      Joiner.on(FilesDatatype.SEPARATOR_STRING).join(files));
			}
			for (ScriptOption option : getOptions()) {
				ScriptParameter param = ((Pipeline1ScriptOption)option).param;
				Iterable<String> val = input.getOption(option.getName());
				if (val.iterator().hasNext())
					job.setParameterValue(param.getName(),
					                      ((Pipeline1ScriptOption)option).convertValue(val));
			}
			for (ScriptPort port : getOutputPorts()) {
				ScriptParameter param = ((Pipeline1ScriptPort)port).param;
				String portName = port.getName();
				String resultPath; {
					switch (param.getDatatype().getType()) {
					case FILE:
						resultPath = String.format("%s/%s%s",
						                           portName, portName, ScriptPort.getFileExtension(port.getMediaType()));
						// Pipeline1 may expect directory to exist
						new File(resultDir, resultPath).getParentFile().mkdirs();
						break;
					case DIRECTORY:
						resultPath = String.format("%s/", portName);
						break;
					default:
						throw new IllegalStateException("coding error");
					}
				}
				job.setParameterValue(param.getName(),
				                      new File(resultDir, resultPath).getAbsolutePath());
			}
			provider.core.execute(job);
			// because the Pipeline 1 core only sends these events to EventBus.getInstance(), and we need the
			// event to close the last message appender
			busListener.received(new JobStateChangeEvent(job, StateChangeEvent.Status.STOPPED));
			for (ScriptPort port : getOutputPorts()) {
				ScriptParameter param = ((Pipeline1ScriptPort)port).param;
				File resultPath = new File(job.getParameterValue(param.getName()));
				if (resultPath.exists()) {
					switch (param.getDatatype().getType()) {
					case FILE:
						resultBuilder = resultBuilder.addResult(port.getName(),
						                                        resultDir.toURI().relativize(resultPath.toURI()).toString(),
						                                        resultPath,
						                                        port.getMediaType());
						break;
					case DIRECTORY:
						for (File f : treeFileList(resultPath)) {
							resultBuilder = resultBuilder.addResult(port.getName(),
							                                        resultDir.toURI().relativize(f.toURI()).toString(),
							                                        f,
							                                        port.getMediaType());
						}
						break;
					default:
						throw new IllegalStateException("coding error");
					}
				}
			}
			return Status.SUCCESS;
		} catch (DatatypeException e) {
			// should not happen because input was already validated in BoundScript
			throw new IllegalArgumentException(e);
		} catch (JobFailedException e) {
			// Handle error here, using printStackTrace(), instead of in AbstractJob, which uses
			// Logger.error(String,Throwable), because the stack trace would otherwise be
			// incomplete. This happens because all exceptions that extend BaseException have no
			// getCause() but a getRootCause().
			StringWriter stackTrace = new StringWriter();
			e.printStackTrace(new PrintWriter(stackTrace));
			detailedLog.error("job finished with error state\n" + stackTrace.toString());
			currentTaskMessages.updateAndGet(appender -> {
					if (appender != null)
						appender.close();
					return null; });
			messages.append(new MessageBuilder()
			                .withLevel(Level.ERROR)
			                .withText(e.getMessage() + " (Please see detailed log for more info.)"))
			        .close();
			return Status.ERROR;
		} finally {
			currentTaskMessages.updateAndGet(appender -> {
					if (appender != null)
						appender.close();
					return null; });
			if (bus != null) {
				EventBus.REGISTRY.remove(job);
				bus.unsubscribe(busListener, MessageEvent.class);
				bus.unsubscribe(busListener, StateChangeEvent.class);
			}
		}
	}

	/**
	 * Builder for {@link Pipeline1Script} objects.
	 */
	static class Builder extends Script.Builder {

		private final Pipeline1ScriptProvider provider;
		private final org.daisy.pipeline.core.script.Script script;

		public Builder(Pipeline1ScriptProvider provider,
		               ScriptService<?> descriptor,
		               org.daisy.pipeline.core.script.Script script) {
			super(descriptor);
			this.provider = provider;
			this.script = script;
			shortName = script.getNicename() + " (experimental Pipeline 1 backend)";
			description = script.getDescription();
			int numberOfRequiredInputPorts = 0; {
				for (Map.Entry<String,ScriptParameter> e : script.getParameters().entrySet()) {
					ScriptParameter param = e.getValue();
					Datatype type = e.getValue().getDatatype();
					switch (type.getType()) {
					case FILE:
					case FILES:
					case DIRECTORY:
						if (((FileBasedDatatype)type).isInput()
						    && type.getType() != Datatype.Type.DIRECTORY
						    && param.isRequired())
							numberOfRequiredInputPorts++;
						break;
					default:
					}
				}
			}
			for (Map.Entry<String,ScriptParameter> e : script.getParameters().entrySet()) {
				String name = e.getKey();
				ScriptParameter param = e.getValue();
				Datatype type = param.getDatatype();
				switch (type.getType()) {
				case FILE:
				case FILES:
				case DIRECTORY:
					if (!(type instanceof FileBasedDatatype))
						throw new IllegalStateException(); // should not happen
					if (((FileBasedDatatype)type).isInput()) {
						switch (type.getType()) {
						case DIRECTORY:
							withOption(name, new Pipeline1ScriptOption(name, param, script, provider.datatypeRegistry));
							break;
						default:
							if (numberOfRequiredInputPorts == 1)
								// FIXME: make sure that "source" is not already the name of an option or input
								name = "source";
							withInputPort(name, new Pipeline1ScriptPort(name, param));
						}
					} else { // output
						switch (type.getType()) {
						case FILES:
							throw new IllegalStateException("<files type=\"output\"/> not supported");
						default:
							withOutputPort(name, new Pipeline1ScriptPort(name, param));
						}
					}
					break;
				default:
					withOption(name, new Pipeline1ScriptOption(name, param, script, provider.datatypeRegistry));
				}
			}
			if (id.startsWith("daisy-")) {
				withInputFileset("daisy202");
				withInputFileset("daisy3");
			} else if (id.startsWith("daisy202-"))
				withInputFileset("daisy202");
			else if (id.startsWith("daisy3-"))
				withInputFileset("daisy3");
			else if (id.startsWith("dtbook-")) {
				withInputFileset("dtbook");
				withInputFileset("nimas");
			} else if (id.startsWith("ebraille-"))
				withInputFileset("ebraille");
			else if (id.startsWith("epub-")) {
				withInputFileset("epub2");
				withInputFileset("epub3");
			} else if (id.startsWith("epub2-"))
				withInputFileset("epub2");
			else if (id.startsWith("epub3-"))
				withInputFileset("epub3");
			else if (id.startsWith("html-"))
				withInputFileset("html");
			else if (id.startsWith("mp3-"))
				withInputFileset("mp3");
			else if (id.startsWith("nimas-"))
				withInputFileset("nimas");
			else if (id.startsWith("odt-"))
				withInputFileset("odt");
			else if (id.startsWith("pef-"))
				withInputFileset("pef");
			else if (id.startsWith("rtf-"))
				withInputFileset("rtf");
			else if (id.startsWith("word-"))
				withInputFileset("docx");
			else if (id.startsWith("zedai-"))
				withInputFileset("zedai");
		}

		/**
		 * Builds the {@link Pipeline1Script} instance.
		 */
		@Override
		public Pipeline1Script build() {
			return new Pipeline1Script(id, version, shortName, description, homepage,
			                           inputPorts, outputPorts, options,
			                           inputFilesets, outputFilesets,
			                           provider, script);
		}
	}

	private final Pipeline1ScriptProvider provider;
	private final org.daisy.pipeline.core.script.Script script;

	private Pipeline1Script(String id, String version, String name, String description, String homepage,
	                        Map<String,ScriptPort> inputPorts, Map<String,ScriptPort> outputPorts,
	                        Map<String,ScriptOption> options,
	                        List<String> inputFilesets, List<String> outputFilesets,
	                        Pipeline1ScriptProvider provider, org.daisy.pipeline.core.script.Script script) {
		super(id, version, name, description, homepage, inputPorts, outputPorts, options,
		      inputFilesets, outputFilesets);
		this.provider = provider;
		this.script = script;
	}

	private static class Pipeline1ScriptPort implements ScriptPort {

		private final String name;
		private final ScriptParameter param;
		private final String mediaType;
		private final boolean sequence;

		private Pipeline1ScriptPort(String name, ScriptParameter param) {
			this.name = name;
			this.param = param;
			Datatype type = param.getDatatype();
			if (!(type instanceof FileBasedDatatype))
				throw new IllegalArgumentException(); // should not happen
			mediaType = ((FileBasedDatatype)type).getMime();
			switch (type.getType()) {
			case FILES:
			case DIRECTORY:
				sequence = true;
				break;
			default:
				sequence = false;
				break;
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isPrimary() {
			return false;
		}

		@Override
		public boolean isSequence() {
			return sequence;
		}

		@Override
		public boolean isRequired() {
			return param.isRequired();
		}

		@Override
		public String getNiceName() {
			return param.getNicename();
		}

		@Override
		public String getDescription() {
			return param.getDescription();
		}

		@Override
		public String getMediaType() {
			return mediaType;
		}
	}

	private static class Pipeline1ScriptOption implements ScriptOption {

		private final String name;
		private final ScriptParameter param;
		private final DatatypeService type;

		private Pipeline1ScriptOption(String name,
		                              ScriptParameter param,
		                              org.daisy.pipeline.core.script.Script script,
		                              DatatypeRegistry datatypes) {
			this.name = name;
			this.param = param;
			Datatype type = param.getDatatype();
			switch (type.getType()) {
			case BOOLEAN:
				this.type = DatatypeService.XS_BOOLEAN;
				break;
			case ENUM:
				if (!(type instanceof EnumDatatype))
					throw new IllegalStateException(); // should not happen
				this.type = new EnumDatatypeService(
					String.format("%s-%s", script.getName(), param.getName()),
					(EnumDatatype)type);
				datatypes.register(this.type);
				break;
			case STRING:
				if (!(type instanceof StringDatatype))
					throw new IllegalStateException(); // should not happen
				if (((StringDatatype)type).getRegex() != null) {
					this.type = new RegexDatatypeService(
						String.format("%s-%s", script.getName(), param.getName()),
						(StringDatatype)type);
					datatypes.register(this.type);
				} else
					this.type = DatatypeService.XS_STRING;
				break;
			case INTEGER:
				if (!(type instanceof IntegerDatatype))
					throw new IllegalStateException(); // should not happen
				Integer max = ((IntegerDatatype)type).getMax();
				if (max != Integer.MAX_VALUE)
					throw new IllegalStateException("<integer max=\"" + max + "\"/> not supported");
				Integer min = ((IntegerDatatype)type).getMin();
				if (min == Integer.MIN_VALUE)
					this.type = DatatypeService.XS_INTEGER;
				else if (min == 0)
					this.type = DatatypeService.XS_NON_NEGATIVE_INTEGER;
				else
					throw new IllegalStateException("<integer min=\"" + min + "\"/> not supported");
				break;
			case DIRECTORY:
				this.type = DatatypeService.ANY_DIR_URI;
				break;
			case FILE:
			case FILES:
			default:
				throw new IllegalStateException("coding error");
			}
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isRequired() {
			return param.isRequired();
		}

		@Override
		public String getDefault() {
			return param.getValue();
		}

		@Override
		public String getNiceName() {
			return param.getNicename();
		}

		@Override
		public String getDescription() {
			return param.getDescription();
		}

		@Override
		public DatatypeService getType() {
			return type;
		}

		@Override
		public String getMediaType() {
			return null;
		}

		@Override
		public boolean isPrimary() {
			return false;
		}

		@Override
		public boolean isSequence() {
			return false;
		}

		@Override
		public boolean isOrdered() {
			return false;
		}

		@Override
		public ScriptOption.Role getRole() {
			return null;
		}

		public String convertValue(Iterable<String> value) {
			Iterator<String> i = value.iterator();
			if (i.hasNext()) {
				String v = i.next();
				if (i.hasNext())
					// should not happen if input was validated in BoundScript
					throw new IllegalArgumentException(
						"did not expect more than one value for option" + name + ": " + value);
				return convertValue(v);
			} else
				throw new IllegalArgumentException();
		}

		private String convertValue(String value) {
			if (type == DatatypeService.XS_BOOLEAN) {
				Datatype t = param.getDatatype();
				if (!(t instanceof BooleanDatatype))
					throw new IllegalStateException(); // should not happen
				if (type.validate(value).isValid())
					return ((BooleanDatatype)t).getTrueValue();
				else
					return ((BooleanDatatype)t).getFalseValue();
			} else if (type == DatatypeService.ANY_DIR_URI) {
				try {
					return new File(new URI(value)).getAbsolutePath();
				} catch (URISyntaxException e) {
					// should not happen because value was validated
					throw new IllegalStateException(e);
				} catch (IllegalArgumentException e) { // thrown by new File()
					// should not happen because ScripInput.storeToDisk() was called
					throw new IllegalStateException(e);
				}
			} else
				return value;
		}
	}

	private static class RegexDatatypeService extends DatatypeService {

		private final StringDatatype type;
		private Document xmlDefinition;

		public RegexDatatypeService(String id, StringDatatype type) {
			super(id);
			this.type = type;
		}

		@Override
		public Document asDocument() {
			if (xmlDefinition == null) {
				try {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					                                     .getDOMImplementation().createDocument(null, "data", null);
					Element data = doc.getDocumentElement();
					Attr type = doc.createAttribute("type");
					type.setValue("string");
					data.setAttributeNode(type);
					Element param = (Element)data.appendChild(doc.createElement("param"));
					Attr name = doc.createAttribute("name");
					name.setValue("pattern");
					param.setAttributeNode(name);
					Pattern regex = this.type.getRegex();
					if (regex == null)
						throw new IllegalStateException(); // should not happen
					param.appendChild(doc.createTextNode(regex.pattern()));
					xmlDefinition = doc;
				} catch (ParserConfigurationException|DOMException e) {
					throw new RuntimeException(e);
				}
			}
			return xmlDefinition;
		}

		@Override
		public ValidationResult validate(String content) {
			try {
				type.validate(content);
				return ValidationResult.valid();
			} catch (DatatypeException e) {
				return ValidationResult.notValid(e.getMessage());
			}
		}
	}

	private static class EnumDatatypeService extends DatatypeService {

		private final EnumDatatype type;
		private Document xmlDefinition;

		public EnumDatatypeService(String id, EnumDatatype type) {
			super(id);
			this.type = type;
		}

		@Override
		public Document asDocument() {
			if (xmlDefinition == null) {
				try {
					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
					                                     .getDOMImplementation().createDocument(null, "choice", null);
					Element choice = doc.getDocumentElement();
					for (EnumItem i : type.getItems()) {
						choice.appendChild(doc.createElement("value"))
						      .appendChild(doc.createTextNode(i.getValue()));
						choice.appendChild(doc.createElementNS("http://relaxng.org/ns/compatibility/annotations/1.0", "documentation"))
						      .appendChild(doc.createTextNode(i.getNiceName()));
					}
					xmlDefinition = doc;
				} catch (ParserConfigurationException|DOMException e) {
					throw new RuntimeException(e);
				}
			}
			return xmlDefinition;
		}

		@Override
		public ValidationResult validate(String content) {
			try {
				type.validate(content);
				return ValidationResult.valid();
			} catch (DatatypeException e) {
				return ValidationResult.notValid(e.getMessage());
			}
		}
	}

	/**
	 * @param uri The base URI of a document on an input port of the provided {@link ScriptInput}.
	 */
	private static URI resolveRelativePath(URI uri, ScriptInput input) {
		if (uri.isAbsolute()) { // absolute means URI has scheme component
			if (!"file".equals(uri.getScheme()) || uri.isOpaque())
				throw new IllegalStateException(); // should not happen if the URI comes from a document on an input
				                                   // port: ScripInput does not allow this
			// URI is a file URI with an absolute file path
			return uri;
		} else {
			// URI is a relative path
			JobResources resources = input.getResources();
			if (!(resources instanceof JobResourcesDir))
				throw new IllegalStateException(); // should not happen because ScripInput.storeToDisk() was called
			return ((JobResourcesDir)resources).getBaseDir().toURI().resolve(uri);
		}
	}

	/**
	 * Create a flat list out of a tree directory.
	 */
	private static List<File> treeFileList(File base) {
		LinkedList<File> result = new LinkedList<>();
		File[] fList = base.listFiles();
		if (fList != null)
			for (File f : base.listFiles()) {
				if (f.isDirectory()) {
					result.addAll(treeFileList(f));
				} else {
					result.add(f);
				}
			}
		return result;
	}
}
