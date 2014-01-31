package org.daisy.dotify.api.formatter;

import java.util.Map;

/**
 * Provides an interface for a condition.
 * 
 * @author Joel Håkansson
 */
public interface Condition {

	/**
	 * Evaluates the condition without supplying any variables.
	 * 
	 * @return returns the evaluation result
	 */
	public boolean evaluate();

	/**
	 * <p>Evaluates the condition using the supplied variables.</p> 
	 * <p>The variable names may only contain word characters.</p>
	 * 
	 * @param variables the variables
	 * @return returns the evaluation result
	 */
	public boolean evaluate(Map<String, String> variables);

	/**
	 * <p>Evaluates the condition using the supplied variables. Each supplied string 
	 * should be a key/value pair separated by '='. E.g. <tt>myVariable=1</tt></p> 
	 * <p>The variable names may only contain word characters.</p>
	 * 
	 * @param variables the variables
	 * @return returns the evaluation result
	 */
	public boolean evaluate(String... vars);
}
