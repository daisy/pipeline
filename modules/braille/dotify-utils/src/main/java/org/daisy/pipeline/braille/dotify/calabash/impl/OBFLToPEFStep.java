package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.Maps;

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

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.daisy.dotify.api.engine.FormatterEngine;
import org.daisy.dotify.api.engine.FormatterEngineFactoryService;
import org.daisy.dotify.api.formatter.FormatterConfiguration;
import org.daisy.dotify.api.writer.MediaTypes;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriter;
import org.daisy.dotify.api.writer.PagedMediaWriterConfigurationException;
import org.daisy.dotify.api.writer.PagedMediaWriterFactoryService;

import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.util.Function0;
import org.daisy.pipeline.braille.common.util.Functions;
import org.daisy.pipeline.braille.css.CompoundTranslator;
import org.daisy.pipeline.braille.css.TextTransformParser;

import org.osgi.framework.FrameworkUtil;

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
	private static final QName _identifier = new QName("identifier");
	private static final QName _style_type = new QName("style-type");
	private static final QName _css_text_transform_definitions = new QName("css-text-transform-definitions");
	
	/** Code for Dotify formatting errors caused by invalid input or input that can not be handled. */
	private static final QName DOTIFY_ERROR = new QName("DOTIFY");

	private ReadablePipe source = null;
	private WritablePipe result = null;
	private final Map<String,String> params = new HashMap<String,String>();
	
	private final Iterable<PagedMediaWriterFactoryService> writerFactoryServices;
	private final Iterable<FormatterEngineFactoryService> engineFactoryServices;
	private final org.daisy.pipeline.braille.common.Provider<Query,BrailleTranslator> brailleTranslatorProvider;
	private final TemporaryBrailleTranslatorProvider temporaryBrailleTranslatorProvider;
	
	public OBFLToPEFStep(XProcRuntime runtime,
	                     XAtomicStep step,
	                     Iterable<PagedMediaWriterFactoryService> writerFactoryServices,
	                     Iterable<FormatterEngineFactoryService> engineFactoryServices,
	                     org.daisy.pipeline.braille.common.Provider<Query,BrailleTranslator> brailleTranslatorProvider,
	                     TemporaryBrailleTranslatorProvider temporaryBrailleTranslatorProvider) {
		super(runtime, step);
		this.writerFactoryServices = writerFactoryServices;
		this.engineFactoryServices = engineFactoryServices;
		this.brailleTranslatorProvider = brailleTranslatorProvider;
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
			
			if ("text/css".equals(styleType)) {
				
				// We're assuming that no other translators than the DAISY Pipeline implementations
				// support text/css.
				Query mainQuery;
				try {
					mainQuery = query(mode);
				} catch (Exception e) {
					throw new IllegalArgumentException("Expected mode in query format: " + mode, e);
				}
				if (locale != null && !"und".equals(locale))
					mainQuery = mutableQuery(mainQuery).add("locale", locale);
				BrailleTranslator mainTranslator;
				try {
					mainTranslator = brailleTranslatorProvider.get(mainQuery).iterator().next();
				} catch (NoSuchElementException e) {
					throw new XProcException(
						step.getNode(),
						"No translator available for mode '" + mode + "' and locale '" + locale + "' "
						+ "that supports style type " + styleType);
				}
				String textTransformDefinitions = getOption(_css_text_transform_definitions, "");
				if (!"".equals(textTransformDefinitions)) {
					Map<String,Query> subQueries = TextTransformParser.getBrailleTranslatorQueries(textTransformDefinitions,
					                                                                               obflNode.getBaseURI(),
					                                                                               mainQuery);
					if (subQueries != null && !subQueries.isEmpty()) {
						BrailleTranslator defaultTranslator = mainTranslator;
						Query defaultQuery = subQueries.remove("auto");
						if (defaultQuery != null && !defaultQuery.equals(mainQuery))
							try {
								defaultTranslator = brailleTranslatorProvider.get(defaultQuery).iterator().next();
							} catch (NoSuchElementException e) {
								throw new XProcException(
									step.getNode(), "No translator available for " + defaultQuery + "");
							}
						if (defaultTranslator != mainTranslator || !subQueries.isEmpty()) {
							Map<String,Supplier<BrailleTranslator>> subTranslators
								= Maps.transformValues(
									subQueries,
									q -> () -> brailleTranslatorProvider.get(q).iterator().next());
							BrailleTranslator compoundTranslator = new CompoundTranslator(defaultTranslator, subTranslators);
							evictTempTranslator = temporaryBrailleTranslatorProvider.provideTemporarily(compoundTranslator);
							mode = mutableQuery().add("id", compoundTranslator.getIdentifier()).toString();
							locale = "und";
						}
					}
				}
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
			// FIXME: duplication with DotifyTaskSystem! => use that class in
			// here (like in XMLToOBFL) when it supports setting of the mode
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
			boolean allowEndingVolumeOnHyphen; {
				String p = params.get("allow-ending-volume-on-hyphen");
				allowEndingVolumeOnHyphen = (p == null) ? true : p.equalsIgnoreCase("true"); }
			FormatterConfiguration.Builder config = FormatterConfiguration.with(locale, mode)
				.markCapitalLetters(markCapitalLetters)
				.hyphenate(hyphenate)
				.allowsTextOverflowTrimming(allowTextOverflowTrimming)
				.allowsEndingVolumeOnHyphen(allowEndingVolumeOnHyphen);
			if (removeStyles)
				config.ignoreStyle("em").ignoreStyle("strong");
			
			FormatterEngine engine = newFormatterEngine(config.build(), identifier);
			s = new ByteArrayOutputStream();
			engine.convert(obflStream, s);
			obflStream.close();
			InputStream pefStream = new ByteArrayInputStream(s.toByteArray());
			s.close();
			
			// Write PEF
			result.write(runtime.getProcessor().newDocumentBuilder().build(new StreamSource(pefStream)));
			pefStream.close(); }
		
		catch (Throwable e) {
			if (e.getMessage().contains("Failed to solve table"))
				throw new XProcException(DOTIFY_ERROR, step, e);
			throw XProcStep.raiseError(e, step); }
		finally {
			evictTempTranslator.apply(); }
	}
	
	private PagedMediaWriter newPagedMediaWriter(String target) throws PagedMediaWriterConfigurationException {
		target = target.toLowerCase();
		for (PagedMediaWriterFactoryService s : writerFactoryServices)
			if (s.supportsMediaType(target))
				return s.newFactory(target).newPagedMediaWriter();
		throw new RuntimeException("Cannot find a PagedMediaWriter factory for " + target);
	}
	
	private PagedMediaWriter newPEFWriter(String identifier) throws PagedMediaWriterConfigurationException {
		PagedMediaWriter ret = newPagedMediaWriter(MediaTypes.PEF_MEDIA_TYPE);
		if (!"".equals(identifier)) {
			List<MetaDataItem> meta = new ArrayList<MetaDataItem>();
			javax.xml.namespace.QName name = new javax.xml.namespace.QName("http://purl.org/dc/elements/1.1/", "identifier", "dc");
			meta.add(new MetaDataItem(name, identifier));
			ret.prepare(meta);
		}
		return ret;
	}
	
	private FormatterEngine newFormatterEngine(FormatterConfiguration config, PagedMediaWriter writer) {
		return engineFactoryServices.iterator().next().newFormatterEngine(config, writer);
	}
	
	private FormatterEngine newFormatterEngine(FormatterConfiguration config, String identifier) throws PagedMediaWriterConfigurationException {
		return newFormatterEngine(config, newPEFWriter(identifier));
	}
	
	@Component(
		name = "pxi:obfl-to-pef",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}obfl-to-pef" }
	)
	public static class Provider implements XProcStepProvider  {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new OBFLToPEFStep(runtime,
			                         step,
			                         writerFactoryServices,
			                         engineFactoryServices,
			                         brailleTranslatorProvider,
			                         temporaryBrailleTranslatorProvider);
		}
		
		private final List<PagedMediaWriterFactoryService> writerFactoryServices
			= new ArrayList<PagedMediaWriterFactoryService>();
		
		@Reference(
			name = "PagedMediaWriterFactoryService",
			unbind = "unbindPagedMediaWriterFactoryService",
			service = PagedMediaWriterFactoryService.class,
			cardinality = ReferenceCardinality.AT_LEAST_ONE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindPagedMediaWriterFactoryService(PagedMediaWriterFactoryService service) {
			if (!OSGiHelper.inOSGiContext())
				service.setCreatedWithSPI();
			writerFactoryServices.add(service);
		}
		
		protected void unbindPagedMediaWriterFactoryService(PagedMediaWriterFactoryService service) {
			writerFactoryServices.remove(service);
		}
		
		private final List<FormatterEngineFactoryService> engineFactoryServices
			= new ArrayList<FormatterEngineFactoryService>();
		
		@Reference(
			name = "FormatterEngineFactoryService",
			unbind = "unbindFormatterEngineFactoryService",
			service = FormatterEngineFactoryService.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindFormatterEngineFactoryService(FormatterEngineFactoryService service) {
			if (!OSGiHelper.inOSGiContext())
				service.setCreatedWithSPI();
			engineFactoryServices.add(service);
		}
		
		protected void unbindFormatterEngineFactoryService(FormatterEngineFactoryService service) {
			engineFactoryServices.remove(service);
		}
		
		private final List<BrailleTranslatorProvider<BrailleTranslator>> brailleTranslatorProviders
			= new ArrayList<BrailleTranslatorProvider<BrailleTranslator>>();
		
		private final org.daisy.pipeline.braille.common.Provider.util.MemoizingProvider<Query,BrailleTranslator> brailleTranslatorProvider
			= memoize(dispatch(brailleTranslatorProviders));
		
		@Reference(
			name = "BrailleTranslatorProvider",
			unbind = "unbindBrailleTranslatorProvider",
			service = BrailleTranslatorProvider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		@SuppressWarnings(
			"unchecked" // safe cast to BrailleTranslatorProvider<BrailleTranslator>
		)
		protected void bindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
			brailleTranslatorProviders.add((BrailleTranslatorProvider<BrailleTranslator>)provider);
		}

		protected void unbindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
			brailleTranslatorProviders.remove(provider);
			brailleTranslatorProvider.invalidateCache();
		}

		private TemporaryBrailleTranslatorProvider temporaryBrailleTranslatorProvider = null;

		@Reference(
			name = "TemporaryBrailleTranslatorProvider",
			service = TemporaryBrailleTranslatorProvider.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindTemporaryBrailleTranslatorProvider(TemporaryBrailleTranslatorProvider provider) {
			temporaryBrailleTranslatorProvider = provider;
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
