package es_once_cidat;

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
import org.daisy.paper.SheetPaper;
import org.daisy.paper.SheetPaperFormat;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author Bert Frees
 */
public class PortathielBlueEmbosserTest {

    private static EmbosserCatalog ec = EmbosserCatalog.newInstance();
    private static CidatEmbosser e = (CidatEmbosser)ec.get("es_once_cidat.CidatEmbosserProvider.EmbosserType.PORTATHIEL_BLUE");

    private static PaperCatalog pc = PaperCatalog.newInstance();
    private static PageFormat a4 = new SheetPaperFormat((SheetPaper)pc.get("org_daisy.ISO216PaperProvider.PaperSize.A4"), SheetPaperFormat.Orientation.DEFAULT);

    @Test
    public void testPrintableArea() {

        assertEquals("Assert that max width for a A4 paper is 33 cells",  e.getMaxWidth(a4),  33);
        assertEquals("Assert that max height for a A4 paper is 29 lines", e.getMaxHeight(a4), 29);
    }

    @Test
    @Ignore
    public void testTableFilter() {

        TableCatalog tc = TableCatalog.newInstance();
	assertTrue("Assert that number of character sets is 2", tc.list(e.getTableFilter()).size() == 2);
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
        File pef =  File.createTempFile("test_portathiel_", ".pef");

        FileCompare fc = new FileCompare();
        PEFHandler.Builder builder;
        EmbosserWriter w;

        e.setFeature(EmbosserFeatures.PAGE_FORMAT, a4);

        // Single sided, transparent mode

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/single_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/portathiel_transparent_single_sided.prn"), new FileOutputStream(prn2));
        PEFConverterFacade.parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Double sided, transparent mode

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/double_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/portathiel_transparent_double_sided.prn"), new FileOutputStream(prn2));
        PEFConverterFacade.parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Single sided, MIT set

        e.setFeature(EmbosserFeatures.TABLE, "org_daisy.EmbosserTableProvider.TableType.MIT");
        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/single_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/portathiel_mit_single_sided.prn"), new FileOutputStream(prn2));
        PEFConverterFacade.parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Double sided, MIT set

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/double_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/portathiel_mit_double_sided.prn"), new FileOutputStream(prn2));
        PEFConverterFacade.parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        prn1.deleteOnExit();
        prn2.deleteOnExit();
        pef.deleteOnExit();
    }
}
