package org.daisy.dotify.api.formatter;

/**
 * Provides an interface for a dynamic renderer.
 * @author Joel Håkansson
 *
 */
public interface DynamicRenderer {

	/**
	 * Gets the rendering scenarios in this renderer.
	 * @return returns a list of renderers
	 */
	public Iterable<RenderingScenario> getScenarios();
}
