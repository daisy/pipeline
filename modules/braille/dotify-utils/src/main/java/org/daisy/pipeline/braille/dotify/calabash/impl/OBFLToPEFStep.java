package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.Iterables;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.RuleCounterStyle;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.formatter.FormatterFactory;
import org.daisy.dotify.api.obfl.ObflParserFactoryService;
import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderFactory;
import org.daisy.dotify.api.translator.TextBorderFactoryMakerService;
import org.daisy.dotify.api.translator.TextBorderFactoryService;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.TransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.util.Function0;
import org.daisy.pipeline.braille.common.util.Functions;
import org.daisy.pipeline.braille.dotify.impl.CounterHandlingBrailleTranslator;
import org.daisy.pipeline.braille.pef.TableRegistry;
import org.daisy.pipeline.css.CounterStyle;

import org.osgi.framework.FrameworkUtil;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the <code>{http://code.google.com/p/dotify/}obfl-to-pef</code> step.
 *
 * @see <a href="../../../../../../../../resources/xml/library.xpl">The XProc library
 *      <code>http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl</code></a>.
 */
public class OBFLToPEFStep extends DefaultStep implements XProcStep {
	
	private static final QName _locale = new QName("locale");
	private static final QName _mode = new QName("mode");
	private static final QName _braille_charset = new QName("braille-charset");
	private static final QName _identifier = new QName("identifier");
	private static final QName _style_type = new QName("style-type");
	private static final QName _css_text_transform_definitions = new QName("css-text-transform-definitions");
	private static final QName _css_hyphenation_resource_definitions = new QName("css-hyphenation-resource-definitions");
	private static final QName _css_counter_style_definitions = new QName("css-counter-style-definitions");
	private static final QName _has_volume_transition = new QName("has-volume-transition");
	
	private static final String ERR_NS = "http://www.daisy.org/ns/pipeline/errors";
	
	/**
	 * Code for Dotify errors caused by invalid or unsupported OBFL.
	 */
	private static final QName ERR_DOTIFY_001 = new QName("pe", ERR_NS, "DOTIFY001");

	/**
	 * Code for Dotify errors caused by OBFL input that can not be formatted.
	 */
	private static final QName ERR_DOTIFY_002 = new QName("pe", ERR_NS, "DOTIFY002");

	/**
	 * Code for unexpected Dotify errors.
	 */
	private static final QName ERR_DOTIFY_003 = new QName("pe", ERR_NS, "DOTIFY003");

	private ReadablePipe source = null;
	private WritablePipe result = null;
	private final Map<String,String> params = new HashMap<String,String>();
	
	private final FormatterEngineFactoryService formatterEngineFactoryService;
	private final FormatterFactory formatterFactory;
	private final ObflParserFactoryService obflParserFactoryService;
	private final TextBorderFactoryService textBorderFactoryService;
	private final BrailleTranslatorRegistry brailleTranslatorRegistry;
	private final TableRegistry tableRegistry;
	private final TemporaryBrailleTranslatorProvider temporaryBrailleTranslatorProvider;
	
	public OBFLToPEFStep(XProcRuntime runtime,
	                     XAtomicStep step,
	                     FormatterEngineFactoryService formatterEngineFactoryService,
	                     FormatterFactory formatterFactory,
	                     ObflParserFactoryService obflParserFactoryService,
	                     TextBorderFactoryService textBorderFactoryService,
	                     BrailleTranslatorRegistry brailleTranslatorRegistry,
	                     TableRegistry tableRegistry,
	                     TemporaryBrailleTranslatorProvider temporaryBrailleTranslatorProvider) {
		super(runtime, step);
		this.formatterEngineFactoryService = formatterEngineFactoryService;
		this.formatterFactory = formatterFactory;
		this.obflParserFactoryService = obflParserFactoryService;
		this.textBorderFactoryService = textBorderFactoryService;
		this.brailleTranslatorRegistry = brailleTranslatorRegistry;
		this.tableRegistry = tableRegistry;
		if (temporaryBrailleTranslatorProvider == null) throw new IllegalStateException();
		this.temporaryBrailleTranslatorProvider = temporaryBrailleTranslatorProvider;
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		source = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}
	
	@Override
	public void setParameter(String port, QName name, RuntimeValue value) {
		if ("parameters".equals(port))
			setParameter(name, value);
		else
			throw new XProcException("No parameters allowed on port '" + port + "'");
	}
	
	@Override
	public void setParameter(QName name, RuntimeValue value) {
		if ("".equals(name.getNamespaceURI()))
			params.put(name.getLocalName(), value.getString());
	}

	@Override
	public void reset() {
		source.resetReader();
		result.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		Function0<Void> evictTempTranslator = Functions.noOp; // any temporary translators that are created specially
		                                                      // for this conversion need to be destroyed afterwards
		try {
			
			XdmNode obflNode = source.read();
			String styleType = getOption(_style_type, "");
			String mode = getOption(_mode).getString();
			String locale = getOption(_locale).getString();
			String brailleCharset = getOption(_braille_charset, "");

			if (brailleCharset != null && !"".equals(brailleCharset)) {
				// if braille-charset is specified we can assume that mode is in query format
				Query modeQuery;
				try {
					modeQuery = query(mode);
				} catch (Exception e) {
					throw new IllegalArgumentException("Expected mode in query format: " + mode, e);
				}
				mode = mutableQuery(modeQuery).add("braille-charset", brailleCharset).toString();
			}

			if ("text/css".equals(styleType)) {
				
				// We're assuming that no other translators than the DAISY Pipeline implementations
				// support text/css.
				Query mainQuery;
				try {
					mainQuery = query(mode);
				} catch (Exception e) {
					throw new IllegalArgumentException("Expected mode in query format: " + mode, e);
				}
				mainQuery = mutableQuery(mainQuery).add("input", "text-css").add("output", "braille");
				mode = mainQuery.toString();
				if (locale != null && !"und".equals(locale))
					mainQuery = mutableQuery(mainQuery).add("document-locale", locale);
				BrailleTranslator translator; {
					String textTransformAndHyphenationResourceDefinitions
						= getOption(_css_text_transform_definitions, "")
						+ getOption(_css_hyphenation_resource_definitions, "");
					try {
						translator = (!"".equals(textTransformAndHyphenationResourceDefinitions)
							? brailleTranslatorRegistry.getWithHyphenator(mainQuery,
							                                              textTransformAndHyphenationResourceDefinitions,
							                                              obflNode.getBaseURI(), // note that this is the empty string
							                                                                     // if the OBFL came from pxi:css-to-obfl
							                                              false)
							: brailleTranslatorRegistry.getWithHyphenator(mainQuery)
						).iterator().next();
					} catch (NoSuchElementException e) {
						if (!"".equals(textTransformAndHyphenationResourceDefinitions))
							throw new XProcException(
								"No translator available for style type '" + styleType + "', mode '" + mode
								+ "', locale '" + locale + "' and CSS rules:\n" + textTransformAndHyphenationResourceDefinitions);
						else
							throw new XProcException(
								step.getNode(),
								"No translator available for style type '" + styleType + "', mode '" + mode
								+ "' and locale '" + locale + "'");
					}
				}
				String counterStyleDefinitions = getOption(_css_counter_style_definitions, "");
				Map<String,CounterStyle> counterStyles = "".equals(counterStyleDefinitions)
					? null
					: CounterStyle.parseCounterStyleRules(
						Iterables.filter(new InlineStyle(counterStyleDefinitions), RuleCounterStyle.class));
				// handle text-transform: -dotify-counter
				translator = new CounterHandlingBrailleTranslator(translator, counterStyles);
				translator = logCreate(translator, logger);
				evictTempTranslator = temporaryBrailleTranslatorProvider.provideTemporarily(translator);
				mode = mutableQuery().add("id", translator.getIdentifier()).toString();
				locale = "und";
			} else if (!"".equals(styleType)) {
				throw new XProcException(step.getNode(), "Value of style-type option not recognized: " + styleType);
			}
			
			// Read OBFL
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			Serializer serializer = runtime.getProcessor().newSerializer();
			serializer.setOutputStream(s);
			serializer.setCloseOnCompletion(true);
			serializer.serializeNode(obflNode);
			serializer.close();
			InputStream obflStream = new ByteArrayInputStream(s.toByteArray());
			s.close();
			
			// Convert
			String identifier = getOption(_identifier, "");
			boolean markCapitalLetters; {
				String p = params.get("mark-capital-letters");
				markCapitalLetters = (p == null) ? true : !p.equalsIgnoreCase("false"); }
			boolean hyphenate; {
				String p = params.get("hyphenate");
				hyphenate = (p == null) ? true : !p.equalsIgnoreCase("false"); }
			boolean allowTextOverflowTrimming; {
				String p = params.get("allow-text-overflow-trimming");
				allowTextOverflowTrimming = (p == null) ? false : p.equalsIgnoreCase("true"); }
			boolean removeStyles; {
				String p = params.get("remove-styles");
				removeStyles = (p == null) ? false : p.equalsIgnoreCase("true"); }
			boolean allowEndingPageOnHyphen;
			boolean allowEndingVolumeOnHyphen; {
				String p = params.get("hyphenation-at-page-breaks");
				if ("false".equalsIgnoreCase(p)) {
					allowEndingPageOnHyphen = false;
					allowEndingVolumeOnHyphen = false;
				} else if ("except-at-volume-breaks".equalsIgnoreCase(p)) {
					allowEndingPageOnHyphen = true;
					allowEndingVolumeOnHyphen = false;
				} else { // true (default)
					allowEndingPageOnHyphen = true;
					// Follow the OBFL standard which says that "when volume-transition is
					// present, the last page or sheet in each volume may be modified so that
					// the volume break occurs earlier than usual: preferably between two
					// blocks, or if that is not possible, between words"
					// (http://braillespecs.github.io/obfl/obfl-specification.html#L8701). In
					// other words, volumes should by default not be allowed to end on a hyphen.
					allowEndingVolumeOnHyphen = !getOption(_has_volume_transition, false);
				}
			}
			FormatterConfiguration.Builder config = FormatterConfiguration.with(locale, mode)
				.markCapitalLetters(markCapitalLetters)
				.hyphenate(hyphenate)
				.allowsTextOverflowTrimming(allowTextOverflowTrimming)
				.allowsEndingPageOnHyphen(allowEndingPageOnHyphen)
				.allowsEndingVolumeOnHyphen(allowEndingVolumeOnHyphen);
			if (removeStyles)
				config.ignoreStyle("em").ignoreStyle("strong");
			Table brailleCharsetTable = "".equals(brailleCharset)
				? null
				: tableRegistry.get(mutableQuery().add("id", brailleCharset)).iterator().next();
			FormatterEngine engine = newFormatterEngine(config.build(),
			                                            newPEFWriter(identifier, brailleCharsetTable),
			                                            brailleCharsetTable == null
			                                                ? null
			                                                : brailleCharsetTable.newBrailleConverter());
			s = new ByteArrayOutputStream();
			engine.convert(obflStream, s);
			obflStream.close();
			InputStream pefStream = new ByteArrayInputStream(s.toByteArray());
			s.close();
			
			// Write PEF
			result.write(runtime.getProcessor().newDocumentBuilder().build(new StreamSource(pefStream)));
			pefStream.close(); }
		
		catch (Throwable e) {
			String msg = e.getMessage();
			if (msg != null)
				if ((msg.contains("Cannot fit") && msg.contains("into a margin-region of size"))
				    || msg.contains("Failed to solve table"))
					throw new XProcException(ERR_DOTIFY_002, step, e);
			throw XProcStep.raiseError(e, step); }
		finally {
			evictTempTranslator.apply(); }
	}
	
	private PagedMediaWriter newPEFWriter(String identifier, Table brailleCharset) throws PagedMediaWriterConfigurationException {
		PagedMediaWriter writer = null;
		List<MetaDataItem> meta = new ArrayList<MetaDataItem>();
		if (brailleCharset != null)
			meta.add(new MetaDataItem(new javax.xml.namespace.QName("http://www.daisy.org/ns/pipeline/",
			                                                        "ascii-braille-charset",
			                                                        "daisy"),
			                          brailleCharset.getIdentifier()));
		if (!"".equals(identifier))
			meta.add(new MetaDataItem(new javax.xml.namespace.QName("http://purl.org/dc/elements/1.1/", "identifier", "dc"),
			                          identifier));
		writer = new PEFWriter(brailleCharset);
		if (!meta.isEmpty())
			writer.prepare(meta);
		return writer;
	}
	
	private FormatterEngine newFormatterEngine(FormatterConfiguration config,
	                                           PagedMediaWriter writer,
	                                           BrailleConverter brailleCharset) {
		// HACK: If a BrailleConverter is specified, we create a new TextBorderFactoryMakerService
		// that uses the default TextBorderFactoryService to create Unicode braille patterns and
		// encodes them with the given BrailleConverter. We then make the default
		// FormatterEngineFactoryService, FormatterFactory and ObflParserFactoryService use this
		// TextBorderFactoryMakerService by using reflection. This assumes that the
		// FormatterEngineFactoryService is a
		// org.daisy.dotify.formatter.impl.engine.LayoutEngineFactoryImpl, the FormatterFactory is a
		// org.daisy.dotify.formatter.impl.FormatterFactoryImpl, and the ObflParserFactoryService is
		// a org.daisy.dotify.formatter.impl.obfl.ObflParserFactoryImpl, and that these are the only
		// classes that bind a TextBorderFactoryMakerService. If no BrailleConverter is specified,
		// we need to reset the TextBorderFactoryMakerService of the default
		// FormatterEngineFactoryService, FormatterFactory and ObflParserFactoryService.
		TextBorderFactoryMakerService asciiTextBorderFactoryMakerService = new TextBorderFactoryMakerService() {
				public TextBorderStyle newTextBorderStyle(Map<String,Object> features)
						throws TextBorderConfigurationException {
					TextBorderFactory f = textBorderFactoryService.newFactory();
					for (String k : features.keySet())
						f.setFeature(k, features.get(k));
					TextBorderStyle style = f.newTextBorderStyle();
					if (brailleCharset == null)
						return style;
					return new TextBorderStyle.Builder()
						.topLeftCorner    (brailleCharset.toText(style.getTopLeftCorner()))
						.topBorder        (brailleCharset.toText(style.getTopBorder()))
						.topRightCorner   (brailleCharset.toText(style.getTopRightCorner()))
						.leftBorder       (brailleCharset.toText(style.getLeftBorder()))
						.rightBorder      (brailleCharset.toText(style.getRightBorder()))
						.bottomLeftCorner (brailleCharset.toText(style.getBottomLeftCorner()))
						.bottomBorder     (brailleCharset.toText(style.getBottomBorder()))
						.bottomRightCorner(brailleCharset.toText(style.getBottomRightCorner()))
						.build();
				}
			};
		for (Object object : new Object[]{formatterEngineFactoryService,
		                                  formatterFactory,
		                                  obflParserFactoryService}) {
			try {
				object.getClass()
					.getMethod(object == formatterFactory ? "setTextBorderFactory" : "setTextBorderFactoryMaker",
					           TextBorderFactoryMakerService.class)
					.invoke(object, asciiTextBorderFactoryMakerService);
			} catch (NoSuchMethodException |
			         SecurityException |
			         IllegalAccessException |
			         IllegalArgumentException |
			         InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		return formatterEngineFactoryService.newFormatterEngine(config, writer);
	}
	
	@Component(
		name = "pxi:obfl-to-pef",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}obfl-to-pef" }
	)
	public static class Provider implements XProcStepProvider  {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new OBFLToPEFStep(runtime,
			                         step,
			                         formatterEngineFactoryService,
			                         formatterFactory,
			                         obflParserFactoryService,
			                         textBorderFactoryService,
			                         brailleTranslatorRegistry,
			                         tableRegistry,
			                         temporaryBrailleTranslatorProvider);
		}
		
		@Activate
		protected void connectDotifyServices() {
			for (Object object : new Object[]{formatterEngineFactoryService,
			                                  obflParserFactoryService}) {
				try {
					object.getClass()
						.getMethod("setFormatterFactory", FormatterFactory.class)
						.invoke(object, formatterFactory);
				} catch (NoSuchMethodException |
				         SecurityException |
				         IllegalAccessException |
				         IllegalArgumentException |
				         InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
			try {
				formatterEngineFactoryService.getClass()
					.getMethod("setObflParserFactory", ObflParserFactoryService.class)
					.invoke(formatterEngineFactoryService, obflParserFactoryService);
			} catch (NoSuchMethodException |
			         SecurityException |
			         IllegalAccessException |
			         IllegalArgumentException |
			         InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		
		private FormatterEngineFactoryService formatterEngineFactoryService;
		
		@Reference(
			name = "FormatterEngineFactoryService",
			unbind = "-",
			service = FormatterEngineFactoryService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindFormatterEngineFactoryService(FormatterEngineFactoryService service) {
			if (!OSGiHelper.inOSGiContext())
				service.setCreatedWithSPI();
			formatterEngineFactoryService = service;
		}
		
		private FormatterFactory formatterFactory;
		
		@Reference(
			name = "FormatterFactory",
			unbind = "-",
			service = FormatterFactory.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindFormatterFactory(FormatterFactory factory) {
			if (!OSGiHelper.inOSGiContext())
				factory.setCreatedWithSPI();
			formatterFactory = factory;
		}
		
		private ObflParserFactoryService obflParserFactoryService;
		
		@Reference(
			name = "ObflParserFactoryService",
			unbind = "-",
			service = ObflParserFactoryService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindObflParserFactoryService(ObflParserFactoryService service) {
			if (!OSGiHelper.inOSGiContext())
				service.setCreatedWithSPI();
			obflParserFactoryService = service;
		}
		
		private TextBorderFactoryService textBorderFactoryService;
		
		@Reference(
			name = "TextBorderFactoryService",
			unbind = "-",
			service = TextBorderFactoryService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindTextBorderFactoryService(TextBorderFactoryService service) {
			if (!OSGiHelper.inOSGiContext())
				service.setCreatedWithSPI();
			textBorderFactoryService = service;
		}
		
		private BrailleTranslatorRegistry brailleTranslatorRegistry;
		
		@Reference(
			name = "BrailleTranslatorRegistry",
			unbind = "-",
			service = BrailleTranslatorRegistry.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindBrailleTranslatorRegistry(BrailleTranslatorRegistry registry) {
			brailleTranslatorRegistry = registry.withContext(logger);
			logger.debug("Binding BrailleTranslator registry: {}", registry);
		}

		private TemporaryBrailleTranslatorProvider temporaryBrailleTranslatorProvider = null;

		@Reference(
			name = "TemporaryBrailleTranslatorProvider",
			unbind = "-",
			service = TemporaryBrailleTranslatorProvider.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindTemporaryBrailleTranslatorProvider(TemporaryBrailleTranslatorProvider provider) {
			temporaryBrailleTranslatorProvider = provider;
		}
		
		private TableRegistry tableRegistry;
		
		@Reference(
			name = "TableRegistry",
			unbind = "-",
			service = TableRegistry.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindTableRegistry(TableRegistry registry) {
			tableRegistry = registry;
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(OBFLToPEFStep.class);
	
	private static abstract class OSGiHelper {
		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}
	}
}
