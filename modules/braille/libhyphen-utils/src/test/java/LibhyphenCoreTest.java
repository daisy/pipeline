import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Hyphenator.FullHyphenator;
import static org.daisy.pipeline.braille.common.Hyphenator.LineBreaker;
import static org.daisy.pipeline.braille.common.Hyphenator.LineIterator;
import static org.daisy.pipeline.braille.common.Query.util.query;

import org.daisy.pipeline.braille.libhyphen.LibhyphenHyphenator;

import org.daisy.pipeline.junit.AbstractTest;

import static org.daisy.pipeline.pax.exam.Options.thisPlatform;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.options.UrlProvisionOption;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import org.ops4j.pax.exam.util.PathUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibhyphenCoreTest extends AbstractTest {
	
	@Inject
	public LibhyphenHyphenator.Provider provider;
	
	private static final Logger messageBus = LoggerFactory.getLogger("JOB_MESSAGES");
	
	@Test
	public void testStandardHyphenation() {
		FullHyphenator hyphenator= provider.withContext(messageBus)
		                                   .get(query("(table:'standard.dic')"))
		                                   .iterator().next()
		                                   .asFullHyphenator();
		assertEquals("foo\u00ADbar", hyphenator.transform("foobar"));
		assertEquals("foo-\u200Bbar", hyphenator.transform("foo-bar"));
		assertEquals("foo\u00ADbar foob\u00ADar", hyphenator.transform("foobar foob\u00ADar"));
	}
	
	@Test(expected=RuntimeException.class)
	public void testStandardHyphenationException() {
		FullHyphenator hyphenator= provider.withContext(messageBus)
		                                   .get(query("(table:'non-standard.dic')"))
		                                   .iterator().next()
		                                   .asFullHyphenator();
		hyphenator.transform("foobar");
	}
	
	@Test
	public void testNonStandardHyphenation() {
		LineBreaker hyphenator= provider.withContext(messageBus)
		                                .get(query("(table:'non-standard.dic')"))
		                                .iterator().next()
		                                .asLineBreaker();
		assertEquals("f\n" +
		             "oo\n" +
		             "ba\n" +
		             "r",
		             fillLines(hyphenator.transform("foobar"), 2, '-'));
		assertEquals("fu-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foobar"), 3, '-'));
		assertEquals("foo-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foo-bar"), 4, '-'));
		assertEquals("foo-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foo-bar"), 5, '-'));
		assertEquals("foo-\n" +
		             "bar",
		             fillLines(hyphenator.transform("foo-bar"), 6, '-'));
	}
	
	private static String fillLines(LineIterator lines, int width, char hyphen) {
		String s = "";
		while (lines.hasNext()) {
			lines.mark();
			String line = lines.nextLine(width, true);
			if (lines.lineHasHyphen())
				line += hyphen;
			if (line.length() > width) {
				lines.reset();
				line = lines.nextLine(width - 1, true);
				if (lines.lineHasHyphen())
					line += hyphen; }
			s += line;
			if (lines.hasNext())
				s += '\n'; }
		return s;
	}
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"org.daisy.bindings:jhyphen:?",
			brailleModule("braille-common"),
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// needed because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/table-path.xml");
		return probe;
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
