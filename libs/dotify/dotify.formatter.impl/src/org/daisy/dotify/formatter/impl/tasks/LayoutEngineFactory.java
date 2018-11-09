package org.daisy.dotify.formatter.impl.tasks;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.engine.FormatterEngineMaker;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMaker;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.streamline.api.media.FormatIdentifier;
import org.daisy.streamline.api.tasks.TaskGroup;
import org.daisy.streamline.api.tasks.TaskGroupFactory;
import org.daisy.streamline.api.tasks.TaskGroupInformation;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.daisy.streamline.api.validity.ValidatorFactoryMaker;
import org.daisy.streamline.api.validity.ValidatorFactoryMakerService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

/**
 * Provides a task group factory for running the Dotify formatter.
 *  
 * @author Joel Håkansson
 */
@Component
public class LayoutEngineFactory implements TaskGroupFactory {
	private static final String LOCALE = "sv-SE";
	private final Set<TaskGroupSpecification> supportedSpecifications;
	private final Set<TaskGroupInformation> information;
	private PagedMediaWriterFactoryMakerService pmw;
	private FormatterEngineFactoryService fe;
	private ValidatorFactoryMakerService vf;

	/**
	 * Creates a new layout engine factory.
	 */
	public LayoutEngineFactory() {
		supportedSpecifications = new HashSet<>();
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.PEF_FORMAT), LOCALE).build());
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.TEXT_FORMAT), LOCALE).build());
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.TEXT_FORMAT), "en-US").build());
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.TEXT_FORMAT), "no-NO").build());
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.TEXT_FORMAT), "de").build());
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.TEXT_FORMAT), "de-DE").build());
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.TEXT_FORMAT), "da").build());
		supportedSpecifications.add(new TaskGroupSpecification.Builder(FormatIdentifier.with("obfl"), FormatIdentifier.with(Keys.TEXT_FORMAT), "da-DK").build());
		Set<TaskGroupInformation> tmp = new HashSet<>();
		tmp.add(TaskGroupInformation.newConvertBuilder("obfl", Keys.PEF_FORMAT).locale(LOCALE).build());
		tmp.add(TaskGroupInformation.newConvertBuilder("obfl", Keys.TEXT_FORMAT).build());
		information = Collections.unmodifiableSet(tmp);
	}

	@Override
	public boolean supportsSpecification(TaskGroupInformation spec) {
		return listAll().contains(spec);
	}
	
	@Override
	public TaskGroup newTaskGroup(TaskGroupSpecification spec) {
		return new LayoutEngine(spec, pmw, fe, vf);
	}

	@Override
	public Set<TaskGroupInformation> listAll() {
		return information;
	}

	public void setCreatedWithSPI() {
		if (pmw == null) {
			pmw = PagedMediaWriterFactoryMaker.newInstance();
		}
		if (fe == null) {
			fe = FormatterEngineMaker.newInstance().getFactory();
		}
		if (vf == null) {
			vf = ValidatorFactoryMaker.newInstance();
		}
	}
	
	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setPagedMediaWriterFactory(PagedMediaWriterFactoryMakerService service) {
		this.pmw = service;
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetPagedMediaWriterFactory(PagedMediaWriterFactoryMakerService service) {
		this.pmw = null;
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setFormatterEngineFactory(FormatterEngineFactoryService service) {
		this.fe = service;
	}
	
	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetFormatterEngineFactory(FormatterEngineFactoryService service) {
		this.fe = null;
	}
	
	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference(cardinality=ReferenceCardinality.MANDATORY)
	public void setValidatorFactory(ValidatorFactoryMakerService service) {
		this.vf = service;
	}
	
	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetValidatorFactory(ValidatorFactoryMakerService service) {
		this.vf = null;
	}
}
