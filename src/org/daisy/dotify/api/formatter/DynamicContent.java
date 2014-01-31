package org.daisy.dotify.api.formatter;

import java.util.Map;

/**
 * Provides an interface for dynamic text, in other words
 * content that may change between renderings.
 * 
 * @author Joel Håkansson
 */
public interface DynamicContent {

	/**
	 * Renders the content without supplying any variables.
	 * 
	 * @return returns the evaluation result
	 */
	public String render();

	/**
	 * <p>Renders the content using the supplied variables.</p> 
	 * <p>The variable names may only contain word characters.</p>
	 * 
	 * @param variables the variables
	 * @return returns the evaluation result
	 */
	public String render(Map<String, String> variables);

	/**
	 * <p>Renders the content using the supplied variables. Each supplied string 
	 * should be a key/value pair separated by '='. E.g. <tt>myVariable=1</tt></p> 
	 * <p>The variable names may only contain word characters.</p>
	 * 
	 * @param variables the variables
	 * @return returns the evaluation result
	 */
	public String render(String... vars);
}
