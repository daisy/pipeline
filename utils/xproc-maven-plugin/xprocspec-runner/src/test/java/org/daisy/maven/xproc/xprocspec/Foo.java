package org.daisy.maven.xproc.xprocspec;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

public class Foo extends Identity {
	public Foo(XProcRuntime runtime, XAtomicStep step) {
		super(runtime,step);
	}
}
