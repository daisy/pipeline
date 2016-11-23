package org.daisy.dotify.api.translator;

import static org.junit.Assert.assertEquals;

import org.daisy.dotify.api.translator.BorderSpecification.Align;
import org.daisy.dotify.api.translator.BorderSpecification.Style;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class BorderTest {

	@Test
	public void testBorder() {
		Border b = new Border.Builder()
				.getDefault()
					.align(Align.CENTER)
				.getBottom()
					.style(Style.SOLID)
					.width(2)
				.build();
		
		assertEquals(Align.CENTER, b.getBottom().getAlign());
		assertEquals(Align.CENTER, b.getLeft().getAlign());
		assertEquals(1, b.getRight().getWidth());
		assertEquals(2, b.getBottom().getWidth());
		assertEquals(Style.NONE, b.getTop().getStyle());
		assertEquals(Style.SOLID, b.getBottom().getStyle());
	}

}
