package org.daisy.pipeline.braille.css.impl;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermLengthOrPercent;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.TermURI;
import cz.vutbr.web.csskit.TermStringImpl;
import cz.vutbr.web.csskit.TermURIImpl;

import org.daisy.braille.css.BrailleCSSExtension;
import org.daisy.braille.css.BrailleCSSParserFactory.Context;
import org.daisy.braille.css.SupportedBrailleCSS;
import org.daisy.common.file.URLs;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * This class is immutable
 */
public class ContentList extends AbstractList<Term<?>> implements Term<ContentList> {

	private List<Term<?>> list;
	private final BrailleCssParser parser;
	private final Context context;

	private ContentList(BrailleCssParser parser, Context context, List<Term<?>> list) {
		super();
		this.list = list;
		this.parser = parser;
		this.context = context;
	}

	/**
	 * @param list assumed to not change
	 */
	public static ContentList of(BrailleCssParser parser, Context context, List<Term<?>> list) {
		return of(parser, null, context, list);
	}

	public static ContentList of(BrailleCssParser parser,
	                             Collection<BrailleCSSExtension> extensions,
	                             Context context,
	                             List<Term<?>> list) {
		List<Term<?>> items = new ArrayList<>();
		for (Term<?> t : list) {
			if (t instanceof UnmodifiableTermString ||
			         t instanceof AttrFunction ||
			         t instanceof ContentFunction ||
			         t instanceof StringFunction ||
			         t instanceof CounterFunction ||
			         t instanceof TextFunction ||
			         t instanceof LeaderFunction ||
			         t instanceof FlowFunction)
				items.add(t);
			else if (t instanceof TermString)
				items.add(new UnmodifiableTermString((TermString)t));
			else if (t instanceof TermFunction) {
				TermFunction func = (TermFunction)t;
				String funcname = func.getFunctionName();
				if ("attr".equals(funcname)) {
					AttrFunction f = AttrFunction.of(func);
					if (f != null && !f.asURL) items.add(f); }
				else if ("content".equals(funcname) || "target-content".equals(funcname)) {
					ContentFunction f = ContentFunction.of(func);
					if (f != null) items.add(f); }
				else if ("string".equals(funcname) || "target-string".equals(funcname)) {
					StringFunction f = StringFunction.of(func);
					if (f != null) items.add(f); }
				else if ("counter".equals(funcname) || "target-counter".equals(funcname)) {
					CounterFunction f = CounterFunction.of(func, extensions);
					if (f != null) items.add(f); }
				else if ("target-text".equals(funcname)) {
					TextFunction f = TextFunction.of(func);
					if (f != null) items.add(f); }
				else if ("leader".equals(funcname)) {
					LeaderFunction f = LeaderFunction.of(func);
					if (f != null) items.add(f); }
				else if ("flow".equals(funcname)) {
					FlowFunction f = FlowFunction.of(func);
					if (f != null) items.add(f); }
				else if (funcname.matches("-(\\p{L}|_)+-.*")) // vendor prefixed
					items.add(func);
			} else
				throw new IllegalArgumentException("unexpected term in content list: " + t);
		}
		return new ContentList(parser, context, items);
	}

	@Override
	public Term<?> get(int index) {
		return list.get(index);
	}

	@Override
	public int size() {
		return list.size();
	}

	@Override
	public ContentList getValue() {
		return this;
	}

	@Override
	public Operator getOperator() {
		return null;
	}

	@Override
	public ContentList shallowClone() {
		try {
			return (ContentList)super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("coding error");
		}
	}

	@Override
	public ContentList clone() {
		ContentList clone = shallowClone();
		clone.list = new ArrayList<>(list);
		return clone;
	}

	@Override
	public ContentList setValue(ContentList value) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	@Override
	public ContentList setOperator(Operator operator) {
		throw new UnsupportedOperationException("Unmodifiable");
	}

	/**
	 * {@link BrailleCssParser} instance that was used to create this {@link ContentList}.
	 */
	public BrailleCssParser getParser() {
		return parser;
	}

	/**
	 * {@link Context} in which this {@link ContentList} was created.
	 */
	public Context getContext() {
		return context;
	}

	/**
	 * Evaluate <code>attr()</code> and <code>content()</code> values.
	 *
	 * This method is mutating, but we can still say that the object is immutable because the method
	 * is package private and only used by {@link BrailleCssStyle}, {@link StringSetList} and {@link
	 * BrailleCssParser} _before_ the object is made available.
	 */
	void evaluate(Element context) {
		for (int i = 0; i < list.size(); i++) {
			Term<?> t = list.get(i);
			if (t instanceof AttrFunction)
				list.set(i, ((AttrFunction)t).evaluate(context));
			if (t instanceof ContentFunction)
				list.set(i, ((ContentFunction)t).evaluate(context));
			else if (t instanceof StringFunction)
				list.set(i, ((StringFunction)t).evaluate(context));
			else if (t instanceof CounterFunction)
				list.set(i, ((CounterFunction)t).evaluate(context));
			else if (t instanceof TextFunction)
				list.set(i, ((TextFunction)t).evaluate(context));
			if (list.get(i) == null)
				list.remove(i--);
		}
	}

	/**
	 * This class is immutable
	 */
	public static class AttrFunction extends UnmodifiableTerm<AttrFunction> {

		// these fields need to be package private because they are used in BrailleCssSerializer
		final TermIdent name;
		final boolean asURL;

		private AttrFunction(TermIdent name, boolean asURL) {
			this.name = name;
			this.asURL = asURL;
		}

		/**
		 * @param fn assumed to not change
		 */
		private static AttrFunction of(TermFunction fn) {
			if (!"attr".equals(fn.getFunctionName()))
				return null;
			TermIdent name = null;
			boolean asURL = false;
			for (Term<?> arg : fn)
				if (name == null)
					if (arg instanceof TermIdent)
						name = (TermIdent)arg;
					else
						return null;
				else if (asURL == false)
					if (arg instanceof TermIdent)
						if ("url".equals(((TermIdent)arg).getValue()))
							asURL = true;
						else
							return null;
					else
						return null;
				else
					return null;
			if (name != null)
				return new AttrFunction(name, asURL);
			return null;
		}

		/**
		 * Evaluate <code>attr()</code> and <code>content()</code> values.
		 */
		private Term<String> evaluate(Element context) {
			Attr attr = context.getAttributeNode(name.getValue());
			if (attr == null)
				return null;
			if (asURL) {
				TermURI evaluated = new TermURIImpl(){}.setValue(attr.getValue());
				String base = context.getBaseURI();
				if (base != null && !"".equals(base))
					evaluated.setBase(URLs.asURL(base));
				return new UnmodifiableTermURI(evaluated);
			} else
				return new UnmodifiableTermString(new TermStringImpl(){}.setValue(attr.getValue()));
		}
	}

	public static class URL {

		// these fields need to be package private because they are used in BrailleCssSerializer
		final TermURI url;
		final AttrFunction urlAttr;

		private URL(TermURI url) {
			this.url = url;
			this.urlAttr = null;
		}

		private URL(AttrFunction urlAttr) {
			this.url = null;
			this.urlAttr = urlAttr;
		}

		/**
		 * @param t assumed to not change
		 */
		private static URL of(Term<?> t) {
			if (t instanceof TermURI)
				return new URL((TermURI)t);
			else if (t instanceof TermFunction) {
				AttrFunction a = AttrFunction.of((TermFunction)t);
				if (a != null) {
					if (!a.asURL)
						a = new AttrFunction(a.name, true);
					return new URL(a);
				} else
					return null; }
			else
				return null;
		}

		/**
		 * Evaluate <code>attr()</code> and <code>content()</code> values.
		 */
		private URL evaluate(Element context) {
			if (urlAttr != null) {
				Attr attr = context.getAttributeNode(urlAttr.name.getValue());
				if (attr == null)
					return null;
				TermURI evaluated = new TermURIImpl(){}.setValue(attr.getValue());
				String base = context.getBaseURI();
				if (base != null && !"".equals(base))
					evaluated.setBase(URLs.asURL(base));
				return new URL(evaluated);
			} else
				return this;
		}
	}

	/**
	 * This class is immutable
	 */
	public static class ContentFunction extends UnmodifiableTerm<ContentFunction> {

		// this field needs to be package private because it is used in BrailleCssSerializer
		final Optional<URL> target;

		private ContentFunction() {
			this.target = Optional.empty();
		}

		private ContentFunction(URL target) {
			this.target = Optional.of(target);
		}

		/**
		 * @param fn assumed to not change
		 */
		private static ContentFunction of(TermFunction fn) {
			boolean needTarget = false;
			if ("target-content".equals(fn.getFunctionName()))
				needTarget = true;
			else if (!"content".equals(fn.getFunctionName()))
				return null;
			URL target = null;
			for (Term<?> arg : fn)
				if (needTarget && target == null) {
					target = URL.of(arg);
					if (target == null)
						return null; }
				else
					return null;
			if (needTarget && target != null)
				return new ContentFunction(target);
			else if (!needTarget && target == null)
				return new ContentFunction();
			return null;
		}

		/**
		 * Evaluate <code>attr()</code> and <code>content()</code> values.
		 */
		private Term<?> evaluate(Element context) {
			if (target.isPresent()) {
				URL url = target.get();
				URL evaluatedURL = url.evaluate(context);
				if (evaluatedURL == null)
					return null;
				else if (evaluatedURL != url)
					return new ContentFunction(evaluatedURL);
			} else
				return new UnmodifiableTermString(new TermStringImpl(){}.setValue(context.getTextContent()));
			return this;
		}
	}

	/**
	 * This class is immutable
	 */
	public static class StringFunction extends UnmodifiableTerm<StringFunction> {

		// these fields need to be package private because they are used in BrailleCssSerializer
		final Optional<URL> target;
		final TermIdent name;
		final Optional<Scope> scope;

		public enum Scope {

			FIRST("first"),
			START("start"),
			LAST("last"),
			START_EXCEPT_FIRST("start-except-first"),
			START_EXCEPT_LAST("start-except-last"),
			LAST_EXCEPT_START("last-except-start"),
			PAGE_FIRST("page-first"),
			PAGE_START("page-start"),
			PAGE_LAST("page-last"),
			PAGE_START_EXCEPT_LAST("page-start-except-last"),
			PAGE_LAST_EXCEPT_START("page-last-except-start"),
			SPREAD_FIRST("spread-first"),
			SPREAD_START("spread-start"),
			SPREAD_LAST("spread-last"),
			SPREAD_START_EXCEPT_LAST("spread-start-except-last"),
			SPREAD_LAST_EXCEPT_START("spread-last-except-start");

			private final String ident;

			private static final Map<String,Scope> lookup = new HashMap<>(); static {
				for (Scope scope : EnumSet.allOf(Scope.class))
					lookup.put(scope.ident, scope); }

			private Scope(String ident) {
				this.ident = ident;
			}

			public static Scope of(TermIdent term) throws IllegalArgumentException {
				Scope scope = lookup.get(term.getValue());
				if (scope == null)
					throw new IllegalArgumentException("unexpected scope argument in string() function: " + term.getValue());
				return scope;
			}

			@Override
			public String toString() {
				return ident;
			}
		}

		private StringFunction(TermIdent name, Optional<TermIdent> scope) throws IllegalArgumentException {
			this.target = Optional.empty();
			this.name = name;
			this.scope = scope.map(Scope::of);
		}

		private StringFunction(URL target, TermIdent name) {
			this.target = Optional.of(target);
			this.name = name;
			this.scope = Optional.empty();
		}

		/**
		 * @param fn assumed to not change
		 */
		private static StringFunction of(TermFunction fn) {
			boolean needTarget = false;
			if ("target-string".equals(fn.getFunctionName()))
				needTarget = true;
			else if (!"string".equals(fn.getFunctionName()))
				return null;
			URL target = null;
			TermIdent name = null;
			TermIdent scope = null;
			for (Term<?> arg : fn)
				if (needTarget && target == null) {
					target = URL.of(arg);
					if (target == null)
						return null; }
				else if (name == null)
					if (arg instanceof TermIdent)
						name = (TermIdent)arg;
					else
						return null;
				else if (scope == null)
					if (arg instanceof TermIdent)
						scope = (TermIdent)arg;
					else
						return null;
				else
					return null;
			if (name != null)
				if (needTarget && target != null)
					if (scope != null)
						return null;
					else
						return new StringFunction(target, name);
				else if (!needTarget && target == null)
					try {
						return new StringFunction(name, Optional.ofNullable(scope));
					} catch (IllegalArgumentException e) {
						// invalid scope argument
					}
			return null;
		}

		/**
		 * Evaluate <code>attr()</code> and <code>content()</code> values.
		 */
		private StringFunction evaluate(Element context) {
			if (target.isPresent()) {
				URL url = target.get();
				URL evaluatedURL = url.evaluate(context);
				if (evaluatedURL == null)
					return null;
				else if (evaluatedURL != url)
					return new StringFunction(evaluatedURL, name);
			}
			return this;
		}
	}

	public static class CounterStyle {

		// these fields need to be package private because they are used in BrailleCssSerializer
		final String name;
		final TermString symbol;
		final TermFunction symbols;
		final org.daisy.pipeline.css.CounterStyle style;

		private CounterStyle(TermIdent name) {
			if ("none".equals(name.getValue()))
				this.name = null;
			else
				this.name = name.getValue();
			this.symbol = null;
			this.symbols = null;
			this.style = null;
		}

		private CounterStyle(TermString symbol) {
			this.name = null;
			this.symbol = symbol;
			this.symbols = null;
			this.style = org.daisy.pipeline.css.CounterStyle.constant(symbol.getValue());
		}

		private CounterStyle(TermFunction style) throws IllegalArgumentException {
			this.name = null;
			this.symbol = null;
			this.symbols = style;
			this.style = org.daisy.pipeline.css.CounterStyle.fromSymbolsFunction(style);
		}

		/**
		 * @param t assumed to not change
		 */
		private static CounterStyle of(Term<?> t) {
			if (t instanceof TermIdent)
				return new CounterStyle((TermIdent)t);
			else if (t instanceof TermString)
				return new CounterStyle((TermString)t);
			else if (t instanceof TermFunction)
				try {
					return new CounterStyle((TermFunction)t);
				} catch (IllegalArgumentException e) {
					// invalid symbols() function
				}
			return null;
		}
	}

	/**
	 * This class is immutable
	 */
	public static class CounterFunction extends UnmodifiableTerm<org.daisy.pipeline.braille.css.CounterFunction>
		                                implements org.daisy.pipeline.braille.css.CounterFunction {

		// these fields need to be package private because they are used in BrailleCssSerializer
		final Optional<URL> target;
		final TermIdent name;
		final Optional<CounterStyle> style;

		@Override
		public String getCounter() {
			return name.getValue();
		}

		@Override
		public Optional<org.daisy.pipeline.css.CounterStyle> getStyle() {
			return style.map(x -> x.style);
		}

		private CounterFunction(TermIdent name, Optional<CounterStyle> style) {
			this.target = Optional.empty();
			this.name = name;
			this.style = style;
		}

		private CounterFunction(URL target, TermIdent name, Optional<CounterStyle> style) {
			this.target = Optional.of(target);
			this.name = name;
			this.style = style;
		}

		/**
		 * @param fn assumed to not change
		 */
		private static CounterFunction of(TermFunction fn, Collection<BrailleCSSExtension> extensions) {
			boolean needTarget = false;
			if ("target-counter".equals(fn.getFunctionName()))
				needTarget = true;
			else if (!"counter".equals(fn.getFunctionName()))
				return null;
			URL target = null;
			TermIdent name = null;
			CounterStyle style = null;
			for (Term<?> arg : fn)
				if (needTarget && target == null) {
					target = URL.of(arg);
					if (target == null)
						return null; }
				else if (name == null) {
					if (extensions != null)
						if (arg instanceof TermIdent && ((TermIdent)arg).getValue().startsWith("-")) {
							// if the counter name starts with an extension prefix, let the extension parse it
							String n = ((TermIdent)arg).getValue();
							for (BrailleCSSExtension x : extensions)
								if (n.startsWith(x.getPrefix()))
									try {
										name = x.parseCounterName(arg);
										break;
									} catch (IllegalArgumentException e) {
										return null;
									}
						} else 
							// other, give extensions the chance to normalize names
							for (BrailleCSSExtension x : extensions)
								try {
									name = x.parseCounterName(arg);
									break;
								} catch (IllegalArgumentException e) {
									// continue
								}
					if (name == null && arg instanceof TermIdent)
						name = (TermIdent)arg;
					if (name == null)
						return null;
				} else if (style == null) {
					style = CounterStyle.of(arg);
					if (style == null)
						return null; }
				else
					return null;
			if (name != null)
				if (needTarget && target != null)
					return new CounterFunction(target, name, Optional.ofNullable(style));
				else if (!needTarget && target == null)
					return new CounterFunction(name, Optional.ofNullable(style));
			return null;
		}

		/**
		 * Evaluate <code>attr()</code> and <code>content()</code> values.
		 */
		private CounterFunction evaluate(Element context) {
			if (target.isPresent()) {
				URL url = target.get();
				URL evaluatedURL = url.evaluate(context);
				if (evaluatedURL == null)
					return null;
				else if (evaluatedURL != url)
					return new CounterFunction(evaluatedURL, name, style);
			}
			return this;
		}
	}

	/**
	 * This class is immutable
	 */
	public static class TextFunction extends UnmodifiableTerm<TextFunction> {

		// this field needs to be package private because it is used in BrailleCssSerializer
		final URL target;

		private TextFunction(URL target) {
			this.target = target;
		}

		/**
		 * @param fn assumed to not change
		 */
		private static TextFunction of(TermFunction fn) {
			if (!"target-text".equals(fn.getFunctionName()))
				return null;
			URL target = null;
			for (Term<?> arg : fn)
				if (target == null) {
					target = URL.of(arg);
					if (target == null)
						return null; }
				else
					return null;
			if (target != null)
				return new TextFunction(target);
			return null;
		}

		/**
		 * Evaluate <code>attr()</code> and <code>content()</code> values.
		 */
		private TextFunction evaluate(Element context) {
			URL evaluatedTarget = target.evaluate(context);
			if (evaluatedTarget == null)
				return null;
			else if (evaluatedTarget != target)
				return new TextFunction(evaluatedTarget);
			return this;
		}
	}

	/**
	 * This class is immutable
	 */
	public static class LeaderFunction extends UnmodifiableTerm<LeaderFunction> {

		// these fields need to be package private because they are used in BrailleCssSerializer
		final TermString pattern;
		final Optional<TermLengthOrPercent> position;
		final Optional<Alignment> alignment;

		public enum Alignment {

			LEFT("left"),
			CENTER("center"),
			RIGHT("right");

			private final String ident;

			private static final Map<String,Alignment> lookup = new HashMap<>(); static {
				for (Alignment alignment : EnumSet.allOf(Alignment.class))
					lookup.put(alignment.ident, alignment); }

			private Alignment(String ident) {
				this.ident = ident;
			}

			public static Alignment of(TermIdent term) throws IllegalArgumentException {
				Alignment alignment = lookup.get(term.getValue());
				if (alignment == null)
					throw new IllegalArgumentException("unexpected alignment argument in leader() function: " + term.getValue());
				return alignment;
			}

			@Override
			public String toString() {
				return ident;
			}
		}

		private LeaderFunction(TermString pattern, Optional<TermLengthOrPercent> position, Optional<TermIdent> alignment)
				throws IllegalArgumentException {
			this.pattern = pattern;
			this.position = position;
			this.alignment = alignment.map(Alignment::of);
		}

		/**
		 * @param fn assumed to not change
		 */
		private static LeaderFunction of(TermFunction fn) {
			if (!"leader".equals(fn.getFunctionName()))
				return null;
			TermString pattern = null;
			TermLengthOrPercent position = null;
			TermIdent alignment = null;
			for (Term<?> arg : fn)
				if (pattern == null)
					if (arg instanceof TermString) {
						pattern = (TermString)arg;
						if (!pattern.getValue().matches("[\\u2800-\\u28ff]*")) // \\p{IsBraillePatterns}*
							return null; }
					else
						return null;
				else if (position == null)
					if (arg instanceof TermInteger || arg instanceof TermPercent)
						position = (TermLengthOrPercent)arg;
					else
						return null;
				else if (alignment == null)
					if (arg instanceof TermIdent)
						alignment = (TermIdent)arg;
					else
						return null;
				else
					return null;
			if (pattern != null)
				try {
					return new LeaderFunction(pattern, Optional.ofNullable(position), Optional.ofNullable(alignment));
				} catch (IllegalArgumentException e) {
					// invalid alignment argument
				}
			return null;
		}
	}

	/**
	 * This class is immutable
	 */
	public static class FlowFunction extends UnmodifiableTerm<FlowFunction> {

		// these fields need to be package private because they are used in BrailleCssSerializer
		final TermIdent from;
		final Optional<Scope> scope;

		public enum Scope {

			DOCUMENT("document"),
			VOLUME("volume"),
			PAGE("page");

			private final String ident;

			private static final Map<String,Scope> lookup = new HashMap<>(); static {
				for (Scope scope : EnumSet.allOf(Scope.class))
					lookup.put(scope.ident, scope); }

			private Scope(String ident) {
				this.ident = ident;
			}

			public static Scope of(TermIdent term) throws IllegalArgumentException {
				Scope scope = lookup.get(term.getValue());
				if (scope == null)
					throw new IllegalArgumentException("unexpected scope argument in string() function: " + term.getValue());
				return scope;
			}

			@Override
			public String toString() {
				return ident;
			}
		}

		private FlowFunction(TermIdent from, Optional<TermIdent> scope) throws IllegalArgumentException {
			this.from = from;
			this.scope = scope.map(Scope::of);
		}

		/**
		 * @param fn assumed to not change
		 */
		private static FlowFunction of(TermFunction fn) {
			if (!"flow".equals(fn.getFunctionName()))
				return null;
			TermIdent from = null;
			TermIdent scope = null;
			for (Term<?> arg : fn)
				if (from == null)
					if (arg instanceof TermIdent)
						from = (TermIdent)arg;
					else
						return null;
				else if (scope == null)
					if (arg instanceof TermIdent)
						scope = (TermIdent)arg;
					else
						return null;
				else
					return null;
			if (from != null)
				try {
					return new FlowFunction(from, Optional.ofNullable(scope));
				} catch (IllegalArgumentException e) {
					// invalid scope argument
				}
			return null;
		}
	}
}
