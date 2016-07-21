package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.tasks.TaskSystemFactoryException;
import org.daisy.dotify.api.tasks.TaskSystemFactoryMakerService;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.daisy.dotify.tasks.runner.TaskRunner;

import org.daisy.pipeline.braille.common.Query.Feature;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.util.Files.asFile;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

public class FileToOBFLStep extends DefaultStep {
	private static final QName _source = new QName("source");
	
	private static final QName _locale = new QName("locale");
	private static final QName _format = new QName("format");
	private static final QName _dotifyOptions = new QName("dotify-options");
	
	private static final QName _template = new QName("template");
    private static final QName _rows = new QName("rows");
    private static final QName _cols = new QName("cols");
    private static final QName _innerMargin = new QName("inner-margin");
    private static final QName _outerMargin = new QName("outer-margin");
    private static final QName _rowgap = new QName("rowgap");
    private static final QName _splitterMax = new QName("splitterMax");
    private static final QName _identifier = new QName("identifier");
	
	private WritablePipe result = null;
	private final Map<String,String> parameters = new HashMap<String,String>();
	
	private final TaskSystemFactoryMakerService taskSystemFactoryService;
	private final TaskGroupFactoryMakerService taskGroupFactoryService;
	
	public FileToOBFLStep(XProcRuntime runtime,
	                      XAtomicStep step,
	                      TaskSystemFactoryMakerService taskSystemFactoryService,
	                      TaskGroupFactoryMakerService taskGroupFactoryService) {
		super(runtime, step);
		this.taskSystemFactoryService = taskSystemFactoryService;
		this.taskGroupFactoryService = taskGroupFactoryService;
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		throw new XProcException("No input document allowed on port '" + port + "'");
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}
	
	@Override
	public void setParameter(String port, QName name, RuntimeValue value) {
		if ("parameters".equals(port))
			setParameter(name, value);
		else
			throw new XProcException("No parameters allowed on port '" + port + "'");
	}
	
	@Override
	public void setParameter(QName name, RuntimeValue value) {
		if ("".equals(name.getNamespaceURI()))
			parameters.put(name.getLocalName(), value.getString());
	}
	
	@Override
	public void reset() {
		result.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			File inputFile = asFile(getOption(_source).getString());
			Map<String, Object> params = new HashMap<String, Object>();
			addOption(_template, params);
			addOption(_rows, params);
			addOption(_cols, params);
			addOption(_innerMargin, params);
			addOption(_outerMargin, params);
			addOption(_rowgap, params);
			addOption(_splitterMax, params);
			addOption(_identifier, params);
			
			RuntimeValue rv = getOption(_dotifyOptions);
			if (rv!=null) {
				for (Feature f : query(rv.getString())) {
					String key = f.getKey();
					Optional<String> val = f.getValue();
					//if there isn't a value, just repeat the key
					params.put(key, val.or(key));
				}
			}
			
			params.putAll(parameters);
			String locale = getOption(_locale, Locale.getDefault().toString());
			InputStream resultStream = convert(
					newTaskSystem(locale, getOption(_format, "obfl")),
					inputFile, locale, params);
			
			// Write result
			result.write(runtime.getProcessor().newDocumentBuilder().build(new StreamSource(resultStream)));
			resultStream.close();
		} catch (Exception e) {
			logger.error("dotify:file-to-obfl failed", e);
			throw new XProcException(step.getNode(), e);
		}
	}
	
	private void addOption(QName opt, Map<String, Object> params) {
		RuntimeValue o = getOption(opt);
		if (o!=null) {
			params.put(opt.getLocalName(), o.getString());
		}
	}
		
	private InputStream convert(TaskSystem system, File src, String locale, Map<String, Object> params) throws TaskSystemFactoryException, TaskSystemException, IOException {
		
		// FIXME: see https://github.com/joeha480/dotify/issues/205
		String inputFormat; {
			inputFormat = "";
			String inp = src.getName();
			int inx = inp.lastIndexOf('.');
			if (inx > -1) {
				inputFormat = inp.substring(inx + 1);
				if (!taskGroupFactoryService.listSupportedSpecifications().contains(new TaskGroupSpecification(inputFormat, "obfl", locale))) {
					logger.debug("No input factory for " + inputFormat);
					// attempt to detect a supported type
					try {
						if (XMLTools.isWellformedXML(src)) {
							inputFormat = "xml";
							logger.info("Input is well-formed xml."); }}
					catch (XMLToolsException e) {
						e.printStackTrace(); }}
				else
					logger.info("Found an input factory for " + inputFormat); }}
		params.put("inputFormat", inputFormat);
		params.put("input", src.getAbsolutePath());
		List<InternalTask> tasks = system.compile(params);
		
		// Create a destination file
		File dest = File.createTempFile("file-to-obfl", ".tmp");
		dest.deleteOnExit();
		
		// Run tasks
		TaskRunner runner = TaskRunner.withName("dotify:file-to-obfl").build();
		runner.runTasks(src, dest, tasks);
		
		// Return stream
		return new FileInputStream(dest);
	}

	private TaskSystem newTaskSystem(String locale, String format) throws TaskSystemFactoryException {
		return taskSystemFactoryService.newTaskSystem(locale, format);
	}
	
	@Component(
		name = "dotify:file-to-obfl",
		service = { XProcStepProvider.class },
		property = { "type:String={http://code.google.com/p/dotify/}file-to-obfl" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new FileToOBFLStep(runtime, step, taskSystemFactoryService, taskGroupFactoryService);
		}
		
		private TaskSystemFactoryMakerService taskSystemFactoryService = null;
		private TaskGroupFactoryMakerService taskGroupFactoryService = null;
		
		@Reference(
			name = "TaskSystemFactoryMakerService",
			service = TaskSystemFactoryMakerService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindTaskSystemFactoryMakerService(TaskSystemFactoryMakerService service) {
			taskSystemFactoryService = service;
		}
		
		@Reference(
			name = "TaskGroupFactoryMakerService",
			service = TaskGroupFactoryMakerService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindTaskGroupFactoryMakerService(TaskGroupFactoryMakerService service) {
			taskGroupFactoryService = service;
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(FileToOBFLStep.class);
	
}
