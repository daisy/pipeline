package org.daisy.pipeline.file.calabash.impl;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import static org.daisy.common.saxon.SaxonHelper.jaxpQName;
import org.daisy.common.transform.TransformerException;
import org.daisy.common.xml.DocumentBuilder;
import org.daisy.common.xproc.calabash.XMLCalabashOutputValue;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;

@Component(
	name = "px:load",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}load" }
)
public class LoadProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		return new Load(runtime, step);
	}

	private static final QName _href = new QName("href");
	private static final QName _content_type = new QName("content-type");
	private static final QName err_XD0011 = new QName("err", XProcConstants.NS_XPROC_ERROR, "XD0011");

	public class Load extends DefaultStep implements XProcStep {

		private WritablePipe result = null;

		public Load(XProcRuntime runtime, XAtomicStep step) {
			super(runtime,step);
		}

		public void setOutput(String port, WritablePipe pipe) {
			result = pipe;
		}

		public void reset() {
			result.resetWriter();
		}

		public void run() throws SaxonApiException {
			super.run();
			try {
				RuntimeValue href = getOption(_href);
				URI absoluteHref = href.getBaseURI().resolve(href.getString());
				InputSource source = new InputSource(absoluteHref.toString());
				String contentType = getOption(_content_type, "");
				boolean isXml = "".equals(contentType) || contentType.matches("[^ ]*(/|\\+)xml");
				Document doc = null;
				DocumentBuilder xmlParser = null;
				try {
					for (DocumentBuilder p : parsers) {
						if (xmlParser == null && p.supportsContentType("text/xml")) {
							xmlParser = p;
							if (isXml) break;
						}
						if (!isXml && p.supportsContentType(contentType)) {
							doc = p.parse(source);
							break;
						}
					}
					if (doc == null && xmlParser != null) {
						// content-type is either XML, not specified, or not recognized
						// in all cases we use (fallback to) the XML parser
						doc = xmlParser.parse(source);
					}
				} catch (SAXException|IOException e) {
					throw new TransformerException(
						jaxpQName(err_XD0011),
						new IllegalArgumentException("Error parsing " + absoluteHref + " (content-type: " + contentType + ")", e));
				}
				if (doc == null) {
					// this means no XML parser was found
					if (xmlParser != null) throw new RuntimeException(); // coding error
					throw new TransformerException(new IllegalStateException("No XML parser found"));
				}
				XMLCalabashOutputValue.of(result, runtime).asNodeConsumer().accept(doc);
			} catch (Throwable e) {
				throw XProcStep.raiseError(e, step);
			}
		}
	}

	private final List<DocumentBuilder> parsers = new ArrayList<>();

	@Reference(
		name = "input-parser",
		unbind = "-",
		service = DocumentBuilder.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	protected void addInputParser(DocumentBuilder parser) {
		parsers.add(parser);
	}
}
