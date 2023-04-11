package org.daisy.pipeline.script;

/**
 * {@link Script} proxy.
 */
public interface ScriptService<T extends Script> {

	/**
	 * Get the script ID.
	 */
	public String getId();

	/**
	 * Get the script version.
	 */
	public String getVersion();

	/**
	 * Get the {@link Script} object.
	 */
	public T load();

}
