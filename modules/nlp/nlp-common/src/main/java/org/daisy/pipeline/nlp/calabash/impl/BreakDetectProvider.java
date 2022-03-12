package org.daisy.pipeline.nlp.calabash.impl;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.nlp.lexing.LexServiceRegistry;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * OSGi component that provides an XProcStep on the top of LexServices satisfied
 * by OSGi services.
 */
@Component(
	name = "break-detector",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}break-detect" }
)
public class BreakDetectProvider implements XProcStepProvider {

	private LexServiceRegistry mRegistry = null;

	@Reference(
		name = "LexServiceRegistry",
		unbind = "unsetLexServiceRegistry",
		service = LexServiceRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
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
