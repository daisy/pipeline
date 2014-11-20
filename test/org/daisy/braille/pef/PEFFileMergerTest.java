package org.daisy.braille.pef;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.daisy.braille.pef.PEFFileMerger.SortType;
import org.daisy.braille.tools.FileTools;
import org.daisy.braille.tools.XMLFileCompare;
import org.daisy.validator.ValidatorFactory;
import org.junit.Test;

public class PEFFileMergerTest {
	
	@Test
	public void testMerger() throws IOException, TransformerException {
		File f = File.createTempFile("MergerTest", "");
		assertTrue("Verify that test is correctly set up", f.delete());
		File dir = new File(f.getParentFile(), f.getName());
		assertTrue("Verify that test is correctly set up", dir.mkdir());
		
		//copy resources to folder
		File f1 = new File(dir, "volume-1");
		File f2 = new File(dir, "volume-2");
		File f3 = new File(dir, "volume-3");
		File output = File.createTempFile("MergerTest", ".tmp");
		try {
			FileTools.copy(this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestInput-1.pef"), new FileOutputStream(f1));
			FileTools.copy(this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestInput-2.pef"), new FileOutputStream(f2));
			FileTools.copy(this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestInput-3.pef"), new FileOutputStream(f3));
			
			PEFFileMerger merger = new PEFFileMerger(ValidatorFactory.newInstance());
			merger.merge(dir, new FileOutputStream(output), "Merged file", SortType.STANDARD);
			XMLFileCompare fc = new XMLFileCompare(TransformerFactory.newInstance());
			assertTrue("Assert that the contents of the file is as expected.", fc.compareXML(
					this.getClass().getResourceAsStream("resource-files/PEFFileMergerTestExpected.pef"), 
					new FileInputStream(output))
				);
		} finally {
			if (!f1.delete()) { f1.deleteOnExit(); }
			if (!f2.delete()) { f2.deleteOnExit(); }
			if (!f3.delete()) { f3.deleteOnExit(); }
			if (!dir.delete()) { dir.deleteOnExit(); }
			if (!output.delete()) {output.deleteOnExit(); }
		}

	}

}
