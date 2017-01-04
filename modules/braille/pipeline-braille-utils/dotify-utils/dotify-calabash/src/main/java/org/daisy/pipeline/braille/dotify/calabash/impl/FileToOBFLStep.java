package org.daisy.pipeline.braille.dotify.calabash.impl;

import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.util.Files.asFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.stream.StreamSource;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.dotify.api.identity.IdentityProviderService;
import org.daisy.dotify.api.tasks.AnnotatedFile;
import org.daisy.dotify.api.tasks.DefaultAnnotatedFile;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupInformation;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.tasks.TaskSystemFactoryException;
import org.daisy.dotify.api.tasks.TaskSystemFactoryMakerService;
import org.daisy.dotify.common.xml.XMLTools;
import org.daisy.dotify.common.xml.XMLToolsException;
import org.daisy.dotify.tasks.runner.TaskRunner;
import org.daisy.pipeline.braille.common.Query.Feature;
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
    
    private static final Logger logger = LoggerFactory.getLogger(FileToOBFLStep.class);
	
	private WritablePipe result = null;
	private final Map<String,String> parameters = new HashMap<>();
	
	private final TaskSystemFactoryMakerService taskSystemFactoryService;
	private final TaskGroupFactoryMakerService taskGroupFactoryService;
	private final IdentityProviderService identityService;

	public FileToOBFLStep(XProcRuntime runtime,
	                      XAtomicStep step,
	                      TaskSystemFactoryMakerService taskSystemFactoryService,
	                      TaskGroupFactoryMakerService taskGroupFactoryService,
	                      IdentityProviderService identityService) {
		super(runtime, step);
		this.taskSystemFactoryService = taskSystemFactoryService;
		this.taskGroupFactoryService = taskGroupFactoryService;
		this.identityService = identityService;
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
			String outputFormat = getOption(_format, "obfl");
			InputStream resultStream = convert(inputFile, outputFormat, locale, params);
			
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
		
	private InputStream convert(File input, String outputFormat, String locale, Map<String, Object> params) throws TaskSystemFactoryException, TaskSystemException, IOException {
		
		// FIXME: see https://github.com/joeha480/dotify/issues/205
		AnnotatedFile ai = identityService.identify(input);

		String inputFormat = getFormatString(ai);
		if (!supportsInputFormat(inputFormat, taskGroupFactoryService.listAll())) {
			logger.debug("No input factory for " + inputFormat);
			logger.info("Note, the following detection code has been deprected. In future versions, an exception will be thrown if this point is reached."
						+ " To avoid this, use the IdentifierFactory interface to implement a detector for the file type.");
			// attempt to detect a supported type
			try {
				if (XMLTools.isWellformedXML(ai.getFile())) {
					ai = DefaultAnnotatedFile.with(ai).extension("xml").build();
					inputFormat = ai.getExtension();
					logger.info("Input is well-formed xml."); }}
			catch (XMLToolsException e) {
				logger.info("File is not well-formed xml: " + ai.getFile(), e);
			}
		} else {
			logger.info("Found an input factory for " + inputFormat);
		}

		params.put("inputFormat", inputFormat);
		params.put("input", ai.getFile().getAbsolutePath());
		TaskSystem system = newTaskSystem(inputFormat, outputFormat, locale);
		List<InternalTask> tasks = system.compile(params);
		
		// Create a destination file
		File dest = File.createTempFile("file-to-obfl", ".tmp");
		dest.deleteOnExit();
		
		// Run tasks
		TaskRunner runner = TaskRunner.withName("dotify:file-to-obfl").build();
		runner.runTasks(ai, dest, tasks);
		
		// Return stream
		return new FileInputStream(dest);
	}
	
	private static boolean supportsInputFormat(String inputFormat, Set<TaskGroupInformation> specs) {
		for (TaskGroupInformation s : specs) {
			if (s.getInputFormat().equals(inputFormat)) {
				return true;
			}
		}
		return false;
	}
	
	private static String getFormatString(AnnotatedFile f) {
		// FIXME: see https://github.com/joeha480/dotify/issues/205

		if (f.getFormatName()!=null) {
			return f.getFormatName();
		} else if (f.getExtension()!=null) {
			return f.getExtension();
		} else if (f.getMediaType()!=null) {
			return f.getMediaType();
		} else {
			return null;
		}
	}

	private TaskSystem newTaskSystem(String inputFormat, String outputFormat, String locale) throws TaskSystemFactoryException {
		return taskSystemFactoryService.newTaskSystem(inputFormat, outputFormat, locale);
	}
	
	@Component(
		name = "dotify:file-to-obfl",
		service = { XProcStepProvider.class },
		property = { "type:String={http://code.google.com/p/dotify/}file-to-obfl" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new FileToOBFLStep(runtime, step, taskSystemFactoryService, taskGroupFactoryService, identityService);
		}
		
		private TaskSystemFactoryMakerService taskSystemFactoryService = null;
		private TaskGroupFactoryMakerService taskGroupFactoryService = null;
		private IdentityProviderService identityService = null;
		
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
		
		@Reference(
			name = "IdentityProviderService",
			service = IdentityProviderService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindIdentityProviderService(IdentityProviderService service) {
			identityService = service;
		}
	}
	
}
