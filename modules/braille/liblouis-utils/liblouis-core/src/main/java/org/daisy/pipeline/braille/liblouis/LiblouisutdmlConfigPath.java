package org.daisy.pipeline.braille.liblouis;

import java.util.HashMap;
import java.util.Map;

import org.daisy.pipeline.braille.common.BundledResourcePath;

import org.osgi.service.component.ComponentContext;

public class LiblouisutdmlConfigPath extends BundledResourcePath {
	
	@Override
	protected void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		if (properties.get(BundledResourcePath.UNPACK) != null)
			throw new IllegalArgumentException(BundledResourcePath.UNPACK + " property not supported");
		Map<Object,Object> props = new HashMap<Object,Object>(properties);
		props.put(BundledResourcePath.UNPACK, true);
		super.activate(context, props);
	}
}
