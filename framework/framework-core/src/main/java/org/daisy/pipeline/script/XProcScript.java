package org.daisy.pipeline.script;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;

import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.properties.Properties;
import org.daisy.common.xml.DocumentBuilder;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.common.xproc.XProcResult;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.job.impl.IOHelper;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.script.impl.DynamicResultProvider;
import org.daisy.pipeline.script.impl.StatusResultProvider;
import org.daisy.pipeline.script.impl.XProcDecorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;

/**
 * XProc based implementation of {@link Script}
 */
public final class XProcScript extends Script {

	private static final Logger logger = LoggerFactory.getLogger(XProcScript.class);

	private final URI uri;
	private final Map<String,XProcScriptOption> inputOptions;
	private final List<XProcScriptOption> tempOptions;
	private final Map<String,XProcScriptOption> resultOptions;
	private final Optional<ScriptPort> statusPort;
	final boolean someOptionsUseProperty; // whether one or more options have a default value that depends
	                                      // on a (possibly settable) property
	private final XProcEngine xprocEngine;
	private final List<DocumentBuilder> inputParsers;

	public XProcScriptOption getInputOption(String name) {
		return inputOptions.get(name);
	}

	public Iterable<XProcScriptOption> getTempOptions() {
		return tempOptions;
	}

	public XProcScriptOption getResultOption(String name) {
		return resultOptions.get(name);
	}

	public Optional<ScriptPort> getStatusPort() {
		return statusPort;
	}

	@Override
	public String toString() {
		return String.format("XProcScript[name=%s]", this.getName());
	}

	@Override
	public Status run(ScriptInput input,
	                  Map<String,String> properties,
	                  MessageAppender messages,
	                  JobResultSet.Builder resultBuilder,
	                  File resultDir) throws IOException {
		XProcPipeline pipeline = xprocEngine.load(uri);
		XProcDecorator decorator = XProcDecorator.from(this, resultDir, inputParsers);
		XProcInput xprocInput = decorator.decorate(input);
		XProcResult xprocResult = pipeline.run(xprocInput, () -> messages, properties);
		XProcOutput xprocOutput = decorator.decorate(new XProcOutput.Builder().build());
		xprocResult.writeTo(xprocOutput); // writes to files and/or streams specified in output
		buildResultSet(xprocInput, xprocOutput, resultDir, resultBuilder);
		if (checkStatusPort(xprocOutput))
			return Status.SUCCESS;
		else
			return Status.FAIL;
	}

	/**
	 * Builder for {@link XProcScript} objects.
	 */
	public static class Builder extends Script.Builder {

		private final URI uri;
		private final Map<String,XProcScriptOption> inputOptions = new HashMap<>();
		private final List<XProcScriptOption> tempOptions = new ArrayList<>();
		private final Map<String,XProcScriptOption> resultOptions = new HashMap<>();
		private ScriptPort statusPort;
		private final XProcEngine xprocEngine;
		private final List<DocumentBuilder> inputParsers;
		private final DatatypeRegistry datatypes;

		public Builder(XProcScriptService descriptor, URI uri, XProcEngine xprocEngine, List<DocumentBuilder> inputParsers, DatatypeRegistry datatypes) {
			super(descriptor);
			this.uri = uri;
			this.xprocEngine = xprocEngine;
			this.inputParsers = inputParsers;
			this.datatypes = datatypes;
		}

		public Builder(String id, String version, URI uri, XProcEngine xprocEngine, List<DocumentBuilder> inputParsers, DatatypeRegistry datatypes) {
			super(id, version);
			this.uri = uri;
			this.xprocEngine = xprocEngine;
			this.inputParsers = inputParsers;
			this.datatypes = datatypes;
		}

		public Builder withOption(XProcOptionInfo info, XProcOptionMetadata metadata) {
			switch (metadata.getOutput()) {
			case TEMP:
				if (XProcOptionMetadata.ANY_DIR_URI.equals(metadata.getType())) {
					// need (unique) name to derive default value from
					tempOptions.add(new XProcScriptOption(uniqueOptionName(info.getName()), info, metadata, datatypes));
					return this;
				}
				break;
			case RESULT:
				String type = metadata.getType();
				if (XProcOptionMetadata.ANY_FILE_URI.equals(type) || XProcOptionMetadata.ANY_DIR_URI.equals(type)) {
					String portName = uniqueOutputPortName(info.getName());
					ScriptPort port = new ScriptPort() {
							@Override
							public String getName() {
								return portName;
							}
							@Override
							public boolean isPrimary() {
								return metadata.isPrimary();
							}
							@Override
							public boolean isSequence() {
								return XProcOptionMetadata.ANY_DIR_URI.equals(type);
							}
							@Override
							public boolean isRequired() {
								return false;
							}
							@Override
							public String getNiceName() {
								return metadata.getNiceName();
							}
							@Override
							public String getDescription() {
								return metadata.getDescription();
							}
							@Override
							public String getMediaType() {
								return metadata.getMediaType();
							}
						};
					withOutputPort(port.getName(), port);
					// need (unique) name to derive default file/dir from
					resultOptions.put(port.getName(),
					                  new XProcScriptOption(uniqueOptionName(info.getName()), info, metadata, datatypes));
					return this;
				}
				break;
			default:
				if (XProcOptionMetadata.ANY_FILE_URI.equals(metadata.getType())) {
					String portName = uniqueInputPortName(info.getName());
					ScriptPort port = new ScriptPort() {
							@Override
							public String getName() {
								return portName;
							}
							@Override
							public boolean isPrimary() {
								return metadata.isPrimary();
							}
							@Override
							public boolean isSequence() {
								return metadata.isSequence();
							}
							@Override
							public boolean isRequired() {
								return info.isRequired();
							}
							@Override
							public String getNiceName() {
								return metadata.getNiceName();
							}
							@Override
							public String getDescription() {
								return metadata.getDescription();
							}
							@Override
							public String getMediaType() {
								return metadata.getMediaType();
							}
						};
					withInputPort(port.getName(), port);
					inputOptions.put(port.getName(), new XProcScriptOption(null, info, metadata, datatypes));
					return this;
				}
			}
			XProcScriptOption option = new XProcScriptOption(uniqueOptionName(info.getName()), info, metadata, datatypes);
			super.withOption(option.getName(), option);
			return this;
		}

		public Builder withInputPort(XProcPortInfo info, XProcPortMetadata metadata) {
			ScriptPort port = new XProcScriptPort(info, metadata);
			super.withInputPort(port.getName(), port);
			return this;
		}

		public Builder withOutputPort(XProcPortInfo info, XProcPortMetadata metadata) {
			String mediaType = metadata.getMediaType();
			ScriptPort port = new XProcScriptPort(info, metadata);
			if (XProcPortMetadata.MEDIA_TYPE_STATUS_XML.equals(mediaType))
				statusPort = port;
			else
				super.withOutputPort(port.getName(), port);
			return this;
		}

		/**
		 * Builds the {@link XProcScript} instance.
		 */
		@Override
		public XProcScript build() {
			return new XProcScript(xprocEngine, inputParsers, uri, id, version, shortName, description, homepage,
			                       inputPorts, outputPorts, options, inputFilesets, outputFilesets,
			                       inputOptions, tempOptions, resultOptions, statusPort);
		}

		private Map<String,Integer> inputPortCollisions = new HashMap<>();
		private Map<String,Integer> optionCollisions = new HashMap<>();
		private Map<String,Integer> outputPortCollisions = new HashMap<>();

		/**
		 * Remove namespace and handle collisions.
		 */
		private String uniqueInputPortName(QName qName) {
			String name = qName.getLocalPart();
			if (inputPorts.containsKey(name)) {
				inputPorts.put(name + "-1", inputPorts.remove(name));
				inputPortCollisions.put(name, 1);
			}
			if (inputPortCollisions.containsKey(name)) {
				int i = inputPortCollisions.get(name) + 1;
				inputPortCollisions.put(name, i);
				return name + "-" + i;
			} else
				return name;
		}

		/**
		 * Remove namespace and handle collisions.
		 */
		private String uniqueOptionName(QName qName) {
			String name = qName.getLocalPart();
			if (options.containsKey(name)) {
				options.put(name + "-1", options.remove(name));
				optionCollisions.put(name, 1);
			}
			if (optionCollisions.containsKey(name)) {
				int i = optionCollisions.get(name) + 1;
				optionCollisions.put(name, i);
				return name + "-" + i;
			} else
				return name;
		}

		/**
		 * Remove namespace and handle collisions.
		 */
		private String uniqueOutputPortName(QName qName) {
			String name = qName.getLocalPart();
			if (outputPorts.containsKey(name)) {
				outputPorts.put(name + "-1", outputPorts.remove(name));
				outputPortCollisions.put(name, 1);
			}
			if (outputPortCollisions.containsKey(name)) {
				int i = outputPortCollisions.get(name) + 1;
				outputPortCollisions.put(name, i);
				return name + "-" + i;
			} else
				return name;
		}
	}

	private XProcScript(XProcEngine xprocEngine, List<DocumentBuilder> inputParsers,
	                    URI uri, String id, String version, String name, String description, String homepage,
	                    Map<String,ScriptPort> inputPorts, Map<String,ScriptPort> outputPorts, Map<String,ScriptOption> options,
	                    List<String> inputFilesets, List<String> outputFilesets,
	                    Map<String,XProcScriptOption> inputOptions, List<XProcScriptOption> tempOptions,
	                    Map<String,XProcScriptOption> resultOptions, ScriptPort statusPort) {
		super(id, version, name, description, homepage, inputPorts, outputPorts, options, inputFilesets, outputFilesets);
		this.uri = uri;
		this.inputOptions = ImmutableMap.copyOf(inputOptions);
		this.tempOptions = ImmutableList.copyOf(tempOptions);
		this.resultOptions = ImmutableMap.copyOf(resultOptions);
		this.statusPort = Optional.ofNullable(statusPort);
		this.someOptionsUseProperty = Iterables.any(options.values(), o -> ((XProcScriptOption)o).usesProperty)
			|| Iterables.any(inputOptions.values(), o -> o.usesProperty)
			|| Iterables.any(tempOptions, o -> o.usesProperty)
			|| Iterables.any(resultOptions.values(), o -> o.usesProperty);
		this.xprocEngine = xprocEngine;
		this.inputParsers = inputParsers;
	}

	@Override
	public XProcScriptOption getOption(String name) {
		return (XProcScriptOption)super.getOption(name);
	}

	public static class XProcScriptOption implements ScriptOption {

		private final String name;
		private final XProcOptionInfo info;
		private final XProcOptionMetadata metadata;
		private final String defaultValue;
		final boolean usesProperty; // whether the default value depends on a (possibly settable) propeprty
		private final DatatypeService datatype;
		private final boolean isSequence;
		private final boolean typeIsString;
		private final boolean typeIsSequence;

		private static final Pattern SYSTEM_PROPERTY = Pattern.compile("^(?<prefix>[a-zA-Z_][\\w.-]*):system-property\\((?<arg>[^)]+)\\)$");
		private static final Pattern QNAME = Pattern.compile("^(?<prefix>[a-zA-Z_][\\w.-]*):(?<localPart>[a-zA-Z_][\\w.-]*)");
		private static final String NS_XPROC = "http://www.w3.org/ns/xproc";
		private static final String NS_PIPELINE_DATA = "http://www.daisy.org/ns/pipeline/data";

		public XProcScriptOption(String name,
		                         XProcOptionInfo info,
		                         XProcOptionMetadata metadata,
		                         DatatypeRegistry datatypes) {
			this.name = name;
			this.info = info;
			this.metadata = metadata;
			String type = null; {
				if (info.getType() != null) {
					type = info.getType();
					typeIsSequence = type.endsWith("*");
					if (typeIsSequence) {
						type = type.substring(0, type.length() - 1);
						isSequence = true; // ignore metadata.isSequence()
					} else {
						isSequence = metadata.isSequence();
					}
					if (type.contains(":")) {
						String prefix = type.substring(0, type.indexOf(":"));
						String ns = info.getNamespaceContext().getNamespaceURI(prefix);
						if (ns == null)
							throw new IllegalArgumentException(
								"Unbound namespace prefix '" + prefix + "' in cx:as='" + info.getType() + "'");
						else if ("http://www.w3.org/2001/XMLSchema".equals(ns)) {
							type = type.substring(prefix.length() + 1);
							if (type != null && !type.matches("string|integer|nonNegativeInteger|boolean"))
								type = null;
						} else
							type = null;
					} else
						type = null;
					if (type == null)
						throw new IllegalArgumentException(
							"cx:as='" + info.getType() + "' not supported on XProc script options");
					typeIsString = "string".equals(type);
				} else {
					typeIsString = true;
					typeIsSequence = false;
					isSequence = metadata.isSequence();
				}
				if (type == null || "string".equals(type)) // otherwise ignore metadata.getType()
					type = metadata.getType();
			}
			if (type == null || "".equals(type) || "xs:string".equals(type) || "string".equals(type))
				datatype = DatatypeService.XS_STRING;
			else if ("xs:integer".equals(type) || "integer".equals(type))
				datatype = DatatypeService.XS_INTEGER;
			else if ("xs:nonNegativeInteger".equals(type) || "nonNegativeInteger".equals(type))
				datatype = DatatypeService.XS_NON_NEGATIVE_INTEGER;
			else if ("xs:boolean".equals(type) || "boolean".equals(type))
				datatype = DatatypeService.XS_BOOLEAN;
			else if ("xs:anyURI".equals(type) || "anyURI".equals(type))
				datatype = DatatypeService.XS_ANY_URI;
			else if ("anyFileURI".equals(type))
				datatype = DatatypeService.ANY_FILE_URI;
			else if ("anyDirURI".equals(type))
				datatype = DatatypeService.ANY_DIR_URI;
			else {
				datatype = datatypes.getDatatype(type).orNull();
				if (datatype == null)
					throw new IllegalArgumentException(
						"Invalid px:type '" + type + "': does not match a known data type");
			}
			if (info.isRequired()) {
				defaultValue = null;
				usesProperty = false;
			} else {
				String select = info.getSelect();
				if (select != null)
					select = select.trim();
				if (select == null || "".equals(select)) {
					// script options must have a default value even if the XProc option does not have one
					defaultValue = "";
					usesProperty = false;
				} else if (typeIsSequence && select.matches("\\(.*\\)")) {
					logger.debug("Select statement can not be a sequence: " + select); // currently not supported
					defaultValue = "";
					usesProperty = false;
				} else {
					String defaultValue = null;
					String fallbackDefaultValue
						= (datatype == DatatypeService.XS_INTEGER || datatype == DatatypeService.XS_NON_NEGATIVE_INTEGER)
							? "0"
							: datatype == DatatypeService.XS_BOOLEAN
								? "false"
								// FIXME: what about URI types and custom types?
								: "";
					boolean usesProperty = false;
					// check if select statement is a p:system-property() function (allows for setting a default
					// value globally through a Pipeline property)
					Matcher m = SYSTEM_PROPERTY.matcher(select);
					if (m.matches()) {
						NamespaceContext nsContext = info.getNamespaceContext();
						if (nsContext != null) {
							String prf = m.group("prefix");
							String ns = nsContext.getNamespaceURI(prf);
							if (NS_XPROC.equals(ns)) {
								String arg = m.group("arg").trim();
								char quote = arg.charAt(0);
								if ((quote == '"' || quote == '\'') && arg.charAt(arg.length() - 1) == quote) {
									arg = arg.substring(1, arg.length() - 1);
									m = QNAME.matcher(arg);
									if (m.matches()) {
										prf = m.group("prefix");
										ns = nsContext.getNamespaceURI(prf);
										String prop = m.group("localPart");
										if (NS_PIPELINE_DATA.equals(ns)) {
											usesProperty = true;
											defaultValue = Properties.getSnapshot().get(prop);
											if (defaultValue == null) {// if property not settable
												defaultValue = Properties.getProperty(prop);
												if (defaultValue != null)
													try {
														defaultValue = "" + convertValue(defaultValue);
													} catch (IllegalArgumentException e) {
														logger.debug(select + " can not be evaluated to a " + datatype.getId() + ": " + defaultValue);
														defaultValue = fallbackDefaultValue;
													}
											}
											if (defaultValue == null) {
												logger.debug("Property does not have a value: " + prop);
												defaultValue = fallbackDefaultValue;
											}
										} else if (ns == null)
											logger.debug("Unbound namespace prefix: " + prf);
										else {
											logger.debug("Property '" + prop + "'is in an unknown namespace: '" + ns + "'");
											defaultValue = fallbackDefaultValue;
										}
									} else
										logger.debug("system-property() argument is not a qualified name: " + arg);
								} else
									logger.debug("system-property() argument is not a string: " + arg);
							} else if (ns == null)
								logger.debug("Unbound namespace prefix: " + prf);
							else
								logger.debug("system-property() function is not in the '" + NS_XPROC + "' namespace: " + select);
						}
						if (defaultValue == null) {
							logger.debug("Select statement is not a valid system-property() function: " + select);
							defaultValue = fallbackDefaultValue;
						}
					} else if (typeIsString) {
						char quote = select.charAt(0);
						if (quote == '"' || quote == '\'') {
							if (select.charAt(select.length() - 1) == quote) {
								defaultValue = select.substring(1, select.length() - 1); // FIXME: unescape
								try {
									defaultValue = "" + convertValue(defaultValue);
								} catch (IllegalArgumentException e) {
									logger.debug(select + " can not be evaluated to a " + datatype.getId());
									defaultValue = fallbackDefaultValue;
								}
							} else {
								logger.debug("Select statement is not a valid string literal: " + select);
								defaultValue = fallbackDefaultValue;
							}
						} else {
							logger.debug("Select statement is not a string literal or a system-property() function: " + select);
							defaultValue = fallbackDefaultValue;
						}
					} else if (datatype == DatatypeService.XS_INTEGER ||
					           datatype == DatatypeService.XS_NON_NEGATIVE_INTEGER) {
						try {
							int i = Integer.parseInt(select);
							if (i < 0 && datatype == DatatypeService.XS_NON_NEGATIVE_INTEGER) {
								logger.debug("Select statement is not a valid " + datatype.getId() + ": " + select);
								defaultValue = "0";
							}
							defaultValue = "" + i;
						} catch (NumberFormatException e) {
							logger.debug("Select statement is not a valid " + datatype.getId() + ": " + select);
							defaultValue = "0";
						}
					} else if (datatype == DatatypeService.XS_BOOLEAN) {
						if ("true".equals(select) || "true()".equals(select) || "1".equals(select))
							defaultValue = "true";
						else if ("false".equals(select) || "false()".equals(select) || "0".equals(select))
							defaultValue = "false";
						else {
							logger.debug("Select statement is not a valid " + datatype.getId() + ": " + select);
							defaultValue = "false";
						}
					} else
						throw new RuntimeException("coding error");
					this.defaultValue = defaultValue;
					this.usesProperty = usesProperty;
				}
			}
		}

		@Override
		public String getName() {
			return name;
		}

		public QName getXProcOptionName() {
			return info.getName();
		}

		@Override
		public boolean isRequired() {
			return info.isRequired();
		}

		@Override
		public String getDefault() {
			return defaultValue;
		}

		@Override
		public String getNiceName() {
			return metadata.getNiceName();
		}

		@Override
		public String getDescription() {
			return metadata.getDescription();
		}

		@Override
		public DatatypeService getType() {
			return datatype;
		}

		@Override
		public String getMediaType() {
			return metadata.getMediaType();
		}

		@Override
		public boolean isPrimary() {
			return metadata.isPrimary();
		}

		@Override
		public boolean isSequence() {
			return isSequence;
		}

		@Override
		public boolean isOrdered() {
			return metadata.isOrdered();
		}

		@Override
		public Role getRole() {
			return metadata.getRole();
		}

		/**
		 * Convert a sequence of string values in order to pass it to
		 * {@link org.daisy.common.xproc.XProcInput.Builder#withOption()}.
		 */
		public Object convertValue(Iterable<String> value) {
			if (typeIsSequence)
				return Iterables.transform(value, this::convertValue);
			else if (typeIsString)
				return Joiner.on(metadata.getSeparator()).skipNulls().join(value);
			else {
				Iterator<String> i = value.iterator();
				if (i.hasNext()) {
					String v = i.next();
					if (i.hasNext())
						throw new IllegalArgumentException(
							"did not expect more than one value for option" + name + ": " + value);
					return convertValue(v);
				} else
					throw new IllegalArgumentException("did not expect empty value for option " + name);
			}
		}

		private Object convertValue(String value) {
			if (!(datatype.validate(value).isValid()))
				throw new IllegalArgumentException("not a valid " + datatype.getId()+ ": " + value);
			if (typeIsString)
				return value;
			else if (datatype == DatatypeService.XS_INTEGER ||
			         datatype == DatatypeService.XS_NON_NEGATIVE_INTEGER)
				try {
					int i = Integer.parseInt(value);
					if (i < 0 && datatype == DatatypeService.XS_NON_NEGATIVE_INTEGER)
						throw new IllegalArgumentException(
							"can not convert value to non-negative integer: " + value);
					return i;
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("can not convert value to integer: " + value, e);
				}
			else if (datatype == DatatypeService.XS_BOOLEAN)
				return "true".equals(value.toLowerCase()) || "1".equals(value);
			else
				throw new RuntimeException("coding error");
		}
	}

	private static class XProcScriptPort implements ScriptPort {

		private final XProcPortInfo info;
		private final XProcPortMetadata metadata;

		public XProcScriptPort(XProcPortInfo info,
		                       XProcPortMetadata metadata) {
			this.info = info;
			this.metadata = metadata;
		}

		@Override
		public String getName() {
			return info.getName();
		}

		@Override
		public boolean isPrimary() {
			return info.isPrimary();
		}

		@Override
		public boolean isSequence() {
			return info.isSequence();
		}

		@Override
		public boolean isRequired() {
			return info.isRequired();
		}

		@Override
		public String getNiceName() {
			return metadata.getNiceName();
		}

		@Override
		public String getDescription() {
			return metadata.getDescription();
		}

		@Override
		public String getMediaType() {
			return metadata.getMediaType();
		}
	}

	private void buildResultSet(XProcInput inputs, XProcOutput outputs,
	                            File resultDir, JobResultSet.Builder builder) throws IOException {

		// iterate over output ports
		for (ScriptPort port : getOutputPorts()) {
			String mediaType = port.getMediaType();

			// check if it is implemented as an output option
			XProcScriptOption option = getResultOption(port.getName());
			if (option != null) {
				if (inputs.getOptions().get(option.getXProcOptionName()) == null)
					// option was not set
					continue;
				if (XProcOptionMetadata.ANY_FILE_URI.equals(option.getType().getId())) {
					URI path; {
						Object val = inputs.getOptions().get(option.getXProcOptionName());
						try {
							path = URI.create((String)val);
						} catch (ClassCastException e) {
							throw new RuntimeException(
								"Expected string value for option " + option.getName()
								+ " but got: " + val.getClass());
						}
					}
					File f = new File(path);
					if (f.exists()) {
						builder = builder.addResult(port.getName(),
						                            resultDir.toURI().relativize(path).toString(),
						                            f,
						                            mediaType);
					}
				} else if (XProcOptionMetadata.ANY_DIR_URI.equals(option.getType().getId())) {
					String dir; {
						Object val = inputs.getOptions().get(option.getXProcOptionName());
						try {
							dir = (String)val;
						} catch (ClassCastException e) {
							throw new RuntimeException(
								"Expected string value for option " + option.getName()
								+ " but got: " + val.getClass());
						}
					}
					// scan the directory to get all files inside and write them to the XProcOutput
					for (File f : IOHelper.treeFileList(new File(URI.create(dir)))) {
						URI path = f.toURI();
						builder = builder.addResult(port.getName(),
						                            resultDir.toURI().relativize(path).toString(),
						                            f,
						                            mediaType);
					}
				}
			} else {
				Supplier<Result> resultProvider = outputs.getResultProvider(port.getName());
				if (resultProvider == null)
					// XProcDecorator makes sure this can not happen
					continue;
				if (!(resultProvider instanceof DynamicResultProvider))
					// XProcDecorator makes sure this can not happen
					throw new RuntimeException(
						"Result supplier is expected to be a DynamicResultProvider but got: " + resultProvider);
				for (Result result : ((DynamicResultProvider)resultProvider).providedResults()) {
					String sysId = result.getSystemId();
					if (sysId == null)
						// XProcDecorator makes sure this can not happen
						throw new RuntimeException(
							"Result is expected to be a DynamicResult but got: " + result);
					URI path = URI.create(sysId);
					builder = builder.addResult(port.getName(),
					                            resultDir.toURI().relativize(path).toString(),
					                            new File(path),
					                            mediaType);
				}
			}
		}
	}

	// for unit tests
	JobResultSet buildResultSet(XProcInput inputs, XProcOutput outputs, File resultDir) throws IOException {
		JobResultSet.Builder builder = new JobResultSet.Builder(this);
		buildResultSet(inputs, outputs, resultDir, builder);
		return builder.build();
	}

	/**
	 * Check the validation status from the status port and get it's value.
	 */
	// package private for unit tests
	boolean checkStatusPort(XProcOutput outputs) {
		Optional<ScriptPort> statusPort = getStatusPort();
		if (statusPort.isPresent()) {
			Supplier<Result> provider = outputs.getResultProvider(statusPort.get().getName());
			if (provider != null && provider instanceof StatusResultProvider) { // should always be true
				boolean ok = true;
				for (InputStream status : ((StatusResultProvider)provider).read()) {
					ok &= processStatus(status);
				}
				return ok;
			}
		}
		return true;
	}

	/**
	 * Read the XML file to check that validation status is equal to "ok".
	 */
	// package private for unit tests
	static boolean processStatus(InputStream status) {
		// check the contents of the xml and check if result is "ok"
		// <d:status xmlns:d="http://www.daisy.org/ns/pipeline/data" result="error"/>
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			docBuilderFactory.setNamespaceAware(true);
			javax.xml.parsers.DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(status);
			String result = doc.getDocumentElement().getAttribute("result");
			if (result == null || result.isEmpty()) {
				throw new RuntimeException("No result attribute was found in the status port");
			}
			return result.equalsIgnoreCase("ok");
		} catch (Exception e) {
			throw new RuntimeException("Error processing status file", e);
		}
	}
}
