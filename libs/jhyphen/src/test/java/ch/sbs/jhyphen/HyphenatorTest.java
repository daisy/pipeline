package ch.sbs.jhyphen;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.util.Collection;

import static org.apache.commons.io.filefilter.FileFilterUtils.asFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HyphenatorTest {
	
	private static File tablesDir; {
		File testRootDir = new File(this.getClass().getResource("/").getPath());
		tablesDir = new File(testRootDir, "tables");
		Hyphen.setLibraryPath(((Collection<File>)FileUtils.listFiles(
				new File(testRootDir, "../dependency"),
				asFileFilter(new FilenameFilter() {
					public boolean accept(File dir, String fileName) {
						return dir.getName().equals("shared") && fileName.startsWith("libhyphen"); }}),
				trueFileFilter())).iterator().next());
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testTableNotFound() throws Exception {
		new Hyphenator(new File(tablesDir, "non-existing.dic"));
	}
	
	@Test(expected=CompilationException.class)
	public void testUnsupportedCharset() throws Exception {
		new Hyphenator(new File(tablesDir, "invalid_charset.dic"));
	}
	
	@Test
	public void testStandardHyphenation() throws Exception {
		Hyphenator hyphenator = new Hyphenator(new File(tablesDir, "standard.dic"));
		try {
			assertEquals("foo=bar", hyphenator.hyphenate("foobar", '=', '|'));
			assertEquals("foo-|bar", hyphenator.hyphenate("foo-bar", '=', '|'));
		} finally {
			hyphenator.close();
		}
	}
	
	@Test(expected=StandardHyphenationException.class)
	public void testStandardHyphenationException() throws Exception {
		Hyphenator hyphenator = new Hyphenator(new File(tablesDir, "non-standard.dic"));
		try {
			hyphenator.hyphenate("foobar", '=', '|');
		} finally {
			hyphenator.close();
		}
	}
	
	@Test
	public void testNonStandardHyphenation() throws Exception {
		Hyphenator hyphenator = new Hyphenator(new File(tablesDir, "non-standard.dic"));
		try {
			assertEquals("fu=bar", hyphenator.hyphenate("foobar", 4, '=', '|'));
			assertEquals("foo-|bar", hyphenator.hyphenate("foo-bar", 4, '=', '|'));
			assertEquals("buss=stopp", hyphenator.hyphenate("busstopp", 6, '=', '|'));
		} finally {
			hyphenator.close();
		}
	}
}
