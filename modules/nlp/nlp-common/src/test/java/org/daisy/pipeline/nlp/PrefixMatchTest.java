package org.daisy.pipeline.nlp;

import java.util.Arrays;

import org.daisy.pipeline.nlp.impl.PrefixMatchStringFinder;
import org.junit.Assert;
import org.junit.Test;

public class PrefixMatchTest {
	@Test
	public void one() {
		String s = "test";
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList(s));
		Assert.assertEquals(s, finder.find(s + "aa"));
	}

	@Test
	public void two() {
		String s = "test";
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList(s, "other"));
		Assert.assertEquals(s, finder.find(s + "aa"));
	}

	@Test
	public void nothing() {
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList("something", "other"));
		Assert.assertTrue(null == finder.find("anything"));
	}

	@Test
	public void inclusion1() {
		String s = "test";
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList(s, s + "-"));
		Assert.assertEquals(s, finder.find(s + "aa"));
	}

	@Test
	public void inclusion2() {
		String s = "test";
		String s2 = s + "-";
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList(s, s2));
		Assert.assertEquals(s2, finder.find(s2 + "aa"));
	}

	@Test
	public void inclusion3() {
		String s = "test";
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList(s + "-", s));
		Assert.assertEquals(s, finder.find(s + "aa"));
	}

	@Test
	public void inclusion4() {
		String s = "test";
		String s2 = s + "-";
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList(s2, s));
		Assert.assertEquals(s2, finder.find(s2 + "aa"));
	}

	@Test
	public void unicode() {
		String s = "மூலூ";
		PrefixMatchStringFinder finder = new PrefixMatchStringFinder();
		finder.compile(Arrays.asList(s));
		Assert.assertEquals(s, finder.find(s + "aa"));
	}
}
