package org.daisy.pipeline.common.calabash.impl;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "px:log-message",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}log-message" }
)
public class Message implements XProcStepProvider {

	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new MessageStep(runtime, step);
	}

	public static class MessageStep extends Identity implements XProcStep {

		private static final QName _message = new QName("message");
		private static final QName _severity = new QName("severity");
		
		private MessageStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		@Override
		public void run() throws SaxonApiException {
			String severity = getOption(_severity, "INFO");
			String message = getOption(_message, (String)null);
			if ("WARN".equals(severity)) {
				warning(this.step.getNode(), message);
			} else if ("DEBUG".equals(severity)) {
				// DefaultStep#fine() removed in ndw/xmlcalabash1@ce9b07d
				this.runtime.getMessageListener().fine(this,this.step.getNode(), message);
			} else {
				info(this.step.getNode(), message);
			}
			
			super.run();
		}
	}
}
