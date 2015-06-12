package com_braillo;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.embosser.UnsupportedWidthException;
import org.daisy.braille.consumer.table.TableCatalog;
import org.junit.Test;
import org.xml.sax.SAXException;

import com_braillo.BrailloEmbosserProvider.EmbosserType;

public class Braillo440SW4PEmbosserTest extends AbstractTestBraillo440Embosser {

	public Braillo440SW4PEmbosserTest() {
		super(new Braillo440SWEmbosser(TableCatalog.newInstance(), EmbosserType.BRAILLO_440_SW));
		emb.setFeature(EmbosserFeatures.SADDLE_STITCH, true);
		emb.setFeature(EmbosserFeatures.PAGE_FORMAT, fa44_4p);
	}
	
	@Test
	public void testEmbossing() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException, TransformerException {
		performTest("resource-files/test-input-1.pef", "resource-files/test-input-1_braillo440SW_4p_us_fa44-", ".prn", 3);
	}
}
