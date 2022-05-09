package org.liblouis;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultTablesTest {
	
	@Test
	public void testTranslateDutch() throws Exception {
		assertEquals(
			"⠨foobar",
			Translator.find("locale:nl grade:0").translate("Foobar", null, null, null).getBraille());
	}
	
	@Test
	public void testCountAvailableLanguages() {
		Set<String> locales = new HashSet<String>();
		for (Table t : Louis.listTables())
			locales.add(t.getInfo().get("language"));
		assertEquals(99, locales.size());
	}
	
	@Test
	public void testCompileAllTables() throws CompilationException {
		int count = 0;
		for (Table t : Louis.listTables()) {
			t.getTranslator();
			count++;
		}
		assertEquals(182, count);
	}
}
