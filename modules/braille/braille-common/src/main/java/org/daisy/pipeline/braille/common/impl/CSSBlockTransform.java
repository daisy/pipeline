package org.daisy.pipeline.braille.common.impl;

import java.util.Map;
import java.net.URI;
import javax.xml.namespace.QName;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

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
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.calabash.CxEvalBasedTransformer;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.CompoundBrailleTranslator;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TextTransformParser;
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
		}
		
		private final static Iterable<BrailleTranslator> empty = Iterables.<BrailleTranslator>empty();
		
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
			final String brailleCharset = q.containsKey("braille-charset") ? q.getOnly("braille-charset").getValue().get() : null;
			q.add("input", "text-css");
			if (braille)
				q.add("output", "braille");
			Iterable<BrailleTranslator> translators = logSelect(q, translatorRegistry);
			return transform(
				translators,
				new Function<BrailleTranslator,BrailleTranslator>() {
					public BrailleTranslator _apply(BrailleTranslator translator) {
						return __apply(
							logCreate(new TransformImpl(translator, false, brailleCharset, q))
						);
					}
				}
			);
		}
			
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("o.d.p.b.common.impl.CSSBlockTransform$Provider");
		}
		
		private class TransformImpl extends AbstractBrailleTranslator implements XProcStepProvider {
			
			private final Query mainQuery;
			private final BrailleTranslator mainTranslator;
			private final boolean forceMainTranslator;
			private final Map<String,String> options;
			
			/**
			 * @param mainTranslator translator to be used for the parts of the document that do
			 *                       not have their own sub-translator
			 * @param forceMainTranslator if true, the main translator is used even if a default
			 *                            translator has been defined in CSS
			 */
			// FIXME: mainTranslator is optional if default translator has been defined in CSS (which we can not know in advance)
			private TransformImpl(BrailleTranslator mainTranslator, boolean forceMainTranslator,
			                      String brailleCharset, Query query) {
				options = ImmutableMap.of("braille-charset", brailleCharset != null ? brailleCharset : "");
				this.mainTranslator = mainTranslator;
				this.forceMainTranslator = forceMainTranslator;
				mainQuery = query;
			}
			
			@Override
			public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
				return XProcStep.of(
					new SingleInSingleOutXMLTransformer() {
						public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) {
							return () -> {
								if (!(source instanceof SaxonInputValue))
									throw new IllegalArgumentException();
								Mult<SaxonInputValue> mult = ((SaxonInputValue)source).mult(2);
								// analyze the input
								Map<String,Query> subTranslators
									= readTextTransformRules(
										mult.get().ensureSingleItem().asNodeIterator().next(),
										mainQuery);
								BrailleTranslator compoundTranslator;
								Function0<Void> evictTempTranslator; {
									if (subTranslators != null) {
										BrailleTranslator defaultTranslator = mainTranslator;
										Query defaultQuery = subTranslators.remove("auto");
										if (!forceMainTranslator && defaultQuery != null && !defaultQuery.equals(mainQuery))
											defaultTranslator = translatorRegistry.get(defaultQuery).iterator().next();
										compoundTranslator = new CompoundBrailleTranslator(
											defaultTranslator,
											Maps.transformValues(
												subTranslators,
												q -> () -> translatorRegistry.get(q).iterator().next()));
										evictTempTranslator = Provider.this.provideTemporarily(compoundTranslator);
									} else {
										compoundTranslator = mainTranslator;
										evictTempTranslator = Functions.noOp;
									}
								}
								// run the transformation
								new CxEvalBasedTransformer(
									href,
									null,
									ImmutableMap.<String,String>builder()
									            .putAll(options)
									            .put("text-transform",
									                 mutableQuery().add("id", compoundTranslator.getIdentifier()).toString())
									            .build()
								).newStep(runtime, step).transform(
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
				return MoreObjects.toStringHelper("o.d.p.b.common.impl.CSSBlockTransform$Provider$TransformImpl")
					.add("translator", mainTranslator);
			}
		}
		
		private static Map<String,Query> readTextTransformRules(Node doc, Query baseQuery) {
			if (!(doc instanceof Document))
				throw new TransformerException(new IllegalArgumentException());
			String style = (((Document)doc).getDocumentElement()).getAttribute("style");
			URI baseURI = URLs.asURI(((Document)doc).getBaseURI());
			return TextTransformParser.getBrailleTranslatorQueries(style, baseURI, baseQuery);
		}
		
		// FIXME: should ideally bind BrailleTranslatorRegistry, but does not work because
		// BrailleTranslatorRegistry binds BrailleTranslatorProvider, which CSSBlockTransform
		// implements (so there's a cyclic dependency)
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
		
		private BrailleTranslatorRegistry translatorRegistry = new BrailleTranslatorRegistry();
		
		private static final Logger logger = LoggerFactory.getLogger(Provider.class);
		
	}
}
