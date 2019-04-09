package org.daisy.pipeline.braille.liblouis.impl;

import org.daisy.pipeline.braille.common.ResourceRegistry;
import org.daisy.pipeline.braille.liblouis.LiblouisutdmlConfigPath;
import org.daisy.pipeline.braille.liblouis.LiblouisutdmlConfigResolver;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisutdmlConfigRegistry",
	service = { LiblouisutdmlConfigResolver.class }
)
public class LiblouisutdmlConfigRegistry
	extends ResourceRegistry<LiblouisutdmlConfigPath>
	implements LiblouisutdmlConfigResolver {
	
	@Reference(
		name = "LiblouisutdmlConfigPath",
		unbind = "_unregister",
		service = LiblouisutdmlConfigPath.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	protected void _register(LiblouisutdmlConfigPath path) {
		register(path);
	}
	
	protected void _unregister(LiblouisutdmlConfigPath path) {
		unregister(path);
	}
}
