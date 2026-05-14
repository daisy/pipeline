package org.daisy.pipeline.css;

import java.util.Optional;

import org.daisy.common.xpath.saxon.ExtensionFunctionProvider;
import org.daisy.common.xpath.saxon.ReflexiveExtensionFunctionProvider;

import org.osgi.service.component.annotations.Component;

import org.w3c.dom.Element;

public interface StyleAccessor {

	@Component(
		name = "StyleAccesor",
		service = { ExtensionFunctionProvider.class }
	)
	public static class Provider extends ReflexiveExtensionFunctionProvider {
		public Provider() {
			super(StyleAccessor.class);
		}
	}

	/**
	 * Get the <a
	 * href="https://www.w3.org/TR/2013/CR-css-cascade-3-20131003/#specified">specified
	 * value</a> of a CSS property and element.
	 */
	public Optional<String> get(Element element, String property);

	/**
	 * Test whether an element matches a CSS selector.
	 *
	 * @throws IllegalArgumentException if the selector can not be compiled
	 */
	public boolean matches(Element element, String selector);

}
