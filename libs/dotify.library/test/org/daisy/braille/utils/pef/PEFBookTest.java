package org.daisy.braille.utils.pef;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class PEFBookTest {

    @Test
    public void testSerializable() throws
            IOException,
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            URISyntaxException,
            ClassNotFoundException {

        File f = File.createTempFile("PEFBookTest", ".tmp");
        try {
            PEFBook p1 = PEFBook.load(this.getClass().getResource("resource-files/PEFBookTestInput.pef").toURI());
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
            oos.writeObject(p1);
            oos.close();
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
            PEFBook p2 = (PEFBook) ois.readObject();
            ois.close();
            assertTrue("Assert that deserialized object is equal to original object", p1.equals(p2));
        } finally {
            if (!f.delete()) {
                f.deleteOnExit();
            }
        }
    }

    @Test
    public void testUnevenSectionsDuplex() throws
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            IOException,
            URISyntaxException {

        PEFBook p1 = PEFBook.load(this.getClass().getResource("resource-files/uneven-sections-duplex.pef").toURI());
        assertEquals(false, p1.containsEightDot());
        assertEquals(5, p1.getPages());
        assertEquals(5, p1.getLastPage(1));
        assertEquals(3, p1.getPageTags());
        assertEquals(3, p1.getSheets());
    }

    @Test
    public void testUnevenSectionsSimplex() throws
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            IOException,
            URISyntaxException {

        PEFBook p1 = PEFBook.load(this.getClass().getResource("resource-files/uneven-sections-simplex.pef").toURI());
        assertEquals(false, p1.containsEightDot());
        assertEquals(6, p1.getPages());
        assertEquals(6, p1.getLastPage(1));
        assertEquals(3, p1.getPageTags());
        assertEquals(3, p1.getSheets());
    }

    @Test
    public void testMultipleDimensions() throws
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            IOException,
            URISyntaxException {

        PEFBook p1 = PEFBook.load(this.getClass().getResource("resource-files/multiple-dimensions.pef").toURI());
        assertEquals(false, p1.containsEightDot());
        assertEquals(30, p1.getMaxWidth());
        assertEquals(20, p1.getMaxHeight());
        assertEquals(3, p1.getFirstPage(2));
    }

    @Test
    public void testSections() throws
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            IOException,
            URISyntaxException {

        PEFBook p1 = PEFBook.load(this.getClass().getResource("resource-files/several-sections.pef").toURI());

        assertEquals(2, p1.getSectionsInVolume(1));
        assertEquals(1, p1.getSectionsInVolume(2));

        assertEquals(2, p1.getSheets(1));
        assertEquals(1, p1.getSheets(1, 1));
        assertEquals(1, p1.getSheets(1, 2));
        assertEquals(1, p1.getFirstPage(1));
        assertEquals(2, p1.getLastPage(1, 1));
        assertEquals(3, p1.getFirstPage(1, 2));
        assertEquals(4, p1.getLastPage(1));

        assertEquals(2, p1.getSheets(2));
        assertEquals(2, p1.getSheets(2, 1));
        assertEquals(5, p1.getFirstPage(2));
        assertEquals(7, p1.getLastPage(2));

    }

    @Test
    public void testEightDot() throws
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            IOException,
            URISyntaxException {

        PEFBook p1 = PEFBook.load(this.getClass().getResource("resource-files/8-dot-chart.pef").toURI());
        assertEquals(true, p1.containsEightDot());
        assertEquals("8-dot Chart", p1.getTitle().iterator().next());
        assertEquals("org.pef-format.00005", p1.getMetadata("identifier").iterator().next());
    }
}
