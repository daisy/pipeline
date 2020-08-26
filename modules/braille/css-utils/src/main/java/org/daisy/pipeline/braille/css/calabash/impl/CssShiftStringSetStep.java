package org.daisy.pipeline.braille.css.calabash.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;

import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.PropertyValue;
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
import org.daisy.pipeline.braille.common.util.Strings;
import org.daisy.pipeline.braille.css.impl.BrailleCssSerializer;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssShiftStringSetStep extends DefaultStep implements XProcStep {
	
	@Component(
		name = "css:shift-string-set",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/braille-css}shift-string-set" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new CssShiftStringSetStep(runtime, step);
		}
	}
	
	private ReadablePipe sourcePipe = null;
	private WritablePipe resultPipe = null;
	
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	private static final QName CSS_STRING_SET = new QName(XMLNS_CSS, "string-set");
	private static final QName CSS_BOX = new QName(XMLNS_CSS, "box");
	private static final QName CSS__ = new QName(XMLNS_CSS, "_");
	private static final QName _TYPE = new QName("type");
	
	private CssShiftStringSetStep(XProcRuntime runtime, XAtomicStep step) {
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
			new CssShiftStringSetTransformer()
			.transform(
				new XMLCalabashInputValue(sourcePipe, runtime),
				new XMLCalabashOutputValue(resultPipe, runtime))
			.run(); }
		catch (Exception e) {
			logger.error("css:shift-string-set failed", e);
			throw new XProcException(step.getNode(), e); }
	}
	
	private static class CssShiftStringSetTransformer extends SingleInSingleOutXMLTransformer {
		
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
			List<TermPair<String,TermList>> pendingStringSet = new ArrayList<TermPair<String,TermList>>();
			ShiftedStringSet shiftedStringSet = null;
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
								String stringSet = null;
								for (int i = 0; i < reader.getAttributeCount(); i++) {
									QName name = reader.getAttributeName(i);
									String value = reader.getAttributeValue(i);
									if (CSS_STRING_SET.equals(name))
										stringSet = value;
									else {
										if (isBox && _TYPE.equals(name))
											if ("inline".equalsIgnoreCase(value))
												isInlineBox = true;
											else if ("block".equalsIgnoreCase(value))
												isBlockBox = true;
										writeAttribute(writer, name, value); }}
								if (isBlockBox || isInlineBox)
									if (shiftedStringSet != null) {
										shiftedStringSet.render();
										shiftedStringSet = null; }
								if (isInlineBox) {
									if (stringSet != null)
										if (!pendingStringSet.isEmpty())
											parseStringSet(stringSet, pendingStringSet);
										else
											writeAttribute(writer, CSS_STRING_SET, stringSet);
									if (!pendingStringSet.isEmpty()) {
										stringSet = serializeStringSet(pendingStringSet);
										pendingStringSet.clear();
										if (stringSet != null)
											writeAttribute(writer, CSS_STRING_SET, stringSet); }}
								else if (stringSet != null)
									parseStringSet(stringSet, pendingStringSet);
								if (isInlineBox)
									insideInlineBox = true; }
							blockBoxes.push(isBlockBox);
							inlineBoxes.push(isInlineBox);
							break; }
						case END_ELEMENT: {
							boolean isBlockBox = blockBoxes.pop();
							boolean isInlineBox = inlineBoxes.pop();
							if (isBlockBox) {
								if (!pendingStringSet.isEmpty()) {
									if (shiftedStringSet == null)
										throw new RuntimeException();
									else
										shiftedStringSet.putAll(pendingStringSet);
									pendingStringSet.clear(); }}
							if (isInlineBox) {
								if (shiftedStringSet != null)
									throw new RuntimeException("coding error");
								shiftedStringSet = new ShiftedStringSet();
								writer.writeEvent(shiftedStringSet); }
							if (isInlineBox)
								insideInlineBox = false;
							writeEvent(writer, reader);
							break; }
						default:
							writeEvent(writer, reader); }
						event = reader.next(); }
					catch (NoSuchElementException e) {
						break; }
				if (!pendingStringSet.isEmpty())
					if (shiftedStringSet == null)
						throw new RuntimeException("invalid input");
					else
						shiftedStringSet.putAll(pendingStringSet);
				if (shiftedStringSet != null)
					shiftedStringSet.render();
				writer.flush(); }
			catch (XMLStreamException e) {
				throw new TransformerException(e); }
		}
		
		private static class ShiftedStringSet implements FutureWriterEvent {
			
			private List<TermPair<String,TermList>> stringSet;
			private boolean ready = false;
			
			private ShiftedStringSet() {
			}
			
			private void put(TermPair<String,TermList> stringSet) {
				if (this.stringSet == null)
					this.stringSet = new ArrayList<TermPair<String,TermList>>();
				this.stringSet.add(stringSet);
			}
			
			private void putAll(List<TermPair<String,TermList>> stringSet) {
				for (TermPair<String,TermList> s : stringSet)
					put(s);
			}
			
			private void render() {
				ready = true;
			}
			
			public void writeTo(XMLStreamWriter writer) throws XMLStreamException {
				if (!ready)
					throw new XMLStreamException("not ready");
				if (stringSet != null) {
					String value = serializeStringSet(stringSet);
					if (value != null)
						writeStartElement(writer, CSS__);
						writeAttribute(writer, CSS_STRING_SET, value);
						writer.writeEndElement(); }
			}
			
			public boolean isReady() {
				return ready;
			}
		}
	}
	
	private static void parseStringSet(String value, List<TermPair<String,TermList>> appendTo) {
		PropertyValue decl = PropertyValue.parse("string-set", value);
		if (decl != null) {
			StringSet stringSet = (StringSet)decl.getProperty();
			switch (stringSet) {
			case INHERIT:
				throw new RuntimeException("'string-set: inherit' not supported");
			case list_values:
				for (Term<?> t : (TermList)decl.getValue())
					appendTo.add((TermPair<String,TermList>)t);
				break;
			case NONE:
				break; }}
	}
	
	private static String serializeStringSet(List<TermPair<String,TermList>> stringSet) {
		if (stringSet.isEmpty())
			return null;
		return Strings.join(stringSet, ", ", BrailleCssSerializer::toString);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CssShiftStringSetStep.class);
	
}
