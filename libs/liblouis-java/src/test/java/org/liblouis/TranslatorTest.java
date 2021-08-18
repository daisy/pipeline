package org.liblouis;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.liblouis.DisplayTable.StandardDisplayTables;
import static org.liblouis.Louis.asFile;
import static org.liblouis.Utilities.Hyphenation.insertHyphens;

public class TranslatorTest {
	
	@Test
	public void testVersion() {
		assertEquals(
			"3.17.0",
			Louis.getVersion());
	}
	
	@Test(expected=CompilationException.class)
	public void testCompileTable() throws Exception {
		new Translator("unexisting_file");
	}
	
	@Test
	public void testToString() throws Exception {
		String tableFile = new File(tablesDir, "foobar.cti").getCanonicalPath();
		Translator translator = new Translator(tableFile);
		assertEquals(
			"Translator{table=" + tableFile + "}",
			translator.toString());
	}
	
	@Test
	public void testTranslate() throws Exception {
		Translator translator = newTranslator("foobar.cti");
		assertEquals(
			"foobar",
			translator.translate("foobar", null, null, null).getBraille());
	}

	public void testSurrogatePair() throws Exception {
		String text = "\uD835\uDEFC";
		int textLength = text.codePoints().toArray().length;
		int[] interCharAttr = new int[textLength - 1];
		for (int k = 0; k < interCharAttr.length; k++)
			interCharAttr[k] = k;
		int[] charAtts = new int[textLength];
		for (int k = 0; k < charAtts.length; k++)
			charAtts[k] = k;
		Translator translator = newTranslator("foobar.cti");
		assertEquals(
			"⠄⡳⠽⠁⠙⠋⠋⠉⠄", // 1D6FC
			translator.translate(text, null, charAtts, interCharAttr, StandardDisplayTables.UNICODE).getBraille());
	}

	@Test
	public void testBackTranslate() throws Exception {
		Translator translator = newTranslator("foobar.cti");
		assertEquals("foobar", translator.backTranslate("foobar"));
	}
	
	@Test
	public void testHyphenate() throws Exception {
		Translator hyphenator = newTranslator("foobar.cti,foobar.dic");
		assertEquals(
			"foo-bar",
			insertHyphens("foobar", hyphenator.hyphenate("foobar"), '-', null));
	}
	
	@Test
	public void testTranslateAndHyphenate() throws Exception {
		Translator translator = newTranslator("foobar.cti,foobar.dic");
		String text = "foobar";
		TranslationResult result = translator.translate(text, null, null, byteToInt(translator.hyphenate(text)));
		assertEquals(
				"foo-bar",
				insertHyphens(result.getBraille(), intToByte(result.getInterCharacterAttributes()), '-', null));
	}
	
	@Test
	public void testDisplay() throws Exception {
		Translator translator = newTranslator("foobar.cti");
		assertEquals("foobar", translator.display("⠋⠕⠕⠃⠁⠗"));
	}
	
	@Test
	public void testTypeform() throws Exception {
		Translator translator = newTranslator("foobar.cti");
		Typeform t = Typeform.PLAIN_TEXT;
		for (Typeform tt : translator.getSupportedTypeforms())
			t = t.add(tt);
		assertEquals(
			"_/foobar/_",
			translator.translate("foobar", new Typeform[]{t,t,t,t,t,t}, null, null).getBraille());
	}
	
	@Test
	public void testDotsIO() throws Exception {
		Translator translator = newTranslator("foobar.cti");
		assertEquals(
			"⢋⠕⠕⠃⠁⠗",
			translator.translate("Foobar", null, null, null, StandardDisplayTables.UNICODE).getBraille());
	}

	@Test(expected=DisplayException.class)
	public void testDecodingError() throws Exception {
		Translator translator = newTranslator("foobar.cti");
		assertEquals(
			"⠋⠕⠕⠀⠃⠁⠗",
			translator.translate("foo\tbar", null, null, null, StandardDisplayTables.UNICODE).getBraille());
	}
	
	@Test
	public void testDotsIOFallback() throws Exception {
		Translator translator = newTranslator("foobar.cti");
		assertEquals(
			"⠋⠕⠕⠀⠃⠁⠗",
			translator.translate(
				"foo\tbar", null, null, null,
				new DisplayTable.UnicodeBrailleDisplayTable(DisplayTable.Fallback.MASK)
			).getBraille());
	}
	
	private Translator newTranslator(String tables) throws IOException, CompilationException {
		String[] subTables = tables.split(",");
		for (int i = 0; i < subTables.length; i++)
			subTables[i] = new File(tablesDir, subTables[i]).getCanonicalPath();
		tables = subTables[0];
		for (int i = 1; i < subTables.length; i++)
			tables += ("," + subTables[i]);
		return new Translator(tables);
	}
	
	private byte[] intToByte(int [] array) {
		byte[] ret = new byte[array.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (byte)array[i];
		return ret;
	}
	
	private int[] byteToInt(byte [] array) {
		int[] ret = new int[array.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = array[i];
		return ret;
	}
	
	private final File tablesDir;

	public TranslatorTest() {
		File testRootDir = asFile(this.getClass().getResource("/"));
		tablesDir = new File(testRootDir, "tables");
	}
}
