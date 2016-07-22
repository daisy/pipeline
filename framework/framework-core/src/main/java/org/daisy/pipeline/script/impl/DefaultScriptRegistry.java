/*
 *
 */
package org.daisy.pipeline.script.impl;

import java.net.URI;
import java.util.HashMap;
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
	private final Map<URI, XProcScriptService> descriptors = Maps.newHashMap();
	private final Map<String, URI> byNameDirectory = new HashMap<String, URI>();

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
		logger.trace("registering script {}",script.getURI());
		if (!script.hasParser()){
			script.setParser(parser);
		}
		// TODO check
		descriptors.put(script.getURI(), script);
		byNameDirectory.put(script.getId(), script.getURI());
	}

	/**
	 * Unregisters the script
	 *
	 * @param script the script
	 */
	public void unregister(XProcScriptService script) {
		// TODO check
		descriptors.remove(script);
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.script.ScriptRegistry#getScript(java.net.URI)
	 */
	@Override
	public XProcScriptService getScript(URI uri) {
		return descriptors.get(uri);
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
		URI uri = byNameDirectory.get(name);
		if (uri==null){
			logger.warn("Script {} does not exist",name);
			return null;
		}
		return descriptors.get(uri);
	}
}
