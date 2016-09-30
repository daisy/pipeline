package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.junit.Test;

public class DefaultTextAttributeTest {

	@Test(expected = IllegalArgumentException.class)
	public void testTranslatorAttributes_UnmatchedAttributesList() {
		new DefaultTextAttribute.Builder()
			.add(new DefaultTextAttribute.Builder("bold").build(10))
			.build(105);
	}

}
