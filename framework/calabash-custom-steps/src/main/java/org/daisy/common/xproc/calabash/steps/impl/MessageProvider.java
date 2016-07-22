package org.daisy.common.xproc.calabash.steps.impl;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;


/**
 * This class offers a reimplementation of calabash's Message step (cx:message) which uses internal message throwing mechanisms instead of dumping the messages to stdout.
 */
public class MessageProvider implements XProcStepProvider {

	/** The logger. */
	Logger logger = LoggerFactory.getLogger(this.getClass());

	/* (non-Javadoc)
	 * @see org.daisy.common.xproc.calabash.XProcStepProvider#newStep(com.xmlcalabash.core.XProcRuntime, com.xmlcalabash.runtime.XAtomicStep)
	 */
	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new Message(runtime, step);
	}

	/**
	 * Activate (OSGI)
	 */
	public void activate() {
		logger.trace("Activating cx:message provider");
	}
}

/**
 * Actual implementation of the Message step
 *
 *
 */
class Message extends DefaultStep {
	private static final QName _message = new QName("", "message");
	private ReadablePipe source = null;
	private WritablePipe result = null;

	/**
	 * Creates a new instance of Identity
	 */
	public Message(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}

	@Override
	public void setInput(String port, ReadablePipe pipe) {
		source = pipe;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}

	@Override
	public void reset() {
		source.resetReader();
		result.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		super.run();

		String message = getOption(_message).getString();
		// System.err.println("Message: " + "Message:"+message);
		runtime.info(this, step.getNode(), "Message:" + message);
		while (source.moreDocuments()) {
			XdmNode doc = source.read();
			runtime.finest(
					this,
					step.getNode(),
					"Message step " + step.getName() + " read "
							+ doc.getDocumentURI());
			result.write(doc);
		}
	}
}