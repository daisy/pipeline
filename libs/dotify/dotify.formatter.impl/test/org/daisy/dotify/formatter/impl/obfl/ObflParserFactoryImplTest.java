package org.daisy.dotify.formatter.impl.obfl;

import static org.junit.Assert.assertNotNull;

import org.daisy.dotify.api.obfl.ObflParserFactoryMaker;
import org.daisy.dotify.api.obfl.ObflParserFactoryService;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class ObflParserFactoryImplTest {

	@Test
	public void testFactory() {
		ObflParserFactoryService factory = ObflParserFactoryMaker.newInstance().getFactory();
		assertNotNull(factory);
	}
}
