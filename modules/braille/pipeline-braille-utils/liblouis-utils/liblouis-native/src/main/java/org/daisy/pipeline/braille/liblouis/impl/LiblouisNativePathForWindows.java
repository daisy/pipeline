package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.daisy.pipeline.braille.common.BundledNativePath;
import org.daisy.pipeline.braille.common.NativePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.ComponentContext;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisNativePathForWindows",
	service = {
		NativePath.class
	},
	property = {
		"identifier:String=http://www.liblouis.org/native/windows/",
		"path:String=/native/windows",
		"os.family:String=windows"
	}
)
public class LiblouisNativePathForWindows extends BundledNativePath {
	
	@Activate
	protected void activate(ComponentContext context, Map<?,?> properties) throws Exception {
		super.activate(context, properties);
	}
}
