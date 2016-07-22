package org.daisy.braille.css;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import cz.vutbr.web.css.CSSFactory;
import cz.vutbr.web.css.CSSProperty;
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
import org.daisy.braille.css.BrailleCSSProperty.Border;
import org.daisy.braille.css.BrailleCSSProperty.Content;
import org.daisy.braille.css.BrailleCSSProperty.Display;
import org.daisy.braille.css.BrailleCSSProperty.Flow;
import org.daisy.braille.css.BrailleCSSProperty.Hyphens;
import org.daisy.braille.css.BrailleCSSProperty.LetterSpacing;
import org.daisy.braille.css.BrailleCSSProperty.LineHeight;
import org.daisy.braille.css.BrailleCSSProperty.ListStyleType;
import org.daisy.braille.css.BrailleCSSProperty.Margin;
import org.daisy.braille.css.BrailleCSSProperty.MaxLength;
import org.daisy.braille.css.BrailleCSSProperty.MinLength;
import org.daisy.braille.css.BrailleCSSProperty.Padding;
import org.daisy.braille.css.BrailleCSSProperty.Page;
import org.daisy.braille.css.BrailleCSSProperty.RenderTableBy;
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
	
	private static final int TOTAL_SUPPORTED_DECLARATIONS = 47;
	
	private static final TermFactory tf = CSSFactory.getTermFactory();
	
	private static final CSSProperty DEFAULT_UA_TEXT_ALIGN = TextAlign.LEFT;
	private static final Term<?> DEFAULT_UA_TEXT_IDENT = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_MARGIN = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_PADDING = tf.createInteger(0);
	private static final Term<?> DEFAULT_UA_ORPHANS = tf.createInteger(2);
	private static final Term<?> DEFAULT_UA_WIDOWS = tf.createInteger(2);
	private static final Term<?> DEFAULT_UA_LINE_HEIGHT = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_LETTER_SPACING = tf.createInteger(1);
	private static final Term<?> DEFAULT_UA_WORD_SPACING = tf.createInteger(1);
	
	private Map<String, CSSProperty> defaultCSSproperties;
	private Map<String, Term<?>> defaultCSSvalues;
	
	private Map<String, Integer> ordinals;
	private Map<Integer, String> ordinalsRev;
	
	private static SupportedBrailleCSS instance;
	
	public final static SupportedBrailleCSS getInstance() {
		if (instance == null)
			instance = new SupportedBrailleCSS();
		return instance;
	}
	
	private SupportedBrailleCSS() {
		this.setSupportedCSS();
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
		return defaultCSSproperties.containsKey(property);
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
	
	private void setSupportedCSS() {
		
		Map<String, CSSProperty> props = new HashMap<String, CSSProperty>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		Map<String, Term<?>> values = new HashMap<String, Term<?>>(TOTAL_SUPPORTED_DECLARATIONS, 1.0f);
		
		// text spacing
		props.put("text-align", DEFAULT_UA_TEXT_ALIGN);
		props.put("text-indent", TextIndent.integer);
		values.put("text-indent", DEFAULT_UA_TEXT_IDENT);
		props.put("line-height", LineHeight.integer);
		values.put("line-height", DEFAULT_UA_LINE_HEIGHT);
		
		// layout box
		props.put("left", AbsoluteMargin.integer);
		values.put("left", DEFAULT_UA_MARGIN);
		props.put("right", AbsoluteMargin.integer);
		values.put("right", DEFAULT_UA_MARGIN);
		
		props.put("margin", Margin.component_values);
		props.put("margin-top", Margin.integer);
		values.put("margin-top", DEFAULT_UA_MARGIN);
		props.put("margin-right", Margin.integer);
		values.put("margin-right", DEFAULT_UA_MARGIN);
		props.put("margin-bottom", Margin.integer);
		values.put("margin-bottom", DEFAULT_UA_MARGIN);
		props.put("margin-left", Margin.integer);
		values.put("margin-left", DEFAULT_UA_MARGIN);

		props.put("padding", Padding.component_values);
		props.put("padding-top", Padding.integer);
		values.put("padding-top", DEFAULT_UA_PADDING);
		props.put("padding-right", Padding.integer);
		values.put("padding-right", DEFAULT_UA_PADDING);
		props.put("padding-bottom", Padding.integer);
		values.put("padding-bottom", DEFAULT_UA_PADDING);
		props.put("padding-left", Padding.integer);
		values.put("padding-left", DEFAULT_UA_PADDING);
		
		props.put("border", Border.component_values);
		props.put("border-top", Border.NONE);
		props.put("border-right", Border.NONE);
		props.put("border-bottom", Border.NONE);
		props.put("border-left", Border.NONE);
		
		// positioning
		props.put("display", Display.INLINE);
		
		// elements
		props.put("list-style-type", ListStyleType.NONE);
		
		// paged
		props.put("page", Page.AUTO);
		props.put("page-break-before", PageBreak.AUTO);
		props.put("page-break-after", PageBreak.AUTO);
		props.put("page-break-inside", PageBreakInside.AUTO);
		props.put("orphans", Orphans.integer);
		values.put("orphans", DEFAULT_UA_ORPHANS);
		props.put("widows", Widows.integer);
		values.put("widows", DEFAULT_UA_WIDOWS);
		
		// @volume rule
		props.put("min-length", MinLength.AUTO);
		props.put("max-length", MaxLength.AUTO);
		
		// volume breaking
		props.put("volume-break-before", VolumeBreak.AUTO);
		props.put("volume-break-after", VolumeBreak.AUTO);
		props.put("volume-break-inside", VolumeBreakInside.AUTO);
		
		// tables
		props.put("render-table-by", RenderTableBy.AUTO);
		props.put("table-header-policy", TableHeaderPolicy.ONCE);
		
		// misc
		props.put("counter-reset", CounterReset.NONE);
		props.put("counter-set", CounterSet.NONE);
		props.put("counter-increment", CounterIncrement.NONE);
		props.put("string-set", StringSet.NONE);
		props.put("content", Content.NONE);
		props.put("text-transform", TextTransform.AUTO);
		props.put("white-space", WhiteSpace.NORMAL);
		props.put("hyphens", Hyphens.MANUAL);
		props.put("letter-spacing", LetterSpacing.length);
		values.put("letter-spacing", DEFAULT_UA_LETTER_SPACING);
		props.put("word-spacing", WordSpacing.length);
		values.put("word-spacing", DEFAULT_UA_WORD_SPACING);
		props.put("flow", Flow.NORMAL);
		
		this.defaultCSSproperties = props;
		this.defaultCSSvalues = values;
		
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
