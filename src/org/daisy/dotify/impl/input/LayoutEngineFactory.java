package org.daisy.dotify.impl.input;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.tasks.TaskGroupInformation;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactory;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component
public class LayoutEngineFactory implements TaskGroupFactory {
	private static final String LOCALE = "sv-SE";
	private final Set<TaskGroupSpecification> supportedSpecifications;
	private final Set<TaskGroupInformation> information;
	private PagedMediaWriterFactoryMakerService pmw;
	private FormatterEngineFactoryService fe;

	public LayoutEngineFactory() {
		supportedSpecifications = new HashSet<>();
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.PEF_FORMAT, LOCALE));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, LOCALE));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, "en-US"));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, "no-NO"));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, "de"));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, "de-DE"));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, "da"));
		supportedSpecifications.add(new TaskGroupSpecification("obfl", Keys.TEXT_FORMAT, "da-DK"));
		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder("obfl", Keys.PEF_FORMAT).locale(LOCALE).build());
		tmp.add(TaskGroupInformation.newConvertBuilder("obfl", Keys.TEXT_FORMAT).build());
		information = Collections.unmodifiableSet(tmp);
	}

	@Override
	public boolean supportsSpecification(TaskGroupSpecification spec) {
		//TODO: move this to default implementation after move to java 8
		for (TaskGroupInformation i : listAll()) {
			if (spec.matches(i)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	@Deprecated
	public Set<TaskGroupSpecification> listSupportedSpecifications() {
		return Collections.unmodifiableSet(supportedSpecifications);
	}

	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new LayoutEngine(spec, pmw, fe);
	}

	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
	}

	@Override
	public Set<TaskGroupInformation> list(String locale) {
		//TODO: move this to default implementation after move to java 8 (and use streams)
		Objects.requireNonNull(locale);
		Set<TaskGroupInformation> ret = new HashSet<>();
		for (TaskGroupInformation info : listAll()) {
			if (info.matchesLocale(locale)) {
				ret.add(info.newCopyBuilder().locale(locale).build());
			}
		}
		return ret;
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
