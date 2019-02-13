package org.daisy.pipeline.asciimathml;

import java.io.InputStreamReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.w3c.dom.Element;

public class ASCIIMathML {
	
	private static final Logger logger = LoggerFactory.getLogger(ASCIIMathML.class);
	
	private static final Invocable engine;
	static {
		try {
			ScriptEngine javascriptEngine = new ScriptEngineManager().getEngineByName("javascript");
			logger.debug("Using JavaScript engine: " + javascriptEngine);
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
			logger.error("Failed to load ASCIIMathML.js", e);
			throw new RuntimeException(e); }
	}
	
	public static Element convert(String asciimath) {
		try {
			return (Element)engine.invokeFunction("main", asciimath); }
		catch (ScriptException e) {
			throw new RuntimeException("JavaScript exception was thrown", e); }
		catch (NoSuchMethodException e) {
			throw new RuntimeException("coding error", e); }
	}
}
