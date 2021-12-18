package org.daisy.maven.xproc.pipeline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
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
import org.daisy.common.messaging.ProgressMessage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

class MessagesXmlWriter {

	private MessagesXmlWriter() {}

	static String serializeMessages(MessageAccessor messages) {
		try {
			Element messagesElem = DocumentBuilderFactory.newInstance().newDocumentBuilder()
				.getDOMImplementation().createDocument(NS_PIPELINE_DATA, "messages", null)
				.getDocumentElement();
			for (Message m : messages.getAll())
				addMessage(m, true, messagesElem);

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

	private static final String NS_PIPELINE_DATA = "http://www.daisy.org/ns/pipeline/data";

	/* This method was copied from
	 * org.daisy.pipeline.webservice.xml.JobXmlWriter because we don't
	 * want to create a dependency to webservice */
	private static void addMessage(Message message, boolean progress, Element parentElem) {
		Document doc = parentElem.getOwnerDocument();
		Element messageElem = doc.createElementNS(NS_PIPELINE_DATA, "message");
		messageElem.setAttribute("level", message.getLevel().toString());
		messageElem.setAttribute("sequence", Integer.toString(message.getSequence()));
		messageElem.setAttribute("content", message.getText());
		messageElem.setAttribute("timeStamp", Long.toString(message.getTimeStamp().getTime()));
		if (message instanceof ProgressMessage) {
			ProgressMessage jm = (ProgressMessage)message;
			if (progress) {
				BigDecimal portion = jm.getPortion();
				if (portion.compareTo(BigDecimal.ZERO) > 0) {
					messageElem.setAttribute("portion", Float.toString(portion.floatValue()));
					messageElem.setAttribute("progress", Float.toString(jm.getProgress().floatValue()));
				} else {
					progress = false;
				}
			}
			for (Message m : jm) {
				addMessage(m, progress, messageElem);
			}
		}
		parentElem.appendChild(messageElem);
	}
}
