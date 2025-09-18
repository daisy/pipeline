package org.daisy.pipeline.script;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.daisy.pipeline.script.impl.StaxXProcScriptParser;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO check thread safety

/**
 * Keeps track of the scripts defined by the loaded modules.
 */
@Component(
	name = "script-registry",
	service = { ScriptRegistry.class }
)
public class ScriptRegistry {

	/**
	 * Gets all the scripts.
	 */
	public Iterable<ScriptService<?>> getScripts() {
		return ImmutableList.copyOf(descriptors.values());
	}

	/**
	 * Gets the script looking it up by its short name.
	 */
	public ScriptService<?> getScript(String name) {
		ScriptService<?> descriptor = descriptors.get(name);
		if (descriptor == null)
			logger.warn("Script {} does not exist", name);
		return descriptor;
	}

	private static final Logger logger = LoggerFactory.getLogger(ScriptRegistry.class);
	private final Map<String,ScriptService<?>> descriptors = Maps.newHashMap();

	@Activate
	protected void activate() {
		logger.debug("Activating script registry");
		for (ScriptService<?> script : descriptors.values())
			if (script instanceof XProcScriptService)
				((XProcScriptService)script).setParser(parser);
	}

	@Reference(
		name = "script-service",
		unbind = "-",
		service = ScriptService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void register(ScriptService script) {
		logger.debug("Registering script {}", script.getId());
		descriptors.put(script.getId(), script);
	}

	// FIXME: the reason for this seperate method is that most XProc scripts currently implement the
	// XProcScriptService, but not ScriptService. In case there are scripts that implement both,
	// they will be registered (and created?) twice, but will only end up in the map once.
	@Reference(
		name = "xproc-script-service",
		unbind = "-",
		service = XProcScriptService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void registerXProcScriptService(ScriptService script) {
		register(script);
	}

	@Reference(
		name = "script-service-provider",
		unbind = "-",
		service = ScriptServiceProvider.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void register(ScriptServiceProvider scripts) {
		logger.debug("Registering scripts from {}", scripts);
		for (ScriptService<?> s : scripts.getScripts())
			register(s);
	}

	/**
	 * The parser to load {@link Script} objects from XProc files.
	 */
	private StaxXProcScriptParser parser;

	@Reference(
		name = "script-parser",
		unbind = "-",
		service = StaxXProcScriptParser.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setParser(StaxXProcScriptParser parser) {
		// TODO check
		this.parser = parser;
	}
}
