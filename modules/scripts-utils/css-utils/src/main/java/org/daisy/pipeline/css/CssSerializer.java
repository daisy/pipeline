package org.daisy.pipeline.css;

import java.net.URI;
import java.util.List;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermFunction;
import cz.vutbr.web.css.TermInteger;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermNumber;
import cz.vutbr.web.css.TermPair;
import cz.vutbr.web.css.TermPercent;
import cz.vutbr.web.css.TermString;
import cz.vutbr.web.css.TermURI;

import org.daisy.common.file.URLs;

public final class CssSerializer {

	private CssSerializer() {}

	public static String toString(Term<?> term) {
		if (term instanceof TermInteger) {
			TermInteger integer = (TermInteger)term;
			return "" + integer.getIntValue(); }
		else if (term instanceof TermNumber) {
			TermNumber number = (TermNumber)term;
			Double value = number.getValue().doubleValue();
			if (value == Math.floor(value))
				return "" + value.intValue();
			else
				return "" + value; }
		else if (term instanceof TermPercent) {
			TermPercent percent = (TermPercent)term;
			Double value = percent.getValue().doubleValue();
			if (value == Math.floor(value))
				return "" + value.intValue() + "%";
			else
				return "" + value + "%"; }
		else if (term instanceof TermList
		         || term instanceof Declaration) {
			String s = serializeTermList((List<Term<?>>)term);
			if (term instanceof TermFunction) {
				TermFunction function = (TermFunction)term;
				s = function.getFunctionName() + "(" + s + ")"; }
			return s; }
		else if (term instanceof TermPair) {
			TermPair<?,?> pair = (TermPair<?,?>)term;
			Object val = pair.getValue();
			return "" + pair.getKey() + " " + (val instanceof Term ? toString((Term<?>)val) : val.toString()); }
		else if (term instanceof TermURI) {
			TermURI termURI = (TermURI)term;
			URI uri = URLs.asURI(termURI.getValue());
			if (termURI.getBase() != null)
				uri = URLs.resolve(URLs.asURI(termURI.getBase()), uri);
			return "url(\"" + uri + "\")"; }
		else if (term instanceof TermString) {
			TermString string = (TermString)term;
			return "'" + string.getValue().replaceAll("\n", "\\\\A ").replaceAll("'", "\\\\27 ") + "'"; }
		else
			return term.toString().replaceAll("^[,/ ]+", "");
	}

	public static String serializeTermList(List<Term<?>> termList) {
		String s = "";
		for (Term<?> t : termList) {
			if (!s.isEmpty()) {
				Term.Operator o = t.getOperator();
				if (o != null)
					switch (o) {
					case COMMA:
						s += ","; }
				s += " "; }
			s += toString(t); }
		return s;
	}
}
