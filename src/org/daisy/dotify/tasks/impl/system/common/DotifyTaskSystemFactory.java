package org.daisy.dotify.tasks.impl.system.common;

import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupInformation;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemFactory;
import org.daisy.dotify.api.tasks.TaskSystemFactoryException;
import org.daisy.dotify.tasks.impl.input.SPIHelper;

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

	@Override
	public boolean supportsSpecification(String inputFormat, String outputFormat, String locale) {
		boolean inputMatch = false;
		boolean outputMatch = false;
		for (TaskGroupInformation info : imf.list(locale)) {
			if (info.getInputFormat().equals(inputFormat)) {
				inputMatch = true;
			}
			if (info.getOutputFormat().equals(outputFormat)) {
				outputMatch = true;
			}
			if (inputMatch && outputMatch) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TaskSystem newTaskSystem(String inputFormat, String outputFormat, String locale)
			throws TaskSystemFactoryException {
		if (supportsSpecification(inputFormat, outputFormat, locale)) {
			return new DotifyTaskSystem("Dotify Task System", inputFormat, outputFormat, locale, imf);
		}
		throw new TaskSystemFactoryException("Unsupported specification: " + locale + "(" + inputFormat + "->" + outputFormat + ")");
	}

	/**
	 * Sets a factory dependency.
	 * @param service the dependency
	 */
	@Reference
	public void setInputManagerFactory(TaskGroupFactoryMakerService service) {
		this.imf = service;
	}

	/**
	 * Removes a factory dependency.
	 * @param service the dependency to remove
	 */
	public void unsetInputManagerFactory(TaskGroupFactoryMakerService service) {
		this.imf = null;
	}

	@Override
	public void setCreatedWithSPI() {
		if (imf == null) {
			imf = SPIHelper.getInputManagerFactoryMakerService();
		}
	}

}
