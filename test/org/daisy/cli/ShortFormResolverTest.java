package org.daisy.cli;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShortFormResolverTest {

	@Test
	public void testShortFormResolver_01() {
		String id1 = "org.daisy.braille.cli.impl1";
		String id2 = "org.daisy.braille.cli.impl2";
		ShortFormResolver sf = new ShortFormResolver(id1, id2);
		assertEquals("impl1", sf.getShortForm(id1));
		assertEquals("impl2", sf.getShortForm(id2));
		assertEquals(id1, sf.resolve("impl1"));
		assertEquals(id2, sf.resolve("impl2"));
	}
}
