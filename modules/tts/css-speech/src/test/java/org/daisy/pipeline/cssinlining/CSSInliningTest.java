package org.daisy.pipeline.cssinlining;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmNode;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.xmlcalabash.util.TreeWriter;

import cz.vutbr.web.css.CSSException;

public class CSSInliningTest implements TreeWriterFactory {

	static private Processor Proc;
	static private DocumentBuilder Builder;
	static private Serializer Serializer;
	static private CSSInliner CSSInliner;
	static private SpeechSheetAnalyser SheetAnalyzer;

	@BeforeClass
	static public void setUp() throws URISyntaxException {
		Proc = new Processor(false);
		Builder = Proc.newDocumentBuilder();
		Serializer = Proc.newSerializer();
		Serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, "yes");
		Serializer.setOutputProperty(Property.INDENT, "no");
		CSSInliner = new CSSInliner();
		SheetAnalyzer = new SpeechSheetAnalyser();
	}

	@Override
	public TreeWriter newInstance() {
		return new TreeWriter(Proc);
	}

	private void check(String input, String cssFile, String... expectedParts)
	        throws SaxonApiException, URISyntaxException, IOException, CSSException {

		//build the expected output regex
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
		XdmNode document = Builder.build(source);

		if (cssFile == null) {
			cssFile = "test.css";
		}

		URI cssFileURI = new URI("file://"
		        + Paths.get(System.getProperty("user.dir"), "src/test/resources/", cssFile)
		                .toString());

		SheetAnalyzer.analyse(Arrays.asList(cssFileURI), Collections.EMPTY_LIST, null);
		XdmNode tree = CSSInliner.inline(this, new URI("http://doc"), document, SheetAnalyzer,
		        "tmp");

		OutputStream result = new ByteArrayOutputStream();
		Serializer.setOutputStream(result);
		Serializer.serializeNode(tree);

		boolean match = Pattern.matches(expected.toString(), result.toString());

		Assert.assertTrue(expected.toString() + " and,\n" + result.toString()
		        + " must match with each other", match);
	}

	@Test
	public void pauseBefore() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:pause-before=\"[0-9.]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void pauseAfter() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:pause-after=\"[0-9.]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void cueBefore() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:cue-before=\".*\\.mp3\"", "test", "</simple>", "</root>");
	}

	@Test
	public void cueAfter() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:cue-after=\".*\\.mp3\"", "test", "</simple>", "</root>");
	}

	@Test
	public void cuePath() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		String expectedPath = new URL("file://"
		        + Paths.get(System.getProperty("user.dir"), "src/test/resources/").toString())
		        .toString();
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:cue-after=\"" + expectedPath + "/[-_a-z0-9]+\\.mp3\"", "test",
		        "</simple>", "</root>");
	}

	@Test
	public void volume() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:volume=\"[a-z.0-9]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void speechRate() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:speech-rate=\"[a-z.0-9]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void hyphen1() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><hyphens>test</hyphens></root>", null, "<root", "<hyphens",
		        "tts:speech-rate=\"x-slow\"", "test", "</hyphens>", "</root>");
	}

	@Test
	public void hyphen2() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><hyphens>test</hyphens></root>", null, "<root", "<hyphens",
		        "tts:cue-before=\".*a_b-c.*\"", "test", "</hyphens>", "</root>");
	}

	@Test
	public void voiceFamily() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", null, "<root", "<simple",
		        "tts:voice-family=\"[a-z0-9]+,[a-z0-9]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void prefixedAttr() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root xmlns:epub=\"http://epub\"><n epub:type=\"prefixed\">test</n></root>",
		        null, "<root", "<n", "=\".*prefixed\\.mp3", "test", "</n>", "</root>");
	}

	@Test
	public void keepContent() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root x=\"1\"><n y=\"1\">content1<n z=\"1\">content2</n></n>content3</root>",
		        null, "<root", "x=\"1\"", "<n", "y=\"1\"", "content1", "<n", "z=\"1\"",
		        "content2", "</n>", "</n>", "content3", "</root>");
	}

	@Test
	public void selectors1() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><div><b><p>test</p></b></div><div><a>test</a></div></root>", null,
		        "<root", "<div", "<b", "<p", "tts:speech-rate=\"10[.]?[0]*\"", "test", "</p>",
		        "</b>", "</div>", "<div", "<a", "tts:speech-rate=\"20[.]?[0]*\"", "test",
		        "</a>", "</div>", "</root>");
	}

	@Test
	public void selectors2() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><div><x>test</x></div></root>", null, "<root", "<div", "<x",
		        "tts:speech-rate=\"30[.]?[0]*\"", "test", "</x>", "</div>", "</root>");
	}

	@Test
	public void selectors3() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><div>test1</div><y>test2</y></root>", null, "<root", "<div", "test1",
		        "</div>", "<y", "tts:speech-rate=\"40[.]?[0]*\"", "test2", "</y>", "</root>");
	}

	@Test
	public void mixedMedia() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple>test</simple></root>", "mixed.css", "<root", "<simple",
		        "tts:volume=\"[a-z]+\"", "test", "</simple>", "</root>");
	}

	@Test
	public void units1() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple2>test</simple2></root>", "test.css", "<root", "<simple2",
		        "tts:pause=\"[.0-9]+ms\"", "test", "</simple2>", "</root>");
	}

	@Test
	public void units2() throws SaxonApiException, URISyntaxException, IOException,
	        CSSException {
		check("<root><simple3>test</simple3></root>", "test.css", "<root", "<simple3",
		        "tts:pause=\"[.0-9]+s\"", "test", "</simple3>", "</root>");
	}

	@Test
	public void keepProcessingInstructions() throws SaxonApiException, URISyntaxException,
	        IOException, CSSException {
		String PI = "<?xml-stylesheet type=\"text/xsl\" href=\"style.xsl\"?>";
		check(PI + "<root>content</root>", null, PI.replace("?", "\\?") + "<root",
		        "content</root>");
	}

	@Test
	public void retrieveCSSuris() throws SaxonApiException, URISyntaxException {
		String xml = "<?node type=\"text/css\" href=\"sheet1.css\"?>";
		xml += "<?xml-stylesheet type=\"text/zzz\" href=\"sheet2.css\"?>";
		xml += "<?xml-stylesheet type=\"text/css\" href=\"sheet3.css\"?>"; //valid
		xml += "<?xml-stylesheet type=\"text/css\" href=\"file:///foo/bar/sheet4.css\"?>"; //valid
		xml += "<root><head>";
		xml += "<link rel=\"stylesheet\" href=\"sheet5.css\"/>"; //valid
		xml += "<link rel=\"zzz\" href=\"sheet6.css\"/>";
		xml += "</head></root>";
		SAXSource source = new SAXSource(new InputSource(new StringReader(xml)));
		String directory = "file:///dir/";
		source.setSystemId(directory + "file.xml");
		XdmNode document = Builder.build(source);

		Set<URI> uris = new HashSet<URI>(InlineCSSStep.getCSSurisInContent(document));
		Assert.assertTrue(uris.contains(new URI(directory + "sheet3.css")));
		Assert.assertTrue(uris.contains(new URI("file:///foo/bar/sheet4.css")));
		Assert.assertTrue(uris.contains(new URI(directory + "sheet5.css")));
		Assert.assertEquals(3, uris.size());
	}
}
