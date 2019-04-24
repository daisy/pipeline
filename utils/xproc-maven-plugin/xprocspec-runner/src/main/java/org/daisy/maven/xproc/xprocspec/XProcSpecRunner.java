package org.daisy.maven.xproc.xprocspec;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ServiceLoader;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

import com.google.common.base.Joiner;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableMap;
import static com.google.common.io.Files.asByteSink;

import net.sf.saxon.xpath.XPathFactoryImpl;

import org.daisy.maven.xproc.api.XProcExecutionException;
import org.daisy.maven.xproc.api.XProcEngine;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.xml.sax.InputSource;

@Component(
	name = "org.daisy.maven.xproc.xprocspec.XProcSpecRunner",
	service = { XProcSpecRunner.class }
)
public class XProcSpecRunner {
	
	private XProcEngine engine;
	
	@Reference(
		name = "XProcEngine",
		unbind = "-",
		service = XProcEngine.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setXProcEngine(XProcEngine engine) {
		this.engine = engine;
	}
	
	private static final Map<String,String> XPROCSPEC_NS = new HashMap<String,String>(); {
		XPROCSPEC_NS.put("x", "http://www.daisy.org/ns/xprocspec");
	}
	
	public boolean hasFocus(File testDir) {
		return hasFocus(listXProcSpecFilesRecursively(testDir));
	}
	
	public boolean hasFocus(Collection<File> tests) {
		for (File test : tests)
			if (test.exists())
				if (fileHasFocus(test))
					return true;
		return false;
	}
	
	private boolean fileHasFocus(File test) {
		return (Boolean)evaluateXPath(test,
		                              "exists(//x:scenario[@focus and not(ancestor-or-self::*[@pending])]) or " +
		                              "exists(/x:description[not(@pending) and @focus])",
		                              XPROCSPEC_NS,
		                              Boolean.class);
	}
	
	private boolean fileIsPending(File test) {
		return (Boolean)evaluateXPath(test, "exists(/x:description[@pending])", XPROCSPEC_NS, Boolean.class);
	}
	
	public boolean run(Map<String,File> tests,
	                   File reportsDir,
	                   File surefireReportsDir,
	                   File tempDir,
	                   File catalogFile,
	                   File configFile,
	                   Reporter reporter) {
		
		URL catalog = null;
		if (catalogFile != null)
			try {
				catalog = catalogFile.toURL(); }
			catch (MalformedURLException e) {
				throw new RuntimeException(e); }
		
		// register catalog file that contains http://www.daisy.org/xprocspec/custom-assertion-steps.xpl
		if (engine.getClass().getName().equals("org.daisy.maven.xproc.calabash.Calabash")) {
			
			// if you want to specify a catalog AND use custom-assertion-steps.xpl, you're out of luck
			if (catalog == null)
				try {
					catalog = new URL(
						XProcSpecRunner.class.getResource("/xprocspec-extra/custom-assertion-steps/library.xpl"),
						"../../META-INF/catalog.xml");
				} catch (MalformedURLException e) {
					throw new RuntimeException(e); }
		} else if (engine.getClass().getName().equals("org.daisy.maven.xproc.pipeline.DaisyPipeline2")) {
			// Pipeline module registered through declarative services or SPI
		}
		
		if (catalog != null)
			engine.setCatalog(catalog);
		
		engine.setConfiguration(configFile);
		
		// register Java implementation of px:message
		// FIXME: make a generic step that can be used by all XProc engines
		if (engine.getClass().getName().equals("org.daisy.maven.xproc.calabash.Calabash")) {
			try {
				engine.getClass().getMethod("setDefaultConfiguration", Reader.class).invoke(engine, new StringReader(
				    "<xproc-config xmlns=\"http://xmlcalabash.com/ns/configuration\" xmlns:px=\"http://www.daisy.org/ns/xprocspec\">" +
				    "  <implementation type=\"px:message\" class-name=\"org.daisy.maven.xproc.xprocspec.logging.calabash.impl.MessageStep\"/>" +
				    "</xproc-config>"));
			} catch (NoSuchMethodException e) {
				System.out.println("WARNING: Please use a version of xproc-engine-calabash >= 1.1.0");
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e.getTargetException());
			}
		} else if (engine.getClass().getName().equals("org.daisy.maven.xproc.pipeline.DaisyPipeline2")) {
			
			// org.daisy.maven.xproc.xprocspec.logging.pipeline.impl.MessageStepProvider registered
			// through declarative services or SPI
		}
		
		URI xprocspec = asURI(XProcSpecRunner.class.getResource("/content/xml/xproc/xprocspec.xpl"));
		URI xprocspecSummary = asURI(XProcSpecRunner.class.getResource("/xprocspec-extra/xprocspec-summary.xpl"));
		URL xspecCss = XProcSpecRunner.class.getResource("/xprocspec-extra/xspec.css");
		
		reportsDir.mkdirs();
		surefireReportsDir.mkdirs();
		
		int totalRun = 0;
		int totalFailures = 0;
		int totalErrors = 0;
		int totalSkipped = 0;
		
		Set<String> focusTests = new HashSet<String>(); {
			for (String testName : tests.keySet()) {
				File test = tests.get(testName);
				if (test.exists())
					if (fileHasFocus(test))
						focusTests.add(testName); }}
		Set<String> skipTests = new HashSet<String>(); {
			for (String testName : tests.keySet()) {
				File test = tests.get(testName);
				if ((!focusTests.isEmpty() && !focusTests.contains(testName))
				    || (test.exists() && fileIsPending(test)))
					skipTests.add(testName); }}
		
		long startTime = System.nanoTime();
		
		reporter.init();
		
		for (String testName : tests.keySet()) {
			if (skipTests.contains(testName)) {
				reporter.skipping(testName);
				continue; }
			File test = tests.get(testName);
			File report = new File(reportsDir, testName + ".html");
			File surefireReport = new File(surefireReportsDir, "TEST-" + testName + ".xml");
			Map<String,List<String>> input = ImmutableMap.of("source", Arrays.asList(new String[]{asURI(test).toASCIIString()}));
			Map<String,String> output = ImmutableMap.of("html", asURI(report).toASCIIString(),
			                                            "junit", asURI(surefireReport).toASCIIString());
			Map<String,String> options = ImmutableMap.of("temp-dir", asURI(tempDir) + "/tmp/",
			                                             "enable-log", "false");
			reporter.running(testName, focusTests.contains(testName));
			try {
				engine.run(xprocspec.toASCIIString(), input, output, options, null,
				           ImmutableMap.of("XPROCSPEC_TEST_ID", testName));
				if (!surefireReport.exists()) {
					totalRun += 1;
					totalErrors += 1;
					reporter.result(1, 0, 1, 0, 0L, null, null); }
				else {
					Integer run = (Integer)evaluateXPath(surefireReport, "sum(/testsuites/@tests)", null, Integer.class);
					Integer failures = (Integer)evaluateXPath(surefireReport, "number(/testsuites/@failures)", null, Integer.class);
					Integer errors = (Integer)evaluateXPath(surefireReport, "number(/testsuites/@errors)", null, Integer.class);
					Integer skipped = (Integer)evaluateXPath(surefireReport, "sum(/testsuites/*/number(@skipped))", null, Integer.class);
					Long time = (Long)evaluateXPath(surefireReport, "number(/testsuites/@time)", null, Long.class);
					String shortDesc = (String)evaluateXPath(surefireReport,
					                                         "string-join("
					                                         + "/testsuites/testsuite[testcase[@status='FAILED']]"
					                                         + "/(string(@name),testcase[@status='FAILED']/@name/concat(' - ',.))"
					                                         + ",'\n')",
					                                         null, String.class);
					totalRun += run;
					totalFailures += failures;
					totalErrors += errors;
					totalSkipped += skipped;
					reporter.result(run, failures, errors, skipped, time, shortDesc, null); }}
			catch (XProcExecutionException e) {
				totalRun += 1;
				totalErrors += 1;
				reporter.result(1, 0, 1, 0, 0L, e.getMessage(), Throwables.getStackTraceAsString(e)); }
		}
		
		long totalTime = TimeUnit.SECONDS.convert(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
		reporter.finish(totalRun, totalFailures, totalErrors, totalSkipped, totalTime);
		
		/* Generate summary */
		try {
			String[] testNames = new String[tests.size() - skipTests.size()];
			String[] skipTestNames = new String[skipTests.size()];
			String[] surefireReports = new String[tests.size() - skipTests.size()];
			String[] reports = new String[tests.size() - skipTests.size()];
			File summary = new File(reportsDir, "index.html");
			File css = new File(reportsDir, "xspec.css");
			Joiner joiner = Joiner.on(" ");
			int i = 0;
			int j = 0;
			for (String testName : tests.keySet()) {
				if (skipTests.contains(testName))
					skipTestNames[j++] = testName;
				else {
					testNames[i] = testName;
					File surefireReport = new File(surefireReportsDir, "TEST-" + testName + ".xml");
					surefireReports[i] = asURI(surefireReport).toASCIIString();
					File report = new File(reportsDir, testName + ".html");
					reports[i] = asURI(report).toASCIIString();
					i++; }}
			Map<String,String> output = ImmutableMap.of("result", asURI(summary).toASCIIString());
			Map<String,String> params = ImmutableMap.of("test-names", joiner.join(testNames),
			                                            "skip-test-names", joiner.join(skipTestNames),
			                                            "surefire-reports", joiner.join(surefireReports),
			                                            "reports", joiner.join(reports));
			engine.run(xprocspecSummary.toASCIIString(), null, output, null, ImmutableMap.of("parameters", params));
			asByteSink(css).writeFrom(xspecCss.openStream()); }
		catch (XProcExecutionException e) {
			throw new RuntimeException(e); }
		catch (IOException e) {
			throw new RuntimeException(e); }
		
		return totalErrors == 0 && totalFailures == 0;
	}
	
	public boolean run(Map<String,File> tests,
	                   File reportsDir,
	                   File surefireReportsDir,
	                   File tempDir,
	                   File catalogFile,
	                   Reporter reporter) {
		return run(tests, reportsDir, surefireReportsDir, tempDir, catalogFile, null, reporter);
	}
	
	public boolean run(File testsDir,
	                   File reportsDir,
	                   File surefireReportsDir,
	                   File tempDir,
	                   File configFile,
	                   Reporter reporter) {
		
		Map<String,File> tests = new HashMap<String,File>();
		for (File file : listXProcSpecFilesRecursively(testsDir))
			tests.put(
				file.getAbsolutePath().substring(testsDir.getAbsolutePath().length() + 1)
					.replaceAll("\\.xprocspec$", "")
					.replaceAll("[\\./\\\\]", "_"),
				file);
		File catalogFile = new File(testsDir, "catalog.xml");
		if (!catalogFile.exists()) catalogFile = null;
		return run(tests, reportsDir, surefireReportsDir, tempDir, catalogFile, configFile, reporter);
	}
	
	public boolean run(File testsDir,
	                   File reportsDir,
	                   File surefireReportsDir,
	                   File tempDir,
	                   Reporter reporter) {
		return run(testsDir, reportsDir, surefireReportsDir, tempDir, null, reporter);
	}
	
	public static interface Reporter {
		
		public void init();
		public void running(String name, boolean focus);
		public void skipping(String name);
		public void result(int run, int failures, int errors, int skipped, long time, String shortDesc, String longDesc);
		public void finish(int run, int failures, int errors, int skipped, long time);
		
		public static class DefaultReporter implements Reporter {
			
			private PrintStream stream;
			private String currentTest;
			private List<String> failedTests = new ArrayList<String>();
			private List<String> testsInError = new ArrayList<String>();
			
			public DefaultReporter() {
				this(System.out);
			}
			
			public DefaultReporter(PrintStream stream) {
				this.stream = stream;
			}
			
			private void println(String format, Object... args) {
				if (args.length > 0)
					stream.format(format + "\n", args);
				else
					stream.print(format + "\n");
			}
			
			public void init() {
				println("-------------------------------------------------------");
				println(" X P R O C S P E C   T E S T S");
				println("-------------------------------------------------------");
			}
			
			public void running(String name, boolean focus) {
				println("Running %s", name);
				currentTest = name;
			}
			
			public void skipping(String name) {}
			
			public void result(int run, int failures, int errors, int skipped, long time, String shortDesc, String longDesc) {
				println("Tests run: %d, Failures: %d, Errors: %d, Skipped: %d, Time elapsed: %s sec%s",
				        run, failures, errors, skipped, (new DecimalFormat("0.#")).format(time),
				        (failures > 0 || errors > 0) ? " <<< FAILURE!" : "");
				List<String> summary = errors > 0 ? testsInError : failures > 0 ? failedTests : null;
				if (summary != null) {
					if (shortDesc != null) {
						String formatted = "";
						for (String s : shortDesc.split("\\r?\\n"))
							for (String ss : fillParagraph(s, 86).split("\\r?\\n"))
								formatted += "\n    " + ss;
						summary.add(currentTest + formatted);
					} else
						summary.add(currentTest); }
				if (longDesc != null)
					println(longDesc);
			}
			
			public void finish(int run, int failures, int errors, int skipped, long time) {
				if (run == 0)
					println("There are no tests to run.");
				println("");
				println("Results :");
				println("");
				if (failedTests.size() > 0) {
					println("Failed tests:");
					for (String test : failedTests)
						println("  " + test);
					println(""); }
				if (testsInError.size() > 0) {
					println("Tests in error:");
					for (String test : testsInError)
						println("  " + test);
					println(""); }
				println("Tests run: %d, Failures: %d, Errors: %d, Skipped: %d", run, failures, errors, skipped);
			}
		}
	}
	
	/*
	 * FileUtils.listFiles from Apache Commons IO could be used here as well,
	 * but would introduce another dependency.
	 */
	public static Set<File> listXProcSpecFilesRecursively(File directory) {
		ImmutableSet.Builder<File> builder = new ImmutableSet.Builder<File>();
		if (directory.isDirectory())
			for (File file : directory.listFiles()) {
				if (file.isDirectory())
					builder.addAll(listXProcSpecFilesRecursively(file));
				else if (file.getName().endsWith(".xprocspec"))
					builder.add(file); }
		return builder.build();
	}
	
	public static URI asURI(Object o) {
		try {
			if (o instanceof URI)
				return (URI)o;
			if (o instanceof File)
				return asURI(((File)o).toURI());
			if (o instanceof URL) {
				URL url = (URL)o;
				if (url.getProtocol().equals("jar"))
					return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
				String authority = (url.getPort() != -1) ?
					url.getHost() + ":" + url.getPort() :
					url.getHost();
				return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef()); }}
		catch (Exception e) {}
		throw new RuntimeException("Object can not be converted to URI: " + o);
	}
	
	private static XPath xpath = new XPathFactoryImpl().newXPath();
	
	public static Object evaluateXPath(File file, String expression, final Map<String,String> namespaces, Class<?> type) {
		try {
			if (namespaces != null)
				xpath.setNamespaceContext(
					new NamespaceContext() {
						public String getNamespaceURI(String prefix) {
							return namespaces.get(prefix); }
						public String getPrefix(String namespaceURI) {
							for (String prefix : namespaces.keySet())
								if (namespaces.get(prefix).equals(namespaceURI))
									return prefix;
							return null; }
						public Iterator<String> getPrefixes(String namespaceURI) {
							List<String> prefixes = new ArrayList<String>();
							for (String prefix : namespaces.keySet())
								if (namespaces.get(prefix).equals(namespaceURI))
									prefixes.add(prefix);
							return prefixes.iterator(); }});
			else
				xpath.setNamespaceContext(null);
			XPathExpression expr = xpath.compile(expression);
			InputSource source = new InputSource(file.toURI().toURL().openStream());
			if (type.equals(Boolean.class))
				return expr.evaluate(source, XPathConstants.BOOLEAN);
			if (type.equals(String.class))
				return expr.evaluate(source, XPathConstants.STRING);
			if (type.equals(Integer.class))
				return ((Double)expr.evaluate(source, XPathConstants.NUMBER)).intValue();
			if (type.equals(Long.class))
				return ((Double)expr.evaluate(source, XPathConstants.NUMBER)).longValue();
			else
				throw new RuntimeException("Cannot evaluate to a " + type.getName()); }
		catch (Exception e) {
			throw new RuntimeException("Exception occured during XPath evaluation.", e); }
	}
	
	private static String fillParagraph(String string, int maxColumns) {
		String prefix = "";
		Matcher m = Pattern.compile("^( *-? *)[^ -].*$").matcher(string);
		if (m.matches())
			prefix = m.group(1).replace('-',' ');
		StringBuilder b = new StringBuilder();
		int col = 0;
		boolean first = true;
		for (String word : string.split("\\s+")) {
			while (true) {
				if (col == 0) {
					if (!first)
						b.append(prefix);
					col += prefix.length();
				}
				if (col + word.length() <= maxColumns || col == 0) {
					b.append(word).append(" ");
					col += (word.length() + 1);
					break;
				} else {
					b.append("\n");
					col = 0;
				}
			}
			first = false;
		}
		return b.toString();
	}
}
