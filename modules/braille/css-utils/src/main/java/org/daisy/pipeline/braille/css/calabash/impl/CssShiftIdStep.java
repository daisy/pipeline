package org.daisy.pipeline.braille.css.calabash.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.saxon.SaxonBuffer;
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

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssShiftIdStep extends DefaultStep implements XProcStep {
	
	@Component(
		name = "css:shift-id",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/braille-css}shift-id" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new CssShiftIdStep(runtime, step);
		}
	}
	
	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;
	
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final QName CSS_ID = new QName(XMLNS_CSS, "id");
	private static final QName CSS_BOX = new QName(XMLNS_CSS, "box");
	private static final QName CSS__ = new QName(XMLNS_CSS, "_");
	private static final QName _TYPE = new QName("type");
	private static final QName CSS_COUNTER = new QName(XMLNS_CSS, "counter");
	private static final QName _NAME = new QName("name");
	private static final QName _TARGET = new QName("target");
	private static final QName CSS_ANCHOR = new QName(XMLNS_CSS, "anchor");
	
	private CssShiftIdStep(XProcRuntime runtime, XAtomicStep step) {
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
			// Two passes: one for shifting id, second for updating references.
			// Alternative is to combine the two and make use of FutureEvent for forward references.
			Map<String,String> idMap = new TreeMap<String,String>();
			new ShiftIdTransformer(idMap)
			.andThen(new UpdateRefsTransformer(idMap),
			         new SaxonBuffer(runtime.getProcessor().getUnderlyingConfiguration()),
			         false)
			.transform(
				new XMLCalabashInputValue(sourcePipe, runtime),
				new XMLCalabashOutputValue(resultPipe, runtime))
			.run(); }
		catch (Throwable e) {
			throw XProcStep.raiseError(e, step); }
	}
	
	private static class ShiftIdTransformer extends SingleInSingleOutXMLTransformer {
		
		final Map<String,String> idMap;
		
		public ShiftIdTransformer(Map<String,String> idMap) {
			this.idMap = idMap;
		}
		
		public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
			if (source == null || result == null)
				throw new IllegalArgumentException();
			return () -> transform(source.asXMLStreamReader(), result.asXMLStreamWriter());
		}

		public void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter output) throws TransformerException {
			BufferedXMLStreamWriter writer = new BufferedXMLStreamWriter(output);
			boolean insideInlineBox = false;
			Stack<Boolean> blockBoxes = new Stack<Boolean>();
			Stack<Boolean> inlineBoxes = new Stack<Boolean>();
			List<String> pendingId = new ArrayList<String>();
			ShiftedId shiftedId = null;
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
								String id = null;
								for (int i = 0; i < reader.getAttributeCount(); i++) {
									QName name = reader.getAttributeName(i);
									String value = reader.getAttributeValue(i);
									if (CSS_ID.equals(name))
										id = value;
									else {
										if (isBox && _TYPE.equals(name))
											if ("inline".equalsIgnoreCase(value))
												isInlineBox = true;
											else if ("block".equalsIgnoreCase(value))
												isBlockBox = true;
										writeAttribute(writer, name, value); }}
								if (isBlockBox || isInlineBox)
									if (shiftedId != null) {
										shiftedId.render();
										shiftedId = null; }
								if (isInlineBox) {
									if (id != null)
										if (!pendingId.isEmpty())
											pendingId.add(id);
										else
											writeAttribute(writer, CSS_ID, id);
									if (!pendingId.isEmpty()) {
										id = pendingId.get(pendingId.size() - 1);
										for (String oldId : pendingId)
											if (!oldId.equals(id))
												idMap.put(oldId, id);
										pendingId.clear();
										if (id != null)
											writeAttribute(writer, CSS_ID, id); }}
								else if (id != null)
									pendingId.add(id);
								if (isInlineBox)
									insideInlineBox = true; }
							blockBoxes.push(isBlockBox);
							inlineBoxes.push(isInlineBox);
							break; }
						case END_ELEMENT: {
							boolean isBlockBox = blockBoxes.pop();
							boolean isInlineBox = inlineBoxes.pop();
							if (isBlockBox) {
								if (!pendingId.isEmpty()) {
									if (shiftedId == null)
										throw new RuntimeException();
									else
										shiftedId.putAll(pendingId);
									pendingId.clear(); }}
							if (isInlineBox) {
								if (shiftedId != null)
									throw new RuntimeException("coding error");
								shiftedId = new ShiftedId();
								writer.writeEvent(shiftedId); }
							if (isInlineBox)
								insideInlineBox = false;
							writeEvent(writer, reader);
							break; }
						default:
							writeEvent(writer, reader); }
						event = reader.next(); }
					catch (NoSuchElementException e) {
						break; }
				if (!pendingId.isEmpty())
					if (shiftedId == null)
						throw new RuntimeException("invalid input");
					else
						shiftedId.putAll(pendingId);
				if (shiftedId != null)
					shiftedId.render();
				writer.flush(); }
			catch (XMLStreamException e) {
				throw new TransformerException(e); }
		}
		
		private class ShiftedId implements FutureWriterEvent {
			
			private List<String> ids;
			private boolean ready = false;
			
			private void put(String id) {
				if (this.ids == null)
					this.ids = new ArrayList<String>();
				this.ids.add(id);
			}
			
			private void putAll(List<String> ids) {
				for (String id : ids) put(id);
			}
			
			private void render() {
				ready = true;
			}
			
			public void writeTo(XMLStreamWriter writer) throws XMLStreamException {
				if (!ready)
					throw new XMLStreamException("not ready");
				if (ids != null) {
					String id = ids.get(0);
					for (String oldId : ids)
						if (!oldId.equals(id))
							idMap.put(oldId, id);
					writeStartElement(writer, CSS__);
					writeAttribute(writer, CSS_ID, id);
					writer.writeEndElement(); }
			}
			
			public boolean isReady() {
				return ready;
			}
		}
	}
	
	private static class UpdateRefsTransformer extends SingleInSingleOutXMLTransformer {
		
		final Map<String,String> idMap;
		
		public UpdateRefsTransformer(Map<String,String> idMap) {
			this.idMap = idMap;
		}
		
		public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params) throws IllegalArgumentException {
			if (source == null || result == null)
				throw new IllegalArgumentException();
			return () -> transform(source.asXMLStreamReader(), result.asXMLStreamWriter());
		}
		
		void transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter output) throws TransformerException {
			BufferedXMLStreamWriter writer = new BufferedXMLStreamWriter(output);
			try {
				while (true)
					try {
						int event = reader.getEventType();
						switch (event) {
						case START_ELEMENT: {
							writeEvent(writer, reader);
							QName elemName = reader.getName();
							boolean isCounter = CSS_COUNTER.equals(elemName);
							String counterTarget = null;
							String anchor = null;
							for (int i = 0; i < reader.getAttributeCount(); i++) {
								QName attrName = reader.getAttributeName(i);
								String attrValue = reader.getAttributeValue(i);
								if (CSS_ANCHOR.equals(attrName))
									anchor = attrValue;
								else if (isCounter && _TARGET.equals(attrName)) {
									counterTarget = attrValue;
								} else {
									writeAttribute(writer, attrName, attrValue);
								}
							}
							if (anchor != null) {
								if (idMap.containsKey(anchor)) {
									anchor = idMap.get(anchor);
								}
								writeAttribute(writer, CSS_ANCHOR, anchor);
							}
							if (counterTarget != null) {
								if (idMap.containsKey(counterTarget)) {
									counterTarget = idMap.get(counterTarget);
								}
								writeAttribute(writer, _TARGET, counterTarget);
							}
							break; }
						default:
							writeEvent(writer, reader); }
						reader.next(); }
					catch (NoSuchElementException e) {
						break; }
				writer.flush(); }
			catch (XMLStreamException e) {
				throw new TransformerException(e); }
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CssShiftIdStep.class);
	
}
