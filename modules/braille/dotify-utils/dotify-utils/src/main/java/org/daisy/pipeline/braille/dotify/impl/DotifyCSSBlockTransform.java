package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URI;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static org.daisy.common.file.URIs.asURI;
import org.daisy.common.file.URLs;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
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
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.memoize;
import org.daisy.pipeline.braille.dotify.DotifyTranslator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

public interface DotifyCSSBlockTransform {
	
	@Component(
		name = "org.daisy.pipeline.braille.dotify.impl.DotifyCSSBlockTransform.Provider",
		service = {
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<BrailleTranslator> implements BrailleTranslatorProvider<BrailleTranslator> {
		
		private URI href;
		
		@Activate
		protected void activate(final Map<?,?> properties) {
			href = asURI(URLs.getResourceFromJAR("xml/transform/dotify-block-translate.xpl", DotifyCSSBlockTransform.class));
		}
		
		private final static Iterable<BrailleTranslator> empty = Iterables.<BrailleTranslator>empty();
		
		protected Iterable<BrailleTranslator> _get(Query query) {
			final MutableQuery q = mutableQuery(query);
			for (Feature f : q.removeAll("input"))
				if ("html".equals(f.getValue().get())) {}
				else if (!"css".equals(f.getValue().get()))
					return empty;
			boolean braille = false;
			final boolean htmlOut; {
				boolean html = false;
				for (Feature f : q.removeAll("output"))
					if ("css".equals(f.getValue().get())) {}
					else if ("html".equals(f.getValue().get()))
						html = true;
					else if ("braille".equals(f.getValue().get()))
						braille = true;
					else
						return empty;
				htmlOut = html;
			}
			final String locale = q.containsKey("locale") ? q.getOnly("locale").getValue().get() : null;
			if (q.containsKey("translator"))
				if (!"dotify".equals(q.removeOnly("translator").getValue().get()))
					return empty;
			q.add("input", "text-css");
			if (braille)
				q.add("output", "braille");
			Iterable<DotifyTranslator> translators = logSelect(q, dotifyTranslatorProvider);
			return transform(
				translators,
				new Function<DotifyTranslator,BrailleTranslator>() {
					public BrailleTranslator _apply(DotifyTranslator translator) {
						return __apply(
							logCreate(new TransformImpl(q.toString(), translator, htmlOut, locale))
						);
					}
				}
			);
		}
		
		private class TransformImpl extends AbstractBrailleTranslator {
			
			private final DotifyTranslator translator;
			private final XProc xproc;
			
			private TransformImpl(String translatorQuery, DotifyTranslator translator, boolean htmlOut, String mainLocale) {
				Map<String,String> options = ImmutableMap.of("text-transform", translatorQuery,
				                                             // This will omit the <_ style="text-transform:none">
				                                             // wrapper. It is assumed that if (output:html) is set, the
				                                             // result is known to be braille (which is the case if
				                                             // (output:braille) is also set).
				                                             "no-wrap", String.valueOf(htmlOut),
				                                             "main-locale", mainLocale != null ? mainLocale : "");
				xproc = new XProc(href, null, options);
				this.translator = translator;
			}
			
			@Override
			public XProc asXProc() {
				return xproc;
			}
			
			@Override
			public ToStringHelper toStringHelper() {
				return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisCSSBlockTransform$TransformImpl")
					.add("translator", translator);
			}
		}
		
		@Reference(
			name = "DotifyTranslatorProvider",
			unbind = "unbindDotifyTranslatorProvider",
			service = DotifyTranslator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindDotifyTranslatorProvider(DotifyTranslator.Provider provider) {
			dotifyTranslatorProviders.add(provider);
		}
		
		protected void unbindDotifyTranslatorProvider(DotifyTranslator.Provider provider) {
			dotifyTranslatorProviders.remove(provider);
			dotifyTranslatorProvider.invalidateCache();
		}
		
		private List<TransformProvider<DotifyTranslator>> dotifyTranslatorProviders
		= new ArrayList<TransformProvider<DotifyTranslator>>();
		
		private TransformProvider.util.MemoizingProvider<DotifyTranslator> dotifyTranslatorProvider
		= memoize(dispatch(dotifyTranslatorProviders));
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper(DotifyCSSBlockTransform.Provider.class.getName());
		}
	}
}
