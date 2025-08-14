package org.daisy.pipeline.script.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.daisy.common.xml.DocumentBuilder;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobResourcesDir;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScript.XProcScriptOption;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.XProcOptionMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class XProcDecorator {
	
	private static final Logger logger = LoggerFactory.getLogger(XProcDecorator.class);

	private final XProcScript script;
	private final File resultDir;
	private List<DocumentBuilder> inputParsers;

	/**
	 * Constructs a new instance.
	 *
	 * @param contextDir The contextDir for this instance.
	 */
	private XProcDecorator(XProcScript script, File resultDir, List<DocumentBuilder> inputParsers) {
		this.script = script;
		this.resultDir = resultDir;
		this.inputParsers = inputParsers;
	}

	public static XProcDecorator from(XProcScript script, File resultDir, List<DocumentBuilder> inputParsers) throws IOException {
		return new XProcDecorator(script, resultDir, inputParsers);
	}

	public XProcInput decorate(ScriptInput input) {
		logger.debug(String.format("Translating inputs for script :%s",script));
		XProcInput.Builder decorated = new XProcInput.Builder();
		try {
			// Store everything to disk just in case it hasn't been done before.
			// We need this for two reasons:
			// - to make sure documents on input ports have a non-empty system ID (they don't need to be stored on disk per se though)
			// - to make sure documents can be passed as a file path to XProc options
			input = input.storeToDisk();
			decorateInputPorts(script, input, decorated);
			decorateOptions(script, input, decorated);
		} catch(IOException e) {
			throw new RuntimeException("Error translating inputs", e);
		}
		return decorated.build();
	}

	/**
	 * Output port 'result' use cases:
	 *
	 * 1. Relative URI ./myoutput/file.xml is allowed and resolved to
	 *    ../data/../outputs/myoutput/file.xml (file-1.xml if more). In case there is no extension
	 *    (myoutput/file) the outputs will be named as myoutput/file-1
	 * 2. ./myscript/ will be resolved to ../data/../outputs/myscript/result.xml (result-1.xml if
	 *    more).
	 * 3. No output provided will resolve to ../data/../outputs/result/result.xml (if more documents
	 *    ../data/../outputs/result/result-1.xml)
	 */
	public XProcOutput decorate(XProcOutput output) {
		logger.debug(String.format("Translating outputs for script :%s",script));
		//just make sure that any generated output gets a proper  	
		//place to be stored, map those ports which an uri has been provided
		//and generate a uri for those without. 
		XProcOutput.Builder builder = new XProcOutput.Builder();
		for (ScriptPort port : script.getOutputPorts()) {
			if (script.getResultOption(port.getName()) == null) {
				/* Note that in practice output will always be empty because the {@link
				 * BoundXProcScript} API doesn't allow specifying outputs anymore.
				 */
				Supplier<Result> prov = output == null ? null : output.getResultProvider(port.getName());
				builder.withOutput(port.getName(),
				                   new DynamicResultProvider(prov,
				                                             port.getName(),
				                                             port.getMediaType(),
				                                             resultDir));
			}
		}
		Optional<ScriptPort> statusPort = script.getStatusPort();
		if (statusPort.isPresent()) {
			String name = statusPort.get().getName();
			builder.withOutput(name, new StatusResultProvider(name));
		}
		return builder.build();
	}
	/**
	 * Resolve input ports.
	 *
	 * @param script the script
	 * @param input the input
	 * @param builder the builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void decorateInputPorts(final XProcScript script, final ScriptInput input, XProcInput.Builder builder) throws IOException {
		for (ScriptPort port : script.getInputPorts()) {
			if (script.getInputOption(port.getName()) == null) {
				for (Source src : input.getInput(port.getName())) {
					InputSource is = SAXSource.sourceToInputSource(src);
					// make sure documents on input ports have a non-empty base URI
					if (src.getSystemId() == null
					    || "".equals(src.getSystemId())
					    || (is != null && (is.getByteStream() != null || is.getCharacterStream() != null)))
						throw new IllegalStateException(); // should not happen because ScripInput.storeToDisk() was called
					// make relative file paths absolute
					try {
						URI baseURI = URI.create(src.getSystemId());
						baseURI = resolveRelativePath(baseURI, input);
						src.setSystemId(baseURI.toString());
					} catch (Exception e) {
						throw new RuntimeException(
							"Error parsing URI when building the input port" + port.getName(), e);
					}
					String mediaType = port.getMediaType();
					if (mediaType != null && !(src instanceof DOMSource)) {
						mediaType = mediaType.trim();
						if (!mediaType.isEmpty()) {
							List<String> types = Lists.newArrayList(mediaType.split("\\s+"));
							if (!Iterables.all(types, t -> t.matches("[^ ]*(/|\\+)xml"))) {
								// input might be non-XML: transform to XML
								if (is == null)
									throw new IOException("Error reading input on port " + port.getName() + ": " + src.getClass());
								Document doc = null; {
									if (inputParsers != null)
										for (DocumentBuilder p : inputParsers)
											if (types.isEmpty())
												break;
											else if (types.removeIf(p::supportsContentType))
												try {
													doc = p.parse(is);
													break;
												} catch (SAXException|IllegalArgumentException e) {
													// ignore: if non of the parsers can handle the input, throw new error (see below)
												}
								}
								if (doc == null)
									throw new IOException(
										"Input on port " + port.getName() + " (media-type: " + mediaType + ") could not be parsed");
								Source domSrc = new DOMSource(doc);
								builder.withInput(port.getName(), () -> domSrc);
								continue;
							}
						}
					}
					builder.withInput(port.getName(), () -> src);
				}
			}
		}
	}
	
	/**
	 * Resolve options, input/output options without value will be automaticaly assigned.
	 */
	void decorateOptions(final XProcScript script, final ScriptInput input, XProcInput.Builder resolvedInput) throws IOException {
		decorateInputOptions(script, input, resolvedInput);
		decorateOutputOptions(script, input, resolvedInput);
	}

	void decorateInputOptions(XProcScript script, ScriptInput input, XProcInput.Builder resolvedInput) throws IOException {

		// options with type "anyFileURI"
		for (ScriptPort port : script.getInputPorts()) {
			XProcScriptOption option = script.getInputOption(port.getName());
			if (option != null) {
				List<String> value = new ArrayList<>();
				for (Source src : input.getInput(port.getName())) {
					InputSource is = SAXSource.sourceToInputSource(src);
					// make sure documents are stored on disk so we can pass a file path to the XProc option
					if (src.getSystemId() == null
					    || "".equals(src.getSystemId())
					    || (is != null && (is.getByteStream() != null || is.getCharacterStream() != null)))
						throw new IllegalStateException(); // should not happen because ScripInput.storeToDisk() was called
					// make relative file paths absolute
					try {
						URI baseURI = URI.create(src.getSystemId());
						baseURI = resolveRelativePath(baseURI, input);
						src.setSystemId(baseURI.toString());
					} catch (Exception e) {
						throw new RuntimeException(
							"Error parsing URI for option %s: %s" + option.getName(), e);
					}
					value.add(src.getSystemId());
				}
				resolvedInput.withOption(
					option.getXProcOptionName(),
					((XProcScriptOption)option).convertValue(value));
			}
		}

		// other options
		for (ScriptOption option : script.getOptions()) {
			Iterable<String> val = input.getOption(option.getName()); // may be empty collections but never null
			if (val.iterator().hasNext()) {
				String type = option.getType().getId();
				// check if type is "anyDirURI"
				if (XProcOptionMetadata.ANY_DIR_URI.equals(type)) {
					val = Iterables.transform(
						Iterables.filter(
							val,
							XProcDecorator::notEmpty),
						v -> {
							try {
								URI u = URI.create(v);
								u = resolveRelativePath(u, input); // ScriptInput does not check "anyDirURI" options,
								                                   // so this will fail if it is a relative path but
								                                   // no context ZIP was provided!
								return u.toString();
							} catch (IllegalArgumentException e) {
								throw new RuntimeException(
									String.format("Error parsing URI for option %s: %s", option.getName(), v));
							}
						}
					);
				}
				resolvedInput.withOption(
					((XProcScriptOption)option).getXProcOptionName(),
					((XProcScriptOption)option).convertValue(val));
			}
		}
	}

	void decorateOutputOptions(XProcScript script, ScriptInput input, XProcInput.Builder resolvedInput) {

		// temp options
		for (XProcScriptOption option : script.getTempOptions()) {
			resolvedInput.withOption(option.getXProcOptionName(),
			                         decorateOutputOption(option).toString());
		}

		// result options
		for (ScriptPort port : script.getOutputPorts()) {
			XProcScriptOption option = script.getResultOption(port.getName());
			if (option != null) {
				resolvedInput.withOption(option.getXProcOptionName(),
				                         decorateOutputOption(option).toString());
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

	private final HashSet<String> generatedOutputs = Sets.newHashSet();

	private URI decorateOutputOption(XProcScriptOption option) {
		// get default
		String uri = option.getDefault();
		// otherwise generate URI
		if (!notEmpty(uri)) {
			if (XProcOptionMetadata.ANY_DIR_URI.equals(option.getType().getId())) {
				uri = option.getName() + "/";
			} else {
				uri = option.getName() + ScriptPort.getFileExtension(option.getMediaType());
			}
			if (generatedOutputs.contains(uri)) {
				// should not happen because option names are unique
				throw new IllegalArgumentException(
					String.format("Conflict when generating URIs a default value and option name have are equal: %s", uri));
			}
			generatedOutputs.add(uri);
		}
		try {
			return resultDir.toURI().resolve(URI.create(uri));
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("Error parsing URI for option %s: %s", option.getName(), uri), e);
		}
	}

	// package private for unit tests
	static final boolean notEmpty(String value){
		return value != null
			&& !value.isEmpty()
			&& ! value.equals("''")
			&& !value.equals("\"\"");
	}
}
