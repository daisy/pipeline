package org.daisy.pipeline.braille.dotify.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.Set;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import static com.google.common.collect.Iterables.concat;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.dotify.api.translator.AttributeWithContext;
import org.daisy.dotify.api.translator.BrailleTranslator;
import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.BrailleTranslatorFactoryService;
import org.daisy.dotify.api.translator.BrailleTranslatorResult;
import org.daisy.dotify.api.translator.FollowingText;
import org.daisy.dotify.api.translator.PrecedingText;
import org.daisy.dotify.api.translator.ResolvableText;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorSpecification;

import org.daisy.pipeline.braille.common.AbstractBrailleTranslator.util.DefaultLineBreaker;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.Query.util.QUERY;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.dotify.impl.BrailleTranslatorFactoryServiceImpl",
	service = { BrailleTranslatorFactoryService.class }
)
public class BrailleTranslatorFactoryServiceImpl implements BrailleTranslatorFactoryService {
	
	public void setCreatedWithSPI() {}
	
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
	protected void bindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
		brailleTranslatorProviders.add((BrailleTranslatorProvider<org.daisy.pipeline.braille.common.BrailleTranslator>)provider);
		logger.debug("Adding BrailleTranslator provider: {}", provider);
	}
	
	protected void unbindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
		brailleTranslatorProviders.remove(provider);
		brailleTranslatorProvider.invalidateCache();
		logger.debug("Removing BrailleTranslator provider: {}", provider);
	}
	
	private final List<BrailleTranslatorProvider<org.daisy.pipeline.braille.common.BrailleTranslator>> brailleTranslatorProviders
	= new ArrayList<BrailleTranslatorProvider<org.daisy.pipeline.braille.common.BrailleTranslator>>();
	
	private final Provider.util.MemoizingProvider<Query,org.daisy.pipeline.braille.common.BrailleTranslator> brailleTranslatorProvider
	= memoize(dispatch(brailleTranslatorProviders));
	
	public boolean supportsSpecification(String locale, String mode) {
		try {
			factory.newTranslator(locale, mode);
			return true; }
		catch (TranslatorConfigurationException e) {
			return false; }
	}
	
	public Collection<TranslatorSpecification> listSpecifications() {
		return ImmutableList.of();
	}
	
	public BrailleTranslatorFactory newFactory() {
		return factory;
	}
	
	private final BrailleTranslatorFactory factory = new BrailleTranslatorFactoryImpl();
	
	/*
	 * Mode for pre-translated text with support for text-level CSS and line
	 * breaking according to CSS. Corresponds with translator query
	 * `(input:braille)(input:text-css)(output:braille)`
	 */
	private final static String PRE_TRANSLATED_MODE = "pre-translated-text-css";
	
	private class BrailleTranslatorFactoryImpl implements BrailleTranslatorFactory {
		public BrailleTranslator newTranslator(String locale, String mode) throws TranslatorConfigurationException {
			if (PRE_TRANSLATED_MODE.equals(mode))
				return new PreTranslatedBrailleTranslator();
			Matcher m = QUERY.matcher(mode);
			if (!m.matches())
				throw new TranslatorConfigurationException();
			Query query = query(mode);
			boolean isPreTranslatedQuery = false; {
				for (Query.Feature f : query)
					if ("input".equals(f.getKey()) && "braille".equals(f.getValue().orElse(null)))
						isPreTranslatedQuery = true;
					else if (!("locale".equals(f.getKey()) ||
					           "input".equals(f.getKey()) && "text-css".equals(f.getValue().orElse(null)) ||
					           "output".equals(f.getKey()) && "braille".equals(f.getValue().orElse(null)))) {
						isPreTranslatedQuery = false;
						break; }}
			if (isPreTranslatedQuery)
				return new PreTranslatedBrailleTranslator();
			if (locale != null && !"und".equals(locale))
				query = mutableQuery(query).add("locale", locale);
			for (org.daisy.pipeline.braille.common.BrailleTranslator t : brailleTranslatorProvider.get(query))
				try {
					return new BrailleTranslatorFromBrailleTranslator(mode, t); }
				catch (UnsupportedOperationException e) {}
			try {
				MutableQuery q = mutableQuery(query);
				for (Feature f : q.removeAll("input"))
					if (!"text-css".equals(f.getValue().get()))
						throw new NoSuchElementException();
				for (Feature f : q.removeAll("output"))
					if (!"braille".equals(f.getValue().get()))
						throw new NoSuchElementException();
				if (!q.isEmpty())
					throw new NoSuchElementException();
				return new BrailleTranslatorFromBrailleTranslator(mode, NumberBrailleTranslator.getInstance());
			} catch (NoSuchElementException e) {}
			throw new TranslatorConfigurationException("Factory does not support " + locale + "/" + mode);
		}
	}
	
	public <T> void setReference(Class<T> c, T reference) throws TranslatorConfigurationException {}
	
	/**
	 * {@link BrailleTranslator} wrapper for a {@link org.daisy.pipeline.braille.common.BrailleTranslator}
	 *
	 * <a href="http://braillespecs.github.io/braille-css/#h3_white-space-processing">White space
	 * processing</a> and <a href="http://braillespecs.github.io/braille-css/#line-breaking">line
	 * breaking</a> are done according to braille CSS.
	 *
	 * White space is normalised. Preserved spaces must have been converted to
	 * no-break spaces and preserved line feeds must have been converted to
	 * &lt;obfl:br/&gt;.
	 *
	 * Through setHyphenating() the translator can be made to perform automatic hyphenation or
	 * not. Regardless of this setting, hyphenation characters (SHY and ZWSP) in the input are used
	 * in line breaking, except when overridden with a <code>hyphens: none</code> style.
	 *
	 * Supports special variable assignments (in the form of "<code>-dotify-def:foo</code>") and
	 * tests (in the form of "<code>-dotify-ifdef:foo</code>" or "<code>-dotify-ifndef:foo</code>")
	 * in text attributes in order to support special ad hoc handling of marker-references.
	 *
	 * Support <code>text-transform</code> value "<code>-dotify-counter</code>" which causes numbers
	 * to be formatted according to the value of the <code>-dotify-counter-style</code> property.
	 */
	private static class BrailleTranslatorFromBrailleTranslator implements BrailleTranslator {
		
		final String mode;
		org.daisy.pipeline.braille.common.BrailleTranslator.LineBreakingFromStyledText lineBreakingFromStyledText;
		org.daisy.pipeline.braille.common.BrailleTranslator.FromStyledTextToBraille fromStyledTextToBraille;
		
		private BrailleTranslatorFromBrailleTranslator(
				String mode,
				org.daisy.pipeline.braille.common.BrailleTranslator translator)
				throws UnsupportedOperationException {
			this.mode = mode;
			try {
				this.lineBreakingFromStyledText = translator.lineBreakingFromStyledText();
				this.fromStyledTextToBraille = null; }
			catch (UnsupportedOperationException e) {
				this.lineBreakingFromStyledText = null;
				this.fromStyledTextToBraille = translator.fromStyledTextToBraille(); }
		}
		
		public BrailleTranslatorResult translate(Translatable input) throws TranslationException {
			if (input.getAttributes() == null && input.isHyphenating() == false) {
				String text = input.getText();
				if ("".equals(text))
					// see org.daisy.dotify.formatter.impl.row.SegmentProcessor.layoutLeader
					return new DefaultLineBreaker.LineIterator("", '\u2800', '\u2824', 1);
				if (" ".equals(text))
					// If input text is a space, it will be user for calculating the margin character
					// (see org.daisy.dotify.formatter.impl.common.FormatterCoreContext)
					return new DefaultLineBreaker.LineIterator("\u2800", '\u2800', '\u2824', 1); }
			return translate(cssStyledTextFromTranslatable(input), 0, -1);
		}
		
		public BrailleTranslatorResult translate(TranslatableWithContext input) throws TranslationException {
			List<CSSStyledText> styledText = Lists.newArrayList(cssStyledTextFromTranslatable(input));
			int from = input.getPrecedingText().size();
			int to = from + input.getTextToTranslate().size();
			for (int i = 0; i < styledText.size(); i++)
				if ("??".equals(styledText.get(i).getText()))
					// If input text is "??", it will be used for creating a placeholder for content that
					// can not be computed yet (see org.daisy.dotify.formatter.impl.row.SegmentProcessor).
					// Because normally this will never end up in the resulting PEF, it is okay to replace
					// it with "0". We choose a number because in some cases the translator may assume that
					// the input is numerical.
					styledText.set(i, new CSSStyledText("0", styledText.get(i).getStyle()));
			return translate(styledText, from, to);
		}
		
		private BrailleTranslatorResult translate(Iterable<CSSStyledText> styledText, int from, int to) throws TranslationException {
			if (lineBreakingFromStyledText != null)
				return lineBreakingFromStyledText.transform(styledText, from, to);
			else {
				List<String> braille = new ArrayList<>();
				Iterator<CSSStyledText> style = styledText.iterator();
				for (String s : fromStyledTextToBraille.transform(styledText)) {
					SimpleInlineStyle st = style.next().getStyle();
					if (st != null) {
						if (st.getProperty("hyphens") == Hyphens.NONE) {
							s = s.replaceAll("[\u00AD\u200B]","");
							st.removeProperty("hyphens"); }
						CSSProperty ws = st.getProperty("white-space");
						if (ws != null) {
							if (ws == WhiteSpace.PRE_WRAP)
								s = s.replaceAll("[\\x20\t\\u2800]+", "$0\u200B")
								     .replaceAll("[\\x20\t\\u2800]", "\u00A0");
							if (ws == WhiteSpace.PRE_WRAP || ws == WhiteSpace.PRE_LINE)
								s = s.replaceAll("[\\n\\r]", "\u2028");
							st.removeProperty("white-space"); }}
					braille.add(s);
				}
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
				return new DefaultLineBreaker.LineIterator(brailleString.toString(), fromChar, toChar, '\u2800', '\u2824', 1);
			}
		}
		
		public String getTranslatorMode() {
			return mode;
		}
	}
	
	/**
	 * Same as above but assumes that input text exists of only braille and white space
	 * characters. Supports CSS properties "word-spacing", "hyphens" and "white-space".
	 */
	private static class PreTranslatedBrailleTranslator implements BrailleTranslator {
		
		private PreTranslatedBrailleTranslator() {}
		
		public BrailleTranslatorResult translate(Translatable input) throws TranslationException {
			return translate(cssStyledTextFromTranslatable(input), 0, -1);
		}
			
		public BrailleTranslatorResult translate(TranslatableWithContext input) throws TranslationException {
			int from = input.getPrecedingText().size();
			int to = from + input.getTextToTranslate().size();
			return translate(cssStyledTextFromTranslatable(input), from, to);
		}
		
		private BrailleTranslatorResult translate(Iterable<CSSStyledText> input, int from, int to) throws TranslationException {
			List<String> braille = new ArrayList<>();
			int wordSpacing; {
				wordSpacing = -1;
				for (CSSStyledText styledText : input) {
					SimpleInlineStyle style = styledText.getStyle();
					int spacing = 1;
					String text = styledText.getText();
					if (style != null) {
						CSSProperty val = style.getProperty("word-spacing");
						if (val != null) {
							if (val == WordSpacing.length) {
								spacing = style.getValue(TermInteger.class, "word-spacing").getIntValue();
								if (spacing < 0) {
									if (logger != null)
										logger.warn("word-spacing: {} not supported, must be non-negative", val);
									spacing = 1; }}
									
							// FIXME: assuming style is mutable and text.iterator() does not create copies
							style.removeProperty("word-spacing"); }
						if (style.getProperty("hyphens") == Hyphens.NONE) {
							text = text.replaceAll("[\u00AD\u200B]","");
							style.removeProperty("hyphens"); }
						val = style.getProperty("white-space");
						if (val != null) {
							if (val == WhiteSpace.PRE_WRAP)
								text = text.replaceAll("[\\x20\t\\u2800]+", "$0\u200B")
								           .replaceAll("[\\x20\t\\u2800]", "\u00A0");
							if (val == WhiteSpace.PRE_WRAP || val == WhiteSpace.PRE_LINE)
								text = text.replaceAll("[\\n\\r]", "\u2028");
							style.removeProperty("white-space"); }
						for (String prop : style.getPropertyNames())
							logger.warn("{}: {} not supported", prop, style.get(prop)); }
					if (wordSpacing < 0)
						wordSpacing = spacing;
					else if (wordSpacing != spacing)
						throw new RuntimeException("word-spacing must be constant, but both "
						                           + wordSpacing + " and " + spacing + " specified");
					Map<String,String> attrs = styledText.getTextAttributes();
					if (attrs != null)
						for (String k : attrs.keySet())
							logger.warn("Text attribute \"{}:{}\" ignored", k, attrs.get(k));
					braille.add(text); }
				if (wordSpacing < 0) wordSpacing = 1; }
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
			return new DefaultLineBreaker.LineIterator(brailleString.toString(), fromChar, toChar, '\u2800', '\u2824', wordSpacing);
		}
		
		public String getTranslatorMode() {
			return PRE_TRANSLATED_MODE;
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(BrailleTranslatorFactoryServiceImpl.class);

	/* ========= */
	/* UTILITIES */
	/* ========= */

	/**
	 * Convert Translatable specification to text + CSS style. Text attributes are assumed to
	 * contain only CSS or special variable assignments/tests. Computation of the text-transform
	 * property is assumed to have been performed already.
	 */
	protected static Iterable<CSSStyledText> cssStyledTextFromTranslatable(Translatable specification) {
		return handleCounterStyles(
			handleVariables(
				cssStyledTextFromTranslatable(
					specification.getText(),
					specification.getAttributes(),
					specification.isHyphenating(),
					null)));
	}

	private static Iterable<CSSStyledText> cssStyledTextFromTranslatable(String text,
	                                                                     TextAttribute attributes,
	                                                                     boolean hyphenating,
	                                                                     SimpleInlineStyle parentStyle) {
		if (attributes != null && attributes.getWidth() != text.length())
			throw new RuntimeException("Coding error");
		SimpleInlineStyle style; {
			String s = attributes != null ? attributes.getDictionaryIdentifier() : null;
			if (s == null && parentStyle == null)
				style = null;
			else
				// FIXME: extend caching of parsed CSS to support parentStyle!
				style = new SimpleInlineStyle(s != null ? s : "", parentStyle);
			if (hyphenating && (style == null || style.getProperty("hyphens") == null))
				// FIXME: add hyphens declaration through braille-css model
				style = new SimpleInlineStyle("hyphens: auto", style); }
		if (attributes != null && attributes.hasChildren())
			return cssStyledTextFromTranslatable(text, attributes.iterator(), false, style);
		else
			return Collections.singleton(new CSSStyledText(text, style));
	}

	private static Iterable<CSSStyledText> cssStyledTextFromTranslatable(String text,
	                                                                     Iterator<TextAttribute> attributes,
	                                                                     boolean hyphenating,
	                                                                     SimpleInlineStyle parentStyle) {
		if (attributes.hasNext()) {
			TextAttribute a = attributes.next();
			int w = a.getWidth();
			return concat(cssStyledTextFromTranslatable(text.substring(0, w), a, hyphenating, parentStyle),
			              cssStyledTextFromTranslatable(text.substring(w), attributes, hyphenating, parentStyle)); }
		else
			return empty;
	}

	/**
	 * Convert TranslatableWithContext specification to text + CSS style. Text attributes are
	 * assumed to contain only CSS or special variable assignments/tests. Computation of the
	 * text-transform property is assumed to have been performed already.
	 */
	private static Iterable<CSSStyledText> cssStyledTextFromTranslatable(TranslatableWithContext specification) {
		return handleCounterStyles(
			handleVariables(
				cssStyledTextFromTranslatable(
					specification.getPrecedingText(),
					specification.getTextToTranslate(),
					specification.getFollowingText(),
					specification.getAttributes().orElse(null),
					null)));
	}

	private static Iterable<CSSStyledText> cssStyledTextFromTranslatable(List<PrecedingText> preceding,
	                                                                     List<ResolvableText> text,
	                                                                     List<FollowingText> following,
	                                                                     AttributeWithContext attributes,
	                                                                     SimpleInlineStyle parentStyle) {
		int textSize = text == null ? 0 : text.size();
		int precedingSize = preceding == null ? 0 : preceding.size();
		int followingSize = following == null ? 0 : following.size();
		if (attributes != null && attributes.getWidth() != precedingSize + textSize + followingSize)
			throw new RuntimeException("Coding error");
		SimpleInlineStyle style; {
			String s = attributes != null ? attributes.getName().orElse(null) : null;
			if (s == null && parentStyle == null)
				style = null;
			else
				// FIXME: extend caching of parsed CSS to support parentStyle!
				style = new SimpleInlineStyle(s != null ? s : "", parentStyle);
		}
		if (attributes != null && attributes.hasChildren())
			return cssStyledTextFromTranslatable(preceding, text, following, attributes.iterator(), style);
		else
			return concat(cssStyledTextFromTranslatable(preceding, style),
			              cssStyledTextFromTranslatable(text, style),
			              cssStyledTextFromTranslatable(following, style));
	}

	private static Iterable<CSSStyledText> cssStyledTextFromTranslatable(List<PrecedingText> preceding,
	                                                                     List<ResolvableText> text,
	                                                                     List<FollowingText> following,
	                                                                     Iterator<AttributeWithContext> attributes,
	                                                                     SimpleInlineStyle parentStyle) {
		int textSize = text == null ? 0 : text.size();
		int precedingSize = preceding == null ? 0 : preceding.size();
		int followingSize = following == null ? 0 : following.size();
		if (attributes.hasNext()) {
			AttributeWithContext a = attributes.next();
			int w = a.getWidth();
			if (w <= precedingSize)
				return concat(
					cssStyledTextFromTranslatable(preceding.subList(0, w), null, null, a, parentStyle),
					cssStyledTextFromTranslatable(preceding.subList(w, precedingSize), text, following, attributes, parentStyle));
			else if (w - precedingSize <= textSize)
				return concat(
					cssStyledTextFromTranslatable(preceding, text.subList(0, w - precedingSize), null, a, parentStyle),
					cssStyledTextFromTranslatable(null, text.subList(w - precedingSize, textSize), following, attributes, parentStyle));
			else
				return concat(
					cssStyledTextFromTranslatable(preceding, text, following.subList(0, w - precedingSize - textSize), a, parentStyle),
					cssStyledTextFromTranslatable(null, null, following.subList(w - precedingSize - textSize, followingSize), attributes, parentStyle));
		} else
			return empty;
	}

	private static Iterable<CSSStyledText> cssStyledTextFromTranslatable(List<? extends Object/*PrecedingText|FollowingText*/> precedingOrFollowingText,
	                                                                     SimpleInlineStyle parentStyle) {
		if (precedingOrFollowingText == null)
			return empty;
		List<CSSStyledText> styledText = new ArrayList<>();
		for (Object t : precedingOrFollowingText) {
			String text;
			boolean hyphenate; {
				if (t instanceof PrecedingText) {
					text = ((PrecedingText)t).resolve();
					hyphenate = ((PrecedingText)t).shouldHyphenate(); }
				else if (t instanceof FollowingText) {
					text = ((FollowingText)t).peek();
					hyphenate = ((FollowingText)t).shouldHyphenate(); }
				else
					throw new RuntimeException();
			}
			SimpleInlineStyle style; {
				if (hyphenate && (parentStyle == null || parentStyle.getProperty("hyphens") == null))
					style = new SimpleInlineStyle("hyphens: auto", parentStyle);
				else if (parentStyle == null)
					style = null;
				else
					style = new SimpleInlineStyle("", parentStyle);
			}
			styledText.add(new CSSStyledText(text, style));
		}
		return styledText;
	}

	private static Iterable<CSSStyledText> empty = Optional.<CSSStyledText>absent().asSet();

	private static Iterable<CSSStyledText> handleVariables(Iterable<CSSStyledText> styledText) {
		List<CSSStyledText> segments = new ArrayList<CSSStyledText>();
		Set<String> env = null;
		String segment = null;
		SimpleInlineStyle style = null;
		Map<String,String> attrs = null;
		for (CSSStyledText st : styledText) {
			String t = st.getText();
			SimpleInlineStyle s = st.getStyle();
			Map<String,String> a = st.getTextAttributes();
			if (s != null) {
				Collection<String> properties = s.getPropertyNames();
				String key = null;
				if (properties.contains("-dotify-def")) {
					key = "-dotify-def"; }
				else if (properties.contains("-dotify-ifdef")) {
					key = "-dotify-ifdef"; }
				else if (properties.contains("-dotify-ifndef")) {
					key = "-dotify-ifndef"; }
				else if (properties.contains("-dotify-defifndef")) {
					key = "-dotify-defifndef"; }
				if (key != null) {
					if (!"".equals(t)) {
						String var = s.getProperty(key, true).toString();
						if (env == null)
							env = new HashSet<String>();
						if (key.equals("-dotify-ifdef") && !env.contains(var)
						    || (key.equals("-dotify-ifndef") || key.equals("-dotify-defifndef")) && env.contains(var))
							t = "";
						if (key.equals("-dotify-def") || key.equals("-dotify-defifndef"))
							env.add(var); }
					s.removeProperty(key); }}
			if (segment != null)
				segments.add(new CSSStyledText(segment, style, attrs));
			segment = t;
			style = s;
			attrs = a; }
		if (segment != null)
			segments.add(new CSSStyledText(segment, style, attrs));
		return segments;
	}

	private static Iterable<CSSStyledText> handleCounterStyles(Iterable<CSSStyledText> styledText) {
		List<CSSStyledText> segments = new ArrayList<CSSStyledText>();
		String segment = null;
		SimpleInlineStyle style = null;
		Map<String,String> attrs = null;
		for (CSSStyledText st : styledText) {
			String t = st.getText();
			SimpleInlineStyle s = st.getStyle();
			Map<String,String> a = st.getTextAttributes();
			if (s != null) {
				if (s.getProperty("text-transform") == TextTransform.list_values) {
					TermList list = s.getValue(TermList.class, "text-transform");
					if (((TermIdent)list.get(0)).getValue().equals("-dotify-counter")) {
						if (list.size() == 1)
							s.removeProperty("text-transform");
						else
							list.remove(0);
						if ("??".equals(t)) {
						} else {
							int counterValue = Integer.parseInt(t);
							Term<?> counterStyle = s.getValue(TermFunction.class, "-dotify-counter-style");
							if (counterStyle instanceof TermFunction
							    && ((TermFunction)counterStyle).getFunctionName().equals("symbols")) {
								String system = null;
								List<String> symbols = new ArrayList<String>();
								for (Term<?> term : (TermFunction)counterStyle) {
									if (system == null) {
										if (term instanceof TermIdent)
											system = ((TermIdent)term).getValue();
										else
											system = "symbolic"; }
									else
										symbols.add(((TermString)term).getValue()); }
								if (system.equals("alphabetic"))
									t = counterRepresentationAlphabetic(counterValue, symbols);
								else if (system.equals("numeric"))
									t = counterRepresentationNumeric(counterValue, symbols);
								else if (system.equals("cyclic"))
									t = counterRepresentationCyclic(counterValue, symbols);
								else if (system.equals("fixed"))
									t = counterRepresentationFixed(counterValue, symbols);
								else if (system.equals("symbolic"))
									t = counterRepresentationSymbolic(counterValue, symbols); }}}}
				s.removeProperty("-dotify-counter-style"); }
			if (segment != null)
				segments.add(new CSSStyledText(segment, style, attrs));
			segment = t;
			style = s;
			attrs = a; }
		if (segment != null)
			segments.add(new CSSStyledText(segment, style, attrs));
		return segments;
	}

	private static int mod(int a, int n) {
		int result = a % n;
		if (result < 0)
			result += n;
		return result;
	}

	static String counterRepresentationAlphabetic(int counterValue, List<String> symbols) {
		if (counterValue < 1)
			return "";
		if (counterValue > symbols.size())
			return counterRepresentationAlphabetic((counterValue - 1) / symbols.size(), symbols)
				+ symbols.get(mod(counterValue - 1, symbols.size()));
		else
			return symbols.get(counterValue - 1);
	}

	static String counterRepresentationCyclic(int counterValue, List<String> symbols) {
		return symbols.get(mod(counterValue - 1, symbols.size()));
	}

	static String counterRepresentationFixed(int counterValue, List<String> symbols) {
		if (counterValue < 1 || counterValue > symbols.size())
			return "";
		else
			return symbols.get(counterValue - 1);
	}

	static String counterRepresentationNumeric(int counterValue, List<String> symbols) {
		if (counterValue < 0)
			return "-" + counterRepresentationNumeric(- counterValue, symbols);
		if (counterValue >= symbols.size())
			return counterRepresentationNumeric(counterValue / symbols.size(), symbols)
				+ symbols.get(mod(counterValue, symbols.size()));
		else
			return symbols.get(counterValue);
	}

	static String counterRepresentationSymbolic(int counterValue, List<String> symbols) {
		if (counterValue < 1)
			return "";
		String symbol = symbols.get(mod(counterValue - 1, symbols.size()));
		String s = symbol;
		for (int i = 0; i < ((counterValue - 1) / symbols.size()); i++)
			s += symbol;
		return s;
	}
}
