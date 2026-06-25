package org.daisy.pipeline.dtbook_to_ebraille.calabash.impl;

import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.collect.Sets;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper;
import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;

public class HtmlInsertSyncPointsStep extends DefaultStep implements XProcStep {

	@Component(
		name = "pxi:html-insert-sync-points",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}html-insert-sync-points" }
	)
	public static class Provider implements XProcStepProvider {
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new HtmlInsertSyncPointsStep(runtime, step);
		}
	}

	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;

	private HtmlInsertSyncPointsStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		sourcePipe = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		resultPipe = pipe;
	}
	
	@Override
	public void reset() {
		sourcePipe.resetReader();
		resultPipe.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		try {
			new HtmlInsertSyncPoints()
				.transform(
					XMLCalabashInputValue.of(sourcePipe),
					XMLCalabashOutputValue.of(resultPipe, runtime))
				.run();
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	private static final String XMLNS_HTML = "http://www.daisy.org/z3986/2005/html/";
	private static final QName HTML_A = new QName(XMLNS_HTML, "a");
	private static final QName HTML_SPAN = new QName(XMLNS_HTML, "span");
	private static final QName _CLASS = new QName("class");
	private static final String SYNC_CLASS = "__tmp__sync__";

	private static final Set<String> INLINE_ELEMENTS = Sets.newHashSet(
		// elements that are conventionally considered inline in HTML by browsers, excluding
		// browser-specific oddities
		// (some elements could be missing but it is not a problem to insert more synchronization points)
		"a",        "i",          "ruby",
		"abbr",     "img",        "s",
		"audio",    "ins",        "samp",
		"b",        "kbd",        "small",
		"bdi",      "label",      "span",
		"bdo",      "map",        "strong",
		"br",       "mark",       "sub",
		"canvas",   "meter",      "sup",
		"cite",     "object",     "svg",
		"code",     "output",     "time",
		"data",     "progress",   "u",
		"del",      "q",          "var",
		"dfn",      "rp",         "video",
		"em",       "rt",         "wbr"
	);
	private static final Set<String> TEXT_ONLY_ELEMENTS = Sets.newHashSet(
		// does not include elements that can contain no children at all, or elements that can contain
		// elements but no text
		"script",
		"style",
		"textarea",
		"title"
	);

	private static class HtmlInsertSyncPoints extends SingleInSingleOutXMLTransformer {

		public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
			if (source == null || result == null)
				throw new IllegalArgumentException();
			return () -> transform(source.asXMLStreamReader(), result.asXMLStreamWriter());
		}

		public void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter writer) throws TransformerException {
			boolean newBlock = true;
			LinkedList<QName> parentElement = new LinkedList<>();
			try {
				int event = reader.getEventType();
				while (true)
					try {
						switch (event) {
						case START_DOCUMENT:
							writer.setBaseURI(reader.getBaseURI());
							XMLStreamWriterHelper.writeEvent(writer, reader);
							break;
						case START_ELEMENT: {
							QName name = reader.getName();
							parentElement.push(name);
							XMLStreamWriterHelper.writeStartElement(writer, reader);
							XMLStreamWriterHelper.writeAttributes(writer, reader);
							if (!INLINE_ELEMENTS.contains(name.getLocalPart()))
								newBlock = true;
							break; }
						case END_ELEMENT: {
							QName name = parentElement.pop();
							writer.writeEndElement();
							if (!INLINE_ELEMENTS.contains(name.getLocalPart()))
								newBlock = true;
							break; }
						case CHARACTERS:
							if (newBlock) {
								String text = reader.getText();
								if (text.trim().isEmpty()) {
									writer.writeCharacters(text);
								} else if (text.matches("^\\s.*$")) {
									// Place sync point in between any leading white space and actual text.
									String leadingSpace = text.replaceFirst("^(\\s+).*$", "$1");
									writer.writeCharacters(leadingSpace);
									if (insertSyncPoint(writer, parentElement.get(0)))
										newBlock = false;
									writer.writeCharacters(text.substring(leadingSpace.length()));
								} else {
									if (insertSyncPoint(writer, parentElement.get(0)))
										newBlock = false;
									writer.writeCharacters(text);
								}
							} else {
								XMLStreamWriterHelper.writeEvent(writer, reader);
							}
							break;
						default:
							XMLStreamWriterHelper.writeEvent(writer, reader);
						}
						event = reader.next();
					} catch (NoSuchElementException e) {
						break;
					}
				writer.flush();
			} catch (XMLStreamException e) {
				throw new TransformerException(e);
			}
		}

		private static boolean insertSyncPoint(XMLStreamWriter writer, QName parent) throws XMLStreamException {
			if (TEXT_ONLY_ELEMENTS.contains(parent.getLocalPart()))
				return false;
			XMLStreamWriterHelper.writeStartElement(writer, HTML_A.equals(parent) ? HTML_SPAN : HTML_A);
			XMLStreamWriterHelper.writeAttribute(writer, _CLASS, SYNC_CLASS);
			// note that an ID attribute is added in a subsequent step
			writer.writeEndElement();
			return true;
		}
	}
}
