package org.daisy.dotify.api.formatter;


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
	 * <p>Evaluates the condition in the supplied context.</p> 
	 * 
	 * @param context the context
	 * @return returns the evaluation result
	 */
	public boolean evaluate(Context context);

}
