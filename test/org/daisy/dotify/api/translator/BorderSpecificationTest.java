package org.daisy.dotify.api.translator;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.translator.BorderSpecification.Style;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class BorderSpecificationTest {

	@Test
	public void testBuildBorder() {
		BorderSpecification.Builder def = new BorderSpecification.Builder();
		def.align(BorderSpecification.Align.INNER);
		BorderSpecification override = new BorderSpecification.Builder().style(Style.SOLID).build(def.build());
		//from override
		assertEquals(Style.SOLID, override.getStyle());
		//default (not set)
		assertEquals(1, override.getWidth());
		//default (set)
		assertEquals(BorderSpecification.Align.INNER, override.getAlign());
	}
	
	@Test
	public void testAlignInner() {
		assertEquals(3, BorderSpecification.Align.INNER.align(3));
	}
	
	@Test
	public void testAlignOuter() {
		assertEquals(0, BorderSpecification.Align.OUTER.align(3));
	}
	
	@Test
	public void testAlignCenter() {
		assertEquals(2, BorderSpecification.Align.CENTER.align(3));
	}
}
