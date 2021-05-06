package org.daisy.dotify.api.translator;

import org.daisy.dotify.api.translator.TranslatorMode.DotsPerCell;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class TranslatorModeTest {

    @Test
    public void test_01() {
        TranslatorMode m = TranslatorMode.withGrade(1);

        assertEquals("grade:1", m.getIdentifier());
        assertEquals(Optional.empty(), m.getType());
        assertEquals(1, m.getContractionGrade().get().doubleValue(), 0);
        assertEquals(Optional.empty(), m.getDotsPerCell());
    }

    @Test
    public void test_02() {
        TranslatorMode m = TranslatorMode.withGrade(1.5);

        assertEquals("grade:1.5", m.getIdentifier());
        assertEquals(Optional.empty(), m.getType());
        assertEquals(1.5, m.getContractionGrade().get().doubleValue(), 0);
        assertEquals(Optional.empty(), m.getDotsPerCell());
    }

    @Test
    public void test_03() {
        String input = "uncontracted";
        TranslatorMode m = TranslatorMode.parse(input);

        assertEquals(input, m.getIdentifier());
        assertEquals(TranslatorType.UNCONTRACTED, m.getType().get());
        assertEquals(Optional.empty(), m.getContractionGrade());
        assertEquals(Optional.empty(), m.getDotsPerCell());
    }

    @Test
    public void test_04() {
        String input = "grade:1";
        TranslatorMode m = TranslatorMode.parse(input);

        assertEquals(input, m.getIdentifier());
        assertEquals(Optional.empty(), m.getType());
        assertEquals(1, m.getContractionGrade().get().doubleValue(), 0);
        assertEquals(Optional.empty(), m.getDotsPerCell());
    }

    @Test
    public void test_05() {
        String input = "8-dot/uncontracted";
        TranslatorMode m = TranslatorMode.parse(input);

        assertEquals(input, m.getIdentifier());
        assertEquals(TranslatorType.UNCONTRACTED, m.getType().get());
        assertEquals(Optional.empty(), m.getContractionGrade());
        assertEquals(DotsPerCell.EIGHT, m.getDotsPerCell().get());
    }

    @Test
    public void test_06() {
        String input = "8-dot/uncontracted/grade:0";
        TranslatorMode m = TranslatorMode.parse(input);

        assertEquals(input, m.getIdentifier());
        assertEquals(TranslatorType.UNCONTRACTED, m.getType().get());
        assertEquals(0, m.getContractionGrade().get(), 0);
        assertEquals(DotsPerCell.EIGHT, m.getDotsPerCell().get());
    }

    @Test
    public void test_07() {
        String input = "8-dot/uncontracted/grade:0";
        String id = "my-identifier";
        TranslatorMode m = TranslatorMode.Builder.parse(input).identifier(id).build();

        assertEquals(id, m.getIdentifier());
        assertEquals(TranslatorType.UNCONTRACTED, m.getType().get());
        assertEquals(0, m.getContractionGrade().get(), 0);
        assertEquals(DotsPerCell.EIGHT, m.getDotsPerCell().get());
    }

    @Test
    public void test_08() {
        String input = "pre-translated";
        TranslatorMode m = TranslatorMode.parse(input);

        assertEquals(input, m.getIdentifier());
        assertEquals(TranslatorType.PRE_TRANSLATED, m.getType().get());
        assertEquals(Optional.empty(), m.getContractionGrade());
        assertEquals(Optional.empty(), m.getDotsPerCell());
    }

}
