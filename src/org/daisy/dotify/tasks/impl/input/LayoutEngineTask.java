package org.daisy.dotify.tasks.impl.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.engine.LayoutEngineException;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.writer.AttributeItem;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.dotify.tasks.impl.input.ObflResourceLocator.ObflResourceIdentifier;
import org.daisy.streamline.api.tasks.AnnotatedFile;
import org.daisy.streamline.api.tasks.DefaultAnnotatedFile;
import org.daisy.streamline.api.tasks.InternalTaskException;
import org.daisy.streamline.api.tasks.ReadWriteTask;
import org.daisy.streamline.api.tasks.TaskGroupSpecification;
import org.daisy.streamline.api.tasks.TaskOption;
import org.daisy.streamline.api.tasks.TaskOptionValue;
import org.daisy.streamline.api.tasks.TaskSystemException;

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
	private static final String MARK_CAPITAL_LETTERS = "mark-capital-letters";
	private static final String HYPHENATE = "hyphenate";
	private static final String TRANSLATE = "translate";
	private static final String REMOVE_STYLES = "remove-styles";
	private static final String DATE_FORMAT = "dateFormat";
	private static final String DATE = "date";
	private static final String IDENTIFIER = "identifier";
	private static final String ALLOWS_ENDING_VOLUME_ON_HYPHEN = "allows-ending-volume-on-hyphen";
	private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
	private static final QName ENTRY = new QName("http://www.daisy.org/ns/2015/dotify", "entry", "generator");
	private final FormatterConfiguration config;
	private final PagedMediaWriter writer;
	private final FormatterEngineFactoryService fe;
	private final TaskGroupSpecification spec;
	private static final Logger logger = Logger.getLogger(LayoutEngineTask.class.getCanonicalName());
	private List<TaskOption> options;
	
	/**
	 * Creates a new instance.
	 * @param p2 the options
	 * @param spec the specification
	 * @param pmw the paged media writer factory maker service
	 * @param fe the formatter engine factory service
	 * @throws TaskSystemException if the instance could not be created 
	 */
	public LayoutEngineTask(Properties p2, TaskGroupSpecification spec, PagedMediaWriterFactoryMakerService pmw, FormatterEngineFactoryService fe) throws TaskSystemException {
		super(buildName(spec.getOutputFormat().toUpperCase()));
		addDefaults(p2);
		String translatorMode = getTranslationMode(p2, spec.getOutputFormat());
		this.spec = spec;
		this.writer = getWriter(p2, spec, pmw);
		this.config = getFormatterConfig(p2, translatorMode, spec.getLocale());
		this.fe = fe;
		this.options = null;
	}
	
	static String buildName(String outputFormat) {
		return "OBFL to " + outputFormat + " converter";
	}
	
	private static PagedMediaWriter getWriter(Properties p2, TaskGroupSpecification spec, PagedMediaWriterFactoryMakerService pmw) throws TaskSystemException {
		try {
			PagedMediaWriterFactory pmf = pmw.getFactory(mediaTypeForFormat(spec.getOutputFormat()));
			PagedMediaWriter paged = pmf.newPagedMediaWriter();
			paged.prepare(asMetadata(p2));
			return paged;
		} catch (PagedMediaWriterConfigurationException e) {
			throw new TaskSystemException(e);
		}		
	}
	
	private static String getTranslationMode(Properties p2, String out) throws TaskSystemException {
		switch (out) {
			case Keys.PEF_FORMAT:
				return p2.getProperty(TRANSLATE, BrailleTranslatorFactory.MODE_UNCONTRACTED);
			case Keys.TEXT_FORMAT:
				return p2.getProperty(TRANSLATE, BrailleTranslatorFactory.MODE_BYPASS);
			default:
				throw new TaskSystemException("Unknown format: " + out);
		}
	}
	
	private static String mediaTypeForFormat(String ext) throws TaskSystemException {
		switch (ext) {
			case Keys.PEF_FORMAT:
				return MediaTypes.PEF_MEDIA_TYPE;
			case Keys.TEXT_FORMAT:
				return MediaTypes.TEXT_MEDIA_TYPE;
			default:
				throw new TaskSystemException("Unknown format: " + ext);
		}
	}
	
	private static FormatterConfiguration getFormatterConfig(Properties p2, String translatorMode, String locale) {
		boolean markCapitals = !p2.getProperty(MARK_CAPITAL_LETTERS, "true").equalsIgnoreCase("false");
		boolean hyphenate = !p2.getProperty(HYPHENATE, "true").equalsIgnoreCase("false");
		boolean allowsEndingVolumeOnHyphen = !p2.getProperty(ALLOWS_ENDING_VOLUME_ON_HYPHEN, "true").equalsIgnoreCase("false");
		
		FormatterConfiguration.Builder config = FormatterConfiguration.with(locale, translatorMode)
			.markCapitalLetters(markCapitals)
			.hyphenate(hyphenate)
			.allowsEndingVolumeOnHyphen(allowsEndingVolumeOnHyphen);
		if (p2.getProperty(REMOVE_STYLES, "false").equalsIgnoreCase("true")) {
			config.ignoreStyle("em").ignoreStyle("strong");
		}
		return config.build();
	}
	
	private static void addDefaults(Properties p2) {
 		String dateFormat = p2.getProperty(DATE_FORMAT);
		if (dateFormat==null || "".equals(dateFormat)) {
			dateFormat = DEFAULT_DATE_FORMAT;
			p2.put(DATE_FORMAT, dateFormat);
		}
		if (p2.getProperty(DATE)==null || "".equals(p2.getProperty(DATE))) {
			p2.put(DATE, getDefaultDate(dateFormat));
		}
		if (p2.getProperty(IDENTIFIER)==null || "".equals(p2.getProperty(IDENTIFIER))) {
			String id = Double.toHexString(Math.random());
			id = id.substring(id.indexOf('.')+1);
			id = id.substring(0, id.indexOf('p'));
			p2.put(IDENTIFIER, "dummy-id-"+ id);
		}
	}
	
	private static List<MetaDataItem> asMetadata(Properties p2) {
		ArrayList<MetaDataItem> meta = new ArrayList<>();
		
		String ident = p2.getProperty(IDENTIFIER);
		if (ident!=null) {
			meta.add(asDCItem(IDENTIFIER, ident));
		}
		String date = p2.getProperty(DATE);
		if (date!=null) {
			meta.add(asDCItem(DATE, date));
		}
		for (Object key : p2.keySet()) {
			meta.add(new MetaDataItem.Builder(ENTRY, p2.get(key).toString()).attribute(new AttributeItem("key", key.toString())).build());
		}
		return meta;
	}

	private static MetaDataItem asDCItem(String name, String value) {
		return new MetaDataItem.Builder(new QName("http://purl.org/dc/elements/1.1/", name, "dc"), value).build();
	}

	private static List<TaskOption> buildOptions(TaskGroupSpecification spec) {
		List<TaskOption> ret = new ArrayList<>();
		ret.add(withBooleanValues(
				new TaskOption.Builder(MARK_CAPITAL_LETTERS)
				.description("Specifies if capital letters should be marked in braille."))
				.defaultValue("true")
				.build());
		ret.add(withBooleanValues(
				new TaskOption.Builder(REMOVE_STYLES)
				.description("Specifies if em/strong styles should be removed."))
				.defaultValue("false")
			.build());
		ret.add(withBooleanValues(
				new TaskOption.Builder(HYPHENATE)
				.description("Specifies if hyphenation should be used."))
				.defaultValue("true")
			.build());
		ret.add(new TaskOption.Builder(TRANSLATE)
			.description("Specifies a translation mode.")
			.build());
		//PEF supports additional options
		if (Keys.PEF_FORMAT.equals(spec.getOutputFormat())) {
			ret.add(new TaskOption.Builder(IDENTIFIER)
				.description("Sets identifier in meta data.")
				.build());
			ret.add(new TaskOption.Builder(DATE)
				.description("Sets date in meta data.")
				.defaultValue(getDefaultDate(DEFAULT_DATE_FORMAT))
				.build());
		}
		return ret;
	}
	
	private static TaskOption.Builder withBooleanValues(TaskOption.Builder builder) {
		return builder.addValue(new TaskOptionValue.Builder("true").build())
		.addValue(new TaskOptionValue.Builder("false").build());
	}
	
	private static String getDefaultDate(String dateFormat) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return sdf.format(c.getTime());
	}

	@Override
	public AnnotatedFile execute(AnnotatedFile input, File output) throws InternalTaskException {
		try {

			logger.info("Validating input...");

			try {
				ValidatorTask.validate(input.getFile(), ObflResourceLocator.getInstance().getResourceByIdentifier(ObflResourceIdentifier.OBFL_RNG_SCHEMA));
			} catch (ValidatorException e) {
				throw new InternalTaskException("Input validation failed.", e);
			}
			
			FormatterEngine engine = fe.newFormatterEngine(config, writer);
			engine.convert(new FileInputStream(input.getFile()), new FileOutputStream(output));

		} catch (LayoutEngineException e) {
			throw new InternalTaskException(e);
		} catch (FileNotFoundException e) {
			throw new InternalTaskException(e);
		}
		return new DefaultAnnotatedFile.Builder(output).build();
	}

	@Override
	public void execute(File input, File output) throws InternalTaskException {
		execute(new DefaultAnnotatedFile.Builder(input).build(), output);
	}

	@Override
	public List<TaskOption> getOptions() {
		if (options==null) {
			options = Collections.unmodifiableList(buildOptions(spec));
		}
		return options;
	}

}
 