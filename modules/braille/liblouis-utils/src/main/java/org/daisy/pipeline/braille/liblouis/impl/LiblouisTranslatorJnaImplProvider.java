package org.daisy.pipeline.braille.liblouis.impl;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import static java.util.Collections.singleton;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static com.google.common.collect.Iterables.any;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.toArray;
import com.google.common.collect.Iterators;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.FontStyle;
import cz.vutbr.web.css.CSSProperty.FontWeight;
import cz.vutbr.web.css.CSSProperty.TextDecoration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;

import org.daisy.braille.css.BrailleCSSProperty.BrailleCharset;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.LetterSpacing;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator;
import org.daisy.pipeline.braille.common.AbstractBrailleTranslator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.AbstractHyphenator.util.NoHyphenator;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Function;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.memoize;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables.transform;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logSelect;
import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CompoundBrailleTranslator;
import org.daisy.pipeline.braille.common.Hyphenator;
import org.daisy.pipeline.braille.common.Hyphenator.NonStandardHyphenationException;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.TransformationException;
import org.daisy.pipeline.braille.common.TransformProvider;
import org.daisy.pipeline.braille.common.UnityBrailleTranslator;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.insertHyphens;
import static org.daisy.pipeline.braille.common.util.Strings.join;
import static org.daisy.pipeline.braille.common.util.Strings.splitInclDelimiter;
import static org.daisy.pipeline.braille.common.util.Tuple2;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;
import org.daisy.pipeline.braille.liblouis.impl.LiblouisTableJnaImplProvider.LiblouisTableJnaImpl;
import org.daisy.pipeline.braille.liblouis.pef.LiblouisDisplayTableBrailleConverter;

import org.liblouis.DisplayException;
import org.liblouis.DisplayTable;
import org.liblouis.TranslationException;
import org.liblouis.TranslationResult;
import org.liblouis.Translator;
import org.liblouis.Typeform;

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
	private final static char NBSP = '\u00A0'; // no-break space
	private final static char LS = '\u2028';   // line separator
	private final static char RS = '\u001E';   // (for segmentation)
	private final static char US = '\u001F';   // (for segmentation)
	private final static Splitter SEGMENT_SPLITTER = Splitter.on(RS);
	private final static Pattern ON_NBSP_SPLITTER = Pattern.compile("[" + SHY + ZWSP + "]*" + NBSP + "[" + SHY + ZWSP + NBSP + "]*");
	private final static Pattern ON_SPACE_SPLITTER = Pattern.compile("[" + SHY + ZWSP + "]*[\\x20\t\\n\\r\\u2800" + NBSP + "][" + SHY + ZWSP + "\\x20\t\\n\\r\\u2800" + NBSP+ "]*");
	private final static Pattern LINE_SPLITTER = Pattern.compile("[" + SHY + ZWSP + "]*[\\n\\r][" + SHY + ZWSP + "\\n\\r]*");
	private final static Pattern WORD_SPLITTER = Pattern.compile("[\\x20\t\\n\\r\\u2800" + NBSP + "]+");
	
	private LiblouisTableJnaImplProvider tableProvider;
	
	@Reference(
		name = "LiblouisTableJnaImplProvider",
		unbind = "-",
		service = LiblouisTableJnaImplProvider.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindLiblouisTableJnaImplProvider(LiblouisTableJnaImplProvider provider) {
		tableProvider = provider;
		logger.debug("Registering Liblouis table provider: " + provider);
	}
	
	protected void unbindLiblouisTableJnaImplProvider(LiblouisTableJnaImplProvider provider) {
		tableProvider = null;
	}

	private final static Iterable<LiblouisTranslator> empty
	= Iterables.<LiblouisTranslator>empty();
	
	private final static List<String> supportedInput = ImmutableList.of("text-css");
	
	protected final Iterable<LiblouisTranslator> _get(Query query) {
		MutableQuery q = mutableQuery(query);
		for (Feature f : q.removeAll("input"))
			if (!supportedInput.contains(f.getValue().get()))
				return empty;
		if (q.containsKey("output")) {
			String v = q.removeOnly("output").getValue().get();
			if ("braille".equals(v)) {}
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
		if (q.containsKey("handle-non-standard-hyphenation"))
			v = q.removeOnly("handle-non-standard-hyphenation").getValue().get();
		else
			v = "ignore";
		final int handleNonStandardHyphenation = v.equalsIgnoreCase("fail") ?
			LiblouisTranslatorImpl.NON_STANDARD_HYPH_FAIL : v.equalsIgnoreCase("defer") ?
			LiblouisTranslatorImpl.NON_STANDARD_HYPH_DEFER :
			LiblouisTranslatorImpl.NON_STANDARD_HYPH_IGNORE;
		if (table != null)
			q.add("table", table);
		q.add("white-space");
		Iterable<LiblouisTranslator> translators = memoize(
			getSimpleTranslator(
				q.asImmutable(),
				handleNonStandardHyphenation));
		if (translators.apply(NOP_LOGGER).iterator().hasNext()) {
			// all translators use the same display table
			// FIXME: display table has already been computed in the getSimpleTranslator() call above
			DisplayTable displayTable = tableProvider.withContext(NOP_LOGGER).get(q).iterator().next().getDisplayTable();
			BrailleTranslator unityTranslator = new UnityBrailleTranslator(
				new LiblouisDisplayTableBrailleConverter(displayTable), false);
			return Iterables.transform(
				translators,
				new Function<LiblouisTranslator,LiblouisTranslator>() {
					public LiblouisTranslator _apply(LiblouisTranslator t) {
						return __apply(logCreate(new HandleTextTransformNone(t, unityTranslator))); }});
		} else
			return translators;
	}

	private Iterable<LiblouisTranslator> getSimpleTranslator(Query query, int handleNonStandardHyphenation) {
		return transform(
			logSelect(query, tableProvider),
			new Function<LiblouisTableJnaImpl,LiblouisTranslator>() {
				public LiblouisTranslator _apply(LiblouisTableJnaImpl table) {
					return __apply(
						logCreate((LiblouisTranslator)new LiblouisTranslatorImpl(
									table,
									null,
									handleNonStandardHyphenation)));
				}
			}
		);
	}
	
	@Override
	public ToStringHelper toStringHelper() {
		return MoreObjects.toStringHelper("LiblouisTranslatorJnaImplProvider");
	}
	
	class LiblouisTranslatorImpl extends AbstractBrailleTranslator implements LiblouisTranslator {
		
		private final LiblouisTableJnaImpl table;
		protected final Translator translator;
		private final DisplayTable displayTable;
		private Hyphenator hyphenator;
		protected FullHyphenator fullHyphenator;
		private Hyphenator.LineBreaker lineBreaker;
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
		
		public final static int NON_STANDARD_HYPH_IGNORE = 0;
		public final static int NON_STANDARD_HYPH_FAIL = 1;
		public final static int NON_STANDARD_HYPH_DEFER = 2;
		
		/**
		 * The following assumptions are made for <code>displayTable</code>:
		 * <ul>
		 *   <li> the NBSP character, if present in the display table, represents a preserved space, and no other
		 *        characters do </li>
		 *   <li> the LS character, if present in the display table, represents a preserved line break, and no other
		 *        characters do </li>
		 *   <li> the SPACE, TAB, CR, LF and BLANK BRAILLE PATTERN characters, if present in the display table, all
		 *        represent white space, and no other characters do </li>
		 * </ul>
		 */
		LiblouisTranslatorImpl(LiblouisTableJnaImpl table,
		                       Hyphenator hyphenator,
		                       int handleNonStandardHyphenation) {
			super(hyphenator, null);
			this.table = table;
			this.translator = table.getTranslator();
			this.displayTable = table.getDisplayTable();
			this.handleNonStandardHyphenation = handleNonStandardHyphenation;
			this.supportedTypeforms
				= translator.getSupportedTypeforms().stream().collect(Collectors.toMap(Typeform::getName, e -> e));
			this.unicodeNormalization = table.getUnicodeNormalizationForm();
			this.hyphenator = hyphenator;
			if (hyphenator == null)
				fullHyphenator = compoundWordHyphenator;
			else {
				try {
					fullHyphenator = new HyphenatorAsFullHyphenator(hyphenator); }
				catch (UnsupportedOperationException e) {}
				try {
					lineBreaker = hyphenator.asLineBreaker(); }
				catch (UnsupportedOperationException e) {}}
		}
		
		private LiblouisTranslatorImpl(LiblouisTranslatorImpl from, Hyphenator hyphenator) {
			super(from);
			this.table = from.table;
			this.translator = from.translator;
			this.displayTable = from.displayTable;
			this.handleNonStandardHyphenation = from.handleNonStandardHyphenation;
			this.supportedTypeforms = from.supportedTypeforms;
			this.unicodeNormalization = from.unicodeNormalization;
			this.hyphenator = hyphenator;
			if (hyphenator == null)
				fullHyphenator = compoundWordHyphenator;
			else {
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
		
		@Override
		public LiblouisTranslatorImpl _withHyphenator(Hyphenator hyphenator) {
			if (hyphenator == this.hyphenator)
				return this;
			LiblouisTranslatorImpl t = new LiblouisTranslatorImpl(this, hyphenator);
			LiblouisTranslatorJnaImplProvider.this.rememberId(t);
			return t;
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
		
		/**
		 * @throw TransformationException if underlying hyphenator throws {@link
		 *                                NonStandardHyphenationException}
		 */
		@Override
		public FromStyledTextToBraille fromStyledTextToBraille() {
			if (fromStyledTextToBraille == null)
				fromStyledTextToBraille = new FromStyledTextToBraille() {
					public java.lang.Iterable<String> transform(java.lang.Iterable<CSSStyledText> styledText, int from, int to)
							throws TransformationException {
						try {
							List<String> result = LiblouisTranslatorImpl.this.transform(styledText, false, false);
							if (to < 0) to = result.size();
							if (from > 0 || to < result.size())
								return result.subList(from, to);
							else
								return result;
						} catch (NonStandardHyphenationException e) {
							throw new TransformationException(e);
						}
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
		
		class LineBreaker extends DefaultLineBreaker {
			
			final FromStyledTextToBraille fullTranslator;
			
			protected LineBreaker(FromStyledTextToBraille fullTranslator) {
				// Note that `displayTable.encode('\u2800')` is always a space because of the addition of spaces.dis
				super(displayTable.encode('\u2800'),
				      displayTable.encode('\u2824'),
				      new LiblouisDisplayTableBrailleConverter(displayTable),
				      logger);
				this.fullTranslator = fullTranslator;
			}
			
			protected BrailleStream translateAndHyphenate(java.lang.Iterable<CSSStyledText> styledText, int from, int to) {
				// styledText is cloned because we are mutating the style objects
				java.lang.Iterable<CSSStyledText> styledTextCopy
					= org.daisy.pipeline.braille.common.util.Iterables.clone(styledText);
				java.lang.Iterable<String> braille;
				try {
					braille = fullTranslator.transform(styledTextCopy); }
				catch (NonStandardHyphenationException e) {
					return new BrailleStreamImpl(styledText,
					                             from,
					                             to); }
				// style is mutated and may not be empty
				Iterator<SimpleInlineStyle> style = Iterators.transform(styledTextCopy.iterator(), CSSStyledText::getStyle);
				List<String> brailleWithPreservedWS = new ArrayList<>(); {
					for (String s : braille) {
						// the only property expected in the output is white-space
						// ignore other properties
						SimpleInlineStyle st = style.next();
						if (st != null) {
							CSSProperty ws = st.getProperty("white-space");
							if (ws != null) {
								if (ws == WhiteSpace.PRE_WRAP)
									s = s.replaceAll("[\\x20\t\\u2800]+", "$0"+ZWSP)
										.replaceAll("[\\x20\t\\u2800]", ""+NBSP);
								if (ws == WhiteSpace.PRE_WRAP || ws == WhiteSpace.PRE_LINE)
									s = s.replaceAll("[\\n\\r]", ""+LS); }}
						brailleWithPreservedWS.add(s);
					}
				}
				StringBuilder joined = new StringBuilder();
				int fromChar = 0;
				int toChar = to >= 0 ? 0 : -1;
				for (String s : brailleWithPreservedWS) {
					joined.append(s);
					if (--from == 0)
						fromChar = joined.length();
					if (--to == 0)
						toChar = joined.length();
				}
				return new FullyHyphenatedAndTranslatedString(joined.toString(), fromChar, toChar);
			}
			
			class BrailleStreamImpl implements BrailleStream {
				
				final Locale[] languages;
				
				// FIXME: remove duplication!!
				
				// convert style into typeform, hyphenate, preserveLines, preserveSpace and letterSpacing arrays
				final Typeform[] typeform;
				final boolean[] hyphenate;
				final boolean[] preserveLines;
				final boolean[] preserveSpace;
				final int[] letterSpacing;
				
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
				
				// mapping from character (codepoint) index in joinedText to segment index in textWithWs
				int[] joinedTextMapping;
				
				// byte array for tracking hyphenation positions
				byte[] manualHyphens;
				
				// translation result without hyphens and with preserved white space not restored
				String joinedBraille;
				
				// mapping from character (codepoint) index in joinedBraille to character (codepoint) index in joinedText
				int[] characterIndicesInBraille;
				
				// mapping from inter-character (codepoint) index in joinedBraille to inter-character (codepoint) index in joinedText
				// 1-based because a inter-character attribute value of "0" in the output of Liblouis means "no attribute"
				int[] interCharacterIndicesInBraille;
				
				// current position (codepoint index) in input and output (joinedText and joinedBraille)
				int curPos = -1;
				int curPosInBraille = -1;
				int endPos = -1;
				int endPosInBraille = -1;
				final int to;
				
				BrailleStreamImpl(java.lang.Iterable<CSSStyledText> styledText,
				                  int from,
				                  int to) {
					
					// convert Iterable<CSSStyledText> into an text array and a style array
					int size = size(styledText);
					if (to < 0) to = size;
					this.to = to;
					
					// FIXME: handle from and to properly
					String[] text = new String[size];
					SimpleInlineStyle[] styles = new SimpleInlineStyle[size];
					languages = new Locale[size]; {
						int i = 0;
						for (CSSStyledText t : styledText) {
							text[i] = t.getText();
							styles[i] = t.getStyle();
							languages[i] = t.getLanguage();
							i++; }}
					
					// perform Unicode normalization
					if (unicodeNormalization != null)
						for (int k = 0; k < text.length; k++)
							text[k] = Normalizer.normalize(text[k], unicodeNormalization);
					
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
										// "text-transform: none" is handled by HandleTextTransformNone, but
										// HandleTextTransformNone is a CompoundBrailleTranslator and
										// CompoundBrailleTranslator puts "text-transform: none" on (already translated)
										// context segments. We assume that all Liblouis tables correctly handle Unicode
										// braille. If this is not the case, it is not the end of the words because this
										// is a context segment.
										val = style.getProperty("braille-charset");
										if (val != null) {
											if (val == BrailleCharset.CUSTOM)
												// translate to Unicode braille
												text[i] = displayTable.decode(text[i]);
											style.removeProperty("braille-charset"); }
										style.removeProperty("text-transform");
										continue; }
									else if (val == TextTransform.AUTO) {}
									else if (val == TextTransform.list_values) {
										TermList values = style.getValue(TermList.class, "text-transform");
										text[i] = textFromTextTransform(text[i], values);
										typeform[i] = typeform[i].add(typeformFromTextTransform(values, translator, supportedTypeforms)); }
									style.removeProperty("text-transform"); }
								val = style.getProperty("hyphens");
								if (val != null) {
									if (val == Hyphens.AUTO)
										hyphenate[i] = true;
									else if (val == Hyphens.NONE)
										text[i] = extractHyphens(text[i], false, SHY, ZWSP)._1;
									style.removeProperty("hyphens"); }
								val = style.getProperty("letter-spacing");
								if (val != null) {
									if (val == LetterSpacing.length) {
										letterSpacing[i] = style.getValue(TermInteger.class, "letter-spacing").getIntValue();
										if (letterSpacing[i] < 0) {
											logger.warn("letter-spacing: {} not supported, must be non-negative", val);
											letterSpacing[i] = 0; }}
									style.removeProperty("letter-spacing"); }
								typeform[i] = typeform[i].add(typeformFromInlineCSS(style, translator, supportedTypeforms));
								for (String prop : style.getPropertyNames())
									logger.warn("{}: {} not supported", prop, style.get(prop)); }}
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
						Tuple2<String,byte[]> t = extractHyphens(join(textWithWsReplaced, RS), true, SHY, ZWSP);
						manualHyphens = t._2;
						String[] nohyph = toArray(SEGMENT_SPLITTER.split(t._1), String.class);
						joinedTextMapping = new int[lengthByCodePoints(join(nohyph))];
						int i = 0;
						int j = 0;
						for (String s : nohyph) {
							int l = lengthByCodePoints(s);
							for (int k = 0; k < l; k++)
								joinedTextMapping[i++] = j;
							j++; }
						t = extractHyphens(manualHyphens, t._1, true, null, null, null, RS);
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
				}
				
				public String next(final int limit, final boolean force, boolean allowHyphens) {
					String next = "";
					if (limit > 0) {
					int available = limit;
				  segments: while (true) {
						if (curPos == endPos)
							break;
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
						String segment = substringByCodePoints(joinedText, curPos, curSegmentEnd);
						String segmentInBraille = joinedBraille.substring(curPosInBraille, curSegmentEndInBraille);
						byte[] segmentManualHyphens = manualHyphens != null
							? Arrays.copyOfRange(manualHyphens, curPos, curSegmentEnd - 1)
							: null;
						
						// restore preserved white space segments
						if (pre[curSegment]) {
							Matcher m = Pattern.compile("\\xA0([\\xAD\\u200B]*)").matcher(segmentInBraille);
							if (m.matches()) {
								String restoredSpace = segment.replaceAll("[\\x20\t\\u2800]", ""+NBSP)
								                              .replaceAll("[\\n\\r]", ""+LS) + m.group(1);
								next += restoredSpace;
								available -= lengthByCodePoints(restoredSpace);
								curPos = curSegmentEnd;
								curPosInBraille = curSegmentEndInBraille;
								continue segments; }}
						
						// don't hyphenate if the segment fits in the available space
						// FIXME: we don't know for sure that the segment fits because there might be letter spacing!
						if (segmentInBraille.length() <= available) {
							segmentInBraille = addLetterSpacing(segment, segmentInBraille, curPos, curPosInBraille,
							                                    letterSpacing[textWithWsMapping[curSegment]]);
							next += segmentInBraille;
							available -= segmentInBraille.length();
							curPos = curSegmentEnd;
							curPosInBraille = curSegmentEndInBraille;
							continue segments; }
						
						// don't hyphenate if hyphenation is disabled for this segment, but do break compound words with a hyphen
						Locale language = languages[textWithWsMapping[curSegment]];
						if (!hyphenate[textWithWsMapping[curSegment]]) {
							segmentInBraille = addHyphensAndLetterSpacing(compoundWordHyphenator,
							                                              segment, segmentInBraille, curPos, curPosInBraille,
							                                              segmentManualHyphens, language,
							                                              letterSpacing[textWithWsMapping[curSegment]]);
							next += segmentInBraille;
							available -= segmentInBraille.length();
							curPos = curSegmentEnd;
							curPosInBraille = curSegmentEndInBraille;
							continue segments; }
						
						// try standard hyphenation of the whole segment
						if (fullHyphenator != null) {
							if (fullHyphenator == compoundWordHyphenator)
								logger.warn("hyphens: auto not supported");
							try {
								segmentInBraille = addHyphensAndLetterSpacing(fullHyphenator, segment, segmentInBraille, curPos, curPosInBraille,
								                                              segmentManualHyphens, language,
								                                              letterSpacing[textWithWsMapping[curSegment]]);
								next += segmentInBraille;
								available -= segmentInBraille.length();
								curPos = curSegmentEnd;
								curPosInBraille = curSegmentEndInBraille;
								continue segments; }
							catch (NonStandardHyphenationException e) {}}
						
						// loop over words in segment
						Matcher m = WORD_SPLITTER.matcher(segment);
						int segmentStart = curPos;
						boolean foundSpace;
						while ((foundSpace = m.find()) || curPos < curSegmentEnd) {
							int wordEnd = foundSpace ? segmentStart + m.start() : curSegmentEnd;
							if (wordEnd > curPos) {
								int wordEndInBraille = positionInBraille(wordEnd);
								if (wordEndInBraille > curPosInBraille) {
									String word = substringByCodePoints(joinedText, curPos, wordEnd);
									String wordInBraille = joinedBraille.substring(curPosInBraille, wordEndInBraille);
									byte[] wordManualHyphens = manualHyphens != null
										? Arrays.copyOfRange(manualHyphens, curPos, wordEnd - 1)
										: null;
									
									// don't hyphenate if word fits in the available space
									if (wordInBraille.length() <= available) {
										next += wordInBraille;
										available -= wordInBraille.length();
										curPos = wordEnd;
										curPosInBraille = wordEndInBraille; }
									else {
										
										// try standard hyphenation of the whole word
										try {
											if (fullHyphenator == null) throw new NonStandardHyphenationException();
											wordInBraille = addHyphensAndLetterSpacing(fullHyphenator, word, wordInBraille, curPos, curPosInBraille,
											                                           wordManualHyphens, language,
											                                           letterSpacing[textWithWsMapping[curSegment]]);
											next += wordInBraille;
											available -= wordInBraille.length();
											curPos = wordEnd;
											curPosInBraille = wordEndInBraille; }
										catch (NonStandardHyphenationException ee) {
											
											// before we try non-standard hyphenation, return what we have already
											// we do this because we are not sure that the value of "available" is accurate
											// this way we leave the responsibility of white space normalisation to DefaultLineBreaker
											// FIXME: remove the "available" variable
											if (!next.isEmpty())
												break segments;
											
											// try non-standard hyphenation
											if (lineBreaker == null) throw ee;
											Hyphenator.LineIterator lines = lineBreaker.transform(word, language);
											
											// do a binary search for the optimal break point
											LineBreakSolution bestSolution = null;
											int left = 1;
											int right = lengthByCodePoints(word) - 1;
											int textAvailable = available;
											if (textAvailable > right)
												textAvailable = right;
											if (textAvailable < left)
												break segments;
											while (true) {
												String line = lines.nextLine(textAvailable, force && next.isEmpty(), allowHyphens);
												String replacementWord = line + lines.remainder();
												if (updateInput(curPos, wordEnd, replacementWord)) {
													wordEnd = curPos + lengthByCodePoints(replacementWord);
													updateBraille(); }
												int lineEnd = curPos + lengthByCodePoints(line);
												int lineEndInBraille = positionInBraille(lineEnd);
												String lineInBraille = joinedBraille.substring(curPosInBraille, lineEndInBraille);
												lineInBraille = addLetterSpacing(line, lineInBraille, curPos, curPosInBraille,
												                                 letterSpacing[textWithWsMapping[curSegment]]);
												int lineInBrailleLength = lineInBraille.length();
												if (lines.lineHasHyphen()) {
													lineInBraille += "\u00ad";
													lineInBrailleLength++; }
												if (lineInBrailleLength == available) {
													bestSolution = new LineBreakSolution(); {
														bestSolution.line = line;
														bestSolution.replacementWord = replacementWord;
														bestSolution.lineInBraille = lineInBraille;
														bestSolution.lineInBrailleLength = lineInBrailleLength; }
													left = textAvailable + 1;
													right = textAvailable - 1; }
												else if (lineInBrailleLength < available) {
													left = textAvailable + 1;
													if (bestSolution == null || lineInBrailleLength > bestSolution.lineInBrailleLength) {
														bestSolution = new LineBreakSolution(); {
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
														curPos += lengthByCodePoints(bestSolution.line);
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
				
				public boolean hasNext() {
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
					if (joinedBraille == null)
						updateBraille();
					lastPeek = joinedBraille.charAt(curPosInBraille);
					return lastPeek;
				}
				
				// FIXME: does not take into account white-space property of segments: if "white-space: pre",
				// spaces are not replaced with NBSP yet at this point (this only happens in next() method)
				public String remainder() {
					if (joinedBraille == null)
						updateBraille();
					return joinedBraille.substring(curPosInBraille, endPosInBraille);
				}
				
				// FIXME: does not take into account white-space property of segments: if "white-space: pre",
				// spaces are not replaced with NBSP yet at this point (this only happens in next() method)
				public boolean hasPrecedingSpace() {
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
				
				private String addHyphensAndLetterSpacing(FullHyphenator fullHyphenator,
				                                          String segment,
				                                          String segmentInBraille,
				                                          int curPos,
				                                          int curPosInBraille,
				                                          byte[] manualHyphens,
				                                          Locale language,
				                                          int letterSpacing) {
					byte[] hyphens = fullHyphenator.hyphenate(
						// insert manual hyphens first so that hyphenator knows which words to skip
						insertHyphens(segment, manualHyphens, true, SHY, ZWSP),
						language);
					// FIXME: don't hard-code the number 4
					byte[] hyphensAndLetterBoundaries
						= (letterSpacing > 0) ? detectLetterBoundaries(hyphens, segment, (byte)4) : hyphens;
					if (hyphensAndLetterBoundaries == null && manualHyphens == null)
						return segment;
					byte[] hyphensAndLetterBoundariesInBraille = new byte[segmentInBraille.length() - 1];
					if (hyphensAndLetterBoundaries != null) {
						for (int i = 0; i < hyphensAndLetterBoundariesInBraille.length; i++) {
							int pos = interCharacterIndicesInBraille[curPosInBraille + i] - 1;
							if (pos >= 0)
								hyphensAndLetterBoundariesInBraille[i] = hyphensAndLetterBoundaries[pos - curPos];
						}
					}
					String r = insertHyphens(segmentInBraille, hyphensAndLetterBoundariesInBraille, false, SHY, ZWSP, US);
					return (letterSpacing > 0) ? applyLetterSpacing(r, letterSpacing) : r;
				}
				
				private String addLetterSpacing(String segment,
				                                String segmentInBraille,
				                                int curPos,
				                                int curPosInBraille,
				                                int letterSpacing) {
					if (letterSpacing > 0) {
						// FIXME: don't hard-code the number 1
						byte[] letterBoundaries = detectLetterBoundaries(null, segment, (byte)1);
						byte[] letterBoundariesInBraille = new byte[segmentInBraille.length() - 1];
						for (int i = 0; i < letterBoundariesInBraille.length; i++) {
							int pos = interCharacterIndicesInBraille[curPosInBraille + i] - 1;
							if (pos >= 0)
								letterBoundariesInBraille[i] = letterBoundaries[pos - curPos];
						}
						return applyLetterSpacing(insertHyphens(segmentInBraille, letterBoundariesInBraille, false, US), letterSpacing); }
					else
						return segmentInBraille;
				}
				
				private boolean updateInput(int start, int end, String replacement) {
					if (substringByCodePoints(joinedText, start, end).equals(replacement))
						return false;
					joinedText = substringByCodePoints(joinedText, 0, start) + replacement + substringByCodePoints(joinedText, end);
					{ // recompute joinedTextMapping
						int[] updatedJoinedTextMapping = new int[lengthByCodePoints(joinedText)];
						int i = 0;
						int j = 0;
						while (i < start)
							updatedJoinedTextMapping[j++] = joinedTextMapping[i++];
						int startSegment = joinedTextMapping[start];
						while (i < end)
							if (joinedTextMapping[i++] != startSegment)
								throw new RuntimeException("Coding error");
						while (j < start + lengthByCodePoints(replacement))
							updatedJoinedTextMapping[j++] = startSegment;
						while (j < updatedJoinedTextMapping.length)
							updatedJoinedTextMapping[j++] = joinedTextMapping[i++];
						joinedTextMapping = updatedJoinedTextMapping;
					}
					// recompute manualHyphens
					if (manualHyphens != null) {
						byte[] updatedManualHyphens = new byte[lengthByCodePoints(joinedText) - 1];
						int i = 0;
						int j = 0;
						while (i < start)
							updatedManualHyphens[j++] = manualHyphens[i++];
						while (j < start + lengthByCodePoints(replacement) - 1)
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
					int joinedTextLength = lengthByCodePoints(joinedText);

					String partBeforeCurPos = curPosInBraille > 0 ? joinedBraille.substring(0, curPosInBraille): null;
					int[] characterIndices = new int[joinedTextLength]; {
						for (int i = 0; i < joinedTextLength; i++)
							characterIndices[i] = i; }
					int[] interCharacterIndices = new int[joinedTextLength - 1]; {
						for (int i = 0; i < joinedTextLength - 1; i++)
							interCharacterIndices[i] = i + 1; }
					
					// typeform var with the same length as joinedText
					Typeform[] _typeform = null;
					for (Typeform t : typeform)
						if (t != Typeform.PLAIN_TEXT) {
							_typeform = new Typeform[joinedTextLength];
							for (int i = 0; i < _typeform.length; i++)
								_typeform[i] = typeform[textWithWsMapping[joinedTextMapping[i]]];
							break; }
					try {
						TranslationResult r = translator.translate(joinedText, _typeform, characterIndices, interCharacterIndices, displayTable);
						joinedBraille = r.getBraille();
						if (lengthByCodePoints(joinedBraille) != joinedBraille.length())
							throw new RuntimeException(); // assuming there are no characters above U+FFFF in braille
						characterIndicesInBraille = r.getCharacterAttributes();
						interCharacterIndicesInBraille = r.getInterCharacterAttributes(); }
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
		                               boolean failWhenNonStandardHyphenation) throws NonStandardHyphenationException {
			try {
				if (fullHyphenator == compoundWordHyphenator)
					if (any(styledText, t -> {
								SimpleInlineStyle style = t.getStyle();
								return style != null && style.getProperty("hyphens") == Hyphens.AUTO; }))
						logger.warn("hyphens: auto not supported");
				styledText = fullHyphenator.transform(styledText); }
			catch (NonStandardHyphenationException e) {
				if (failWhenNonStandardHyphenation)
					throw e;
				else
					switch (handleNonStandardHyphenation) {
					case NON_STANDARD_HYPH_IGNORE:
						logger.warn("hyphens: auto can not be applied due to non-standard hyphenation points.");
						break;
					case NON_STANDARD_HYPH_FAIL:
						logger.error("hyphens: auto can not be applied due to non-standard hyphenation points.");
						throw e;
					case NON_STANDARD_HYPH_DEFER:
						if (forceBraille) {
							logger.error("hyphens: auto can not be applied due to non-standard hyphenation points.");
							throw e; }
						logger.debug("Deferring hyphenation to formatting phase due to non-standard hyphenation points.");
						
						// TODO: split up text in words and only defer the words with non-standard hyphenation
						List<String> result = new ArrayList<>();
						for (CSSStyledText t : styledText) result.add(t.getText());
						return result; }}
			
			int size = size(styledText);
			String[] text = new String[size];
			SimpleInlineStyle[] style = new SimpleInlineStyle[size];
			int i = 0;
			for (CSSStyledText t : styledText) {
				text[i] = t.getText();
				style[i] = t.getStyle();
				if (style[i] != null)
					style[i].removeProperty("hyphens"); // handled above
				i++; }
			return Arrays.asList(transform(text, style));
		}
		
		private String[] transform(String[] text, SimpleInlineStyle[] styles) {
			int size = text.length;
			Typeform[] typeform = new Typeform[size];
			boolean[] preserveLines = new boolean[size];
			boolean[] preserveSpace = new boolean[size];
			int[] letterSpacing = new int[size];
			for (int i = 0; i < size; i++) {
				typeform[i] = Typeform.PLAIN_TEXT;
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
							// "text-transform: none" is handled by HandleTextTransformNone, but HandleTextTransformNone
							// is a CompoundBrailleTranslator and CompoundBrailleTranslator puts "text-transform: none"
							// on (already translated) context segments. We assume that all Liblouis tables correctly
							// handle Unicode braille. If this is not the case, it is not the end of the words because
							// this is a context segment.
							val = style.getProperty("braille-charset");
							if (val != null) {
								if (val == BrailleCharset.CUSTOM)
									// translate to Unicode braille
									text[i] = displayTable.decode(text[i]);
								style.removeProperty("braille-charset"); }
							style.removeProperty("text-transform");
							continue; }
						else if (val == TextTransform.AUTO) {}
						else if (val == TextTransform.list_values) {
							TermList values = style.getValue(TermList.class, "text-transform");
							text[i] = textFromTextTransform(text[i], values);
							typeform[i] = typeform[i].add(typeformFromTextTransform(values, translator, supportedTypeforms)); }
						style.removeProperty("text-transform"); }
					val = style.getProperty("letter-spacing");
					if (val != null) {
						if (val == LetterSpacing.length) {
							letterSpacing[i] = style.getValue(TermInteger.class, "letter-spacing").getIntValue();
							if (letterSpacing[i] < 0) {
								logger.warn("letter-spacing: {} not supported, must be non-negative", val);
								letterSpacing[i] = 0; }}
						style.removeProperty("letter-spacing"); }
					typeform[i] = typeform[i].add(typeformFromInlineCSS(style, translator, supportedTypeforms));
					for (String prop : style.getPropertyNames())
						if (!"white-space".equals(prop))
							logger.warn("{}: {} not supported", prop, style.get(prop)); }}
			
			return transform(text, typeform, preserveLines, preserveSpace, letterSpacing);
		}
		
		private String[] transform(String[] text, Typeform[] typeform) {
			int size = text.length;
			boolean[] preserveLines = new boolean[size];
			boolean[] preserveSpace = new boolean[size];
			int[] letterSpacing = new int[size];
			for (int i = 0; i < text.length; i++) {
				preserveLines[i] = preserveSpace[i] = false;
				letterSpacing[i] = 0; }
			return transform(text, typeform, preserveLines, preserveSpace, letterSpacing);
		}
		
		// the positions in the text where spacing must be inserted have been previously indicated with a US control character
		private String applyLetterSpacing(String text, int letterSpacing) {
			String space = "";
			for (int i = 0; i < letterSpacing; i++)
				space += NBSP;
			return text.replaceAll("\u001F", space);
		}
		
		private String[] transform(String[] text,
		                           Typeform[] typeform,
		                           boolean[] preserveLines,
		                           boolean[] preserveSpace,
		                           int[] letterSpacing) {
			
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
				Tuple2<String,byte[]> t = extractHyphens(join(textWithWsReplaced, RS), true, SHY, ZWSP);
				joinedText = t._1;
				inputAttrs = t._2;
				String[] nohyph = toArray(SEGMENT_SPLITTER.split(joinedText), String.class);
				joinedTextMapping = new int[lengthByCodePoints(join(nohyph))];
				int i = 0;
				int j = 0;
				for (String s : nohyph) {
					int l = lengthByCodePoints(s);
					for (int k = 0; k < l; k++)
						joinedTextMapping[i++] = j;
					j++; }
				t = extractHyphens(inputAttrs, joinedText, true, null, null, null, RS);
				joinedText = t._1;
				inputAttrs = t._2;
				if (joinedText.matches("\\xA0*"))
					return text;
				if (inputAttrs == null)
					inputAttrs = new byte[lengthByCodePoints(joinedText) - 1];
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
					_typeform = new Typeform[lengthByCodePoints(joinedText)];
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
					TranslationResult r = translator.translate(joinedText, _typeform, null, inputAttrsAsInt, displayTable);
					joinedBrailleWithoutHyphens = r.getBraille();
					if (lengthByCodePoints(joinedBrailleWithoutHyphens) != joinedBrailleWithoutHyphens.length())
						throw new RuntimeException(); // assuming there are no characters above U+FFFF in braille
					int [] outputAttrsAsInt = r.getInterCharacterAttributes();
					if (outputAttrsAsInt != null) {
						outputAttrs = new byte[outputAttrsAsInt.length];
						for (int i = 0; i < outputAttrs.length; i++)
							outputAttrs[i] = (byte)outputAttrsAsInt[i];
						joinedBraille = insertHyphens(joinedBrailleWithoutHyphens, outputAttrs, false, SHY, ZWSP, US, RS); }
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
						int imax = lengthByCodePoints(joinedText);
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
						TranslationResult r = translator.translate(joinedText, _typeform, inputSegmentNumbers, null, displayTable);
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
		private byte[] detectLetterBoundaries(byte[] addTo, String text, byte val) {
			if (addTo == null)
				addTo = new byte[lengthByCodePoints(text) - 1];
			int i = 0;
			int prev = -1;
			for (int c : text.codePoints().toArray()) {
				if (i > 0 && ((Character.isLetter(c) && Character.isLetter(prev)) ||
				              c == '-' ||
				              prev == '-'))
					addTo[i - 1] |= val;
				if (i < addTo.length && c == '\u00ad') // SHY is not actual character, so boundary only after SHY
					addTo[i] |= val;
				prev = c;
				i++;
			}
			return addTo;
		}
		
		@Override
		public ToStringHelper toStringHelper() {
			return MoreObjects.toStringHelper("LiblouisTranslatorJnaImplProvider$LiblouisTranslatorImpl")
				.add("translator", translator)
				.add("displayTable", displayTable)
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
			if (!this.displayTable.equals(that.displayTable))
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
	
	private class HandleTextTransformNone extends CompoundBrailleTranslator implements LiblouisTranslator {
		
		final LiblouisTranslator translator;
		
		HandleTextTransformNone(LiblouisTranslator translator, BrailleTranslator unityTranslator) {
			super(translator, ImmutableMap.of("none", () -> unityTranslator));
			this.translator = translator;
		}

		private HandleTextTransformNone(CompoundBrailleTranslator from, LiblouisTranslator translator) {
			super(from);
			this.translator = translator;
		}

		@Override
		public HandleTextTransformNone _withHyphenator(Hyphenator hyphenator) {
			HandleTextTransformNone t = new HandleTextTransformNone(
				(CompoundBrailleTranslator)super._withHyphenator(hyphenator),
				translator);
			LiblouisTranslatorJnaImplProvider.this.rememberId(t);
			return t;
		}
		
		@Override
		public LiblouisTable asLiblouisTable() {
			return translator.asLiblouisTable();
		}

		@Override
		public FromTypeformedTextToBraille fromTypeformedTextToBraille() {
			return translator.fromTypeformedTextToBraille();
		}
	}
	
	private interface FullHyphenator extends Hyphenator.FullHyphenator {
		public byte[] hyphenate(String text, Locale language);
	}
	
	/**
	  * Hyphenator that add a zero-width space after hard hyphens ("-" followed and preceded
	  * by a letter or number)
	  */
	private final static FullHyphenator compoundWordHyphenator = new CompoundWordHyphenator();
	
	private static class CompoundWordHyphenator extends NoHyphenator implements FullHyphenator {

		public byte[] hyphenate(String text, Locale language) {
			if (text.isEmpty())
				return null;
			Tuple2<String,byte[]> t = extractHyphens(text, true, SHY, ZWSP);
			if (t._1.isEmpty())
				return null;
			return transform(t._2, t._1, language);
		}
	}
	
	private static class HyphenatorAsFullHyphenator implements FullHyphenator {
		
		private final Hyphenator.FullHyphenator hyphenator;
		
		private HyphenatorAsFullHyphenator(Hyphenator hyphenator) {
			this.hyphenator = hyphenator.asFullHyphenator();
		}
		
		public java.lang.Iterable<CSSStyledText> transform(java.lang.Iterable<CSSStyledText> text) {
			return hyphenator.transform(text);
		}
		
		private final static SimpleInlineStyle HYPHENS_AUTO = new SimpleInlineStyle("hyphens: auto");
		
		public byte[] hyphenate(String text, Locale language) {
			return extractHyphens(
				hyphenator.transform(singleton(new CSSStyledText(text, HYPHENS_AUTO, language))).iterator().next().getText(),
				true, SHY, ZWSP)._2;
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
						logger.warn("{}: {} not supported: emphclass 'italic' not defined in table {}",
						            prop, style.get(prop),
						            table.getTable());
					style.removeProperty(prop);
					continue; }}
			else if (prop.equals("font-weight")) {
				CSSProperty value = style.getProperty(prop);
				if (value == FontWeight.BOLD) {
					Typeform t = supportedTypeforms.get("bold");
					if (t != null)
						typeform = typeform.add(t);
					else
						logger.warn("{}: {} not supported: emphclass 'bold' not defined in table {}",
						            prop, style.get(prop),
						            table.getTable());
					style.removeProperty(prop);
					continue; }}
			else if (prop.equals("text-decoration")) {
				CSSProperty value = style.getProperty(prop);
				if (value == TextDecoration.UNDERLINE) {
					Typeform t = supportedTypeforms.get("underline");
					if (t != null)
						typeform = typeform.add(t);
					else
						logger.warn("{}: {} not supported: emphclass 'underline' not defined in table {}",
						            prop, style.get(prop),
						            table.getTable());
					style.removeProperty(prop);
					continue; }}}
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
			else if (!LOUIS_TEXT_TRANSFORM.matcher(tt).matches())
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
			} else if (tt.equals("uppercase") || tt.equals("lowercase")) {
				// handled in textFromTextTransform
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
	
	private static int lengthByCodePoints(String s) {
		return s.codePointCount(0, s.length());
	}
	
	private static String substringByCodePoints(String s, int beginIndex) {
		return s.substring(s.offsetByCodePoints(0, beginIndex));
	}
	
	private static String substringByCodePoints(String s, int beginIndex, int endIndex) {
		return s.substring(s.offsetByCodePoints(0, beginIndex), s.offsetByCodePoints(0, endIndex));
	}

	private static class LineBreakSolution {
		String line;
		String replacementWord;
		String lineInBraille;
		int lineInBrailleLength;
	}

	private static final Logger logger = LoggerFactory.getLogger(LiblouisTranslatorJnaImplProvider.class);
	
}
