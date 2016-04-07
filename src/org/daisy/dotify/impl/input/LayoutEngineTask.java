package org.daisy.dotify.impl.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Logger;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.ReadWriteTask;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.impl.input.ObflResourceLocator.ObflResourceIdentifier;

/**
 * <p>
 * The LayoutEngineTask converts an OBFL-file into a file format defined by the
 * supplied {@link PagedMediaWriter}.</p>
 * 
 * <p>The LayoutEngineTask is an advanced text-only layout system.</p>
 * <p>Input file must be of type OBFL.</p>
 * 
 * @author Joel Håkansson
 *
 */
public class LayoutEngineTask extends ReadWriteTask  {
	private final FormatterConfiguration config;
	private final PagedMediaWriter writer;
	private final FormatterEngineFactoryService fe;
	private final Logger logger;
	
	/**
	 * Creates a new instance of LayoutEngineTask.
	 * @param name the name of the task
	 * @param locale the locale
	 * @param mode the translation mode
	 * @param writer the output writer
	 * @param fe 
	 */
	public LayoutEngineTask(String name, FormatterConfiguration config, PagedMediaWriter writer, FormatterEngineFactoryService fe) {
		super(name);
		this.config = config;
		this.writer = writer;
		this.logger = Logger.getLogger(LayoutEngineTask.class.getCanonicalName());
		this.fe = fe;
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		try {

			logger.info("Validating input...");

			try {
				ValidatorTask.validate(input, ObflResourceLocator.getInstance().getResourceByIdentifier(ObflResourceIdentifier.OBFL_RNG_SCHEMA));
			} catch (ValidatorException e) {
				throw new InternalTaskException("Input validation failed.", e);
			}
			
			FormatterEngine engine = fe.newFormatterEngine(config, writer);
			engine.convert(new FileInputStream(input), new FileOutputStream(output));

		} catch (LayoutEngineException e) {
			throw new InternalTaskException(e);
		} catch (FileNotFoundException e) {
			throw new InternalTaskException(e);
		}
	}

}
 