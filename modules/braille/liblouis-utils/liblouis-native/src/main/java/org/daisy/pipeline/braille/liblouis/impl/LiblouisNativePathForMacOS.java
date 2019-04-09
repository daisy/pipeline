package org.daisy.pipeline.braille.liblouis.impl;

import java.util.Map;

import org.daisy.pipeline.braille.common.BundledNativePath;
import org.daisy.pipeline.braille.common.NativePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisNativePathForMacOS",
	service = { NativePath.class },
	property = {
		"identifier:String=http://www.liblouis.org/native/macosx/",
		"path:String=/native/macosx",
		"os.family:String=macosx"
	}
)
public class LiblouisNativePathForMacOS extends BundledNativePath {
	
	/**
	 * @throws RuntimeException if the bundle doesn't work on Mac OS
	 */
	@Activate
	protected void activate(Map<?,?> properties) throws RuntimeException {
		activate(properties, LiblouisNativePathForMacOS.class);
	}
}
