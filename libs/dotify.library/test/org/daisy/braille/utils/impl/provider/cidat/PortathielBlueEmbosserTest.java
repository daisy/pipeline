package org.daisy.braille.utils.impl.provider.cidat;

import org.daisy.braille.utils.pef.FileCompare;
import org.daisy.braille.utils.pef.PEFConverterFacade;
import org.daisy.braille.utils.pef.PEFHandler;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.daisy.dotify.api.embosser.EmbosserCatalog;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.embosser.EmbosserWriter;
import org.daisy.dotify.api.paper.PageFormat;
import org.daisy.dotify.api.paper.PaperCatalog;
import org.daisy.dotify.api.paper.SheetPaper;
import org.daisy.dotify.api.paper.SheetPaperFormat;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.dotify.common.io.FileIO;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Bert Frees
 */
@SuppressWarnings("javadoc")
public class PortathielBlueEmbosserTest {

    private static EmbosserCatalog ec = EmbosserCatalog.newInstance();
    private static CidatEmbosser e =
            (CidatEmbosser) ec.get("es_once_cidat.CidatEmbosserProvider.EmbosserType.PORTATHIEL_BLUE");

    private static PaperCatalog pc = PaperCatalog.newInstance();
    private static PageFormat a4 =
            new SheetPaperFormat(
                (SheetPaper) pc.get("org_daisy.ISO216PaperProvider.PaperSize.A4"),
                SheetPaperFormat.Orientation.DEFAULT
            );

    @Test
    public void testPrintableArea() {

        assertEquals("Assert that max width for a A4 paper is 33 cells", e.getMaxWidth(a4), 33);
        assertEquals("Assert that max height for a A4 paper is 29 lines", e.getMaxHeight(a4), 29);
    }

    @Test
    @Ignore
    public void testTableFilter() {

        TableCatalog tc = TableCatalog.newInstance();
        assertTrue(
            "Assert that number of character sets is 2",
            tc.list(e.getTableFilter()).size() == 2
        );
    }

    @Test
    public void testDuplex() {
        assertTrue("Assert that duplex is supported", e.supportsDuplex());
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
    @Ignore
    public void testEmbosserWriter() throws IOException,
            ParserConfigurationException,
            SAXException,
            UnsupportedWidthException {

        File prn1 = File.createTempFile("test_portathiel_", ".prn");
        File prn2 = File.createTempFile("test_portathiel_", ".prn");
        File pef = File.createTempFile("test_portathiel_", ".pef");

        FileCompare fc = new FileCompare();
        PEFHandler.Builder builder;
        EmbosserWriter w;

        e.setFeature(EmbosserFeatures.PAGE_FORMAT, a4);

        // Single sided, transparent mode

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                .range(null)
                .align(org.daisy.braille.utils.pef.PEFHandler.Alignment.INNER)
                .offset(0)
                .topOffset(0);

        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/single_sided.pef"),
            new FileOutputStream(pef)
        );
        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/portathiel_transparent_single_sided.prn"),
            new FileOutputStream(prn2)
        );
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        try (InputStream is2 = new FileInputStream(prn2)) {
            assertTrue("Assert that the contents of the file is as expected.",
                    fc.compareBinary(new FileInputStream(prn1), is2)
            );
        }

        // Double sided, transparent mode

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                .range(null)
                .align(org.daisy.braille.utils.pef.PEFHandler.Alignment.INNER)
                .offset(0)
                .topOffset(0);

        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/double_sided.pef"),
            new FileOutputStream(pef)
        );
        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/portathiel_transparent_double_sided.prn"),
            new FileOutputStream(prn2)
        );
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        try (InputStream is2 = new FileInputStream(prn2)) {
            assertTrue("Assert that the contents of the file is as expected.",
                    fc.compareBinary(new FileInputStream(prn1), is2)
            );
        }

        // Single sided, MIT set

        e.setFeature(EmbosserFeatures.TABLE, "org_daisy.EmbosserTableProvider.TableType.MIT");
        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                .range(null)
                .align(org.daisy.braille.utils.pef.PEFHandler.Alignment.INNER)
                .offset(0)
                .topOffset(0);

        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/single_sided.pef"),
            new FileOutputStream(pef)
        );
        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/portathiel_mit_single_sided.prn"),
            new FileOutputStream(prn2)
        );
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        try (InputStream is2 = new FileInputStream(prn2)) {
            assertTrue("Assert that the contents of the file is as expected.",
                    fc.compareBinary(new FileInputStream(prn1), is2)
            );
        }

        // Double sided, MIT set

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                .range(null)
                .align(org.daisy.braille.utils.pef.PEFHandler.Alignment.INNER)
                .offset(0)
                .topOffset(0);

        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/double_sided.pef"),
            new FileOutputStream(pef)
        );
        FileIO.copy(this.getClass().getResourceAsStream(
            "resource-files/portathiel_mit_double_sided.prn"),
            new FileOutputStream(prn2)
        );
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        try (InputStream is2 = new FileInputStream(prn2)) {
            assertTrue("Assert that the contents of the file is as expected.",
                    fc.compareBinary(new FileInputStream(prn1), is2)
            );
        }

        prn1.deleteOnExit();
        prn2.deleteOnExit();
        pef.deleteOnExit();
    }
}
