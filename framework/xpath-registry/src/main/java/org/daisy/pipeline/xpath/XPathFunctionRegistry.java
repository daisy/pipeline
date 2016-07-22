package org.daisy.pipeline.xpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.trans.XPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathFunctionRegistry  {


	private static final Logger mLogger = LoggerFactory
			.getLogger(XPathFunctionRegistry.class);

	HashMap<QName,ExtensionFunctionDefinition> mFunctions = new HashMap<QName, ExtensionFunctionDefinition>();

	public void init(){

	}



	public void addFunction(ExtensionFunctionDefinition functionDefinition) throws XPathException{
		mLogger.info("Adding extension function definition to registry {}",functionDefinition.getFunctionQName().toString());
		mFunctions.put(functionDefinition.getFunctionQName().toJaxpQName(), functionDefinition);
	}

	public void removeFunction(ExtensionFunctionDefinition functionDefinition){
		mLogger.info("Deleting extension function definition to registry {}",functionDefinition.getFunctionQName().toString());
		mFunctions.remove(functionDefinition.getFunctionQName().toJaxpQName());

	}


	public void close(){

	}

	public Set<ExtensionFunctionDefinition> getFunctions(){
		return new HashSet<ExtensionFunctionDefinition>(mFunctions.values());
	}



}
