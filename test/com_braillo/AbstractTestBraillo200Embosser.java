package com_braillo;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.UnsupportedWidthException;
import org.daisy.braille.facade.PEFConverterFacade;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.pef.PEFHandler.Alignment;
import org.daisy.braille.table.TableCatalog;
import org.daisy.braille.tools.FileCompare;
import org.daisy.braille.tools.Length;
import org.daisy.paper.PaperCatalog;
import org.daisy.paper.RollPaperFormat;
import org.daisy.paper.TractorPaperFormat;
import org.xml.sax.SAXException;

public abstract class AbstractTestBraillo200Embosser {
	final TableCatalog tc;
	final PaperCatalog pc;
	//final PageFormat a4;
	final TractorPaperFormat tractor_210mm_x_12inch;
	final RollPaperFormat roll_a4;
	final Embosser emb;

	public AbstractTestBraillo200Embosser(Embosser emb) {
		this.tc = TableCatalog.newInstance();
		this.pc = PaperCatalog.newInstance();
		//this.a4 = new SheetPaperFormat(pc.get("org_daisy.ISO216PaperProvider.PaperSize.A4").asSheetPaper(), SheetPaperFormat.Orientation.DEFAULT);
		this.tractor_210mm_x_12inch = new TractorPaperFormat(pc.get("org_daisy.TractorPaperProvider.PaperSize.W210MM_X_H12INCH").asTractorPaper());
		this.roll_a4 = new RollPaperFormat(pc.get("org_daisy.RollPaperProvider.PaperSize.W21CM").asRollPaper(), Length.newMillimeterValue(297));
		this.emb = emb;
		
		//emb.setFeature(EmbosserFeatures.PAGE_FORMAT, a4);
		emb.setFeature(EmbosserFeatures.TABLE, tc.get("com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_001_00"));
	}
	
	public void performTest(String resource, String expPath) throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException {
		File tmp = File.createTempFile("BrailloEmbosserTest", ".tmp");
		try {
			EmbosserWriter ew = emb.newEmbosserWriter(new FileOutputStream(tmp));
			PEFHandler.Builder builder = new PEFHandler.Builder(ew);
			builder.align(Alignment.CENTER_INNER);
			PEFConverterFacade.parsePefFile(this.getClass().getResourceAsStream(resource), builder.build());
			FileCompare fc = new FileCompare();
			assertTrue("Assert that the contents of the file is as expected.",
	                fc.compareBinary(new FileInputStream(tmp), this.getClass().getResourceAsStream(expPath))
	        );
		} finally {
			if (!tmp.delete()) {
				tmp.deleteOnExit();
			}
		}
	}
}
