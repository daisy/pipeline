package org.daisy.braille.utils.pef;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class SearchIndexTest {

	@Test
	public void testSearch_01() {
		SearchIndex<String> si = new SearchIndex<>();
		String obj1 = "1";
		String obj2 = "2";
		si.add("house", obj1);
		si.add("boat", obj1);
		si.add("house", obj2);
		si.add("food", obj2);
		assertEquals(obj1, si.containsAll("house boat").iterator().next());
		assertEquals(0, si.containsAll("boat food").size());
		assertEquals(2, si.containsAll("house").size());
	}

	@Test
	public void testSubstringSearch() {
		SearchIndex<String> si = new SearchIndex<>();
		String obj1 = "1";
		String obj2 = "2";
		si.add("house", obj1);
		si.add("boat", obj1);
		si.add("house", obj2);
		si.add("food", obj2);
		assertEquals(obj1, si.containsAll("hous boa").iterator().next());
		assertEquals(0, si.containsAll("boa foo").size());
		assertEquals(2, si.containsAll("hous").size());
	}

	@Test
	public void testStrictTerms() {
		SearchIndex<String> si = new SearchIndex<>();
		String obj1 = "1";
		String obj2 = "2";
		si.add("house", obj1, true);
		si.add("boat", obj1, true);
		si.add("house", obj2, true);
		si.add("food", obj2, true);
		assertEquals(0, si.containsAll("hous boa").size());
		assertEquals(0, si.containsAll("hous").size());
	}

	@Test
	public void testRedundantAdditions() {
		SearchIndex<String> si = new SearchIndex<>();
		String obj1 = "1";
		String obj2 = "2";
		si.add("house", obj1);
		si.add("house", obj1);
		si.add("hous", obj1);
		si.add("boat", obj1);
		si.add("house", obj2);
		si.add("food", obj2);
		assertEquals(obj1, si.containsAll("house boat").iterator().next());
		assertEquals(0, si.containsAll("boat food").size());
		assertEquals(2, si.containsAll("house").size());
	}

}
