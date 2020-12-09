package org.daisy.pipeline.braille.liblouis.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URI;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.file.URLs;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.calabash.CxEvalBasedTransformer;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.memoize;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see <a href="../../../../../../../README.md">Documentation</a>
 */
public interface LiblouisCSSStyledDocumentTransform {
	
	@Component(
		name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisCSSStyledDocumentTransform.Provider",
		service = {
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<Transform> {
		
		private URI href;
			
		@Activate
		protected void activate(final Map<?,?> properties) {
			href = URLs.asURI(URLs.getResourceFromJAR("xml/transform/liblouis-transform.xpl", LiblouisCSSStyledDocumentTransform.class));
		}
		
		private final static Iterable<Transform> empty = Iterables.<Transform>empty();
		
		private final static List<String> supportedOutput = ImmutableList.of("braille","pef");
		
		protected Iterable<Transform> _get(final Query query) {
			final MutableQuery q = mutableQuery(query);
			try {
				if ("css".equals(q.removeOnly("input").getValue().get())) {
					for (Feature f : q.removeAll("output"))
						if (!supportedOutput.contains(f.getValue().get()))
							return empty;
					if (query.containsKey("formatter"))
						if (!"liblouis".equals(q.removeOnly("formatter").getValue().get()))
							return empty;
					q.add("input", "css")
					 .add("output", "css");
					Iterable<BrailleTranslator> blockTransforms = logSelect(q, brailleTranslatorProvider);
					return transform(
						blockTransforms,
						new Function<BrailleTranslator,Transform>() {
							public Transform _apply(BrailleTranslator blockTransform) {
								return __apply(
									logCreate(new TransformImpl(blockTransform))); }}); }}
			catch (IllegalStateException e) {}
			return empty;
		}
		
		private class TransformImpl extends AbstractTransform implements XProcStepProvider {
			
			private final BrailleTranslator blockTransform;
			private final Map<String,String> options;
			
			private TransformImpl(BrailleTranslator blockTransform) {
				options = ImmutableMap.of("block-transform",
				                          mutableQuery().add("id", blockTransform.getIdentifier()).toString());
				this.blockTransform = blockTransform;
			}
			
			@Override
			public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
				return new CxEvalBasedTransformer(href, null, options).newStep(runtime, step);
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisCSSStyledDocumentTransform$Provider$TransformImpl")
					.add("blockTransform", blockTransform);
			}
		}
		
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
		public void bindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
			brailleTranslatorProviders.add((BrailleTranslatorProvider<BrailleTranslator>)provider);
		}
		
		public void unbindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
			brailleTranslatorProviders.remove(provider);
			brailleTranslatorProvider.invalidateCache();
		}
	
		private List<BrailleTranslatorProvider<BrailleTranslator>> brailleTranslatorProviders
		= new ArrayList<BrailleTranslatorProvider<BrailleTranslator>>();
		
		private TransformProvider.util.MemoizingProvider<BrailleTranslator> brailleTranslatorProvider
		= memoize(dispatch(brailleTranslatorProviders));
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper(LiblouisCSSStyledDocumentTransform.Provider.class.getName());
		}
		
		private static final Logger logger = LoggerFactory.getLogger(Provider.class);
		
	}
}
