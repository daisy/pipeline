package org.daisy.pipeline.modules;

// This is a (temporary) solution for the problem that Modules need to
// be created with a ModuleBuilder and are therefore not suited for
// dependency injection using Declarative Services.

public interface ModuleRef {
	
	/* Always returns the same object. */
	public Module get();
}
