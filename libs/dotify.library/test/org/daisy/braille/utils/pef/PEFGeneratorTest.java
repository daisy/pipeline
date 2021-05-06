package org.daisy.braille.utils.pef;

import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class PEFGeneratorTest {
    private static final Logger logger = Logger.getLogger(PEFGeneratorTest.class.getCanonicalName());

    @Test
    public void testSectionDivisor_01() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put(PEFGenerator.KEY_VOLUMES, "3");
        defaults.put(PEFGenerator.KEY_SPV, "5");
        defaults.put(PEFGenerator.KEY_PPV, "21");
        defaults.put(PEFGenerator.KEY_EIGHT_DOT, "false");
        defaults.put(PEFGenerator.KEY_ROWS, "29");
        defaults.put(PEFGenerator.KEY_COLS, "32");
        defaults.put(PEFGenerator.KEY_DUPLEX, "true");
        PEFGenerator pg = new PEFGenerator(defaults);

        for (int i = 0; i < 30; i++) {
            List<Integer> sections = pg.getSectionDivisors();
            for (Integer x : sections) {
                logger.info("" + x);
            }
            assertTrue("Expected >= 1: " + sections.get(0), sections.get(0) >= 1);
            assertTrue("Expected <= 5: " + sections.get(0), sections.get(0) <= 5);
            assertTrue("Expected > 5: " + sections.get(1), sections.get(1) > 5);
            assertTrue("Expected <= 10: " + sections.get(1), sections.get(1) <= 10);
            assertTrue("Expected > 10: " + sections.get(2), sections.get(2) > 10);
            assertTrue("Expected <= 15: " + sections.get(2), sections.get(2) <= 15);
            assertTrue("Expected > 15: " + sections.get(3), sections.get(3) > 15);
            assertTrue("Expected <= 20: " + sections.get(3), sections.get(3) <= 20);

        }
    }

    @Test
    public void testSectionDivisor_02() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put(PEFGenerator.KEY_VOLUMES, "3");
        defaults.put(PEFGenerator.KEY_SPV, "1");
        defaults.put(PEFGenerator.KEY_PPV, "20");
        defaults.put(PEFGenerator.KEY_EIGHT_DOT, "false");
        defaults.put(PEFGenerator.KEY_ROWS, "29");
        defaults.put(PEFGenerator.KEY_COLS, "32");
        defaults.put(PEFGenerator.KEY_DUPLEX, "true");
        PEFGenerator pg = new PEFGenerator(defaults);
        List<Integer> sections = pg.getSectionDivisors();
        assertEquals(0, sections.size());
    }

    @Test
    public void testSectionDivisor_03() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put(PEFGenerator.KEY_VOLUMES, "3");
        defaults.put(PEFGenerator.KEY_SPV, "2");
        defaults.put(PEFGenerator.KEY_PPV, "20");
        defaults.put(PEFGenerator.KEY_EIGHT_DOT, "false");
        defaults.put(PEFGenerator.KEY_ROWS, "29");
        defaults.put(PEFGenerator.KEY_COLS, "32");
        defaults.put(PEFGenerator.KEY_DUPLEX, "true");
        PEFGenerator pg = new PEFGenerator(defaults);
        List<Integer> sections = pg.getSectionDivisors();
        assertEquals(1, sections.size());
    }

    @Test
    public void testSectionDivisor_04() {
        Map<String, String> defaults = new HashMap<>();
        defaults.put(PEFGenerator.KEY_VOLUMES, "3");
        defaults.put(PEFGenerator.KEY_SPV, "4");
        defaults.put(PEFGenerator.KEY_PPV, "4");
        defaults.put(PEFGenerator.KEY_EIGHT_DOT, "false");
        defaults.put(PEFGenerator.KEY_ROWS, "29");
        defaults.put(PEFGenerator.KEY_COLS, "32");
        defaults.put(PEFGenerator.KEY_DUPLEX, "true");
        PEFGenerator pg = new PEFGenerator(defaults);
        List<Integer> sections = pg.getSectionDivisors();
        assertEquals(3, sections.size());
        assertEquals(1, (int) sections.get(0));
        assertEquals(2, (int) sections.get(1));
        assertEquals(3, (int) sections.get(2));
    }

}
