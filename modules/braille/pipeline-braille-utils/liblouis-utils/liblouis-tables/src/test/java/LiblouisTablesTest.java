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
	LiblouisTranslator.Provider provider;
	
	@Test
	public void testCompileAllTablesInManifest() throws IOException {
		File manifest = new File(new File(PathUtils.getBaseDir()), "target/classes/tables/manifest");
		for (File f : manifest.listFiles()) {
			String table = "manifest/" + f.getName();
			assertNotEmpty("Table " + table + " does not compile", provider.get(query("(table:'" + table + "')"))); }
	}
	
	@Test
	public void testQueryTranslator() {
		assertTrue(provider.get(query("(locale:nl_BE)")).iterator().next()
		           .asLiblouisTable().asURIs()[2].toString().endsWith("manifest/nl_BE"));
	}
	
	@Test
	public void testUnicodeBraille() {
		assertTrue(provider.get(query("(locale:nl_BE)")).iterator().next()
		           .fromTypeformedTextToBraille()
		           .transform(new String[]{"foobar"}, new byte[]{LiblouisTranslator.Typeform.PLAIN})[0]
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
