package org.daisy.common.xproc.calabash;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

// TODO: Auto-generated Javadoc
/**
 * The Interface XProcStepProvider.
 */
public interface XProcStepProvider {

	/**
	 *  Returns the implementation for the step represented by the XAtomicStep
	 *
	 * @param runtime the runtime
	 * @param step the step
	 * @return the x proc step
	 */
	XProcStep newStep(XProcRuntime runtime, XAtomicStep step);
}
