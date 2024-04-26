package org.daisy.pipeline.job.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScript.XProcScriptOption;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.XProcOptionMetadata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;

import com.google.common.base.Supplier;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;

public class XProcDecorator {
	
	private static final Logger logger = LoggerFactory.getLogger(XProcDecorator.class);

	private final XProcScript script;
	private final URIMapper mapper;

	/**
	 * Constructs a new instance.
	 *
	 * @param contextDir The contextDir for this instance.
	 */
	private XProcDecorator(URIMapper mapper, XProcScript script) {
		this.script=script;
		this.mapper=mapper;
	}

	public static XProcDecorator from(XProcScript script, URIMapper mapper) throws IOException {
		return new XProcDecorator(mapper,script);
	}

	public XProcInput decorate(ScriptInput input) {
		logger.debug(String.format("Translating inputs for script :%s",script));
		XProcInput.Builder decorated = new XProcInput.Builder();
		try{
			decorateInputPorts(script, input, decorated);
			decorateOptions(script, input, decorated);
		}catch(IOException ex){
			throw new RuntimeException("Error translating inputs",ex);
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
				                                             mapper));
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
				// number of inputs for this port
				int inputCnt = 0;
				for (Source src : input.getInput(port.getName())) {
					URI relUri = null; {
						InputSource is = SAXSource.sourceToInputSource(src);
						if (is != null && (is.getByteStream() != null || is.getCharacterStream() != null))
							// this is the case when no zip context was provided (all comes from the xml)
							relUri = URI.create(port.getName() + '-' + inputCnt + ".xml");
						else {
							try {
								relUri = URI.create(src.getSystemId());
							} catch (Exception e) {
								throw new RuntimeException(
									"Error parsing uri when building the input port"
									+ port.getName(), e);
							}
						}
					}
					URI uri = mapper.mapInput(relUri);
					src.setSystemId(uri.toString());
					builder.withInput(port.getName(), () -> src);
					inputCnt++;
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
				for (Source source : input.getInput(port.getName())) {
					InputSource is = SAXSource.sourceToInputSource(source);
					if (is != null && (is.getByteStream() != null || is.getCharacterStream() != null)) {
						// document is not stored on disk so we can't pass a file path to the XProc option
						// store it to a temporary location
						String sysId = is.getSystemId();
						// give the file a name that resembles the original name
						File f = File.createTempFile("input", sysId != null ? new File(sysId).getName() : null);
						f.deleteOnExit();
						InputStream stream = is.getByteStream();
						if (stream == null)  {
							Reader reader = is.getCharacterStream();
							String encoding = is.getEncoding();
							if (encoding == null)
								encoding = "UTF-8";
							stream = new ByteArrayInputStream(CharStreams.toString(reader).getBytes(encoding));
						}
						IOHelper.dump(is.getByteStream(), new FileOutputStream(f));
						value.add(f.toURI().toString());
					} else {
						String sysId = source.getSystemId();
						try {
							value.add(mapper.mapInput(URI.create(sysId)).toString());
						} catch (IllegalArgumentException e) {
							throw new RuntimeException(
								String.format("Error parsing URI for option %s: %s", option.getName(), sysId));
						}
					}
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
								return mapper.mapInput(URI.create(v)).toString();
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

	private final HashSet<String> generatedOutputs = Sets.newHashSet();

	private URI decorateOutputOption(XProcScriptOption option) {
		// get default
		String uri = option.getDefault();
		// otherwise generate URI
		if (!notEmpty(uri)) {
			if (XProcOptionMetadata.ANY_DIR_URI.equals(option.getType().getId())) {
				uri = option.getName() + "/";
			} else {
				uri = option.getName() + ".xml";
			}
			if (generatedOutputs.contains(uri)) {
				// should not happen because option names are unique
				throw new IllegalArgumentException(
					String.format("Conflict when generating URIs a default value and option name have are equal: %s", uri));
			}
			generatedOutputs.add(uri);
		}
		try {
			return mapper.mapOutput(URI.create(uri));
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
