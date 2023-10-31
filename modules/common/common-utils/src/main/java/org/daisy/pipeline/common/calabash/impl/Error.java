package org.daisy.pipeline.common.calabash.impl;

import java.util.Map;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.CalabashExceptionFromXProcError;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcError;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pxi:error",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}error" }
)
public class Error implements XProcStepProvider {

	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		return new ErrorStep(runtime, step);
	}

	/**
	 * Own version of <code>p:error</code> that reads one or more <code>c:errors</code> documents
	 * with one or more <code>c:error</code> elements in each, creates {@link XProcException}s,
	 * reports them, and throws the last one.
	 */
	public static class ErrorStep extends DefaultStep implements XProcStep {

		private ReadablePipe errorPipe = null;
		private XProcException lastError = null;

		private ErrorStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		@Override
		public void setInput(String port, ReadablePipe pipe) {
			if ("error".equals(port))
				errorPipe = pipe;
		}

		@Override
		public void setOutput(String port, WritablePipe pipe) {
		}
		
		@Override
		public void reset() {
			errorPipe.resetReader();
			lastError = null;
		}
		
		@Override
		public void run() throws SaxonApiException {
			try {
				lastError = null;
				new ErrorReporter(this::report)
					.transform(
						ImmutableMap.of(new QName("source"), new XMLCalabashInputValue(errorPipe)),
						ImmutableMap.of())
					.run();
				if (lastError != null)
					throw lastError;
				else
					throw new RuntimeException("Expected c:errors document with at least one c:error");
			} catch (Throwable e) {
				throw XProcStep.raiseError(e, step);
			}
		}

		private void report(XProcError error) {
			lastError = CalabashExceptionFromXProcError.from(error);
			step.reportError(lastError);
		}
	}
}
