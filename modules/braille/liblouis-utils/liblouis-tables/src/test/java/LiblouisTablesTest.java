import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.ops4j.pax.exam.util.PathUtils;

public class LiblouisTablesTest extends AbstractXSpecAndXProcSpecTest {
	
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
		assertTrue(provider.get(query("(locale:nl-BE)")).iterator().next()
		           .asLiblouisTable().asURIs()[2].toString().endsWith("/nl_BE.tbl"));
	}
	
	@Test
	public void testUnicodeBraille() {
		assertTrue(provider.get(query("(locale:nl-BE)")).iterator().next()
		           .fromTypeformedTextToBraille()
		           .transform(new String[]{"foobar"}, new short[]{LiblouisTranslator.Typeform.PLAIN})[0]
		           .matches("[\\s\\t\\n\u00a0\u00ad\u200b\u2800-\u28ff]*"));
	}
	
	private void assertNotEmpty(String message, Iterable<?> iterable) {
		try {
			iterable.iterator().next(); }
		catch (NoSuchElementException e) {
			throw new AssertionError(message); }
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			brailleModule("liblouis-core"),
			"org.daisy.pipeline.modules.braille:liblouis-native:jar:" + thisPlatform() + ":?",
			brailleModule("libhyphen-core"),
			brailleModule("pef-core"),
			"org.daisy.pipeline:calabash-adapter:?"
		};
	}
}
