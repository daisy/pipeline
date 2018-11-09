package org.daisy.braille.utils.impl.provider.braillo;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.table.TableCatalog;
import org.daisy.braille.utils.impl.provider.braillo.BrailloEmbosserProvider.EmbosserType;
import org.daisy.braille.utils.pef.UnsupportedWidthException;
import org.junit.Test;
import org.xml.sax.SAXException;

@SuppressWarnings("javadoc")
public class Braillo440SWSFEmbosserTest extends AbstractTestBraillo440Embosser {

	public Braillo440SWSFEmbosserTest() {
		super(new Braillo440SFEmbosser(TableCatalog.newInstance(), EmbosserType.BRAILLO_440_SWSF));
		emb.setFeature(EmbosserFeatures.PAGE_FORMAT, fa44_4p);
	}

	@Test
	public void testEmbossing() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException, TransformerException {
		performTest("resource-files/test-input-1.pef", "resource-files/test-input-1_braillo440SWSF_us_fa44-", ".prn", 3);
	}
}
