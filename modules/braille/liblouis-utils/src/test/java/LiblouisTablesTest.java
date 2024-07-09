import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import javax.inject.Inject;

import com.google.common.base.Optional;

import static org.daisy.pipeline.braille.common.Query.util.query;
import org.daisy.pipeline.braille.css.CSSStyledText;
import org.daisy.pipeline.braille.liblouis.LiblouisTranslator;

import org.daisy.pipeline.junit.AbstractTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;
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
		           .transform(Optional.of(new CSSStyledText("foobar")).asSet()).iterator().next()
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
			brailleModule("braille-common"),
			brailleModule("braille-css-utils"),
			brailleModule("pef-utils"),
			pipelineModule("file-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("common-utils"),
			"org.liblouis:liblouis-java:?",
			"org.daisy.dotify:dotify.library:?",
			"org.daisy.pipeline:calabash-adapter:?",
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		return options(
			thisBundle(thisPlatform()),
			composite(super.config()));
	}
	
	private static UrlProvisionOption thisBundle(String classifier) {
		File classes = new File(PathUtils.getBaseDir() + "/target/classes");
		Properties dependencies = new Properties(); {
			try {
				dependencies.load(new FileInputStream(new File(classes, "META-INF/maven/dependencies.properties"))); }
			catch (IOException e) {
				throw new RuntimeException(e); }
		}
		String artifactId = dependencies.getProperty("artifactId");
		String version = dependencies.getProperty("version");
		// assuming JAR is named ${artifactId}-${version}.jar
		return bundle("reference:" +
		              new File(PathUtils.getBaseDir() + "/target/" + artifactId + "-" + version + "-" + classifier + ".jar").toURI());
	}
}
