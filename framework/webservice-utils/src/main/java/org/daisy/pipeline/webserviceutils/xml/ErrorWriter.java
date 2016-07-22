package org.daisy.pipeline.webserviceutils.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ErrorWriter {

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

	Throwable error;
	String uri;
	private static Logger logger = LoggerFactory.getLogger(ErrorWriter.class.getName());

	private ErrorWriter(Throwable error, String uri) {
		this.error = error;
		this.uri = uri;
	}

	public Document getXmlDocument() {
		Document doc = XmlUtils.createDom("error");
		Element root = doc.getDocumentElement();
		root.setAttribute("query", this.uri);
		if (error != null) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			this.error.printStackTrace(new PrintStream(os));

			Element desc = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA,
					"description");
			desc.setTextContent(this.error.getMessage());
			Element trace = doc.createElementNS(XmlUtils.NS_PIPELINE_DATA,
					"trace");
			root.appendChild(desc);
			root.appendChild(trace);
			trace.setTextContent(os.toString());
		}
		if (!XmlValidator.validate(doc, XmlValidator.ERROR_SCHEMA_URL)) {
			logger.error("INVALID XML:\n" + XmlUtils.DOMToString(doc));
		}
		return doc;
	}
}
