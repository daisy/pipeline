package org.daisy.dotify.impl.system.common;

import org.daisy.dotify.api.cr.TaskSystem;
import org.daisy.dotify.api.cr.TaskSystemFactory;
import org.daisy.dotify.api.cr.TaskSystemFactoryException;
import org.daisy.dotify.common.text.FilterLocale;
import org.daisy.dotify.impl.input.Keys;

/**
 * Provides a default task system factory for PEF, OBFL and text output.
 * 
 * @author Joel Håkansson
 */
public class DotifyTaskSystemFactory implements TaskSystemFactory {

	public boolean supportsSpecification(String locale, String outputFormat) {
		//TODO: remove conditions guard once possible 
		return FilterLocale.parse(locale).equals(FilterLocale.parse("sv-SE")) && 
				(Keys.PEF_FORMAT.equals(outputFormat) || Keys.OBFL_FORMAT.equals(outputFormat))
				|| Keys.TEXT_FORMAT.equals(outputFormat);
	}

	public TaskSystem newTaskSystem(String locale, String outputFormat) throws TaskSystemFactoryException {
		if (supportsSpecification(locale, outputFormat)) {
			return new DotifyTaskSystem("Dotify Task System", outputFormat, locale);
		}
		throw new TaskSystemFactoryException("Unsupported specification: " + locale + "/" + outputFormat);
	}

}
