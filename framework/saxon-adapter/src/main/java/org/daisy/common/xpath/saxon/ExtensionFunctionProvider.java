package org.daisy.common.xpath.saxon;

import java.util.Collection;

import net.sf.saxon.lib.ExtensionFunctionDefinition;

public interface ExtensionFunctionProvider {

	public Collection<ExtensionFunctionDefinition> getDefinitions();

}
