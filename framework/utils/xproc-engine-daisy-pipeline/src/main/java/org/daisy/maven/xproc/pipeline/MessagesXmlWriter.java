package org.daisy.maven.xproc.pipeline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XsltTransformer;

import org.daisy.common.file.URLs;
import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.pipeline.webserviceutils.xml.JobXmlWriter;
import org.daisy.pipeline.webserviceutils.xml.XmlUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class MessagesXmlWriter {

	private MessagesXmlWriter() {}

	static String serializeMessages(MessageAccessor messages) {
		try {
			Element messagesElem = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.getDOMImplementation().createDocument(XmlUtils.NS_PIPELINE_DATA, "messages", null)
				.getDocumentElement();
			for (Message m : messages.getAll())
				JobXmlWriter.addMessage(m, true, messagesElem);

			// add corrected portion attributes based on timestamps
			Processor proc = new Processor(false);
			XsltTransformer correctPortions = proc.newXsltCompiler()
				.compile(
					new StreamSource(
						URLs.getResourceFromJAR(
							"/org/daisy/maven/xproc/pipeline/resource-files/analyze-portions.xsl", MessagesXmlWriter.class)
						.openStream()))
				.load();
			correctPortions.setSource(new DOMSource(messagesElem.getOwnerDocument()));
			XdmDestination dest = new XdmDestination();
			correctPortions.setDestination(dest);
			correctPortions.transform();
			Serializer serializer = proc.newSerializer();
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			serializer.setOutputStream(os);
			serializer.setCloseOnCompletion(true);
			serializer.serializeNode(dest.getXdmNode());
			return new String(os.toByteArray(), StandardCharsets.UTF_8);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (SaxonApiException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
