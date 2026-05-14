package org.daisy.pipeline.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermString;

// we use some classes from braille-css because they are not available in jStyleParser (but they are
// not specific to braille CSS)
import org.daisy.braille.css.InlineStyle;
import org.daisy.braille.css.RuleCounterStyle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.unbescape.css.CssEscape;

/**
 * See http://www.w3.org/TR/css-counter-styles-3
 */
public interface CounterStyle {

	public String format(int counterValue);

	public String format(int counterValue, boolean withPrefixAndSuffix);

	@Deprecated
	public String getTextTransform(int counterValue);

	// numeric
	public final static CounterStyle DECIMAL = CounterStyleImpl.predefinedCounterStyles.get("decimal");
	// alphabetic
	public final static CounterStyle LOWER_ALPHA = CounterStyleImpl.predefinedCounterStyles.get("lower-alpha");
	public final static CounterStyle UPPER_ALPHA = CounterStyleImpl.predefinedCounterStyles.get("upper-alpha");
	public final static CounterStyle LOWER_ROMAN = CounterStyleImpl.predefinedCounterStyles.get("lower-roman");
	public final static CounterStyle UPPER_ROMAN = CounterStyleImpl.predefinedCounterStyles.get("upper-roman");
	// cyclic
	public final static CounterStyle DISC = CounterStyleImpl.predefinedCounterStyles.get("disc");

	/**
	 * Create a CounterStyle from a <code>symbols()</code> function
	 */
	public static CounterStyle fromSymbolsFunction(Term<?> term) throws IllegalArgumentException {
		return CounterStyleImpl.fromSymbolsFunction(term);
	}

	/**
	 * Create a map of named CounterStyle from a set of <code>@counter-style</code> rules.
	 */
	public static Map<String,CounterStyle> parseCounterStyleRules(Iterable<RuleCounterStyle> style) {
		return (Map<String,CounterStyle>)(Map)CounterStyleImpl.parseCounterStyleRules(style);
	}

	/**
	 * Return the predefined counter style with the given name, or {@code null} if no counter style
	 * with that name exists.
	 */
	public static CounterStyle predefined(String name) {
		return CounterStyleImpl.predefinedCounterStyles.get(name);
	}

	/**
	 * Create a constant CounterStyle (cyclic with a single symbol)
	 */
	public static CounterStyle constant(String symbol) {
		return new CounterStyleImpl(symbol);
	}

	/**
	 * Create a cyclic CounterStyle
	 */
	public static CounterStyle cycle(Iterable<String> symbols) {
		return new CounterStyleImpl(ImmutableList.copyOf(symbols));
	}
}

class CounterStyleImpl implements CounterStyle {

	private enum System {
		ALPHABETIC,
		NUMERIC,
		CYCLIC,
		FIXED,
		SYMBOLIC,
		ADDITIVE
	};

	// package private for tests
	static class AdditiveTuple {
		final int weight;
		final String symbol;
		AdditiveTuple(int weight, String symbol) {
			this.weight = weight;
			this.symbol = symbol;
		}
	}

	private final static Logger logger = LoggerFactory.getLogger(CounterStyle.class);

	final static Map<String,CounterStyleImpl> predefinedCounterStyles = parseCounterStyleRules(
		Iterables.filter(
			new InlineStyle(
				"@counter-style decimal {"                                    + "\n" +
				"   system: numeric;"                                         + "\n" +
				"   symbols: '0' '1' '2' '3' '4' '5' '6' '7' '8' '9';"        + "\n" +
				"   negative: '-';"                                           + "\n" +
				"}"                                                           + "\n" +
				"@counter-style lower-alpha {"                                + "\n" +
				"   system: alphabetic;"                                      + "\n" +
				"   symbols: 'a' 'b' 'c' 'd' 'e' 'f' 'g' 'h' 'i' 'j'"         + "\n" +
				"            'k' 'l' 'm' 'n' 'o' 'p' 'q' 'r' 's' 't'"         + "\n" +
				"            'u' 'v' 'w' 'x' 'y' 'z';"                        + "\n" +
				"}"                                                           + "\n" +
				"@counter-style upper-alpha {"                                + "\n" +
				"   system: alphabetic;"                                      + "\n" +
				"   symbols: 'A' 'B' 'C' 'D' 'E' 'F' 'G' 'H' 'I' 'J'"         + "\n" +
				"            'K' 'L' 'M' 'N' 'O' 'P' 'Q' 'R' 'S' 'T'"         + "\n" +
				"            'U' 'V' 'W' 'X' 'Y' 'Z';"                        + "\n" +
				"}"                                                           + "\n" +
				"@counter-style lower-roman {"                                + "\n" +
				"   system: additive;"                                        + "\n" +
				"   range: 1 3999;"                                           + "\n" +
				"   additive-symbols: 1000 'm', 900 'cm', 500 'd', 400 'cd'," + "\n" +
				"                     100 'c', 90 'xc', 50 'l', 40 'xl',"     + "\n" +
				"                     10 'x', 9 'ix', 5 'v', 4 'iv', 1 'i';"  + "\n" +
				"}"                                                           + "\n" +
				"@counter-style upper-roman {"                                + "\n" +
				"   system: additive;"                                        + "\n" +
				"   range: 1 3999;"                                           + "\n" +
				"   additive-symbols: 1000 'M', 900 'CM', 500 'D', 400 'CD'," + "\n" +
				"                     100 'C', 90 'XC', 50 'L', 40 'XL',"     + "\n" +
				"                     10 'X', 9 'IX', 5 'V', 4 'IV', 1 'I';"  + "\n" +
				"}"                                                           + "\n" +
				"@counter-style disc {"                                       + "\n" +
				"   system: cyclic;"                                          + "\n" +
				"   symbols: '\\2022';"                                       + "\n" +
				"   suffix: ' ';"                                             + "\n" +
				"}"
			), RuleCounterStyle.class));

	private final System system;
	private final List<String> symbols;
	private final List<AdditiveTuple> additiveSymbols;
	private final String negative;
	private final Supplier<CounterStyleImpl> fallback;
	private final String textTransform; // for braille CSS
	private final String prefix;
	private final String suffix;

	/**
	 * Create a CounterStyle from a <code>symbols()</code> function
	 *
	 * @param term assumed to be a "symbols" function
	 */
    CounterStyleImpl(TermFunction term) throws IllegalArgumentException {
		System system;
		List<Term<?>> symbols;
		if (term.size() > 0 && term.get(0) instanceof TermIdent) {
			system = readSystem((TermIdent)term.get(0));
			if (system == System.ADDITIVE)
				throw new IllegalArgumentException("system 'additive' not supported in symbols() function");
			symbols = term.subList(1, term.size());
		} else {
			system = System.SYMBOLIC;
			symbols = term;
		}
		this.symbols = readSymbols(symbols);
		this.additiveSymbols = null;
		this.system = system;
		this.negative = "-";
		this.fallback = () -> (CounterStyleImpl)DECIMAL;
		this.textTransform = "auto";
		this.prefix = "";
		this.suffix = " ";
	}

	/**
	 * Create a CounterStyle from a <code>@counter-style</code> rule
	 */
	// FIXME: the "range" descriptor is not taken into account
    CounterStyleImpl(RuleCounterStyle rule, Map<String,CounterStyleImpl> fallbacks) throws IllegalArgumentException {
		Declaration systemDecl = null;
		Declaration symbolsDecl = null;
		Declaration additiveSymbolsDecl = null;
		Declaration negativeDecl = null;
		Declaration fallbackDecl = null;
		Declaration prefixDecl = null;
		Declaration suffixDecl = null;
		Declaration textTransformDecl = null;
		for (Declaration d : rule) {
			String prop = d.getProperty();
			if ("system".equals(prop)) {
				if (systemDecl == null) systemDecl = d;
			} else if ("symbols".equals(prop)) {
				if (symbolsDecl == null) symbolsDecl = d;
			} else if ("additive-symbols".equals(prop)) {
				if (additiveSymbolsDecl == null) additiveSymbolsDecl = d;
			} else if ("negative".equals(prop)) {
				if (negativeDecl == null) negativeDecl = d;
			} else if ("fallback".equals(prop)) {
				if (fallbackDecl == null) fallbackDecl = d;
			} else if ("prefix".equals(prop)) {
				if (prefixDecl == null) prefixDecl = d;
			} else if ("suffix".equals(prop)) {
				if (suffixDecl == null) suffixDecl = d;
			} else if ("text-transform".equals(prop)) {
				if (textTransformDecl == null) textTransformDecl = d;
			}
		}
		System system; {
			system = null;
			if (systemDecl != null)
				try {
					system = readSystem(systemDecl);
				} catch (IllegalArgumentException e) {
					logger.warn(e.getMessage());
				}
			if (system == null) system = System.SYMBOLIC;
		}
		List<String> symbols; {
			if (system != System.ADDITIVE) {
				if (symbolsDecl != null)
					symbols = readSymbols(symbolsDecl);
				else
					throw new IllegalArgumentException("Missing symbols descriptor");
			} else
				symbols = null;
		}
		List<AdditiveTuple> additiveSymbols; {
			if (system == System.ADDITIVE) {
				if (additiveSymbolsDecl != null)
					additiveSymbols = readAdditiveSymbols(additiveSymbolsDecl);
				else
					throw new IllegalArgumentException("Missing symbols descriptor");
			} else
				additiveSymbols = null;
		}
		String negative; {
			negative = null;
			if (negativeDecl != null)
				try {
					negative = readSingleString("negative", negativeDecl).getValue();
				} catch (IllegalArgumentException e) {
					logger.warn(e.getMessage());
				}
			if (negative == null)
				negative = "-";
		}
		String fallback; {
			String ident = null;
			if (fallbackDecl != null)
				try {
					ident = readSingleIdent("fallback", fallbackDecl).getValue();
				} catch (IllegalArgumentException e) {
					logger.warn(e.getMessage());
				}
			if (ident == null)
				ident = "decimal";
			fallback = ident;
		}
		String textTransform; {
			textTransform = null;
			if (textTransformDecl != null) {
				logger.warn("'text-transform' descriptor in @counter-style rule is deprecated. "
				            + "Use a 'text-transform' property within the rule containing the 'content' property instead.");
				try {
					textTransform = readSingleIdent("text-transform", textTransformDecl).getValue();
				} catch (IllegalArgumentException e) {
					logger.warn(e.getMessage());
				}
			}
			if (textTransform == null)
				textTransform = "auto";
		}
		String prefix; {
			prefix = null;
			if (prefixDecl != null)
				try {
					prefix = readSingleString("prefix", prefixDecl).getValue();
				} catch (IllegalArgumentException e) {
					logger.warn(e.getMessage());
				}
			if (prefix == null)
				prefix = "";
		}
		String suffix; {
			suffix = null;
			if (suffixDecl != null)
				try {
					suffix = readSingleString("suffix", suffixDecl).getValue();
				} catch (IllegalArgumentException e) {
					logger.warn(e.getMessage());
				}
			if (suffix == null)
				suffix = ". ";
		}
		this.system = system;
		this.symbols = symbols;
		this.additiveSymbols = additiveSymbols;
		this.negative = negative;
		this.fallback = memoize(
			() -> {
				if (fallbacks != null && fallbacks.containsKey(fallback))
					return fallbacks.get(fallback);
				else if (predefinedCounterStyles.containsKey(fallback))
					return predefinedCounterStyles.get(fallback);
				else
					return (CounterStyleImpl)DECIMAL;
			}
		);
		this.textTransform = textTransform;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	/**
	 * Create a constant CounterStyle (cyclic with a single symbol)
	 */
    CounterStyleImpl(String symbol) {
		this(Collections.singletonList(symbol));
	}

	/**
	 * Create a cyclic CounterStyle
	 */
    CounterStyleImpl(List<String> symbols) {
		system = System.CYCLIC;
		this.symbols = symbols;
		textTransform = "none";
		prefix = "";
		suffix = " ";
		additiveSymbols = null;
		negative = null;
		fallback = null;
	}

	static final Map<String,CounterStyle> fromSymbolsCache = new HashMap<>();

	static CounterStyle fromSymbolsFunction(Term<?> term) throws IllegalArgumentException {
		if (term instanceof TermFunction) {
			TermFunction function = (TermFunction)term;
			if (function.getFunctionName().equals("symbols")) {
				String k = function.toString();
				CounterStyle s = fromSymbolsCache.get(k);
				if (s == null) {
					s = new CounterStyleImpl(function);
					fromSymbolsCache.put(k, s);
				}
				return s;
			}
		}
		throw new IllegalArgumentException("argument must be a symbols() function");
	}

	static Map<String,CounterStyleImpl> parseCounterStyleRules(Iterable<RuleCounterStyle> style) {
		Map<String,CounterStyleImpl> namedStyles = new HashMap<>();
		for (RuleCounterStyle rule : style)
			try {
				namedStyles.put(rule.getName(), new CounterStyleImpl(rule, namedStyles));
			} catch (IllegalArgumentException e) {
				logger.warn(e.getMessage());
			}
		return namedStyles;
	}

	@Override
	public String format(int counterValue) {
		Object o = formatOrFallback(counterValue);
		if (o instanceof String)
			return (String)o;
		CounterStyleImpl fallback = (CounterStyleImpl)o;
		return fallback.format(counterValue);
	}

	private Object formatOrFallback(int counterValue) {
		switch (system) {
		case ALPHABETIC: {
			Optional<String> formatted = counterRepresentationAlphabetic(counterValue, symbols);
			if (formatted.isPresent())
				return formatted.get();
			break; }
		case NUMERIC:
			return counterRepresentationNumeric(counterValue, symbols, negative);
		case CYCLIC:
			return counterRepresentationCyclic(counterValue, symbols);
		case FIXED: {
			Optional<String> formatted = counterRepresentationFixed(counterValue, symbols);
			if (formatted.isPresent())
				return formatted.get();
			break; }
		case SYMBOLIC: {
			Optional<String> formatted = counterRepresentationSymbolic(counterValue, symbols);
			if (formatted.isPresent())
				return formatted.get();
			break; }
		case ADDITIVE: {
			Optional<String> formatted = counterRepresentationAdditive(counterValue, additiveSymbols);
			if (formatted.isPresent())
				return formatted.get();
			break; }
		default:
			throw new IllegalStateException(); // can not happen
		}
		if (fallback.get() == null || fallback.get() == this)
			throw new IllegalStateException(); // can not happen
		return fallback.get();
	}

	@Override
	public String format(int counterValue, boolean withPrefixAndSuffix) {
		// prefix and suffix always come from the specified counter-style, even if the actual
		// representation is constructed by a fallback style
		if (withPrefixAndSuffix)
			return prefix + format(counterValue) + suffix;
		else
			return format(counterValue);
	}

	@Override
	public String getTextTransform(int counterValue) {
		Object o = formatOrFallback(counterValue);
		if (o instanceof String)
			return this.textTransform;;
		CounterStyleImpl fallback = (CounterStyleImpl)o;
		return fallback.textTransform;
	}

	/* ============ private ========================================================= */

	private static System readSystem(List<Term<?>> terms) throws IllegalArgumentException {
		return readSystem(readSingleIdent("system", terms));
	}

	private static System readSystem(TermIdent ident) throws IllegalArgumentException {
		String system = ident.getValue();
		try {
			return System.valueOf(system.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid system: " + system);
		}
	}

	private static List<String> readSymbols(List<Term<?>> terms) throws IllegalArgumentException {
		List<String> symbols = new ArrayList<>();
		for (Term<?> t : terms)
			symbols.add(readSymbol(t));
		if (symbols.isEmpty())
			throw new IllegalArgumentException("Empty symbols list");
		return symbols;
	}

	private static List<AdditiveTuple> readAdditiveSymbols(List<Term<?>> terms) throws IllegalArgumentException {
		List<AdditiveTuple> symbols = new ArrayList<>();
		Iterator<Term<?>> tt = terms.iterator();
		int prevWeight = -1;
		while (tt.hasNext()) {
			Term<?> t = tt.next();
			if (t instanceof TermInteger) {
				int weight = ((TermInteger)t).getIntValue();
				if (prevWeight >= 0 && weight > prevWeight)
					throw new IllegalArgumentException(
						"Invalid additive tuple: expected integer weight greater than " + prevWeight + " but got " + t);
				prevWeight = weight;
				if (tt.hasNext()) {
					t = tt.next();
					try {
						symbols.add(new AdditiveTuple(weight, readSymbol(t)));
					} catch (IllegalArgumentException e) {
						throw new IllegalArgumentException("Invalid additive tuple: expected symbol but got " + t, e);
					}
				} else
					throw new IllegalArgumentException("Invalid additive tuple: expected symbol");
			} else
				throw new IllegalArgumentException("Invalid additive tuple: expected integer weight but got " + t);
		}
		if (symbols.isEmpty())
			throw new IllegalArgumentException("Empty additive-symbols list");
		return symbols;
	}

	private static String readSymbol(Term<?> term) {
		if (term instanceof TermString)
			return ((TermString)term).getValue();
		else if (term instanceof TermIdent)
			return CssEscape.unescapeCss(((TermIdent)term).getValue());
		else if (term instanceof TermInteger)
			return "" + ((TermInteger)term).getValue();
		else
			throw new IllegalArgumentException("Invalid symbol: " + term);
	}

	private static TermIdent readSingleIdent(String property, List<Term<?>> terms) throws IllegalArgumentException {
		if (terms.size() != 1 || !(terms.get(0) instanceof TermIdent))
			throw new IllegalArgumentException("Invalid " + property + ": " + CssSerializer.getInstance().serializeTermList(terms));
		return (TermIdent)terms.get(0);
	}

	private static TermString readSingleString(String property, List<Term<?>> terms) throws IllegalArgumentException {
		if (terms.size() != 1 || !(terms.get(0) instanceof TermString))
			throw new IllegalArgumentException("Invalid " + property + ": " + CssSerializer.getInstance().serializeTermList(terms));
		return (TermString)terms.get(0);
	}

	private static int mod(int a, int n) {
		int result = a % n;
		if (result < 0)
			result += n;
		return result;
	}

	private static <T> Supplier<T> memoize(final Supplier<T> supplier) {
		return new Memoize<T>() {
			protected T _get() {
				return supplier.get();
			}
			@Override
			public String toString() {
				return "memoize( " + supplier + " )";
			}
		};
	}

	private static abstract class Memoize<T> implements Supplier<T> {
		private boolean computed = false;
		private T value = null;
		protected abstract T _get();
		public final T get() {
			if (!computed) {
				value = _get();
				computed = true;
			}
			return value;
		}
	}

	/* ============ package private for tests ======================================= */

	static Optional<String> counterRepresentationAlphabetic(int counterValue, List<String> symbols) {
		if (counterValue < 1)
			return Optional.empty();
		if (counterValue > symbols.size())
			return Optional.of(
				counterRepresentationAlphabetic((counterValue - 1) / symbols.size(), symbols).get()
					+ symbols.get(mod(counterValue - 1, symbols.size())));
		else
			return Optional.of(symbols.get(counterValue - 1));
	}

	static String counterRepresentationCyclic(int counterValue, List<String> symbols) {
		return symbols.get(mod(counterValue - 1, symbols.size()));
	}

	static Optional<String> counterRepresentationFixed(int counterValue, List<String> symbols) {
		if (counterValue < 1 || counterValue > symbols.size())
			return Optional.empty();
		else
			return Optional.of(symbols.get(counterValue - 1));
	}

	static String counterRepresentationNumeric(int counterValue, List<String> symbols, String negative) {
		if (counterValue < 0)
			return negative + counterRepresentationNumeric(- counterValue, symbols, negative);
		if (counterValue >= symbols.size())
			return counterRepresentationNumeric(counterValue / symbols.size(), symbols, negative)
				+ symbols.get(mod(counterValue, symbols.size()));
		else
			return symbols.get(counterValue);
	}

	static Optional<String> counterRepresentationSymbolic(int counterValue, List<String> symbols) {
		if (counterValue < 1)
			return Optional.empty();
		String symbol = symbols.get(mod(counterValue - 1, symbols.size()));
		String s = symbol;
		for (int i = 0; i < ((counterValue - 1) / symbols.size()); i++)
			s += symbol;
		return Optional.of(s);
	}

	static Optional<String> counterRepresentationAdditive(int counterValue, List<AdditiveTuple> symbols) {
		if (counterValue < 0)
			return Optional.empty();
		int rest = counterValue;
		String s = "";
		for (AdditiveTuple tuple : symbols) {
			if (tuple.weight == 0) {
				if (rest == 0 && s.isEmpty())
					s += tuple.symbol;
			} else {
				for (int i = 0; i < rest / tuple.weight; i++)
					s += tuple.symbol;
				rest = mod(rest, tuple.weight);
				if (rest == 0 && !s.isEmpty())
					break;
			}
		}
		if (rest != 0 || s.isEmpty())
			return Optional.empty();
		return Optional.of(s);
	}
}
