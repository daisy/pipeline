package org.daisy.pipeline.nlp.lexing.ruled.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.daisy.pipeline.nlp.lexing.LexResultPrettyPrinter;
import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;
import org.daisy.pipeline.nlp.lexing.LexService.Sentence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class LexingTest {

	private LexResultPrettyPrinter mPrinter;
	private LexerToken mLexerToken;
	private RuleBasedLexer mLexer;

	@Before
	public void setUp() throws LexerInitException {
		mPrinter = new LexResultPrettyPrinter();
		mLexer = new RuleBasedLexer();
		mLexer.globalInit();
		mLexerToken = mLexer.newToken();
		mLexerToken.addLang(Locale.ENGLISH);
	}

	@After
	public void shutdown() {
		mLexer.globalRelease();
	}

	@Test
	public void basicSplit() throws LexerInitException {
		String inp = "this is a   basic test";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);

		Assert.assertEquals("{/this/ /is/ /a/   /basic/ /test/}", text);
	}

	@Test
	public void twoSentences1() throws LexerInitException {
		String inp = "first sentence. Second sentence";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);

		Assert.assertEquals("{/first/ /sentence/.}{/Second/ /sentence/}", text);
	}

	@Test
	public void twoSentences2() throws LexerInitException {
		String inp = "first sentence! Second sentence";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);

		Assert.assertEquals("{/first/ /sentence/!}{/Second/ /sentence/}", text);
	}

	@Test
	public void noSentence1() throws LexerInitException {
		String inp = "first block. 55";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/first/ /block/. /55/}", text);
	}

	@Test
	public void capitalizedWords() throws LexerInitException {
		String inp = "Only One Sentence";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/Only/ /One/ /Sentence/}", text);
	}

	@Test
	public void acronym1() throws LexerInitException {
		String inp = "test A.C.R.O.N.Y.M. other";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/test/ /A.C.R.O.N.Y.M./ /other/}", text);
	}

	@Test
	public void acronym2() throws LexerInitException {
		String inp = "test A.C.R.O.N.Y.M. Other";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/test/ /A.C.R.O.N.Y.M/.}{/Other/}", text);
	}

	@Test
	public void httpAddress() throws LexerInitException {
		String link = "http://www.google.fr/toto?a=b&_sessid=4547";
		String inp = "before " + link + " after";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/before/ /" + link + "/ /after/}", text);
	}

	@Ignore
	@Test
	public void latin() throws LexerInitException {
		String inp = "a priori a posteriori";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/a priori/ /a posteriori/}", text);
	}

	@Test
	public void whitespaces1() throws LexerInitException {
		String inp = "     sentence1.       Sentence2   ";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/sentence1/.}{/Sentence2/}", text);
	}

	@Test
	public void punctuationOnly1() throws LexerInitException {
		String inp = "sentence1! ??!!!  !! ? sentence2! ";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/sentence1/!}{/sentence2/!}", text);

	}

	@Test
	public void foreign() throws LexerInitException {
		String inp = "découpage basé sur des règles";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/découpage/ /basé/ /sur/ /des/ /règles/}", text);
	}

	@Test
	public void brackets1() throws LexerInitException {
		String ref = "Bracket example (this is not a sentence!), after.";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals(
		        "{/Bracket/ /example/ (/this/ /is/ /not/ /a/ /sentence/!), /after/.}", text);
	}

	@Test
	public void quotes1() throws LexerInitException {
		String ref = "\"First sentence.\" Second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{\"/First/ /sentence/.\"}{/Second/ /sentence/}", text);
	}

	@Test
	public void quotes2() throws LexerInitException {
		String ref = "\"First sentence\". Second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{\"/First/ /sentence/\".}{/Second/ /sentence/}", text);
	}

	@Test
	public void quotes3() throws LexerInitException {
		String ref = "\"First sentence. \" Second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{\"/First/ /sentence/. \"}{/Second/ /sentence/}", text);
	}

	@Test
	public void quotes4() throws LexerInitException {
		String ref = "First sentence. » Second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/First/ /sentence/. »}{/Second/ /sentence/}", text);
	}

	@Test
	public void quotes5() throws LexerInitException {
		String ref = "First block: \"Second block";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);

		Assert.assertEquals("{/First/ /block/:}{\"/Second/ /block/}", text);
	}

	@Test
	public void weirdSentence1() throws LexerInitException {
		String ref = "Then he asked this and that?, etc.";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/Then/ /he/ /asked/ /this/ /and/ /that/?, /etc/.}", text);
	}

	@Test
	public void weirdSentence2() throws LexerInitException {
		String ref = "He sighed…: Bla bla.";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/He/ /sighed/…:}{/Bla/ /bla/.}", text);
	}

	@Test
	public void openingQuote() {
		String inp = "end. “Begin";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/end/.}{“/Begin/}", text);
	}

	@Test
	public void quotedQuestion() {
		String inp = "He asked me \"what is this?\" and";
		List<Sentence> sentences = mLexerToken.split(inp, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, inp);
		Assert.assertEquals("{/He/ /asked/ /me/ \"/what/ /is/ /this/?\" /and/}", text);
	}
}
