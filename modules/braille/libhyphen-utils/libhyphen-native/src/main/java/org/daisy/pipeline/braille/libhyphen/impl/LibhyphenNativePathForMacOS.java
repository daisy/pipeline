package org.daisy.pipeline.braille.libhyphen.impl;

import java.util.Map;

import org.daisy.pipeline.braille.common.BundledNativePath;
import org.daisy.pipeline.braille.common.NativePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.libhyphen.impl.LibhyphenNativePathForMacOS",
	service = { NativePath.class },
	property = {
		"identifier:String=http://hunspell.sourceforge.net/Hyphen/native/macosx/",
		"path:String=/native/macosx",
		"os.family:String=macosx"
	}
)
public class LibhyphenNativePathForMacOS extends BundledNativePath {
	
	/**
	 * @throws RuntimeException if the bundle doesn't work on Mac OS
	 */
	@Activate
	protected void activate(Map<?,?> properties) throws RuntimeException {
		activate(properties, LibhyphenNativePathForMacOS.class);
	}
}
