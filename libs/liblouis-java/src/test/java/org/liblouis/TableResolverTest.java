package org.liblouis;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.liblouis.Louis.asFile;
import static org.liblouis.Louis.asURL;

public class TableResolverTest {
	
	@Test
	public void testMagicTokenTable() throws Exception {
		Translator translator = new Translator("<FOOBAR>");
		assertEquals(
			"foobar",
			translator.translate("foobar", null, null, null).getBraille());
	}
	
	@Test
	public void testIncludeMagicTokenTable() throws Exception {
		Translator translator = new Translator("tables/include_magic_token");
		assertEquals(
			"foobar",
			translator.translate("foobar", null, null, null).getBraille());
	}
	
	public TableResolverTest() {
		final File testRootDir = asFile(this.getClass().getResource("/"));
		Louis.setTableResolver(new TableResolver() {
				public URL resolve(String table, URL base) {
					if (table == null)
						return null;
					File tableFile = new File(testRootDir, table);
					if (tableFile.exists())
						return asURL(tableFile);
					if (table.equals("<FOOBAR>"))
						return resolve("tables/foobar.cti", null);
					return null;
				}
				public Set<String> list() {
					return Collections.emptySet();
				}
			}
		);
	}
}
