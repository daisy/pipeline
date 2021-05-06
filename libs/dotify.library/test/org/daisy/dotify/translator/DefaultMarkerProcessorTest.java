package org.daisy.dotify.translator;

import org.daisy.dotify.api.translator.DefaultAttributeWithContext;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextAttribute;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
public class DefaultMarkerProcessorTest {
    private final DefaultMarkerProcessor sap;

    public DefaultMarkerProcessorTest() {
        RegexMarkerDictionary boldDefinition = new RegexMarkerDictionary.Builder().
                addPattern("\\s+", new Marker("{mb:", ":mb}"), new Marker("{sb:", ":sb}")).
                build();

        RegexMarkerDictionary italicDefinition = new RegexMarkerDictionary.Builder().
                addPattern("\\s+", new Marker("{mi:", ":mi}"), new Marker("{si:", ":si}")).
                build();

        RegexMarkerDictionary supDefinition = new RegexMarkerDictionary.Builder().
                addPattern("\\A[a-zA-Z0-9]+\\z", new Marker("{sup:", ":sup}")).
                build();

        RegexMarkerDictionary subDefinition = new RegexMarkerDictionary.Builder().
                addPattern("\\A[a-zA-Z0-9]+\\z", new Marker("{sub:", ":sub}")).
                build();

        sap = new DefaultMarkerProcessor.Builder().
                addDictionary("b", boldDefinition).
                addDictionary("i", italicDefinition).
                addDictionary("sup", supDefinition).
                addDictionary("sub", subDefinition).
                build();
    }

    @Test
    public void test_01() {
        DefaultTextAttribute.Builder t = new DefaultTextAttribute.Builder();

        DefaultTextAttribute.Builder s1 = new DefaultTextAttribute.Builder("b");
        s1.add(new DefaultTextAttribute.Builder("sup").build(1));
        s1.add(new DefaultTextAttribute.Builder().build(2));
        t.add(s1.build(3));

        t.add(new DefaultTextAttribute.Builder().build(2));
        t.add(new DefaultTextAttribute.Builder("i").build(5));

        String actual = sap.processAttributes(t.build(10), "1234567890");
        assertEquals("Tests a single string of digits", "{sb:{sup:1:sup}23:sb}45{si:67890:si}", actual);
    }

    @Test
    public void test_02() {
        DefaultTextAttribute.Builder t = new DefaultTextAttribute.Builder("i");

        DefaultTextAttribute.Builder s1 = new DefaultTextAttribute.Builder("b");
        s1.add(new DefaultTextAttribute.Builder("sup").build(1));
        s1.add(new DefaultTextAttribute.Builder().build(2));
        t.add(s1.build(3));

        t.add(new DefaultTextAttribute.Builder().build(2));
        t.add(new DefaultTextAttribute.Builder("i").build(5));

        String[] ret = sap.processAttributesRetain(t.build(10), new String[]{"123", "45", "678", "90"});
        assertEquals(Arrays.toString(ret), 4, ret.length);
        assertEquals("", "{si:{sb:{sup:1:sup}23:sb}", ret[0]);
        assertEquals("", "45", ret[1]);
        assertEquals("", "{si:678", ret[2]);
        assertEquals("", "90:si}:si}", ret[3]);
        String actual = join(ret);
        assertEquals("Tests a single string of digits", "{si:{sb:{sup:1:sup}23:sb}45{si:67890:si}:si}", actual);
    }

    @Test
    public void test_03() {
        String input = "Test of a multi-word attribution.";
        DefaultTextAttribute.Builder t = new DefaultTextAttribute.Builder();
        t.add(new DefaultTextAttribute.Builder().build(5));
        t.add(new DefaultTextAttribute.Builder("b").build(4));
        t.add(new DefaultTextAttribute.Builder().build(1));
        t.add(new DefaultTextAttribute.Builder("i").build(22));
        t.add(new DefaultTextAttribute.Builder().build(1));
        String[] actual = sap.processAttributesRetain(t.build(input.length()), new String[]{input});
        assertEquals(Arrays.toString(actual), 1, actual.length);
        assertEquals("", "Test {mb:of a:mb} {mi:multi-word attribution:mi}.", actual[0]);
    }

    @Test
    @Ignore
    public void testProcessAttributesRetain_04() {
        DefaultTextAttribute t = new DefaultTextAttribute.Builder()
                .add(new DefaultTextAttribute.Builder("i")
                        .add(new DefaultTextAttribute.Builder().build(1))
                        .add(new DefaultTextAttribute.Builder("b")
                                .add(new DefaultTextAttribute.Builder().build(0))
                                .build(0))
                        .add(new DefaultTextAttribute.Builder().build(1))
                        .build(2)
                )
                .build(2);
        DefaultMarkerProcessor mp = new DefaultMarkerProcessor.Builder()
                .addDictionary("i", (String str, TextAttribute attributes) -> new Marker("1>", "<1"))
                .addDictionary("b", (String str, TextAttribute attributes) -> new Marker("2>", "<2"))
                .build();
        String[] actual = mp.processAttributesRetain(t, new String[]{"a", "", "c"});

        assertArrayEquals(new String[]{">1a", "2><2", "c<1"}, actual);
    }

    public void testProcessAttributesRetainContext() {
        DefaultAttributeWithContext t = new DefaultAttributeWithContext.Builder()
                .add(new DefaultAttributeWithContext.Builder("i")
                        .add(new DefaultAttributeWithContext.Builder().build(1))
                        .add(new DefaultAttributeWithContext.Builder("b")
                                .add(new DefaultAttributeWithContext.Builder().build(1))
                                .build(1))
                        .add(new DefaultAttributeWithContext.Builder().build(1))
                        .build(3)
                )
                .build(3);
        DefaultMarkerProcessor mp = new DefaultMarkerProcessor.Builder()
                .addDictionary("i", (String str, TextAttribute attributes) -> new Marker("1>", "<1"))
                .addDictionary("b", (String str, TextAttribute attributes) -> new Marker("2>", "<2"))
                .build();
        String[] actual = mp.processAttributesRetain(t, Arrays.asList("a", "", "c"));

        assertArrayEquals(new String[]{">1a", "2><2", "c<1"}, actual);
    }


    @Test
    public void test_04() {
        SubstringReturn ret = sap_substrings(new String[]{"12345", "678", "9ABC"}, 5, 7);
        assertEquals(Arrays.toString(ret.getStrings()), 1, ret.getStrings().length);
        assertEquals("", "67", ret.getStrings()[0]);

    }

    @Test
    public void test_05() {
        SubstringReturn ret = sap_substrings(new String[]{"12345", "678", "9ABC"}, 0, 7);
        assertEquals(Arrays.toString(ret.getStrings()), 2, ret.getStrings().length);
        assertEquals("", 0, ret.getArrayStart());
        assertEquals("", "12345", ret.getStrings()[0]);
        assertEquals("", "67", ret.getStrings()[1]);
    }

    @Test
    public void test_06() {
        SubstringReturn ret = sap_substrings(new String[]{"12345", "678", "9ABC"}, 3, 9);
        assertEquals("", 3, ret.getStrings().length);
        assertEquals("", 0, ret.getArrayStart());
        assertEquals("", "45", ret.getStrings()[0]);
        assertEquals("", "678", ret.getStrings()[1]);
        assertEquals("", "9", ret.getStrings()[2]);
    }

    @Test
    public void test_07() {
        SubstringReturn ret = sap_substrings(new String[]{"12345"}, 3, 4);
        assertEquals("", 1, ret.getStrings().length);
        assertEquals("", 0, ret.getArrayStart());
        assertEquals("", "4", ret.getStrings()[0]);
    }

    @Test
    public void test_08() {
        SubstringReturn ret = sap_substrings(new String[]{"12345", "678", "9ABC"}, 8, 9);
        assertEquals("", 1, ret.getStrings().length);
        assertEquals("", 2, ret.getArrayStart());
        assertEquals("", "9", ret.getStrings()[0]);
    }

    @Test
    public void test_empty_input_01() {
        DefaultTextAttribute.Builder t = new DefaultTextAttribute.Builder();
        t.add(new DefaultTextAttribute.Builder("b").build(0));
        sap.processAttributes(t.build(0), "");
        String[] ret = sap.processAttributesRetain(t.build(0), new String[]{});
        assertEquals(1, ret.length);
        assertEquals("{sb::sb}", ret[0]);
    }

    @Test
    public void test_empty_input_02() {
        DefaultTextAttribute.Builder t = new DefaultTextAttribute.Builder();
        t.add(new DefaultTextAttribute.Builder("b").build(0));
        String[] ret = sap.processAttributesRetain(t.build(0), new String[]{});
        assertEquals(1, ret.length);
        assertEquals("{sb::sb}", ret[0]);
    }

    @Test
    public void testToTextAttribute_01() {
        DefaultAttributeWithContext context = new DefaultAttributeWithContext.Builder("em")
                .add(2)
                .add(new DefaultAttributeWithContext.Builder("strong")
                        .build(1))
                .add(1)
                .build(4);
        TextAttribute ta = DefaultMarkerProcessor.toTextAttribute(context, Arrays.asList("ab", "cde", "fa", "ghi"));
        assertEquals(10, ta.getWidth());
        assertEquals("em", ta.getDictionaryIdentifier());
        assertTrue(ta.hasChildren());
        Iterator<TextAttribute> i = ta.iterator();
        {
            TextAttribute a = i.next();
            assertEquals(5, a.getWidth());
            assertEquals(null, a.getDictionaryIdentifier());
            assertFalse(a.hasChildren());
        }
        {
            TextAttribute a = i.next();
            assertEquals(2, a.getWidth());
            assertEquals("strong", a.getDictionaryIdentifier());
            assertFalse(a.hasChildren());
        }
        {
            TextAttribute a = i.next();
            assertEquals(3, a.getWidth());
            assertEquals(null, a.getDictionaryIdentifier());
            assertFalse(a.hasChildren());
        }
        assertFalse(i.hasNext());
    }

    @Test
    public void testToTextAttribute_02() {
        DefaultAttributeWithContext context = new DefaultAttributeWithContext.Builder()
                .add(new DefaultAttributeWithContext.Builder("strong")
                        .build(4))
                .build(4);
        TextAttribute ta = DefaultMarkerProcessor.toTextAttribute(context, Arrays.asList("ab", "cde", "fa", "ghi"));
        assertEquals(10, ta.getWidth());
        assertEquals(null, ta.getDictionaryIdentifier());
        assertTrue(ta.hasChildren());
        Iterator<TextAttribute> i = ta.iterator();
        {
            TextAttribute a = i.next();
            assertEquals(10, a.getWidth());
            assertEquals("strong", a.getDictionaryIdentifier());
            assertFalse(a.hasChildren());
        }
        assertFalse(i.hasNext());
    }

    private String join(String[] strs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strs.length; i++) {
            sb.append(strs[i]);
        }
        return sb.toString();
    }

    /**
     * Testing private method using reflection.
     *
     * @param inp
     * @param start
     * @param end
     * @return
     */
    private SubstringReturn sap_substrings(String[] inp, int start, int end) {
        try {
            Method method = sap.getClass().getDeclaredMethod("substrings", inp.getClass(), int.class, int.class);
            method.setAccessible(true);
            Object ret = method.invoke(sap, inp, start, end);
            String[] strs = (String[]) ret.getClass().getDeclaredMethod("getStrings").invoke(ret);
            int arrayStart = (Integer) ret.getClass().getDeclaredMethod("getArrayStart").invoke(ret);
            return new SubstringReturn(strs, arrayStart);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class SubstringReturn {
        private final String[] strings;
        private final int arrayStart;

        public SubstringReturn(String[] strings, int arrayStart) {
            super();
            this.strings = strings;
            this.arrayStart = arrayStart;
        }

        public String[] getStrings() {
            return strings;
        }

        public int getArrayStart() {
            return arrayStart;
        }

    }

}
