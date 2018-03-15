package org.daisy.pipeline.braille.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class QueryTest {
	
	@Test
	public void testSerializeQuery() {
		Query.MutableQuery q = Query.util.mutableQuery();
		q.add("locale", "en-US");
		q.add("grade", "2");
		q.add("foo");
		q.add("bar", "ds<:;dsqf");
		assertEquals("(locale:en-US)(grade:2)(foo)(bar:'ds<:;dsqf')",
		             q.toString());
	}
	
	@Test
	public void testParseQuery() {
		Query.MutableQuery q = Query.util.mutableQuery();
		q.add("locale", "en-US");
		q.add("grade", "2");
		q.add("foo");
		q.add("locale", "fr");
		assertEquals(q, Query.util.query(" (locale:en-US ) ( grade: 2)(foo) (locale:fr)"));
	}
	
	@Test
	public void testParseAndSerializeQuery() {
		assertEquals("(locale:en-US)(grade:2)(foo)(locale:fr)",
		             Query.util.query(" (locale:en-US ) ( grade: 2)(foo) (locale:fr)").toString());
	}
}
