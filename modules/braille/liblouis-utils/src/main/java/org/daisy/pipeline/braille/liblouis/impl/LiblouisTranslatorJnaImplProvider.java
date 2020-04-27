package org.daisy.pipeline.braille.liblouis.impl;

import java.net.URI;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.toArray;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.FontStyle;
import cz.vutbr.web.css.CSSProperty.FontWeight;
import cz.vutbr.web.css.CSSProperty.TextDecoration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;

import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.LetterSpacing;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.dotify.api.translator.UnsupportedMetricException;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
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
import static org.daisy.pipeline.braille.common.TransformProvider.util.memoize;
import static org.daisy.pipeline.braille.common.TransformProvider.util.dispatch;
import static org.daisy.pipeline.braille.common.util.Iterables.combinations;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.insertHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.Strings.splitInclDelimiter;
import static org.daisy.pipeline.braille.common.util.Tuple2;
import org.daisy.pipeline.braille.common.WithSideEffect;

import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;
import org.daisy.pipeline.braille.liblouis.impl.LiblouisTableJnaImplProvider.LiblouisTableJnaImpl;

import org.liblouis.DisplayException;
import org.liblouis.TranslationException;
import org.liblouis.TranslationResult;
import org.liblouis.Translator;
import org.liblouis.Typeform;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisTranslatorJnaImplProvider",
	service = {
		LiblouisTranslator.Provider.class,
		BrailleTranslatorProvider.class,
		TransformProvider.class
	}
)
public class LiblouisTranslatorJnaImplProvider extends AbstractTransformProvider<LiblouisTranslator> implements LiblouisTranslator.Provider {
	
	private final static char SHY = '\u00AD';  // soft hyphen
	private final static char ZWSP = '\u200B'; // zero-width space
	
	private LiblouisTableJnaImplProvider tableProvider;
	
	@Reference(
		name = "LiblouisTableJnaImplProvider",
		unbind = "unbindLiblouisTableJnaImplProvider",
		service = LiblouisTableJnaImplProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindLiblouisTableJnaImplProvider(LiblouisTableJnaImplProvider provider) {
		tableProvider = provider;
		logger.debug("Registering Liblouis JNA translator provider: " + provider);
	}
	
	protected void unbindLiblouisTableJnaImplProvider(LiblouisTableJnaImplProvider provider) {
		tableProvider = null;
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
		if (provider instanceof LiblouisHyphenatorJnaImplProvider)
			return;
		hyphenatorProviders.add((TransformProvider<Hyphenator>)provider);
		hyphenatorProvider.invalidateCache();
		logger.debug("Adding Hyphenator provider: " + provider);
	}
	
	protected void unbindHyphenatorProvider(HyphenatorProvider<?> provider) {
		if (provider instanceof LiblouisHyphenatorJnaImplProvider)
			return;
		hyphenatorProviders.remove(provider);
		hyphenatorProvider.invalidateCache();
		logger.debug("Removing Hyphenator provider: " + provider);
	}
	
	private List<TransformProvider<Hyphenator>> hyphenatorProviders
	= new ArrayList<TransformProvider<Hyphenator>>();
	
	private TransformProvider.util.MemoizingProvider<Hyphenator> hyphenatorProvider
	= memoize(dispatch(hyphenatorProviders));
	
	private final static Iterable<LiblouisTranslator> empty
	= Iterables.<LiblouisTranslator>empty();
	
	private final static List<String> supportedInput = ImmutableList.of("text-css");
	
	protected final Iterable<LiblouisTranslator> _get(Query query) {
		MutableQuery q = mutableQuery(query);
		for (Feature f : q.removeAll("input"))
			if (!supportedInput.contains(f.getValue().get()))
				return empty;
		boolean asciiBraille = false;
		if (q.containsKey("output")) {
			String v = q.removeOnly("output").getValue().get();
			if ("braille".equals(v)) {}
			else if ("ascii".equals(v))
				asciiBraille = true;
			else
				return empty; }
		if (q.containsKey("translator"))
			if (!"liblouis".equals(q.removeOnly("translator").getValue().get()))
				return empty;
		String table = null;
		if (q.containsKey("liblouis-table"))
			table = q.removeOnly("liblouis-table").getValue().get();
		if (q.containsKey("table"))
			if (table != null) {
				logger.warn("A query with both 'table' and 'liblouis-table' never matches anything");
				return empty; }
			else
				table = q.removeOnly("table").getValue().get();
		String v = null;
		if (q.containsKey("hyphenator"))
			v = q.removeOnly("hyphenator").getValue().get();
		else
			v = "auto";
		final String hyphenator = v;
		v = null;
		if (q.containsKey("locale"))
			v = q.removeAll("locale").iterator().next().getValue().get();
		final String locale = v;
		v = null;
		if (q.containsKey("handle-non-standard-hyphenation"))
			v = q.removeOnly("handle-non-standard-hyphenation").getValue().get();
		else
			v = "ignore";
		final int handleNonStandardHyphenation = v.equalsIgnoreCase("fail") ?
			LiblouisTranslatorImpl.NON_STANDARD_HYPH_FAIL : v.equalsIgnoreCase("defer") ?
			LiblouisTranslatorImpl.NON_STANDARD_HYPH_DEFER :
			LiblouisTranslatorImpl.NON_STANDARD_HYPH_IGNORE;
		if (table != null && !q.isEmpty()) {
			logger.warn("A query with both 'table' or 'liblouis-table' and '"
			            + q.iterator().next().getKey() + "' never matches anything");
			return empty; }
		if (table != null)
			q.add("table", table);
		if (locale != null)
			try {
				q.add("locale", parseLocale(locale).toLanguageTag()); }
			catch (IllegalArgumentException e) {
				logger.error("Invalid locale", e);
				return empty; }
		if (!asciiBraille)
			q.add("unicode");
		q.add("white-space");
		Iterable<LiblouisTranslator> translators = getSimpleTranslator(
			q.asImmutable(),
			locale,
			hyphenator,
			handleNonStandardHyphenation);
		if (translators.iterator().hasNext()) {
			String contraction = q.containsKey("contraction")
				? q.removeAll("contraction").iterator().next().getValue().get()
				: null;
			if (!"no".equals(contraction)) {
				q.add("contraction", "no");
				q.removeAll("grade");
				Iterable<LiblouisTranslator> nonContractingTranslators = Iterables.memoize(
					getSimpleTranslator(
						q.asImmutable(),
						locale,
						hyphenator,
						handleNonStandardHyphenation));
				if (nonContractingTranslators.iterator().hasNext())
					return Iterables.transform(
						combinations(
							Maps.toMap(
								ImmutableList.<Boolean>of(Boolean.TRUE, Boolean.FALSE),
								contracted -> contracted ? translators : nonContractingTranslators)),
						new Function<Map<Boolean,WithSideEffect<LiblouisTranslator,Logger>>,LiblouisTranslator>() {
							public LiblouisTranslator _apply(Map<Boolean,WithSideEffect<LiblouisTranslator,Logger>> translators) {
								return new HandleTextTransformUncontracted(__apply(translators.get(true)),
								                                           __apply(translators.get(false))); }});
			}
		}
		return translators;
	}

	private Iterable<LiblouisTranslator> getSimpleTranslator(Query query,
	                                                         String locale,
	                                                         String hyphenator,
	                                                         int handleNonStandardHyphenation) {
		return concat(
			transform(
				logSelect(query, tableProvider),
				new Function<LiblouisTableJnaImpl,Iterable<LiblouisTranslator>>() {
					public Iterable<LiblouisTranslator> _apply(final LiblouisTableJnaImpl table) {
						Iterable<LiblouisTranslator> translators = empty;
						Normalizer.Form unicodeNormalization = table.getUnicodeNormalizationForm();
						if (!"none".equals(hyphenator)) {
							if ("liblouis".equals(hyphenator) || "auto".equals(hyphenator))
								for (URI t : table.asURIs())
									if (t.toString().endsWith(".dic")) {
										translators = Iterables.of(
											logCreate((LiblouisTranslator)new LiblouisTranslatorHyphenatorImpl(
													table.getTranslator(),
													handleNonStandardHyphenation,
													locale,
													unicodeNormalization))
										);
										break; }
							if (!"liblouis".equals("hyphenator")) {
								MutableQuery hyphenatorQuery = mutableQuery();
								if (!"auto".equals(hyphenator))
									hyphenatorQuery.add("hyphenator", hyphenator);
								if (locale != null)
									hyphenatorQuery.add("locale", locale);
								Iterable<Hyphenator> hyphenators = logSelect(hyphenatorQuery.asImmutable(), hyphenatorProvider);
								if (locale != null && !"auto".equals(hyphenator)) {
									// also search without locale because locale might only refer to translator itself
									hyphenatorQuery.removeAll("locale");
									hyphenators = concat(
										hyphenators,
										logSelect(hyphenatorQuery.asImmutable(), hyphenatorProvider)); }
								translators = concat(
									translators,
									transform(
										hyphenators,
										new Function<Hyphenator,LiblouisTranslator>() {
											public LiblouisTranslator _apply(Hyphenator hyphenator) {
												return __apply(
													logCreate(
														(LiblouisTranslator)new LiblouisTranslatorImpl(
																table.getTranslator(),
																hyphenator,
																handleNonStandardHyphenation,
																locale,
																unicodeNormalization))); }}));
								}}
						if ("none".equals(hyphenator) || "auto".equals(hyphenator))
							translators = concat(
								translators,
								logCreate((LiblouisTranslator)new LiblouisTranslatorImpl(
										table.getTranslator(),
										null,
										handleNonStandardHyphenation,
										locale,
										unicodeNormalization)));
						return translators;
					}
				}
			)
		);
	}
	
	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisTranslatorJnaImplProvider");
	}
	
	static class LiblouisTranslatorImpl extends AbstractBrailleTranslator implements LiblouisTranslator {
		
		private final LiblouisTable table;
		protected final Translator translator;
		private Hyphenator hyphenator;
		protected FullHyphenator fullHyphenator;
		private Hyphenator.LineBreaker lineBreaker;
		private final String mainLocale;
		private final Map<String,Typeform> supportedTypeforms;
		
		// how to handle non-standard hyphenation in pre-translation mode

		// FIXME: DEFER should not be supported in FromStyledTextToBraille because that interface is
		// supposed to return only braille. However this is currently the only way to do
		// it. Components that use this translator should be aware of this when they request the
		// (handle-non-standard-hyphenation:defer) feature. They can assume that if the result
		// contains non-braille characters, the result is an exact copy of the input with no styles
		// applied.
		private final int handleNonStandardHyphenation;
		private final Normalizer.Form unicodeNormalization;
		
		private final static int NON_STANDARD_HYPH_IGNORE = 0;
		private final static int NON_STANDARD_HYPH_FAIL = 1;
		private final static int NON_STANDARD_HYPH_DEFER = 2;
		
		private LiblouisTranslatorImpl(Translator translator,
			                           int handleNonStandardHyphenation,
			                           String mainLocale,
			                           Normalizer.Form unicodeNormalization) {
			table = new LiblouisTable(translator.getTable());
			this.translator = translator;
			this.handleNonStandardHyphenation = handleNonStandardHyphenation;
			this.mainLocale = mainLocale;
			this.supportedTypeforms
				= translator.getSupportedTypeforms().stream().collect(Collectors.toMap(Typeform::getName, e -> e));
			this.unicodeNormalization = unicodeNormalization;
		}
		
		private LiblouisTranslatorImpl(Translator translator,
			                           Hyphenator hyphenator,
			                           int handleNonStandardHyphenation,
			                           String mainLocale,
			                           Normalizer.Form unicodeNormalization) {
			this(translator, handleNonStandardHyphenation, mainLocale, unicodeNormalization);
			this.hyphenator = hyphenator;
			if (hyphenator != null) {
				try {
					fullHyphenator = new HyphenatorAsFullHyphenator(hyphenator); }
				catch (UnsupportedOperationException e) {}
				try {
					lineBreaker = hyphenator.asLineBreaker(); }
				catch (UnsupportedOperationException e) {}}
		}
		
		// FIXME: not if (input:text-css)
		public LiblouisTable asLiblouisTable() {
			return table;
		}
		
		private FromTypeformedTextToBraille fromTypeformedTextToBraille;
		
		public FromTypeformedTextToBraille fromTypeformedTextToBraille() {
			if (fromTypeformedTextToBraille == null)
				fromTypeformedTextToBraille = new FromTypeformedTextToBraille() {
					public String[] transform(String[] text, String[] emphClasses) {
						Typeform[] typeform = new Typeform[emphClasses.length];
						for (int i = 0; i < typeform.length; i++) {
							typeform[i] = supportedTypeforms.get(emphClasses[i]);
							if (typeform[i] == null)
								logger.warn("emphclass 'italic' not defined in table {}", translator.getTable());
						}
						return LiblouisTranslatorImpl.this.transform(text, typeform);
					}
					@Override
					public String toString() {
						return LiblouisTranslatorImpl.this.toString();
					}
				};
			return fromTypeformedTextToBraille;
		}
		
		private FromStyledTextToBraille fromStyledTextToBraille;
		
		@Override
		public FromStyledTextToBraille fromStyledTextToBraille() {
			if (fromStyledTextToBraille == null)
				fromStyledTextToBraille = new FromStyledTextToBraille() {
					public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
						List<String> result = LiblouisTranslatorImpl.this.transform(styledText, false, false);
						if (to < 0) to = result.size();
						if (from > 0 || to < result.size())
							return result.subList(from, to);
						else
							return result;
					}
					@Override
					public String toString() {
						return LiblouisTranslatorImpl.this.toString();
					}
				};
			return fromStyledTextToBraille;
		}
		
		private LineBreakingFromStyledText lineBreakingFromStyledText;
		
		@Override
		public LineBreakingFromStyledText lineBreakingFromStyledText() {
			if (lineBreakingFromStyledText == null)
				lineBreakingFromStyledText = new LineBreaker(
					translator,
					supportedTypeforms,
					unicodeNormalization,
					lineBreaker,
					fullHyphenator,
					new FromStyledTextToBraille() {
						public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
							List<String> result = LiblouisTranslatorImpl.this.transform(styledText, true, true);
							if (to < 0) to = result.size();
							if (from > 0 || to < result.size())
								return result.subList(from, to);
							else
								return result;
						}
					}) {
						@Override
						public String toString() {
							return LiblouisTranslatorImpl.this.toString();
						}
					};
			return lineBreakingFromStyledText;
		}
		
		private final static Pattern WORD_SPLITTER = Pattern.compile("[\\x20\t\\n\\r\\u2800\\xA0]+");
		
		static class LineBreaker extends DefaultLineBreaker {
			
			final Translator liblouisTranslator;
			final Map<String,Typeform> supportedTypeforms;
			final Hyphenator.LineBreaker lineBreaker;
			final FullHyphenator fullHyphenator;
			final FromStyledTextToBraille fullTranslator;
			final Normalizer.Form unicodeNormalization;
			
			protected LineBreaker(Translator liblouisTranslator,
			                      Map<String,Typeform> supportedTypeforms,
			                      Normalizer.Form unicodeNormalization,
			                      Hyphenator.LineBreaker lineBreaker,
			                      FullHyphenator fullHyphenator,
			                      FromStyledTextToBraille fullTranslator) {
				super(logger);
				this.liblouisTranslator = liblouisTranslator;
				this.supportedTypeforms = supportedTypeforms;
				this.lineBreaker = lineBreaker;
				this.fullHyphenator = fullHyphenator;
				this.fullTranslator = fullTranslator;
				this.unicodeNormalization = unicodeNormalization;
			}
			
			protected BrailleStream translateAndHyphenate(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
				// styledText is cloned because we are mutating the style objects
				java.lang.Iterable<CSSStyledText> styledTextCopy
					= org.daisy.pipeline.braille.common.util.Iterables.clone(styledText);
				java.lang.Iterable<String> braille;
				try {
					braille = fullTranslator.transform(styledTextCopy); }
				catch (Exception e) {
					return new BrailleStreamImpl(liblouisTranslator,
					                             supportedTypeforms,
					                             lineBreaker,
					                             fullHyphenator,
					                             unicodeNormalization,
					                             styledText,
					                             from,
					                             to); }
				StringBuilder brailleString = new StringBuilder();
				int fromChar = 0;
				int toChar = to >= 0 ? 0 : -1;
				for (String s : braille) {
					brailleString.append(s);
					if (--from == 0)
						fromChar = brailleString.length();
					if (--to == 0)
						toChar = brailleString.length();
				}
				return new FullyHyphenatedAndTranslatedString(brailleString.toString(), fromChar, toChar);
			}
			
			static class BrailleStreamImpl implements BrailleStream {
				
				final Translator liblouisTranslator;
				final Map<String,Typeform> supportedTypeforms;
				final Hyphenator.LineBreaker lineBreaker;
				final FullHyphenator fullHyphenator;
				
				// FIXME: remove duplication!!
				
				// convert style into typeform, hyphenate, preserveLines, preserveSpace and letterSpacing arrays
				final Typeform[] typeform;
				final boolean[] hyphenate;
				final boolean[] preserveLines;
				final boolean[] preserveSpace;
				final int[] letterSpacing;
				
				// don't perform a translation at all
				final boolean noTransform;
				
				// text with some segments split up into white space segments that need to be preserved
				// in the output and other segments
				final String[] textWithWs;
				
				// boolean array for tracking which (non-empty white space) segments in textWithWs need
				// to be preserved
				final boolean[] pre;
				
				// mapping from index in textWithWs to index in text
				final int[] textWithWsMapping;
				
				// textWithWs segments joined together with hyphens removed and sequences of preserved
				// white space replaced with a nbsp
				String joinedText;
				
				// mapping from character index in joinedText to segment index in textWithWs
				int[] joinedTextMapping;
				
				// byte array for tracking hyphenation positions
				byte[] manualHyphens;
				
				// translation result without hyphens and with preserved white space not restored
				String joinedBraille;
				
				// mapping from character index in joinedBraille to character index in joinedText
				int[] characterIndicesInBraille;
				
				// mapping from inter-character index in joinedBraille to inter-character index in joinedText
				int[] interCharacterIndicesInBraille;
				
				// current position in input and output (joinedText and joinedBraille)
				int curPos = -1;
				int curPosInBraille = -1;
				int endPos = -1;
				int endPosInBraille = -1;
				final int to;
				
				BrailleStreamImpl(Translator liblouisTranslator,
				                  Map<String,Typeform> supportedTypeforms,
				                  Hyphenator.LineBreaker lineBreaker,
				                  FullHyphenator fullHyphenator,
				                  Normalizer.Form unicodeNormalization,
				                  java.lang.Iterable<CSSStyledText> styledText,
				                  int from,
				                  int to) {
					
					this.liblouisTranslator = liblouisTranslator;
					this.supportedTypeforms = supportedTypeforms;
					this.lineBreaker = lineBreaker;
					this.fullHyphenator = fullHyphenator;
					
					// convert Iterable<CSSStyledText> into an text array and a style array
					int size = size(styledText);
					if (to < 0) to = size;
					this.to = to;
					
					// FIXME: handle from and to properly
					String[] text = new String[size];
					SimpleInlineStyle[] styles = new SimpleInlineStyle[size]; {
						int i = 0;
						for (CSSStyledText t : styledText) {
							Map<String,String> attrs = t.getTextAttributes();
							if (attrs != null)
								for (String k : attrs.keySet())
									logger.warn("Text attribute \"{}:{}\" ignored", k, attrs.get(k));
							text[i] = t.getText();
							styles[i] = t.getStyle();
							i++; }}
					
					// perform Unicode normalization
					if (unicodeNormalization != null)
						for (int k = 0; k < text.length; k++)
							text[k] = Normalizer.normalize(text[k], unicodeNormalization);
					
					boolean someTransform = false;
					boolean someNotTransform = false;
					
					{ // compute typeform, hyphenate, preserveLines, preserveSpace and letterSpacing
						typeform = new Typeform[size];
						hyphenate = new boolean[size];
						preserveLines = new boolean[size];
						preserveSpace = new boolean[size];
						letterSpacing = new int[size];
						for (int i = 0; i < size; i++) {
							typeform[i] = Typeform.PLAIN_TEXT;
							hyphenate[i] = false;
							preserveLines[i] = preserveSpace[i] = false;
							letterSpacing[i] = 0;
							SimpleInlineStyle style = styles[i];
							if (style != null) {
								CSSProperty val = style.getProperty("white-space");
								if (val != null) {
									if (val == WhiteSpace.PRE_WRAP)
										preserveLines[i] = preserveSpace[i] = true;
									else if (val == WhiteSpace.PRE_LINE)
										preserveLines[i] = true;
									style.removeProperty("white-space"); }
								val = style.getProperty("text-transform");
								if (val != null) {
									if (val == TextTransform.NONE) {
										someNotTransform = true;
										style.removeProperty("text-transform");
										if (WORD_SPLITTER.matcher(text[i]).matches())
											style.removeProperty("hyphens");
										if (!style.isEmpty()) {
											String p = style.getPropertyNames().iterator().next();
											CSSProperty v = style.getProperty(p);
											logger.warn("'text-transform: none' can not be used in combination with '" + p + ": " + v + "'");
											logger.debug("(text is: '" + text[i] + "')"); }
										continue; }
									else if (val == TextTransform.AUTO) {}
									else if (val == TextTransform.list_values) {
										TermList values = style.getValue(TermList.class, "text-transform");
										text[i] = textFromTextTransform(text[i], values);
										typeform[i] = typeform[i].add(typeformFromTextTransform(values, liblouisTranslator, supportedTypeforms)); }
									style.removeProperty("text-transform"); }
								someTransform = true;
								val = style.getProperty("hyphens");
								if (val != null) {
									if (val == Hyphens.AUTO)
										hyphenate[i] = true;
									else if (val == Hyphens.NONE)
										text[i] = extractHyphens(text[i], SHY, ZWSP)._1;
									style.removeProperty("hyphens"); }
								val = style.getProperty("letter-spacing");
								if (val != null) {
									if (val == LetterSpacing.length) {
										letterSpacing[i] = style.getValue(TermInteger.class, "letter-spacing").getIntValue();
										if (letterSpacing[i] < 0) {
											logger.warn("letter-spacing: {} not supported, must be non-negative", val);
											letterSpacing[i] = 0; }}
									style.removeProperty("letter-spacing"); }
								typeform[i] = typeform[i].add(typeformFromInlineCSS(style, liblouisTranslator, supportedTypeforms)); }
							else
								someTransform = true; }
					}
					{ // compute preserved white space segments (textWithWs, textWithWsMapping, pre)
						List<String> l1 = new ArrayList<String>();
						List<Boolean> l2 = new ArrayList<Boolean>();
						List<Integer> l3 = new ArrayList<Integer>();
						for (int i = 0; i < text.length; i++) {
							String t = text[i];
							if (t.isEmpty()) {
								l1.add(t);
								l2.add(false);
								l3.add(i); }
							else {
								Pattern ws;
								if (preserveSpace[i])
									ws = ON_SPACE_SPLITTER;
								else if (preserveLines[i])
									ws = LINE_SPLITTER;
								else
									ws = ON_NBSP_SPLITTER;
								boolean p = false;
								for (String s : splitInclDelimiter(t, ws)) {
									if (!s.isEmpty()) {
										l1.add(s);
										l2.add(p);
										l3.add(i); }
									p = !p; }}}
						int len = l1.size();
						textWithWs = new String[len];
						pre = new boolean[len];
						textWithWsMapping = new int[len];
						for (int i = 0; i < len; i++) {
							textWithWs[i] = l1.get(i);
							pre[i] = l2.get(i);
							textWithWsMapping[i] = l3.get(i); }
					}
					{ // compute joined text and manual hyphens array (joinedText, joinedTextMapping, manualHyphens)
						String[] textWithWsReplaced = new String[textWithWs.length];
						for (int i = 0; i < textWithWs.length; i++)
							textWithWsReplaced[i] = pre[i] ? ""+NBSP : textWithWs[i];
						Tuple2<String,byte[]> t = extractHyphens(join(textWithWsReplaced, RS), SHY, ZWSP);
						manualHyphens = t._2;
						String[] nohyph = toArray(SEGMENT_SPLITTER.split(t._1), String.class);
						joinedTextMapping = new int[join(nohyph).length()];
						int i = 0;
						int j = 0;
						for (String s : nohyph) {
							int l = s.length();
							for (int k = 0; k < l; k++)
								joinedTextMapping[i++] = j;
							j++; }
						t = extractHyphens(manualHyphens, t._1, null, null, null, RS);
						joinedText = t._1;
					}
					{ // compute initial curPos and endPos from from and to
						int fromChar = -1;
						int toChar = -1;
						for (int i = 0; i < joinedTextMapping.length; i++) {
							if (fromChar < 0 || (toChar < 0 && to >= 0)) {
								int indexInText = textWithWsMapping[joinedTextMapping[i]];
								if (fromChar < 0 && indexInText >= from)
									fromChar = i;
								if (toChar < 0 && indexInText >= to)
									toChar = i;
							} else
								break;
						}
						if (toChar < 0) toChar = joinedTextMapping.length;
						this.curPos = fromChar;
						this.endPos = toChar;
					}
					
					// FIXME: also handle (someNotTransform && someTransform)
					noTransform = joinedText.matches("\\xA0*") || (someNotTransform && !someTransform);
				}
				
				public String next(final int limit, final boolean force, boolean allowHyphens) {
					String next = "";
					if (limit > 0) {
					int available = limit;
				  segments: while (true) {
						if (curPos == endPos)
							break;
						if (noTransform) {
							next = joinedText.substring(curPos, endPos);
							curPos = endPos;
							break; }
						if (joinedBraille == null)
							updateBraille();
						int curSegment = joinedTextMapping[curPos];
						int curSegmentEnd; {
							int i = curPos;
							for (; i < endPos; i++)
								if (joinedTextMapping[i] > curSegment)
									break;
							curSegmentEnd = i; }
						int curSegmentEndInBraille = positionInBraille(curSegmentEnd);
						if (curSegmentEndInBraille == curPosInBraille)
							continue segments;
						String segment = joinedText.substring(curPos, curSegmentEnd);
						String segmentInBraille = joinedBraille.substring(curPosInBraille, curSegmentEndInBraille);
						
						// restore preserved white space segments
						if (pre[curSegment]) {
							Matcher m = Pattern.compile("\\xA0([\\xAD\\u200B]*)").matcher(segmentInBraille);
							if (m.matches()) {
								String restoredSpace = segment.replaceAll("[\\x20\t\\u2800]", ""+NBSP)
								                              .replaceAll("[\\n\\r]", ""+LS) + m.group(1);
								next += restoredSpace;
								available -= restoredSpace.length();
								curPos = curSegmentEnd;
								curPosInBraille = curSegmentEndInBraille;
								continue segments; }}
						
						// don't hyphenate if hyphenation is disabled, no hyphenator is
						// available, or the segment fits in the available space
						if (segmentInBraille.length() <= available || !hyphenate[textWithWsMapping[curSegment]]
						    || (fullHyphenator == null && lineBreaker == null)) {
							if (hyphenate[textWithWsMapping[curSegment]] || segmentInBraille.length() > available)
								logger.warn("hyphens:auto not supported");
							
							segmentInBraille = addLetterSpacing(segment, segmentInBraille, letterSpacing[textWithWsMapping[curSegment]]);
							next += segmentInBraille;
							available -= segmentInBraille.length();
							curPos = curSegmentEnd;
							curPosInBraille = curSegmentEndInBraille;
							continue segments; }
							
						// try standard hyphenation of the whole segment
						if (fullHyphenator != null) {
							try {
								
								segmentInBraille = addHyphensAndLetterSpacing(segment, segmentInBraille, letterSpacing[textWithWsMapping[curSegment]]);
								next += segmentInBraille;
								available -= segmentInBraille.length();
								curPos = curSegmentEnd;
								curPosInBraille = curSegmentEndInBraille;
								continue segments; }
							catch (Exception e) {}}
						
						// loop over words in segment
						Matcher m = WORD_SPLITTER.matcher(segment);
						int segmentStart = curPos;
						boolean foundSpace;
						while ((foundSpace = m.find()) || curPos < curSegmentEnd) {
							int wordEnd = foundSpace ? segmentStart + m.start() : curSegmentEnd;
							if (wordEnd > curPos) {
								int wordEndInBraille = positionInBraille(wordEnd);
								if (wordEndInBraille > curPosInBraille) {
									String word = joinedText.substring(curPos, wordEnd);
									String wordInBraille = joinedBraille.substring(curPosInBraille, wordEndInBraille);
									
									// don't hyphenate if word fits in the available space
									if (wordInBraille.length() <= available) {
										next += wordInBraille;
										available -= wordInBraille.length();
										curPos = wordEnd;
										curPosInBraille = wordEndInBraille; }
									else {
										
										// try standard hyphenation of the whole word
										try {
											
											wordInBraille = addHyphensAndLetterSpacing(word, wordInBraille, letterSpacing[textWithWsMapping[curSegment]]);
											next += wordInBraille;
											available -= wordInBraille.length();
											curPos = wordEnd;
											curPosInBraille = wordEndInBraille; }
										catch (Exception ee) {
											
											// before we try non-standard hyphenation, return what we have already
											// we do this because we are not sure that the value of "available" is accurate
											// this way we leave the responsibility of white space normalisation to DefaultLineBreaker
											// FIXME: remove the "available" variable
											if (!next.isEmpty())
												break segments;
											
											// try non-standard hyphenation
											Hyphenator.LineIterator lines = lineBreaker.transform(word);
											
											// do a binary search for the optimal break point
											Solution bestSolution = null;
											int left = 1;
											int right = word.length() - 1;
											int textAvailable = available;
											if (textAvailable > right)
												textAvailable = right;
											if (textAvailable < left)
												break segments;
											while (true) {
												String line = lines.nextLine(textAvailable, force && next.isEmpty(), allowHyphens);
												String replacementWord = line + lines.remainder();
												if (updateInput(curPos, wordEnd, replacementWord)) {
													wordEnd = curPos + replacementWord.length();
													updateBraille(); }
												int lineEnd = curPos + line.length();
												int lineEndInBraille = positionInBraille(lineEnd);
												String lineInBraille = joinedBraille.substring(curPosInBraille, lineEndInBraille);
												lineInBraille = addLetterSpacing(line, lineInBraille, letterSpacing[textWithWsMapping[curSegment]]);
												int lineInBrailleLength = lineInBraille.length();
												if (lines.lineHasHyphen()) {
													lineInBraille += "\u00ad";
													lineInBrailleLength++; }
												if (lineInBrailleLength == available) {
													bestSolution = new Solution(); {
														bestSolution.line = line;
														bestSolution.replacementWord = replacementWord;
														bestSolution.lineInBraille = lineInBraille;
														bestSolution.lineInBrailleLength = lineInBrailleLength; }
													left = textAvailable + 1;
													right = textAvailable - 1; }
												else if (lineInBrailleLength < available) {
													left = textAvailable + 1;
													if (bestSolution == null || lineInBrailleLength > bestSolution.lineInBrailleLength) {
														bestSolution = new Solution(); {
															bestSolution.line = line;
															bestSolution.replacementWord = replacementWord;
															bestSolution.lineInBraille = lineInBraille;
															bestSolution.lineInBrailleLength = lineInBrailleLength; }}}
												else
													right = textAvailable - 1;
												lines.reset();
												textAvailable = (right + left) / 2;
												if (textAvailable < left || textAvailable > right) {
													if (bestSolution != null) {
														next += bestSolution.lineInBraille;
														available = 0;
														if (updateInput(curPos, wordEnd, bestSolution.replacementWord))
															updateBraille();
														curPos += bestSolution.line.length();
														curPosInBraille = positionInBraille(curPos); }
													else if (force && next.isEmpty()) {
														next = wordInBraille;
														available = 0;
														curPos = wordEnd;
														curPosInBraille = wordEndInBraille; }
													break segments; } }}}}}
							if (foundSpace) {
								int spaceEnd = segmentStart + m.end();
								int spaceEndInBraille = positionInBraille(spaceEnd);
								if (spaceEndInBraille > curPosInBraille) {
									String spaceInBraille = joinedBraille.substring(curPosInBraille, spaceEndInBraille);
									next += spaceInBraille;
									available -= spaceInBraille.length();
									curPos = spaceEnd;
									curPosInBraille = spaceEndInBraille; }}}}
					}
					if (lastPeek != null && !next.isEmpty() && next.charAt(0) != lastPeek)
						throw new IllegalStateException();
					lastPeek = null;
					return next;
				}
				
				private static class Solution {
					String line;
					String replacementWord;
					String lineInBraille;
					int lineInBrailleLength;
				}
				
				public boolean hasNext() {
					if (noTransform)
						return curPos < endPos;
					if (joinedBraille == null)
						updateBraille();
					boolean hasNextOutput = curPosInBraille < endPosInBraille;
					boolean hasNextInput = curPos < endPos;
					if (hasNextInput != hasNextOutput)
						throw new RuntimeException("coding error");
					return hasNextOutput;
				}
				
				Character lastPeek = null;
				
				public Character peek() {
					if (noTransform)
						return joinedText.charAt(curPos);
					if (joinedBraille == null)
						updateBraille();
					lastPeek = joinedBraille.charAt(curPosInBraille);
					return lastPeek;
				}
				
				// FIXME: does not take into account white-space property of segments: if "white-space: pre",
				// spaces are not replaced with NBSP yet at this point (this only happens in next() method)
				public String remainder() {
					if (noTransform)
						return joinedText.substring(curPos, endPos);
					if (joinedBraille == null)
						updateBraille();
					return joinedBraille.substring(curPosInBraille, endPosInBraille);
				}
				
				// FIXME: does not take into account white-space property of segments: if "white-space: pre",
				// spaces are not replaced with NBSP yet at this point (this only happens in next() method)
				public boolean hasPrecedingSpace() {
					if (noTransform)
						return DefaultLineBreaker.hasPrecedingSpace(joinedText, curPos);
					if (joinedBraille == null)
						updateBraille();
					return DefaultLineBreaker.hasPrecedingSpace(joinedBraille, curPosInBraille);
				}
				
				@Override
				public Object clone() {
					try {
						BrailleStreamImpl clone = (BrailleStreamImpl)super.clone();
						if (joinedTextMapping != null)
							clone.joinedTextMapping = joinedTextMapping.clone();
						if (manualHyphens != null)
							clone.manualHyphens = manualHyphens.clone();
						if (characterIndicesInBraille != null)
							clone.characterIndicesInBraille = characterIndicesInBraille.clone();
						if (interCharacterIndicesInBraille != null)
							clone.interCharacterIndicesInBraille = interCharacterIndicesInBraille.clone();
						return clone;
					} catch (CloneNotSupportedException e) {
						throw new InternalError("coding error");
					}
				}
				
				private int positionInBraille(int pos) {
					int posInBraille = curPosInBraille;
					if (posInBraille < 0) posInBraille = 0;
					for (; posInBraille < joinedBraille.length(); posInBraille++)
						if (characterIndicesInBraille[posInBraille] >= pos)
							break;
					return posInBraille;
				}
				
				private String addHyphensAndLetterSpacing(String segment, String segmentInBraille, int letterSpacing) {
					
					byte[] autoHyphens = fullHyphenator.hyphenate(segment);
					// FIXME: don't hard-code the number 4
					byte[] autoHyphensAndLetterBoundaries
						= (letterSpacing > 0) ? detectLetterBoundaries(autoHyphens, segment, (byte)4) : autoHyphens;
					if (autoHyphensAndLetterBoundaries == null && manualHyphens == null)
						return segment;
					byte[] hyphensAndLetterBoundariesInBraille = new byte[segmentInBraille.length() - 1];
					if (autoHyphensAndLetterBoundaries != null)
						for (int i = 0; i < hyphensAndLetterBoundariesInBraille.length; i++)
							hyphensAndLetterBoundariesInBraille[i]
								= autoHyphensAndLetterBoundaries[interCharacterIndicesInBraille[curPosInBraille + i] - curPos];
					if (manualHyphens != null)
						for (int i = 0; i < hyphensAndLetterBoundariesInBraille.length; i++)
							hyphensAndLetterBoundariesInBraille[i]
								= manualHyphens[interCharacterIndicesInBraille[curPosInBraille + i]];
					
					String r = insertHyphens(segmentInBraille, hyphensAndLetterBoundariesInBraille, SHY, ZWSP, US);
					return (letterSpacing > 0) ? applyLetterSpacing(r, letterSpacing) : r;
				}
				
				private String addLetterSpacing(String segment, String segmentInBraille, int letterSpacing) {
					if (letterSpacing > 0) {
						// FIXME: don't hard-code the number 1
						byte[] letterBoundaries = detectLetterBoundaries(null, segment, (byte)1);
						byte[] letterBoundariesInBraille = new byte[segmentInBraille.length() - 1];
						for (int i = 0; i < letterBoundariesInBraille.length; i++)
							letterBoundariesInBraille[i] = letterBoundaries[interCharacterIndicesInBraille[curPosInBraille + i] - curPos];
						return applyLetterSpacing(insertHyphens(segmentInBraille, letterBoundariesInBraille, US), letterSpacing); }
					else
						return segmentInBraille;
				}
				
				private boolean updateInput(int start, int end, String replacement) {
					if (joinedText.substring(start, end).equals(replacement))
						return false;
					joinedText = joinedText.substring(0, start) + replacement + joinedText.substring(end);
					{ // recompute joinedTextMapping
						int[] updatedJoinedTextMapping = new int[joinedText.length()];
						int i = 0;
						int j = 0;
						while (i < start)
							updatedJoinedTextMapping[j++] = joinedTextMapping[i++];
						int startSegment = joinedTextMapping[start];
						while (i < end)
							if (joinedTextMapping[i++] != startSegment)
								throw new RuntimeException("Coding error");
						while (j < start + replacement.length())
							updatedJoinedTextMapping[j++] = startSegment;
						while (j < updatedJoinedTextMapping.length)
							updatedJoinedTextMapping[j++] = joinedTextMapping[i++];
						joinedTextMapping = updatedJoinedTextMapping;
					}
					// recompute manualHyphens
					if (manualHyphens != null) {
						byte[] updatedManualHyphens = new byte[joinedText.length() - 1];
						int i = 0;
						int j = 0;
						while (i < start)
							updatedManualHyphens[j++] = manualHyphens[i++];
						while (j < start + replacement.length() - 1)
							updatedManualHyphens[j++] = 0;
						i = end - 1;
						while (j < updatedManualHyphens.length)
							updatedManualHyphens[j++] = manualHyphens[i++];
						manualHyphens = updatedManualHyphens;
					}
					{ // recompute endPos
						int toChar = -1;
						if (to >= 0)
							for (int i = 0; i < joinedTextMapping.length; i++)
								if (textWithWsMapping[joinedTextMapping[i]] >= to) {
									toChar = i;
									break; }
						this.endPos = toChar > 0 ? toChar : joinedTextMapping.length;
					}
					return true;
				}
				
				// TODO: warn about lost white space?
				private void updateBraille() {
					String partBeforeCurPos = curPosInBraille > 0 ? joinedBraille.substring(0, curPosInBraille): null;
					int[] characterIndices = new int[joinedText.length()]; {
						for (int i = 0; i < joinedText.length(); i++)
							characterIndices[i] = i; }
					int[] interCharacterIndices = new int[joinedText.length() - 1]; {
						for (int i = 0; i < joinedText.length() - 1; i++)
							interCharacterIndices[i] = i; }
					
					// typeform var with the same length as joinedText
					Typeform[] _typeform = null;
					for (Typeform t : typeform)
						if (t != Typeform.PLAIN_TEXT) {
							_typeform = new Typeform[joinedText.length()];
							for (int i = 0; i < _typeform.length; i++)
								_typeform[i] = typeform[textWithWsMapping[joinedTextMapping[i]]];
							break; }
					try {
						TranslationResult r = liblouisTranslator.translate(joinedText, _typeform, characterIndices, interCharacterIndices);
						joinedBraille = r.getBraille();
						characterIndicesInBraille = r.getCharacterAttributes();
						interCharacterIndicesInBraille = r.getCharacterAttributes(); }
					catch (TranslationException e) {
						throw new RuntimeException(e); }
					catch (DisplayException e) {
						throw new RuntimeException(e); }
					if (partBeforeCurPos != null)
						if (!joinedBraille.substring(0, curPosInBraille).equals(partBeforeCurPos))
							throw new IllegalStateException();
					int newCurPosInBraille = positionInBraille(curPos);
					if (curPosInBraille >= 0) {
						if (curPosInBraille != newCurPosInBraille)
							throw new IllegalStateException();
					} else
						curPosInBraille = newCurPosInBraille;
					endPosInBraille = positionInBraille(endPos);
				}
			}
		}
		
		private List<String> transform(java.lang.Iterable<CSSStyledText> styledText,
		                               boolean forceBraille,
		                               boolean failWhenNonStandardHyphenation) {
			int size = size(styledText);
			String[] text = new String[size];
			SimpleInlineStyle[] style = new SimpleInlineStyle[size];
			int i = 0;
			for (CSSStyledText t : styledText) {
				Map<String,String> attrs = t.getTextAttributes();
				if (attrs != null)
					for (String k : attrs.keySet())
						logger.warn("Text attribute \"{}:{}\" ignored", k, attrs.get(k));
				text[i] = t.getText();
				style[i] = t.getStyle();
				i++; }
			return Arrays.asList(transform(text, style, forceBraille, failWhenNonStandardHyphenation));
		}
		
		private String[] transform(String[] text, SimpleInlineStyle[] styles, boolean forceBraille, boolean failWhenNonStandardHyphenation) {
			int size = text.length;
			Typeform[] typeform = new Typeform[size];
			boolean[] hyphenate = new boolean[size];
			boolean[] preserveLines = new boolean[size];
			boolean[] preserveSpace = new boolean[size];
			int[] letterSpacing = new int[size];
			boolean someTransform = false;
			boolean someNotTransform = false;
			for (int i = 0; i < size; i++) {
				typeform[i] = Typeform.PLAIN_TEXT;
				hyphenate[i] = false;
				preserveLines[i] = preserveSpace[i] = false;
				letterSpacing[i] = 0;
				SimpleInlineStyle style = styles[i];
				if (style != null) {
					CSSProperty val = style.getProperty("white-space");
					if (val != null) {
						if (val == WhiteSpace.PRE_WRAP)
							preserveLines[i] = preserveSpace[i] = true;
						else if (val == WhiteSpace.PRE_LINE)
							preserveLines[i] = true;
						// don't remove "white-space" property because it has not been fully handled
					}
					val = style.getProperty("text-transform");
					if (val != null) {
						if (val == TextTransform.NONE) {
							someNotTransform = true;
							style.removeProperty("text-transform");
							if (WORD_SPLITTER.matcher(text[i]).matches())
								style.removeProperty("hyphens");
							if (!style.isEmpty()) {
								String p = style.getPropertyNames().iterator().next();
								CSSProperty v = style.getProperty(p);
								logger.warn("'text-transform: none' can not be used in combination with '" + p + ": " + v + "'");
								logger.debug("(text is: '" + text[i] + "')"); }
							continue; }
						else if (val == TextTransform.AUTO) {}
						else if (val == TextTransform.list_values) {
							TermList values = style.getValue(TermList.class, "text-transform");
							text[i] = textFromTextTransform(text[i], values);
							typeform[i] = typeform[i].add(typeformFromTextTransform(values, translator, supportedTypeforms)); }
						style.removeProperty("text-transform"); }
					someTransform = true;
					val = style.getProperty("hyphens");
					if (val != null) {
						if (val == Hyphens.AUTO)
							hyphenate[i] = true;
						else if (val == Hyphens.NONE)
							text[i] = extractHyphens(text[i], SHY, ZWSP)._1;
						style.removeProperty("hyphens"); }
					val = style.getProperty("letter-spacing");
					if (val != null) {
						if (val == LetterSpacing.length) {
							letterSpacing[i] = style.getValue(TermInteger.class, "letter-spacing").getIntValue();
							if (letterSpacing[i] < 0) {
								logger.warn("letter-spacing: {} not supported, must be non-negative", val);
								letterSpacing[i] = 0; }}
						style.removeProperty("letter-spacing"); }
					typeform[i] = typeform[i].add(typeformFromInlineCSS(style, translator, supportedTypeforms)); }
				else
					someTransform = true; }
			
			// FIXME: also handle (someNotTransform && someTransform)
			if (someNotTransform && !someTransform)
				return text;
			return transform(text, typeform, hyphenate, preserveLines, preserveSpace, letterSpacing,
			                 forceBraille, failWhenNonStandardHyphenation);
		}
		
		private String[] transform(String[] text, Typeform[] typeform) {
			int size = text.length;
			boolean[] hyphenate = new boolean[size];
			boolean[] preserveLines = new boolean[size];
			boolean[] preserveSpace = new boolean[size];
			int[] letterSpacing = new int[size];
			for (int i = 0; i < hyphenate.length; i++) {
				hyphenate[i] = preserveLines[i] = preserveSpace[i] = false;
				letterSpacing[i] = 0; }
			return transform(text, typeform, hyphenate, preserveLines, preserveSpace, letterSpacing, false, false);
		}
		
		protected final static char RS = '\u001E';   // (for segmentation)
		protected final static char US = '\u001F';   // (for segmentation)
		protected final static char LS = '\u2028';   // line separator
		protected final static char NBSP = '\u00A0'; // no-break space
		protected final static Splitter SEGMENT_SPLITTER = Splitter.on(RS);
		private final static Pattern ON_NBSP_SPLITTER = Pattern.compile("[\\xAD\\u200B]*\\xA0[\\xAD\\u200B\\xA0]*");
		private final static Pattern ON_SPACE_SPLITTER = Pattern.compile("[\\xAD\\u200B]*[\\x20\t\\n\\r\\u2800\\xA0][\\xAD\\u200B\\x20\t\\n\\r\\u2800\\xA0]*");
		private final static Pattern LINE_SPLITTER = Pattern.compile("[\\xAD\\u200B]*[\\n\\r][\\xAD\\u200B\\n\\r]*");
		
		// the positions in the text where spacing must be inserted have been previously indicated with a US control character
		private static String applyLetterSpacing(String text, int letterSpacing) {
			String space = "";
			for (int i = 0; i < letterSpacing; i++)
				space += NBSP;
			return text.replaceAll("\u001F", space);
		}
		
		private String[] transform(String[] text,
		                           Typeform[] typeform,
		                           boolean[] hyphenate,
		                           boolean[] preserveLines,
		                           boolean[] preserveSpace,
		                           int[] letterSpacing,
		                           boolean forceBraille,
		                           boolean failWhenNonStandardHyphenation) {
			
			// perform Unicode normalization
			if (unicodeNormalization != null)
				for (int k = 0; k < text.length; k++)
					text[k] = Normalizer.normalize(text[k], unicodeNormalization);
			
			// text with some segments split up into white space segments that need to be preserved
			// in the output and other segments
			String[] textWithWs;
			// boolean array for tracking which (non-empty white space) segments in textWithWs need
			// to be preserved
			boolean[] pre;
			// mapping from index in textWithWs to index in text
			int[] textWithWsMapping; {
				List<String> l1 = new ArrayList<String>();
				List<Boolean> l2 = new ArrayList<Boolean>();
				List<Integer> l3 = new ArrayList<Integer>();
				for (int i = 0; i < text.length; i++) {
					String t = text[i];
					if (t.isEmpty()) {
						l1.add(t);
						l2.add(false);
						l3.add(i); }
					else {
						Pattern ws;
						if (preserveSpace[i])
							ws = ON_SPACE_SPLITTER;
						else if (preserveLines[i])
							ws = LINE_SPLITTER;
						else
							ws = ON_NBSP_SPLITTER;
						boolean p = false;
						for (String s : splitInclDelimiter(t, ws)) {
							if (!s.isEmpty()) {
								l1.add(s);
								l2.add(p);
								l3.add(i); }
							p = !p; }}}
				int len = l1.size();
				textWithWs = new String[len];
				pre = new boolean[len];
				textWithWsMapping = new int[len];
				for (int i = 0; i < len; i++) {
					textWithWs[i] = l1.get(i);
					pre[i] = l2.get(i);
					textWithWsMapping[i] = l3.get(i); }
			}
			
			// textWithWs segments joined together with hyphens removed and sequences of preserved
			// white space replaced with a nbsp
			String joinedText;
			// mapping from character index in joinedText to segment index in textWithWs
			int[] joinedTextMapping;
			// byte array for tracking hyphenation positions, segment boundaries and boundaries of
			// sequences of preserved white space
			byte[] inputAttrs; {
				String[] textWithWsReplaced = new String[textWithWs.length];
				for (int i = 0; i < textWithWs.length; i++)
					textWithWsReplaced[i] = pre[i] ? ""+NBSP : textWithWs[i];
				Tuple2<String,byte[]> t = extractHyphens(join(textWithWsReplaced, RS), SHY, ZWSP);
				joinedText = t._1;
				inputAttrs = t._2;
				String[] nohyph = toArray(SEGMENT_SPLITTER.split(joinedText), String.class);
				joinedTextMapping = new int[join(nohyph).length()];
				int i = 0;
				int j = 0;
				for (String s : nohyph) {
					int l = s.length();
					for (int k = 0; k < l; k++)
						joinedTextMapping[i++] = j;
					j++; }
				t = extractHyphens(inputAttrs, joinedText, null, null, null, RS);
				joinedText = t._1;
				inputAttrs = t._2;
				if (joinedText.matches("\\xA0*"))
					return text;
				if (inputAttrs == null)
					inputAttrs = new byte[joinedText.length() - 1];
			}
			
			// add automatic hyphenation points to inputAttrs array
			{
				boolean someHyphenate = false;
				boolean someNotHyphenate = false;
				for (int i = 0; i < hyphenate.length; i++)
					if (hyphenate[i]) someHyphenate = true;
					else someNotHyphenate = true;
				if (someHyphenate) {
					byte[] autoHyphens = null;
					try {
						if (fullHyphenator == null) {
							logger.warn("hyphens:auto not supported");
							if (lineBreaker != null)
								throw new RuntimeException(); }
						else
							autoHyphens = fullHyphenator.hyphenate(joinedText); }
					catch (Exception e) {
						if (failWhenNonStandardHyphenation)
							throw e;
						else
							switch (handleNonStandardHyphenation) {
							case NON_STANDARD_HYPH_IGNORE:
								logger.warn("hyphens:auto can not be applied due to non-standard hyphenation points.");
								break;
							case NON_STANDARD_HYPH_FAIL:
								logger.error("hyphens:auto can not be applied due to non-standard hyphenation points.");
								throw e;
							case NON_STANDARD_HYPH_DEFER:
								if (forceBraille) {
									logger.error("hyphens:auto can not be applied due to non-standard hyphenation points.");
									throw e; }
								logger.info("Deferring hyphenation to formatting phase due to non-standard hyphenation points.");
								
								// TODO: split up text in words and only defer the words with non-standard hyphenation
								return text; }}
					if (autoHyphens != null) {
						if (someNotHyphenate) {
							int i = 0;
							for (int j = 0; j < text.length; j++) {
								if (hyphenate[j])
									while (i < autoHyphens.length && textWithWsMapping[joinedTextMapping[i]] < j + 1) i++;
								else {
									if (i > 0)
										autoHyphens[i - 1] = 0;
									while (i < autoHyphens.length && textWithWsMapping[joinedTextMapping[i]] < j + 1)
										autoHyphens[i++] = 0; }}}
						for (int i = 0; i < autoHyphens.length; i++)
							inputAttrs[i] += autoHyphens[i]; }}
			}
			
			// add letter information to inputAttrs array
			boolean someLetterSpacing = false; {
				for (int i = 0; i < letterSpacing.length; i++)
					if (letterSpacing[i] > 0) someLetterSpacing = true; }
			if (someLetterSpacing)
				// FIXME: don't hard-code the number 4
				inputAttrs = detectLetterBoundaries(inputAttrs, joinedText, (byte)4);
			
			// typeform var with the same length as joinedText
			Typeform[] _typeform = null;
			for (Typeform t : typeform)
				if (t != Typeform.PLAIN_TEXT) {
					_typeform = new Typeform[joinedText.length()];
					for (int i = 0; i < _typeform.length; i++)
						_typeform[i] = typeform[textWithWsMapping[joinedTextMapping[i]]];
					break; }
			
			// translate to braille with hyphens and restored white space
			String[] brailleWithWs;
			try {
				
				// translation result with hyphens and segment boundary marks
				String joinedBrailleWithoutHyphens;
				String joinedBraille;
				byte[] outputAttrs; {
					int[] inputAttrsAsInt = new int[inputAttrs.length];
					for (int i = 0; i < inputAttrs.length; i++)
						inputAttrsAsInt[i] = inputAttrs[i];
					TranslationResult r = translator.translate(joinedText, _typeform, null, inputAttrsAsInt);
					joinedBrailleWithoutHyphens = r.getBraille();
					int [] outputAttrsAsInt = r.getInterCharacterAttributes();
					if (outputAttrsAsInt != null) {
						outputAttrs = new byte[outputAttrsAsInt.length];
						for (int i = 0; i < outputAttrs.length; i++)
							outputAttrs[i] = (byte)outputAttrsAsInt[i];
						joinedBraille = insertHyphens(joinedBrailleWithoutHyphens, outputAttrs, SHY, ZWSP, US, RS); }
					else {
						joinedBraille = joinedBrailleWithoutHyphens;
						outputAttrs = null; }
				}
				
				// single segment
				if (textWithWs.length == 1)
					brailleWithWs = new String[]{joinedBraille};
				else {
					
					// split into segments
					{
						brailleWithWs = new String[textWithWs.length];
						int i = 0;
						int imax = joinedText.length();
						int kmax = textWithWs.length;
						int k = (i < imax) ? joinedTextMapping[i] : kmax;
						int l = 0;
						while (l < k) brailleWithWs[l++] = "";
						for (String s : SEGMENT_SPLITTER.split(joinedBraille)) {
							brailleWithWs[l++] = s;
							while (k < l)
								k = (++i < imax) ? joinedTextMapping[i] : kmax;
							while (l < k)
								brailleWithWs[l++] = ""; }
						if (l == kmax) {
							boolean wsLost = false;
							for (k = 0; k < kmax; k++)
								if (pre[k]) {
									Matcher m = Pattern.compile("\\xA0([\\xAD\\u200B]*)").matcher(brailleWithWs[k]);
									if (m.matches())
										brailleWithWs[k] = textWithWs[k] + m.group(1);
									else
										wsLost = true; }
							if (wsLost) {
								logger.warn("White space was not preserved (see detailed log for more info)");
								logger.debug("White space was lost in the output.\n"
								             + "Input: " + Arrays.toString(textWithWs) + "\n"
								             + "Output: " + Arrays.toString(brailleWithWs)); }}
						else {
							logger.warn("Text segmentation was lost (see detailed log for more info)");
							logger.debug("Text segmentation was lost in the output. Falling back to fuzzy mode.\n"
							             + "=> input segments: " + Arrays.toString(textWithWs) + "\n"
							             + "=> output segments: " + Arrays.toString(Arrays.copyOf(brailleWithWs, l)));
							brailleWithWs = null; }
					}
					
					// if some segment breaks were discarded, fall back on a fuzzy split method
					if (brailleWithWs == null) {
						
						// int array for tracking segment numbers
						// Note that we make the assumption here that the text is not longer than Integer.MAX_VALUE!
						int[] inputSegmentNumbers = joinedTextMapping;
						
						// split at all positions where the segment number is increased in the output
						TranslationResult r = translator.translate(joinedText, _typeform, inputSegmentNumbers, null);
						if (!r.getBraille().equals(joinedBrailleWithoutHyphens))
							throw new RuntimeException("Coding error");
						int[] outputSegmentNumbers = r.getCharacterAttributes();
						brailleWithWs = new String[textWithWs.length];
						boolean wsLost = false;
						StringBuffer b = new StringBuffer();
						int jmax = joinedBrailleWithoutHyphens.length();
						int kmax = textWithWs.length;
						int k = joinedTextMapping[0];
						int l = 0;
						while (l < k)
							brailleWithWs[l++] = "";
						for (int j = 0; j < jmax; j++) {
							if (outputSegmentNumbers[j] > l) {
								brailleWithWs[l] = b.toString();
								b = new StringBuffer();
								// FIXME: don't hard-code the number 8
								if (j > 0 && (outputAttrs[j - 1] & 8) == 8) {
									if (pre[l]) {
										Matcher m = Pattern.compile("\\xA0([\\xAD\\u200B]*)").matcher(brailleWithWs[l]);
										if (m.matches())
											brailleWithWs[l] = textWithWs[l] + m.group(1);
										else
											wsLost = true; }}
								else {
									if (pre[l])
										wsLost = true;
									if (l <= kmax && pre[l + 1]) {
										pre[l + 1] = false;
										wsLost = true; }}
								l++;
								while (outputSegmentNumbers[j] > l) {
									brailleWithWs[l] = "";
									if (pre[l])
										wsLost = true;
									l++; }}
							b.append(joinedBrailleWithoutHyphens.charAt(j));
							if (j < jmax - 1) {
								// FIXME: don't hard-code these numbers
								if ((outputAttrs[j] & 1) == 1)
									b.append(SHY);
								if ((outputAttrs[j] & 2) == 2)
									b.append(ZWSP);
								if ((outputAttrs[j] & 4) == 4)
									b.append(US); }}
						brailleWithWs[l] = b.toString();
						if (pre[l])
							if (brailleWithWs[l].equals(""+NBSP))
								brailleWithWs[l] = textWithWs[l];
							else
								wsLost = true;
						l++;
						while (l < kmax) {
							if (pre[l])
								wsLost = true;
							brailleWithWs[l++] = ""; }
						if (wsLost) {
							logger.warn("White space was not preserved: " + joinedText.replaceAll("\\s+"," "));
							logger.debug("White space was lost in the output.\n"
							             + "Input: " + Arrays.toString(textWithWs) + "\n"
							             + "Output: " + Arrays.toString(brailleWithWs)); }
					}
				}
			} catch (TranslationException e) {
				throw new RuntimeException(e);
			} catch (DisplayException e) {
				throw new RuntimeException(e); }
			
			// recombine white space segments with other segments
			String braille[] = new String[text.length];
			for (int i = 0; i < braille.length; i++)
				braille[i] = "";
			for (int j = 0; j < brailleWithWs.length; j++)
				braille[textWithWsMapping[j]] += brailleWithWs[j];
			
			// apply letter spacing
			if (someLetterSpacing)
				for (int i = 0; i < braille.length; i++)
					braille[i] = applyLetterSpacing(braille[i], letterSpacing[i]);
			
			return braille;
		}
		
		/*
		 * Detect where letter boundaries meet. Length of addTo must be one less than length of text.
		 */
		private static byte[] detectLetterBoundaries(byte[] addTo, String text, byte val) {
			if (addTo == null)
				addTo = new byte[text.length() - 1];
			for(int i = 0; i < addTo.length; i++){
				if(Character.isLetter(text.charAt(i)) && Character.isLetter(text.charAt(i+1)))
					addTo[i] |= val;
				if((text.charAt(i) == '-') || (text.charAt(i+1) == '-'))
					addTo[i] |= val;
				if((text.charAt(i) == '\u00ad')) // SHY is not actual character, so boundary only after SHY
					addTo[i] |= val;
				}
			return addTo;
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisTranslatorJnaImplProvider$LiblouisTranslatorImpl")
				.add("translator", translator)
				.add("hyphenator", hyphenator);
		}
	
		@Override
		public int hashCode() {
			final int prime = 31;
			int hash = 1;
			hash = prime * hash + translator.hashCode();
			hash = prime * hash + ((hyphenator == null) ? 0 : hyphenator.hashCode());
			hash = prime * hash + handleNonStandardHyphenation;
			return hash;
		}
	
		@Override
		public boolean equals(Object object) {
			if (this == object)
				return true;
			if (object == null)
				return false;
			if (object.getClass() != LiblouisTranslatorImpl.class)
				return false;
			LiblouisTranslatorImpl that = (LiblouisTranslatorImpl)object;
			if (!this.translator.equals(that.translator))
				return false;
			if (this.hyphenator == null && that.hyphenator != null)
				return false;
			if (this.hyphenator != null && that.hyphenator == null)
				return false;
			if (!this.hyphenator.equals(that.hyphenator))
				return false;
			return true;
		}
	}
	
	private static class HandleTextTransformUncontracted extends AbstractBrailleTranslator implements LiblouisTranslator {
		
		final LiblouisTranslator translator;
		final LiblouisTranslator nonContractingTranslator;
		
		HandleTextTransformUncontracted(LiblouisTranslator translator,
		                                LiblouisTranslator nonContractingTranslator) {
			this.translator = translator;
			this.nonContractingTranslator = nonContractingTranslator;
		}
		
		@Override
		public LiblouisTable asLiblouisTable() {
			return translator.asLiblouisTable();
		}

		@Override
		public FromTypeformedTextToBraille fromTypeformedTextToBraille() {
			return translator.fromTypeformedTextToBraille();
		}
		
		@Override
		public FromStyledTextToBraille fromStyledTextToBraille() {
			return fromStyledTextToBraille;
		}
		
		private static abstract class TransformImpl<T> {
			
			abstract java.lang.Iterable<T> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to,
			                                         boolean uncontracted);
			
			List<T> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
				if (to < 0)
					to = size(styledText);
				if (from < 0 || from > to)
					throw new IndexOutOfBoundsException();
				List<T> transformed = new ArrayList<>();
				if (from == to) return transformed;
				List<CSSStyledText> buffer = new ArrayList<CSSStyledText>();
				boolean curUncontracted = false;
				for (CSSStyledText st : styledText) {
					SimpleInlineStyle style = st.getStyle();
					boolean uncontracted; {
						uncontracted = false;
						if (style != null) {
							CSSProperty val = style.getProperty("text-transform");
							if (val != null) {
								if (val == TextTransform.list_values) {
									TermList values = style.getValue(TermList.class, "text-transform");
									
									// According to the spec values should be "applied" from left to right, and
									// values of inner elements always come before values of outer elements (see
									// http://braillespecs.github.io/braille-css/#the-text-transform-property). This
									// is the most logical situation in most cases. However the order in which
									// "uncontracted" and "contracted" should overwrite each other is exactly the
									// opposite. Therefore invert the list.
									Iterator<Term<?>> it = Lists.reverse(values).iterator();
									while (it.hasNext()) {
										String tt = ((TermIdent)it.next()).getValue();
										if (tt.equals("uncontracted")) {
											uncontracted = true;
											it.remove(); }
										else if (tt.equals("contracted")) { // means "allow contracted"
											uncontracted = false; // "contracted" overwrites "uncontracted" if it comes later in the list
											it.remove(); }}
									if (values.isEmpty())
										style.removeProperty("text-transform"); }}}
					}
					if (uncontracted != curUncontracted && !buffer.isEmpty()) {
						if (from < buffer.size())
							for (T s : transform(buffer, from, to < buffer.size() ? to : -1, curUncontracted))
								transformed.add(s);
						from -= buffer.size();
						if (from < 0) from = 0;
						if (to > 0) {
							to -= buffer.size();
							if (to <= 0)
								return transformed; }
						buffer = new ArrayList<CSSStyledText>(); }
					curUncontracted = uncontracted;
					buffer.add(st); }
				if (!buffer.isEmpty() && from < buffer.size())
					for (T s : transform(buffer, from, to < buffer.size() ? to : -1, curUncontracted))
						transformed.add(s);
				return transformed;
			}
		}
		
		private final FromStyledTextToBraille fromStyledTextToBraille = new FromStyledTextToBraille() {
			TransformImpl<String> impl = new TransformImpl<String>() {
				java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to,
				                                     boolean uncontracted) {
					return (uncontracted ? nonContractingTranslator : translator)
					       .fromStyledTextToBraille().transform(styledText, from, to);
				}
			};
			public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
				return impl.transform(styledText, from, to);
			}
		};
		
		@Override
		public LineBreakingFromStyledText lineBreakingFromStyledText() {
			return lineBreakingFromStyledText;
		}
		
		private final LineBreakingFromStyledText lineBreakingFromStyledText = new LineBreakingFromStyledText() {
			TransformImpl<LineIterator> impl = new TransformImpl<LineIterator>() {
				java.lang.Iterable<LineIterator> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to,
				                                           boolean uncontracted) {
					return Collections.singleton(
						(uncontracted ? nonContractingTranslator : translator)
						.lineBreakingFromStyledText().transform(styledText, from, to));
				}
			};
			public LineIterator transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
				return concatLineIterators(impl.transform(styledText, from, to));
			}
		};
	}
	
	private static class LiblouisTranslatorHyphenatorImpl extends LiblouisTranslatorImpl {
		
		private LiblouisTranslatorHyphenatorImpl(Translator translator,
			                                     int handleNonStandardHyphenation,
			                                     String mainLocale,
			                                     Normalizer.Form unicodeNormalization) {
			super(translator, handleNonStandardHyphenation, mainLocale, unicodeNormalization);
			fullHyphenator = new LiblouisTranslatorAsFullHyphenator(translator);
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("o.d.p.b.liblouis.impl.LiblouisTranslatorJnaImplProvider$LiblouisTranslatorImpl")
				.add("translator", translator)
				.add("hyphenator", "self");
		}
	}
	
	private interface FullHyphenator {
		public byte[] hyphenate(String text);
	}
	
	private static class LiblouisTranslatorAsFullHyphenator implements FullHyphenator {
		
		private final Translator translator;
		
		private LiblouisTranslatorAsFullHyphenator(Translator translator) {
			this.translator = translator;
		}
		
		public byte[] hyphenate(String text) {
			try { return translator.hyphenate(text); }
			catch (TranslationException e) {
				throw new RuntimeException(e); }
		}
	}
	
	private static class HyphenatorAsFullHyphenator implements FullHyphenator {
		
		private final Hyphenator.FullHyphenator hyphenator;
		
		private HyphenatorAsFullHyphenator(Hyphenator hyphenator) {
			this.hyphenator = hyphenator.asFullHyphenator();
		}
		
		public byte[] hyphenate(String text) {
			return extractHyphens(hyphenator.transform(text), SHY, ZWSP)._2;
		}
	}
	
	private static BrailleTranslator.LineIterator concatLineIterators(List<BrailleTranslator.LineIterator> iterators) {
		if (iterators.size() == 0)
			return new BrailleTranslator.LineIterator() {
				public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
					return "";
				}
				public String getTranslatedRemainder() {
					return "";
				}
				public int countRemaining() {
					return 0;
				}
				public boolean hasNext() {
					return false;
				}
				public BrailleTranslator.LineIterator copy() {
					return this;
				}
				public boolean supportsMetric(String metric) {
					return false;
				}
				public double getMetric(String metric) {
					throw new UnsupportedMetricException("Metric not supported: " + metric);
				}
			};
		else if (iterators.size() == 1 && iterators.get(0) != null)
			return iterators.get(0);
		else
			return new ConcatLineIterators(iterators);
	}
	
	private static class ConcatLineIterators implements BrailleTranslator.LineIterator {
		
		final List<BrailleTranslator.LineIterator> iterators;
		BrailleTranslator.LineIterator current;
		int currentIndex = 0;
		
		ConcatLineIterators(List<BrailleTranslator.LineIterator> iterators) {
			this.iterators = iterators;
			currentIndex = -1;
			current = null;
			computeCurrent();
		}
		
		void computeCurrent() {
			while (current == null || !current.hasNext())
				if (currentIndex + 1 < iterators.size())
					current = iterators.get(++currentIndex);
				else {
					current = null;
					break; }
		}
		
		public String nextTranslatedRow(int limit, boolean force, boolean wholeWordsOnly) {
			String row = "";
			while (limit > row.length()) {
				if (current == null) break;
				row += current.nextTranslatedRow(limit - row.length(), force, wholeWordsOnly);
				computeCurrent(); }
			return row;
		}
		
		public String getTranslatedRemainder() {
			String remainder = "";
			if (current == null) return remainder;
			for (int i = currentIndex; i < iterators.size(); i++)
				if (iterators.get(i) != null)
					remainder += iterators.get(i).getTranslatedRemainder();
			return remainder;
		}
		
		public int countRemaining() {
			int remaining = 0;
			if (current == null) return remaining;
			for (int i = currentIndex; i < iterators.size(); i++)
				if (iterators.get(i) != null)
					remaining += iterators.get(i).countRemaining();
			return remaining;
		}
		
		public boolean hasNext() {
			computeCurrent();
			return current != null;
		}
		
		public ConcatLineIterators copy() {
			List<BrailleTranslator.LineIterator> iteratorsCopy = new ArrayList<>(iterators.size() - currentIndex);
			for (int i = currentIndex; i < iterators.size(); i++)
				if (iterators.get(i) != null)
					iteratorsCopy.add((BrailleTranslator.LineIterator)iterators.get(i).copy());
			return new ConcatLineIterators(iteratorsCopy);
		}
		
		public boolean supportsMetric(String metric) {
			return false;
		}
		
		public double getMetric(String metric) {
			throw new UnsupportedMetricException("Metric not supported: " + metric);
		}
	}
	
	/**
	 * @param style An inline CSS style
	 * @return the corresponding Typeform object.
	 * @see <a href="http://liblouis.googlecode.com/svn/documentation/liblouis.html#lou_translateString">lou_translateString</a>
	 */
	protected static Typeform typeformFromInlineCSS(SimpleInlineStyle style, Translator table, Map<String,Typeform> supportedTypeforms) {
		Typeform typeform = Typeform.PLAIN_TEXT;
		for (String prop : style.getPropertyNames()) {
			if (prop.equals("font-style")) {
				CSSProperty value = style.getProperty(prop);
				if (value == FontStyle.ITALIC || value == FontStyle.OBLIQUE) {
					Typeform t = supportedTypeforms.get("italic");
					if (t != null)
						typeform = typeform.add(t);
					else
						logger.warn("Inline CSS property {} not supported: emphclass 'italic' not defined in table {}",
						            style.getSourceDeclaration(prop),
						            table.getTable());
					continue; }}
			else if (prop.equals("font-weight")) {
				CSSProperty value = style.getProperty(prop);
				if (value == FontWeight.BOLD) {
					Typeform t = supportedTypeforms.get("bold");
					if (t != null)
						typeform = typeform.add(t);
					else
						logger.warn("Inline CSS property {} not supported: emphclass 'bold' not defined in table {}",
						            style.getSourceDeclaration(prop),
						            table.getTable());
					continue; }}
			else if (prop.equals("text-decoration")) {
				CSSProperty value = style.getProperty(prop);
				if (value == TextDecoration.UNDERLINE) {
					Typeform t = supportedTypeforms.get("underline");
					if (t != null)
						typeform = typeform.add(t);
					else
						logger.warn("Inline CSS property {} not supported: emphclass 'underline' not defined in table {}",
						            style.getSourceDeclaration(prop),
						            table.getTable());
					continue; }}
			logger.warn("Inline CSS property {} not supported", style.getSourceDeclaration(prop)); }
		return typeform;
	}
	
	/**
	 * @param text The text to be transformed.
	 * @param textTransform A text-transform value as a space separated list of keywords.
	 * @return the transformed text, or the original text if no transformations were performed.
	 */
	protected static String textFromTextTransform(String text, TermList textTransform) {
		for (Term<?> t : textTransform) {
			String tt = ((TermIdent)t).getValue();
			if (tt.equals("uppercase"))
				text = text.toUpperCase();
			else if (tt.equals("lowercase"))
				text = text.toLowerCase();
			else if (!tt.equals("uncontracted") && !LOUIS_TEXT_TRANSFORM.matcher(tt).matches())
				logger.warn("text-transform: {} not supported", tt);
		}
		return text;
	}
	
	/**
	 * @param textTransform A text-transform value as a space separated list of keywords.
	 * @return the corresponding Typeform object. Recognized values are:
	 * * -louis-italic
	 * * -louis-underline
	 * * -louis-bold
	 * * -louis-computer
	 * * -louis-...
	 * These values can be added for multiple emphasis.
	 * @see <a href="http://liblouis.googlecode.com/svn/documentation/liblouis.html#lou_translateString">lou_translateString</a>
	 */
	protected static Typeform typeformFromTextTransform(TermList textTransform, Translator table, Map<String,Typeform> supportedTypeforms) {
		Typeform typeform = Typeform.PLAIN_TEXT;
		for (Term<?> t : textTransform) {
			String tt = ((TermIdent)t).getValue();
			Matcher m = LOUIS_TEXT_TRANSFORM.matcher(tt);
			if (m.matches()) {
				String emphClass = m.group("class");
				if (emphClass.equals("computer") || emphClass.equals("comp")) {
					typeform = typeform.add(Typeform.COMPUTER);
				} else {
					if (emphClass.equals("ital"))
						emphClass= "italic";
					else if (emphClass.equals("under"))
						emphClass = "underline";
					Typeform tf = supportedTypeforms.get(emphClass);
					if (tf != null)
						typeform = typeform.add(tf);
					else
						logger.warn("text-transform: {} not supported: emphclass '{}' not defined in table {}",
						            tt,
						            emphClass,
						            table.getTable());
				}
				continue;
			} else if (tt.equals("uppercase") || tt.equals("lowercase") || tt.equals("uncontracted")) {
				// - uppercase and lowercase handled in textFromTextTransform
				// - uncontracted can be ignored because the current contraction grade is already 0
				//   (otherwise uncontracted would already have been handled in
				//   HandleTextTransformUncontracted)
				continue;
			}
			logger.warn("text-transform: {} not supported", tt);
		}
		return typeform;
	}
	
	private final static Pattern LOUIS_TEXT_TRANSFORM = Pattern.compile("^-?(lib)?louis-(?<class>.+)$");
	
	@SuppressWarnings("unused")
	private static int mod(int a, int n) {
		int result = a % n;
		if (result < 0)
			result += n;
		return result;
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LiblouisTranslatorJnaImplProvider.class);
	
}
