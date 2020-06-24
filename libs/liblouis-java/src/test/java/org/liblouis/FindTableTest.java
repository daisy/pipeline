package org.liblouis;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import static org.liblouis.Louis.asFile;
import static org.liblouis.Louis.asURL;

public class FindTableTest {
	
	@Test(expected=IllegalArgumentException.class)
	public void testInvalidQuery() throws Exception {
		Table.find("locale: foo");
	}
	
	@Test(expected=NoSuchElementException.class)
	public void testNoMatchFound() throws Exception {
		Table.find("locale:fu");
	}
	
	@Test
	public void testMatchFound() throws Exception {
		assertEquals(
			"foobar",
			Translator.find("locale:foo").translate("foobar", null, null, null).getBraille());
	}
	
	@Test
	public void testListAvailableLocales() {
		List<String> locales = new ArrayList<String>();
		for (Table t : Louis.listTables())
			locales.add(t.getInfo().get("locale"));
		assertEquals(1, locales.size());
		assertEquals("foo", locales.get(0));
	}
	
	public FindTableTest() {
		File testRootDir = asFile(this.getClass().getResource("/"));
		final Set<String> tables = new HashSet<String>();
		for (File f : new File(testRootDir, "tables").listFiles())
			tables.add(f.getAbsolutePath());
		Louis.setTableResolver(new TableResolver() {
				public URL resolve(String table, URL base) {
					if (table == null)
						return null;
					if (base != null && base.toString().startsWith("file:")) {
						File f = base.toString().endsWith("/")
							? new File(asFile(base), table)
							: new File(asFile(base).getParentFile(), table);
						if (f.exists())
							return asURL(f);
					} else if (base == null) {
						File f = new File(table);
						if (f.exists())
							return asURL(f);
					}
					return null;
				}
				public Set<String> list() {
					return tables;
				}
			}
		);
	}
}
