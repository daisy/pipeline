package org.daisy.pipeline.braille.dotify.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.file.URLs;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.Mult;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.dotify.api.table.Table;
import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.calabash.CxEvalBasedTransformer;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorRegistry;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.pef.TableRegistry;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import static org.slf4j.helpers.NOPLogger.NOP_LOGGER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see <a href="../../../../../../../../../doc/">User documentation</a>.
 */
public interface DotifyCSSStyledDocumentTransform {
	
	@Component(
		name = "org.daisy.pipeline.braille.dotify.impl.DotifyCSSStyledDocumentTransform.Provider",
		service = {
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<Transform> {
		
		private static final Logger logger = LoggerFactory.getLogger(DotifyCSSStyledDocumentTransform.class);
		
		private URI href;
		
		@Activate
		protected void activate(final Map<?,?> properties) {
			href = URLs.asURI(URLs.getResourceFromJAR("xml/transform/dotify-transform.xpl", DotifyCSSStyledDocumentTransform.class));
		}
		
		private final static Iterable<Transform> empty = Iterables.<Transform>empty();
		
		protected Iterable<Transform> _get(Query query) {
			final MutableQuery q = mutableQuery(query);
			try {
				if ("css".equals(q.removeOnly("input").getValue().get())) {
					if (q.containsKey("formatter"))
						if (!"dotify".equals(q.removeOnly("formatter").getValue().get()))
							return empty;
					boolean braille = false;
					boolean pef = false;
					boolean obfl = false;
					for (Feature f : q.removeAll("output"))
						if ("pef".equals(f.getValue().get()))
							pef = true;
						else if ("obfl".equals(f.getValue().get()))
							obfl = true;
						else if ("braille".equals(f.getValue().get()))
							braille = true;
						else
							return empty;
					if ((pef && obfl) || !(pef || obfl))
						return empty;
					boolean forcePretranslation = false;
					if (q.containsKey("force-pre-translation")) {
						forcePretranslation = true;
						q.removeOnly("force-pre-translation");
					}
					Query textTransformQuery = mutableQuery(q).add("input", "text-css").add("output", "braille");
					if (logSelect(textTransformQuery, translatorRegistry).apply(NOP_LOGGER).iterator().hasNext()) {
						MutableQuery blockTransformQuery = null; {
							// only pre-translate if an intermediary OBFL with braille content is requested
							if (obfl && braille || forcePretranslation) {
								blockTransformQuery = mutableQuery(q).add("input", "css")
								                                     .add("output", "css")
								                                     .add("output", "braille");
								if (!logSelect(blockTransformQuery, translatorRegistry).apply(NOP_LOGGER).iterator().hasNext())
									return empty;
							}
						}
						return AbstractTransformProvider.util.Iterables.of(
							logCreate(new TransformImpl(obfl, blockTransformQuery, textTransformQuery))); }}}
			catch (IllegalStateException e) {}
			return empty;
		}
		
		private class TransformImpl extends AbstractTransform implements XProcStepProvider {
			
			private final String output;
			private final Query blockTransformQuery;
			private final Query textTransformQuery;
			private final Map<String,String> options;
			
			private TransformImpl(boolean obfl,
			                      Query blockTransformQuery,
			                      Query textTransformQuery) {
				MutableQuery mode = mutableQuery(textTransformQuery);
				String locale = "und";
				if (mode.containsKey("document-locale"))
					locale = mode.removeOnly("document-locale").getValue().get();
				// (input:text-css) is assumed
				Iterator<Feature> input = mode.get("input").iterator();
				while (input.hasNext())
					if ("text-css".equals(input.next().getValue().get()))
						input.remove();
				// (output:braille) is assumed
				Iterator<Feature> output = mode.get("output").iterator();
				while (output.hasNext())
					if ("braille".equals(output.next().getValue().get()))
						output.remove();
				this.output = obfl ? "obfl" : "pef";
				options = ImmutableMap.of(
					"output", this.output,
					"css-block-transform", blockTransformQuery != null ? blockTransformQuery.toString() : "",
					"document-locale", locale,
					"text-transform", mode.toString());
				this.blockTransformQuery = blockTransformQuery;
				this.textTransformQuery = textTransformQuery;
			}
			
			@Override
			public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
				return XProcStep.of(
					new SingleInSingleOutXMLTransformer() {
						public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) {
							return () -> {
								Map<String,String> options = TransformImpl.this.options;
								InputValue<?> paramsCopy = params;
								// get value of preview-table parameter
								try {
									Mult<? extends InputValue<?>> m = params.mult(2); // multiply params input
									paramsCopy = m.get();
									Map map = m.get().asObject(Map.class);
									Object v = map.get(new QName("preview-table"));
									if (v != null)
										if (v instanceof InputValue) {
											m = ((InputValue<?>)v).mult(2); // multiply preview-table value
											map.put(new QName("preview-table"), m.get());
											try {
												v = m.get().asObject();
												if (v instanceof String) {
													try {
														Table previewTable = tableRegistry.get(query((String)v)).iterator().next();
														// Check if the output charset of the braille translator can be
														// set to this table. If not, ignore preview-table.
														MutableQuery q = mutableQuery(textTransformQuery).add("braille-charset",
														                                                      previewTable.getIdentifier());
														if (logSelect(q, translatorRegistry).apply(NOP_LOGGER).iterator().hasNext()) {
															q.removeAll("document-locale");
															options = new HashMap<>(); {
																options.putAll(TransformImpl.this.options);
																options.put("css-block-transform",
																            blockTransformQuery != null
																                ? mutableQuery(blockTransformQuery)
																                      .add("braille-charset", previewTable.getIdentifier())
																                      .toString()
																                : "");
																options.put("braille-charset", previewTable.getIdentifier());
															}
														}
													} catch (NoSuchElementException e) {
													}
												}
											} catch (UnsupportedOperationException e) {
											}
										}
								} catch (UnsupportedOperationException e) {
								}
								new CxEvalBasedTransformer(
									href,
									null,
									options
								).newStep(runtime, step, monitor, properties).transform(
									ImmutableMap.of(
										new QName("source"), source,
										new QName("parameters"), paramsCopy),
									ImmutableMap.of(
										new QName("result"), result)
								).run();
							};
						}
					},
					runtime,
					step
				);
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return MoreObjects.toStringHelper("DotifyCSSStyledDocumentTransform$Provider$TransformImpl")
					.add("output", output)
					.add("textTransform", textTransformQuery);
			}
		}
		
		@Reference(
			name = "BrailleTranslatorRegistry",
			unbind = "-",
			service = BrailleTranslatorRegistry.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void bindBrailleTranslatorRegistry(BrailleTranslatorRegistry registry) {
			translatorRegistry = registry.withContext(logger);
		}
		
		private BrailleTranslatorRegistry translatorRegistry;
		
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
		
		private TableRegistry tableRegistry;
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("DotifyCSSStyledDocumentTransform$Provider");
		}
	}
}
