import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.inject.Inject;

import com.google.common.base.Optional;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.daisy.pipeline.junit.AbstractTest;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.ops4j.pax.exam.util.PathUtils;

public class LiblouisTablesTest extends AbstractTest {
	
	@Inject
	public LiblouisTranslator.Provider provider;
	
	@Test
	public void testCompileAllTables() throws IOException {
		File manifest = new File(new File(PathUtils.getBaseDir()), "target/classes/tables");
		for (File f : manifest.listFiles())
			if (f.getName().endsWith(".tbl"))
				assertNotEmpty("Table " + f.getName() + " does not compile", provider.get(query("(table:'" + f.getName() + "')")));
	}
	
	@Test
	public void testQueryTranslator() {
		assertTrue(provider.get(query("(document-locale:nl-BE)(type:literary)")).iterator().next()
		           .asLiblouisTable().asURIs()[1].toString().endsWith("/nl-NL-g0.utb"));
	}
	
	@Test
	public void testUnicodeBraille() {
		assertTrue(provider.get(query("(document-locale:nl-BE)")).iterator().next()
		           .fromStyledTextToBraille()
		           .transform(Optional.of(new CSSStyledText("foobar")).asSet()).iterator().next().getText()
		           .matches("[\\s\\t\\n\u00a0\u00ad\u200b\u2800-\u28ff]*"));
	}
	
	private void assertNotEmpty(String message, Iterable<?> iterable) {
		try {
			iterable.iterator().next(); }
		catch (NoSuchElementException e) {
			throw new AssertionError(message); }
	}
}
