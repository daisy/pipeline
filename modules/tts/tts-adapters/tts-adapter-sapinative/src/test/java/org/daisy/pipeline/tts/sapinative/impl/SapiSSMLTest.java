package org.daisy.pipeline.tts.sapinative.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.custommonkey.xmlunit.Diff;
import org.daisy.common.xslt.ThreadUnsafeXslTransformer;
import org.daisy.common.xslt.XslTransformCompiler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.xmlcalabash.util.TreeWriter;

public class SapiSSMLTest {

	private ThreadUnsafeXslTransformer Transformer;
	private static Processor Proc = new Processor(false);
	private static String SsmlNs = "http://www.w3.org/2001/10/synthesis";

	@Before
	public void setUp() throws SaxonApiException {
		InputStream is = this.getClass().getResourceAsStream("/transform-ssml.xsl");
		Transformer = new XslTransformCompiler(Proc.getUnderlyingConfiguration())
		        .compileStylesheet(is).newTransformer();
	}

	@Test
	public void completeSSML() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String endingmark = "emark";
		String voice = "john";

		TreeWriter tw = new TreeWriter(Proc);
		tw.startDocument(new URI("http://test"));
		tw.startContent();
		tw.addStartElement(new QName(SsmlNs, "speak"));
		tw.addStartElement(new QName(SsmlNs, "s"));
		tw.addStartElement(new QName(SsmlNs, "y"));
		tw.addAttribute(new QName(null, "attr"), "attr-val");
		tw.addEndElement();
		tw.addStartElement(new QName(SsmlNs, "token"));
		tw.addText("this");
		tw.addEndElement();
		tw.addText(" is text");
		tw.addEndElement();

		Map<String, Object> params = new TreeMap<String, Object>();
		params.put("ending-mark", endingmark);
		params.put("voice", voice);

		String result = Transformer.transformToString(tw.getResult(), params);
		String expected = "<s:speak xmlns:s=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s>"
		        + "<s:y attr=\"attr-val\"/>this is text</s:s><s:break time=\"250ms\"/><bookmark mark=\""
		        + endingmark + "\"/></s:speak>";

		Diff d = new Diff(result, expected);

		Assert.assertTrue(d.similar());
	}

	@Test
	public void incompleteSSML() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String endingmark = "emark";
		String voice = "john";

		TreeWriter tw = new TreeWriter(Proc);
		tw.startDocument(new URI("http://test"));
		tw.startContent();
		tw.addStartElement(new QName(SsmlNs, "s"));
		tw.addStartElement(new QName(SsmlNs, "y"));
		tw.addAttribute(new QName(null, "attr"), "attr-val");
		tw.addEndElement();
		tw.addText("this is text");
		tw.addEndElement();

		Map<String, Object> params = new TreeMap<String, Object>();
		params.put("ending-mark", endingmark);
		params.put("voice", voice);

		String result = Transformer.transformToString(tw.getResult(), params);

		String expected = "<s:speak xmlns:s=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s>"
		        + "<s:y attr=\"attr-val\"/>this is text</s:s><s:break time=\"250ms\"/><bookmark mark=\""
		        + endingmark + "\"/></s:speak>";

		Diff d = new Diff(result, expected);

		Assert.assertTrue(d.similar());
	}

	@Test
	public void noDocumentRoot() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String endingmark = "emark";
		String voice = "john";

		TreeWriter tw = new TreeWriter(Proc);
		tw.startDocument(new URI("http://test"));
		tw.startContent();
		tw.addStartElement(new QName(SsmlNs, "speak"));
		tw.addStartElement(new QName(SsmlNs, "s"));
		tw.addStartElement(new QName(SsmlNs, "y"));
		tw.addAttribute(new QName(null, "attr"), "attr-val");
		tw.addEndElement();
		tw.addText("this is text");
		tw.addEndElement();

		Map<String, Object> params = new TreeMap<String, Object>();
		params.put("ending-mark", endingmark);
		params.put("voice", voice);

		XdmNode firstChild = (XdmNode) tw.getResult().axisIterator(Axis.CHILD).next();

		String result = Transformer.transformToString(firstChild, params);
		String expected = "<s:speak xmlns:s=\"http://www.w3.org/2001/10/synthesis\" version=\"1.0\"><s:s>"
		        + "<s:y attr=\"attr-val\"/>this is text</s:s><s:break time=\"250ms\"/><bookmark mark=\""
		        + endingmark + "\"/></s:speak>";

		Diff d = new Diff(result, expected);

		Assert.assertTrue(d.similar());
	}

}
