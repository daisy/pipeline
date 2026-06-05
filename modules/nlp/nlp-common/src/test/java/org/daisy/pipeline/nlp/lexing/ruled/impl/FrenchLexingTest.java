package org.daisy.pipeline.nlp.lexing.ruled.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.daisy.pipeline.nlp.lexing.LexResultPrettyPrinter;
import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;
import org.daisy.pipeline.nlp.lexing.LexService.Sentence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FrenchLexingTest {
	private LexResultPrettyPrinter mPrinter;
	private LexerToken mLexerToken;
	private RuleBasedLexer mLexer;

	@Before
	public void setUp() throws LexerInitException {
		mPrinter = new LexResultPrettyPrinter();
		mLexer = new RuleBasedLexer();
		mLexer.globalInit();
		mLexerToken = mLexer.newToken();
		mLexerToken.addLang(Locale.FRENCH);
	}

	@Test
	public void apostrophe() throws LexerInitException {
		String inp = "la raison d'être";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.FRENCH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/la/ /raison/ /d'//être/}", text);
	}

	@Test
	public void abbr1() {
		String inp = "fausse-abbr. Majuscule";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.FRENCH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/fausse-abbr/.}{/Majuscule/}", text);
	}

	@Test
	public void abbr2() {
		String inp = "mr. Majuscule";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.FRENCH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/mr./ /Majuscule/}", text);
	}
	
	@Test
	public void openingQuote(){
		String inp = "fin. « Début";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.FRENCH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/fin/.}{« /Début/}", text);
	}

}
