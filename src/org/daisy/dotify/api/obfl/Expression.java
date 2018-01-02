package org.daisy.dotify.api.obfl;

/**
 * Provides an interface for OBFL-expressions.
 * @author Joel Håkansson
 *
 */
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
	 * Removes all global variables
	 */
	public void removeAllVariables();

	/**
	 * Evaluate is the method to use when evaluating an expression.
	 * 
	 * @param expr
	 *            the expression to evaluate
	 * @return returns the evaluation result
	 */
	public Object evaluate(String expr);

}
