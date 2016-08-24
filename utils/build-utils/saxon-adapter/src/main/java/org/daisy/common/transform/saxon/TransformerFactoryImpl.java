package org.daisy.common.transform.saxon;

import java.util.Set;
import java.util.HashSet;

import javax.xml.transform.URIResolver;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.ExtensionFunctionDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerFactoryImpl extends net.sf.saxon.TransformerFactoryImpl {
	
	private URIResolver uriResolver;
	private final HashSet<ExtensionFunctionDefinition> xpathExtensionFunctions
		= new HashSet<ExtensionFunctionDefinition>();
	
	public void addFunction(ExtensionFunctionDefinition function) {
		logger.debug("Adding extension function definition {}", function.getFunctionQName().toString());
		xpathExtensionFunctions.add(function);
	}
	
	public void removeFunction(ExtensionFunctionDefinition function){
		logger.debug("Removing extension function definition {}", function.getFunctionQName().toString());
		xpathExtensionFunctions.remove(function);
	}
	
	public void setURIResolver(URIResolver resolver) {
		uriResolver = resolver;
	}
	
	public void activate() {
		Configuration config = getConfiguration();
		if (uriResolver != null)
			config.setURIResolver(uriResolver);
		for (Object function : xpathExtensionFunctions)
			config.registerExtensionFunction((ExtensionFunctionDefinition)function);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(TransformerFactoryImpl.class);
	
}
