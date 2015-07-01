package org.daisy.dotify.impl.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.daisy.dotify.api.cr.InternalTaskException;
import org.daisy.dotify.api.cr.ReadWriteTask;
import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.LayoutEngineException;
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
	private final String locale;
	private final String mode;
	private final PagedMediaWriter writer;
	private final Logger logger;
	
	/**
	 * Creates a new instance of LayoutEngineTask.
	 * @param name a descriptive name for the task
	 * @param translator the translator to use
	 * @param writer the output writer
	 */
	public LayoutEngineTask(String name, String locale, String mode, PagedMediaWriter writer) {
		super(name);
		this.locale = locale;
		this.mode = mode;
		//this.locale = locale;
		this.writer = writer;
		this.logger = Logger.getLogger(LayoutEngineTask.class.getCanonicalName());
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		try {

			logger.info("Validating input...");

			try {
				ValidatorTask.validate(input, ObflResourceLocator.getInstance().getResourceByIdentifier(ObflResourceIdentifier.OBFL_XML_SCHEMA));
			} catch (ValidatorException e) {
				throw new InternalTaskException("Input validation failed.", e);
			}
			FormatterEngine engine;	//FormatterEngineMaker.newInstance().newFormatterEngine(locale, mode, writer);
									//TODO: add service interface for formatter engine. For now, use reflexion.	
			try {
				Class<?> cls = Class.forName("org.daisy.dotify.consumer.engine.FormatterEngineMaker");
				Method m = cls.getMethod("newInstance");
				Object o = m.invoke(null);
				Method m2 = o.getClass().getMethod("newFormatterEngine", String.class, String.class, PagedMediaWriter.class);
			
				engine = (FormatterEngine)m2.invoke(o, locale, mode, writer);
			} catch (Exception e) {
				throw new InternalTaskException("Failed to instantiate FormatterEngine using reflexion.", e);
			}

			engine.convert(new FileInputStream(input), new FileOutputStream(output));

		} catch (LayoutEngineException e) {
			throw new InternalTaskException(e);
		} catch (FileNotFoundException e) {
			throw new InternalTaskException(e);
		}
	}

}
 