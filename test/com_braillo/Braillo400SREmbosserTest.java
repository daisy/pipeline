package com_braillo;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.UnsupportedWidthException;
import org.junit.Test;
import org.xml.sax.SAXException;

import com_braillo.BrailloEmbosserProvider.EmbosserType;

public class Braillo400SREmbosserTest extends AbstractTestBraillo200Embosser {
	
	public Braillo400SREmbosserTest() {
		super(new Braillo400SREmbosser(EmbosserType.BRAILLO_400_SR));
		emb.setFeature(EmbosserFeatures.PAGE_FORMAT, roll_a4);
	}
	
	@Test
	public void testEmbossing() throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException {
		performTest("resource-files/test-input-1.pef", "resource-files/test-input-1_braillo400SR_us_a4_roll.prn");
	}
}