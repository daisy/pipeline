package org.daisy.pipeline.nlp.lexing.light.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.daisy.pipeline.nlp.lexing.LexResultPrettyPrinter;
import org.daisy.pipeline.nlp.lexing.LexService;
import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;
import org.daisy.pipeline.nlp.lexing.LexService.Sentence;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LightLexingTest {

	LexResultPrettyPrinter mPrinter;
	LexService mLexer;
	LexerToken mLexerToken;

	static Locale SPANISH;
	static Locale CHINESE;
	static Locale ARABIC;

	static {
		if (System.getProperty("java.version").startsWith("1.7.")) {
			SPANISH = new Locale("spa");
			CHINESE = new Locale("zho");
			ARABIC = new Locale("ara");
		} else {
			SPANISH = new Locale("es");
			CHINESE = new Locale("zh");
			ARABIC = new Locale("ar");
		}
	}

	@Before
	public void setUp() throws LexerInitException {
		mPrinter = new LexResultPrettyPrinter();
		mLexer = new LightLexer();
		mLexer.globalInit();
		mLexerToken = mLexer.newToken();
		mLexerToken.addLang(Locale.ENGLISH);
		mLexerToken.addLang(SPANISH);
	}

	@Test
	public void twoSentences() throws LexerInitException {
		String ref = "first sentence! Second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{first sentence!}{Second sentence}", text);
	}

	@Test
	public void spanish() throws LexerInitException {
		String ref = "first sentence! ¿Second sentence?";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		//the question mark is captured by the second sentence
		Assert.assertEquals("{first sentence!}{¿Second sentence?}", text);
	}

	@Test
	public void mixed() throws LexerInitException {
		String ref = "first sentence !!... second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{first sentence !!...}{second sentence}", text);
	}

	@Test
	public void malformed() throws LexerInitException {
		String ref = "!!! first sentence  ! second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{!!! first sentence  !}{second sentence}", text);
	}

	@Test
	public void whitespaces1() throws LexerInitException {
		String ref = "first sentence !!  !! second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{first sentence !!  !!}{second sentence}", text);
	}

	@Test
	public void whitespaces2() throws LexerInitException {
		String ref = "first sentence !!  ¿¿ second sentence ?!";
		List<Sentence> sentences = mLexerToken.split(ref, SPANISH, new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{first sentence !!}{¿¿ second sentence ?!}", text);
	}

	@Test
	public void newline1() throws LexerInitException {
		String ref = "\n";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("", text);
	}

	@Test
	public void newline2() throws LexerInitException {
		String ref = "\n  \n\n\n  \t\n ";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("", text);
	}

	@Test
	public void newline3() throws LexerInitException {
		String ref = "text text ? \t\n ";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{text text ?}", text);
	}

	@Test
	public void brackets1() throws LexerInitException {
		String ref = "Bracket example (this is not a sentence!), after.";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{Bracket example (this is not a sentence!), after.}", text);
	}
}
