package org.daisy.dotify.impl.input.xml;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.daisy.braille.pef.XMLFileCompare;
import org.daisy.dotify.api.tasks.InternalTaskException;
import org.daisy.dotify.api.tasks.TaskGroup;
import org.daisy.dotify.api.tasks.TaskGroupSpecification;
import org.daisy.dotify.api.tasks.TaskSystemException;
import org.daisy.dotify.impl.input.XsltTask;
import org.daisy.dotify.tasks.runner.TaskRunner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TestHelper {
	private static DocumentBuilder db = null;
	
	private static DocumentBuilder getDefaultDocumentBuilder() {
		if (db==null) {
			try {
				db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
		}
		return db;
	}
	
	private final static NamespaceContext obflContext = new NamespaceContext(){
		private static final String OBFL_NS = "http://www.daisy.org/ns/2011/obfl";
		private static final String OBFL_PREFIX = "obfl";

		@Override
		public String getNamespaceURI(String prefix) {
			if (OBFL_PREFIX.equals(prefix)) {
				return OBFL_NS;
			}
			return null;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			if (OBFL_NS.equals(namespaceURI)) {
				return OBFL_PREFIX;
			}
			return null;
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			if (OBFL_NS.equals(namespaceURI)) {
				return Arrays.asList(new String[]{OBFL_PREFIX}).iterator();
			}
			return null;
		}};
		
	public static File toObfl(String inputPath) throws IOException, TaskSystemException, URISyntaxException {
		return toObfl(inputPath, false);
	}
	
	public static File toObfl(String inputPath, boolean keep) throws IOException, TaskSystemException, URISyntaxException {
		File f = File.createTempFile(TestHelper.class.getClass().getCanonicalName(), ".tmp");
		if (keep) {
			System.err.println(f);
		} else {
			f.deleteOnExit();
		}
		TestHelper.toObfl(inputPath, f, "sv-SE", new HashMap<String, Object>());
		return f;
	}

	public static void toObfl(String srcPath, File res, String lang, Map<String, Object> parameters) throws IOException, TaskSystemException, URISyntaxException {
		TaskGroup tg = new XMLInputManagerFactory().newTaskGroup(new TaskGroupSpecification("xml", "obfl", lang));
		TaskRunner tr = new TaskRunner.Builder().build();
		File src = new File(TestHelper.class.getResource(srcPath).toURI());
		tr.runTasks(src, res, tg.compile(parameters));
	}

	public static void runXSLT(String input, String xslt, String expected, Map<String, Object> options, boolean keep) throws IOException, InternalTaskException, URISyntaxException{
		File actual = File.createTempFile(TestHelper.class.getName(), ".tmp");
		try {
			XsltTask task = new XsltTask("XSLT Test", TestHelper.class.getResource(xslt), options);
			File src = new File(TestHelper.class.getResource(input).toURI());
			task.execute(src, actual);
			XMLFileCompare c = new XMLFileCompare(TransformerFactory.newInstance());
			try {
				assertTrue(c.compareXML(new FileInputStream(actual), TestHelper.class.getResourceAsStream(expected)));
			} catch (TransformerException e) {
				e.printStackTrace();
				fail();
			}
		} finally {
			if (!keep) {
				if (!actual.delete()) {
					actual.deleteOnExit();
				}
			} else {
				System.out.println("Result file is: " + actual);
			}
		}
	}
	
	public static void assertEqualPath(InputStream expected, InputStream actual, String path, NamespaceContext nc) throws ParserConfigurationException, XPathExpressionException, SAXException, IOException {
		assertEqualPath(getDefaultDocumentBuilder().parse(expected), getDefaultDocumentBuilder().parse(actual), path, nc);
	}
	
	public static void assertEqualPath(Document expected, Document actual, String path, NamespaceContext nc) throws XPathExpressionException {
		assertTrue(getNode(normalize(actual), path, nc).isEqualNode(getNode(normalize(expected), path, nc)));
	}
	
	private static Node getNode(Node d, String xpath, NamespaceContext nc) throws XPathExpressionException {
		XPathFactory xpf = XPathFactory.newInstance();
		XPath xp = xpf.newXPath();
		xp.setNamespaceContext(nc);
		return (Node)xp.evaluate(xpath, d, XPathConstants.NODE);
	}
	
	private static Document normalize(Document d) {
		d.normalizeDocument();
		return d;
	}
	
	/**
	 * Gets a Namespace context with the prefix obfl and corresponding namespace URI.
	 * @return returns a namespace context
	 */
	public static NamespaceContext getObflNamespaceContext() {
		return obflContext;
	}

}
