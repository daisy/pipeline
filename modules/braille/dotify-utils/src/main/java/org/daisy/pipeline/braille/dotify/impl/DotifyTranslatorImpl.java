package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import static com.google.common.collect.Iterables.size;

import cz.vutbr.web.css.CSSProperty;

import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.BrailleFilterFactoryService;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorMode;
import org.daisy.dotify.api.translator.TranslatorType;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.concat;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
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
import static org.daisy.pipeline.braille.common.TransformProvider.util.varyLocale;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import org.daisy.pipeline.braille.dotify.DotifyTranslator;

import org.osgi.framework.FrameworkUtil;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DotifyTranslatorImpl extends AbstractBrailleTranslator implements DotifyTranslator {
	
	private final BrailleFilter filter;
	private final boolean hyphenating;
	private final Hyphenator externalHyphenator;
	
	protected DotifyTranslatorImpl(BrailleFilter filter, boolean hyphenating) {
		this.filter = filter;
		this.hyphenating = hyphenating;
		this.externalHyphenator = null;
	}
	
	protected DotifyTranslatorImpl(BrailleFilter filter, Hyphenator externalHyphenator) {
		this.filter = filter;
		this.hyphenating = true;
		this.externalHyphenator = externalHyphenator;
	}
	
	public BrailleFilter asBrailleFilter() {
		return filter;
	}
	
	@Override
	public FromStyledTextToBraille fromStyledTextToBraille() {
		return fromStyledTextToBraille;
	}
	
	private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
		public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
			int size = size(styledText);
			if (to < 0) to = size;
			String[] braille = new String[to - from];
			int i = 0;
			for (CSSStyledText t : styledText) {
				if (i >= from && i < to)
					braille[i - from] = DotifyTranslatorImpl.this.transform(t.getText(), t.getStyle());
				i++; }
			return Arrays.asList(braille);
		}
	};
	
	@Override
	public LineBreakingFromStyledText lineBreakingFromStyledText() {
		return lineBreakingFromStyledText;
	}
		
	private final LineBreakingFromStyledText lineBreakingFromStyledText
	= new DefaultLineBreaker() {
		protected BrailleStream translateAndHyphenate(final java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
			return new FullyHyphenatedAndTranslatedString(join(fromStyledTextToBraille.transform(styledText)), from, to);
		}
	};
	
	private String transform(String text, boolean hyphenate) {
		if (hyphenate && !hyphenating)
			throw new RuntimeException("'hyphens:auto' is not supported");
		try {
			if (hyphenate && externalHyphenator != null)
				return filter.filter(Translatable.text(externalHyphenator.asFullHyphenator().transform(text)).hyphenate(false).build());
			else
				return filter.filter(Translatable.text(text).hyphenate(hyphenate).build()); }
		catch (TranslationException e) {
			throw new RuntimeException(e); }
	}
	
	public String transform(String text, SimpleInlineStyle style) {
		boolean hyphenate = false;
		if (style != null) {
			CSSProperty val = style.getProperty("hyphens");
			if (val != null) {
				if (val == Hyphens.AUTO)
					hyphenate = true;
				else if (val == Hyphens.MANUAL)
					logger.warn("hyphens:{} not supported", val);
				style.removeProperty("hyphens"); }
			for (String prop : style.getPropertyNames())
				logger.warn("CSS property {} not supported", style.getSourceDeclaration(prop)); }
		return transform(text, hyphenate);
	}
	
	@Component(
		name = "org.daisy.pipeline.braille.dotify.DotifyTranslatorImpl.Provider",
		service = {
			DotifyTranslator.Provider.class,
			BrailleTranslatorProvider.class,
			TransformProvider.class
		}
	)
	public static class Provider extends AbstractTransformProvider<DotifyTranslator>
	                             implements DotifyTranslator.Provider {
		
		public Iterable<DotifyTranslator> _get(Query query) {
			MutableQuery q = mutableQuery(query);
			for (Feature f : q.removeAll("input"))
				if (!supportedInput.contains(f.getValue().get()))
					return empty;
			for (Feature f : q.removeAll("output"))
				if (!supportedOutput.contains(f.getValue().get()))
					return empty;
			if (q.containsKey("translator"))
				if (!"dotify".equals(q.removeOnly("translator").getValue().get()))
					return empty;
			return logSelect(q, _provider);
		}
		
		private final static Iterable<DotifyTranslator> empty = Iterables.<DotifyTranslator>empty();
		
		// "text-css" not supported: CSS styles not recognized and line breaking and white space
		// processing not according to CSS
		private final static List<String> supportedInput = Collections.emptyList();
		private final static List<String> supportedOutput = ImmutableList.of("braille");
		
		private TransformProvider<DotifyTranslator> _provider
		= varyLocale(
			new AbstractTransformProvider<DotifyTranslator>() {
				public Iterable<DotifyTranslator> _get(Query query) {
					MutableQuery q = mutableQuery(query);
					if (q.containsKey("locale")) {
						final String locale; {
							try {
								locale = parseLocale(q.removeOnly("locale").getValue().get()).toLanguageTag(); }
							catch (IllegalArgumentException e) {
								logger.error("Invalid locale", e);
								return empty; }
						}
						final String mode = TranslatorMode.Builder.withType(TranslatorType.UNCONTRACTED).build().toString();
						String v = null;
						if (q.containsKey("hyphenator"))
							v = q.removeOnly("hyphenator").getValue().get();
						else
							v = "auto";
						final String hyphenator = v;
						if (!q.isEmpty()) {
							logger.warn("Unsupported feature '"+ q.iterator().next().getKey() + "'");
							return empty; }
						Iterable<BrailleFilter> filters = Iterables.transform(
							factoryServices,
							new Function<BrailleFilterFactoryService,BrailleFilter>() {
								public BrailleFilter _apply(BrailleFilterFactoryService service) {
									try {
										if (service.supportsSpecification(locale, mode))
											return service.newFactory().newFilter(locale, mode); }
									catch (TranslatorConfigurationException e) {
										logger.error("Could not create BrailleFilter for locale " + locale + " and mode " + mode, e); }
									throw new NoSuchElementException(); }});
						return concat(
							Iterables.transform(
								filters,
								new Function<BrailleFilter,Iterable<DotifyTranslator>>() {
									public Iterable<DotifyTranslator> _apply(final BrailleFilter filter) {
										Iterable<DotifyTranslator> translators = empty;
										if (!"none".equals(hyphenator)) {
											MutableQuery hyphenatorQuery = mutableQuery();
											if (!"auto".equals(hyphenator))
												hyphenatorQuery.add("hyphenator", hyphenator);
											hyphenatorQuery.add("locale", locale);
											Iterable<Hyphenator> hyphenators = logSelect(hyphenatorQuery, hyphenatorProvider);
											translators = Iterables.transform(
												hyphenators,
												new Function<Hyphenator,DotifyTranslator>() {
													public DotifyTranslator _apply(Hyphenator hyphenator) {
														return __apply(
															logCreate(
																(DotifyTranslator)new DotifyTranslatorImpl(filter, hyphenator))); }}); }
										if ("auto".equals(hyphenator))
											translators = concat(
												translators,
												Iterables.of(
													logCreate((DotifyTranslator)new DotifyTranslatorImpl(filter, true))));
										if ("none".equals(hyphenator))
											translators = concat(
												translators,
												Iterables.of(
													logCreate((DotifyTranslator)new DotifyTranslatorImpl(filter, false))));
										return translators;
									}
								}
							)
						);
					}
					return empty;
				}
			}
		);
		
		private final List<BrailleFilterFactoryService> factoryServices = new ArrayList<BrailleFilterFactoryService>();
		
		@Reference(
			name = "BrailleFilterFactoryService",
			unbind = "unbindBrailleFilterFactoryService",
			service = BrailleFilterFactoryService.class,
			cardinality = ReferenceCardinality.MULTIPLE,
			policy = ReferencePolicy.DYNAMIC
		)
		protected void bindBrailleFilterFactoryService(BrailleFilterFactoryService service) {
			if (!OSGiHelper.inOSGiContext())
				service.setCreatedWithSPI();
			factoryServices.add(service);
			invalidateCache();
		}
		
		protected void unbindBrailleFilterFactoryService(BrailleFilterFactoryService service) {
			factoryServices.remove(service);
			invalidateCache();
		}
		
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
			hyphenatorProvider.invalidateCache();
			logger.debug("Adding Hyphenator provider: " + provider);
		}
		
		protected void unbindHyphenatorProvider(HyphenatorProvider<?> provider) {
			hyphenatorProviders.remove(provider);
			hyphenatorProvider.invalidateCache();
			logger.debug("Removing Hyphenator provider: " + provider);
		}
		
		private List<TransformProvider<Hyphenator>> hyphenatorProviders
		= new ArrayList<TransformProvider<Hyphenator>>();
		
		private TransformProvider.util.MemoizingProvider<Hyphenator> hyphenatorProvider
		= memoize(dispatch(hyphenatorProviders));
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper(DotifyTranslatorImpl.Provider.class.getName());
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(DotifyTranslatorImpl.class);
	
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
