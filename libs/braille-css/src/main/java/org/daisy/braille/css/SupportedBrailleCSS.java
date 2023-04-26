package org.daisy.braille.css;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.CSSProperty.Border;
import cz.vutbr.web.css.CSSProperty.CounterIncrement;
import cz.vutbr.web.css.CSSProperty.CounterReset;
import cz.vutbr.web.css.CSSProperty.CounterSet;
import cz.vutbr.web.css.CSSProperty.Orphans;
import cz.vutbr.web.css.CSSProperty.PageBreak;
import cz.vutbr.web.css.CSSProperty.PageBreakInside;
import cz.vutbr.web.css.CSSProperty.TextAlign;
import cz.vutbr.web.css.CSSProperty.Widows;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFactory;

import org.daisy.braille.css.BrailleCSSProperty.AbsoluteMargin;
import org.daisy.braille.css.BrailleCSSProperty.BorderAlign;
import org.daisy.braille.css.BrailleCSSProperty.BorderPattern;
import org.daisy.braille.css.BrailleCSSProperty.BorderStyle;
import org.daisy.braille.css.BrailleCSSProperty.BorderWidth;
import org.daisy.braille.css.BrailleCSSProperty.BrailleCharset;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.Flow;
import org.daisy.braille.css.BrailleCSSProperty.HyphenateCharacter;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.LetterSpacing;
import org.daisy.braille.css.BrailleCSSProperty.LineHeight;
import org.daisy.braille.css.BrailleCSSProperty.ListStyleType;
import org.daisy.braille.css.BrailleCSSProperty.Margin;
import org.daisy.braille.css.BrailleCSSProperty.MaxHeight;
import org.daisy.braille.css.BrailleCSSProperty.MaxLength;
import org.daisy.braille.css.BrailleCSSProperty.MinLength;
import org.daisy.braille.css.BrailleCSSProperty.Padding;
import org.daisy.braille.css.BrailleCSSProperty.Page;
import org.daisy.braille.css.BrailleCSSProperty.RenderTableBy;
import org.daisy.braille.css.BrailleCSSProperty.Size;
import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.BrailleCSSProperty.TableHeaderPolicy;
import org.daisy.braille.css.BrailleCSSProperty.TextIndent;
import org.daisy.braille.css.BrailleCSSProperty.TextTransform;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreak;
import org.daisy.braille.css.BrailleCSSProperty.VolumeBreakInside;
import org.daisy.braille.css.BrailleCSSProperty.WhiteSpace;
import org.daisy.braille.css.BrailleCSSProperty.WordSpacing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * @author bert
 */
public class SupportedBrailleCSS implements SupportedCSS {
	
	private static Logger log = LoggerFactory.getLogger(SupportedBrailleCSS.class);
	
	private static final int TOTAL_SUPPORTED_DECLARATIONS = 70;
	
	private static final TermFactory tf = CSSFactory.getTermFactory();
	
	private static final CSSProperty DEFAULT_UA_TEXT_ALIGN = TextAlign.LEFT;
	private static final Term<?> DEFAULT_UA_TEXT_IDENT = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_MARGIN = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_PADDING = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_BORDER_WIDTH = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_ORPHANS = tf.createInteger(2);
	private static final Term<?> DEFAULT_UA_WIDOWS = tf.createInteger(2);
	private static final Term<?> DEFAULT_UA_LINE_HEIGHT = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_LETTER_SPACING = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_WORD_SPACING = tf.createInteger(1);
	
	private Set<String> supportedCSSproperties;
	private Map<String, CSSProperty> defaultCSSproperties;
	private Map<String, Term<?>> defaultCSSvalues;
	
	private Map<String, Integer> ordinals;
	private Map<Integer, String> ordinalsRev;
	
	public SupportedBrailleCSS() {
		this(false, true);
	}
	
	public SupportedBrailleCSS(boolean allowComponentProperties, boolean allowShorthandProperties) {
		this.setSupportedCSS(allowComponentProperties, allowShorthandProperties);
		this.setOridinals();
	}
	
	@Override
	public boolean isSupportedMedia(String media) {
		if (media == null)
			return false;
		return media.toLowerCase().equals("embossed");
	}
	
	@Override
	public final boolean isSupportedCSSProperty(String property) {
		if (supportedCSSproperties.contains(property))
			return true;
		else {
			if (defaultCSSproperties.containsKey(property))
				log.info("Shorthand or component property not supported: {}");
			return false; }
	}
	
	@Override
	public final CSSProperty getDefaultProperty(String property) {
		CSSProperty value = defaultCSSproperties.get(property);
		log.debug("Asked for property {}'s default value: {}", property, value);
		return value;
	}
	
	@Override
	public final Term<?> getDefaultValue(String property) {
		return defaultCSSvalues.get(property);
	}
	
	@Override
	public final int getTotalProperties() {
		return defaultCSSproperties.size();
	}
	
	@Override
	public final Set<String> getDefinedPropertyNames() {
		return defaultCSSproperties.keySet();
	}
	
	@Override
	public String getRandomPropertyName() {
		final Random generator = new Random();
		int o = generator.nextInt(getTotalProperties());
		return getPropertyName(o);
	}
	
	@Override
	public int getOrdinal(String propertyName) {
		Integer i = ordinals.get(propertyName);
		return (i == null) ? -1 : i.intValue();
	}
	
	@Override
	public String getPropertyName(int o) {
		return ordinalsRev.get(o);
	}
	
	private void setProperty(String name, CSSProperty defaultProperty) {
		setProperty(name, true, defaultProperty, null);
	}
	
	private void setProperty(String name, boolean allow, CSSProperty defaultProperty) {
		setProperty(name, allow, defaultProperty, null);
	}
	
	private void setProperty(String name, CSSProperty defaultProperty, Term<?> defaultValue) {
		setProperty(name, true, defaultProperty, defaultValue);
	}
	
	private void setProperty(String name, boolean allow, CSSProperty defaultProperty, Term<?> defaultValue) {
		if (allow)
			supportedCSSproperties.add(name);
		if (defaultProperty != null)
			defaultCSSproperties.put(name, defaultProperty);
		if (defaultValue != null)
			defaultCSSvalues.put(name, defaultValue);
	}
	
	private void setSupportedCSS(boolean allowComponentProperties, boolean allowShorthandProperties) {
		
		supportedCSSproperties = new HashSet<String>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSproperties = new HashMap<String, CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		defaultCSSvalues = new HashMap<String, Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);;
		
		// text spacing
		setProperty("text-align", DEFAULT_UA_TEXT_ALIGN);
		setProperty("text-indent", TextIndent.integer, DEFAULT_UA_TEXT_IDENT);
		setProperty("line-height", LineHeight.number, DEFAULT_UA_LINE_HEIGHT);
		
		// layout box
		setProperty("left", AbsoluteMargin.AUTO);
		setProperty("right", AbsoluteMargin.AUTO);
		
		setProperty("margin-top", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin-right", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin-bottom", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin-left", Margin.integer, DEFAULT_UA_MARGIN);
		setProperty("margin", allowShorthandProperties, Margin.component_values);
		
		setProperty("padding-top", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding-right", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding-bottom", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding-left", Padding.integer, DEFAULT_UA_PADDING);
		setProperty("padding", allowShorthandProperties, Padding.component_values);
		
		setProperty("border-top-pattern", allowComponentProperties, BorderPattern.NONE);
		setProperty("border-right-pattern", allowComponentProperties, BorderPattern.NONE);
		setProperty("border-bottom-pattern", allowComponentProperties, BorderPattern.NONE);
		setProperty("border-left-pattern", allowComponentProperties, BorderPattern.NONE);
		
		setProperty("border-top-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-right-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-bottom-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-left-style", allowComponentProperties, BorderStyle.NONE);
		setProperty("border-style", allowShorthandProperties, BorderStyle.component_values);
		
		setProperty("border-top-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-right-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-bottom-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-left-width", allowComponentProperties, BorderWidth.integer, DEFAULT_UA_BORDER_WIDTH);
		setProperty("border-width", allowShorthandProperties, BorderWidth.component_values);
		
		setProperty("border-top-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-right-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-bottom-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-left-align", allowComponentProperties, BorderAlign.CENTER);
		setProperty("border-align", allowShorthandProperties, BorderAlign.component_values);
		
		setProperty("border-top", allowShorthandProperties, Border.component_values);
		setProperty("border-right", allowShorthandProperties, Border.component_values);
		setProperty("border-bottom", allowShorthandProperties, Border.component_values);
		setProperty("border-left", allowShorthandProperties, Border.component_values);
		setProperty("border", allowShorthandProperties, Border.component_values);
		
		// positioning
		setProperty("display", Display.INLINE);
		
		// elements
		setProperty("list-style-type", ListStyleType.NONE);
		setProperty("list-style", allowShorthandProperties, ListStyleType.NONE);
		
		// @page rule
		setProperty("size", Size.AUTO);
		
		// paged
		setProperty("page", Page.AUTO);
		setProperty("page-break-before", PageBreak.AUTO);
		setProperty("page-break-after", PageBreak.AUTO);
		setProperty("page-break-inside", PageBreakInside.AUTO);
		setProperty("orphans", Orphans.integer, DEFAULT_UA_ORPHANS);
		setProperty("widows", Widows.integer, DEFAULT_UA_WIDOWS);
		
		// @footnotes rule
		setProperty("max-height", MaxHeight.NONE);
		
		// @volume rule
		setProperty("min-length", MinLength.AUTO);
		setProperty("max-length", MaxLength.AUTO);
		
		// volume breaking
		setProperty("volume-break-before", VolumeBreak.AUTO);
		setProperty("volume-break-after", VolumeBreak.AUTO);
		setProperty("volume-break-inside", VolumeBreakInside.AUTO);
		
		// tables
		setProperty("render-table-by", RenderTableBy.AUTO);
		setProperty("table-header-policy", TableHeaderPolicy.ONCE);
		
		// misc
		setProperty("counter-reset", CounterReset.NONE);
		setProperty("counter-set", CounterSet.NONE);
		setProperty("counter-increment", CounterIncrement.NONE);
		setProperty("string-set", StringSet.NONE);
		setProperty("content", Content.NONE);
		setProperty("text-transform", TextTransform.AUTO);
		setProperty("braille-charset", BrailleCharset.UNICODE);
		setProperty("white-space", WhiteSpace.NORMAL);
		setProperty("hyphenate-character", HyphenateCharacter.AUTO);
		setProperty("hyphens", Hyphens.MANUAL);
		setProperty("letter-spacing", LetterSpacing.length, DEFAULT_UA_LETTER_SPACING);
		setProperty("word-spacing", WordSpacing.length, DEFAULT_UA_WORD_SPACING);
		setProperty("flow", Flow.NORMAL);
		
	}
	
	private void setOridinals() {
		
		Map<String, Integer> ords = new HashMap<String, Integer>(getTotalProperties(), 1.0f);
		Map<Integer, String> ordsRev = new HashMap<Integer, String>(getTotalProperties(), 1.0f);
		
		int i = 0;
		for (String key : defaultCSSproperties.keySet()) {
			ords.put(key, i);
			ordsRev.put(i, key);
			i++;
		}
		
		this.ordinals = ords;
		this.ordinalsRev = ordsRev;
		
	}
}
