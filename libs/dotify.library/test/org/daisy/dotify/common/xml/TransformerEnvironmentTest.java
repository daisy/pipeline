package org.daisy.dotify.common.xml;

import org.junit.Test;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

/**
 * TODO: write java doc.
 */
public class TransformerEnvironmentTest {

    @Test
    public void testThrowable_01() {
        TransformerEnvironment<XMLToolsException> t = TransformerEnvironment.builder().build();
        XMLToolsException ex1 = new XMLToolsException("test");
        XMLToolsException ex2 = t.toThrowable(ex1);
        assertSame(ex1, ex2);
    }

    @Test
    public void testThrowable_02() {
        TransformerEnvironment<XMLToolsException> t = TransformerEnvironment.builder().build();
        Exception ex1 = new Exception();
        XMLToolsException ex2 = t.toThrowable(ex1);
        assertNotSame(ex1, ex2);
    }

    @Test
    public void testThrowable_03() {
        TransformerEnvironment<XMLToolsException> t = TransformerEnvironment.builder().build();
        XMLToolsExceptionExt ex1 = new XMLToolsExceptionExt();
        XMLToolsException ex2 = t.toThrowable(ex1);
        assertSame(ex1, ex2);
    }

    static class XMLToolsExceptionExt extends XMLToolsException {

        /**
         *
         */
        private static final long serialVersionUID = 3633188503742794479L;

    }
}
