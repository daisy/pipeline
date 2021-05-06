package org.daisy.dotify.translator.impl.sv;

import org.daisy.dotify.translator.impl.DefaultBrailleFinalizer;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
public class SwedishBrailleFilterTest {
    private final SwedishBrailleFilter filter;

    public SwedishBrailleFilterTest() {
        filter = new SwedishBrailleFilter("sv-SE");
    }

    // 1.2 - Numbers
    @Test
    public void testSwedishFilter_Numbers() {
        assertEquals("⠼⠃⠚⠚⠊", filter.filter("2009"));
    }

    // 2.1 - Punctuation
    @Test
    public void testSwedishFilter_Punctuation_ex1() {
        assertEquals("⠠⠓⠕⠝ ⠅⠪⠏⠞⠑ ⠎⠍⠪⠗⠂ ⠞⠑ ⠕⠉⠓ ⠕⠎⠞⠄", filter.filter("Hon köpte smör, te och ost."));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex2() {
        assertEquals("⠠⠅⠕⠍⠍⠑⠗ ⠙⠥⠢", filter.filter("Kommer du?"));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex3() {
        assertEquals("⠠⠓⠪⠗ ⠥⠏⠏⠖", filter.filter("Hör upp!"));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex4() {
        assertEquals("⠠⠓⠕⠝ ⠎⠁⠒ ⠠⠠⠙⠝⠒⠎ ⠗⠑⠙⠁⠅⠞⠊⠕⠝ ⠜⠗ ⠎⠞⠕⠗⠄", filter.filter("Hon sa: DN:s redaktion är stor."));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex5() {
        assertEquals(
            "⠠⠎⠅⠊⠇⠇⠝⠁⠙⠑⠝ ⠍⠑⠇⠇⠁⠝ ⠁⠗⠃⠑⠞⠎- ⠕⠉⠓ ⠧⠊⠇⠕⠙⠁⠛⠁⠗ ⠃⠇⠑⠧ ⠍⠊⠝⠙⠗⠑ ⠎⠅⠁⠗⠏⠆ ⠓⠕⠝ ⠅⠥⠝⠙⠑ ⠞⠊⠇⠇⠡⠞⠁ " +
                    "⠎⠊⠛ ⠧⠊⠇⠕⠙⠁⠛⠁⠗ ⠍⠊⠞⠞ ⠊ ⠧⠑⠉⠅⠁⠝⠄",
            filter.filter(
                "Skillnaden mellan arbets- och vilodagar blev mindre skarp; hon kunde tillåta " +
                    "sig vilodagar mitt i veckan."
            ));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex6() {
        assertEquals("⠠⠍⠌⠠⠎ ⠠⠅⠗⠕⠝⠁⠝", filter.filter("M/S Kronan"));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex7() {
        assertEquals("⠼⠚⠂⠑⠑ ⠇⠊⠞⠑⠗⠌⠍⠊⠇", filter.filter("0,55 liter/mil"));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex8() {
        assertEquals("⠍⠡⠝⠁⠙⠎⠎⠅⠊⠋⠞⠑⠞ ⠁⠏⠗⠊⠇⠌⠍⠁⠚", filter.filter("månadsskiftet april/maj"));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex9() {
        assertEquals("⠰⠠⠧⠊⠇⠇ ⠙⠥ ⠇⠑⠅⠁⠢⠰", filter.filter("\"Vill du leka?\""));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex10() {
        assertEquals("⠠⠙⠑⠞ ⠧⠁⠗ ⠠⠊⠗⠊⠎⠐ ⠃⠇⠕⠍⠍⠕⠗⠄", filter.filter("Det var Iris' blommor."));
    }

    @Test
    public void testSwedishFilter_Punctuation_ex11() {
        assertEquals(
            "⠰⠠⠧⠁⠙ ⠃⠑⠞⠽⠙⠑⠗ ⠐⠁⠃⠎⠞⠗⠥⠎⠐⠢⠰ ⠋⠗⠡⠛⠁⠙⠑ ⠓⠁⠝⠄",
            filter.filter("\"Vad betyder 'abstrus'?\" frågade han.")
        );
    }

    // 2.2 - Dashes
    @Test
    public void testSwedishFilter_Dashes_ex1() {
        assertEquals(
            "⠠⠁⠝⠝⠑-⠠⠍⠁⠗⠊⠑ ⠓⠁⠗ ⠛⠥⠇- ⠕⠉⠓ ⠧⠊⠞⠗⠁⠝⠙⠊⠛ ⠅⠚⠕⠇⠄",
            filter.filter("Anne-Marie har gul- och vitrandig kjol.")
        );
    }

    @Test
    public void testSwedishFilter_Dashes_ex2() {
        assertEquals(
            "⠠⠑⠞⠞ ⠋⠑⠃⠗⠊⠇⠞ ⠎⠽⠎⠎⠇⠁⠝⠙⠑ ⠍⠑⠙ ⠤⠤ ⠊⠝⠛⠑⠝⠞⠊⠝⠛ ⠁⠇⠇⠎⠄",
            filter.filter("Ett febrilt sysslande med \u2013 ingenting alls.")
        );
    }

    @Test
    public void testSwedishFilter_Dashes_ex3() {
        assertEquals("⠤⠤ ⠠⠧⠁⠙ ⠓⠑⠞⠑⠗ ⠓⠥⠝⠙⠑⠝⠢", filter.filter("\u2013 Vad heter hunden?"));
    }

    @Test
    public void testSwedishFilter_Dashes_ex4() {
        assertEquals(
            "⠠⠓⠁⠝ ⠞⠕⠛ ⠞⠡⠛⠑⠞ ⠠⠎⠞⠕⠉⠅⠓⠕⠇⠍⠤⠤⠠⠛⠪⠞⠑⠃⠕⠗⠛⠄",
            filter.filter("Han tog tåget Stockholm\u2013Göteborg.")
        );
    }

    // 2.3.1 - Parentheses
    @Test
    public void testSwedishFilter_Parentheses_ex1() {
        assertEquals(
            "⠠⠎⠽⠝⠎⠅⠁⠙⠁⠙⠑⠎ ⠠⠗⠊⠅⠎⠋⠪⠗⠃⠥⠝⠙ ⠦⠠⠠⠎⠗⠋⠴",
            filter.filter("Synskadades Riksförbund (SRF)")
        );
    }

    @Test
    public void testSwedishFilter_Parentheses_ex2() {
        assertEquals(
            "⠠⠗⠁⠏⠏⠕⠗⠞⠑⠗ ⠁⠴ ⠋⠗⠡⠝ ⠋⠪⠗⠃⠥⠝⠙⠎⠍⠪⠞⠑⠞ ⠃⠴ ⠅⠁⠎⠎⠁⠜⠗⠑⠝⠙⠑⠝",
            filter.filter("Rapporter a) från förbundsmötet b) kassaärenden")
        );
    }

    // 2.3.2 - Brackets
    @Test
    public void testSwedishFilter_Brackets_ex1() {
        assertEquals(
            "⠠⠅⠗⠁⠧⠑⠞ ⠓⠁⠗ ⠎⠞⠜⠇⠇⠞⠎ ⠋⠗⠡⠝ ⠕⠇⠊⠅⠁ ⠛⠗⠥⠏⠏⠑⠗ ⠦⠃⠇⠄⠁⠄ ⠷⠓⠪⠛⠎⠅⠕⠇⠑⠾⠎⠞⠥⠙⠑⠗⠁⠝⠙⠑ ⠕⠉⠓ " +
            "⠙⠑⠇⠞⠊⠙⠎⠁⠗⠃⠑⠞⠁⠝⠙⠑⠴ ⠍⠑⠝ ⠙⠑⠞ ⠓⠁⠗ ⠁⠇⠇⠞⠊⠙ ⠁⠧⠧⠊⠎⠁⠞⠎⠄",
            filter.filter(
                "Kravet har ställts från olika grupper (bl.a. [högskole]studerande och " +
                "deltidsarbetande) men det har alltid avvisats."
            )
        );
    }

    @Test
    public void testSwedishFilter_Brackets_ex2() {
        assertEquals("⠠⠗⠑⠙ ⠠⠏⠕⠗⠞ ⠷⠗⠜⠙ ⠏⠡⠗⠞⠾", filter.filter("Red Port [räd pårt]"));
    }

    // COULDDO 2.3.4
    // 2.3.5 - Braces
    @Test
    public void testSwedishFilter_Braces() {
        assertEquals(
            "⠠⠷⠼⠁⠂ ⠼⠉⠂ ⠼⠑⠠⠾ ⠥⠞⠇⠜⠎⠑⠎ ⠍⠜⠝⠛⠙⠑⠝ ⠁⠧ ⠞⠁⠇⠑⠝ ⠑⠞⠞⠂ ⠞⠗⠑ ⠕⠉⠓ ⠋⠑⠍⠄",
            filter.filter("{1, 3, 5} utläses mängden av talen ett, tre och fem.")
        );
    }

    // 2.4.1 (ex 2) COULDDO ex 1, 3
    @Test
    public void testSwedishFilter_2_4_1() {
        assertEquals("⠎⠑ ⠬ ⠼⠛⠤⠤⠼⠊", filter.filter("se § 7\u20139"));
    }

    // 2.4.2
    @Test
    public void testSwedishFilter_2_4_2() {
        assertEquals("⠠⠁⠇⠍⠟⠧⠊⠎⠞ ⠯ ⠠⠺⠊⠅⠎⠑⠇⠇", filter.filter("Almqvist & Wiksell"));
    }

    // 2.4.3 COULDDO ex 2, 3
    @Test
    public void testSwedishFilter_2_4_3() {
        assertEquals("⠠⠇⠁⠗⠎ ⠠⠛⠥⠎⠞⠁⠋⠎⠎⠕⠝ ⠔⠼⠁⠊⠉⠋", filter.filter("Lars Gustafsson *1936"));
    }

    // 2.4.4
    @Test
    public void testSwedishFilter_2_4_4() {
        assertEquals("⠞⠗⠽⠉⠅ ⠘⠼⠼⠃⠁⠘⠼", filter.filter("tryck #21#"));
    }

    // 2.4.5, 2.4.7
    @Test
    public void testSwedishFilter_2_4_5() {
        assertEquals("⠑⠍⠊⠇⠘⠤⠑⠍⠊⠇⠎⠎⠕⠝⠘⠷⠓⠕⠞⠍⠁⠊⠇⠄⠉⠕⠍", filter.filter("emil_emilsson@hotmail.com"));
    }

    // 2.4.6
    @Test
    public void testSwedishFilter_2_4_6() {
        assertEquals(
            "⠠⠉⠒⠘⠌⠠⠠⠺⠊⠝⠙⠕⠺⠎⠘⠌⠎⠽⠎⠞⠑⠍⠘⠌⠇⠕⠛⠊⠝⠺⠼⠉⠁⠄⠙⠇⠇",
            filter.filter("C:\\WINDOWS\\system\\loginw31.dll")
        );
    }

    // 2.4.8
    @Test
    public void testSwedishFilter_2_4_8() {
        assertEquals("⠁⠇⠅⠸⠁", filter.filter("alk|a"));
    }

    // 2.4.9
    @Test
    public void testSwedishFilter_2_4_9() {
        assertEquals("⠎⠥⠃⠎⠞⠄ ⠘⠒⠝ ⠘⠒⠁⠗", filter.filter("subst. ~n ~ar"));
    }

    // 2.5
    @Test
    public void testSwedishFilter_2_5() {
        assertEquals("⠠⠏⠗⠊⠎⠑⠞ ⠧⠁⠗ ⠼⠑⠚⠚ ⠘⠑⠄", filter.filter("Priset var 500 €."));
    }

    // COULDDO 2.6, 2.7
    // 3.2 - Uppercase
    // 3.2.1
    @Test
    public void testSwedishFilter_3_2_1() {
        assertEquals(
            "⠠⠓⠁⠝ ⠓⠑⠞⠑⠗ ⠠⠓⠁⠝⠎ ⠕⠉⠓ ⠃⠗⠕⠗ ⠓⠁⠝⠎ ⠓⠑⠞⠑⠗ ⠠⠃⠗⠕⠗⠄",
            filter.filter("Han heter Hans och bror hans heter Bror.")
        );
    }

    // 3.2.2
    @Test
    public void testSwedishFilter_3_2_2_ex1() {
        assertEquals("⠠⠠⠎⠁⠧", filter.filter("SAV"));
    }

    @Test
    public void testSwedishFilter_3_2_2_ex2() {
        assertEquals("⠠⠠⠊⠅⠑⠁⠱⠎ ⠅⠁⠞⠁⠇⠕⠛", filter.filter("IKEAs katalog"));
    }

    @Test
    public void testSwedishFilter_3_2_2_ex3() {
        assertEquals("⠠⠎⠧⠑⠝⠎⠅⠁ ⠠⠠⠊⠎⠃⠝⠱-⠉⠑⠝⠞⠗⠁⠇⠑⠝", filter.filter("Svenska ISBN-centralen"));
    }

    // 3.2.3
    @Test
    public void testSwedishFilter_3_2_3_ex1() {
        assertEquals(
            "⠠⠠⠠⠇⠕⠌⠞⠉⠕⠌⠎⠁⠉⠕⠱⠒⠎ ⠠⠃⠗⠽⠎⠎⠑⠇⠅⠕⠝⠞⠕⠗",
            filter.filter("LO/TCO/SACO:s Brysselkontor")
        );
    }

    @Test
    public void testSwedishFilter_3_2_3_ex2() {
        assertEquals("⠠⠠⠠⠎⠽⠝⠎⠅⠁⠙⠁⠙⠑⠎ ⠗⠊⠅⠎⠋⠪⠗⠃⠥⠝⠙⠱", filter.filter("SYNSKADADES RIKSFÖRBUND"));
    }

    @Test
    public void testSwedishFilter_3_2_3_ex3() {
        assertEquals("⠅⠠⠺⠓⠂ ⠠⠚⠜⠍⠠⠕", filter.filter("kWh, JämO"));
    }

    @Test
    public void testSwedishFilter_3_2_3_ex4() {
        assertEquals("⠠⠇⠪⠎⠑⠝⠕⠗⠙⠒ ⠕⠠⠧⠃⠠⠑⠠⠛⠚", filter.filter("Lösenord: oVbEGj"));
    }

    // 3.3.1
    @Test
    public void testSwedishFilter_3_3() {
        assertEquals("⠠⠇⠪⠎⠑⠝⠕⠗⠙⠒ ⠇⠧⠃⠼⠑⠛⠱⠚", filter.filter("Lösenord: lvb57j"));
    }

    @Test
    public void testSwedishFilter_additional_ex1() throws FileNotFoundException {
        assertEquals(
            "⠘⠦⠎⠞⠚⠜⠗⠝⠁⠘⠴ ⠘⠦⠃⠇⠊⠭⠞⠘⠴ ⠘⠦⠒⠦⠘⠴ ⠘⠦⠒⠴⠘⠴ ⠬⠕",
            filter.filter("\u066d \u2607 \u2639 \u263a \u00ba")
        );
    }

    @Test
    public void testSwedishFilter_additional_ex2() throws FileNotFoundException {
        assertEquals("⠠⠝⠑⠛⠁⠞⠊⠧⠁ ⠞⠁⠇⠒ -⠼⠙⠑⠋⠙⠑", filter.filter("Negativa tal: -45645"));
    }

    @Test
    public void testSwedishFilter_additional_ex3() throws FileNotFoundException {
        assertEquals("⠘⠦⠓⠚⠜⠗⠞⠑⠗⠘⠴", filter.filter("\u2665")); // hjärter
    }

    @Test
    public void testFinalizer_001() {
        DefaultBrailleFinalizer finalizer = new DefaultBrailleFinalizer();
        assertEquals(
            "This\u2800is\u2800a\u2800test\u2800string\u2800to\u2800" +
                    "finalize\u2800\u2824\u2800nothing\u2800more.",
            finalizer.finalizeBraille("This is a test string to finalize - nothing more.")
        );
    }

    @Test
    @Ignore
    public void testFinalizer_performance() {
        //This test is most interesting to run manually when optimizing performance, but it is included
        //here in case of future improvements.
        String s = "This is a test string to finalize - nothing more.";
        int threshold = 500;
        DefaultBrailleFinalizer f = new DefaultBrailleFinalizer();
        long d = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            f.finalizeBraille(s);
        }
        long actualTime = System.currentTimeMillis() - d;
        assertTrue(
            "Time exceeded threshold (" + threshold + " ms), was " + actualTime + " ms.",
            (actualTime < threshold)
        );
    }

    @Test
    public void testFractions_001() {
        assertEquals("⠼⠃⠼⠁⠌⠼⠙⠂ ⠼⠁⠼⠁⠌⠼⠃⠂ ⠼⠉⠼⠉⠌⠼⠙", filter.filter("2¼, 1½, 3¾"));
    }

    @Test
    public void testCapitalIWithDot() {
        assertEquals("⠠⠈⠊", filter.filter("İ"));
    }

    @Test
    public void testExactDiacriticalMarks() {
        assertEquals(
            "⠷ ⠜⠗ ⠁ ⠍⠑⠙ ⠛⠗⠁⠧ ⠁⠉⠉⠑⠝⠞",
            filter.filter("à är a med grav accent")
        );
        assertEquals(
            "⠠⠷ ⠜⠗ ⠑⠞⠞ ⠧⠑⠗⠎⠁⠇⠞ ⠠⠁ ⠍⠑⠙ ⠛⠗⠁⠧ ⠁⠉⠉⠑⠝⠞",
            filter.filter("À är ett versalt A med grav accent")
        );
        assertEquals(
            "⠿ ⠜⠗ ⠑⠞⠞ ⠑ ⠍⠑⠙ ⠁⠅⠥⠞ ⠁⠉⠉⠑⠝⠞",
            filter.filter("é är ett e med akut accent")
        );
        assertEquals(
            "⠠⠿ ⠜⠗ ⠑⠞⠞ ⠧⠑⠗⠎⠁⠇⠞ ⠠⠑ ⠑ ⠍⠑⠙ ⠁⠅⠥⠞ ⠁⠉⠉⠑⠝⠞",
            filter.filter("É är ett versalt E e med akut accent")
    );
        assertEquals(
            "⠮ ⠜⠗ ⠑⠞⠞ ⠑ ⠍⠑⠙ ⠛⠗⠁⠧ ⠁⠉⠉⠑⠝⠞",
            filter.filter("è är ett e med grav accent")
        );
        assertEquals(
            "⠠⠮ ⠜⠗ ⠑⠞⠞ ⠧⠑⠗⠎⠁⠇⠞ ⠠⠑ ⠑ ⠍⠑⠙ ⠛⠗⠁⠧ ⠁⠉⠉⠑⠝⠞",
            filter.filter("È är ett versalt E e med grav accent")
        );
        assertEquals(
            "⠳ ⠜⠗ ⠑⠞⠞ ⠥ ⠞⠽⠎⠅⠞ ⠽⠂ ⠥ ⠍⠑⠙ ⠞⠗⠑⠍⠁",
            filter.filter("ü är ett u tyskt y, u med trema")
        );
        assertEquals(
            "⠠⠳ ⠜⠗ ⠑⠞⠞ ⠧⠑⠗⠎⠁⠇⠞ ⠠⠥ ⠞⠽⠎⠅⠞ ⠽⠂ ⠥ ⠍⠑⠙ ⠞⠗⠑⠍⠁",
            filter.filter("Ü är ett versalt U tyskt y, u med trema")
        );
    }

    @Test
    public void testGeneralDiacriticalMarksInSentences() {
        String str = "Biff à la Lindström till supé är mitt förslag, sa Irène Blücher.";
        assertEquals(
            "⠠⠃⠊⠋⠋ ⠷ ⠇⠁ ⠠⠇⠊⠝⠙⠎⠞⠗⠪⠍ ⠞⠊⠇⠇ ⠎⠥⠏⠿ ⠜⠗ ⠍⠊⠞⠞ ⠋⠪⠗⠎⠇⠁⠛⠂ ⠎⠁ ⠠⠊⠗⠮⠝⠑ ⠠⠃⠇⠳⠉⠓⠑⠗⠄",
            filter.filter(str)
        );
        str = "Rhône, Moçambique, Boëthius, Dvořák";
        assertEquals("⠠⠗⠓⠈⠕⠝⠑⠂ ⠠⠍⠕⠈⠉⠁⠍⠃⠊⠟⠥⠑⠂ ⠠⠃⠕⠈⠑⠞⠓⠊⠥⠎⠂ ⠠⠙⠧⠕⠈⠗⠈⠁⠅", filter.filter(str));
        str = "Jūratė";
        assertEquals("⠠⠚⠈⠥⠗⠁⠞⠈⠑", filter.filter(str));
    }

    @Test
    public void testGeneralDiacriticalMarks() {
        // This characters are taken from a list of letters with diacritical marks.
        // https://sv.wikipedia.org/wiki/Diakritiskt_tecken
        /* acute accent */
        assertEquals("⠈⠁", filter.filter("á"));
        assertEquals("⠈⠉", filter.filter("ć"));
        assertEquals("⠈⠊", filter.filter("í"));
        assertEquals("⠈⠇", filter.filter("ĺ"));
        assertEquals("⠈⠝", filter.filter("ń"));
        assertEquals("⠈⠕", filter.filter("ó"));
        assertEquals("⠈⠗", filter.filter("ŕ"));
        assertEquals("⠈⠎", filter.filter("ś"));
        assertEquals("⠈⠥", filter.filter("ú"));
        assertEquals("⠈⠽", filter.filter("ý"));
        assertEquals("⠈⠵", filter.filter("ź"));

        /* double acute accent */
        assertEquals("⠈⠕", filter.filter("ő"));
        assertEquals("⠈⠥", filter.filter("ű"));

        /* grave accent*/
        assertEquals("⠈⠊", filter.filter("ì"));
        assertEquals("⠈⠕", filter.filter("ò"));
        assertEquals("⠈⠥", filter.filter("ù"));

        /* diaeresis */
        assertEquals("⠈⠑", filter.filter("ë"));
        assertEquals("⠈⠊", filter.filter("ï"));
        assertEquals("⠈⠽", filter.filter("ÿ"));

        /* circumflex */
        assertEquals("⠈⠁", filter.filter("â"));
        assertEquals("⠈⠉", filter.filter("ĉ"));
        assertEquals("⠈⠑", filter.filter("ê"));
        assertEquals("⠈⠛", filter.filter("ĝ"));
        assertEquals("⠈⠓", filter.filter("ĥ"));
        assertEquals("⠈⠊", filter.filter("î"));
        assertEquals("⠈⠚", filter.filter("ĵ"));
        assertEquals("⠈⠕", filter.filter("ô"));
        assertEquals("⠈⠎", filter.filter("ŝ"));
        assertEquals("⠈⠥", filter.filter("û"));
        assertEquals("⠈⠽", filter.filter("ŷ"));

        /* tilde */
        assertEquals("⠈⠁", filter.filter("ã"));
        assertEquals("⠈⠝", filter.filter("ñ"));
        assertEquals("⠈⠊", filter.filter("ĩ"));
        assertEquals("⠈⠕", filter.filter("õ"));
        assertEquals("⠈⠥", filter.filter("ũ"));

        /* cedilla */
        assertEquals("⠈⠉", filter.filter("ç"));
        assertEquals("⠈⠙", filter.filter("ḑ"));
        assertEquals("⠈⠅", filter.filter("ķ"));
        assertEquals("⠈⠇", filter.filter("ļ"));
        assertEquals("⠈⠝", filter.filter("ņ"));
        assertEquals("⠈⠗", filter.filter("ŗ"));
        assertEquals("⠈⠎", filter.filter("ş"));
        assertEquals("⠈⠞", filter.filter("ţ"));

        /* cedilla over */
        assertEquals("⠈⠛", filter.filter("ģ"));

        /* comma */
        assertEquals("⠈⠎", filter.filter("ș"));
        assertEquals("⠈⠞", filter.filter("ț"));

        /* caron */
        assertEquals("⠈⠁", filter.filter("ǎ"));
        assertEquals("⠈⠉", filter.filter("č"));
        assertEquals("⠈⠑", filter.filter("ě"));
        assertEquals("⠈⠊", filter.filter("ǐ"));
        assertEquals("⠈⠝", filter.filter("ň"));
        assertEquals("⠈⠕", filter.filter("ǒ"));
        assertEquals("⠈⠗", filter.filter("ř"));
        assertEquals("⠈⠎", filter.filter("š"));
        assertEquals("⠈⠥", filter.filter("ǔ"));
        assertEquals("⠈⠵", filter.filter("ž"));

        /* ogonek */
        assertEquals("⠈⠁", filter.filter("ą"));
        assertEquals("⠈⠑", filter.filter("ę"));
        assertEquals("⠈⠊", filter.filter("į"));
        assertEquals("⠈⠥", filter.filter("ų"));

        /* Breve */
        assertEquals("⠈⠁", filter.filter("ă"));
        assertEquals("⠈⠛", filter.filter("ğ"));
        assertEquals("⠈⠊", filter.filter("ĭ"));
        assertEquals("⠈⠥", filter.filter("ŭ"));

        /* macron */
        assertEquals("⠈⠁", filter.filter("ā"));
        assertEquals("⠈⠑", filter.filter("ē"));
        assertEquals("⠈⠊", filter.filter("ī"));
        assertEquals("⠈⠕", filter.filter("ō"));
        assertEquals("⠈⠥", filter.filter("ū"));
        assertEquals("⠈⠥", filter.filter("ǖ"));

        /* full point */
        assertEquals("⠈⠉", filter.filter("ċ"));
        assertEquals("⠈⠑", filter.filter("ė"));
        assertEquals("⠈⠛", filter.filter("ġ"));
        assertEquals("⠈⠵", filter.filter("ż"));

        /* ring */
        assertEquals("⠈⠥", filter.filter("ů"));

        /* apostrophe */
        assertEquals("⠈⠙", filter.filter("ď"));
        assertEquals("⠈⠇", filter.filter("ľ"));
        assertEquals("⠈⠞", filter.filter("ť"));

        /* slash*/
        assertEquals("⠈⠇", filter.filter("ł"));

        /*  Others */
        assertEquals("⠈⠙", filter.filter("đ"));
        assertEquals("⠈⠓", filter.filter("ħ"));
        assertEquals("⠈⠞", filter.filter("ŧ"));
        assertEquals("⠈⠑", filter.filter("ẻ"));
        assertEquals("⠈⠑", filter.filter("ế"));
        assertEquals("⠈⠑", filter.filter("ệ"));
        assertEquals("⠈⠑", filter.filter("ễ"));
    }
}
