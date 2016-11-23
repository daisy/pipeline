package org.daisy.dotify.api.formatter;

import java.util.Map;

/**
 * Provides a rendering scenario.
 * 
 * @author Joel Håkansson
 */
public interface RenderingScenario {

	/**
	 * Renders the scenario into the supplied formatter.
	 * @param formatter the formatter to render into
	 * @throws FormatterException if rendering fails. If this exception is thrown,
	 * the supplied formatter may be in an unknown state.
	 */
	public void renderScenario(FormatterCore formatter) throws FormatterException;

	/**
	 * Calculates the cost of this scenario with the supplied 
	 * variables (which might, for example, include the total height
	 * of the rendered result).
	 *  
	 * @param variables the variables to use when calculating the cost
	 * @return returns the cost for this scenario
	 */
	public double calculateCost(Map<String, Double> variables);
}
