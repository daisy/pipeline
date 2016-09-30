package org.daisy.pipeline.nlp.breakdetect;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.Serializer.Property;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.nlp.DummyLangDetector;
import org.daisy.pipeline.nlp.breakdetect.DummyLexer.DummyLexerToken;
import org.daisy.pipeline.nlp.breakdetect.DummyLexer.Strategy;
import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.xmlcalabash.util.TreeWriter;

public class BreakDetectTest implements TreeWriterFactory {

	static private Processor Proc;
	static private DocumentBuilder Builder;
	static private Serializer Serializer;
	static private DummyLexerToken LexerToken;
	static private HashMap<Locale, LexerToken> Lexers;

	@BeforeClass
	static public void setUp() throws URISyntaxException {
		Proc = new Processor(true);
		Builder = Proc.newDocumentBuilder();
		Serializer = Proc.newSerializer();
		Serializer.setOutputProperty(Property.OMIT_XML_DECLARATION, "yes");
		Serializer.setOutputProperty(Property.INDENT, "no");
		LexerToken = (DummyLexerToken) new DummyLexer().newToken();
		Lexers = new HashMap<Locale, LexerToken>();
		Lexers.put(null, LexerToken);
	}

	@Override
	public TreeWriter newInstance() {
		return new TreeWriter(Proc);
	}

	private String clean(String str) {
		return str.replaceAll("<([^ ]+) [^><]+/>", "<$1/>") //remove the attributes
		        .replaceAll("<([^ ]+) [^><]+>", "<$1>") //remove the attributes (side effect: comments are removed as well)
		        .replaceAll("<(/?)[^:]*:", "<$1"); //remove the namespace prefixes
	}

	private void check(String input, String expected, boolean removeSpace,
	        boolean forbidAnyDuplication) throws SaxonApiException, LexerInitException {

		if (removeSpace)
			input = input.replaceAll("\\p{Space}", "");

		SAXSource source = new SAXSource(new InputSource(new StringReader(input)));
		XdmNode document = Builder.build(source);

		FormatSpecifications specs = new FormatSpecifications("http://tmp", "s", "w",
		        "http://ns", "lang", Arrays.asList("span1", "span2", "span3", "space"), Arrays
		                .asList("space", "span3"), Arrays.asList("space", "span3"), Arrays
		                .asList("sentbefore"), Arrays.asList("sentafter"));

		XdmNode tree = new XmlBreakRebuilder().rebuild(this, Lexers, document, specs,
		        new DummyLangDetector(), forbidAnyDuplication, new ArrayList<String>());

		OutputStream result = new ByteArrayOutputStream();
		Serializer.setOutputStream(result);
		Serializer.serializeNode(tree);

		String actual = clean(result.toString());

		if (removeSpace)
			expected = expected.replaceAll("\\p{Space}", "");

		Assert.assertEquals(clean(expected), actual);
	}

	@Test
	public void singleSentence() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root>word</root>", "<root><s>word</s></root>", false, false);
	}

	@Test
	public void twosections() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<r><root>word1<sep/>word2</root></r>",
		        "<r><root><s>word1</s><sep/><s>word2</s></root></r>", false, false);
	}

	@Test
	public void threesections() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root>z1<sep>z2</sep>z3</root>",
		        "<root><s>z1</s><sep><s>z2</s></sep><s>z3</s></root>", false, false);
	}

	@Test
	public void sameSection1() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root>a<span1>b</span1>c</root>", "<root><s>a<span1>b</span1>c</s></root>",
		        false, false);
	}

	@Test
	public void sameSection2() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root>a<span1/>b</root>", "<root><s>a<span1/>b</s></root>", false, false);
	}

	@Test
	public void sameSection3() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root><span1><span2>a</span2>b<span2>c</span2></span1></root>",
		        "<root><span1><s><span2>a</span2>b<span2>c</span2></s></span1></root>", false,
		        false);
	}

	@Test
	public void multipleSentences1() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARARED_SENTENCES;
		check("<root><span3>sentence1</span3><span3>sentence2</span3></root>",
		        "<root><span3><s>sentence1</s></span3><span3><s>sentence2</s></span3></root>",
		        false, false);
	}

	@Test
	public void multipleSentences2() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARARED_SENTENCES;
		check("<root><span3>sentence1</span3>middle-sent<span3>sentence2</span3></root>",
		        "<root><span3><s>sentence1</s></span3><s>middle-sent</s><span3><s>sentence2</s></span3></root>",
		        false, false);
	}

	@Test
	public void singleWord() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARATED_WORDS;
		check("<root>word</root>", "<root><s><w>word</w></s></root>", false, false);
	}

	@Test
	public void twoWords() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARATED_WORDS;
		check("<root>w1<span3/>w2</root>", "<root><s><w>w1</w><span3/><w>w2</w></s></root>",
		        false, false);
	}

	@Test
	public void emptyParts1() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root>begin<span1/>end</root>", "<root><s>begin<span1/>end</s></root>", false,
		        false);
	}

	@Test
	public void comments1() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARARED_SENTENCES;

		String doc = "<doc><head><!-- comment1--></head><body><frontmatter>"
		        + "<!-- comment2 --></frontmatter></body></doc>";
		check(doc, doc, false, false);
	}

	//A typical case of splittable content (if no CSS attached) is:
	//"John writes that <i>there's a plan underway to build a space agency run by African nations. At least they said so.</i>"
	//It will be transformed to:
	//"<s>John writes that <i>there's a plan [...] by African nations.</i></s> <s><i>At least [...].</i></s>"
	@Test
	public void splittable1() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root><span1>one</span1><span2>two<sep/>three</span2></root>",
		        "<root><s><span1>one</span1><span2>two</span2></s><span2>"
		                + "<sep/><s>three</s></span2></root>", false, false);
	}

	@Test
	public void unsplittable1() throws SaxonApiException, LexerInitException {
		//same as 'splittable1' but with an id on span2
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root><span1>one</span1><span2 id=\"123\">two<sep/>three</span2></root>",
		        "<root><span1><s>one</s></span1><span2><s>two</s>"
		                + "<sep/><s>three</s></span2></root>", false, false);
	}

	@Test
	public void unsplittable2() throws SaxonApiException, LexerInitException {
		//we don't want the id to force the creation of a new inline section.
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root>one<span2 id=\"123\">two</span2>three</root>",
		        "<root><s>one<span2 id=\"123\">two</span2>three</s></root>", false, false);
	}

	@Test
	public void unsplittable3() throws SaxonApiException, LexerInitException {
		//no duplication allowed, regardless of the id
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		check("<root><span1>one</span1><span2>two<sep/>three</span2></root>",
		        "<root><span1><s>one</s></span1><span2><s>two</s><sep/><s>three</s></span2></root>",
		        false, true);
	}

	@Test
	public void unsplittable4() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<root>");
		input.append("<span1>one</span1>");
		input.append("<span2 id=\"123\">");
		input.append("two<sep/>");
		input.append("<span3>section1</span3><sep/>");
		input.append("<span3>section2</span3><sep/>");
		input.append("three");
		input.append("</span2>");
		input.append("</root>");

		StringBuilder expected = new StringBuilder(); //robust to IDE's auto formatting
		expected.append("<root>");
		expected.append("<span1><s>one</s></span1>");
		expected.append("<span2 id=\"123\">");
		expected.append("<s>two</s><sep/>");
		expected.append("<span3><s>section1</s></span3><sep/>");
		expected.append("<span3><s>section2</s></span3><sep/>");
		expected.append("<s>three</s>");
		expected.append("</span2>");
		expected.append("</root>");

		check(input.toString(), expected.toString(), false, false);
	}

	@Test
	public void unsplittable5() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.REGULAR;
		StringBuilder input = new StringBuilder();
		input.append("<root>");
		input.append("<span2 id=\"123\">");
		input.append("<span3>section1</span3><sep/>");
		input.append("<span3>section2</span3><sep/>");
		input.append("begin-sentence");
		input.append("</span2>");
		input.append("end-sentence");
		input.append("</root>");

		StringBuilder expected = new StringBuilder();
		expected.append("<root>");
		expected.append("<span2 id=\"123\">");
		expected.append("<span3><s><w>section1</w></s></span3><sep/>");
		expected.append("<span3><s><w>section2</w></s></span3><sep/>");
		expected.append("<s><w>begin-sentence</w></s>");
		expected.append("</span2>");
		expected.append("<s><w>end-sentence</w></s>");
		expected.append("</root>");

		check(input.toString(), expected.toString(), false, false);
	}

	@Test
	public void sentenceInjection() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.REGULAR;
		check("<root>First <sentafter>sent</sentafter>Second sent<sentbefore>Third sent</sentbefore></root>",
		        "<root><s><w>First</w> <sentafter><w>sent</w></sentafter></s><s><w>Second</w> <w>sent</w></s>"
		                + "<sentbefore><s><w>Third</w> <w>sent</w></s></sentbefore></root>",
		        false, false);
	}

	@Test
	public void hard1() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<root>");
		input.append(" abc");
		input.append(" <span1>");
		input.append("   cde<sep/>f");
		input.append("   <span2>");
		input.append("      <span1/>");
		input.append("      <span2>gh</span2>ij");
		input.append("   </span2>");
		input.append("   test");
		input.append("   <span1>");
		input.append("     <span2>xyz</span2>");
		input.append("   </span1>");
		input.append(" </span1>");
		input.append("</root>");

		StringBuilder expected = new StringBuilder();
		expected.append("<root>");
		expected.append("  <s>abc<span1>cde</span1></s>");
		expected.append("  <span1><sep/><s>");
		expected.append("    f");
		expected.append("    <span2>");
		expected.append("      <span1/>");
		expected.append("      <span2>gh</span2>ij");
		expected.append("    </span2>");
		expected.append("    test");
		expected.append("    <span1>");
		expected.append("      <span2>xyz</span2>");
		expected.append("    </span1>");
		expected.append("  </s></span1>");
		expected.append("</root>");

		check(input.toString(), expected.toString(), true, false);
	}

	@Test
	public void hard2() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.ONE_SENTENCE;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<root>");
		input.append(" <span1/>");
		input.append(" <sep/>");
		input.append(" <span1/>");
		input.append(" <span1><span2><sep/>begin</span2><sep/></span1>");
		input.append(" <span1>middle</span1>end<span1/>");
		input.append("</root>");

		StringBuilder expected = new StringBuilder();
		expected.append("<root>");
		expected.append("  <span1/>");
		expected.append("  <sep/>");
		expected.append("  <span1/>");
		expected.append("  <span1>");
		expected.append("     <span2><sep/><s>begin</s></span2>");
		expected.append("     <sep/>");
		expected.append("  </span1>");
		expected.append("  <s>");
		expected.append("    <span1>middle</span1>end");
		expected.append("  </s>");
		expected.append("  <span1/>");
		expected.append("</root>");

		check(input.toString(), expected.toString(), true, false);
	}

	@Test
	public void hard3() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARATED_WORDS;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<root>");
		input.append(" first");
		input.append(" <span3>second<span3>third</span3><sep/></span3>");
		input.append(" <span3>fourth</span3>fifth<sep/>sixth");
		input.append("</root>");

		StringBuilder expected = new StringBuilder();
		expected.append("<root>");
		expected.append("  <s>");
		expected.append("    <w>first</w>");
		expected.append("    <span3>");
		expected.append("      <w>second</w>");
		expected.append("      <span3><w>third</w></span3>");
		expected.append("    </span3>");
		expected.append("  </s>");
		expected.append("  <span3><sep/></span3>");
		expected.append("  <s>");
		expected.append("    <span3><w>fourth</w></span3><w>fifth</w>");
		expected.append("  </s>");
		expected.append("  <sep/>");
		expected.append("  <s><w>sixth</w></s>");
		expected.append("</root>");

		check(input.toString(), expected.toString(), true, false);
	}

	@Test
	public void hard4() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARATED_WORDS;
		StringBuilder input = new StringBuilder();
		input.append("<root>");
		input.append(" <sep>");
		input.append("   <sep>one<sep/>two</sep>");
		input.append("   <sep><sep/>three<sep/></sep>");
		input.append("   <sep><span1/>four</sep>");
		input.append(" </sep>");
		input.append("</root>");

		StringBuilder expected = new StringBuilder();
		expected.append("<root>");
		expected.append(" <sep>");
		expected.append("   <sep><s><w>one</w></s><sep/><s><w>two</w></s></sep>");
		expected.append("   <sep><sep/><s><w>three</w></s><sep/></sep>");
		expected.append("   <sep><span1/><s><w>four</w></s></sep>");
		expected.append(" </sep>");
		expected.append("</root>");

		check(input.toString(), expected.toString(), true, false);
	}

	@Test
	public void hard5() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARARED_SENTENCES;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<document>");
		input.append("  <head>");
		input.append("    <meta>");
		input.append("      <meta/>");
		input.append("      <meta/>");
		input.append("    </meta>");
		input.append("    <meta>");
		input.append("      <meta>");
		input.append("        <span1>Alice</span1>");
		input.append("      </meta>");
		input.append("      <meta>");
		input.append("        <span1>Rabbit</span1>");
		input.append("      </meta>");
		input.append("    </meta>");
		input.append("  </head>");
		input.append("</document>");

		StringBuilder expected = new StringBuilder();
		expected.append("<document>");
		expected.append("  <head>");
		expected.append("    <meta>");
		expected.append("      <meta/>");
		expected.append("      <meta/>");
		expected.append("    </meta>");
		expected.append("    <meta>");
		expected.append("      <meta>");
		expected.append("        <span1><s>Alice</s></span1>");
		expected.append("      </meta>");
		expected.append("      <meta>");
		expected.append("        <span1><s>Rabbit</s></span1>");
		expected.append("      </meta>");
		expected.append("    </meta>");
		expected.append("  </head>");
		expected.append("</document>");

		check(input.toString(), expected.toString(), true, false);
	}

	@Test
	public void hard6() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARARED_SENTENCES;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<document>");
		input.append("  <head>x");
		input.append("    <meta>y");
		input.append("      <meta/>");
		input.append("      <meta/>");
		input.append("    X</meta>");
		input.append("    <meta>z");
		input.append("      <meta>");
		input.append("        <span1>Alice</span1>");
		input.append("      </meta>");
		input.append("      <meta>");
		input.append("        <span1>Rabbit</span1>");
		input.append("      </meta>");
		input.append("    Y</meta>u");
		input.append("  </head>v");
		input.append("</document>");

		StringBuilder expected = new StringBuilder();
		expected.append("<document>");
		expected.append("  <head><s>x</s>");
		expected.append("    <meta><s>y</s>");
		expected.append("      <meta/>");
		expected.append("      <meta/>");
		expected.append("    <s>X</s></meta>");
		expected.append("    <meta><s>z</s>");
		expected.append("      <meta>");
		expected.append("        <span1><s>Alice</s></span1>");
		expected.append("      </meta>");
		expected.append("      <meta>");
		expected.append("        <span1><s>Rabbit</s></span1>");
		expected.append("      </meta>");
		expected.append("    <s>Y</s></meta><s>u</s>");
		expected.append("  </head><s>v</s>");
		expected.append("</document>");

		check(input.toString(), expected.toString(), true, false);
	}

	@Test
	public void hard7() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.SPACE_SEPARARED_SENTENCES;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<dtbook>n");
		input.append("  <frontmatter>n");
		input.append("    <h2>n");
		input.append("      <space>before</space>");
		input.append("    </h2>");
		input.append("    <table>n");
		input.append("      <tr>n");
		input.append("        <td><span2>middle1</span2></td>");
		input.append("        n<td><span2>middle2</span2></td>");
		input.append("        n<td><span2>middle3</span2></td>");
		input.append("      </tr>");
		input.append("    </table>");
		input.append("    <space>after</space>");
		input.append("  </frontmatter>");
		input.append("</dtbook>");

		StringBuilder expected = new StringBuilder(); //robust to IDE's auto formatting
		expected.append("<dtbook><s>n</s>");
		expected.append("  <frontmatter><s>n</s>");
		expected.append("    <h2><s>n</s>");
		expected.append("      <space><s>before</s></space>");
		expected.append("    </h2>");
		expected.append("    <table><s>n</s>");
		expected.append("      <tr><s>n</s>");
		expected.append("        <td><span2><s>middle1</s></span2></td>");
		expected.append("        <s>n</s><td><span2><s>middle2</s></span2></td>");
		expected.append("        <s>n</s><td><span2><s>middle3</s></span2></td>");
		expected.append("      </tr>");
		expected.append("    </table>");
		expected.append("    <space><s>after</s></space>");
		expected.append("  </frontmatter>");
		expected.append("</dtbook>");

		check(input.toString(), expected.toString(), true, false);
	}

	@Test
	public void hard8() throws SaxonApiException, LexerInitException {
		LexerToken.strategy = Strategy.REGULAR;
		StringBuilder input = new StringBuilder(); //robust to IDE's auto formatting
		input.append("<root>");
		input.append("<p>");
		input.append("<span1>this is sentence one. This is </span1>");
		input.append("sentence two.");
		input.append("</p>");
		input.append("</root>");

		StringBuilder expected = new StringBuilder(); //robust to IDE's auto formatting
		expected.append("<root>");
		expected.append("<p>");
		expected.append("<span1>");
		expected.append("<s>");
		expected.append("<w>this</w> <w>is</w> <w>sentence</w> <w>one</w><w>.</w>");
		expected.append("</s> ");
		expected.append("</span1>");
		expected.append("<s>");
		expected.append("<span1>");
		expected.append("<w>This</w> <w>is</w> ");
		expected.append("</span1>");
		expected.append("<w>sentence</w> <w>two</w><w>.</w>");
		expected.append("</s>");
		expected.append("</p>");
		expected.append("</root>");

		check(input.toString(), expected.toString(), false, false);

	}
}
