package org.daisy.pipeline.braille.dotify.calabash.impl;

import java.util.Map;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

public class OBFLNormalizeSpaceStep extends DefaultStep implements XProcStep {

	private final XMLInputFactory xmlInputFactory;
	private final XMLEventFactory xmlEventFactory;
	private ReadablePipe source = null;
	private WritablePipe result = null;

	public OBFLNormalizeSpaceStep(XProcRuntime runtime, XAtomicStep step, XMLInputFactory xmlInputFactory, XMLEventFactory xmlEventFactory) {
		super(runtime, step);
		this.xmlInputFactory = xmlInputFactory;
		this.xmlEventFactory = xmlEventFactory;
	}

	@Override
	public void setInput(String port, ReadablePipe pipe) {
		source = pipe;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}

	@Override
	public void reset() {
		source.resetReader();
		result.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			new OBFLSpaceNormalizer()
			.transform(
				new XMLCalabashInputValue(source),
				new XMLCalabashOutputValue(result, runtime))
			.run(); }
		catch (Throwable e) {
			throw XProcStep.raiseError(e, step); }
	}

	private class OBFLSpaceNormalizer extends SingleInSingleOutXMLTransformer {

		public Runnable transform(XMLInputValue<?> source, XMLOutputValue<?> result, InputValue<?> params)
				throws IllegalArgumentException {
			if (source == null || result == null)
				throw new IllegalArgumentException();
			return transform(source.ensureSingleItem().asXMLStreamReader(), result.asXMLStreamWriter());
		}

		Runnable transform(BaseURIAwareXMLStreamReader reader, BaseURIAwareXMLStreamWriter writer) {
			return () -> {
				try {
					writer.setBaseURI(reader.getBaseURI());
					new OBFLWsNormalizer(xmlEventFactory)
						.transform(xmlInputFactory.createXMLEventReader(reader),
						           XMLStreamWriterHelper.asXMLEventWriter(writer))
						.run();
				} catch (XMLStreamException e) {
					throw new TransformerException(e);
				}
			};
		}
	}

	@Component(
		name = "pxi:obfl-normalize-space-2",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}obfl-normalize-space-2" }
	)
	public static class Provider implements XProcStepProvider  {

		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new OBFLNormalizeSpaceStep(runtime, step, xmlInputFactory, xmlEventFactory);
		}

		private XMLInputFactory xmlInputFactory;

		@Reference(
			name = "xml-input-factory",
			unbind = "-",
			service = XMLInputFactory.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void setXMLInputFactory(XMLInputFactory factory) {
			xmlInputFactory = factory;
		}

		private XMLEventFactory xmlEventFactory;

		@Reference(
			name = "xml-event-factory",
			unbind = "-",
			service = XMLEventFactory.class,
			cardinality = ReferenceCardinality.MANDATORY,
			policy = ReferencePolicy.STATIC
		)
		protected void setXMLEventFactory(XMLEventFactory factory) {
			xmlEventFactory = factory;
		}
	}
}
