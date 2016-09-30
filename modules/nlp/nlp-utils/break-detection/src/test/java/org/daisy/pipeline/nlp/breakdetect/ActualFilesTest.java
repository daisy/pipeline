package org.daisy.pipeline.nlp.breakdetect;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

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

/**
 * Those are only tests to check that the text is not modified by the step on
 * actual documents. It checks also that no exception or runtime error is
 * raised.
 */
public class ActualFilesTest implements TreeWriterFactory {
	static private Processor Proc;
	static private DocumentBuilder Builder;
	static private DummyLexerToken LexerToken;
	static private HashMap<Locale, LexerToken> Lexers;

	@BeforeClass
	static public void setUp() throws URISyntaxException, LexerInitException {
		Proc = new Processor(true);
		Builder = Proc.newDocumentBuilder();
		LexerToken = (DummyLexerToken) new DummyLexer().newToken();
		Lexers = new HashMap<Locale, LexerToken>();
		Lexers.put(null, LexerToken);

	}

	@Override
	public TreeWriter newInstance() {
		return new TreeWriter(Proc);
	}

	private static XdmNode getRoot(XdmNode node) {
		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			XdmNode n = (XdmNode) iter.next();
			if (n.getNodeKind() == XdmNodeKind.ELEMENT)
				return n;
		}
		return null;
	}

	private static void getText(XdmNode node, StringBuilder sb) {
		if (node.getNodeKind() == XdmNodeKind.TEXT
		        || node.getNodeKind() == XdmNodeKind.COMMENT
		        || node.getNodeKind() == XdmNodeKind.PROCESSING_INSTRUCTION) {
			sb.append(node.toString());
		}
		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			getText((XdmNode) iter.next(), sb);
		}
	}

	private static String getText(XdmNode node) {
		StringBuilder sb = new StringBuilder();
		getText(node, sb);
		return sb.toString();
	}

	private static void getUnmutableElements(XdmNode node, StringBuilder sb,
	        FormatSpecifications specs) {
		if (node.getNodeKind() == XdmNodeKind.TEXT
		        || node.getNodeKind() == XdmNodeKind.COMMENT
		        || node.getNodeKind() == XdmNodeKind.PROCESSING_INSTRUCTION) {
			return;
		}

		boolean toprint = false;
		XdmSequenceIterator iter;
		if (node.getNodeName() != null
		        && !specs.inlineElements.contains(node.getNodeName().getLocalName())
		        && !specs.wordTag.equals(node.getNodeName())
		        && !specs.sentenceTag.equals(node.getNodeName())) {
			toprint = true;
			sb.append(node.getNodeName().getLocalName());
			iter = node.axisIterator(Axis.ATTRIBUTE);
			while (iter.hasNext()) {
				XdmNode attr = (XdmNode) iter.next();
				sb.append(attr.getNodeName().getLocalName());
				sb.append(node.getAttributeValue(attr.getNodeName()));
			}
		}

		iter = node.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			getUnmutableElements((XdmNode) iter.next(), sb, specs);
		}

		if (toprint) {
			sb.append("{" + node.getNodeName().getLocalName() + "}");
		}
	}

	private static String getUnmutableElements(XdmNode node, FormatSpecifications specs) {
		StringBuilder sb = new StringBuilder();
		getUnmutableElements(node, sb, specs);
		return sb.toString();
	}

	private static void getOrphanWords(XdmNode node, boolean isInsideSentence,
	        FormatSpecifications specs, Map<String, Integer> orphans) {
		if (node.getNodeKind() != XdmNodeKind.ELEMENT) {
			return;
		}
		if (specs.sentenceTag.getLocalName().equals(node.getNodeName().getLocalName())) {
			isInsideSentence = true;
		} else if (specs.wordTag.getLocalName().equals(node.getNodeName().getLocalName())
		        && !isInsideSentence) {

			StringBuilder sb = new StringBuilder();
			XdmNode parent = node.getParent();
			while (parent.getNodeKind() == XdmNodeKind.ELEMENT) {
				sb.append(parent.getNodeName().getLocalName() + "_"
				        + parent.getAttributeValue(new QName("id")) + "_"
				        + parent.getAttributeValue(new QName("class")) + "/");
				parent = parent.getParent();
			}
			String key = sb.toString();
			Integer count = orphans.get(key);
			if (count == null)
				count = 0;
			orphans.put(key, count + 1);
		}

		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			getOrphanWords((XdmNode) iter.next(), isInsideSentence, specs, orphans);
		}
	}

	private static boolean hasMoreOrphans(XdmNode before, XdmNode after,
	        FormatSpecifications specs) {
		Map<String, Integer> obefore = new HashMap<String, Integer>();
		Map<String, Integer> oafter = new HashMap<String, Integer>();
		getOrphanWords(before, false, specs, obefore);
		getOrphanWords(after, false, specs, oafter);
		for (Map.Entry<String, Integer> e : oafter.entrySet()) {
			Integer b = obefore.get(e.getKey());
			if (b == null || b < e.getValue()) {
				System.out.println("orphan before: " + b + " orphan now: " + e.getValue()
				        + "; path " + e.getKey());

				return true;
			}
		}
		return false;
	}

	private static boolean hasTooManyLevels(XdmNode node, boolean isInsideTheElement,
	        String theElementName) {
		if (node.getNodeKind() != XdmNodeKind.ELEMENT) {
			return false;
		}
		if (theElementName.equals(node.getNodeName().getLocalName())) {
			if (isInsideTheElement) {
				return true;
			}
			isInsideTheElement = true;
		}

		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			if (hasTooManyLevels((XdmNode) iter.next(), isInsideTheElement, theElementName))
				return true;
		}
		return false;
	}

	private static boolean sentenceContainNonInline(XdmNode node, boolean isInsideSentence,
	        FormatSpecifications specs) {
		if (node.getNodeKind() != XdmNodeKind.ELEMENT) {
			return false;
		}
		if (specs.sentenceTag.getLocalName().equals(node.getNodeName().getLocalName())) {
			isInsideSentence = true;
		} else if (!specs.inlineElements.contains(node.getNodeName().getLocalName())
		        && isInsideSentence) {
			return true;
		}

		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			if (sentenceContainNonInline((XdmNode) iter.next(), isInsideSentence, specs))
				return true;
		}
		return false;
	}

	private static void getIDs(XdmNode node, Map<String, Integer> ids) {
		if (node.getNodeKind() != XdmNodeKind.ELEMENT
		        && node.getNodeKind() != XdmNodeKind.DOCUMENT) {
			return;
		}

		String id = node.getAttributeValue(new QName("id"));
		if (id != null) {
			Integer existing = ids.get(id);
			if (existing == null) {
				existing = 0;
			}
			ids.put(id, existing + 1);
		}

		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			getIDs((XdmNode) iter.next(), ids);
		}
	}

	public static int numberOfDuplicatedIDs(XdmNode ref, XdmNode actual) {
		Map<String, Integer> idrefs = new HashMap<String, Integer>();
		Map<String, Integer> idactuals = new HashMap<String, Integer>();

		getIDs(ref, idrefs);
		getIDs(actual, idactuals);

		int res = 0;
		for (Map.Entry<String, Integer> e : idrefs.entrySet()) {
			if (e.getValue() == 1 && !(Integer.valueOf(1).equals(idactuals.get(e.getKey())))) {
				++res;
			}
		}

		return res;
	}

	private static boolean notLeftTrimmed(XdmNode node) {
		return Pattern.compile("^[\\p{Z}\\s]").matcher(node.getStringValue()).find();
	}

	private static boolean notRightTrimmed(XdmNode node) {
		return Pattern.compile("[\\p{Z}\\s]$").matcher(node.getStringValue()).find();
	}

	private boolean isWellTrimmed(XdmNode node, String lexElement, String subLexElement) {
		if (node.getNodeKind() != XdmNodeKind.DOCUMENT
		        && node.getNodeKind() != XdmNodeKind.ELEMENT)
			return true;

		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		List<XdmNode> children = new ArrayList<XdmNode>();
		while (iter.hasNext()) {
			XdmNode child = (XdmNode) iter.next();
			if (!isWellTrimmed(child, lexElement, subLexElement))
				return false;
			children.add(child);
		}

		if (node.getNodeName() != null && lexElement.equals(node.getNodeName().getLocalName())) {
			XdmNode first = children.get(0);
			XdmNode last = children.get(children.size() - 1);
			if (children.size() == 1 && first.getNodeKind() != XdmNodeKind.TEXT
			        && !first.getNodeName().getLocalName().equals(subLexElement)) {
				System.out.print("bad location for: " + node);
				return false;
			}
			if (first.getNodeKind() == XdmNodeKind.TEXT && notLeftTrimmed(first)) {
				System.out.print("bad left trimming for: " + node);
				return false;
			}
			if (last.getNodeKind() == XdmNodeKind.TEXT && notRightTrimmed(last)) {
				System.out.print("bad right trimming for: " + node);
				return false;
			}
		}
		return true;
	}

	private void check(String file, String[] inlineElements, String[] spaceEquivalents)
	        throws SaxonApiException, LexerInitException {

		for (boolean forbidAnyDuplication : new boolean[]{
		        false, true
		}) {
			for (Strategy strategy : new Strategy[]{
			        Strategy.ONE_SENTENCE, Strategy.SPACE_SEPARARED_SENTENCES,
			        Strategy.SPACE_SEPARATED_WORDS, Strategy.REGULAR
			}) {

				LexerToken.strategy = strategy;
				SAXSource source = new SAXSource(new InputSource(getClass()
				        .getResourceAsStream(file)));
				XdmNode document = Builder.build(source);

				FormatSpecifications specs = new FormatSpecifications("http://tmp", "sss",
				        "www", "http://ns", "lang", Arrays.asList(inlineElements), Arrays
				                .asList(spaceEquivalents), Arrays.asList(spaceEquivalents),
				        null, null);

				XdmNode tree = new XmlBreakRebuilder()
				        .rebuild(this, Lexers, document, specs, new DummyLangDetector(),
				                forbidAnyDuplication, new ArrayList<String>());

				//check the tree well-formedness
				XdmNode root = getRoot(tree);
				Assert.assertFalse(hasMoreOrphans(getRoot(document), root, specs));
				Assert.assertFalse(sentenceContainNonInline(root, false, specs));
				Assert.assertFalse(hasTooManyLevels(root, false, specs.wordTag.getLocalName()));
				Assert.assertFalse(hasTooManyLevels(root, false, specs.sentenceTag
				        .getLocalName()));

				Assert.assertEquals(0, numberOfDuplicatedIDs(document, tree));

				Assert.assertTrue(isWellTrimmed(tree, specs.sentenceTag.getLocalName(),
				        specs.wordTag.getLocalName()));
				Assert.assertTrue(isWellTrimmed(tree, specs.wordTag.getLocalName(), null));

				//check that the content has not changed
				String ref = getUnmutableElements(document, specs);
				String processed = getUnmutableElements(tree, specs);
				Assert.assertEquals(ref, processed);
				Assert.assertEquals(getText(document), getText(tree));
			}
		}

	}

	private static String[] DTBookInline = new String[]{
	        "strong", "a", "acronym", "abbr", "dfn", "linenum", "pagenum", "pagebreak",
	        "samp", "span", "sub", "w", "noteref"
	};

	private static String[] ZedaiInline = new String[]{
	        "emph", "span", "ref", "char", "term", "sub", "ref", "sup", "pagebreak", "name"
	};

	private static String[] EpubInline = new String[]{
	        "span", "i", "b", "a", "br", "del", "font", "ruby", "s", "small", "strike",
	        "strong", "sup", "u", "q", "address", "abbr", "em", "style"
	};

	private static String[] DTBookSpace = new String[]{
	        "acronym", "span", "linenum", "pagenum", "samp", "noteref"
	};

	private static String[] ZedaiSpace = new String[]{
		"span"
	};

	private static String[] EpubSpace = new String[]{
	        "span", "br", "ruby", "s", "address", "abbr", "style"
	};

	@Test
	public void dtbook1() throws SaxonApiException, LexerInitException {
		check("/dtbook_test.xml", DTBookInline, DTBookSpace);
	}

	@Test
	public void zedai1() throws SaxonApiException, LexerInitException {
		check("/zedai_test.xml", ZedaiInline, ZedaiSpace);
	}

	@Test
	public void epub1() throws SaxonApiException, LexerInitException {
		check("/epub3_test.html", EpubInline, EpubSpace);
	}

	private Collection<String> getSamples(String suffix) {
		String path = getClass().getResource("/").getPath();
		File[] files = new File(path).listFiles();
		ArrayList<String> samples = new ArrayList<String>();
		for (File file : files) {
			if (file.getName().endsWith(suffix))
				samples.add("/" + file.getName());
		}
		return samples;
	}

	@Test
	public void extraDtbook() throws SaxonApiException, LexerInitException {
		for (String sample : getSamples(".dtbook.xml")) {
			check(sample, DTBookInline, DTBookSpace);
		}
	}

	@Test
	public void extraZedai() throws SaxonApiException, LexerInitException {
		for (String sample : getSamples(".zedai.xml")) {
			check(sample, ZedaiInline, ZedaiSpace);
		}
	}

	@Test
	public void extraEpub3() throws SaxonApiException, LexerInitException {
		for (String sample : getSamples(".epub3.html")) {
			check(sample, EpubInline, EpubSpace);
		}
	}

}
