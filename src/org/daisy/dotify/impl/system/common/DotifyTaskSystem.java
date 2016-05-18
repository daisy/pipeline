package org.daisy.dotify.impl.system.common;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupFactoryMakerService;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystem;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.writer.AttributeItem;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;
import org.daisy.dotify.impl.input.DuplicatorTask;
import org.daisy.dotify.impl.input.Keys;
import org.daisy.dotify.impl.input.LayoutEngineTask;


/**
 * <p>Transforms XML into braille in PEF 2008-1 format.</p>
 * <p>Transforms documents into text format.</p>
 * 
 * <p>This TaskSystem consists of the following steps:</p>
 * <ol>
	 * <li>Input Manager. Validates and converts input to OBFL.</li>
	 * <li>OBFL to PEF converter.
	 * 		Translates all characters into braille, and puts the text flow onto pages.</li>
 * </ol>
 * <p>The result should be validated against the PEF Relax NG schema using int_daisy_validator.</p>
 * @author Joel Håkansson
 */
public class DotifyTaskSystem implements TaskSystem {
	final static String MARK_CAPITAL_LETTERS = "mark-capital-letters";
	final static String HYPHENATE = "hyphenate";
	final static String TRANSLATE = "translate";
	final static String REMOVE_STYLES = "remove-styles";
	/**
	 * Specifies a location where the intermediary obfl output should be stored
	 */
	final static String OBFL_OUTPUT_LOCATION = "obfl-output-location";
	private final static QName ENTRY = new QName("http://www.daisy.org/ns/2015/dotify", "entry", "generator");
	private final String outputFormat;
	private final String context;
	private final String name;
	private final TaskGroupFactoryMakerService imf;
	private final PagedMediaWriterFactoryMakerService pmw;
	private final FormatterEngineFactoryService fe;
	
	public DotifyTaskSystem(String name, String outputFormat, String context,
			TaskGroupFactoryMakerService imf, PagedMediaWriterFactoryMakerService pmw, FormatterEngineFactoryService fe) {
		this.context = context;
		this.outputFormat = outputFormat;
		this.name = name;
		this.imf = imf;
		this.pmw = pmw;
		this.fe = fe;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public ArrayList<InternalTask> compile(Map<String, Object> pa) throws TaskSystemException {
		//configURL = new URL(resourceBase, config);
		
		RunParameters p;
		{
			Properties p1 = new Properties();
			for (String key : pa.keySet()) {
				p1.put(key, pa.get(key));
			}
			p = new RunParameters(p1);
		}
		Properties p2 = new Properties();
		HashMap<String, Object> h = new HashMap<>();
		for (Object key : p.getKeys()) {
			p2.put(key, p.getProperty(key));
			h.put(key.toString(), p.getProperty(key));
		}
		
		ArrayList<InternalTask> setup = new ArrayList<>();
		{
			//InputDetector
			TaskGroup idts = imf.newTaskGroup(new TaskGroupSpecification(p2.get(Keys.INPUT_FORMAT).toString(), "obfl", context));
			setup.addAll(idts.compile(h));
			
			// Whitespace normalizer TransformerFactoryConstants.SAXON8
			/*
			 * setup.add(new XsltTask("OBFL whitespace normalizer",
			 * ObflResourceLocator.getInstance().getResourceByIdentifier(
			 * ObflResourceIdentifier.OBFL_WHITESPACE_NORMALIZER_XSLT),
			 * null,
			 * h));
			 */
			//setup.add(new OBFLWhitespaceNormalizerTask("OBFL whitespace normalizer"));
		}
		
		String keep = p2.getProperty(OBFL_OUTPUT_LOCATION);
		if (keep!=null && !"".equals(keep)) {
			setup.add(new DuplicatorTask("OBFL archiver", new File(keep)));
		}

		if (Keys.OBFL_FORMAT.equals(outputFormat)) {
			return setup;
		} else {
		
			// Layout FLOW as PEF

			// Customize which parameters are sent to the PEFMediaWriter, as it
			// outputs all parameters for future reference
			// System file paths should be concealed for security reasons
			p2.remove(Keys.INPUT);
			p2.remove(Keys.INPUT_URI);
			p2.remove("output");
			p2.remove(Keys.TEMP_FILES_DIRECTORY);

			String translatorMode;
			PagedMediaWriter paged;
			
			try {
				if (Keys.PEF_FORMAT.equals(outputFormat)) {
					// additional braille modes here...
					translatorMode = p2.getProperty(TRANSLATE, BrailleTranslatorFactory.MODE_UNCONTRACTED);
					PagedMediaWriterFactory pmf = pmw.getFactory(MediaTypes.PEF_MEDIA_TYPE);
					paged = pmf.newPagedMediaWriter();
					paged.prepare(asMetadata(p2));
				} else {
					translatorMode = p2.getProperty(TRANSLATE, BrailleTranslatorFactory.MODE_BYPASS);
					paged = pmw.newPagedMediaWriter(MediaTypes.TEXT_MEDIA_TYPE);
				}
			} catch (PagedMediaWriterConfigurationException e) {
				throw new TaskSystemException(e);
			}
			// BrailleTranslator bt =
			// BrailleTranslatorFactoryMaker.newInstance().newTranslator(context,
			// translatorMode);
			boolean markCapitals = !p2.getProperty(MARK_CAPITAL_LETTERS, "true").equalsIgnoreCase("false");
			boolean hyphenate = !p2.getProperty(HYPHENATE, "true").equalsIgnoreCase("false");
			
			FormatterConfiguration.Builder config = FormatterConfiguration.with(context, translatorMode)
				.markCapitalLetters(markCapitals)
				.hyphenate(hyphenate);
			if (p2.getProperty(REMOVE_STYLES, "false").equalsIgnoreCase("true")) {
				config.ignoreStyle("em").ignoreStyle("strong");
			}
			setup.add(new LayoutEngineTask("OBFL to " + outputFormat.toUpperCase() + " converter", config.build(), paged, fe));

			return setup;
		}
	}
	
	private static List<MetaDataItem> asMetadata(Properties p2) {
		ArrayList<MetaDataItem> meta = new ArrayList<>();
		
		String ident = p2.getProperty("identifier");
		if (ident!=null) {
			meta.add(asDCItem("identifier", ident));
		}
		String date = p2.getProperty("date");
		if (date!=null) {
			meta.add(asDCItem("date", date));
		}
		for (Object key : p2.keySet()) {
			meta.add(new MetaDataItem.Builder(ENTRY, p2.get(key).toString()).attribute(new AttributeItem("key", key.toString())).build());
		}
		return meta;
	}
	
	private static MetaDataItem asDCItem(String name, String value) {
		return new MetaDataItem.Builder(new QName("http://purl.org/dc/elements/1.1/", name, "dc"), value).build();
	}

}