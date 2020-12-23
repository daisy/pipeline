package org.daisy.pipeline.braille.common.calabash.impl;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import static com.xmlcalabash.core.XProcConstants.NS_XPROC_STEP;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.SaxonApiException;

import static org.daisy.common.stax.XMLStreamWriterHelper.writeAttribute;
import static org.daisy.common.stax.XMLStreamWriterHelper.writeStartElement;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.calabash.XMLCalabashOptionValue;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.pipeline.braille.common.Query;
import static org.daisy.pipeline.braille.common.Query.util.query;

import org.osgi.service.component.annotations.Component;

public class PxParseQueryStep extends DefaultStep implements XProcStep {

	@Component(
		name = "px:parse-query",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}parse-query" }
	)
	public static class StepProvider implements XProcStepProvider {
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
			return new PxParseQueryStep(runtime, step);
		}
	}

	private WritablePipe result = null;
	private static final net.sf.saxon.s9api.QName _QUERY = new net.sf.saxon.s9api.QName("query");

	private PxParseQueryStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}

	@Override
	public void reset() {
		result.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		try {
			serializeQuery(query(new XMLCalabashOptionValue(getOption(_QUERY)).toString()),
			               new XMLCalabashOutputValue(result, runtime).asXMLStreamWriter());
		} catch (Exception e) {
			logger.error("px:parse-query failed", e);
			throw new XProcException(step.getNode(), e);
		}
		super.run();
	}

	private static final QName C_PARAM_SET = new QName(NS_XPROC_STEP, "param-set", "c");
	private static final QName C_PARAM = new QName(NS_XPROC_STEP, "param", "c");
	private static final QName _NAME = new QName("name");
	private static final QName _NAMESPACE = new QName("namespace");
	private static final QName _VALUE = new QName("value");

	private static void serializeQuery(Query query, XMLStreamWriter w) throws XMLStreamException {
		writeStartElement(w, C_PARAM_SET);
		for (Query.Feature f : query) {
			writeStartElement(w, C_PARAM);
			writeAttribute(w, _NAME, f.getKey());
			writeAttribute(w, _NAMESPACE, "");
			writeAttribute(w, _VALUE, f.getValue().orElse("true"));
			w.writeEndElement();
		}
		w.writeEndElement();
	}
}
