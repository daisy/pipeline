package org.daisy.braille.css;

import cz.vutbr.web.css.CSSProperty;

public interface BrailleCSSProperty extends CSSProperty {

	/************************************************************************
	 * BRAILLE CSS PROPERTIES *
	 ************************************************************************/
	
	public enum AbsoluteMargin implements BrailleCSSProperty {
		integer(""), AUTO("auto"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private AbsoluteMargin(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum BorderAlign implements BrailleCSSProperty {
		component_values(""), INNER("inner"), CENTER("center"), OUTER("outer"),
		INHERIT("inherit"), INITIAL("initial");

		private String text;

		private BorderAlign(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum BorderPattern implements BrailleCSSProperty {
		dot_pattern(""), NONE("none"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private BorderPattern(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum BorderStyle implements BrailleCSSProperty {
		component_values(""), NONE("none"), SOLID("solid"), INHERIT("inherit"),
		INITIAL("initial");

		private String text;

		private BorderStyle(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum BorderWidth implements BrailleCSSProperty {
		integer(""), component_values(""), THIN("thin"), MEDIUM("medium"),
		THICK("thick"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private BorderWidth(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum BrailleCharset implements BrailleCSSProperty {
		UNICODE("unicode"), CUSTOM("custom"), INHERIT("inherit"), INITIAL("initial");
		
		private String text;
		
		private BrailleCharset(String text) {
			this.text = text;
		}
		
		public boolean inherited() {
			return true;
		}
		
		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum Content implements BrailleCSSProperty {
		content_list(""), NONE("none"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Content(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}
	
		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum Display implements BrailleCSSProperty {
		INLINE("inline"), BLOCK("block"), LIST_ITEM("list-item"), NONE("none"),
		TABLE("table"), custom(""), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Display(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum Flow implements CSSProperty {
		identifier(""), NORMAL("normal"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Flow(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum HyphenateCharacter implements BrailleCSSProperty {
		braille_string(""), AUTO("auto"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private HyphenateCharacter(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return true;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum Hyphens implements BrailleCSSProperty {
		NONE("none"), AUTO("auto"), MANUAL("manual"), INHERIT("inherit"),
		INITIAL("initial");

		private String text;

		private Hyphens(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return true;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum LetterSpacing implements BrailleCSSProperty {
		length(""), INHERIT("inherit"), INITIAL("initial");
		
		private String text;
		
		private LetterSpacing(String text) {
			this.text = text;
			}
		
		public boolean inherited() {
			return true;
		}
		
		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum LineHeight implements BrailleCSSProperty {
		number(""), percentage(""), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private LineHeight(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return true;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum ListStyleType implements BrailleCSSProperty {
		braille_string(""), symbols_fn(""), counter_style_name(""),
		DECIMAL("decimal"), LOWER_ALPHA("lower-alpha"), LOWER_ROMAN("lower-roman"),
		NONE("none"), UPPER_ALPHA("upper-alpha"), UPPER_ROMAN("upper-roman"),
		INHERIT("inherit"), INITIAL("initial");

		private String text;

		private ListStyleType(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return true;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Margin implements BrailleCSSProperty {
		integer(""), component_values(""), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Margin(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum MaxHeight implements BrailleCSSProperty {
		integer(""), NONE("none"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private MaxHeight(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum MaxLength implements BrailleCSSProperty {
		integer(""), AUTO("auto"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private MaxLength(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum MinLength implements BrailleCSSProperty {
		integer(""), AUTO("auto"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private MinLength(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Orphans implements BrailleCSSProperty {
		integer(""), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Orphans(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Padding implements BrailleCSSProperty {
		integer(""), component_values(""), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Padding(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Page implements CSSProperty {
		identifier(""), AUTO("auto"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Page(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return true;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum RenderTableBy implements BrailleCSSProperty {
		axes(""), AUTO("auto"), INHERIT("inherit"), INITIAL("initial");
		
		private String text;

		private RenderTableBy(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum Size implements BrailleCSSProperty {
		integer_pair(""), AUTO("auto"), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Size(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum StringSet implements BrailleCSSProperty {
		list_values(""), NONE("none"), INHERIT("inherit"), INITIAL("initial");
		
		private String text;

		private StringSet(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum TableHeaderPolicy implements BrailleCSSProperty {
		ONCE("once"), ALWAYS("always"), FRONT("front"), INHERIT("inherit"),
		INITIAL("initial");

		private String text;

		private TableHeaderPolicy(String text) {
			this.text = text;
		}
		
		// TODO: make inherited?
		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum TextIndent implements BrailleCSSProperty {
		integer(""), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private TextIndent(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return true;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum TextTransform implements BrailleCSSProperty {
		list_values(""), AUTO("auto"), NONE("none"), INHERIT("inherit"),
		INITIAL("initial");
		
		private String text;
		
		private TextTransform(String text) {
			this.text = text;
		}
		
		public boolean inherited() {
			return true;
		}
		
		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum VolumeBreak implements BrailleCSSProperty {
		ALWAYS("always"), AUTO("auto"), AVOID("avoid"), PREFER("prefer"),
		INHERIT("inherit"), INITIAL("initial");

		private String text;

		private VolumeBreak(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum VolumeBreakInside implements BrailleCSSProperty {
		AUTO("auto"), AVOID("avoid"), custom(""), INHERIT("inherit"),
		INITIAL("initial");

		private String text;

		private VolumeBreakInside(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}
	
	public enum WhiteSpace implements BrailleCSSProperty {
		NORMAL("normal"), PRE_WRAP("pre-wrap"), PRE_LINE("pre-line"),
		INHERIT("inherit"), INITIAL("initial");
		
		private String text;
		
		private WhiteSpace(String text) {
			this.text = text;
		}
		
		public boolean inherited() {
			return true;
		}
		
		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}

	public enum Widows implements BrailleCSSProperty {
		integer(""), INHERIT("inherit"), INITIAL("initial");

		private String text;

		private Widows(String text) {
			this.text = text;
		}

		public boolean inherited() {
			return false;
		}

		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}

		@Override
		public String toString() {
			return text;
		}
	}

	public enum WordSpacing implements BrailleCSSProperty {
		length(""), INHERIT("inherit"), INITIAL("initial");
		
		private String text;
		
		private WordSpacing(String text) {
			this.text = text;
		}
		
		public boolean inherited() {
			return true;
		}
		
		public boolean equalsInherit() {
			return this == INHERIT;
		}

		public boolean equalsInitial() {
			return this == INITIAL;
		}
		
		@Override
		public String toString() {
			return text;
		}
	}
	
	// vendor properties are inherited
	public class GenericVendorCSSPropertyProxy implements CSSProperty {
		
		public static GenericVendorCSSPropertyProxy INHERIT = GenericVendorCSSPropertyProxy.valueOf("inherit");
		
		private String text;
		
		private GenericVendorCSSPropertyProxy(String text) {
			this.text = text;
		}
		
		@Override
		public boolean inherited() {
			return true;
		}
		
		@Override
		public boolean equalsInherit() {
			return "inherit".equals(text);
		}
		
		@Override
		public boolean equalsInitial() {
			return "initial".equals(text);
		}
		
		@Override
		public String toString() {
			return text;
		}
		
		public static GenericVendorCSSPropertyProxy valueOf(String value) {
			return new GenericVendorCSSPropertyProxy(value == null ? "" : value.toLowerCase());
		}
	}
}
