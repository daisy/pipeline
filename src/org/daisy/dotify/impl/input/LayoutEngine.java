package org.daisy.dotify.impl.input;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.tasks.InternalTask;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.writer.AttributeItem;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactory;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryMakerService;

public class LayoutEngine implements TaskGroup {
	private static final String MARK_CAPITAL_LETTERS = "mark-capital-letters";
	private static final String HYPHENATE = "hyphenate";
	private static final String TRANSLATE = "translate";
	private static final String REMOVE_STYLES = "remove-styles";
	private static final QName ENTRY = new QName("http://www.daisy.org/ns/2015/dotify", "entry", "generator");

	private final TaskGroupSpecification spec;
	private final PagedMediaWriterFactoryMakerService pmw;
	private final FormatterEngineFactoryService fe;

	public LayoutEngine(TaskGroupSpecification spec, PagedMediaWriterFactoryMakerService pmw, FormatterEngineFactoryService fe) {
		this.spec = spec;
		this.pmw = pmw;
		this.fe = fe;
	}

	@Override
	public String getName() {
		return "Layout Engine";
	}

	@Override
	public List<InternalTask> compile(Map<String, Object> parameters) throws TaskSystemException {
		Properties p2 = new Properties();
		p2.putAll(parameters);
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
			if (Keys.PEF_FORMAT.equals(spec.getOutputFormat())) {
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

		boolean markCapitals = !p2.getProperty(MARK_CAPITAL_LETTERS, "true").equalsIgnoreCase("false");
		boolean hyphenate = !p2.getProperty(HYPHENATE, "true").equalsIgnoreCase("false");
		
		FormatterConfiguration.Builder config = FormatterConfiguration.with(spec.getLocale(), translatorMode)
			.markCapitalLetters(markCapitals)
			.hyphenate(hyphenate);
		if (p2.getProperty(REMOVE_STYLES, "false").equalsIgnoreCase("true")) {
			config.ignoreStyle("em").ignoreStyle("strong");
		}
		ArrayList<InternalTask> ret = new ArrayList<>();
		ret.add(new LayoutEngineTask("OBFL to " + spec.getOutputFormat().toUpperCase() + " converter", config.build(), paged, fe));
		return ret;
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