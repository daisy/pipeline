package org.daisy.pipeline.mathcat.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Locale;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;

import net.sf.saxon.Configuration;

import onl.mdw.mathcat4j.api.MathCat;
import static onl.mdw.mathcat4j.core.MathCatTransactional.mathCAT;

import org.daisy.common.file.URLs;
import org.daisy.common.saxon.SaxonBuffer;
import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.pipeline.tts.TTSInputProcessor;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Node;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Component(
	name = "MathCAT",
	service = { TTSInputProcessor.class }
)
public class MathCAT extends SingleInSingleOutXMLTransformer implements TTSInputProcessor {

	private final static Logger logger = LoggerFactory.getLogger(MathCAT.class);
	private final static String MIME_MATHML = "application/mathml+xml";
	private final File rulesDir;

	public MathCAT() {
		try {
			String version = mathCAT(MathCat::getVersion);
			// extract all files within the rules directory
			try {
				rulesDir = Files.createTempDirectory("pipeline-").toFile().getCanonicalFile();
			} catch (Exception e) {
				throw new RuntimeException("Could not create temporary directory", e);
			}
			rulesDir.deleteOnExit();
			copyFromJarRecursively("rules/", rulesDir, true);
			logger.debug("Using MathCAT version " + version);
		} catch (Throwable e) {
			throw new IllegalStateException("Failed to initialize MathCAT", e);
		}
	}

	@Override
	public int getPriority() {
		return 10;
	}

	@Override
	public boolean supportsInputMediaType(String mediaType) {
		return MIME_MATHML.equals(mediaType);
	}

	@Override
	public MathCAT forInputMediaType(String mediaType) {
		if (!MIME_MATHML.equals(mediaType))
			throw new UnsupportedOperationException("This processor does not support the input media type " + mediaType);
		return this;
	}

	@Override
	public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
		if (source == null || result == null)
			throw new IllegalArgumentException();
		return () -> {
			transform(source, result, params != null ? getParameterMap(params) : null);
		};
	}

	private void transform(XMLInputValue<?> source, XMLOutputValue<?> result, Map<QName,InputValue<?>> params) throws TransformerException {
		try {
			source = source.ensureSingleItem();
			Locale language = params != null ? TTSInputProcessor.getLanguage(params).orElse(null) : null;
			// drop unneeded namespace declarations in order to work around a bug in MathCAT
			SaxonBuffer buf = new SaxonBuffer(new Configuration());
			deleteNamespaces(source, buf.asOutput());
			buf.done();
			source = buf.asInput();
			Node mathmlNode = source.asNodeIterator().next();
			String mathml = serializeXML(mathmlNode);
			String ssml = mathCAT(m -> {
					m.setRulesDir(rulesDir.getAbsolutePath());
					m.setMathml(mathml);
					m.setPreference("TTS", "SSML");
					m.setPreference("SpeechStyle", "ClearSpeak");
					m.setPreference("Verbosity", "Verbose");
					m.setPreference("Language", language.toLanguageTag());
					return m.getSpokenText();
				}
			);
			ssml = "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\""
				+ " version=\"1.1\""
				+ ">" + ssml + "</speak>";
			Node ssmlNode = parseXML(ssml, mathmlNode.getBaseURI());
			result.asNodeConsumer().accept(ssmlNode);
		} catch (Throwable e) {
			throw new TransformerException(e);
		}
	}

	private static void deleteNamespaces(XMLInputValue<?> input, XMLOutputValue<?> output) throws TransformerException {
		BaseURIAwareXMLStreamReader reader = input.asXMLStreamReader();
		BaseURIAwareXMLStreamWriter writer = output.asXMLStreamWriter();
		try {
			while (true)
				try {
					int event = reader.getEventType();
					switch (event) {
					case START_DOCUMENT:
						writer.setBaseURI(reader.getBaseURI());
						XMLStreamWriterHelper.writeDocument(writer, reader);
						break;
					case START_ELEMENT:
						XMLStreamWriterHelper.writeElement(writer, reader);
						break;
					default:
						XMLStreamWriterHelper.writeEvent(writer, reader);
					}
					event = reader.next();
				} catch (NoSuchElementException e) {
					break;
				}
		} catch (XMLStreamException e) {
			throw new TransformerException(e);
		}
	}

	private static String serializeXML(Node node) throws TransformerConfigurationException, javax.xml.transform.TransformerException {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(writer));
		return writer.toString();
	}

	private static Node parseXML(String string, String baseURI) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		InputSource s = new InputSource(new StringReader(string));
		s.setSystemId(baseURI);
		return factory.newDocumentBuilder().parse(s).getDocumentElement();
	}

	private static void copyFromJarRecursively(String directory, File target, boolean deleteOnExit) throws IOException {
		Iterator<String> resources = URLs.listResourcesFromJAR(directory, MathCAT.class);
		while (resources.hasNext()) {
			String r = resources.next();
			File file = new File(target, new File(r).getName());
			if (r.endsWith("/"))
				copyFromJarRecursively(r, file, deleteOnExit);
			else
				copy(URLs.getResourceFromJAR(r, MathCAT.class), file, deleteOnExit);
		}
	}

	private static void copy(URL url, File file, boolean deleteOnExit) throws IOException {
		mkdirs(file.getParentFile(), deleteOnExit);
		if (file.createNewFile())
			if (deleteOnExit)
				file.deleteOnExit();
		FileOutputStream writer = new FileOutputStream(file);
		url.openConnection();
		InputStream reader = url.openStream();
		byte[] buffer = new byte[153600];
		int bytesRead = 0;
		while ((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
			buffer = new byte[153600];
		}
		writer.close();
		reader.close();
	}

	private static boolean mkdirs(File file, boolean deleteOnExit) {
		if (deleteOnExit)
			return file.mkdirs();
		if (file.exists())
			return false;
		if (file.mkdir()) {
			file.deleteOnExit();
			return true;
		}
		try {
			file = file.getCanonicalFile();
		} catch (IOException e) {
			return false;
		}
		File parent = file.getParentFile();
		return parent != null
			&& (mkdirs(parent, deleteOnExit) || parent.exists())
			&& file.mkdir();
	}
}
