package org.daisy.braille.utils.impl.provider.braillo;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.braille.utils.impl.provider.braillo.BrailloEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.junit.Test;
import org.xml.sax.SAXException;

@SuppressWarnings("javadoc")
public class Braillo200EmbosserTest extends AbstractTestBraillo200Embosser {

	public Braillo200EmbosserTest() {
		super(new Braillo200Embosser(TableCatalog.newInstance(), EmbosserType.BRAILLO_200));
		emb.setFeature(EmbosserFeatures.PAGE_FORMAT, tractor_210mm_x_12inch);
	}

	@Test
	public void testEmbossing() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException {
		performTest("resource-files/test-input-1.pef", "resource-files/test-input-1_braillo200_us_210mm_by_12inch.prn");
	}
}