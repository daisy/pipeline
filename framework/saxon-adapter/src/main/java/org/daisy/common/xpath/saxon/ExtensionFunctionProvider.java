package org.daisy.common.xpath.saxon;

import java.util.Collection;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

public interface ExtensionFunctionProvider {

	public Collection<ExtensionFunctionDefinition> getDefinitions();

	/**
	 * Override this method if specific context is required to enable certain functions.
	 *
	 * @param context allowed to be {@code null}.
	 */
	public default Collection<ExtensionFunctionDefinition> getDefinitions(Collection<Object> context) {
		return getDefinitions();
	}
}
