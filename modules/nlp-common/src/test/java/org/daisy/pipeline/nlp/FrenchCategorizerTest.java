package org.daisy.pipeline.nlp;

import java.io.IOException;

import org.daisy.pipeline.nlp.TextCategorizer.CategorizedWord;
import org.daisy.pipeline.nlp.TextCategorizer.Category;
import org.daisy.pipeline.nlp.TextCategorizer.MatchMode;
import org.daisy.pipeline.nlp.ruledcategorizers.RuledFrenchCategorizer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FrenchCategorizerTest {
	private TextCategorizer mCategorizer;

	@Before
	public void setUp() throws IOException {
		mCategorizer = new RuledFrenchCategorizer();
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

	@Test
	public void time1() throws IOException {
		String l = "12 h 31";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.TIME, w.category);
	}

	@Test
	public void abbr1() throws IOException {
		String l = "ie.";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.ABBREVIATION, w.category);
	}

	@Test
	public void abbr2() throws IOException {
		String l = "i.e.";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.ABBREVIATION, w.category);
	}

	@Test
	public void abbr3() throws IOException {
		String l = "mr.";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.ABBREVIATION, w.category);
	}

	@Test
	public void abbr4() throws IOException {
		String l = "c.-à.-d.";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals(l, w.word);
		Assert.assertEquals(Category.ABBREVIATION, w.category);
	}

	@Test
	public void apostrophe() throws IOException {
		String l = "l'avant";
		CategorizedWord w = categorizeForPrefix(l);
		Assert.assertEquals("l'", w.word);
		Assert.assertEquals(Category.COMMON, w.category);
	}
}
