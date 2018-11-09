package org.daisy.dotify.common.text;

import org.junit.Test;
import static org.junit.Assert.*;

@SuppressWarnings("javadoc")
public class ConditionalMapperTest {

	@Test
	public void testMapper_01() {
		String actual = ConditionalMapper.translate("bar", "abc", "ABC");
		assertEquals("BAr", actual);
	}
	
	@Test
	public void testMapper_02() {
		String actual = ConditionalMapper.translate("--aaa--","abc-","ABC");
		assertEquals("AAA", actual);
	}
	
	@Test
	public void testMapper_03() {
		String actual = ConditionalMapper.withTrigger('(').map("123-","abc").build().replace("123(123)");
		assertEquals("123(abc)", actual);
	}

}
