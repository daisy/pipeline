package org.daisy.dotify.formatter.impl.obfl;

import java.util.Map;

/**
 * Provides helpers for OBFL-expressions.
 * @author Joel Håkansson
 *
 */
public class ExpressionTools {
	
	private ExpressionTools(){}
	
	/**
	 * Resolves $-prefixed variables in the input string.
	 * @param expr the input string
	 * @param variables the variables
	 * @return returns the resolved string
	 */
	public static String resolveVariables(String expr, Map<String, String> variables) {
		if (variables==null) {
			return expr;
		}
		for (String varName : variables.keySet()) {
			expr = expr.replaceAll("\\$"+varName+"(?=\\W)", variables.get(varName));
		}
		return expr;
	}
	
	/**
	 * Resolves $-prefixed variables in the input string.
	 * @param expr the input string
	 * @param vars the variables
	 * @return returns the resolved string
	 */
	public static String resolveVariables(String expr, String ... vars) {
		for (String var : vars) {
			String[] v = var.split("=", 2);
			expr = expr.replaceAll("\\$"+v[0]+"(?=\\W)", v[1]);
		}
		return expr;
	}

}