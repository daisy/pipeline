package org.daisy.dotify.formatter.impl.obfl;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.daisy.dotify.api.formatter.NumeralStyle;
import org.daisy.dotify.api.obfl.Expression;
import org.daisy.dotify.api.text.Integer2Text;
import org.daisy.dotify.api.text.Integer2TextConfigurationException;
import org.daisy.dotify.api.text.Integer2TextFactoryMakerService;
import org.daisy.dotify.api.text.IntegerOutOfRange;

/**
 * <p>
 * Expression is a small expressions language interpreter. The language uses
 * prefix notation with arguments separated by whitespace. The entire expression
 * must be surrounded with parentheses.
 * </p>
 * <p>
 * The following operators are defined: +, -, *, /, %, =, &lt;, &lt;=, &gt;, &gt;=,
 * &amp;, |
 * </p>
 * <p>
 * All operators require at least two arguments. E.g. (+ 5 7 9) evaluates to 21.
 * </p>
 * <p>
 * Special keywords:
 * </p>
 * <ul>
 * <li>if: (if (boolean_expression) value_when_true value_when_false)</li>
 * <li>now: (now date_format) where date_format is as defined by
 * {@link SimpleDateFormat}</li>
 * <li>round: (round value)</li>
 * <li>set: (set key value) where key is the key that will be replaced by value
 * in any subsequent expressions (within the same evaluation).</li>
 * <li>int2text: (int2text number language-code) where number is an integer
 * number to be converted into text using the language specified by
 * language-code.</li>
 * <li>concat: (concat ...) all arguments are concatenated to a single string</li>
 * <li>format: (format string ...) the first argument is the format string as defined by MessageFormat, 
 * following arguments are parameters to insert in the format string.</li>
 * </ul>
 * <p>
 * Quotes must surround arguments containing whitespace.
 * </p>
 * 
 * @author Joel Håkansson
 */
class ExpressionImpl implements Expression {
	private static final Logger logger = Logger.getLogger(ExpressionImpl.class.getCanonicalName());
	private static final Map<String, Instant> CONFIGURATION_WARNING_ISSUED = Collections.synchronizedMap(new HashMap<>());
	private HashMap<String, Object> localVars;
	private Map<String, Object> globalVars;
	private final Integer2TextFactoryMakerService integer2textFactoryMaker;

	public ExpressionImpl(Integer2TextFactoryMakerService integer2textFactoryMaker) {
		// = Integer2TextFactoryMaker.newInstance();
		this.integer2textFactoryMaker = integer2textFactoryMaker;
		this.globalVars = new HashMap<>();
	}
	
	@Override
	public Object evaluate(String expr) {
		localVars = new HashMap<>(globalVars);
		// return value
		String[] exprs = getArgs(expr);
		for (int i=0; i<exprs.length-1; i++) {
			doEvaluate(exprs[i]);
		}
		return toReturnType(doEvaluate(exprs[exprs.length-1]));
	}
	
	private Object toReturnType(Object ret) {
		if (ret instanceof Double) {
			Double d = (Double)ret;
			if (d.intValue()==d) {
				return d.intValue();
			}
		} 
		return ret;
	}
	
	@Override
	public void setVariable(String key, Object value) {
		globalVars.put("$"+key, value);
	}
	
	@Override
	public void removeVariable(String key) {
		globalVars.remove("$"+key);
	}

	@Override
	public void removeAllVariables() {
		globalVars.clear();
	}

	private static final Pattern IDENT = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9-]*");

	private Object doEval1(String expr) {
		if (expr.startsWith("\"") && expr.endsWith("\"")) {
			return expr.substring(1, expr.length()-1);
		}
		if (localVars.containsKey(expr)) {
			return localVars.get(expr);
		}
		if ("true".equals(expr)) {
			return Boolean.TRUE;
		}
		if ("false".equals(expr)) {
			return Boolean.FALSE;
		}
		if (IDENT.matcher(expr).matches()) {
			return expr;
		}
		try {
			return toNumber(expr);
		} catch (NumberFormatException e) {
		}
		throw new IllegalArgumentException("Can not evaluate: " + expr);
	}
	
	private Object doEval2(String[] args1) {
		String operator = args1[0].trim();
		Object[] args = new Object[args1.length-1];
		for (int i=0; i<args.length; i++) {
			args[i] = doEvaluate(args1[i+1]);
		}
		//System.arraycopy(args1, 1, args, 0, args1.length-1);
		if ("+".equals(operator)) {
			return add(args);
		} else if ("-".equals(operator)) {
			return subtract(args);
		} else if ("*".equals(operator)) {
			return multiply(args);
		} else if ("/".equals(operator)) {
			return divide(args);
		} else if ("%".equals(operator)) {
			return modulo(args);
		} else if ("=".equals(operator)) {
			return equalsOp(args);
		} else if ("<".equals(operator)) {
			return smallerThan(args);
		}  else if ("<=".equals(operator)) {
			return smallerThanOrEqualTo(args);
		} else if (">".equals(operator)) {
			return greaterThan(args);
		} else if (">=".equals(operator)) {
			return greaterThanOrEqualTo(args);
		} else if ("&".equals(operator)) {
			return and(args);
		} else if ("|".equals(operator)) {
			return or(args);
		} else if ("if".equals(operator)) {
			return ifOp(args);
		} else if ("now".equals(operator)) {
			return now(args);
		} else if ("round".equals(operator)) {
			return round(args);
		} else if ("set".equals(operator)) {
			return set(args);
		} else if ("int2text".equals(operator)) {
			return int2text(args);
		} else if ("concat".equals(operator)) {
			return concat(args);
		} else if ("format".equals(operator)) {
			return message(args);
		} else if ("!".equals(operator)) {
			return not(args);
		} else if ("numeral-format".equals(operator)) {
			return numeralFormat(args);
		}
		else {
			throw new IllegalArgumentException("Unknown operator: '" + operator + "'");
		}
	}

	private Object doEvaluate(String expr) {
		
		expr = expr.trim();
		int leftPar = expr.indexOf('(');
		int rightPar = expr.lastIndexOf(')');
		if (leftPar==-1 && rightPar==-1) {
			return doEval1(expr);
		} else if (leftPar>-1 && rightPar>-1) {
			return doEval2( getArgs(expr.substring(leftPar+1, rightPar)));
		} else {
			throw new IllegalArgumentException("Unmatched parenthesis");
		}
	}

	private static double toNumber(Object input) {
		return Double.parseDouble(input.toString());
	}
	
	private static double add(Object[] input) {
		double ret = toNumber(input[0]);
		for (int i=1; i<input.length; i++) { ret += toNumber(input[i]); }
		return ret;
	}
	
	private static double subtract(Object[] input) {
		double ret = toNumber(input[0]);
		for (int i=1; i<input.length; i++) { ret -= toNumber(input[i]); }
		return ret;
	}
	
	private static double multiply(Object[] input) {
		double ret = toNumber(input[0]);
		for (int i=1; i<input.length; i++) { ret *= toNumber(input[i]); }
		return ret;
	}
	
	private static double divide(Object[] input) {
		double ret = toNumber(input[0]);
		for (int i=1; i<input.length; i++) { ret /= toNumber(input[i]); }
		return ret;
	}
	
	private static double modulo(Object[] input) {
		double ret = toNumber(input[0]);
		for (int i=1; i<input.length; i++) { ret %= toNumber(input[i]); }
		return ret;
	}
	
	//Renamed method because PMD is a bit stupid 
	private static boolean equalsOp(Object[] input) {
		try {
			for (int i=1; i<input.length; i++) { 
				if (((Double)(input[i-1])).doubleValue()!=((Double)(input[i])).doubleValue()) {
					return false;
				}
			}
			return true;
		} catch (ClassCastException e) {
			for (int i=1; i<input.length; i++) { 
				if (!(input[i-1]).equals(input[i])) {
					return false;
				}
			}
			return true;
		}
	}
	
	private static boolean smallerThan(Object[] input) {
		for (int i=1; i<input.length; i++) { 
			if (!(toNumber(input[i-1])<toNumber(input[i]))) { //NOPMD it makes sense to write it in this way here, because it maps to the purpose of the method
				return false;
			}
		}
		return true;
	}
	
	private static boolean smallerThanOrEqualTo(Object[] input) {
		for (int i=1; i<input.length; i++) { 
			if (!(toNumber(input[i-1])<=toNumber(input[i]))) { //NOPMD it makes sense to write it in this way here, because it maps to the purpose of the method
				return false;
			}
		}
		return true;
	}
	
	private static boolean greaterThan(Object[] input) {
		for (int i=1; i<input.length; i++) { 
			if (!(toNumber(input[i-1])>toNumber(input[i]))) { //NOPMD it makes sense to write it in this way here, because it maps to the purpose of the method
				return false;
			}
		}
		return true;
	}
	
	private static boolean greaterThanOrEqualTo(Object[] input) {
		for (int i=1; i<input.length; i++) { 
			if (!(toNumber(input[i-1])>=toNumber(input[i]))) { //NOPMD it makes sense to write it in this way here, because it maps to the purpose of the method
				return false;
			}
		}
		return true;
	}
	
	private static boolean and(Object[] input) {
		for (int i=1; i<input.length; i++) { 
			if (!((Boolean)(input[i-1]) && (Boolean)(input[i]))) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean or(Object[] input) {
		for (int i=0; i<input.length; i++) { 
			if ((Boolean)(input[i])) {
				return true;
			}
		}
		return false;
	}
	
	private static Object ifOp(Object[] input) {
		if (input.length!=3) {
			throw new IllegalArgumentException("Wrong number of arguments: (if arg1 arg2 arg3)");
		}
		if ((Boolean)(input[0])) {
			return input[1];
		} else {
			return input[2];
		}
	}
	
	private static String now(Object[] input) {
		if (input.length>1) {
			throw new IllegalArgumentException("Wrong number of arguments: (now format)");
		}
		SimpleDateFormat sdf = new SimpleDateFormat(input[0].toString());
		return sdf.format(new Date());
	}
	
	private static int round(Object[] input) {
		if (input.length>1) {
			throw new IllegalArgumentException("Wrong number of arguments: (round value)");
		}
		return (int)Math.round(toNumber(input[0]));
	}
	
	private static String numeralFormat(Object[] input) {
		if (input.length!=2) {
			throw new IllegalArgumentException("Wrong number of arguments: (numeral-format style value)");
		}
		return NumeralStyle.valueOf(input[0].toString().toUpperCase().replace('-', '_')).format((int)toNumber(input[1]));
	}
	
	private static Object not(Object[] input) {
		Object[] ret = new Object[input.length];
		for (int i=0; i<input.length; i++) { 
			if (input[i] instanceof Boolean) {
				ret[i] = !(Boolean)(input[i]);
			} else {
				ret[i] = !Boolean.parseBoolean(input[i].toString());
			}
		}
		if (ret.length==1) {
			return ret[0];
		} else {
			return ret;
		}
	}
	
	private Object set(Object[] input) {
		if (input.length>2) {
			throw new IllegalArgumentException("Wrong number of arguments: (set key value)");
		}
		localVars.put("$"+input[0].toString(), input[1]);
		return input[1];
	}

	private String int2text(Object[] input) {
		if (input.length > 2) {
			throw new IllegalArgumentException("Wrong number of arguments: (int2text integer language-code)");
		}
		int val = 0;
		
		if (input[0] instanceof Integer) {
			val = (Integer) input[0];
		} else {
			try {
				double d = toNumber(input[0]);
				if (Math.round(d) == d) {
					val = (int) Math.round(d);
				} else {
					throw new IllegalArgumentException("First argument must be an integer: " + input[0]);
				}
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("First argument must be an integer: " + input[0], e);
			}
		}

		if (integer2textFactoryMaker == null) {
			logger.warning("int2text operation is not supported in the current configuration.");
			return Integer.toString(val);
		}
		try {
			Integer2Text  t = integer2textFactoryMaker.newInteger2Text(input[1].toString());
			return t.intToText(val);
		} catch (Integer2TextConfigurationException e) {
			Instant t = CONFIGURATION_WARNING_ISSUED.get(input[1].toString());
			if (t==null || Instant.now().isAfter(t.plusSeconds(10))) {
				CONFIGURATION_WARNING_ISSUED.put(input[1].toString(), Instant.now());
				logger.warning("Locale not supported: " + input[1]);
			}
			return Integer.toString(val);
		} catch (IntegerOutOfRange e) {
			logger.warning("Integer out of range: " + input[0]);
			return Integer.toString(val);
		}
	}

	private Object concat(Object[] input) {
		StringBuilder sb = new StringBuilder();
		for (Object o : input) {
			sb.append(toReturnType(o));
		}
		return sb.toString();
	}
	
	private Object message(Object[] input) {
		Object[] args = new Object[input.length-1];
		System.arraycopy(input, 1, args, 0, input.length-1);
		return MessageFormat.format(input[0].toString(), args);
	}

	private static String[] getArgs(String expr) {
		expr = expr.trim();
		ArrayList<String> ret = new ArrayList<>();
		int ci = 0;
		int level = 0;
		boolean str = false;
		for (int i=0; i<expr.length(); i++) {
			if (expr.charAt(i)=='(') {
				if (str) {
					throw new IllegalArgumentException("Unmatched quote");
				}
				level++;
			} else if (expr.charAt(i)==')') {
				if (str) {
					throw new IllegalArgumentException("Unmatched quote");
				}
				level--;
			} else if (expr.charAt(i)=='"') {
				str = !str;
			}
			else if (expr.charAt(i)==' ' && level==0 && !str) {
				ret.add(expr.substring(ci, i));
				while (i+1<expr.length() && expr.charAt(i+1)==' ') { i++; }
				ci=i+1;
			}
		}
		ret.add(expr.substring(ci, expr.length()));
		String[] r = new String[ret.size()];
		/*
		for (int i=0; i<ret.size(); i++) {
			String arg = ret.get(i);
			if (arg.startsWith("\"") && arg.endsWith("\"")) {
				arg = arg.substring(1, arg.length()-1);
				ret.set(i, arg);
			}
		}*/
		return ret.toArray(r);
	}

}
