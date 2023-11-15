package org.daisy.maven.xproc.xprocspec.logging.pipeline.impl;

import java.util.Map;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "org.daisy.maven.xproc.xprocspec.logging.pipeline.impl.MessageStepProvider",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/xprocspec}message" }
)
public class MessageStepProvider implements XProcStepProvider {
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		return new MessageStep(runtime, step);
	}
	private static class MessageStep extends org.daisy.maven.xproc.xprocspec.logging.calabash.impl.MessageStep implements XProcStep {
		MessageStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}
	}
}
