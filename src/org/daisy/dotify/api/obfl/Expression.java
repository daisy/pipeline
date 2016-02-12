package org.daisy.dotify.api.obfl;

import java.util.Map;

public interface Expression {

	/**
	 * Sets a globally accessible variable for this instance
	 * @param key the variable name
	 * @param value the value
	 */
	public void setVariable(String key, Object value);

	/**
	 * Removes a previously set variable
	 * @param key the variable name
	 */
	public void removeVariable(String key);

	/**
	 * Evaluate is the method to use when evaluating an expression.
	 * 
	 * @param expr
	 *            the expression to evaluate
	 * @return returns the evaluation result
	 */
	public Object evaluate(String expr);

	/**
	 * Evaluates this expression by first replacing any occurrences of the
	 * supplied variable
	 * names (prefixed by $) with the corresponding values in the map. The
	 * variable names must only
	 * contain word characters.
	 * 
	 * @param expr the expression to evaluate
	 * @param variables the variables
	 * @return returns the evaluation result
	 * @deprecated replace variables beforehand
	 */
	@Deprecated
	public Object evaluate(String expr, Map<String, String> variables);

	/**
	 * @param expr the expression
	 * @param vars the variables
	 * @return returns the evaluation result
	 * @deprecated replace variables beforehand
	 */
	@Deprecated
	public Object evaluate(String expr, String... vars);
}
