package org.daisy.pipeline.braille.common.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.file.URLs;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.Mult;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.xproc.calabash.XProcBasedTransformer;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.HyphenatorRegistry;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.common.util.Function0;
import org.daisy.pipeline.braille.common.util.Functions;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @see <a href="../../../../../../../README.md">Documentation</a>
 * @see <a href="../../../../../../../resources/xml/block-translator.xpl">XProc code</a>
 */
public interface CSSBlockTransform {
	
	@Component(
		name = "org.daisy.pipeline.braille.common.impl.CSSBlockTransform.Provider",
		service = {
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<BrailleTranslator> implements BrailleTranslatorProvider<BrailleTranslator> {
		
		private URI href;
		
		@Activate
		protected void activate(final Map<?,?> properties) {
			href = URLs.asURI(URLs.getResourceFromJAR("xml/block-translator.xpl", CSSBlockTransform.class));
			translatorRegistry = translatorRegistry.withContext(logger);
		}
		
		private final static Iterable<BrailleTranslator> empty = Iterables.<BrailleTranslator>empty();
		
		@Override
		protected Iterable<BrailleTranslator> _get(Query query) {
			final MutableQuery q = mutableQuery(query);
			for (Feature f : q.removeAll("input"))
				if ("html".equals(f.getValue().get())) {}
				else if (!"css".equals(f.getValue().get()))
					return empty;
			boolean braille = false; {
				for (Feature f : q.removeAll("output"))
					if ("css".equals(f.getValue().get())) {}
					else if ("html".equals(f.getValue().get())) {}
					else if ("braille".equals(f.getValue().get()))
						braille = true;
					else
						return empty;
			}
			final String brailleCharset = q.containsKey("braille-charset")
				? q.getOnly("braille-charset").getValue().get()
				: null;
			final boolean includeBrailleCodeInLanguage = q.containsKey("include-braille-code-in-language")
				? q.getOnly("include-braille-code-in-language").getValue().orElse("true").equalsIgnoreCase("true")
				: false;
			q.add("input", "text-css");
			if (braille)
				q.add("output", "braille");
			return transform(
				logSelect(q, translatorRegistry.getWithHyphenator(q)),
				new Function<BrailleTranslator,BrailleTranslator>() {
					public BrailleTranslator _apply(BrailleTranslator translator) {
						return __apply(
							logCreate(new TransformImpl(translator, false, brailleCharset,
							                            includeBrailleCodeInLanguage, q))
						);
					}
				}
			);
		}
			
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("CSSBlockTransform$Provider");
		}
		
		private class TransformImpl extends AbstractBrailleTranslator implements XProcStepProvider {
			
			private final Query mainQuery;
			private final BrailleTranslator mainTranslator;
			private final boolean forceMainTranslator;
			private final Map<String,String> options;
			private final String brailleCharset;
			
			/**
			 * @param mainTranslator translator to be used for the parts of the document that do
			 *                       not have their own sub-translator
			 * @param forceMainTranslator if true, the main translator is used even if a default
			 *                            translator has been defined in CSS
			 */
			// FIXME: mainTranslator is optional if default translator has been defined in CSS (which we can not know in advance)
			private TransformImpl(BrailleTranslator mainTranslator, boolean forceMainTranslator,
			                      String brailleCharset, boolean includeBrailleCodeInLanguage, Query query) {
				options = ImmutableMap.of("braille-charset", brailleCharset != null ? brailleCharset : "",
				                          "include-braille-code-in-language", "" + includeBrailleCodeInLanguage);
				this.mainTranslator = mainTranslator;
				this.forceMainTranslator = forceMainTranslator;
				mainQuery = query;
				this.brailleCharset = brailleCharset;
			}
			
			private Hyphenator hyphenator = null;
			
			private TransformImpl(TransformImpl from, BrailleTranslator mainTranslator) {
				super(from);
				this.mainQuery = from.mainQuery;
				this.mainTranslator = mainTranslator;
				this.forceMainTranslator = from.forceMainTranslator;
				this.options = from.options;
				this.brailleCharset = from.brailleCharset;
			}
			
			/**
			 * @throws UnsupportedOperationException if {@code mainTranslator.withHyphenator()} throws
			 *                                       UnsupportedOperationException
			 */
			@Override
			public TransformImpl _withHyphenator(Hyphenator hyphenator) throws UnsupportedOperationException {
				TransformImpl t = new TransformImpl(this, mainTranslator.withHyphenator(hyphenator));
				Provider.this.rememberId(t);
				return t;
			}
			
			@Override
			public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
				return XProcStep.of(
					new SingleInSingleOutXMLTransformer() {
						public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) {
							return () -> {
								if (!(source instanceof SaxonInputValue))
									throw new IllegalArgumentException();
								Mult<SaxonInputValue> mult = ((SaxonInputValue)source).mult(2);

								// analyze the input
								Node doc = mult.get().ensureSingleItem().asNodeIterator().next();
								if (!(doc instanceof Document))
									throw new TransformerException(new IllegalArgumentException());
								String style = (((Document)doc).getDocumentElement()).getAttribute("style");
								URI styleBaseURI = URLs.asURI(((Document)doc).getBaseURI());
								BrailleTranslator compoundTranslator
									= translatorRegistry.getWithHyphenator(mainQuery, style, styleBaseURI, forceMainTranslator)
									                    .iterator().next();
								if (hyphenator != null)
									compoundTranslator = compoundTranslator.withHyphenator(hyphenator);
								Function0<Void> evictTempTranslator; {
									if (compoundTranslator != mainTranslator)
										// translatorRegistry.get() call above probably returned an object that was not cached
										evictTempTranslator = Provider.this.provideTemporarily(compoundTranslator);
									else
										evictTempTranslator = Functions.noOp;
								}

								// run the transformation
								Map<QName,InputValue<?>> options = new HashMap<>();
								for (String option : TransformImpl.this.options.keySet())
									options.put(new QName(option), new InputValue<>(TransformImpl.this.options.get(option)));
								options.put(new QName("text-transform"),
								            new InputValue<>(
									            mutableQuery().add("id", compoundTranslator.getIdentifier()).toString()));
								new XProcBasedTransformer(
									href,
									options
								).newStep(runtime, step, monitor, properties).transform(
									ImmutableMap.of(
										new QName("source"), mult.get(),
										new QName("parameters"), params),
									ImmutableMap.of(
										new QName("result"), result)
								).run();
								evictTempTranslator.apply();
							};
						}
					},
					runtime,
					step
				);
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return MoreObjects.toStringHelper("CSSBlockTransform$Provider$TransformImpl")
					.add("translator", mainTranslator);
			}
		}
		
		// FIXME: should ideally bind BrailleTranslatorRegistry, but that does not work because
		// BrailleTranslatorRegistry binds BrailleTranslatorProvider, which CSSBlockTransform
		// implements, so there would be a cyclic dependency. Note that this is very brittle,
		// because there will be a cyclic dependency anyway if at least one other
		// BrailleTranslatorProvider would bind BrailleTranslatorProvider.

		@Reference(
			name = "BrailleTranslatorProvider",
			unbind = "-",
			service = BrailleTranslatorProvider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.STATIC
		)
		protected void bindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
			translatorRegistry.addProvider(provider);
		}
		
		@Reference(
			name = "HyphenatorRegistry",
			unbind = "-",
			service = HyphenatorRegistry.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindHyphenatorRegistry(HyphenatorRegistry registry) {
			translatorRegistry.bindHyphenatorRegistry(registry);
		}
		
		private BrailleTranslatorRegistry translatorRegistry = new BrailleTranslatorRegistry();
		
		private static final Logger logger = LoggerFactory.getLogger(Provider.class);
		
	}
}
