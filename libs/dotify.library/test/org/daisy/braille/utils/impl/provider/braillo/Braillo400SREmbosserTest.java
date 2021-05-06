package org.daisy.braille.utils.impl.provider.braillo;

import org.daisy.braille.utils.impl.provider.braillo.BrailloEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.table.TableCatalog;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * TODO: write java doc.
 */
@SuppressWarnings("javadoc")
public class Braillo400SREmbosserTest extends AbstractTestBraillo200Embosser {

    public Braillo400SREmbosserTest() {
        super(new Braillo400SREmbosser(TableCatalog.newInstance(), EmbosserType.BRAILLO_400_SR));
        emb.setFeature(EmbosserFeatures.PAGE_FORMAT, roll_a4);
    }

    @Test
    public void testEmbossing() throws
            IOException,
            ParserConfigurationException,
            SAXException,
            UnsupportedWidthException {
        performTest(
            "resource-files/test-input-1.pef",
            "resource-files/test-input-1_braillo400SR_us_a4_roll.prn"
        );
    }
}
