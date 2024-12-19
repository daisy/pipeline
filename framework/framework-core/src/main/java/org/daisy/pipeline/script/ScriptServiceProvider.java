package org.daisy.pipeline.script;

public interface ScriptServiceProvider {

	public Iterable<ScriptService<?>> getScripts();

}
