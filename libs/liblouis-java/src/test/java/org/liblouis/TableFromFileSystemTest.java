package org.liblouis;

import java.io.File;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.liblouis.Louis.asFile;

public class TableFromFileSystemTest {
	
	@Test
	public void testTableFromFileSystemAbsolutePath() throws Exception {
		Translator translator = new Translator(new File(tablesDir, "foobar.tbl").getAbsolutePath());
		assertEquals(
			"foobar",
			translator.translate("foobar", null, null, null).getBraille());
	}
	
	private final File tablesDir;

	public TableFromFileSystemTest() {
		File testRootDir = asFile(this.getClass().getResource("/"));
		tablesDir = new File(testRootDir, "tables");
	}
}
