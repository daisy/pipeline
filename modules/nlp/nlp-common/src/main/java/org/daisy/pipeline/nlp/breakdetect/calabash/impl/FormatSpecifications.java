package org.daisy.pipeline.nlp.breakdetect.calabash.impl;

import java.util.function.Predicate;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

/**
 * Encapsulate what the Lexer needs to know about the input format so it can
 * parse and rebuild the input documents.
 */
public class FormatSpecifications {

	public QName sentenceTag;
	public QName wordTag;
	public QName langAttr;
	public Predicate<XdmNode> inlineElements;
	public Predicate<XdmNode> ensureWordBefore;
	public Predicate<XdmNode> ensureWordAfter;
	public Predicate<XdmNode> ensureSentenceBefore;
	public Predicate<XdmNode> ensureSentenceAfter;

	FormatSpecifications(QName sentenceElement, QName wordElement,
	        String langNamespace, String langAttr, Predicate<XdmNode> inlineElements,
	        Predicate<XdmNode> ensureWordBefore, Predicate<XdmNode> ensureWordAfter,
	        Predicate<XdmNode> ensureSentenceBefore, Predicate<XdmNode> ensureSentenceAfter) {
		sentenceTag = sentenceElement;
		wordTag = wordElement;
		this.langAttr = new QName(langNamespace, langAttr);
		this.inlineElements = inlineElements.or(ensureWordBefore)
		                                    .or(ensureWordAfter)
		                                    .or(ensureSentenceBefore)
		                                    .or(ensureSentenceAfter)
		                                    .or(x -> wordTag.equals(x.getNodeName()));
		this.ensureWordBefore = ensureWordBefore;
		this.ensureWordAfter = ensureWordAfter;
		this.ensureSentenceBefore = ensureSentenceBefore;
		this.ensureSentenceAfter = ensureSentenceAfter;
	}
}
