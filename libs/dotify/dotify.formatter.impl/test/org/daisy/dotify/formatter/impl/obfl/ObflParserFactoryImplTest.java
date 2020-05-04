package org.daisy.dotify.formatter.impl.obfl;

import org.daisy.dotify.api.obfl.ObflParserFactoryMaker;
import org.daisy.dotify.api.obfl.ObflParserFactoryService;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * TODO: Write java doc.
 */
@SuppressWarnings("javadoc")
public class ObflParserFactoryImplTest {

    @Test
    public void testFactory() {
        ObflParserFactoryService factory = ObflParserFactoryMaker.newInstance().getFactory();
        assertNotNull(factory);
    }
}
