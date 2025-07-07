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

public class DTBookInsertSyncPointsStep extends DefaultStep implements XProcStep {

	@Component(
		name = "pxi:dtbook-insert-sync-points",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}dtbook-insert-sync-points" }
	)
	public static class Provider implements XProcStepProvider {
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new DTBookInsertSyncPointsStep(runtime, step);
		}
	}

	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;

	private DTBookInsertSyncPointsStep(XProcRuntime runtime, XAtomicStep step) {
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
			new DTBookInsertSyncPoints()
				.transform(
					new XMLCalabashInputValue(sourcePipe),
					new XMLCalabashOutputValue(resultPipe, runtime))
				.run();
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	private static final String XMLNS_DTB = "http://www.daisy.org/z3986/2005/dtbook/";
	private static final QName DTB_A = new QName(XMLNS_DTB, "a");
	private static final QName DTB_SPAN = new QName(XMLNS_DTB, "span");
	private static final QName _CLASS = new QName("class");
	private static final String SYNC_CLASS = "__tmp__sync__";

	private static final Set<String> BLOCK_ELEMENTS = Sets.newHashSet(
		"book",           "doctitle",      "dl",         "blockquote",
		"frontmatter",    "docauthor",     "dt",         "poem",
		"bodymatter",     "covertitle",    "dd",         "address",
		"rearmatter",     "h1",            "table",      "title",
		"level",          "h2",            "caption",    "author",
		"level1",         "h3",            "thead",      "sidebar",
		"level2",         "h4",            "tfoot",      "note",
		"level3",         "h5",            "tbody",      "annotation",
		"level4",         "h6",            "colgroup",   "epigraph",
		"level5",         "bridgehead",    "col",        "byline",
		"level6",         "hd",            "tr",         "dateline",
		"linegroup",      "list",          "th",         "div",
		"line",           "li",            "td",
		"linenum",        "lic",
		"p",
		// both inline and block
		"img",
		"imggroup",
		// inline but can contain block
		"prodnote");
	private static final Set<String> TEXT_ONLY_ELEMENTS = Sets.newHashSet(
		"annoref",
		"noteref",
		"pagenum",
		"linenum");

	private static class DTBookInsertSyncPoints extends SingleInSingleOutXMLTransformer {

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
							if (BLOCK_ELEMENTS.contains(name.getLocalPart()))
								newBlock = true;
							break; }
						case END_ELEMENT: {
							QName name = parentElement.pop();
							writer.writeEndElement();
							if (BLOCK_ELEMENTS.contains(name.getLocalPart()))
								newBlock = true;
							break; }
						case CHARACTERS:
							if (newBlock) {
								String text = reader.getText();
								if (text.trim().isEmpty()) {
									writer.writeCharacters(text);
								} else if (text.matches("^\\s.*$")) {
									// Place sync point in between any leading white space and actual text.
									// (Note that by doing this, we're actually trimming the white space because it is
									// lost in the transformation to HTML. This is not really a problem, but if it needs
									// fixing, it should be fixed on the side of the HTML transformation, not here.)
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
			XMLStreamWriterHelper.writeStartElement(writer, DTB_A.equals(parent) ? DTB_SPAN : DTB_A);
			XMLStreamWriterHelper.writeAttribute(writer, _CLASS, SYNC_CLASS);
			// note that an ID attribute is added in a subsequent step
			writer.writeEndElement();
			return true;
		}
	}
}
