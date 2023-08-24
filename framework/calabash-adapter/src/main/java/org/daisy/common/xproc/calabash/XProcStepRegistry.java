package org.daisy.common.xproc.calabash;

import java.util.Map;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;

import org.daisy.common.xproc.XProcMonitor;

/**
 * The Interface XProcStepRegistry sets the behaviour for step registration
 */
public interface XProcStepRegistry {

	/**
	 * Checks whether the step identified by the qualified name has been already registered.
	 *
	 * @param type the step
	 * @return true, if successful
	 */
	boolean hasStep(QName type);

	/**
	 * returns the step implementation for the atomic step using the the step provider type given by the first argument.
	 */
	XProcStep newStep(QName type, XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties);
}
