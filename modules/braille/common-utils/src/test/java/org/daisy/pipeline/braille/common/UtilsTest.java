package org.daisy.pipeline.braille.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import static org.daisy.pipeline.braille.common.util.Iterables.combinations;
import static org.daisy.pipeline.braille.common.util.Strings.extractHyphens;

public class UtilsTest {
	
	@Test
	public void testExtractHyphens() {
		assertEquals("[0, 0, 1, 0, 0]", Arrays.toString(extractHyphens("foo\u00ADbar", '\u00AD')._2));
		assertEquals("[0, 0, 0, 2, 0, 0]", Arrays.toString(extractHyphens("foo-\u200Bbar", null, '\u200B')._2));
	}
	
	@Test
	public void testCombinations() {
		assertEquals(
			"[{a=1, b=4, c=5}, {a=2, b=4, c=5}, {a=3, b=4, c=5}, {a=1, b=4, c=6}, {a=2, b=4, c=6}, {a=3, b=4, c=6}]",
			printCollection(
				combinations(
					ImmutableMap.of("a", ImmutableList.of(1, 2, 3),
					                "b", ImmutableList.of(4),
					                "c", ImmutableList.of(5, 6)))));
	}
	
	private static String printCollection(Object o) {
		if (o instanceof Map) {
			Map m = (Map)o;
			if (m.isEmpty())
				return "{}";
			List keys = new ArrayList<String>();
			for (Object k : m.keySet())
				keys.add(printCollection(k));
			Collections.sort(keys);
			Iterator<String> i = keys.iterator();
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			while (true) {
				String k = i.next();
				sb.append(k);
				sb.append('=');
				sb.append(printCollection(m.get(k)));
				if (!i.hasNext())
					return sb.append('}').toString();
				sb.append(',').append(' '); }}
		else if (o instanceof Iterable) {
			Iterator i = ((Iterable)o).iterator();
			if (!i.hasNext())
				return "[]";
			StringBuilder sb = new StringBuilder();
			sb.append('[');
			while (true) {
				sb.append(printCollection(i.next()));
				if (!i.hasNext())
					return sb.append(']').toString();
				sb.append(',').append(' '); }}
		else
			return o.toString();
	}
}
