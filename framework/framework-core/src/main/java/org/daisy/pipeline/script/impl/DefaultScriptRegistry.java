/*
 *
 */
package org.daisy.pipeline.script.impl;

import java.util.Map;

import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptParser;
import org.daisy.pipeline.script.XProcScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;


//TODO check thread safety
/**
 *  Default implementation for the ScriptRegistry interface.
 */
public class DefaultScriptRegistry implements ScriptRegistry {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(DefaultScriptRegistry.class);

	/** The descriptors. */
	private final Map<String, XProcScriptService> descriptors = Maps.newHashMap();

	/** The parser. */
	private XProcScriptParser parser;

	/**
	 * Activate (OSGI).
	 */
	public void activate(){
		logger.debug("Activating script registry");
	}

	/**
	 * Registers a script service loaded from DS
	 *
	 * @param script the script
	 */
	public void register(final XProcScriptService script) {
		logger.debug("Registering script {}", script.getId());
		if (!script.hasParser()){
			script.setParser(parser);
		}
		// TODO check
		descriptors.put(script.getId(), script);
	}

	/**
	 * Unregisters the script
	 *
	 * @param script the script
	 */
	public void unregister(XProcScriptService script) {
		logger.debug("Unregistering script {}", script.getId());
		// TODO check
		descriptors.remove(script.getId());
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.script.ScriptRegistry#getScripts()
	 */
	@Override
	public Iterable<XProcScriptService> getScripts() {
		return ImmutableList.copyOf(descriptors.values());
	}

	/**
	 * Sets the parser to load {@link XProcScript} objects from xpl files.
	 *
	 * @param parser the new parser
	 */
	public void setParser(XProcScriptParser parser) {
		// TODO check
		this.parser = parser;
	}

	/**
	 * Unsets the current parser object.
	 *
	 * @param parser the parser
	 */
	public void unsetParser(XProcScriptParser parser) {
	}

	@Override
	public XProcScriptService getScript(String name) {
		XProcScriptService descriptor = descriptors.get(name);
		if (descriptor==null){
			logger.warn("Script {} does not exist",name);
		}
		return descriptor;
	}
}
