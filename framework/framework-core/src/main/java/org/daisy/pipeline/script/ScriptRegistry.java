/*
 *
 */
package org.daisy.pipeline.script;


// TODO: Auto-generated Javadoc
/**
 * The Interface ScriptRegistry keeps track of the scripts defined by the loaded modules.
 */
public interface ScriptRegistry {

	/**
	 * Gets the script looking it up by its short name.
	 *
	 * @param name the script name
	 * @return the script
	 */
	public XProcScriptService getScript(String name);

	/**
	 * Gets all the scripts.
	 *
	 * @return the scripts
	 */
	public Iterable<XProcScriptService> getScripts();
}
