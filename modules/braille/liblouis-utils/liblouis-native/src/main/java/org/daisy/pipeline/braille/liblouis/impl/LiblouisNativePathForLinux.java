package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.daisy.pipeline.braille.common.BundledNativePath;
import org.daisy.pipeline.braille.common.NativePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisNativePathForLinux",
	service = { NativePath.class },
	property = {
		"identifier:String=http://www.liblouis.org/native/linux/",
		"path:String=/native/linux",
		"os.family:String=linux"
	}
)
public class LiblouisNativePathForLinux extends BundledNativePath {
	
	/**
	 * @throws RuntimeException if the bundle doesn't work on Linux
	 */
	@Activate
	protected void activate(Map<?,?> properties) throws RuntimeException {
		activate(properties, LiblouisNativePathForMacOS.class);
	}
}
