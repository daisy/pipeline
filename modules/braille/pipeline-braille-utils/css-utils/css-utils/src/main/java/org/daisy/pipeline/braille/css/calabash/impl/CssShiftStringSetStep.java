package org.daisy.pipeline.braille.css.calabash.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import cz.vutbr.web.css.Term;
import cz.vutbr.web.css.Term.Operator;
import cz.vutbr.web.css.TermList;
import cz.vutbr.web.css.TermPair;
import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.braille.css.BrailleCSSProperty.StringSet;
import org.daisy.braille.css.PropertyValue;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.braille.common.saxon.StreamToStreamTransform;
import org.daisy.pipeline.braille.common.TransformationException;
import static org.daisy.pipeline.braille.common.util.Strings.join;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CssShiftStringSetStep extends DefaultStep {
	
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
			List<TermPair<String,TermList>> pendingStringSet = new ArrayList<TermPair<String,TermList>>();
			while (sourcePipe.moreDocuments()) {
				XdmNode source = sourcePipe.read();
				resultPipe.write(
					new CssShiftStringSetTransform(runtime.getConfiguration().getProcessor().getUnderlyingConfiguration(), pendingStringSet)
					.transform(source.getUnderlyingNode())); }}
		catch (Exception e) {
			logger.error("css:shift-string-set failed", e);
			throw new XProcException(step.getNode(), e); }
	}
	
	private static class CssShiftStringSetTransform extends StreamToStreamTransform {
		
		private final List<TermPair<String,TermList>> pendingStringSet;
		
		public CssShiftStringSetTransform(Configuration configuration, List<TermPair<String,TermList>> pendingStringSet) {
			super(configuration);
			this.pendingStringSet = pendingStringSet;
		}
		
		protected void _transform(XMLStreamReader reader, Writer writer) throws TransformationException {
			int depth = 0;
			boolean insideInlineBox = false;
			int inlineBoxDepth = 0;
			while (true)
				try {
					switch (reader.next()) {
					case START_DOCUMENT:
						writer.writeStartDocument();
						break;
					case END_DOCUMENT:
						writer.writeEndDocument();
						break;
					case START_ELEMENT:
						depth++;
						writer.copyStartElement(reader);
						boolean isBox = CSS_BOX.equals(reader.getName());
						boolean isInlineBox = false;
						String stringSet = null;
						for (int i = 0; i < reader.getAttributeCount(); i++) {
							QName name = reader.getAttributeName(i);
							String value = reader.getAttributeValue(i);
							if (CSS_STRING_SET.equals(name))
								stringSet = value;
							else {
								if (isBox && _TYPE.equals(name) && "inline".equals(value))
									isInlineBox = true;
								writer.writeAttribute(name, value); }}
						if (isBox || insideInlineBox) {
							if (stringSet != null)
								if (!pendingStringSet.isEmpty())
									parseStringSet(stringSet, pendingStringSet);
								else
									writer.writeAttribute(CSS_STRING_SET, stringSet);
							if (!pendingStringSet.isEmpty()) {
								stringSet = serializeStringSet(pendingStringSet);
								pendingStringSet.clear();
								if (stringSet != null)
									writer.writeAttribute(CSS_STRING_SET, stringSet); }}
						else if (stringSet != null) {
							parseStringSet(stringSet, pendingStringSet); }
						if (!insideInlineBox && isInlineBox) {
							insideInlineBox = true;
							inlineBoxDepth = depth; }
						break;
					case END_ELEMENT:
						writer.writeEndElement();
						if (inlineBoxDepth == depth)
							insideInlineBox = false;
						depth--;
						break;
					case SPACE:
					case CHARACTERS:
						writer.copyText(reader);
						break;
					case PROCESSING_INSTRUCTION:
						writer.copyPI(reader);
						break;
					case CDATA:
						writer.copyCData(reader);
						break;
					case COMMENT:
						writer.copyComment(reader);
						break;
					}}
				catch (NoSuchElementException e) {
					break; }
				catch (XMLStreamException e) {
					throw new TransformationException(e); }
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
		return join(stringSet, ", ", CssInlineStep.termToString);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(CssShiftStringSetStep.class);
	
}
