package impl;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

public class JavaStep extends Identity {
	
	private JavaStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}

	@Override
	public void run() throws SaxonApiException {
	}
	
	public static class Provider implements XProcStepProvider {
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new JavaStep(runtime, step);
		}
	}
}
