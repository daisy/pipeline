package org.daisy.pipeline.script;

public interface ScriptServiceProvider {

	/**
	 * Note that calling {@code ScriptRegistry.getScript()} from this method or
	 * from the return {@code Iterable}, is supported.
	 */
	public Iterable<ScriptService<?>> getScripts();

}
