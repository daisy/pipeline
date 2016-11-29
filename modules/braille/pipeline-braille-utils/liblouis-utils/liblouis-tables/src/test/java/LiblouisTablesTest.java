import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;
import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LiblouisTablesTest {
	
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
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				brailleModule("liblouis-core"),
				brailleModule("liblouis-native").forThisPlatform(),
				brailleModule("libhyphen-core"),
				brailleModule("pef-core"),
				mavenBundle("org.daisy.pipeline:calabash-adapter:?"),
				// logging
				logbackClassic())
		);
	}
}
