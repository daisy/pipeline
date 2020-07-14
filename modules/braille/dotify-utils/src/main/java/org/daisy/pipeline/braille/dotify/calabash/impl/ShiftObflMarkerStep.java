package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.base.Splitter;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.stax.BaseURIAwareXMLStreamReader;
import org.daisy.common.stax.BaseURIAwareXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper.BufferedXMLStreamWriter;
import org.daisy.common.stax.XMLStreamWriterHelper.FutureWriterEvent;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttributes;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeEvent;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.xproc.calabash.XMLCalabashInputValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import static org.daisy.pipeline.braille.common.util.Strings.join;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiftObflMarkerStep extends DefaultStep implements XProcStep {
	
	@Component(
		name = "pxi:shift-obfl-marker",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}shift-obfl-marker" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new ShiftObflMarkerStep(runtime, step);
		}
	}
	
	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;
	
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final QName CSS_OBFL_MARKER = new QName(XMLNS_CSS, "_obfl-marker");
	private static final QName CSS_BOX = new QName(XMLNS_CSS, "box");
	private static final QName CSS__ = new QName(XMLNS_CSS, "_");
	private static final QName _TYPE = new QName("type");
	
	private ShiftObflMarkerStep(XProcRuntime runtime, XAtomicStep step) {
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
		super.run();
		try {
			new ShiftObflMarkerTransform()
			.transform(
				new XMLCalabashInputValue(sourcePipe, runtime),
				new XMLCalabashOutputValue(resultPipe, runtime))
			.run(); }
		catch (Exception e) {
			logger.error("pxi:shift-obfl-marker failed", e);
			throw new XProcException(step.getNode(), e); }
	}
	
	private static class ShiftObflMarkerTransform extends SingleInSingleOutXMLTransformer {
		
		public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
			if (source == null || result == null)
				throw new IllegalArgumentException();
			return () -> transform(source.ensureSingleItem().asXMLStreamReader(), result.asXMLStreamWriter());
		}
		
		void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter output) throws TransformerException {
			BufferedXMLStreamWriter writer = new BufferedXMLStreamWriter(output);
			boolean insideInlineBox = false;
			Stack<Boolean> blockBoxes = new Stack<Boolean>();
			Stack<Boolean> inlineBoxes = new Stack<Boolean>();
			List<String> pendingMarker = new ArrayList<String>();
			ShiftedMarker shiftedMarker = null;
			try {
				int event = reader.getEventType();
				while (true)
					try {
						switch (event) {
						case START_ELEMENT: {
							writeEvent(writer, reader);
							boolean isInlineBox = false;
							boolean isBlockBox = false;
							if (insideInlineBox)
								writeAttributes(writer, reader);
							else {
								boolean isBox = CSS_BOX.equals(reader.getName());
								String marker = null;
								for (int i = 0; i < reader.getAttributeCount(); i++) {
									QName name = reader.getAttributeName(i);
									String value = reader.getAttributeValue(i);
									if (CSS_OBFL_MARKER.equals(name))
										marker = value;
									else {
										if (isBox && _TYPE.equals(name))
											if ("inline".equalsIgnoreCase(value))
												isInlineBox = true;
											else if ("block".equalsIgnoreCase(value))
												isBlockBox = true;
										writeAttribute(writer, name, value); }}
								if (isBlockBox || isInlineBox)
									if (shiftedMarker != null) {
										shiftedMarker.render();
										shiftedMarker = null; }
								if (isInlineBox) {
									if (marker != null)
										if (!pendingMarker.isEmpty())
											parseMarker(marker, pendingMarker);
										else
											writeAttribute(writer, CSS_OBFL_MARKER, marker);
									if (!pendingMarker.isEmpty()) {
										marker = serializeMarker(pendingMarker);
										pendingMarker.clear();
										if (marker != null)
											writeAttribute(writer, CSS_OBFL_MARKER, marker); }}
								else if (marker != null)
									parseMarker(marker, pendingMarker);
								if (isInlineBox)
									insideInlineBox = true; }
							blockBoxes.push(isBlockBox);
							inlineBoxes.push(isInlineBox);
							break; }
						case END_ELEMENT: {
							boolean isBlockBox = blockBoxes.pop();
							boolean isInlineBox = inlineBoxes.pop();
							if (isBlockBox) {
								if (!pendingMarker.isEmpty()) {
									if (shiftedMarker == null)
										throw new RuntimeException();
									else
										shiftedMarker.putAll(pendingMarker);
									pendingMarker.clear(); }}
							if (isInlineBox) {
								if (shiftedMarker != null)
									throw new RuntimeException("coding error");
								shiftedMarker = new ShiftedMarker();
								writer.writeEvent(shiftedMarker); }
							if (isInlineBox)
								insideInlineBox = false;
							writeEvent(writer, reader);
							break; }
						default:
							writeEvent(writer, reader); }
						event = reader.next(); }
					catch (NoSuchElementException e) {
						break; }
				if (!pendingMarker.isEmpty())
					if (shiftedMarker == null)
						throw new RuntimeException("invalid input");
					else
						shiftedMarker.putAll(pendingMarker);
				if (shiftedMarker != null)
					shiftedMarker.render();
				writer.flush(); }
			catch (XMLStreamException e) {
				throw new TransformerException(e); }
		}
		
		private static class ShiftedMarker implements FutureWriterEvent {
			
			private List<String> marker;
			private boolean ready = false;
			
			private void put(String marker) {
				if (this.marker == null)
					this.marker = new ArrayList<String>();
				this.marker.add(marker);
			}
			
			private void putAll(List<String> marker) {
				for (String m : marker) put(m);
			}
			
			private void render() {
				ready = true;
			}
			
			public void writeTo(XMLStreamWriter writer) throws XMLStreamException {
				if (!ready)
					throw new XMLStreamException("not ready");
				if (marker != null) {
					writeStartElement(writer, CSS__);
					writeAttribute(writer, CSS_OBFL_MARKER, serializeMarker(marker));
					writer.writeEndElement(); }
			}
			
			public boolean isReady() {
				return ready;
			}
		}
	}
	
	private static final Splitter markerSplitter = Splitter.on(' ').omitEmptyStrings();
	
	private static void parseMarker(String value, List<String> appendTo) {
		for (String m : markerSplitter.split(value))
			appendTo.add(m);
	}
	
	private static String serializeMarker(List<String> marker) {
		if (marker.isEmpty())
			return null;
		return join(marker, " ");
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ShiftObflMarkerStep.class);
	
}
