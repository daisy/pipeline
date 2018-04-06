package org.daisy.pipeline.braille.liblouis.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URI;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.memoize;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface LiblouisCSSBlockTransform {
	
	@Component(
		name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisCSSBlockTransform.Provider",
		service = {
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public class Provider extends AbstractTransformProvider<BrailleTranslator> implements BrailleTranslatorProvider<BrailleTranslator> {
		
		private URI href;
		
		@Activate
		private void activate(ComponentContext context, final Map<?,?> properties) {
			href = asURI(context.getBundleContext().getBundle().getEntry("xml/transform/liblouis-block-translate.xpl"));
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
				if (!"liblouis".equals(q.removeOnly("translator").getValue().get()))
					return empty;
			q.add("input", "text-css");
			if (braille)
				q.add("output", "braille");
			Iterable<LiblouisTranslator> translators = logSelect(q, liblouisTranslatorProvider);
			return transform(
				translators,
				new Function<LiblouisTranslator,BrailleTranslator>() {
					public BrailleTranslator _apply(LiblouisTranslator translator) {
						return __apply(
							logCreate(new TransformImpl(translator, htmlOut, locale))
						);
					}
				}
			);
		}
			
		@Override
		public ToStringHelper toStringHelper() {
			return Objects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisCSSBlockTransform$Provider");
		}
		
		private class TransformImpl extends AbstractBrailleTranslator {
			
			private final LiblouisTranslator translator;
			private final XProc xproc;
			
			private TransformImpl(LiblouisTranslator translator, boolean htmlOut, String mainLocale) {
				Map<String,String> options = ImmutableMap.of("text-transform", mutableQuery().add("id", translator.getIdentifier()).toString(),
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
				return Objects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisCSSBlockTransform$Provider$TransformImpl")
					.add("translator", translator);
			}
		}
		
		@Reference(
			name = "LiblouisTranslatorProvider",
			unbind = "unbindLiblouisTranslatorProvider",
			service = LiblouisTranslator.Provider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindLiblouisTranslatorProvider(LiblouisTranslator.Provider provider) {
			liblouisTranslatorProviders.add(provider);
			logger.debug("Adding LiblouisTranslator provider: {}", provider);
		}
	
		protected void unbindLiblouisTranslatorProvider(LiblouisTranslator.Provider provider) {
			liblouisTranslatorProviders.remove(provider);
			liblouisTranslatorProvider.invalidateCache();
			logger.debug("Removing LiblouisTranslator provider: {}", provider);
		}
	
		private List<TransformProvider<LiblouisTranslator>> liblouisTranslatorProviders
		= new ArrayList<TransformProvider<LiblouisTranslator>>();
		private TransformProvider.util.MemoizingProvider<LiblouisTranslator> liblouisTranslatorProvider
		= memoize(dispatch(liblouisTranslatorProviders));
		
		private static final Logger logger = LoggerFactory.getLogger(Provider.class);
		
	}
}
