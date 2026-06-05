package org.daisy.pipeline.asciimathml.calabash.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;
import org.daisy.pipeline.asciimathml.ASCIIMathML;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

@Component(
	name = "px:asciimath-to-mathml",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc}asciimath-to-mathml" }
)
public class ASCIIMathToMathMLProvider implements XProcStepProvider {
	
	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
		return new ASCIIMathToMathMLStep(runtime, step);
	}
	
	private static class ASCIIMathToMathMLStep extends DefaultStep implements XProcStep {
		
		private static final Logger logger = LoggerFactory.getLogger(ASCIIMathToMathMLStep.class);
		
		private static final QName _asciimath = new QName("asciimath");
	
		private WritablePipe result;
		
		private ASCIIMathToMathMLStep(XProcRuntime runtime, XAtomicStep step) {
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
			super.run();
			try {
				String ascii = getOption(_asciimath).getString().replaceAll("^`", "").replaceAll("`$", "");
				logger.info("Translating `" + ascii + "` to MathML ...");
				result.write(runtime.getProcessor().newDocumentBuilder().build(
					new StreamSource(new StringReader(Serializer.serialize(ASCIIMathML.convert(ascii)))))); }
			catch (Throwable e) {
				throw XProcStep.raiseError(e, step); }
		}
	}
	
	private static class Serializer {
		private static Transformer transformer;
		private static String serialize(Element element) {
			try {
				if (transformer == null) {
					transformer = TransformerFactory.newInstance().newTransformer();
					transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); }
				StringWriter writer = new StringWriter();
				transformer.transform(new DOMSource(element), new StreamResult(writer));
				return writer.toString(); }
			catch (Exception e) {
				throw new RuntimeException(e); }
		}
	}
}
