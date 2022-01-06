package com.xmlcalabash.io;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.util.MessageFormatter;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOnlyPipe implements ReadablePipe {

	protected final Logger logger = LoggerFactory.getLogger(Pipe.class);
	private static int idCounter = 0;
	private final int id;
	private final XProcRuntime runtime;
	private final ReadableDocumentSequence documents;
	protected int pos = 0;
	private boolean readSeqOk = false;
	private Step reader = null;
	private String stepName = null;
	private String portName = null;

	public ReadOnlyPipe(XProcRuntime runtime, ReadableDocumentSequence seq) {
		this.runtime = runtime;
		documents = seq;
		id = ++idCounter;
		seq.addReader();
	}

	public void canReadSequence(boolean sequence) {
		readSeqOk = sequence;
	}

	public boolean readSequence() {
		return readSeqOk;
	}

	public XdmNode read() throws SaxonApiException {
		if (pos > 0 && !readSeqOk) {
			dynamicError(6);
		}
		XdmNode doc = documents.get(pos++);
		if (reader != null) {
			logger.trace(
				MessageFormatter.nodeMessage(
					reader.getNode(),
					reader.getName() + " read '" + (doc == null ? "null" : doc.getBaseURI()) + "' from " + this));
		}
		return doc;
	}

	public void setReader(Step step) {
		reader = step;
	}

	// These are for debugging...
	public void setNames(String stepName, String portName) {
		this.stepName = stepName;
		this.portName = portName;
	}

	public void resetReader() {
		pos = 0;
	}

	public boolean moreDocuments() throws SaxonApiException {
		return pos < documents.size();
	}

	public int documentCount() throws SaxonApiException {
		return documents.size();
	}

	public ReadableDocumentSequence documents() {
		return documents;
	}

	@Override
	public String toString() {
		return "[pipe #" + id + "] (" + documents + ")";
	}

	protected void dynamicError(int errno) {
		String msg = null;
		if (stepName != null) {
			msg = "Reading " + portName + " on " + stepName;
		}
		throw XProcException.dynamicError(errno, reader, msg);
	}
}
