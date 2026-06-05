package org.daisy.pipeline.nlp;

import java.io.IOException;

import org.daisy.pipeline.nlp.TextCategorizer.CategorizedWord;
import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;
import org.daisy.pipeline.nlp.ruledcategorizers.RuledMultilangCategorizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RuledCategorizerTest {
	private TextCategorizer mCategorizer;

	@Before
	public void setUp() throws IOException {
		mCategorizer = new RuledMultilangCategorizer();
	}

	public CategorizedWord categorizeForPrefix(String x) throws IOException {
		mCategorizer.init(MatchMode.PREFIX_MATCH);
		mCategorizer.compile();
		CategorizedWord v = mCategorizer.categorize(x + " foo", x.toLowerCase());
		if (v == null) {
			v = new CategorizedWord();
			v.category = Category.UNKNOWN;
			v.word = "";
		}
		return v;
	}

	public CategorizedWord categorizeWithEndDelimiter(String x) throws IOException {
		mCategorizer.init(MatchMode.PREFIX_MATCH);
		mCategorizer.compile();
		CategorizedWord v = mCategorizer.categorize(x, x.toLowerCase());
		if (v == null) {
			v = new CategorizedWord();
			v.category = Category.UNKNOWN;
			v.word = "";
		}
		return v;
	}

	public CategorizedWord categorizeFullMode(String x) throws IOException {
		mCategorizer.init(MatchMode.FULL_MATCH);
		mCategorizer.compile();
		CategorizedWord v = mCategorizer.categorize(x, x.toLowerCase());
		if (v == null) {
			v = new CategorizedWord();
			v.category = Category.UNKNOWN;
			v.word = "";
		}
		return v;
	}

	@Test
	public void date1() throws IOException {
		String date = "2008-12-24";
		CategorizedWord w = categorizeForPrefix(date);
		Assert.assertEquals(date, w.word);
		Assert.assertEquals(Category.DATE, w.category);
	}

	@Test
	public void date2() throws IOException {
		String date = "30/07/2002";
		CategorizedWord w = categorizeForPrefix(date);
		Assert.assertEquals(date, w.word);
		Assert.assertEquals(Category.DATE, w.category);
	}

	@Test
	public void date3() throws IOException {
		String date = "08/1982";
		CategorizedWord w = categorizeForPrefix(date);
		Assert.assertEquals(date, w.word);
		Assert.assertEquals(Category.DATE, w.category);
	}

	@Test
	public void not_date2() throws IOException {
		String d = "1998-20-18";
		CategorizedWord w = categorizeForPrefix(d);
		Assert.assertNotSame(w.category, Category.DATE);
	}

	@Test
	public void not_date3() throws IOException {
		String d = "1998-11-42";
		CategorizedWord w = categorizeForPrefix(d);
		Assert.assertNotSame(w.category, Category.DATE);
	}

	@Test
	public void not_date4() throws IOException {
		CategorizedWord w = categorizeForPrefix("18/20/1998");
		Assert.assertNotSame(Category.DATE, w.category);
	}

	@Test
	public void not_date5() throws IOException {
		CategorizedWord w = categorizeForPrefix("42/11/1998");
		Assert.assertNotSame(Category.DATE, w.category);
	}

	@Test
	public void not_date6() throws IOException {
		String d = "00/10/1985";
		CategorizedWord w = categorizeForPrefix(d);
		Assert.assertNotSame(w.category, Category.DATE);
	}

	@Test
	public void not_date7() throws IOException {
		String d = "5/00/1985";
		CategorizedWord w = categorizeForPrefix(d);
		Assert.assertNotSame(w.category, Category.DATE);
	}

	@Test
	public void not_date8() throws IOException {
		String date = "08/1982b";
		CategorizedWord w = categorizeForPrefix(date);
		Assert.assertNotSame(w.category, Category.DATE);
	}

	@Test
	public void floatQuantity1() throws IOException {
		String f = "98.32";
		CategorizedWord w = categorizeForPrefix(f);
		Assert.assertEquals(f, w.word);
		Assert.assertEquals(Category.QUANTITY, w.category);
	}

	@Test
	public void floatQuantity2() throws IOException {
		String f = "24 198.32";
		CategorizedWord w = categorizeForPrefix(f);
		Assert.assertEquals(f, w.word);
		Assert.assertEquals(Category.QUANTITY, w.category);
	}

	@Test
	public void longQuantity1() throws IOException {
		String f = "321,154,757";
		CategorizedWord w = categorizeForPrefix(f);
		Assert.assertEquals(f, w.word);
		Assert.assertEquals(Category.QUANTITY, w.category);
	}

	@Test
	public void longQuantity2() throws IOException {
		String f = "321'154'757";
		CategorizedWord w = categorizeForPrefix(f);
		Assert.assertEquals(f, w.word);
		Assert.assertEquals(Category.QUANTITY, w.category);
	}

	@Test
	public void fakeQuantity1() throws IOException {
		CategorizedWord w = categorizeForPrefix("32 42");
		Assert.assertEquals("32", w.word);
		Assert.assertEquals(Category.QUANTITY, w.category);
	}

	@Test
	public void fakeQuantity2() throws IOException {
		CategorizedWord w = categorizeForPrefix("042");
		Assert.assertNotSame(Category.QUANTITY, w.category);
	}

	@Test
	public void currency1() throws IOException {
		String cur = "$35.02";
		CategorizedWord w = categorizeForPrefix(cur);
		Assert.assertEquals(cur, w.word);
		Assert.assertEquals(Category.CURRENCY, w.category);
	}

	@Test
	public void currency2() throws IOException {
		String cur = "35.02$";
		CategorizedWord w = categorizeForPrefix(cur);
		Assert.assertEquals(cur, w.word);
		Assert.assertEquals(Category.CURRENCY, w.category);
	}

	@Test
	public void emailaddr1() throws IOException {
		String email = "an.email@gmail.com";
		CategorizedWord w = categorizeForPrefix(email);
		Assert.assertEquals(email, w.word);
		Assert.assertEquals(Category.EMAIL_ADDR, w.category);
	}

	@Test
	public void emailaddr2() throws IOException {
		String email = "an.email(at)gmail.com";
		CategorizedWord w = categorizeForPrefix(email);
		Assert.assertEquals(email, w.word);
		Assert.assertEquals(Category.EMAIL_ADDR, w.category);
	}

	@Test
	public void not_emailaddr() throws IOException {
		String email = "an.email@_gmail.com";
		CategorizedWord w = categorizeForPrefix(email);
		Assert.assertNotSame(Category.EMAIL_ADDR, w.category);
	}

	@Test
	public void ftp() throws IOException {
		String l = "ftp://google.co.uk/to-to?a=b&_sessid=4547";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.WEB_LINK, w.category);
	}

	@Test
	public void multilang1() throws IOException {
		String l = "écrire-en-français";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.COMMON, w.category);
	}

	@Test
	public void time1() throws IOException {
		String l = "12:31";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.TIME, w.category);
	}

	@Test
	public void time_fullmode() throws IOException {
		String l = "12:31";
		CategorizedWord w = categorizeFullMode(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.TIME, w.category);
	}

	@Test
	public void time_endsep() throws IOException {
		String l = "12:31";
		CategorizedWord w = categorizeWithEndDelimiter(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.TIME, w.category);
	}

	@Test
	public void not_time1() throws IOException {
		String l = "12:311";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertNotSame(Category.TIME, w.category);
	}

	@Test
	public void initialism1() throws IOException {
		String l = "J.B.";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.ACRONYM, w.category);
	}

	@Test
	public void initialism2() throws IOException {
		String l = "J.-B.";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.ACRONYM, w.category);
	}

	@Test
	public void initialism3() throws IOException {
		String l = "R.";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.ACRONYM, w.category);
	}

	@Test
	public void smallabbr() throws IOException {
		String l = "p. III";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals("p.", w.word);
		Assert.assertEquals(Category.ABBREVIATION, w.category);
	}

	@Test
	public void numbering1() throws IOException {
		String item = "35.2.";
		String l = item + " test";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(item, w.word);
		Assert.assertEquals(Category.NUMBERING_ITEM, w.category);
	}

	@Test
	public void quotes1() throws IOException {
		String item = "”";
		String l = item + "after";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(item, w.word);
		Assert.assertEquals(Category.QUOTE, w.category);
	}
}
