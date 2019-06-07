package org.daisy.dotify.formatter.impl.engine;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.formatter.Formatter;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.FormatterException;
import org.daisy.dotify.api.obfl.ObflParser;
import org.daisy.dotify.api.obfl.ObflParserException;
import org.daisy.dotify.api.obfl.ObflParserFactoryService;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterException;
import org.daisy.dotify.formatter.impl.common.FactoryManager;

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
class LayoutEngineImpl implements FormatterEngine {
	private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
	private static final QName DC_IDENTIFIER = new QName(DC_NS, "identifier");
	private static final QName DC_DATE = new QName(DC_NS, "date");
	private static final QName DC_FORMAT = new QName(DC_NS, "format");
	private final FormatterConfiguration config;
	private final PagedMediaWriter writer;
	private final ObflParserFactoryService obflFactory;
	private final Logger logger;
	private final FactoryManager fm;
	
	/**
	 * Creates a new instance of LayoutEngineTask.
	 * @param locale the locale
	 * @param mode the mode
	 * @param writer the output writer
	 * @param fm factory manager
	 * @param obflFactoryService the obfl factory service
	 */
	public LayoutEngineImpl(String locale, String mode, PagedMediaWriter writer, FactoryManager fm, ObflParserFactoryService obflFactoryService) {
		this(FormatterConfiguration.with(locale, mode).build(), writer, fm, obflFactoryService);
	}

	/**
	 * Creates a new instance of LayoutEngineTask.
	 * @param config a descriptive name for the task
	 * @param writer the output writer
	 * @param fm factory manager
	 * @param obflFactory the obfl factory service
	 */
	public LayoutEngineImpl(FormatterConfiguration config, PagedMediaWriter writer, FactoryManager fm, ObflParserFactoryService obflFactory) {
		this.config = config;
		this.writer = writer;
		this.obflFactory = obflFactory;
		this.logger = Logger.getLogger(LayoutEngineImpl.class.getCanonicalName());
		this.fm = fm;
	}

	@Override
	public void convert(InputStream input, OutputStream output) throws LayoutEngineException {
		File f = null;
		try {
			try {
				logger.info("Parsing input...");

				ObflParser obflParser = obflFactory.newObflParser();
				Formatter formatter = fm.getFormatterFactory().newFormatter(config.getLocale(), config.getTranslationMode());
				formatter.setConfiguration(config);
				obflParser.parse(fm.getXmlInputFactory().createXMLEventReader(input), formatter);

				try {
					input.close();
				} catch (Exception e) {
					logger.log(Level.FINE, "Failed to close stream.", e);
				}

				logger.info("Rendering output...");
				List<MetaDataItem> meta = obflParser.getMetaData().stream()
					// Filter out identifier, date and format from the OBFL meta data
					// because the meta data in the OBFL file is about itself, and these properties are not transferable
					.filter(item->!(item.getKey().equals(DC_IDENTIFIER) || item.getKey().equals(DC_DATE) || item.getKey().equals(DC_FORMAT)))
					.collect(Collectors.toList());
				writer.prepare(meta);
				writer.open(output);
				formatter.write(writer);

			} catch (PagedMediaWriterException e) {
				throw new LayoutEngineException("Could not open media writer.", e);
			} catch (XMLStreamException e) {
				throw new LayoutEngineException("XMLStreamException while running task.", e);
			} catch (ObflParserException e) {
				throw new LayoutEngineException("ObflParserException while running task.", e);
			} catch (FormatterException e) {
				throw new LayoutEngineException("FormatterException while running task.", e);
			}
		} finally {
			if (f != null && !f.delete()) {
				f.deleteOnExit();
			}
		}
	}

}
 