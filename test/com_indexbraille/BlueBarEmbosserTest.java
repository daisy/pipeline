package com_indexbraille;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.embosser.EmbosserCatalog;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.UnsupportedWidthException;
import org.daisy.braille.facade.PEFConverterFacade;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.table.TableCatalog;
import org.daisy.braille.tools.FileCompare;
import org.daisy.braille.tools.FileTools;
import org.daisy.paper.PageFormat;
import org.daisy.paper.PaperCatalog;
import org.daisy.paper.TractorPaper;
import org.daisy.paper.TractorPaperFormat;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Bert Frees
 */
public class BlueBarEmbosserTest {

    private static EmbosserCatalog ec = EmbosserCatalog.newInstance();
    private static BlueBarEmbosser e = (BlueBarEmbosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_BLUE_BAR");
    private static PaperCatalog pc = PaperCatalog.newInstance();
    private static PageFormat _210mm_12inch = new TractorPaperFormat((TractorPaper)pc.get("org_daisy.TractorPaperProvider.PaperSize.W210MM_X_H12INCH"));
    private static PageFormat _280mm_12inch = new TractorPaperFormat((TractorPaper)pc.get("org_daisy.TractorPaperProvider.PaperSize.W280MM_X_H12INCH"));

    @Test
    public void testPrintableArea() {

        assertEquals("Assert that max width for a 210mm by 12 inch paper is 35 cells",  e.getMaxWidth(_210mm_12inch),  35);
        assertEquals("Assert that max width for a 280mm by 12 inch paper is 46 cells",  e.getMaxWidth(_280mm_12inch),  46);
        assertEquals("Assert that max height for a 280mm by 12 inch paper is 30 lines", e.getMaxHeight(_280mm_12inch), 30);
    }

    @Test
    public void testTableFilter() {

        TableCatalog tc = TableCatalog.newInstance();
	assertTrue("Assert that encoding cannot be modified", tc.list(e.getTableFilter()).size() <= 1);
    }

    @Test
    public void testDuplex() {
        assertTrue("Assert that duplex is not supported", !e.supportsDuplex());
    }

    @Test
    public void test8dot() {
        assertTrue("Assert that 8-dot is not supported", !e.supports8dot());
    }

    @Test
    public void testAligning() {
        assertTrue("Assert that aligning is supported", e.supportsAligning());
    }

    @Test
    public void testEmbosserWriter() throws IOException,
                                            ParserConfigurationException,
                                            SAXException,
                                            UnsupportedWidthException {

        File prn1 = File.createTempFile("test_bluebar", ".prn");
        File prn2 = File.createTempFile("test_bluebar", ".prn");
        File pef =  File.createTempFile("test_bluebar", ".pef");

        FileCompare fc = new FileCompare();
        PEFHandler.Builder builder;
        EmbosserWriter w;

        e.setFeature(EmbosserFeatures.PAGE_FORMAT, _280mm_12inch);

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/single_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/bluebar_single_sided.prn"), new FileOutputStream(prn2));
        PEFConverterFacade.parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        prn1.deleteOnExit();
        prn2.deleteOnExit();
        pef.deleteOnExit();
    }
}
