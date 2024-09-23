package org.daisy.pipeline.webservice.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ErrorWriter {

	private static final Logger logger = LoggerFactory.getLogger(ErrorWriter.class.getName());

	public static class ErrorWriterBuilder {

		private Throwable error;
		private String uri;

		public ErrorWriterBuilder withError(Throwable error) {
			this.error = error;
			return this;
		}

		public ErrorWriterBuilder withUri(String uri) {
			this.uri = uri;
			return this;
		}
		
		public ErrorWriter build(){
			return new ErrorWriter(this.error,this.uri);
		}
	}

	private final Throwable error;
	private final String uri;

	private ErrorWriter(Throwable error, String uri) {
		this.error = error;
		this.uri = uri;
	}

	public Document getXmlDocument() {
		Document doc = XmlUtils.createDom("error");
		Element root = doc.getDocumentElement();
		root.setAttribute("query", uri);
		if (error != null) {
			Element desc = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "description");
			desc.setTextContent(error.getMessage());
			root.appendChild(desc);

			// error stack trace is not relevant for user of API (server log can be inspected by whom is interested)
			/*Element trace = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA, "trace");
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			error.printStackTrace(new PrintStream(os));
			trace.setTextContent(os.toString());
			root.appendChild(trace);*/
		}
		if (!XmlValidator.validate(doc, XmlValidator.ERROR_SCHEMA_URL)) {
			logger.debug("INVALID XML:\n" + XmlUtils.nodeToString(doc));
		}
		return doc;
	}
}
