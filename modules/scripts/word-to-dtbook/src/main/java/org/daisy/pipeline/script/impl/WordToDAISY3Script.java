package org.daisy.pipeline.script.impl;

import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptServiceProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "word-to-daisy3",
	service = { ScriptServiceProvider.class }
)
public class WordToDAISY3Script extends WordBasedScript {

	public WordToDAISY3Script() {
		super("daisy3");
	}

	@Reference(
		name = "ScriptRegistry",
		unbind = "-",
		service = ScriptRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	@Override
	protected void setScriptRegistry(ScriptRegistry registry) {
		super.setScriptRegistry(registry);
	}
}
