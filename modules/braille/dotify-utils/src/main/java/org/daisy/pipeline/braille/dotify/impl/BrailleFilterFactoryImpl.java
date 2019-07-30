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

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Iterables.concat;
import com.google.common.collect.Lists;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermString;

import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.SimpleInlineStyle;

import org.daisy.dotify.api.translator.AttributeWithContext;
import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.BrailleFilterFactory;
import org.daisy.dotify.api.translator.FollowingText;
import org.daisy.dotify.api.translator.PrecedingText;
import org.daisy.dotify.api.translator.ResolvableText;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslationException;

import org.daisy.pipeline.braille.common.BrailleTranslator;
import org.daisy.pipeline.braille.common.BrailleTranslatorProvider;
import org.daisy.pipeline.braille.common.CSSStyledText;
import org.daisy.pipeline.braille.common.Provider;
import static org.daisy.pipeline.braille.common.Provider.util.memoize;
import static org.daisy.pipeline.braille.common.Provider.util.dispatch;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.query;
import static org.daisy.pipeline.braille.common.Query.util.QUERY;
import static org.daisy.pipeline.braille.common.util.Strings.join;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.dotify.impl.BrailleFilterFactoryImpl",
	service = { BrailleFilterFactoryImpl.class }
)
public class BrailleFilterFactoryImpl implements BrailleFilterFactory {
	
	public BrailleFilterImpl newFilter(String locale, String mode) throws TranslatorConfigurationException {
		try {
			BrailleTranslator.FromStyledTextToBraille translator = getBrailleTranslator(mode);
			return new BrailleFilterImpl(translator); }
		catch (NoSuchElementException e) {
			throw new TranslatorConfigurationException("Factory does not support " + locale + "/" + mode); }
	}
	
	private BrailleTranslator.FromStyledTextToBraille getBrailleTranslator(String mode) throws NoSuchElementException {
		Matcher m = QUERY.matcher(mode);
		if (!m.matches())
			throw new NoSuchElementException();
		Query query = query(mode);
		for (BrailleTranslator t : brailleTranslatorProvider.get(query))
			try { return t.fromStyledTextToBraille(); }
			catch (UnsupportedOperationException e) {}
		MutableQuery q = mutableQuery(query);
		for (Feature f : q.removeAll("input"))
			if (!"text-css".equals(f.getValue().get()))
				throw new NoSuchElementException();
		for (Feature f : q.removeAll("output"))
			if (!"braille".equals(f.getValue().get()))
				throw new NoSuchElementException();
		if (!q.isEmpty())
			throw new NoSuchElementException();
		return NumberBrailleTranslator.getInstance().fromStyledTextToBraille();
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
	protected void bindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
		brailleTranslatorProviders.add((BrailleTranslatorProvider<BrailleTranslator>)provider);
		logger.debug("Adding BrailleTranslator provider: {}", provider);
	}
	
	protected void unbindBrailleTranslatorProvider(BrailleTranslatorProvider<?> provider) {
		brailleTranslatorProviders.remove(provider);
		brailleTranslatorProvider.invalidateCache();
		logger.debug("Removing BrailleTranslator provider: {}", provider);
	}
	
	private final List<BrailleTranslatorProvider<BrailleTranslator>> brailleTranslatorProviders
	= new ArrayList<BrailleTranslatorProvider<BrailleTranslator>>();
	
	private final Provider.util.MemoizingProvider<Query,BrailleTranslator> brailleTranslatorProvider
	= memoize(dispatch(brailleTranslatorProviders));
	
	/**
	 * BrailleFilter wrapper for a org.daisy.pipeline.braille.common.BrailleTranslator.FromStyledTextToBraille
	 *
	 * Supports special variable assignments (in the form of "-dotify-def:foo") and tests (in the form of
	 * "-dotify-ifdef:foo" or "-dotify-ifndef:foo") in text attributes in order to support special ad hoc
	 * handling of marker-references.
	 */
	public static class BrailleFilterImpl implements BrailleFilter {
		
		private final BrailleTranslator.FromStyledTextToBraille translator;
		
		private BrailleFilterImpl(BrailleTranslator.FromStyledTextToBraille translator) {
			this.translator = translator;
		}
		
		public String filter(Translatable specification) throws TranslationException {
			return join(filterRetain(specification));
		}
		
		private String[] filterRetain(Translatable specification) throws TranslationException {
			if (specification.getAttributes() == null && !specification.isHyphenating()) {
				String text = specification.getText();
				
				// If input text is a space, it will be user for calculating the margin character
				// (see org.daisy.dotify.formatter.impl.common.FormatterCoreContext)
				if (" ".equals(text))
					return new String[]{"\u2800"};
			
				// see org.daisy.dotify.formatter.impl.row.SegmentProcessor.layoutLeader
				if ("".equals(text))
					return new String[]{""};
			}
			Iterable<String> result = translator.transform(cssStyledTextFromTranslatable(specification));
			String[] array = new String[size(result)];
			int i = 0;
			for (String s : result)
				array[i++] = s;
			return array;
		}
		
		public String filter(TranslatableWithContext specification) throws TranslationException {
			return join(filterRetain(specification));
		}
		
		private String[] filterRetain(TranslatableWithContext specification) throws TranslationException {
			List<CSSStyledText> styledText = Lists.newArrayList(cssStyledTextFromTranslatable(specification));
			int from = specification.getPrecedingText().size();
			int to = from + specification.getTextToTranslate().size();
			// If input text is "??", it will be used for creating a placeholder for content that
			// can not be computed yet (see org.daisy.dotify.formatter.impl.row.SegmentProcessor).
			// Because normally this will never end up in the resulting PEF, it is okay to replace
			// it with "0". We choose a number because in some cases the translator may assume that
			// the input is numerical.
			for (int i = 0; i < styledText.size(); i++)
				if ("??".equals(styledText.get(i).getText())
				    && (styledText.get(i).getStyle() == null || styledText.get(i).getStyle().isEmpty()))
					styledText.set(i, new CSSStyledText("0"));
			Iterable<String> result = translator.transform(styledText, from, to);
			String[] array = new String[size(result)];
			int i = 0;
			for (String s : result)
				array[i++] = s;
			return array;
		}
		
		@Override
		public String toString() {
			return MoreObjects.toStringHelper("o.d.p.b.dotify.impl.BrailleFilterFactoryImpl$BrailleFilterImpl")
				.add("translator", translator)
				.toString();
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(BrailleFilterFactoryImpl.class);
	
	/* ================ */
	/* SHARED UTILITIES */
	/* ================ */
	
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
			if (hyphenating) {
				// FIXME: add hyphens declaration through braille-css model
				if (s == null || s.isEmpty())
					s = "hyphens: auto";
				else
					s += "; hyphens: auto"; }
			if (s == null && parentStyle == null)
				style = null;
			else
				// FIXME: extend caching of parsed CSS to support parentStyle!
				style = new SimpleInlineStyle(s != null ? s : "", parentStyle); }
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
	protected static Iterable<CSSStyledText> cssStyledTextFromTranslatable(TranslatableWithContext specification) {
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
				String s = hyphenate ? "hyphens: auto" : null;
				if (s == null && parentStyle == null)
					style = null;
				else
					// FIXME: extend caching of parsed CSS to support parentStyle!
					style = new SimpleInlineStyle(s != null ? s : "", parentStyle);
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
					String var = s.getProperty(key, false).toString();
					s.removeProperty(key);
					if (env == null)
						env = new HashSet<String>();
					if (key.equals("-dotify-ifdef") && !env.contains(var)
					    || (key.equals("-dotify-ifndef") || key.equals("-dotify-defifndef")) && env.contains(var))
						t = "";
					if (key.equals("-dotify-def") || key.equals("-dotify-defifndef"))
						env.add(var); }}
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
							// If input text is "??", it will be used for creating a placeholder for content that
							// can not be computed yet (see org.daisy.dotify.formatter.impl.row.SegmentProcessor).
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
	
	protected static String counterRepresentationAlphabetic(int counterValue, List<String> symbols) {
		if (counterValue < 1)
			return "";
		if (counterValue > symbols.size())
			return counterRepresentationAlphabetic((counterValue - 1) / symbols.size(), symbols)
				+ symbols.get(mod(counterValue - 1, symbols.size()));
		else
			return symbols.get(counterValue - 1);
	}
	
	protected static String counterRepresentationCyclic(int counterValue, List<String> symbols) {
		return symbols.get(mod(counterValue - 1, symbols.size()));
	}
	
	protected static String counterRepresentationFixed(int counterValue, List<String> symbols) {
		if (counterValue < 1 || counterValue > symbols.size())
			return "";
		else
			return symbols.get(counterValue - 1);
	}
	
	protected static String counterRepresentationNumeric(int counterValue, List<String> symbols) {
		if (counterValue < 0)
			return "-" + counterRepresentationNumeric(- counterValue, symbols);
		if (counterValue >= symbols.size())
			return counterRepresentationNumeric(counterValue / symbols.size(), symbols)
				+ symbols.get(mod(counterValue, symbols.size()));
		else
			return symbols.get(counterValue);
	}
	
	protected static String counterRepresentationSymbolic(int counterValue, List<String> symbols) {
		if (counterValue < 1)
			return "";
		String symbol = symbols.get(mod(counterValue - 1, symbols.size()));
		String s = symbol;
		for (int i = 0; i < ((counterValue - 1) / symbols.size()); i++)
			s += symbol;
		return s;
	}
}
