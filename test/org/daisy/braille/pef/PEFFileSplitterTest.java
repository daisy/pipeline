package org.daisy.braille.pef;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.daisy.braille.tools.XMLFileCompare;
import org.daisy.validator.ValidatorFactory;
import org.junit.Test;


public class PEFFileSplitterTest {

	@Test
	public void testSplitter() throws IOException, TransformerException {
		File f = File.createTempFile("SplitterTest", "");
		assertTrue("Verify that test is correctly set up", f.delete());
		File dir = new File(f.getParentFile(), f.getName());
		assertTrue("Verify that test is correctly set up", dir.mkdir());
		PEFFileSplitter splitter = new PEFFileSplitter(ValidatorFactory.newInstance());
		assertTrue("Verify that splitter returns true", splitter.split(
				this.getClass().getResourceAsStream("resource-files/PEFFileSplitterTestInput.pef"),
				dir));
		assertEquals("Assert that the number of generated files is correct", 3, dir.listFiles().length);
		int i = 1;
		//System.out.println(dir);
		XMLFileCompare fc = new XMLFileCompare(TransformerFactory.newInstance(), true);
		File[] res = dir.listFiles();
		Arrays.sort(res);
		for (File v : res) {
			assertTrue("Assert that file " + i + " begins with the string 'volume-'", v.getName().startsWith("volume-"));
			assertTrue("Assert that file " + i + " ends with the string '.pef'", v.getName().endsWith(".pef"));
			boolean equal = fc.compareXML(new FileInputStream(v), this.getClass().getResourceAsStream("resource-files/PEFFileSplitterTestExpected-" + i + ".pef"));
			assertTrue("Assert that contents of file " + i + " is as expected: " + fc.getFileOne() + " vs. " + fc.getFileTwo() + " differs at byte position " + fc.getPos(), equal);
			fc.getFileOne().delete();
			fc.getFileTwo().delete();
			i++;
			// clean up
			if (!v.delete()) {
				v.deleteOnExit();
			}
		}
		// clean up
		if (!dir.delete()) {
			dir.deleteOnExit();
		}
		
	}

}