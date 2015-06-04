package be_interpoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.RollPaperFormat;
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

import be_interpoint.InterpointEmbosserProvider.EmbosserType;

/**
 *
 * @author Bert Frees
 */
public class Interpoint55EmbosserTest {

	private static Interpoint55Embosser e = new Interpoint55Embosser(TableCatalog.newInstance(), EmbosserType.INTERPOINT_55);
    private static PaperCatalog pc = PaperCatalog.newInstance();
    private static PageFormat a3 = new SheetPaperFormat((SheetPaper)pc.get("org_daisy.ISO216PaperProvider.PaperSize.A3"), SheetPaperFormat.Orientation.REVERSED);

    @Test
    public void testPageFormat() {

        PageFormat format = new RollPaperFormat(Length.newMillimeterValue(340d),
                                                Length.newMillimeterValue(297d));

        assertTrue("Assert that paper of width 340mm is supported", e.supportsPageFormat(format));

        format = new RollPaperFormat(Length.newMillimeterValue(341d),
                                     Length.newMillimeterValue(297d));
        
        assertTrue("Assert that paper wider than 340mm is not supported", !e.supportsPageFormat(format));
    }

    @Test
    public void testPrintableArea() {

        e.setFeature(EmbosserFeatures.SADDLE_STITCH, false);

        assertEquals("Assert that max width for an A3 paper is 70 cells (if saddle stitch mode is off)", 70, e.getMaxWidth(a3));
        assertEquals("Assert that max height for an A3 paper is 29 lines",                               29, e.getMaxHeight(a3));

        e.setFeature(EmbosserFeatures.SADDLE_STITCH, true);

        assertEquals("Assert that max width for an A3 paper is 35 cells (if saddle stitch mode is on)",  35, e.getMaxWidth(a3));
    }

    @Test
    public void testTableFilter() {
        TableCatalog tc = TableCatalog.newInstance();
	assertTrue("Assert that encoding can be modified", tc.list(e.getTableFilter()).size() == 3);
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
    public void testEmbosserWriter() throws IOException,
                                            ParserConfigurationException,
                                            SAXException,
                                            UnsupportedWidthException {

        File prn1 = File.createTempFile("test_interpoint55_", ".prn");
        File prn2 = File.createTempFile("test_interpoint55_", ".prn");
        File pef =  File.createTempFile("test_interpoint55_", ".pef");
        
        FileCompare fc = new FileCompare();
        PEFHandler.Builder builder;
        EmbosserWriter w;

        PageFormat a4 = new RollPaperFormat(Length.newMillimeterValue(297d),
                                            Length.newMillimeterValue(210d));
        e.setFeature(EmbosserFeatures.PAGE_FORMAT, a4);
        e.setFeature(EmbosserFeatures.SADDLE_STITCH, false);

        // Single sided

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/single_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/interpoint55_single_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        // Double sided

        w = e.newEmbosserWriter(new FileOutputStream(prn1));
        builder = new PEFHandler.Builder(w)
                          .range(null)
                          .align(org.daisy.braille.pef.PEFHandler.Alignment.INNER)
                          .offset(0)
                          .topOffset(0);

        FileTools.copy(this.getClass().getResourceAsStream("resource-files/double_sided.pef"), new FileOutputStream(pef));
        FileTools.copy(this.getClass().getResourceAsStream("resource-files/interpoint55_double_sided.prn"), new FileOutputStream(prn2));
        new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(pef, builder.build());
        assertTrue("Assert that the contents of the file is as expected.",
                fc.compareBinary(new FileInputStream(prn1), new FileInputStream(prn2))
        );

        prn1.deleteOnExit();
        prn2.deleteOnExit();
        pef.deleteOnExit();
    }
}
