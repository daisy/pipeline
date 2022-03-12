package org.daisy.pipeline.common.calabash.impl;

import javax.xml.namespace.QName;

import com.google.common.collect.ImmutableMap;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcError;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "px:log-error",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}log-error" }
)
public class LogError implements XProcStepProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(LogError.class);

	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new LogErrorStep(runtime, step);
	}

	public static class LogErrorStep extends Identity implements XProcStep {

		private static final net.sf.saxon.s9api.QName _severity = new net.sf.saxon.s9api.QName("severity");
		private ReadablePipe errorPipe = null;

		private LogErrorStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		@Override
		public void setInput(String port, ReadablePipe pipe) {
			if ("source".equals(port))
				super.setInput(port, pipe);
			else
				errorPipe = pipe;
		}

		@Override
		public void reset() {
			super.reset();
			errorPipe.resetReader();
		}
		
		@Override
		public void run() throws SaxonApiException {
			String severity = getOption(_severity, "INFO");
			super.run();
			try {
				new ErrorReporter(e -> log(severity, e))
					.transform(
						ImmutableMap.of(new QName("source"), new XMLCalabashInputValue(errorPipe)),
						ImmutableMap.of())
					.run();
			} catch (Throwable e) {
				throw XProcStep.raiseError(e, step);
			}
		}

		private static void log(String severity, XProcError error) {
			String message = error.getMessage();
			if (error.getLocation().length > 0 || error.getCause() != null) {
				message += " (Please see detailed log for more info.)";
				LOGGER.debug(error.toString());
			}
			if ("TRACE".equals(severity))
				LOGGER.trace(message);
			else if ("DEBUG".equals(severity))
				LOGGER.debug(message);
			else if ("INFO".equals(severity))
				LOGGER.info(message);
			else if ("WARN".equals(severity))
				LOGGER.warn(message);
			else if ("ERROR".equals(severity))
				LOGGER.error(message);
		}
	}
}
