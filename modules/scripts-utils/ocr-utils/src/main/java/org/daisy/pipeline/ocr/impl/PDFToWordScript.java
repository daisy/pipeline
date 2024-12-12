package org.daisy.pipeline.ocr.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.common.file.Resource;
import org.daisy.common.file.URLs;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.messaging.Message.Level;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.ValidationResult;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.ocr.OCRProcessor;
import org.daisy.pipeline.ocr.OCRService;
import org.daisy.pipeline.pandoc.Pandoc;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class PDFToWordScript implements ScriptService<Script> {

	private final static String MIMETYPE_PDF = "application/pdf";
	private final static String MIMETYPE_XHTML = "application/xhtml+xml";
	private final static String MIMETYPE_DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	private final static Logger logger = LoggerFactory.getLogger(PDFToWordScript.class);

	private final OCRService ocrService;
	private final String version;
	private final OCRProcessorDatatype processorDatatype;
	private final Pandoc pandoc;

	PDFToWordScript(OCRService service, Collection<OCRProcessor> processors, DatatypeRegistry datatypeRegistry, Pandoc pandoc) {
		ocrService = service;
		Properties mavenProps = new Properties();
		try (InputStream is = URLs.getResourceFromJAR("/maven.properties", PDFToWordScript.class)
		                          .openStream()) {
			mavenProps.load(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		version = mavenProps.getProperty("version");
		// the datatype is static: we assume that the list of available processors does not change when the properties
		// change (only the processor implementations may change)
		processorDatatype = new OCRProcessorDatatype(service, processors);
		datatypeRegistry.register(processorDatatype);
		this.pandoc= pandoc;
	}

	@Override
	public String getId() {
		return "pdf-to-word-" + ocrService.getName();
	}

	@Override
	public String getVersion() {
		return version;
	}

	private Script script = null;

	@Override
	public Script load() {
		if (script == null) {
			Script.Builder b = new Builder()
				.withInputFileset("pdf")
				.withOutputFileset("docx")
				.withShortName("PDF to Word using " + ocrService.getDisplayName() + " (experimental)")
				.withDescription(
					"Transforms a PDF into a Microsoft Office Word (.docx) document, powered by "
					+ ocrService.getDisplayName() + ".\n\n"
					+ "This script is an early development phase and has not been tested with a wide range of input documents yet.")
				.withInputPort("source",
					new ScriptPort() {
						@Override public String getName() { return "source"; }
						@Override public String getNiceName() { return "Input PDF"; }
						@Override public String getDescription() { return "The PDF you want to convert."; }
						@Override public String getMediaType() { return MIMETYPE_PDF; }
						@Override public boolean isRequired() { return true; }
						@Override public boolean isPrimary() { return true; }
						@Override public boolean isSequence() { return false; }})
				.withOutputPort("result",
					new ScriptPort() {
						@Override public String getName() { return "result"; }
						@Override public String getNiceName() { return "Output DOCX"; }
						@Override public String getDescription() { return "The resulting Microsoft Office Word file."; }
						@Override public String getMediaType() { return MIMETYPE_PDF; }
						@Override public boolean isRequired() { return true; }
						@Override public boolean isPrimary() { return true; }
						@Override public boolean isSequence() { return false; }})
				// for now we use the term "model" rather than the more generic "processor", because all
				// implementation are AI-based
				.withOption("model",
					new ScriptOption() {
						@Override public String getName() { return "model"; }
						@Override public String getNiceName() { return "Model version"; }
						@Override public String getDescription() {
							return "The " + ocrService.getDisplayName() + " model to be used."; }
						@Override public boolean isRequired() { return false; }
						@Override public String getDefault() { return processorDatatype.getDefaultValue(); }
						@Override public DatatypeService getType() { return processorDatatype; }
						@Override public String getMediaType() { return null; }
						@Override public boolean isSequence() { return false; }
						@Override public boolean isOrdered() { return false; }
						@Override public Role getRole() { return null; }
						@Override public boolean isPrimary() { return false; }})
				.withOption("include-html",
					new ScriptOption() {
						@Override public String getName() { return "include-html"; }
						@Override public String getNiceName() { return "Include HTML"; }
						@Override public String getDescription() {
							return "Whether or not to keep the intermediary HTML file set (for debugging)."; }
						@Override public boolean isRequired() { return false; }
						@Override public String getDefault() { return "false"; }
						@Override public DatatypeService getType() { return DatatypeService.XS_BOOLEAN; }
						@Override public String getMediaType() { return null; }
						@Override public boolean isSequence() { return false; }
						@Override public boolean isOrdered() { return false; }
						@Override public Role getRole() { return null; }
						@Override public boolean isPrimary() { return false; }})
				.withOutputPort("html",
					new ScriptPort() {
						@Override public String getName() { return "html"; }
						@Override public String getNiceName() { return "HTML"; }
						@Override public String getDescription() { return "The intermediary HTML file set."; }
						@Override public String getMediaType() { return MIMETYPE_XHTML; }
						@Override public boolean isRequired() { return false; }
						@Override public boolean isPrimary() { return false; }
						@Override public boolean isSequence() { return true; }});
			for (ScriptOption o : ocrService.getOptions())
				b.withOption(o.getName(), o);
			script = b.build();
		}
		return script;
	}

	private class Builder extends Script.Builder {

		Builder() {
			super(PDFToWordScript.this);
		}

		@Override
		public Script build() {
			return new ScriptImpl(shortName, description, homepage, inputPorts, outputPorts,
			                      options, inputFilesets, outputFilesets);
		}

		private class ScriptImpl extends Script {

			private ScriptImpl(String name, String description, String homepage,
			                   Map<String,ScriptPort> inputPorts, Map<String,ScriptPort> outputPorts,
			                   Map<String,ScriptOption> options, List<String> inputFilesets,
			                   List<String> outputFilesets) {
				super(id, version, name, description, homepage, inputPorts, outputPorts, options, inputFilesets, outputFilesets);
			}

			@Override
			public Status run(ScriptInput input, Map<String,String> properties,
			                  MessageAppender messages, JobResultSet.Builder resultBuilder,
			                  File resultDir) throws IOException {
				String modelName = processorDatatype.getDefaultValue(); {
					Iterator<String> val = input.getOption("model").iterator(); // iterable can not be null
					if (val.hasNext())
						// must be valid, and must be only value
						modelName = val.next(); }
				// get up to date processor from current properties
				OCRProcessor processor = null; {
					try {
						for (OCRProcessor p : ocrService.getAvailableProcessors(properties))
							if (modelName.equals(p.getName())) {
								processor = p;
								break;
							}
					} catch (OCRService.ServiceDisabledException e) {
						return error(messages, "No models available");
					}
					if (processor == null)
						return error(messages, "Model " + modelName + " is not available");
				}
				Resource pdf; {
					// iterable can not be null and must have single value
					URI uri = input.getInput("source").iterator().next();
					if (uri.isAbsolute()) { // absolute means URI has scheme component
						if (!"file".equals(uri.getScheme()) || uri.isOpaque())
							throw new IllegalStateException(); // should not happen: ScripInput does not allow this
						// URI is a file URI with an absolute file path
						pdf = Resource.load(uri, MIMETYPE_PDF);
					} else
						// URI is a relative path, and JobResources must not be null
						pdf = input.getResources().getResource(uri);
				}
				Map<String,Iterable<String>> options = new HashMap<>(); {
					for (ScriptOption o : ocrService.getOptions())
						options.put(o.getName(), input.getOption(o.getName())); }
				File htmlDir = new File(resultDir, "html");
				htmlDir.mkdirs();
				Resource html = null; {
					try (MessageAppender stepMessages = messages != null
							? messages.append(new MessageBuilder().withProgress(new BigDecimal(.8))
							                                      .withText("Converting from PDF to HTML using "
							                                                + processor.getDisplayName())
							                                      .withLevel(Level.INFO))
							: null) {
						Collection<Resource> htmlAndImages = processor.run(pdf, options, stepMessages, htmlDir);
						// store html and images to disk
						boolean includeHTML = false; {
							Iterator<String> val = input.getOption("include-html").iterator(); // iterable can not be null
							if (val.hasNext())
								// must be valid, and must be only value
								includeHTML = val.next().toLowerCase().matches("true|1"); }
						for (Resource r : htmlAndImages) {
							r = r.store();
							if (MIMETYPE_XHTML.equals(r.getMediaType().get()))
								if (html != null)
									return error(stepMessages, "Expected a single HTML file");
								else
									html = r;
							if (includeHTML)
								resultBuilder.addResult("html",
								                        r.getPath(resultDir.toURI()),
								                        r.readAsFile(),
								                        r.getMediaType().get()); }
						if (html == null)
							return error(stepMessages, "Expected a single HTML file");
					}
				}
				try (MessageAppender stepMessages = messages != null
						? messages.append(new MessageBuilder().withProgress(new BigDecimal(.2))
						                                      .withText("Converting from HTML to DOCX")
						                                      .withLevel(Level.INFO))
						: null) {
					File docx; {
						String fileName = pdf.getPath().toString();
						if (fileName.contains("/"))
							fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
						if (fileName.contains("."))
							fileName = fileName.substring(0, fileName.indexOf("."));
						fileName += ".docx";
						URI path = URI.create("result/" + fileName);
						docx = new File(URLs.resolve(resultDir.toURI(), path));
						docx.getParentFile().mkdirs();
					}
					if (
						pandoc
							.newCommand()
							.withInputFormat(Pandoc.Format.HTML)
							.withInput(html.readAsFile())
							.withOutputFormat(Pandoc.Format.DOCX)
							.withOutput(docx)
							.runner()
							.cd(htmlDir)
							.run()
						!= 0
					)
						return error(stepMessages, "Pandoc process failed. (Please see detailed log for more info.)");

					Resource r = Resource.load(docx, MIMETYPE_DOCX);
					resultBuilder.addResult("result",
					                        r.getPath(resultDir.toURI()),
					                        r.readAsFile(),
					                        r.getMediaType().get());
				} catch (Throwable e) {
					logger.debug("Unexpected error happened during Pandoc invocation", e);
					return error(messages, "Unexpected error happened during Pandoc invocation."
					                       + " (Please see detailed log for more info.)");
				}
				return Status.SUCCESS;
			}
		}
	}

	private static Status error(MessageAppender messageAppender, String message) {
		if (messageAppender != null)
			messageAppender.append(new MessageBuilder().withLevel(Level.ERROR).withText(message)).close();
		return Status.ERROR;
	}

	private static class OCRProcessorDatatype extends DatatypeService {

		private final List<String> values;
		private final Document xmlDefinition;

		OCRProcessorDatatype(OCRService service, Collection<OCRProcessor> processors) {
			super(service.getName() + "-processor");
			values = new ArrayList<>();
			for (OCRProcessor p : processors)
				values.add(p.getName());
			if (values.isEmpty())
				throw new IllegalArgumentException();
			try {
				xmlDefinition = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				                                      .getDOMImplementation().createDocument(null, "choice", null);
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
			Element choice = xmlDefinition.getDocumentElement();
			for (OCRProcessor p : processors) {
				choice.appendChild(xmlDefinition.createElement("value"))
				      .appendChild(xmlDefinition.createTextNode(p.getName()));
				choice.appendChild(xmlDefinition.createElementNS(
				                       "http://relaxng.org/ns/compatibility/annotations/1.0", "documentation"))
				      .appendChild(xmlDefinition.createTextNode(p.getDisplayName()));
			}
		}

		String getDefaultValue() {
			return values.get(0);
		}

		@Override
		public Document asDocument() {
			return xmlDefinition;
		}

		@Override
		public ValidationResult validate(String content) {
			if (values.contains(content))
				return ValidationResult.valid();
			else
				return ValidationResult.notValid("'" + content + "' is not in the list of allowed values.");
		}
	}
}
