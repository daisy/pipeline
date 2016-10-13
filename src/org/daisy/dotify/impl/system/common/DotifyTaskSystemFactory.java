package org.daisy.dotify.impl.system.common;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemFactory;
import org.daisy.dotify.api.tasks.TaskSystemFactoryException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.dotify.impl.input.SPIHelper;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * Provides a default task system factory for PEF, OBFL and text output.
 * 
 * @author Joel Håkansson
 */
@Component
public class DotifyTaskSystemFactory implements TaskSystemFactory {
	private TaskGroupFactoryMakerService imf;
	private PagedMediaWriterFactoryMakerService pmw;
	private FormatterEngineFactoryService fe;

	@Override
	public boolean supportsSpecification(String locale, String outputFormat) {
		for (TaskGroupSpecification spec : imf.listSupportedSpecifications()) {
			if (spec.getOutputFormat().equals(outputFormat) && locale.equalsIgnoreCase(spec.getLocale())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TaskSystem newTaskSystem(String locale, String outputFormat) throws TaskSystemFactoryException {
		if (supportsSpecification(locale, outputFormat)) {
			return new DotifyTaskSystem("Dotify Task System", outputFormat, locale, imf, pmw, fe);
		}
		throw new TaskSystemFactoryException("Unsupported specification: " + locale + "/" + outputFormat);
	}

	@Reference
	public void setInputManagerFactory(TaskGroupFactoryMakerService service) {
		this.imf = service;
	}

	public void unsetInputManagerFactory(TaskGroupFactoryMakerService service) {
		this.imf = null;
	}

	@Reference
	public void setPagedMediaWriterFactory(PagedMediaWriterFactoryMakerService service) {
		this.pmw = service;
	}

	public void unsetPagedMediaWriterFactory(PagedMediaWriterFactoryMakerService service) {
		this.pmw = null;
	}
	
	@Reference
	public void setFormatterEngineFactory(FormatterEngineFactoryService service) {
		this.fe = service;
	}
	
	public void unsetFormatterEngineFactory(FormatterEngineFactoryService service) {
		this.fe = null;
	}

	@Override
	public void setCreatedWithSPI() {
		if (imf == null) {
			imf = SPIHelper.getInputManagerFactoryMakerService();
		}
		if (pmw == null) {
			pmw = SPIHelper.getPagedMediaWriterFactoryMakerService();
		}
		if (fe == null) {
			fe = SPIHelper.getFormatterEngineFactoryService();
		}
	}

}
