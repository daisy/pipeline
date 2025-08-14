package org.daisy.common.xpath.saxon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

	private static final Logger logger = LoggerFactory.getLogger(XPathFunctionRegistry.class);

	private final List<ExtensionFunctionDefinition> definitions = new ArrayList<>();
	private final List<ExtensionFunctionProvider> providers = new ArrayList<>();

	@Reference(
		name = "ExtensionFunctionDefinition",
		unbind = "-",
		service = ExtensionFunctionDefinition.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addFunction(ExtensionFunctionDefinition definition) throws XPathException {
		logger.info("Adding extension function definition to registry {}", definition.getFunctionQName().toString());
		definitions.add(definition);
	}

	@Reference(
		name = "ExtensionFunctionProvider",
		unbind = "-",
		service = ExtensionFunctionProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addFunctionProvider(ExtensionFunctionProvider provider) throws XPathException {
		logger.info("Adding extension function provider to registry {}", provider.toString());
		providers.add(provider);
	}

	public Set<ExtensionFunctionDefinition> getFunctions() {
		return getFunctions(null);
	}

	public Set<ExtensionFunctionDefinition> getFunctions(Collection<Object> context) {
		// to make sure there are no two functions with the same name, we make a map
		Map<QName,ExtensionFunctionDefinition> functions = new HashMap<>();
		for (ExtensionFunctionDefinition definition : definitions)
			functions.put(definition.getFunctionQName().toJaxpQName(), definition);
		for (ExtensionFunctionProvider provider : providers)
			for (ExtensionFunctionDefinition definition : provider.getDefinitions(context))
				functions.put(definition.getFunctionQName().toJaxpQName(), definition);
		return new HashSet<ExtensionFunctionDefinition>(functions.values());
	}
}
