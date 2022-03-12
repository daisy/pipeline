package org.daisy.pipeline.braille.css;

import java.util.ArrayList;
import java.util.List;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermIdent;
import cz.vutbr.web.css.TermString;

public class CounterStyle {

	private String system;
	private List<String> symbols;

	/**
	 * Create a counter-style from a symbols() function
	 */
	public CounterStyle(Term<?> term) throws IllegalArgumentException {
		if (term instanceof TermFunction) {
			TermFunction function = (TermFunction)term;
			if (function.getFunctionName().equals("symbols")) {
				symbols = new ArrayList<String>();
				for (Term<?> t : function) {
					if (t instanceof TermIdent) {
						if (system != null || !symbols.isEmpty())
							throw new IllegalArgumentException();
						system = ((TermIdent)t).getValue();
						if (!(system.equals("alphabetic") ||
							  system.equals("numeric") ||
							  system.equals("cyclic") ||
							  system.equals("fixed") ||
							  system.equals("symbolic")))
							throw new IllegalArgumentException("Unsupported system: " + system);
					} else if (t instanceof TermString) {
						symbols.add(((TermString)t).getValue());
					} else {
						throw new IllegalArgumentException();
					}
				}
				if (system == null) system = "symbolic";
				return;
			}
		}
		throw new IllegalArgumentException("argument must be a symbols() function");
	}

	public String format(int counterValue) {
		if (system.equals("alphabetic"))
			return counterRepresentationAlphabetic(counterValue, symbols);
		else if (system.equals("numeric"))
			return counterRepresentationNumeric(counterValue, symbols);
		else if (system.equals("cyclic"))
			return counterRepresentationCyclic(counterValue, symbols);
		else if (system.equals("fixed"))
			return counterRepresentationFixed(counterValue, symbols);
		else // symbolic
			return counterRepresentationSymbolic(counterValue, symbols);
	}

	/* package private for tests */

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

	private static int mod(int a, int n) {
		int result = a % n;
		if (result < 0)
			result += n;
		return result;
	}
}
