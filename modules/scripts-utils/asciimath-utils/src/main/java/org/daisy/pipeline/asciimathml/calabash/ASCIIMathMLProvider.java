package org.daisy.pipeline.asciimathml.calabash;

import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

public class ASCIIMathMLProvider implements XProcStepProvider {
	
	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new ASCIIMathML(runtime, step);
	}
	
	private static class ASCIIMathML extends DefaultStep {
		
		private static final Logger logger = LoggerFactory.getLogger(ASCIIMathML.class);
		
		private static final QName _asciimath = new QName("asciimath");
	
		private static Invocable engine;
	
		static {
			
			try {
				ScriptEngine javascriptEngine = new ScriptEngineManager().getEngineByName("javascript");
				javascriptEngine.put("document",
				                     DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
				javascriptEngine.eval(new InputStreamReader(
					                      ASCIIMathML.class.getResource("/javascript/ASCIIMathML.js").openStream()));
				javascriptEngine.eval(
					"initSymbols();                                                                      \n" +
					"main = function(ascii) {                                                            \n" +
					"    var content = AMparseExpr(ascii, false)[0];                                     \n" +
					"    content = content.length == 1 ? content : createMmlNode(\"mrow\", content);     \n" +
					"    var semantics = createMmlNode(\"semantics\", content);                          \n" +
					"    var annotation = createMmlNode(\"annotation\", document.createTextNode(ascii)); \n" +
					"    annotation.setAttribute(\"encoding\", \"ASCIIMath\");                           \n" +
					"    semantics.appendChild(annotation);                                              \n" +
					"    return createMmlNode(\"math\", semantics);                                      \n" +
					"}");
				engine = (Invocable)javascriptEngine; }
			catch (Exception e) {
				logger.error("Failed loading ASCIIMathML.js", e);
				throw new RuntimeException(e); }
		}
		
		private WritablePipe result;
		
		private ASCIIMathML(XProcRuntime runtime, XAtomicStep step) {
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
					new StreamSource(new StringReader(Serializer.serialize(
						(Element)engine.invokeFunction("main", ascii)))))); }
			catch (Exception e) {
				logger.error("px:asciimathml failed", e);
				throw new XProcException(step.getNode(), e); }
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
