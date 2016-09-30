package org.daisy.pipeline.nlp.breakdetect;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.saxon.s9api.QName;

/**
 * Encapsulate what the Lexer needs to know about the input format so it can
 * parse and rebuild the input documents.
 */
public class FormatSpecifications {

	public QName sentenceTag;
	public QName wordTag;
	public String tmpNsPrefix = "tmp";
	public QName langAttr;
	public Set<String> inlineElements;
	public Set<String> ensureWordBefore;
	public Set<String> ensureWordAfter;
	public Set<String> ensureSentenceBefore;
	public Set<String> ensureSentenceAfter;
	public String tmpNs;

	FormatSpecifications(String tmpNamespace, String sentenceElement, String wordElement,
	        String langNamespace, String langAttr, Collection<String> inlineElements,
	        Collection<String> ensureWordBefore, Collection<String> ensureWordAfter,
	        Collection<String> ensureSentenceBefore, Collection<String> ensureSentenceAfter) {

		if (ensureWordBefore == null)
			ensureWordBefore = Collections.EMPTY_LIST;
		if (ensureWordAfter == null)
			ensureWordAfter = Collections.EMPTY_LIST;
		if (ensureSentenceBefore == null)
			ensureSentenceBefore = Collections.EMPTY_LIST;
		if (ensureSentenceAfter == null)
			ensureSentenceAfter = Collections.EMPTY_LIST;

		sentenceTag = new QName(tmpNamespace, sentenceElement);
		wordTag = new QName(tmpNamespace, wordElement);
		this.langAttr = new QName(langNamespace, langAttr);

		this.inlineElements = new HashSet<String>(inlineElements);
		this.inlineElements.addAll(ensureWordBefore);
		this.inlineElements.addAll(ensureWordAfter);
		this.inlineElements.addAll(ensureSentenceBefore);
		this.inlineElements.addAll(ensureSentenceAfter);
		this.inlineElements.add(wordElement);

		this.ensureWordBefore = new HashSet<String>(ensureWordBefore);
		this.ensureWordAfter = new HashSet<String>(ensureWordAfter);
		this.ensureSentenceBefore = new HashSet<String>(ensureSentenceBefore);
		this.ensureSentenceAfter = new HashSet<String>(ensureSentenceAfter);

		this.tmpNs = tmpNamespace;
	}
}
