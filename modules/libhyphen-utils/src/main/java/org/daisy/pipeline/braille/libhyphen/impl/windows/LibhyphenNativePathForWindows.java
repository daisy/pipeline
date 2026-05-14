package org.daisy.pipeline.braille.libhyphen.impl.windows;

import java.util.Map;

import org.daisy.pipeline.braille.common.BundledNativePath;
import org.daisy.pipeline.braille.common.NativePath;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.pipeline.braille.libhyphen.impl.windows.LibhyphenNativePathForWindows",
	service = { NativePath.class },
	property = {
		"identifier:String=http://hunspell.sourceforge.net/Hyphen/native/windows/",
		"path:String=/native/windows",
		"os.family:String=windows"
	}
)
public class LibhyphenNativePathForWindows extends BundledNativePath {
	
	/**
	 * @throws RuntimeException if the bundle doesn't work on Windows
	 */
	@Activate
	protected void activate(Map<?,?> properties) throws RuntimeException {
		activate(properties, LibhyphenNativePathForWindows.class);
	}
}
