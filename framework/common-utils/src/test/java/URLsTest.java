// in default package because Pax-Exam would otherwise not find class PatternMatcher

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.junit.AbstractTest;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;

import org.ops4j.pax.exam.util.PathUtils;

import org.osgi.framework.FrameworkUtil;

public class URLsTest extends AbstractTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			"com.google.guava:guava:?",
			"org.daisy.libs:saxon-he:?",
			"org.daisy.libs:com.xmlcalabash:?",
			"org.daisy.libs:jing:?",
		};
	}
	
	@Test
	public void testGetResourceFromJAR() throws Exception {
		String baseDir = URLs.asURL(new File(PathUtils.getBaseDir())).toString();
		String testClassesDir = baseDir + "target/test-classes/";
		String classesDir = baseDir + "target/classes/";
		String jarFile; {
			Properties dependencies; {
				dependencies = new Properties();
				dependencies.load(new FileInputStream(new File(new File(PathUtils.getBaseDir()), "target/classes/META-INF/maven/dependencies.properties")));
			}
			String projectArtifactId = dependencies.getProperty("artifactId");
			String projectVersion = dependencies.getProperty("version");
			jarFile = baseDir + "target/" + projectArtifactId + "-" + projectVersion + ".jar";
		}
		{
			// class from target/test-classes
			Class<?> context = URLsTest.class;
			try {
				URLs.getResourceFromJAR("/unexisting", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("resource does not exist", e.getMessage()); }
			try {
				URLs.getResourceFromJAR("/dir/file1/", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("is not a directory", e.getMessage()); }
			{
				String actual = URLs.getResourceFromJAR("/dir/file1", context).toString();
				if (OSGiHelper.inOSGiContext())
					assertThat(actual, matchesPattern("^bundle://.*/dir/file1$"));
				else
					assertEquals(testClassesDir + "dir/file1", actual);
			}
			{
				String actual = URLs.getResourceFromJAR("/dir/subdir", context).toString();
				if (OSGiHelper.inOSGiContext())
					assertThat(actual, matchesPattern("^bundle://.*/dir/subdir/$"));
				else
					assertEquals(testClassesDir + "dir/subdir/", actual);
			}
		}
		{
			// class from jar in target
			Class<?> context = URLs.class;
			try {
				URLs.getResourceFromJAR("/unexisting", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("resource does not exist", e.getMessage()); }
			try {
				URLs.getResourceFromJAR("/org/daisy/common/file/URLs.class/", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("is not a directory", e.getMessage()); }
			{
				String actual = URLs.getResourceFromJAR("/org/daisy/common/file/URLs.class", context).toString();
				if (OSGiHelper.inOSGiContext())
					assertThat(actual, matchesPattern("^bundle://.*/org/daisy/common/file/URLs\\.class$"));
				else
					try {
						assertEquals(classesDir + "org/daisy/common/file/URLs.class", actual); }
					catch (AssertionError e) {
						assertEquals("jar:" + jarFile + "!/org/daisy/common/file/URLs.class", actual); }
			}
			{
				String actual = URLs.getResourceFromJAR("/org/daisy/common/file", context).toString();
				if (OSGiHelper.inOSGiContext())
					assertThat(actual, matchesPattern("^bundle://.*/org/daisy/common/file/$"));
				else
					try {
						assertEquals(classesDir + "org/daisy/common/file/", actual); }
					catch (AssertionError e) {
						assertEquals("jar:" + jarFile + "!/org/daisy/common/file/", actual); }
			}
		}
		{
			// class from jar somewhere in maven repo
			Class<?> context = com.google.common.base.Function.class;
			try {
				URLs.getResourceFromJAR("/unexisting", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("resource does not exist", e.getMessage()); }
			try {
				URLs.getResourceFromJAR("/com/google/common/base/Function.class/", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("is not a directory", e.getMessage()); }
			{
				String actual = URLs.getResourceFromJAR("/com/google/common/base/Function.class", context).toString();
				if (OSGiHelper.inOSGiContext())
					assertThat(actual, matchesPattern("^bundle://.*/com/google/common/base/Function\\.class$"));
				else
					assertThat(actual, matchesPattern("^jar:file:.*!/com/google/common/base/Function\\.class$"));
			}
			{
				String actual = URLs.getResourceFromJAR("/com/google/common/base", context).toString();
				if (OSGiHelper.inOSGiContext())
					assertThat(actual, matchesPattern("^bundle://.*/com/google/common/base/$"));
				else
					assertThat(actual, matchesPattern("^jar:file:.*!/com/google/common/base/$"));
			}
		}
	}
	
	@Test
	public void testListResourcesFromJAR() {
		{
			// class from target/test-classes
			Class<?> context = URLsTest.class;
			try {
				URLs.listResourcesFromJAR("/unexisting", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("directory does not exist", e.getMessage()); }
			try {
				URLs.listResourcesFromJAR("/dir/file1", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("is not a directory", e.getMessage()); }
			{
				Iterator<String> i = sort(URLs.listResourcesFromJAR("/dir", context));
				assertEquals("dir/file1", i.next());
				assertEquals("dir/file2", i.next());
				assertEquals("dir/subdir/", i.next());
				assertFalse(i.hasNext());
			}
		}
		{
			// class from jar in target
			Class<?> context = URLs.class;
			try {
				URLs.listResourcesFromJAR("/unexisting", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("directory does not exist", e.getMessage()); }
			try {
				URLs.listResourcesFromJAR("/org/daisy/common/file/URLs.class", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("is not a directory", e.getMessage()); }
			{
				Iterator<String> i = sort(URLs.listResourcesFromJAR("/org/daisy/common/file", context));
				assertEquals("org/daisy/common/file/URIs.class", i.next());
				assertEquals("org/daisy/common/file/URLs$1.class", i.next());
				assertEquals("org/daisy/common/file/URLs$OSGiHelper.class", i.next());
				assertEquals("org/daisy/common/file/URLs.class", i.next());
				assertFalse(i.hasNext());
			}
		}
		{
			// class from jar somewhere in maven repo
			Class<?> context = com.google.common.base.Function.class;
			try {
				URLs.listResourcesFromJAR("/unexisting", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("directory does not exist", e.getMessage()); }
			try {
				URLs.listResourcesFromJAR("/com/google/common/base/Function.class", context);
				fail("expected RuntimeException"); }
			catch (RuntimeException e) {
				assertEquals("is not a directory", e.getMessage()); }
			{
				Iterator<String> i = sort(URLs.listResourcesFromJAR("/com/google/common", context));
				assertEquals("com/google/common/annotations/", i.next());
				assertEquals("com/google/common/base/", i.next());
				assertEquals("com/google/common/cache/", i.next());
				assertEquals("com/google/common/collect/", i.next());
				assertEquals("com/google/common/escape/", i.next());
				assertEquals("com/google/common/eventbus/", i.next());
				assertEquals("com/google/common/graph/", i.next());
				assertEquals("com/google/common/hash/", i.next());
				assertEquals("com/google/common/html/", i.next());
				assertEquals("com/google/common/io/", i.next());
				assertEquals("com/google/common/math/", i.next());
				assertEquals("com/google/common/net/", i.next());
				assertEquals("com/google/common/primitives/", i.next());
				assertEquals("com/google/common/reflect/", i.next());
				assertEquals("com/google/common/util/", i.next());
				assertEquals("com/google/common/xml/", i.next());
				assertFalse(i.hasNext());
			}
		}
	}
	
	static <T extends Comparable<? super T>> Iterator<T> sort(Iterator<T> iterator) {
		List<T> list = new ArrayList<>();
		while (iterator.hasNext())
			list.add(iterator.next());
		Collections.<T>sort(list);
		return list.iterator();
	}
	
	// copied from org.hamcrest.text (because I can't get hamcrest 2.0.0.0 to work with PaxExam)
	static Matcher<String> matchesPattern(String regex) {
		return new PatternMatcher(regex);
	}

	static class PatternMatcher extends TypeSafeMatcher<String> {
	
		private final Pattern pattern;
	
		public PatternMatcher(String pattern) {
			this.pattern = Pattern.compile(pattern);
		}
	
		public PatternMatcher(Pattern pattern) {
			this.pattern = pattern;
		}
	
		protected boolean matchesSafely(String item) {
			return pattern.matcher(item).matches();
		}
	
		public void describeTo(Description description) {
			description.appendText("a string matching the pattern '" + pattern + "'");
		}
	}
	
	private static abstract class OSGiHelper {
		static boolean inOSGiContext() {
			try {
				return FrameworkUtil.getBundle(OSGiHelper.class) != null;
			} catch (NoClassDefFoundError e) {
				return false;
			}
		}
	}
}
