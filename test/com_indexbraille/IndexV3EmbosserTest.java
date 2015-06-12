package com_indexbraille;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.api.embosser.UnsupportedWidthException;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.SheetPaper;
import org.daisy.braille.api.paper.SheetPaperFormat;
import org.daisy.braille.api.paper.TractorPaper;
import org.daisy.braille.api.paper.TractorPaperFormat;
import org.daisy.braille.consumer.embosser.EmbosserCatalog;
import org.daisy.braille.consumer.paper.PaperCatalog;
import org.daisy.braille.consumer.table.TableCatalog;
import org.daisy.braille.pef.PEFConverterFacade;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.tools.FileCompare;
import org.daisy.braille.tools.FileTools;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Bert Frees
 */
public class IndexV3EmbosserTest {

    private static EmbosserCatalog ec = EmbosserCatalog.newInstance();
    private static IndexV3Embosser everest = (IndexV3Embosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V3");
    private static IndexV3Embosser basic_s = (IndexV3Embosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_S_V3");
    private static IndexV3Embosser basic_d = (IndexV3Embosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V3");
    private static IndexV3Embosser _4x4pro = (IndexV3Embosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4X4_PRO_V3");
    private static IndexV3Embosser _4waves = (IndexV3Embosser)ec.get("com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4WAVES_PRO_V3");

    private static PaperCatalog pc = PaperCatalog.newInstance();
    private static PageFormat a3 = new SheetPaperFormat((SheetPaper)pc.get("org_daisy.ISO216PaperProvider.PaperSize.A3"), SheetPaperFormat.Orientation.DEFAULT);
    private static PageFormat a3_landscape = new SheetPaperFormat((SheetPaper)pc.get("org_daisy.ISO216PaperProvider.PaperSize.A3"), SheetPaperFormat.Orientation.REVERSED);
    private static PageFormat _210mm_12inch = new TractorPaperFormat((TractorPaper)pc.get("org_daisy.TractorPaperProvider.PaperSize.W210MM_X_H12INCH"));
    private static PageFormat _280mm_12inch = new TractorPaperFormat((TractorPaper)pc.get("org_daisy.TractorPaperProvider.PaperSize.W280MM_X_H12INCH"));

    @Test
    public void testPrintableArea() {

        assertEquals("Assert that max width for a 210mm by 12 inch paper is 35 cells (Basic-S)",  35, basic_s.getMaxWidth(_210mm_12inch));
        assertEquals("Assert that max height for a 210mm by 12 inch paper is 30 lines (Basic-S)", 30, basic_s.getMaxHeight(_210mm_12inch));
        assertEquals("Assert that the absolute max width is 41 cells (Basic-S)",                  41, basic_s.getMaxWidth(_280mm_12inch));

        _4x4pro.setFeature(EmbosserFeatures.SADDLE_STITCH, false);

        assertEquals("Assert that the absolute max width is 41 cells (4X4 Pro)", 41,  _4x4pro.getMaxWidth(a3_landscape));
        assertEquals("Assert that the absolute max height is 29 lines (4X4 Pro)", 29, _4x4pro.getMaxHeight(a3_landscape));

        _4x4pro.setFeature(EmbosserFeatures.SADDLE_STITCH, true);

        assertEquals("Assert that max width for a A3 paper is 34 cells (4X4 Pro, if saddle stitch mode is on)",  _4x4pro.getMaxWidth(a3_landscape),  34);

    }

    @Test
    public void testTableFilter() {

        TableCatalog tc = TableCatalog.newInstance();
	assertTrue("Assert that encoding cannot be modified", tc.list(basic_s.getTableFilter()).size() <= 1);
    }

    @Test
    public void testDuplex() {

        assertTrue("Assert that duplex is not supported for " + basic_s.getDisplayName(), !basic_s.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + basic_d.getDisplayName(), basic_d.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + everest.getDisplayName(), everest.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + _4x4pro.getDisplayName(), _4x4pro.supportsDuplex());
        assertTrue("Assert that duplex is supported for " + _4waves.getDisplayName(), _4waves.supportsDuplex());
    }

    @Test
    public void test8dot() {

        assertTrue("Assert that 8-dot is not supported", !basic_s.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !basic_d.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !everest.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !_4x4pro.supports8dot());
        assertTrue("Assert that 8-dot is not supported", !_4waves.supports8dot());
    }

    @Test
    public void testAligning() {

        assertTrue("Assert that aligning is supported", basic_s.supportsAligning());
        assertTrue("Assert that aligning is supported", basic_d.supportsAligning());
        assertTrue("Assert that aligning is supported", everest.supportsAligning());
        assertTrue("Assert that aligning is supported", _4x4pro.supportsAligning());
        assertTrue("Assert that aligning is supported", _4waves.supportsAligning());
    }

    @Test
    public void testEmbosserWriter() throws IOException,
                                            ParserConfigurationException,
                                            SAXException,
                                            UnsupportedWidthException {

        File prn1 = File.createTempFile("test_indexv3_", ".prn");
        File prn2 = File.createTempFile("test_indexv3_", ".prn");
        File pef =  File.createTempFile("test_indexv3_", ".pef");

        FileCompare fc = new FileCompare();
        PEFHandler.Builder builder;
        EmbosserWriter w;

        basic_s.setFeature(EmbosserFeatures.PAGE_FORMAT, _280mm_12inch);
        basic_d.setFeature(EmbosserFeatures.PAGE_FORMAT, _280mm_12inch);
        everest.setFeature(EmbosserFeatures.PAGE_FORMAT, a3);
        _4x4pro.setFeature(EmbosserFeatures.PAGE_FORMAT, a3_landscape);

        // Single sided on a single sided printer

        w = basic_s.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/single_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/basic_s_v3_single_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Single sided on a double sided printer

        basic_d.setFeature(EmbosserFeatures.Z_FOLDING, false);
        w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v3_single_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Z-folding, single sided

        basic_d.setFeature(EmbosserFeatures.Z_FOLDING, true);
        w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v3_zfolding_single_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Double sided

        basic_d.setFeature(EmbosserFeatures.Z_FOLDING, false);
        w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/double_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v3_double_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Z-folding, double sided

        basic_d.setFeature(EmbosserFeatures.Z_FOLDING, true);
        w = basic_d.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/basic_d_v3_zfolding_double_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Everest

        w = everest.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/everest_v3_double_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // 4X4 Pro

        _4x4pro.setFeature(EmbosserFeatures.SADDLE_STITCH, false);
        w = _4x4pro.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/4x4_pro_v3_double_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // 4X4 Pro in saddle stitch mode

        _4x4pro.setFeature(EmbosserFeatures.SADDLE_STITCH, true);
        w = _4x4pro.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/4x4_pro_v3_saddle_stitch.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        prn1.deleteOnExit();
        prn2.deleteOnExit();
        pef.deleteOnExit();
    }
}
