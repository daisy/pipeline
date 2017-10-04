package nl.dedicon.pipeline.braille.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URI;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.concat;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.HyphenatorProvider;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.TransformProvider.util.memoize;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.URIs.asURI;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.ComponentContext;

public interface DediconTranslator {
	
	@Component(
		name = "nl.dedicon.pipeline.braille.impl.DediconTranslator.Provider",
		service = {
			TransformProvider.class,
			BrailleTranslatorProvider.class,
		}
	)
	public class Provider extends AbstractTransformProvider<BrailleTranslator> implements BrailleTranslatorProvider<BrailleTranslator> {
		
		private URI href;
		
		@Activate
		private void activate(ComponentContext context, final Map<?,?> properties) {
			href = asURI(context.getBundleContext().getBundle().getEntry("xml/xproc/block-translate.xpl"));
		}
		
		private final static Query liblouisTable = mutableQuery()
			.add("liblouis-table", "http://www.dedicon.nl/liblouis/nl-NL-g0.utb,http://www.dedicon.nl/liblouis/undefined.utb");
		private final static Query hyphenTable = mutableQuery().add("libhyphen-table", "http://www.libreoffice.org/dictionaries/hyphen/hyph_nl_NL.dic");
		private final static Query fallbackHyphenationTable = mutableQuery().add("hyphenator", "tex").add("locale", "nl-NL");
		
		private final static Iterable<BrailleTranslator> empty = Iterables.<BrailleTranslator>empty();
		
		private final static List<String> supportedInput = ImmutableList.of("css","text-css","dtbook","html");
		private final static List<String> supportedOutput = ImmutableList.of("css","braille");
		
		/**
		 * Recognized features:
		 *
		 * - translator: Will only match if the value is `dedicon'.
		 * - locale: Will only match if the language subtag is 'nl'.
		 *
		 */
		protected final Iterable<BrailleTranslator> _get(Query query) {
			final MutableQuery q = mutableQuery(query);
			for (Feature f : q.removeAll("input"))
				if (!supportedInput.contains(f.getValue().get()))
					return empty;
			for (Feature f : q.removeAll("output"))
				if (!supportedOutput.contains(f.getValue().get()))
					return empty;
			if (q.containsKey("locale"))
				if (!"nl".equals(parseLocale(q.removeOnly("locale").getValue().get()).getLanguage()))
					return empty;
			if (q.containsKey("translator"))
				if ("dedicon".equals(q.removeOnly("translator").getValue().get()))
					if (q.isEmpty()) {
						Iterable<Hyphenator> hyphenators = concat(
							logSelect(hyphenTable, hyphenatorProvider),
							logSelect(fallbackHyphenationTable, hyphenatorProvider));
						return concat(
							transform(
								concat(
									concat(
										transform(
											hyphenators,
											new Function<Hyphenator,String>() {
												public String _apply(Hyphenator h) {
													return h.getIdentifier(); }}),
										"liblouis"),
									"none"),
								new Function<String,Iterable<BrailleTranslator>>() {
									public Iterable<BrailleTranslator> _apply(String hyphenator) {
										final Query translatorQuery = mutableQuery(liblouisTable).add("hyphenator", hyphenator);
										return transform(
											logSelect(translatorQuery, liblouisTranslatorProvider),
											new Function<LiblouisTranslator,BrailleTranslator>() {
												public BrailleTranslator _apply(LiblouisTranslator translator) {
													return __apply(logCreate(new TransformImpl(translatorQuery.toString(), translator))); }}); }})); }
				return empty;
		}
		
		private class TransformImpl extends AbstractBrailleTranslator {
			
			private final FromStyledTextToBraille translator;
			private final XProc xproc;
			
			private TransformImpl(String translatorQuery, LiblouisTranslator translator) {
				Map<String,String> options = ImmutableMap.of("text-transform", mutableQuery().add("id", this.getIdentifier()).toString());
				xproc = new XProc(href, null, options);
				this.translator = translator.fromStyledTextToBraille();
			}
			
			@Override
			public XProc asXProc() {
				return xproc;
			}
			
			@Override
			public FromStyledTextToBraille fromStyledTextToBraille() {
				return fromStyledTextToBraille;
			}
			
			private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
				@Override
				public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText) {
					return translator.transform(styledText);
				}
			};
			
			@Override
			public String toString() {
				return Objects.toStringHelper(DediconTranslator.class.getSimpleName())
					.add("id", getIdentifier())
					.add("liblouisTranslator", translator)
					.toString();
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
		}
	
		protected void unbindLiblouisTranslatorProvider(LiblouisTranslator.Provider provider) {
			liblouisTranslatorProviders.remove(provider);
			liblouisTranslatorProvider.invalidateCache();
		}
	
		private List<TransformProvider<LiblouisTranslator>> liblouisTranslatorProviders
		= new ArrayList<TransformProvider<LiblouisTranslator>>();
		private TransformProvider.util.MemoizingProvider<LiblouisTranslator> liblouisTranslatorProvider
		= memoize(dispatch(liblouisTranslatorProviders));
		
		@Reference(
			name = "HyphenatorProvider",
			unbind = "unbindHyphenatorProvider",
			service = HyphenatorProvider.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		@SuppressWarnings(
			"unchecked" // safe cast to TransformProvider<Hyphenator>
		)
		protected void bindHyphenatorProvider(HyphenatorProvider<?> provider) {
			hyphenatorProviders.add((TransformProvider<Hyphenator>)provider);
		}
	
		protected void unbindHyphenatorProvider(HyphenatorProvider<?> provider) {
			hyphenatorProviders.remove(provider);
			hyphenatorProvider.invalidateCache();
		}
	
		private List<TransformProvider<Hyphenator>> hyphenatorProviders
		= new ArrayList<TransformProvider<Hyphenator>>();
		private TransformProvider.util.MemoizingProvider<Hyphenator> hyphenatorProvider
		= memoize(dispatch(hyphenatorProviders));
		
	}
}
