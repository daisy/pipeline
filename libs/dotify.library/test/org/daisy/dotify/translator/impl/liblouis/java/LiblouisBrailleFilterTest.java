package org.daisy.dotify.translator.impl.liblouis.java;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class LiblouisBrailleFilterTest {
    private final LiblouisBrailleFilter filter;

    public LiblouisBrailleFilterTest() {
        //LiblouisFileReader lfr = new LiblouisFileReader();
        //lfr.parse("Se-Se-g1.utb");
        //LiblouisBrailleFilter filter = lfr.getFilter();

        filter = new LiblouisBrailleFilter.Builder().
                put((int) 'a', "⠁", CharClass.LOWERCASE).
                put((int) 'b', "⠃", CharClass.LOWERCASE).
                put((int) 'c', "⠉", CharClass.LOWERCASE).
                put((int) 'A', "⠁", CharClass.UPPERCASE).
                put((int) 'B', "⠃", CharClass.UPPERCASE).
                put((int) 'C', "⠉", CharClass.UPPERCASE).
                put((int) '1', "⠁", CharClass.DIGIT).
                put((int) '2', "⠃", CharClass.DIGIT).
                put((int) '3', "⠉", CharClass.DIGIT).
                numsign("\u283c").
                capsign("\u2820").
                build();
    }

    //these are very basic tests, and does not prove much

    //TODO: collect actual test from Liblouis?
    @Test
    public void testFilter_Caps_01() throws IOException {
        assertEquals("⠁⠃⠉", filter.filter("abc"));
    }

    @Test
    public void testFilter_Caps_02() throws IOException {
        assertEquals("\u2820⠁⠃⠉", filter.filter("Abc"));
    }

    @Test
    public void testFilter_Caps_03() throws IOException {
        assertEquals("⠁⠃⠉", filter.filter("ABc"));
    }

    @Test
    public void testFilter_Caps_04() throws IOException {
        assertEquals("⠁\u2820⠃⠉", filter.filter("aBc"));
    }

    @Test
    public void testFilter_Digits_01() throws IOException {
        assertEquals("\u283c⠁⠃⠉", filter.filter("123"));
    }


}
