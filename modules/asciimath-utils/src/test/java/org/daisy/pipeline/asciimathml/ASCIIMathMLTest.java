package org.daisy.pipeline.asciimathml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ASCIIMathMLTest {
	
	@Test
	public void testALotOfConversions() throws Exception {
		File asciimathmlList = new File(this.getClass().getResource("/big_list.txt").getPath());
		try (BufferedReader br = new BufferedReader(new FileReader(asciimathmlList))) {
			int n = 0;
			String line;
			while ((line = br.readLine()) != null) {
				ASCIIMathML.convert(line);
				n++;
			}
			int expected = 992;
			assertEquals(expected + " lines must be converted", expected, n);
		}
	}
}
