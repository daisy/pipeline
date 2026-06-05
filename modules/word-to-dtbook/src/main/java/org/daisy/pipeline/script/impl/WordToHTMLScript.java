package org.daisy.pipeline.script.impl;

import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptServiceProvider;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "word-to-html",
	service = { ScriptServiceProvider.class }
)
public class WordToHTMLScript extends WordBasedScript {

	public WordToHTMLScript() {
		super("html");
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
