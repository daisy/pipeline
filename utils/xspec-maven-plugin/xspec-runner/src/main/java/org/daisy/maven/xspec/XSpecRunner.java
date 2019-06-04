/**
 * Copyright (C) 2013 The DAISY Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.daisy.maven.xspec;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;

import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "xspec-runner",
	service = { XSpecRunner.class }
)
public class XSpecRunner {

	private final static String XSPEC_NAMESPACE = "http://www.jenitennison.com/xslt/xspec";
	private final static QName XSPEC_MAIN_TEMPLATE = new QName("x",
			XSPEC_NAMESPACE, "main");
	private static final String XSPEC_CSS_NAME = "xspec-report.css";
	private static final QName XSPEC_CSS_URI_PARAM = new QName("report-css-uri");
	private static final XdmValue XSPEC_CSS_URI = new XdmAtomicValue(
			XSPEC_CSS_NAME);
	private static final QName JUNIT_NAME_PARAM = new QName("name");;
	private static final QName JUNIT_TIME_PARAM = new QName("time");;

	private Processor processor;
	private URIResolver defaultResolver;
	private XPathCompiler xpathCompiler;
	private XsltExecutable xspecCompilerLoader;
	private XsltExecutable xspecHtmlFormatterLoader;
	private XsltExecutable xspecHtmlSummaryFormatterLoader;
	private XsltExecutable xspecJUnitFormatterLoader;
	private ByteSource cssSupplier;

	@Reference(
		name = "Processor",
		unbind = "-",
		service = Processor.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public boolean hasFocus(File testDir) {
		return hasFocus(listXSpecFilesRecursively(testDir));
	}

	public boolean hasFocus(Collection<File> tests) {
		for (File test : tests) {
			if (test.exists()) {
				try {
					XdmNode node = new XdmNode(
						processor.getUnderlyingConfiguration().buildDocument(new StreamSource(test)));
					if (((XdmAtomicValue)xpathCompiler.evaluateSingle("exists(//scenario[@focus])", node)).getBooleanValue())
						return true;
				} catch (SaxonApiException e) {
				} catch (XPathException e) {
				}
			}
		}
		return false;
	}
	
	public TestResults run(Map<String, File> tests, File reportDir) {
		Set<String> focusTests = new HashSet<String>(); {
			for (String testName : tests.keySet()) {
				File test = tests.get(testName);
				if (test.exists()) {
					try {
						XdmNode node = new XdmNode(
							processor.getUnderlyingConfiguration().buildDocument(new StreamSource(test)));
						if (((XdmAtomicValue)xpathCompiler.evaluateSingle("exists(//scenario[@focus])", node)).getBooleanValue())
							focusTests.add(testName);
					} catch (SaxonApiException e) {
					} catch (XPathException e) {
					}
				}
			}
		}
		Set<String> skipTests = new HashSet<String>(); {
			if (!focusTests.isEmpty())
				for (String testName : tests.keySet())
					if (!focusTests.contains(testName))
						skipTests.add(testName);
		}
		TestResults.Builder builder = new TestResults.Builder("");
		for (Map.Entry<String, File> test : tests.entrySet()) {
			if (skipTests.contains(test.getKey()))
				continue;
			builder.addSubResults(runSingle(test.getKey(), test.getValue(),
					reportDir));
		}
		writeSummaryReport(focusTests.isEmpty() ? tests.keySet() : focusTests, reportDir);
		return builder.build();
	}
	
	public TestResults run(File testDir, File reportDir) {
		Map<String, File> tests = new HashMap<String, File>();
		for (File file : listXSpecFilesRecursively(testDir))
			tests.put(
				file.getAbsolutePath().substring(testDir.getAbsolutePath().length() + 1)
					.replaceAll("\\.xspec$", "")
					.replaceAll("[\\./\\\\]", "_"),
				file);
		return run(tests, reportDir);
	}

	private TestResults runSingle(String testName, File testFile, File reportDir) {
		// Prepare the reporters
		File textReport = new File(reportDir, "OUT-" + testName + ".txt");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(
					Files.newWriter(textReport, Charsets.UTF_8));
		} catch (FileNotFoundException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		SaxonReporter saxonReporter = new SaxonReporter(writer);

		XdmDestination xspecTestResult = new XdmDestination();
		XdmDestination xspecTestCompiled = new XdmDestination();
		SaxonApiException executionException = null;

		Stopwatch stopwatch = Stopwatch.createStarted();
		report("Running " + testName, writer);

		try {
			// Compile the XSPec test into an executable XSLT
			XsltTransformer xspecCompiler = xspecCompilerLoader.load();
			Source testAsSource = new StreamSource(testFile);
			xspecCompiler.setSource(testAsSource);
			xspecCompiler.setDestination(xspecTestCompiled);
			xspecCompiler.setErrorListener(saxonReporter);
			xspecCompiler.setMessageListener(saxonReporter);
			xspecCompiler.setURIResolver(new XSpecResolver(xspecCompiler
					.getURIResolver()));
			xspecCompiler.transform();

			// Create a new URI resolver if a mock catalog is present
			File catalog = new File(testFile.getParentFile(), "catalog.xml");
			URIResolver testResolver = defaultResolver;
			if (catalog.exists()) {
				CatalogManager catman = new CatalogManager();
				catman.setUseStaticCatalog(false);
				catman.setCatalogFiles(catalog.getPath());
				testResolver = new CatalogResolver(catman);
			}

			// Run the compiled XSpec test
			XsltCompiler xspecTestCompiler = processor.newXsltCompiler();
			xspecTestCompiler.setURIResolver(new XSpecResolver(testResolver));
			processor.getUnderlyingConfiguration().setErrorListener(
					saxonReporter);
			Source compiledTestAsSource = xspecTestCompiled.getXdmNode().asSource();
			XsltTransformer xspecTestRunner = xspecTestCompiler.compile(
					compiledTestAsSource).load();
			xspecTestRunner.setInitialTemplate(XSPEC_MAIN_TEMPLATE);
			xspecTestRunner.setDestination(xspecTestResult);
			xspecTestRunner.setErrorListener(saxonReporter);
			xspecTestRunner.setMessageListener(saxonReporter);
			xspecTestRunner.setURIResolver(new XSpecResolver(testResolver));
			xspecTestRunner.transform();

		} catch (SaxonApiException e) {
			report(e.getMessage(), writer);
			e.printStackTrace(writer);
			executionException = e;
		}

		stopwatch.stop();

		TestResults result = (executionException == null) ? XSpecResultBuilder
				.fromReport(testName, xspecTestResult.getXdmNode(),
						xpathCompiler, stopwatch.toString())
				: XSpecResultBuilder.fromException(testName,
						executionException, stopwatch.toString());

		report(result.toString(), writer);

		writer.close();

		if (result.getErrors() == 0) {
			try {
				// Write XSpec report
				File xspecReport = new File(reportDir, "XSPEC-" + testName
						+ ".xml");
				serializeToFile(xspecReport).serializeNode(xspecTestResult
						.getXdmNode());

				// Write HTML report
				File css = new File(reportDir, XSPEC_CSS_NAME);
				if (!css.exists()) {
					cssSupplier.copyTo(new FileOutputStream(css));
				}
				File htmlReport = new File(reportDir, "HTML-" + testName
						+ ".html");
				XsltTransformer htmlFormatter = xspecHtmlFormatterLoader.load();
				htmlFormatter
						.setSource(xspecTestResult.getXdmNode().asSource());
				htmlFormatter.setParameter(XSPEC_CSS_URI_PARAM, XSPEC_CSS_URI);
				htmlFormatter.setDestination(serializeToFile(htmlReport));
				htmlFormatter.setMessageListener(SaxonSinkReporter.INSTANCE);
				htmlFormatter.transform();

				// Write Surefire report
				File surefireReport = new File(reportDir, "TEST-" + testName
						+ ".xml");
				XsltTransformer junitFormatter = xspecJUnitFormatterLoader
						.load();
				junitFormatter.setSource(xspecTestResult.getXdmNode()
						.asSource());
				junitFormatter.setDestination(serializeToFile(surefireReport));
				junitFormatter.setParameter(JUNIT_NAME_PARAM,
						new XdmAtomicValue(testName));
				junitFormatter.setParameter(
						JUNIT_TIME_PARAM,
						new XdmAtomicValue(stopwatch
								.elapsed(TimeUnit.MILLISECONDS) / 1000d));
				junitFormatter.transform();
			} catch (SaxonApiException e) {
				throw new RuntimeException(e);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return result;
	}

	/*
	 * Write HTML summary report
	 * Assumes XSpec reports were written to <reportDir>/XSPEC-<testName>.xml
	 * and HTML reports were written to <reportDir>/HTML-<testName>.html
	 */
	private void writeSummaryReport(Set<String> testNames, File reportDir) {
		try {
			XsltTransformer formatter = xspecHtmlSummaryFormatterLoader.load();
			formatter.setInitialTemplate(new QName("main"));
			formatter.setParameter(new QName("test-names"), new XdmValue(
					Collections2.<String,XdmItem>transform(
						testNames,
						new Function<String,XdmItem>() {
							public XdmItem apply(String s) {
								return new XdmAtomicValue(s); }})));
			formatter.setParameter(new QName("report-dir"),
					new XdmAtomicValue(reportDir.toURI()));
			formatter.setDestination(
					serializeToFile(new File(reportDir, "index.html")));
			formatter.setMessageListener(SaxonSinkReporter.INSTANCE);
			formatter.transform();
		} catch (SaxonApiException e) {
			throw new RuntimeException(e);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private Serializer serializeToFile(File file) throws FileNotFoundException {
		Serializer serializer = processor.newSerializer();
		serializer.setOutputStream(new FileOutputStream(file));
		serializer.setCloseOnCompletion(true);
		return serializer;
	}

	@Activate
	public void init() {
		try {

		System.setProperty("xml.catalog.ignoreMissing", "true");
		if (processor == null)
			processor = new Processor(false);
		defaultResolver = processor.getUnderlyingConfiguration()
				.getURIResolver();

		XsltCompiler xsltCompiler = processor.newXsltCompiler();
		xsltCompiler.setURIResolver(new XSpecResolver(xsltCompiler
				.getURIResolver()));
		// Initialize the XSpec compiler
		xspecCompilerLoader = xsltCompiler
				.compile(getXSpecSource("/xspec/compiler/generate-xspec-tests.xsl"));

		// Initialize the XSpec report formatter
		xspecHtmlFormatterLoader = xsltCompiler
				.compile(getXSpecSource("/xspec/reporter/format-xspec-report.xsl"));

		// Initialize the XSpec summary formatter
		xspecHtmlSummaryFormatterLoader = xsltCompiler
				.compile(getXSpecSource("/xspec-extra/format-xspec-summary.xsl"));

		// Initialize the JUnit report formatter
		xspecJUnitFormatterLoader = xsltCompiler
				.compile(getXSpecSource("/xspec-extra/format-junit-report.xsl"));

		// Configure the XPath compiler used to parse the XSpec report
		xpathCompiler = processor.newXPathCompiler();
		xpathCompiler.setCaching(true);
		xpathCompiler.declareNamespace("", XSPEC_NAMESPACE);

		// Input supplier for the report CSS
		cssSupplier = Resources.asByteSource(XSpecRunner.class
				.getResource("/xspec/reporter/test-report.css"));

		} catch (SaxonApiException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void report(String message, PrintWriter writer) {
		System.out.println(message);
		writer.println(message);
	}

	private static Source getXSpecSource(String path) {
		return new StreamSource(XSpecRunner.class.getResourceAsStream(path),
				"xspec:" + path);
	}
	
	/*
	 * FileUtils.listFiles from Apache Commons IO could be used here as well,
	 * but would introduce another dependency.
	 */
	public static Set<File> listXSpecFilesRecursively(File directory) {
		ImmutableSet.Builder<File> builder = new ImmutableSet.Builder<File>();
		if (directory.isDirectory())
			for (File file : directory.listFiles()) {
				if (file.isDirectory())
					builder.addAll(listXSpecFilesRecursively(file));
				else if (file.getName().endsWith(".xspec"))
					builder.add(file); }
		return builder.build();
	}

	private static class SaxonReporter implements ErrorListener,
			MessageListener {

		private final PrintWriter writer;

		public SaxonReporter(PrintWriter writer) {
			this.writer = writer;
		}

		public void warning(TransformerException exception)
				throws TransformerException {
			writer.println(exception.getMessage());
		}

		public void error(TransformerException exception)
				throws TransformerException {
			writer.println(exception.getMessage());
		}

		public void fatalError(TransformerException exception)
				throws TransformerException {
			writer.println(exception.getMessage());
		}

		public void message(XdmNode content, boolean terminate,
				SourceLocator locator) {
			writer.println(content);
		}

	}

	private static class SaxonSinkReporter implements ErrorListener,
			MessageListener {

		static SaxonSinkReporter INSTANCE = new SaxonSinkReporter();

		private SaxonSinkReporter() {
		}

		public void warning(TransformerException exception)
				throws TransformerException {
		}

		public void error(TransformerException exception)
				throws TransformerException {
		}

		public void fatalError(TransformerException exception)
				throws TransformerException {
		}

		public void message(XdmNode content, boolean terminate,
				SourceLocator locator) {
		}

	}

	private static class XSpecResolver implements URIResolver {

		private final URIResolver delegate;

		public XSpecResolver(URIResolver delegate) {
			this.delegate = delegate;
		}

		@Override
		public Source resolve(String href, String base)
				throws TransformerException {
			try {
				String uri = new URI(base).resolve(href).toString();
				if (uri.startsWith("xspec:")) {
					InputStream is = XSpecRunner.class.getResourceAsStream(uri
							.substring(6));
					return new StreamSource(is, uri);
				} else if (Pattern.compile("\\.(zip|jar|docx)!/").matcher(uri).find() && uri.startsWith("file:")) {
					Source s = delegate.resolve("jar:" + uri, base);
					if (s != null) return s;
				}
			} catch (URISyntaxException e) {
				// Do nothing
			}
			return delegate.resolve(href, base);
		}

	}
}
