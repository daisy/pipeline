/*
 *
 */
package org.daisy.pipeline.script;

import java.net.URI;


// TODO: Auto-generated Javadoc
/**
 * The Interface ScriptRegistry keeps track of the scripts defined by the loaded modules.
 */
public interface ScriptRegistry {

	/**
	 * Gets the script located at the uri.
	 *
	 * @param uri the uri
	 * @return the script
	 */
	public XProcScriptService getScript(URI uri);
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
