package org.daisy.pipeline.braille.common;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class QueryTest {
	
	@Test
	public void testSerializeQuery() {
		Query.MutableQuery q = Query.util.mutableQuery();
		q.add("locale", "en-US");
		q.add("locale", "ar");
		q.add("foo");
		q.add("bar", "ds<:;'\"dsqf");
		q.add("grade", "2");
		assertEquals("(bar:\"ds<:;'\\22 dsqf\")(foo)(grade:2)(locale:en-US)(locale:ar)",
		             q.toString());
	}
	
	@Test
	public void testParseQuery() {
		Query.MutableQuery q = Query.util.mutableQuery();
		q.add("locale", "en-US");
		q.add("grade", "2");
		q.add("foo");
		q.add("bar", "ds<:;'\"dsqf");
		q.add("locale", "fr");
		assertEquals(q, Query.util.query(" (locale:en-US ) ( grade: 2)(foo)(bar:\"ds<:;'\\22 dsqf\") (locale:fr)"));
	}
}
