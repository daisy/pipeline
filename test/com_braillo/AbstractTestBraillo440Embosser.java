package com_braillo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.daisy.braille.api.embosser.Embosser;
import org.daisy.braille.api.embosser.EmbosserFeatures;
import org.daisy.braille.api.embosser.EmbosserWriter;
import org.daisy.braille.api.embosser.UnsupportedWidthException;
import org.daisy.braille.api.paper.Length;
import org.daisy.braille.api.paper.PageFormat;
import org.daisy.braille.api.paper.RollPaperFormat;
import org.daisy.braille.consumer.embosser.EmbosserCatalog;
import org.daisy.braille.consumer.paper.PaperCatalog;
import org.daisy.braille.consumer.table.TableCatalog;
import org.daisy.braille.facade.PEFConverterFacade;
import org.daisy.braille.pef.PEFHandler;
import org.daisy.braille.pef.PEFHandler.Alignment;
import org.daisy.braille.tools.FileCompare;
import org.daisy.printing.FileDevice;
import org.xml.sax.SAXException;

public abstract class AbstractTestBraillo440Embosser {
	final TableCatalog tc;
	final PaperCatalog pc;
	final PageFormat fa44_2p;
	final PageFormat fa44_4p;
	final Embosser emb;

	public AbstractTestBraillo440Embosser(Embosser emb) {
		this.tc = TableCatalog.newInstance();
		this.pc = PaperCatalog.newInstance();
		this.fa44_2p = new RollPaperFormat(pc.get("org_daisy.RollPaperProvider.PaperSize.W33CM").asRollPaper(), Length.newMillimeterValue(261));
		this.fa44_4p = new RollPaperFormat(pc.get("org_daisy.RollPaperProvider.PaperSize.W33CM").asRollPaper(), Length.newMillimeterValue(522));
		this.emb = emb;

		emb.setFeature(EmbosserFeatures.TABLE, tc.get("com_braillo.BrailloTableProvider.TableType.BRAILLO_6DOT_001_00"));
	}

	public void performTest(String resource, String expPath, String expExt, int fileCount) throws IOException, ParserConfigurationException, SAXException, UnsupportedWidthException, TransformerException {
		File tmp = File.createTempFile("BrailloEmbosserTest", ".tmp");
		assertTrue("Verify that test is correctly set up", tmp.delete());
		File dir = new File(tmp.getParentFile(), tmp.getName());
		assertTrue("Verify that test is correctly set up", dir.mkdir());
		FileDevice fd = new FileDevice(dir);
		try {
			EmbosserWriter ew = emb.newEmbosserWriter(fd);
			PEFHandler.Builder builder = new PEFHandler.Builder(ew);
			builder.align(Alignment.CENTER_INNER);
			new PEFConverterFacade(EmbosserCatalog.newInstance()).parsePefFile(this.getClass().getResourceAsStream(resource), builder.build());
			assertEquals("Assert that the number of generated files is correct", fileCount, dir.listFiles().length);
			FileCompare fc = new FileCompare();
			File[] res = dir.listFiles();
			Arrays.sort(res);
			int i = 1;
			for (File v : res) {
				boolean equal = fc.compareBinary(new FileInputStream(v), this.getClass().getResourceAsStream(expPath + i + expExt));
				assertTrue("Assert that the contents of the file is as expected.", equal);
				i++;
				// early clean up
				if (!v.delete()) {
					v.deleteOnExit();
				}
			}
		} finally {
			// clean up again, if an exception occurred
			for (File v : dir.listFiles()) {
				if (!v.delete()) {
					v.deleteOnExit();
				}
			}
			// remove dir
			if (!dir.delete()) {
				dir.deleteOnExit();
			}
		}
	}
}
