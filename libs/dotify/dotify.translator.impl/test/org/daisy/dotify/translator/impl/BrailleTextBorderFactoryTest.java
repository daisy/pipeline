package org.daisy.dotify.translator.impl;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.daisy.dotify.api.translator.BrailleTranslatorFactory;
import org.daisy.dotify.api.translator.TextBorderConfigurationException;
import org.daisy.dotify.api.translator.TextBorderStyle;
import org.daisy.dotify.translator.impl.BrailleTextBorderFactory;
import org.junit.Test;
@SuppressWarnings("javadoc")
public class BrailleTextBorderFactoryTest {

	@Test
	public void testBorderString_01() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "none");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("", str.get(0));
		assertEquals("⠀", str.get(1));
		assertEquals("", str.get(2));
	}
	@Test
	public void testBorderString_02() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "solid");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("⠏⠉⠹", str.get(0));
		assertEquals("⠇⠀⠸", str.get(1));
		assertEquals("⠧⠤⠼", str.get(2));
	}
	
	@Test
	public void testBorderString_03() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "solid");
		f.setFeature("border-left-style", "none");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("⠉⠹", str.get(0));
		assertEquals("⠀⠸", str.get(1));
		assertEquals("⠤⠼", str.get(2));
	}
	
	@Test
	public void testBorderString_04() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "solid");
		f.setFeature("border-left-width", "2");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("⠿⠉⠹", str.get(0));
		assertEquals("⠿⠀⠸", str.get(1));
		assertEquals("⠿⠤⠼", str.get(2));
	}
	
	@Test
	public void testBorderString_05() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "solid");
		f.setFeature("border-left-width", "2");
		f.setFeature("border-top-width", "3");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("⠿⠿⠿", str.get(0));
		assertEquals("⠿⠀⠸", str.get(1));
		assertEquals("⠿⠤⠼", str.get(2));
	}
	
	@Test
	public void testBorderString_06() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "solid");
		f.setFeature("border-bottom-width", "2");
		f.setFeature("border-right-width", "2");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("⠏⠉⠿", str.get(0));
		assertEquals("⠇⠀⠿", str.get(1));
		assertEquals("⠷⠶⠿", str.get(2));
	}

	@Test
	public void testBorderString_07() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "solid");
		f.setFeature("border-align", "inner");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("⠠⠤⠄", str.get(0));
		assertEquals("⠸⠀⠇", str.get(1));
		assertEquals("⠈⠉⠁", str.get(2));
	}
	
	@Test
	public void testBorderString_08() throws TextBorderConfigurationException {
		BrailleTextBorderFactory f = new BrailleTextBorderFactory();
		f.setFeature("mode", BrailleTranslatorFactory.MODE_UNCONTRACTED);
		f.setFeature("border-style", "solid");
		f.setFeature("border-align", "inner");
		f.setFeature("border-width", "2");
		List<String> str = styleToStrings(f.newTextBorderStyle()); 
		assertEquals("⠶⠶⠶", str.get(0));
		assertEquals("⠿⠀⠿", str.get(1));
		assertEquals("⠛⠛⠛", str.get(2));
	}
	
	private List<String> styleToStrings(TextBorderStyle s) {
		ArrayList<String> ret = new ArrayList<>();
		ret.add(s.getTopLeftCorner()+s.getTopBorder()+s.getTopRightCorner());
		ret.add(s.getLeftBorder()+"⠀"+s.getRightBorder());
		ret.add(s.getBottomLeftCorner()+s.getBottomBorder()+s.getBottomRightCorner());
		return ret;
	}
}
