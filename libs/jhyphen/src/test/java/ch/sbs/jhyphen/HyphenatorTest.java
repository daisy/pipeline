package ch.sbs.jhyphen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import static org.apache.commons.io.filefilter.FileFilterUtils.asFileFilter;
import static org.apache.commons.io.filefilter.FileFilterUtils.trueFileFilter;
import org.apache.commons.io.FileUtils;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class HyphenatorTest {
	
	private static File tablesDir, textFile; {
		File testRootDir = new File(this.getClass().getResource("/").getPath());
		tablesDir = new File(testRootDir, "tables");
		textFile = new File(testRootDir, "test.txt");
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
			assertEquals("BUSS=STOPP", hyphenator.hyphenate("BUSSTOPP", 6, '=', '|'));
		} finally {
			hyphenator.close();
		}
	}

	/**
	 * Stability test over many runs on a simple text document.
	 * (Introduced after crashes were detected when using JNA 5.14, that were
	 * caused by early garbage collection of pointer.)
	 *
	 * @throws CompilationException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void testManyRunsHyphenation() throws CompilationException, FileNotFoundException, IOException {
		Hyphenator hyphenator = new Hyphenator(new File(tablesDir, "standard.dic"));
		try{
			for(int i = 0; i < 10; ++i){
				BufferedReader testFileReader = new BufferedReader(new FileReader(textFile));
				String line;
				do {
					line = testFileReader.readLine();
					if(line != null){
						hyphenator.hyphenate(line);
					}
				} while(line != null);
			}
		} finally {
			hyphenator.close();
		}
	}
}
