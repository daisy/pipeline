package org.daisy.pipeline.nlp.lexing.omni;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.daisy.pipeline.nlp.lexing.LexResultPrettyPrinter;
import org.daisy.pipeline.nlp.lexing.LexService;
import org.daisy.pipeline.nlp.lexing.LexService.LexerInitException;
import org.daisy.pipeline.nlp.lexing.LexService.LexerToken;
import org.daisy.pipeline.nlp.lexing.LexService.Sentence;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class OmnilangTest {

	LexResultPrettyPrinter mPrinter;
	LexerToken mLexerToken;
	LexService mLexer;

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
		mLexer = new OmnilangLexer();
		mLexer.globalInit();
		mLexerToken = mLexer.newToken();
		mLexerToken.addLang(Locale.ENGLISH);
		mLexerToken.addLang(Locale.FRENCH);
		mLexerToken.addLang(SPANISH);
		mLexerToken.addLang(CHINESE);
		mLexerToken.addLang(ARABIC);
	}

	@After
	public void shutDown() {
		mLexer.globalRelease();
	}

	@Test
	public void twoSentences() throws LexerInitException {
		String ref = "first sentence! Second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/first/ /sentence/! }{/Second/ /sentence/}", text);
	}

	@Test
	public void mixed() throws LexerInitException {
		String ref = "first sentence !!... second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/first/ /sentence/ !!... }{/second/ /sentence/}", text);
	}

	@Ignore
	@Test
	public void whitespaces1() throws LexerInitException {
		String ref = "first sentence !!  !! second sentence";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/first/ /sentence/ !! !! }{/second/ /sentence/}", text);
	}

	@Test
	public void spanish1() throws LexerInitException {
		String ref = "first sentence. ¿Second sentence?";
		List<Sentence> sentences = mLexerToken.split(ref, SPANISH, new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/first/ /sentence/. }{¿/Second/ /sentence/?}", text);
	}

	@Ignore
	@Test
	public void spanish2() throws LexerInitException {
		String ref = "first sentence. ¿ Second sentence ?";
		List<Sentence> sentences = mLexerToken.split(ref, SPANISH, new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/first/ /sentence/. }{¿ /Second/ /sentence/ ?}", text);
	}

	@Test
	public void chinese() throws LexerInitException {
		String ref = "我喜欢中国。我喜欢英语了。";
		List<Sentence> sentences = mLexerToken.split(ref, CHINESE, new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/我喜欢中国/。}{/我喜欢英语了/。}", text);
	}

	@Test
	public void newline() throws LexerInitException {
		String ref = "They do like\nJames.";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/They/ /do/ /like/\n/James/.}", text);
	}

	@Test
	public void abbr1() throws LexerInitException {
		String ref = "J.J.R. Tolkien";
		List<Sentence> sentences = mLexerToken.split(ref, Locale.ENGLISH,
		        new ArrayList<String>());
		String text = mPrinter.convert(sentences, ref);
		Assert.assertEquals("{/J.J.R./ /Tolkien/}", text);
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
}
