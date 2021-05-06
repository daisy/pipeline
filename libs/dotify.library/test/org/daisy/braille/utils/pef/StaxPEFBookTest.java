package org.daisy.braille.utils.pef;

import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import static org.junit.Assert.assertEquals;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class StaxPEFBookTest {

    @Test
    public void testUnevenSectionsDuplex() throws
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            IOException,
            URISyntaxException {

        PEFBook p1 = StaxPEFBook.loadStax(
            this.getClass().getResource("resource-files/uneven-sections-duplex.pef").toURI()
        );
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
        PEFBook p1 = StaxPEFBook.loadStax(
            this.getClass().getResource("resource-files/uneven-sections-simplex.pef").toURI()
        );
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

        PEFBook p1 = StaxPEFBook.loadStax(
            this.getClass().getResource("resource-files/multiple-dimensions.pef").toURI()
        );
        assertEquals(false, p1.containsEightDot());
        assertEquals(30, p1.getMaxWidth());
        assertEquals(20, p1.getMaxHeight());
        assertEquals(3, p1.getFirstPage(2));
    }

    @Test
    public void testEightDot() throws
            XPathExpressionException,
            ParserConfigurationException,
            SAXException,
            IOException,
            URISyntaxException {
        PEFBook p1 = StaxPEFBook.loadStax(
            this.getClass().getResource("resource-files/8-dot-chart.pef").toURI()
        );
        assertEquals(true, p1.containsEightDot());
        assertEquals("8-dot Chart", p1.getTitle().iterator().next());
        assertEquals("org.pef-format.00005", p1.getMetadata("identifier").iterator().next());
    }
}
