package org.daisy.pipeline.mathcat.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
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
import org.daisy.pipeline.braille.common.AbstractTransform;
import org.daisy.pipeline.braille.common.AbstractTransformProvider;
import org.daisy.pipeline.braille.common.AbstractTransformProvider.util.Iterables;
import static org.daisy.pipeline.braille.common.AbstractTransformProvider.util.logCreate;
import org.daisy.pipeline.braille.common.Query;
import org.daisy.pipeline.braille.common.Query.Feature;
import org.daisy.pipeline.braille.common.Query.MutableQuery;
import static org.daisy.pipeline.braille.common.Query.util.mutableQuery;
import org.daisy.pipeline.braille.common.Transform;
import org.daisy.pipeline.braille.common.TransformProvider;
import static org.daisy.pipeline.braille.common.util.Locales.parseLocale;
import org.daisy.pipeline.braille.common.XMLTransform;
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
	private static File rulesDir = null;
	private final String brailleCode;

	public MathCAT() {
		this(null);
	}

	/**
	 * @param brailleCode If not {@code null}, output braille, otherwise output speech.
	 */
	private MathCAT(String brailleCode) {
		if (rulesDir == null)
			try {
				String version = mathCAT(MathCat::getVersion);
				// extract all files within the rules directory
				File dir;
				try {
					dir = Files.createTempDirectory("pipeline-").toFile().getCanonicalFile();
				} catch (Exception e) {
					throw new RuntimeException("Could not create temporary directory", e);
				}
				dir.deleteOnExit();
				copyFromJarRecursively("rules/", dir, true);
				logger.debug("Using MathCAT version " + version);
				rulesDir = dir;
			} catch (Throwable e) {
				throw new IllegalStateException("Failed to initialize MathCAT", e);
			}
		this.brailleCode = brailleCode;
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
			if (brailleCode != null) {
				String braille = mathCAT(m -> {
						m.setRulesDir(rulesDir.getAbsolutePath());
						m.setMathml(mathml);
						m.setPreference("BrailleCode", brailleCode);
						if (language != null)
							m.setPreference("Language", language.toLanguageTag());
						return m.getBraille("");
					}
				);
				try (BaseURIAwareXMLStreamWriter writer = result.asXMLStreamWriter()) {
					writer.setBaseURI(URI.create(mathmlNode.getBaseURI()));
					writer.writeStartDocument();
					XMLStreamWriterHelper.writeStartElement(writer, new QName("http://www.w3.org/ns/xproc-step", "result", "c"));
					writer.writeCharacters(braille);
					writer.writeEndElement();
					writer.writeEndDocument();
				} catch (XMLStreamException e) {
					throw new TransformerException(e);
				}
			} else {
				String ssml = mathCAT(m -> {
						m.setRulesDir(rulesDir.getAbsolutePath());
						m.setMathml(mathml);
						m.setPreference("TTS", "SSML");
						m.setPreference("SpeechStyle", "ClearSpeak");
						m.setPreference("Verbosity", "Verbose");
						if (language != null)
							m.setPreference("Language", language.toLanguageTag());
						return m.getSpokenText();
					}
				);
				ssml = "<speak xmlns=\"http://www.w3.org/2001/10/synthesis\""
					+ " version=\"1.1\""
					+ ">" + ssml + "</speak>";
				Node ssmlNode = parseXML(ssml, mathmlNode.getBaseURI());
				result.asNodeConsumer().accept(ssmlNode);
			}
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

	@Component(
		name = "mathcat-provider",
		service = {
			TransformProvider.class
		}
	)
	public static class Provider extends AbstractTransformProvider<Transform> {

		private final static Iterable<Transform> empty = Iterables.<Transform>empty();

		@Override
		protected Iterable<Transform> _get(final Query query) {
			MutableQuery q = mutableQuery(query);
			try {
				if ("mathml".equals(q.removeOnly("input").getValue().get())) {
					if (q.containsKey("math-translator"))
						if (!"mathcat".equals(q.removeOnly("math-translator").getValue().get()))
							return empty;
					for (Feature f : q.get("output"))
						if (!"braille".equals(f.getValue().get()))
							return empty;
					Locale locale = null; {
						if (q.containsKey("locale")) {
							try {
								locale = parseLocale(q.removeOnly("locale").getValue().get()); }
							catch (IllegalArgumentException e) {
								return empty;
							}
						}
						if (q.containsKey("document-locale")) {
							if (locale == null)
								try {
									locale = parseLocale(q.removeOnly("document-locale").getValue().get()); }
								catch (IllegalArgumentException e) {
									return empty;
								}
							else
								q.removeAll("document-locale");
						}
					}
					String code = null; {
						if (q.containsKey("math-code"))
							code = q.removeOnly("math-code").getValue().get();
					}
					if (q.isEmpty()) {
						if (code == null)
							code = "Nemeth";
						return Iterables.of(logCreate((Transform)new TransformImpl(new MathCAT(code))));
					}
				}
			} catch (IllegalStateException e) {
				// thrown by Query.getOnly()
			}
			return empty;
		}

		private static class TransformImpl extends AbstractTransform implements XMLTransform {

			private final MathCAT instance;

			private TransformImpl(MathCAT instance) {
				this.instance = instance;
			}

			@Override
			public SingleInSingleOutXMLTransformer fromXmlToXml() {
				return instance;
			}
		}
	}
}
