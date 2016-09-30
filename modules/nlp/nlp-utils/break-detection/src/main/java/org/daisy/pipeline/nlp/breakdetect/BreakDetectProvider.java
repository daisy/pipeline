package org.daisy.pipeline.nlp.breakdetect;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.nlp.lexing.LexServiceRegistry;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;

/**
 * OSGi component that provides an XProcStep on the top of LexServices satisfied
 * by OSGi services.
 */
public class BreakDetectProvider implements XProcStepProvider {

	private LexServiceRegistry mRegistry = null;

	public void setLexServiceRegistry(LexServiceRegistry r) {
		mRegistry = r;
	}

	public void unsetLexServiceRegistry(LexServiceRegistry r) {
		mRegistry = null;
	}

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new BreakDetectStep(runtime, step, mRegistry);
	}
}
