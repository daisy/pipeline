package org.daisy.pipeline.braille.libhyphen;

import javax.inject.Inject;

import static org.daisy.pipeline.braille.common.Hyphenator.FullHyphenator;
import static org.daisy.pipeline.braille.common.Hyphenator.LineBreaker;
import static org.daisy.pipeline.braille.common.Hyphenator.LineIterator;
import static org.daisy.pipeline.braille.common.Query.util.query;

import static org.daisy.pipeline.pax.exam.Options.brailleModule;
import static org.daisy.pipeline.pax.exam.Options.domTraversalPackage;
import static org.daisy.pipeline.pax.exam.Options.felixDeclarativeServices;
import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.logbackConfigFile;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;
import static org.daisy.pipeline.pax.exam.Options.thisBundle;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.util.PathUtils;

import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LibhyphenCoreTest {
	
	@Inject
	LibhyphenHyphenator.Provider provider;
	
	private static final Logger messageBus = LoggerFactory.getLogger("JOB_MESSAGES");
	
	@Test
	public void testStandardHyphenation() {
		FullHyphenator hyphenator= provider.withContext(messageBus)
		                                   .get(query("(table:'standard.dic')"))
		                                   .iterator().next()
		                                   .asFullHyphenator();
		assertEquals("foo\u00ADbar", hyphenator.transform("foobar"));
		assertEquals("foo-\u200Bbar", hyphenator.transform("foo-bar"));
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
	
	@Configuration
	public Option[] config() {
		return options(
			logbackConfigFile(),
			domTraversalPackage(),
			felixDeclarativeServices(),
			thisBundle(),
			junitBundles(),
			mavenBundlesWithDependencies(
				mavenBundle("org.daisy.bindings:jhyphen:?"),
				brailleModule("common-utils"),
				brailleModule("css-core"),
				brailleModule("libhyphen-native").forThisPlatform(),
				// logging
				logbackClassic()),
			bundle("reference:file:" + PathUtils.getBaseDir() + "/target/test-classes/table_paths/")
		);
	}
}
