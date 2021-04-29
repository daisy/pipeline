package org.daisy.maven.xproc.xprocspec;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;
import net.sf.saxon.s9api.SaxonApiException;

public class ThrowJavaError extends Identity {
	public ThrowJavaError(XProcRuntime runtime, XAtomicStep step) {
		super(runtime,step);
	}
	@Override
	public void run() throws SaxonApiException {
		super.run();
		// this error is caught by XProcSpec
		throw XProcException.fromException(new Error("boom!"))
		                    .rebase(step.getLocation(),
		                            new RuntimeException().getStackTrace());
	}
}
