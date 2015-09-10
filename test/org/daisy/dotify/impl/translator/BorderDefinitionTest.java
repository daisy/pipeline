package org.daisy.dotify.impl.translator;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.impl.translator.BorderSpecification.Style;
import org.junit.Test;

public class BorderDefinitionTest {

	@Test
	public void testBuildBorder() {
		BorderSpecification def = new BorderSpecification();
		def.setAlign(BorderSpecification.Align.INNER);
		BorderSpecification override = new BorderSpecification(def);
		override.setStyle(Style.SOLID);
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
