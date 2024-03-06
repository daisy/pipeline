package org.daisy.pipeline.css.speech.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;
import javax.xml.transform.sax.SAXSource;

import cz.vutbr.web.css.CSSException;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.pipeline.css.Medium;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import org.xml.sax.InputSource;

public class SpeechCssCascaderTest {

	private static final Medium SPEECH = Medium.parse("speech");
	private static final QName TTS_NS = new QName("http://www.daisy.org/ns/pipeline/tts", "_", "tts");

	private static Processor proc;
	private static DocumentBuilder docBuilder;
	private static Serializer serializer;
	private static SpeechCssCascader cascader;

	@BeforeClass
	public static void setUp() throws URISyntaxException {
		proc = new Processor(false);
		docBuilder = proc.newDocumentBuilder();
		serializer = proc.newSerializer();
		serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, "yes");
		serializer.setOutputProperty(Property.INDENT, "no");
		cascader = new SpeechCssCascader();
	}

	private void check(String input, String cssFile, String... expectedParts)
			throws SaxonApiException, URISyntaxException, IOException, CSSException {
		// build the expected output regex
		StringBuilder expected = new StringBuilder();
		boolean opened = false;
		for (String p : expectedParts) {
			if (p.startsWith("</")) {
				if (opened) {
					expected.append(".*>");
					opened = false;
				}
				expected.append(p);
			} else if (p.startsWith("<")) {
				if (opened) {
					expected.append(".*>");
					opened = false;
				}
				expected.append(p);
				opened = true;
			} else if (p.contains("=")) {
				expected.append(".+" + p);
			} else {
				if (opened) {
					expected.append(".*>");
					opened = false;
				}
				expected.append(p);
			}
		}
		SAXSource source = new SAXSource(new InputSource(new StringReader(input)));
		source.setSystemId("http://doc");
		XdmNode doc = docBuilder.build(source);
		URI cssFileURI = new URI("file://" + Paths.get(System.getProperty("user.dir"), "src/test/resources/", cssFile));
		OutputStream result = new ByteArrayOutputStream();
		serializer.setOutputStream(result);
		cascader.newInstance(SPEECH, cssFileURI.toString(), null, null, null, TTS_NS, true)
		        .transform(
		            new SaxonInputValue(doc),
		            new SaxonOutputValue(
		                n -> {
		                    try {
		                        serializer.serializeNode((XdmNode)n); }
		                    catch (SaxonApiException e) {
		                        throw new RuntimeException(e); }},
		                proc.getUnderlyingConfiguration()))
		        .run();
		boolean match = Pattern.matches(expected.toString(), result.toString());
		Assert.assertTrue(expected.toString() + " and,\n" + result.toString()
		                  + " must match with each other", match);
	}

	@Test
	public void pauseBefore() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:pause-before=\"[0-9.]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void pauseAfter() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:pause-after=\"[0-9.]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void cueBefore() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:cue-before=\".*\\.mp3\"", "test", "</simple>", "</root>");
	}

	@Test
	public void cueAfter() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:cue-after=\".*\\.mp3\"", "test", "</simple>", "</root>");
	}

	@Test
	public void cuePath() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		URL expectedPath = new URL(
			"file://" + Paths.get(System.getProperty("user.dir"), "src/test/resources/").toString());
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:cue-after=\"" + expectedPath + "/[-_a-z0-9]+\\.mp3\"", "test",
		      "</simple>", "</root>");
	}

	@Test
	public void volume() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:volume=\"[a-z.0-9]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void speechRate() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:speech-rate=\"[a-z.0-9]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void hyphen1() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><hyphens>test</hyphens></root>", "speech.css", "<root", "<hyphens",
		      "tts:speech-rate=\"x-slow\"", "test", "</hyphens>", "</root>");
	}

	@Test
	public void hyphen2() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><hyphens>test</hyphens></root>", "speech.css", "<root", "<hyphens",
		      "tts:cue-before=\".*a_b-c.*\"", "test", "</hyphens>", "</root>");
	}

	@Test
	public void voiceFamily() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech.css", "<root", "<simple",
		      "tts:voice-family=\"[a-z0-9]+ *, *[a-z0-9]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void prefixedAttr() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root xmlns:epub=\"http://epub\"><n epub:type=\"prefixed\">test</n></root>",
		      "speech.css", "<root", "<n", "=\".*prefixed\\.mp3", "test", "</n>", "</root>");
	}

	@Test
	public void keepContent() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root x=\"1\"><n y=\"1\">content1<n z=\"1\">content2</n></n>content3</root>",
		      "speech.css", "<root", "x=\"1\"", "<n", "y=\"1\"", "content1", "<n", "z=\"1\"",
		      "content2", "</n>", "</n>", "content3", "</root>");
	}

	@Test
	public void selectors1() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><div><b><p>test</p></b></div><div><a>test</a></div></root>", "speech.css",
		      "<root", "<div", "<b", "<p", "tts:speech-rate=\"10[.]?[0]*\"", "test", "</p>",
		      "</b>", "</div>", "<div", "<a", "tts:speech-rate=\"20[.]?[0]*\"", "test",
		      "</a>", "</div>", "</root>");
	}

	@Test
	public void selectors2() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><div><x>test</x></div></root>", "speech.css", "<root", "<div", "<x",
		      "tts:speech-rate=\"30[.]?[0]*\"", "test", "</x>", "</div>", "</root>");
	}

	@Test
	public void selectors3() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><div>test1</div><y>test2</y></root>", "speech.css", "<root", "<div", "test1",
		      "</div>", "<y", "tts:speech-rate=\"40[.]?[0]*\"", "test2", "</y>", "</root>");
	}

	@Test
	public void mixedMedia() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "speech-and-print.css", "<root", "<simple",
		      "tts:volume=\"[a-z]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void units1() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple2>test</simple2></root>", "speech.css", "<root", "<simple2",
		      "tts:pause=\"[.0-9]+ms\"", "test", "</simple2>", "</root>");
	}

	@Test
	public void units2() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple3>test</simple3></root>", "speech.css", "<root", "<simple3",
		      "tts:pause=\"[.0-9]+s\"", "test", "</simple3>", "</root>");
	}

	@Test
	public void keepProcessingInstructions() throws SaxonApiException, URISyntaxException,
	        IOException, CSSException {
		String PI = "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>";
		check(PI + "<root>content</root>", "speech.css", PI.replace("?", "\\?") + "<root",
		      "content</root>");
	}
}
