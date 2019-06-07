package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URI;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableMap;

import static org.daisy.common.file.URIs.asURI;
import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
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

public interface DotifyCSSStyledDocumentTransform {
	
	@Component(
		name = "org.daisy.pipeline.braille.dotify.impl.DotifyCSSStyledDocumentTransform.Provider",
		service = {
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<Transform> {
		
		private URI href;
		
		@Activate
		protected void activate(final Map<?,?> properties) {
			href = asURI(URLs.getResourceFromJAR("xml/transform/dotify-transform.xpl", DotifyCSSStyledDocumentTransform.class));
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
					final MutableQuery blockTransformQuery = mutableQuery(q).add("input", "css").add("output", "css");
					if (obfl && braille)
						blockTransformQuery.add("output", "braille");
					Iterable<BrailleTranslator> blockTransforms = logSelect(blockTransformQuery, brailleTranslatorProvider);
					final String textTransformQuery = mutableQuery(q).add("input", "text-css").add("output", "braille").toString();
					final boolean _obfl = obfl;
					return transform(
						blockTransforms,
						new Function<BrailleTranslator,Transform>() {
							public Transform _apply(BrailleTranslator blockTransform) {
								return __apply(
									logCreate(new TransformImpl(_obfl,
									                            blockTransformQuery.toString(),
									                            blockTransform,
									                            textTransformQuery))); }}); }}
			catch (IllegalStateException e) {}
			return empty;
		}
		
		private class TransformImpl extends AbstractTransform {
			
			private final String output;
			private final BrailleTranslator blockTransform;
			private final XProc xproc;
			
			private TransformImpl(boolean obfl,
			                      String blockTransformQuery,
			                      BrailleTranslator blockTransform,
			                      String textTransformQuery) {
				this.output = obfl ? "obfl" : "pef";
				Map<String,String> options = ImmutableMap.of(
					"output", this.output,
					"css-block-transform", blockTransformQuery,
					"text-transform", textTransformQuery);
				xproc = new XProc(href, null, options);
				this.blockTransform = blockTransform;
			}
			
			@Override
			public XProc asXProc() {
				return xproc;
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return MoreObjects.toStringHelper("o.d.p.b.dotify.impl.DotifyCSSStyledDocumentTransform$Provider$TransformImpl")
					.add("output", output)
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
			return MoreObjects.toStringHelper(DotifyCSSStyledDocumentTransform.Provider.class.getName());
		}
	}
}
