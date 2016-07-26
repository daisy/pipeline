package org.daisy.dotify.formatter.impl;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
public class SPIHelperTest {

	@Test
	public void testCreateBrailleFactory() {
		assertNotNull(SPIHelper.getBrailleTranslatorFactoryMaker());
	}
	
	@Test
	public void testCreateMarkerProcessorFactory() {
		assertNotNull(SPIHelper.getMarkerProcessorFactoryMaker());
	}
	
	@Test
	public void testCreateTextBorderFactory() {
		assertNotNull(SPIHelper.getTextBorderFactoryMaker());
	}
	
	@Test
	public void testCreateInteger2TextFactory() {
		assertNotNull(SPIHelper.getInteger2TextFactoryMaker());
	}
	
	@Test
	public void testCreateExpressionFactory() {
		assertNotNull(SPIHelper.getExpressionFactory());
	}
	
	@Test
	public void testCreateFormatterFactory() {
		assertNotNull(SPIHelper.getFormatterFactory());
	}

}
