package com_braillo;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.embosser.UnsupportedWidthException;
import org.daisy.braille.consumer.table.TableCatalog;
import org.junit.Test;
import org.xml.sax.SAXException;

import com_braillo.BrailloEmbosserProvider.EmbosserType;

public class Braillo270EmbosserTest extends AbstractTestBraillo200Embosser {
	
	public Braillo270EmbosserTest() {
		super(new Braillo200_270_400_v12_16Embosser(TableCatalog.newInstance(), EmbosserType.BRAILLO_270));
		emb.setFeature(EmbosserFeatures.PAGE_FORMAT, tractor_210mm_x_12inch);
	}
	
	@Test
	public void testEmbossing() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException {
		performTest("resource-files/test-input-1.pef", "resource-files/test-input-1_braillo270_us_210mm_by_12inch.prn");
	}
}