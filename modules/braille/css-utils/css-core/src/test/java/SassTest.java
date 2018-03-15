import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import io.bit3.jsass.Compiler;
import io.bit3.jsass.Options;
import io.bit3.jsass.OutputStyle;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SassTest {
	
	@Test
	public void testNestedProperties() throws Exception {
		doTest("nested_properties.scss", "nested_properties.css");
	}
	
	@Test
	public void testMisc() throws Exception {
		doTest("misc.scss", "misc.css");
	}
	
	private static void doTest(String scssPath, String expectedCssPath) throws Exception {
		File scssFile = getTestResource(scssPath);
		File expectedCssFile = getTestResource(expectedCssPath);
		Compiler compiler = new Compiler();
		Options options = new Options();
		options.setIsIndentedSyntaxSrc(false);
		options.setOutputStyle(OutputStyle.EXPANDED);
		options.setSourceMapContents(false);
		options.setSourceMapEmbed(false);
		options.setSourceComments(false);
		options.setPrecision(5);
		options.setOmitSourceMapUrl(true);
		String css = compiler.compileString(readFileToString(scssFile), options).getCss();
		assertEquals(readFileToString(expectedCssFile), css);
	}
	
	private static File getTestResource(String path) {
		return new File(SassTest.class.getResource("/" + path).getPath());
	}
	
	private static String readFileToString(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}
}
