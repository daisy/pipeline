package org.daisy.dotify.translator;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
@SuppressWarnings("javadoc")
public class RegexMarkerDictionaryTest {

	@Test
	public void testDefaultFilter() throws MarkerNotFoundException, MarkerNotCompatibleException {
		Marker m = new Marker("1", "2");
		RegexMarkerDictionary d = new RegexMarkerDictionary.Builder().addPattern(".*", m).build();
		Marker ret = d.getMarkersFor("test", null);
		assertEquals(m, ret);
	}

}
