package org.daisy.dotify.translator.impl.sv;

import org.daisy.dotify.api.hyphenator.HyphenatorFactoryMaker;
import org.daisy.dotify.api.translator.AttributeWithContext;
import org.daisy.dotify.api.translator.BrailleFilter;
import org.daisy.dotify.api.translator.DefaultAttributeWithContext;
import org.daisy.dotify.api.translator.DefaultTextAttribute;
import org.daisy.dotify.api.translator.TextAttribute;
import org.daisy.dotify.api.translator.TextWithContext;
import org.daisy.dotify.api.translator.Translatable;
import org.daisy.dotify.api.translator.TranslatableWithContext;
import org.daisy.dotify.api.translator.TranslationException;
import org.daisy.dotify.api.translator.TranslatorConfigurationException;
import org.daisy.dotify.api.translator.TranslatorType;
import org.daisy.dotify.translator.impl.StaticResolvable;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
public class SwedishBrailleFilterFactoryTest {

    @Test
    public void test_01a() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        AttributeWithContext attr = new DefaultAttributeWithContext.Builder()
                .add(1)
                .add(new DefaultAttributeWithContext.Builder("strong").build(1))
                .add(1)
                .build(3);

        TextWithContext twc = Mockito.mock(TextWithContext.class);
        Mockito.when(twc.getTextToTranslate()).thenReturn(StaticResolvable.with("ab ", "c", " de"));
        TranslatableWithContext tr = TranslatableWithContext.text(twc)
                .attributes(attr)
                .build();
        assertEquals("⠁⠃ ⠨⠉ ⠙⠑", f.filter(tr));
    }

    @Test
    public void test_01b() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        TextAttribute attr = new DefaultTextAttribute.Builder()
                .add(3)
                .add(new DefaultTextAttribute.Builder("strong").build(1))
                .add(3)
                .build(7);

        Translatable tr = Translatable.text("ab c de")
                .attributes(attr)
                .build();
        assertEquals("⠁⠃ ⠨⠉ ⠙⠑", f.filter(tr));
    }

    @Test
    public void test_02a() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        AttributeWithContext attr = new DefaultAttributeWithContext.Builder("dd")
                .build(3);

        TextWithContext twc = Mockito.mock(TextWithContext.class);
        Mockito.when(twc.getTextToTranslate()).thenReturn(StaticResolvable.with("ab ", "c", " de"));
        TranslatableWithContext tr = TranslatableWithContext.text(twc)
                .attributes(attr)
                .build();
        assertEquals("⠠⠄⠀⠁⠃ ⠉ ⠙⠑", f.filter(tr));
    }

    @Test
    public void test_02b() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        TextAttribute attr = new DefaultTextAttribute.Builder("dd")
                .build(7);

        Translatable tr = Translatable.text("ab c de")
                .attributes(attr)
                .build();
        assertEquals("⠠⠄⠀⠁⠃ ⠉ ⠙⠑", f.filter(tr));
    }

    @Test
    public void test_03a() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        AttributeWithContext attr = new DefaultAttributeWithContext.Builder("table-cell-continued")
                .build(3);

        TextWithContext twc = Mockito.mock(TextWithContext.class);
        Mockito.when(twc.getTextToTranslate()).thenReturn(StaticResolvable.with("ab ", "c", " de"));
        TranslatableWithContext tr = TranslatableWithContext.text(twc)
                .attributes(attr)
                .build();
        assertEquals("⠻⠻⠁⠃ ⠉ ⠙⠑", f.filter(tr));
    }

    @Test
    public void test_03b() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        TextAttribute attr = new DefaultTextAttribute.Builder("table-cell-continued")
                .build(7);

        Translatable tr = Translatable.text("ab c de")
                .attributes(attr)
                .build();
        assertEquals("⠻⠻⠁⠃ ⠉ ⠙⠑", f.filter(tr));
    }

    @Test
    public void test_04a() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        AttributeWithContext attr = new DefaultAttributeWithContext.Builder()
                .add(1)
                .add(new DefaultAttributeWithContext.Builder("strong").build(1))
                .add(1)
                .build(3);

        TextWithContext twc = Mockito.mock(TextWithContext.class);
        Mockito.when(twc.getTextToTranslate()).thenReturn(StaticResolvable.with("ab ", "c d", " e"));
        TranslatableWithContext tr = TranslatableWithContext.text(twc)
                .attributes(attr)
                .build();
        assertEquals("⠁⠃ ⠨⠨⠉ ⠙⠱ ⠑", f.filter(tr));
    }

    @Test
    public void test_04b() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        TextAttribute attr = new DefaultTextAttribute.Builder()
                .add(3)
                .add(new DefaultTextAttribute.Builder("strong").build(3))
                .add(2)
                .build(8);

        Translatable tr = Translatable.text("ab c d e")
                .attributes(attr)
                .build();
        assertEquals("⠁⠃ ⠨⠨⠉ ⠙⠱ ⠑", f.filter(tr));
    }

    @Test
    public void test_05a() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        AttributeWithContext attr = new DefaultAttributeWithContext.Builder()
                .add(1)
                .add(new DefaultAttributeWithContext.Builder("strong").build(2))
                .build(3);

        TextWithContext twc = Mockito.mock(TextWithContext.class);
        Mockito.when(twc.getTextToTranslate()).thenReturn(StaticResolvable.with("ab ", "c", " de"));
        TranslatableWithContext tr = TranslatableWithContext.text(twc)
                .attributes(attr)
                .build();
        assertEquals("⠁⠃ ⠨⠨⠉ ⠙⠑⠱", f.filter(tr));
    }

    @Test
    public void test_05b() throws TranslationException, TranslatorConfigurationException {
        BrailleFilter f = new SwedishBrailleFilterFactory(
            HyphenatorFactoryMaker.newInstance()
        ).newFilter("sv-SE", TranslatorType.UNCONTRACTED.toString());
        TextAttribute attr = new DefaultTextAttribute.Builder()
                .add(3)
                .add(new DefaultTextAttribute.Builder("strong").build(4))
                .build(7);

        Translatable tr = Translatable.text("ab c de")
                .attributes(attr)
                .build();
        assertEquals("⠁⠃ ⠨⠨⠉ ⠙⠑⠱", f.filter(tr));
    }

}
