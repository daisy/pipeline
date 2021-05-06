package org.daisy.dotify.translator.impl.liblouis;


import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class LiblouisBrailleFilterTest {
    /*
    @Test
    public void testToTypeForm_01() {
        TextAttribute ta = new DefaultTextAttribute.Builder()
                .add(5)
                .add(
                    new DefaultTextAttribute.Builder("italic")
                    .add(2)
                    .add(new DefaultTextAttribute.Builder("bold").build(2))
                    .build(4))
                .add(new DefaultTextAttribute.Builder("bold").build(5))
                .build(14);
        Map<String, Integer> dict = new HashMap<>();
        dict.put("italic", 1);
        dict.put("underline", 2);
        dict.put("bold", 4);
        short[] expecteds = new short[] {0,0,0,0,0,1,1,5,5,4,4,4,4,4};
        short[] actuals = LiblouisBrailleFilter.toTypeForm(ta, dict);
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testToTypeForm_02() {
        TextAttribute ta = new DefaultTextAttribute.Builder()
                .add(5)
                .add(new DefaultTextAttribute.Builder("em").add(4).build(4))
                .add(5)
                .build(14);
        Map<String, Integer> dict = new HashMap<>();
        dict.put("em", 1);
        short[] expecteds = new short[] {0,0,0,0,0,1,1,1,1,0,0,0,0,0};
        short[] actuals = LiblouisBrailleFilter.toTypeForm(ta, dict);
        assertArrayEquals(expecteds, actuals);
    }

    @Test
    public void testToTypeForm_03() {
        TextAttribute ta = new DefaultTextAttribute.Builder("strong")
                .add(5)
                .add(new DefaultTextAttribute.Builder("em").add(4).build(4))
                .add(5)
                .build(14);
        Map<String, Integer> dict = new HashMap<>();
        dict.put("em", 1);
        dict.put("strong", 2);
        short[] expecteds = new short[] {2,2,2,2,2,3,3,3,3,2,2,2,2,2};
        short[] actuals = LiblouisBrailleFilter.toTypeForm(ta, dict);
        assertArrayEquals(expecteds, actuals);
    }*/

    @Test
    public void testToLiblouisSpecification_01() {
        String input = "hyphenate";
        String hyph = "hy\u00adphen\u00adate";
        LiblouisTranslatable hp = LiblouisBrailleFilter.toLiblouisSpecification(hyph, input);
        assertEquals(input, hp.getText());
        assertArrayEquals(new int[]{0, 1, 0, 0, 0, 1, 0, 0}, hp.getInterCharAtts());
    }

    @Test
    public void testToLiblouisSpecification_02() {
        String input = "- - -"; //002d, 0020, 002d, 0020, 002d
        String hyph = "-\u200b -\u200b -"; //002d, 200b, 0020, 002d, 200b, 0020, 002d
        LiblouisTranslatable hp = LiblouisBrailleFilter.toLiblouisSpecification(hyph, input);
        assertEquals(input, hp.getText());
        assertArrayEquals(new int[]{2, 0, 2, 0}, hp.getInterCharAtts());
    }

    @Test
    public void testToBrailleFilterString() {
        String input = "hyphenate";
        String hyph = "hy\u00adphen\u00adate";
        String res = LiblouisBrailleFilter.toBrailleFilterString(input, input,
            new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, new int[]{0, 1, 0, 0, 0, 1, 0, 0}
        );
        assertEquals(hyph, res);
    }


    @Test
    public void testToBrailleFilterStringShouldNotCrash() {
        LiblouisBrailleFilter.toLiblouisSpecification("", "");
    }
}
