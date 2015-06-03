package org.daisy.braille.embosser;

import static org.junit.Assert.assertEquals;

import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.RollPaperFormat;
import org.daisy.braille.api.paper.SheetPaperFormat;
import org.daisy.braille.api.paper.SheetPaperFormat.Orientation;
import org.daisy.braille.embosser.PrintPage;
import org.daisy.braille.embosser.EmbosserProperties.PrintMode;
import org.daisy.braille.embosser.PrintPage.PrintDirection;
import org.junit.Test;

public class PrintPageTest {

	@Test
	public void testShape() {
		SheetPaperFormat spf = new SheetPaperFormat(
				Length.newCentimeterValue(10), 
				Length.newCentimeterValue(15), 
				Orientation.DEFAULT
			);
		SheetPaperFormat spf2 = new SheetPaperFormat(
				Length.newCentimeterValue(10), 
				Length.newCentimeterValue(15), 
				Orientation.REVERSED
			);
		RollPaperFormat rpf = new RollPaperFormat(
				Length.newCentimeterValue(10), 
				Length.newCentimeterValue(15)
			);
		
		RollPaperFormat rpf2 = new RollPaperFormat(
				Length.newCentimeterValue(15),
				Length.newCentimeterValue(10)
			);
		
		//SHEET
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(spf, PrintDirection.UPRIGHT, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(spf, PrintDirection.UPRIGHT, PrintMode.MAGAZINE).getShape());
		
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(spf, PrintDirection.SIDEWAYS, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(spf, PrintDirection.SIDEWAYS, PrintMode.MAGAZINE).getShape());

		assertEquals("", PrintPage.Shape.LANDSCAPE, new PrintPage(spf2, PrintDirection.UPRIGHT, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(spf2, PrintDirection.UPRIGHT, PrintMode.MAGAZINE).getShape());

		assertEquals("", PrintPage.Shape.LANDSCAPE, new PrintPage(spf2, PrintDirection.SIDEWAYS, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(spf2, PrintDirection.SIDEWAYS, PrintMode.MAGAZINE).getShape());
		
		//ROLL
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(rpf, PrintDirection.UPRIGHT, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(rpf, PrintDirection.UPRIGHT, PrintMode.MAGAZINE).getShape());
		
		assertEquals("", PrintPage.Shape.LANDSCAPE, new PrintPage(rpf, PrintDirection.SIDEWAYS, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(rpf, PrintDirection.SIDEWAYS, PrintMode.MAGAZINE).getShape());
		
		assertEquals("", PrintPage.Shape.LANDSCAPE, new PrintPage(rpf2, PrintDirection.UPRIGHT, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(rpf2, PrintDirection.UPRIGHT, PrintMode.MAGAZINE).getShape());
		
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(rpf2, PrintDirection.SIDEWAYS, PrintMode.REGULAR).getShape());
		assertEquals("", PrintPage.Shape.PORTRAIT, new PrintPage(rpf2, PrintDirection.SIDEWAYS, PrintMode.MAGAZINE).getShape());
		
	}
}
