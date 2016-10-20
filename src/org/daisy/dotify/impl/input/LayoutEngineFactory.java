package org.daisy.dotify.impl.input;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class LayoutEngineFactory implements TaskGroupFactory {
	private final Set<TaskGroupSpecification> supportedSpecifications;
	private PagedMediaWriterFactoryMakerService pmw;
	private FormatterEngineFactoryService fe;

	public LayoutEngineFactory() {
		supportedSpecifications = new HashSet<>();
		String locale = "sv-SE";
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.PEF_FORMAT, locale));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, locale));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, "en-US"));
	}

	@Override
	public boolean supportsSpecification(TaskGroupSpecification spec) {
		return supportedSpecifications.contains(spec);
	}
	
	@Override
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(supportedSpecifications);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new LayoutEngine(spec, pmw, fe);
	}

	public void setCreatedWithSPI() {
		if (pmw == null) {
			pmw = SPIHelper.getPagedMediaWriterFactoryMakerService();
		}
		if (fe == null) {
			fe = SPIHelper.getFormatterEngineFactoryService();
		}
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
	
}
