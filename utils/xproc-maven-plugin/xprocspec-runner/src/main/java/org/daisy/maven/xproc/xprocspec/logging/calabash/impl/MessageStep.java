package org.daisy.maven.xproc.xprocspec.logging.calabash.impl;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.library.Identity;
import com.xmlcalabash.runtime.XAtomicStep;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements xprocspec's pxi:message step
 */
// FIXME: make this into a generic step that can be used by other XProc engines too
public class MessageStep extends Identity {

	private static final QName _message = new QName("message");
	private static final QName _severity = new QName("severity");
	private static final String XPROCSPEC_LOGGER_NAME = "org.daisy.xprocspec";
	private static final Logger logger = LoggerFactory.getLogger(XPROCSPEC_LOGGER_NAME);
	
	public MessageStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}

	@Override
	public void run() throws SaxonApiException {
		String severity = getOption(_severity, "INFO");
		String message = getOption(_message, (String)null);
		if ("ERROR".equals(severity)) {
			logger.error(message);
		} else if ("WARN".equals(severity)) {
			logger.warn(message);
		} else if ("INFO".equals(severity)) {
			logger.info(message);
		} else if ("DEBUG".equals(severity)) {
			logger.debug(message);
		}
		super.run();
	}
}
