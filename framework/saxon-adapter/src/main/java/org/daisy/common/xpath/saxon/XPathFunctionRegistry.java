package org.daisy.common.xpath.saxon;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.trans.XPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "xpath-registry",
	service = { XPathFunctionRegistry.class }
)
public class XPathFunctionRegistry  {

	private static final Logger mLogger = LoggerFactory.getLogger(XPathFunctionRegistry.class);

	HashMap<QName,ExtensionFunctionDefinition> mFunctions = new HashMap<QName, ExtensionFunctionDefinition>();

	@Reference(
		name = "ExtensionFunctionDefinition",
		unbind = "removeFunction",
		service = ExtensionFunctionDefinition.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addFunction(ExtensionFunctionDefinition functionDefinition) throws XPathException {
		mLogger.info("Adding extension function definition to registry {}", functionDefinition.getFunctionQName().toString());
		mFunctions.put(functionDefinition.getFunctionQName().toJaxpQName(), functionDefinition);
	}

	public void removeFunction(ExtensionFunctionDefinition functionDefinition) {
		mLogger.info("Deleting extension function definition to registry {}",functionDefinition.getFunctionQName().toString());
		mFunctions.remove(functionDefinition.getFunctionQName().toJaxpQName());
	}

	@Reference(
		name = "ExtensionFunctionProvider",
		unbind = "removeFunctionProvider",
		service = ExtensionFunctionProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addFunctionProvider(ExtensionFunctionProvider functionProvider) throws XPathException {
		for (ExtensionFunctionDefinition f : functionProvider.getDefinitions())
			addFunction(f);
	}

	public void removeFunctionProvider(ExtensionFunctionProvider functionProvider) {
		for (ExtensionFunctionDefinition f : functionProvider.getDefinitions())
			removeFunction(f);
	}

	public Set<ExtensionFunctionDefinition> getFunctions() {
		return new HashSet<ExtensionFunctionDefinition>(mFunctions.values());
	}
}
