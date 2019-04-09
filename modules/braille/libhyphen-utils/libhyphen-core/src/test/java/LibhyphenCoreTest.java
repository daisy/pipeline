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

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

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
			brailleModule("common-utils"),
			brailleModule("css-core"),
			"org.daisy.pipeline.modules.braille:libhyphen-native:jar:" + thisPlatform() + ":?"
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("Service-Component", "OSGI-INF/table-path.xml");
		return probe;
	}
}
