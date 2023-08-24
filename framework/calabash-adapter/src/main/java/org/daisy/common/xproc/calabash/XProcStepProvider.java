package org.daisy.common.xproc.calabash;

import java.util.Map;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.xproc.XProcMonitor;

public interface XProcStepProvider {

	/**
	 *  Returns the implementation for the step represented by the XAtomicStep
	 */
	XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties);
}
