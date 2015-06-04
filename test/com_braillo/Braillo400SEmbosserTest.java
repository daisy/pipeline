package com_braillo;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.consumer.table.TableCatalog;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.UnsupportedWidthException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com_braillo.BrailloEmbosserProvider.EmbosserType;

public class Braillo400SEmbosserTest extends AbstractTestBraillo200Embosser {
	
	public Braillo400SEmbosserTest() {
		super(new Braillo400SEmbosser(TableCatalog.newInstance(), EmbosserType.BRAILLO_400_S));
		emb.setFeature(EmbosserFeatures.PAGE_FORMAT, tractor_210mm_x_12inch);
	}
	
	@Test
	public void testEmbossing() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException {
		performTest("resource-files/test-input-1.pef", "resource-files/test-input-1_braillo400S_us_210mm_by_12inch.prn");
	}
}