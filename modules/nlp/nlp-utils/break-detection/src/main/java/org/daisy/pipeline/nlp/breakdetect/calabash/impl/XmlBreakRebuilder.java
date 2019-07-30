package org.daisy.pipeline.nlp.breakdetect.calabash.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.pipeline.nlp.LangDetector;
import org.daisy.pipeline.nlp.breakdetect.calabash.impl.StringComposer.SentencePointer;
import org.daisy.pipeline.nlp.breakdetect.calabash.impl.StringComposer.TextPointer;
import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;
import org.daisy.pipeline.nlp.lexing.LexService.Sentence;

import com.xmlcalabash.util.TreeWriter;

/**
 * This class is used for rebuilding the input document with the additional XML
 * elements resulting from the lexing, i.e. sentences and words.
 * 
 * The algorithm is run multiple times until no forbidden duplication is
 * performed (this is the main reason why it can be slow sometimes). If the
 * 'no-duplication-allowed' option is not enable, then only the nodes with ID
 * will be kept under watch. Further improvements should forbid the algorithm to
 * duplicate nodes attached to CSS properties such as "display: block", "border"
 * or "cue-before".
 * 
 * The algorithm processes the inline sections on-the-fly as soon as they are
 * detected by the InlineSectionFinder. Since the sections can be dispatched
 * over distinct branches of the document tree, the tree is rebuilt by
 * agglomerating tree paths with writeTree(), instead of the usual top-down
 * recursive way whose building scope is too small for this purpose.
 * 
 * The levels are used for aligning the tree paths each other so that it is easy
 * to find the common ancestor of a group of leaves.
 * 
 */
public class XmlBreakRebuilder implements InlineSectionProcessor {

	private TreeWriter mTreeWriter;
	private Map<Locale, LexerToken> mLexers;
	private StringComposer mStringComposer;
	private FormatSpecifications mSpecs;
	private XdmNode mPreviousNode; //last node written
	private int mPreviousLevel;
	private DuplicationManager mDuplicationManager;
	private String mCurrentLang;
	private LangDetector mLangDetector;
	private List<String> mParsingErrors;

	public XdmNode rebuild(TreeWriterFactory treeWriterFactory,
	        Map<Locale, LexerToken> lexers, XdmNode doc, FormatSpecifications specs,
	        LangDetector langDetector, boolean forbidAnyDup, List<String> parsingErrors)
	        throws LexerInitException {
		mLexers = lexers;
		mSpecs = specs;
		mLangDetector = langDetector;
		mStringComposer = new StringComposer();
		mPreviousNode = getRoot(doc);
		mPreviousLevel = 0;
		mParsingErrors = parsingErrors;

		mDuplicationManager = new DuplicationManager(forbidAnyDup);
		Set<NodeInfo> unsplittable = new HashSet<NodeInfo>();
		List<NodeInfo> duplicated;

		do {
			mDuplicationManager.onNewDocument();
			mTreeWriter = treeWriterFactory.newInstance();
			mTreeWriter.startDocument(doc.getBaseURI());
			XdmSequenceIterator iter = doc.axisIterator(Axis.CHILD);
			while (iter.hasNext()) {
				XdmNode n = (XdmNode) iter.next();
				if (n.getNodeKind() == XdmNodeKind.ELEMENT) {

					XdmNode root = n;
					mPreviousNode = root;
					mTreeWriter.addStartElement(root);
					mTreeWriter.addAttributes(root);
					mTreeWriter.addNamespace(mSpecs.tmpNsPrefix, mSpecs.tmpNs);

					new InlineSectionFinder().find(root, mPreviousLevel, specs, this,
					        unsplittable);
					closeAllElementsUntil(root, 0);
					break;

				} else {
					mTreeWriter.addSubtree(n);
				}
			}
			mTreeWriter.endDocument();

			duplicated = mDuplicationManager.getDuplicatedNodes();

			unsplittable.addAll(duplicated);

		} while (duplicated.size() > 0);

		XdmNode result = mTreeWriter.getResult();

		return result;
	}

	public static int getDuplicatedIDs(Map<String, Integer> ref, Map<String, Integer> actual,
	        Set<String> unsplittable) {
		int res = 0;
		for (Map.Entry<String, Integer> e : ref.entrySet()) {
			if (e.getValue() == 1 && !(Integer.valueOf(1).equals(actual.get(e.getKey())))) {
				unsplittable.add(e.getKey());
				++res;
			}
		}

		return res;
	}

	private void addOneWord(TextPointer word, List<Leaf> leaves, List<String> text) {
		Leaf wordParent = deepestAncestorOf(leaves, word.firstSegment, word.lastSegment);
		if (wordParent.formatting == null)
			return;

		startLexingElement(wordParent, mSpecs.wordTag);
		if (word.firstSegment == word.lastSegment) {
			writeTree(leaves.get(word.firstSegment), text.get(word.firstSegment).substring(
			        word.firstIndex, word.lastIndex));
		} else {
			writeTree(leaves.get(word.firstSegment), text.get(word.firstSegment).substring(
			        word.firstIndex));
			for (int i = word.firstSegment + 1; i < word.lastSegment; ++i)
				writeTree(leaves.get(i), text.get(i));
			writeTree(leaves.get(word.lastSegment), text.get(word.lastSegment).substring(0,
			        word.lastIndex));
		}
		closeAllElementsUntil(wordParent.formatting, 1);
	}

	@Override
	public void onInlineSectionFound(List<Leaf> leaves, List<String> text, Locale lang)
	        throws LexerInitException {

		Locale expectedLang = lang;
		lang = mLangDetector.findLang(lang, text);
		if (lang != null && expectedLang != null) {
			if (expectedLang.getLanguage().equals(lang.getLanguage()))
				mCurrentLang = null; //this indicates that there is no need to add xml:lang attributes
			else
				mCurrentLang = lang.getISO3Language(); //is it the right standard?
		} else
			mCurrentLang = null;

		LexerToken lexer = mLexers.get(lang);
		if (lexer == null) {
			lexer = mLexers.get(null); //a generic lexer is always provided
		}

		String input = mStringComposer.concat(text);
		List<Sentence> sentences = lexer.split(input, lang, mParsingErrors);

		boolean[] isLexMark = new boolean[leaves.size()];
		for (int k = 0; k < isLexMark.length; ++k)
			isLexMark[k] = (leaves.get(k).formatting == null);
		List<SentencePointer> pointers = mStringComposer.makePointers(sentences, text,
		        isLexMark);

		mDuplicationManager.onNewSection();

		int sSegBoundary = -1;
		int sIndexBoundary = -1;
		for (SentencePointer sp : pointers) {
			//Gap between the previous sentence and the current sentence
			fillGap(sSegBoundary, sIndexBoundary, sp.boundaries.firstSegment,
			        sp.boundaries.firstIndex, leaves, text);

			sSegBoundary = sp.boundaries.lastSegment;
			sIndexBoundary = sp.boundaries.lastIndex;
			Leaf sentenceParent = deepestAncestorOf(leaves, sp.boundaries.firstSegment,
			        sp.boundaries.lastSegment);

			if (sentenceParent.formatting == null) {
				//it can happen if the section is composed only of punctuation
				//marks inserted to help the Lexer
				continue;
			}

			startLexingElement(sentenceParent, mSpecs.sentenceTag);
			int wSegBoundary = sp.boundaries.firstSegment;
			int wIndexBoundary = sp.boundaries.firstIndex;
			if (sp.content != null)
				for (TextPointer w : sp.content) {
					//Gap between the previous word and the current word
					fillGap(wSegBoundary, wIndexBoundary, w.firstSegment, w.firstIndex,
					        leaves, text);
					wSegBoundary = w.lastSegment;
					wIndexBoundary = w.lastIndex;
					addOneWord(w, leaves, text);
				}
			//Gap between the last words and the end of the sentence
			fillGap(wSegBoundary, wIndexBoundary, sp.boundaries.lastSegment,
			        sp.boundaries.lastIndex, leaves, text);

			closeAllElementsUntil(sentenceParent.formatting, 1);

		}
		//Gap between the last sentence and the end of the section
		fillGap(sSegBoundary, sIndexBoundary, -1, -1, leaves, text);
		mCurrentLang = null;
	}

	@Override
	public void onEmptySectionFound(List<Leaf> leaves) {
		mDuplicationManager.onNewSection();
		for (int i = 0; i < leaves.size(); ++i) {
			writeTree(leaves.get(i), null);
		}
	}

	private void addNode(XdmNode node, int level) {
		switch (node.getNodeKind()) {
		case ELEMENT:
			mDuplicationManager.onNewNode(node, level);
			mTreeWriter.addStartElement(node);
			mTreeWriter.addAttributes(node);
			mPreviousLevel = level;
			mPreviousNode = node;
			break;
		case COMMENT:
			mTreeWriter.addComment(node.getStringValue());
			mPreviousLevel = level - 1;
			mPreviousNode = node.getParent();
			break;
		case PROCESSING_INSTRUCTION:
			mTreeWriter.addPI(node.getNodeName().getClarkName(), node.getStringValue());
			mPreviousLevel = level - 1;
			mPreviousNode = node.getParent();
			break;
		default:
			mTreeWriter.addSubtree(node);
			mPreviousLevel = level - 1;
			mPreviousNode = node.getParent();
			break;
		}
	}

	private void startLexingElement(Leaf wordOrsentenceParent, QName elementToWrite) {
		XdmNode[] path = new XdmNode[wordOrsentenceParent.level + 1];

		//*** Find the deepest common ancestor. ***
		//Also, keep track of the path between the ancestor and the sentence's parent.
		XdmNode sentenceAncestor = wordOrsentenceParent.formatting;
		XdmNode lastNodeAncestor = mPreviousNode;
		int minLevel = Math.min(wordOrsentenceParent.level, mPreviousLevel);
		int topLevel = wordOrsentenceParent.level;
		for (; topLevel > minLevel; --topLevel) {
			path[topLevel] = sentenceAncestor;
			sentenceAncestor = sentenceAncestor.getParent();
		}
		for (int lastLevel = mPreviousLevel; lastLevel > minLevel; --lastLevel)
			lastNodeAncestor = lastNodeAncestor.getParent();

		for (; !same(sentenceAncestor, lastNodeAncestor); --topLevel) {
			path[topLevel] = sentenceAncestor;
			sentenceAncestor = sentenceAncestor.getParent();
			lastNodeAncestor = lastNodeAncestor.getParent();
		}

		//*** Close the elements between the last written elements and the found common ancestor ***
		// Sentences and words have already been closed by closeAllElementsUntil()
		while (!same(mPreviousNode, lastNodeAncestor)) {
			mTreeWriter.addEndElement();
			mPreviousNode = mPreviousNode.getParent();
		}

		//*** Add all the elements on the path between the sentence/word and the ancestor ***
		for (int l = topLevel + 1; l <= wordOrsentenceParent.level; ++l) {
			XdmNode n = path[l];
			addNode(n, l);
		}

		//*** Create the element ***
		mTreeWriter.addStartElement(elementToWrite);
		if (mCurrentLang != null) {
			//the lang attribute will be dispatched later when the format-compliant
			//elements will be created.
			mTreeWriter.addAttribute(mSpecs.langAttr, mCurrentLang);
		}
		mPreviousLevel = wordOrsentenceParent.level;
	}

	private void writeTree(Leaf leaf, String text) {
		if (text != null && text.isEmpty())
			return; //the fillGap can call writeTree with empty strings

		if (leaf.formatting == null)
			return; //this happens when punctuation marks has been added to help the Lexer

		XdmNode[] leafPath = new XdmNode[leaf.level + 1];

		//*** Find the deepest common ancestor. ***
		//Also, keep track of the path between the ancestor and the leaf.
		XdmNode leafAncestor = leaf.formatting;
		XdmNode lastNodeAncestor = mPreviousNode;
		int minLevel = Math.min(leaf.level, mPreviousLevel);
		int topLevel = leaf.level;
		for (; topLevel > minLevel; --topLevel) {
			leafPath[topLevel] = leafAncestor;
			leafAncestor = leafAncestor.getParent();
		}
		for (int lastLevel = mPreviousLevel; lastLevel > minLevel; --lastLevel)
			lastNodeAncestor = lastNodeAncestor.getParent();

		for (; !same(leafAncestor, lastNodeAncestor); --topLevel) {
			leafPath[topLevel] = leafAncestor;
			leafAncestor = leafAncestor.getParent();
			lastNodeAncestor = lastNodeAncestor.getParent();
		}

		//*** Close the elements between the last written elements and the found common ancestor ***
		// Sentences and words have already been closed by closeAllElementsUntil()
		while (!same(mPreviousNode, lastNodeAncestor)) {
			mTreeWriter.addEndElement();
			mPreviousNode = mPreviousNode.getParent();
		}

		//*** Open the new elements between the common ancestor (already opened) and the leaf (included) ***
		for (int l = topLevel + 1; l <= leaf.level; ++l) {
			XdmNode n = leafPath[l];
			addNode(n, l);
		}

		if (text != null) {
			mTreeWriter.addText(text);
			mPreviousLevel = leaf.level;
		}

	}

	/**
	 * @param dest is not closed.
	 * @param extra is used to close additional nodes like sentences or words
	 */
	private void closeAllElementsUntil(XdmNode dest, int extra) {
		while (!same(mPreviousNode, dest)) {
			mTreeWriter.addEndElement();
			mPreviousNode = mPreviousNode.getParent();
			--mPreviousLevel;
		}
		for (; extra > 0; --extra) {
			mTreeWriter.addEndElement();
		}
	}

	private void fillGap(int leftSegment, int leftIndex, int rightSegment, int rightIndex,
	        List<Leaf> leaves, List<String> text) {
		if (leftSegment == -1) {
			for (leftSegment = 0; leftSegment < text.size() && text.get(leftSegment) == null; ++leftSegment) {
				writeTree(leaves.get(leftSegment), null);
			}
			leftIndex = 0;
		}

		int formerRightSegment = rightSegment;
		if (rightSegment == -1) {
			rightSegment = text.size() - 1;
			formerRightSegment = rightSegment;
			for (; rightSegment >= 0 && text.get(rightSegment) == null; --rightSegment);
			if (rightSegment >= 0) {
				rightIndex = text.get(rightSegment).length();
			}
		}

		if (leftSegment < rightSegment) {
			writeTree(leaves.get(leftSegment), text.get(leftSegment).substring(leftIndex));

			for (int k = leftSegment + 1; k < rightSegment; ++k)
				writeTree(leaves.get(k), text.get(k));

			writeTree(leaves.get(rightSegment), text.get(rightSegment)
			        .substring(0, rightIndex));
		} else if (leftSegment == rightSegment && rightIndex > leftIndex) {
			writeTree(leaves.get(leftSegment), text.get(leftSegment).substring(leftIndex,
			        rightIndex));
		}

		for (++rightSegment; rightSegment <= formerRightSegment; ++rightSegment) {
			writeTree(leaves.get(rightSegment), null);
		}
	}

	//the return object is not really a Tree Leaf but it is convenient to carry the node's level
	private Leaf deepestAncestorOf(List<Leaf> leaves, int from, int to) {
		if (from == to) {
			return leaves.get(from);
		}

		int minLevel = Integer.MAX_VALUE;
		for (int k = from; k <= to; ++k)
			if (leaves.get(k).level < minLevel && leaves.get(k).formatting != null)
				minLevel = leaves.get(k).level;

		XdmNode[] all = new XdmNode[leaves.size()];
		int size = 0;
		for (int i = from; i <= to; ++i) {
			all[size] = leaves.get(i).formatting;
			if (all[size] != null) { //it can be null when the segment is added to help the Lexer
				for (int k = leaves.get(i).level - minLevel; k > 0; --k)
					all[size] = all[size].getParent();
				++size;
			}
		}

		while (true) {
			int i = 1;
			for (; i < size && same(all[i], all[0]); ++i);
			if (i >= size) {
				//ancestor found
				break;
			}
			for (i = 0; i < size; ++i)
				all[i] = all[i].getParent();
			--minLevel;
		}

		Leaf result = new Leaf();
		result.formatting = all[0];
		result.level = minLevel;

		return result;
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

	private static boolean same(XdmNode n1, XdmNode n2) {
		return ((n1 == null && n2 == null) || (n1 != null && n2 != null && n1
		        .getUnderlyingNode().isSameNodeInfo(n2.getUnderlyingNode())));
	}
}
