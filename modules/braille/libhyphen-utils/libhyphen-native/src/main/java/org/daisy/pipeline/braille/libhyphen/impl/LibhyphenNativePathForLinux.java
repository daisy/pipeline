package org.daisy.pipeline.braille.libhyphen.impl;

import java.util.Map;

import org.daisy.pipeline.braille.common.BundledNativePath;
import org.daisy.pipeline.braille.common.NativePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.libhyphen.impl.LibhyphenNativePathForLinux",
	service = { NativePath.class },
	property = {
		"identifier:String=http://hunspell.sourceforge.net/Hyphen/native/linux/",
		"path:String=/native/linux",
		"os.family:String=linux"
	}
)
public class LibhyphenNativePathForLinux extends BundledNativePath {
	
	/**
	 * @throws RuntimeException if the bundle doesn't work on Linux
	 */
	@Activate
	protected void activate(Map<?,?> properties) throws RuntimeException {
		activate(properties, LibhyphenNativePathForLinux.class);
	}
}
