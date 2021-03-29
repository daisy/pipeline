import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import com.google.common.io.CharStreams;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.css.sass.SassCompiler;

import org.junit.Assert;
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
	
	@Test
	public void testCharset() throws Exception {
		doTest("charset.scss", "charset.css");
	}
	
	private static void doTest(String scssPath, String expectedCssPath) throws Exception {
		File scssFile = getTestResource(scssPath);
		File expectedCssFile = getTestResource(expectedCssPath);
		SassCompiler compiler = new SassCompiler(null, null);
		InputStream css = compiler.compile(new FileInputStream(scssFile), URLs.asURL(scssFile), null);
		Assert.assertEquals(new String(Files.readAllBytes(expectedCssFile.toPath()), StandardCharsets.UTF_8),
		                    CharStreams.toString(new InputStreamReader(css)));
	}
	
	private static File getTestResource(String path) {
		return new File(SassTest.class.getResource("/" + path).getPath());
	}
}
