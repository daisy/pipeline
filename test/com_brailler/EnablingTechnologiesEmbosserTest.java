package com_brailler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.SheetPaper;
import org.daisy.braille.api.paper.SheetPaperFormat;
import org.daisy.braille.consumer.paper.PaperCatalog;
import org.daisy.braille.consumer.table.TableCatalog;
import org.daisy.braille.embosser.EmbosserCatalog;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.UnsupportedWidthException;
import org.daisy.braille.facade.PEFConverterFacade;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.tools.FileCompare;
import org.daisy.braille.tools.FileTools;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Bert Frees
 */
public class EnablingTechnologiesEmbosserTest {

    private static EmbosserCatalog ec = EmbosserCatalog.newInstance();
    private static EnablingTechnologiesEmbosser ra =   (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_ATTACHE");
    private static EnablingTechnologiesEmbosser rap =  (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_ATTACHE_PRO");
    private static EnablingTechnologiesEmbosser r25 =  (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_25");
    private static EnablingTechnologiesEmbosser rp50 = (EnablingTechnologiesEmbosser)ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_50");
    private static EnablingTechnologiesEmbosser rpln = (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_LE_NARROW");
    private static EnablingTechnologiesEmbosser rplw =  (EnablingTechnologiesEmbosser)ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_LE_WIDE");
    private static EnablingTechnologiesEmbosser t =     (EnablingTechnologiesEmbosser)ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.THOMAS");
    private static EnablingTechnologiesEmbosser tp =    (EnablingTechnologiesEmbosser)ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.THOMAS_PRO");
    private static EnablingTechnologiesEmbosser m =    (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.MARATHON");
    private static EnablingTechnologiesEmbosser et =   (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ET");
    private static EnablingTechnologiesEmbosser jp =   (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_PRO");
    private static EnablingTechnologiesEmbosser jp60 = (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_PRO_60");
    private static EnablingTechnologiesEmbosser jc =   (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_CLASSIC");
    private static EnablingTechnologiesEmbosser bm =   (EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BOOKMAKER");
    private static EnablingTechnologiesEmbosser be100 =(EnablingTechnologiesEmbosser) ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_EXPRESS_100");
    private static EnablingTechnologiesEmbosser be150 = (EnablingTechnologiesEmbosser)ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_EXPRESS_150");
    private static EnablingTechnologiesEmbosser bp =    (EnablingTechnologiesEmbosser)ec.get("com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_PLACE");

    private static PaperCatalog pc = PaperCatalog.newInstance();
    private static PageFormat a4 = new SheetPaperFormat((SheetPaper)pc.get("org_daisy.ISO216PaperProvider.PaperSize.A4"), SheetPaperFormat.Orientation.DEFAULT);

    @Test
    public void testPrintableArea() {

        assertEquals("Assert that max width for an A4 paper is 34 cells",  ra.getMaxWidth(a4),  34);
        assertEquals("Assert that max height for an A4 paper is 29 lines", ra.getMaxHeight(a4), 29);
    }

    @Test
    public void testTableFilter() {

        TableCatalog tc = TableCatalog.newInstance();

	assertTrue("Assert that encoding cannot be modified", tc.list(ra.getTableFilter()).size() <= 1);
    }

    @Test
    public void testDuplex() {

        assertTrue("Assert that duplex is not supported for " + ra.getDisplayName(),   !ra.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + rap.getDisplayName(),  !rap.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + r25.getDisplayName(),  !r25.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + rp50.getDisplayName(), !rp50.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + rpln.getDisplayName(), !rpln.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + rplw.getDisplayName(), !rplw.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + t.getDisplayName(),    !t.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + tp.getDisplayName(),   !tp.supportsDuplex());
        assertTrue("Assert that duplex is not supported for " + m.getDisplayName(),    !m.supportsDuplex());

        assertTrue("Assert that duplex is supported for " + et.getDisplayName(),    et.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + jp.getDisplayName(),    jp.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + jp60.getDisplayName(),  jp60.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + jc.getDisplayName(),    jc.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + bm.getDisplayName(),    bm.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + be100.getDisplayName(), be100.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + be150.getDisplayName(), be150.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + bp.getDisplayName(),    bp.supportsDuplex());
    }

    @Test
    public void test8dot() {

        assertTrue("Assert that 8-dot is not supported", !ra.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !rap.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !r25.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !rp50.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !rpln.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !rplw.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !t.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !tp.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !m.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !et.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !jp.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !jp60.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !jc.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !bm.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !be100.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !be150.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !bp.supports8dot());
    }

    @Test
    public void testAligning() {

        assertTrue("Assert that aligning is supported", ra.supportsAligning());
        assertTrue("Assert that aligning is supported", rap.supportsAligning());
        assertTrue("Assert that aligning is supported", r25.supportsAligning());
        assertTrue("Assert that aligning is supported", rp50.supportsAligning());
        assertTrue("Assert that aligning is supported", rpln.supportsAligning());
        assertTrue("Assert that aligning is supported", rplw.supportsAligning());
        assertTrue("Assert that aligning is supported", t.supportsAligning());
        assertTrue("Assert that aligning is supported", tp.supportsAligning());
        assertTrue("Assert that aligning is supported", m.supportsAligning());
        assertTrue("Assert that aligning is supported", et.supportsAligning());
        assertTrue("Assert that aligning is supported", jp.supportsAligning());
        assertTrue("Assert that aligning is supported", jp60.supportsAligning());
        assertTrue("Assert that aligning is supported", jc.supportsAligning());
        assertTrue("Assert that aligning is supported", bm.supportsAligning());
        assertTrue("Assert that aligning is supported", be100.supportsAligning());
        assertTrue("Assert that aligning is supported", be150.supportsAligning());
        assertTrue("Assert that aligning is supported", bp.supportsAligning());
    }

    @Test
    public void testEmbosserWriter() throws IOException,
                                            ParserConfigurationException,
                                            SAXException,
                                            UnsupportedWidthException {

        File prn1 = File.createTempFile("test_enabling_technologies_", ".prn");
        File prn2 = File.createTempFile("test_enabling_technologies_", ".prn");
        File pef =  File.createTempFile("test_enabling_technologies_", ".pef");

        FileCompare fc = new FileCompare();
        PEFHandler.Builder builder;
        EmbosserWriter w;

        t.setFeature(EmbosserFeatures.PAGE_FORMAT, a4);
        jp.setFeature(EmbosserFeatures.PAGE_FORMAT, a4);

        // Single sided on single sided embosser

        w = t.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/single_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/thomas_single_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Single sided on double sided embosser

        w = jp.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/juliet_pro_single_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Double sided

        w = jp.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/double_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/juliet_pro_double_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        prn1.deleteOnExit();
        prn2.deleteOnExit();
        pef.deleteOnExit();
    }
}
