package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.daisy.pipeline.braille.liblouis.LiblouisutdmlConfigPath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisMathMLConfigPath",
	service = { LiblouisutdmlConfigPath.class },
	property = {
		"identifier:String=http://www.daisy.org/pipeline/modules/braille/liblouis-mathml/lbu_files/",
		"path:String=/lbu_files/mathml"
	}
)
public class LiblouisMathMLConfigPath extends LiblouisutdmlConfigPath {
	
	@Activate
	protected void activate(Map<?,?> properties) {
		super.activate(properties, LiblouisMathMLConfigPath.class);
	}
}
