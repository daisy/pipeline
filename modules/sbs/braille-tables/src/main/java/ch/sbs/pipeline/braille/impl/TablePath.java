package ch.sbs.pipeline.braille.impl;

import java.util.Map;

import org.daisy.pipeline.braille.liblouis.LiblouisTablePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

@Component(
	name = "ch.sbs.pipeline.braille.impl.TablePath",
	service = {
		LiblouisTablePath.class
	},
	property = {
		"identifier:String=http://www.sbs.ch/pipeline/liblouis/tables/",
		"path:String=/tables"
	}
)

public class TablePath extends LiblouisTablePath {
	
	@Activate
	protected void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		super.activate(context, properties);
	}
}
