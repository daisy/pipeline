package org.daisy.dotify.api.formatter;


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
	 * <p>Renders the content in the supplied context.</p> 
	 * 
	 * @param context the context
	 * @return returns the evaluation result
	 */
	public String render(Context context);

}
