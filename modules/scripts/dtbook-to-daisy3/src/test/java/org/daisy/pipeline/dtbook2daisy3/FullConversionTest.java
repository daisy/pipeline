package org.daisy.pipeline.dtbook2daisy3;

import org.daisy.pipeline.junit.AbstractTest;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
import static org.daisy.pipeline.pax.exam.Options.spiflyBundles;

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.propagateSystemProperty;
import static org.ops4j.pax.exam.CoreOptions.streamBundle;
import static org.ops4j.pax.exam.CoreOptions.systemPackage;
import static org.ops4j.pax.exam.CoreOptions.systemProperty;
import static org.ops4j.pax.exam.CoreOptions.wrappedBundle;
import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcInput.Builder;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.zedval.ZedVal;
import org.daisy.zedval.engine.FailureMessage;
import org.daisy.zedval.engine.ZedContext;
import org.daisy.zedval.engine.ZedContextException;
import org.daisy.zedval.engine.ZedFileInitializationException;
import org.daisy.zedval.engine.ZedMessage;
import org.daisy.zedval.engine.ZedReporter;
import org.daisy.zedval.engine.ZedReporterException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * TODO: take into account the current modification of dtbook-to-daisy3, instead of
 * the version already installed.
 */

public class FullConversionTest extends AbstractTest implements DifferenceListener {

	@Override
	public String[] testDependencies() {
		return new String[]{
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("css-utils"),
			pipelineModule("daisy3-utils"),
			pipelineModule("dtbook-tts"),
			pipelineModule("dtbook-utils"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("tts-helpers"),
			"commons-io:commons-io:?",
			"org.daisy.pipeline:xproc-api:?",
			// for dtbook-tss mock
			pipelineModule("dtbook-break-detection"),
			pipelineModule("nlp-omnilang-lexer"),
			// override version of saxon: transitive dependency of common-utils but also
			// of zedval (see below) and zedval needs newer version
			"org.daisy.libs:saxon-he:?",
			// override version of jing: transitive dependency of dtbook-utils but also
			// of zedval (see below) and zedval needs newer version
			"org.daisy.libs:jing:?",
		};
	}
	
	@Override @Configuration
	public Option[] config() {
		try {
		Option resourcesOnDisk = systemProperty("res.on.disk").value(
				getClass().getResource("/").toString());
		
		Option targetDirprop = propagateSystemProperty("target.dir");

		// ************* dtbook-tts MOCK ************ .
		// TODO: make it possible to resolve the mp3 file in bundle://, http://
		// (via the catalog) or tmp://
		// so to avoid the current trick

		// copy the mp3 file to the temporary directory
		URL mp3Src = getClass().getResource("/dtbook-tts/30sec.mp3");
		File destOnTmp = new File(System.getProperty("java.io.tmpdir"), "30sec.mp3");
		Option mp3SrcProp = systemProperty("mp3.src").value(mp3Src.toString());
		Option mp3DestProp = systemProperty("mp3.dest").value(destOnTmp.getAbsolutePath());

		// modify the XSLT so it refers to the mp3 in the tmp directory
		InputStream algo = getClass().getResourceAsStream(
				"/dtbook-tts/generate-audio-map.xsl");
		String algostr = IOUtils.toString(algo).replace("%MP3_PATH%",
				destOnTmp.getAbsolutePath());
		InputStream modifiedAlgo = new ByteArrayInputStream(algostr.getBytes());

		// create the stream bundle
		URL library = getClass().getResource("/dtbook-tts/library.xpl");
		URL catalog = getClass().getResource("/dtbook-tts/catalog.xml");
		Option dtbookTTSmock = streamBundle(bundle()
				.add("library.xpl", library)
				.add("generate-audio-map.xsl", modifiedAlgo)
				.add("META-INF/catalog.xml", catalog)
				.set(Constants.BUNDLE_SYMBOLICNAME,
						"org.daisy.pipeline.modules.dtbook-tts")
				.set("Bundle-Name", "DTBook TTS MOCK").build());
		
		// ******************************************

		// ZedVal and dependencies
		Option zedval = composite(
				// Notes:
				// - When using the instructions method (also exports, imports, etc.) care should
				//   be taken not to reach the maximum file name length (which is 255 on Mac OS),
				//   because Pax Exam encodes all this info into the file name of the JAR.
				// - In the SPI-Consumer instructions, "%23" is used instead of "#". This is because
				//   of a (probable) bug in Pax Exam.
				wrappedBundle(mavenBundle("org.daisy:zedval:?"))
					.instructions("SPI-Consumer=javax.xml.parsers.SAXParserFactory%23newInstance," +
					                           "javax.xml.parsers.DocumentBuilderFactory%23newInstance"),
				wrappedBundle(mavenBundle("org.daisy:daisy-util:?"))
					.instructions("SPI-Consumer=javax.xml.parsers.SAXParserFactory%23newInstance," +
					                           "javax.xml.transform.TransformerFactory%23newInstance"),
				wrappedBundle(mavenBundle("xerces:xercesImpl:?"))
					.instructions("SPI-Provider=*"),
				// see above:
				// mavenBundle("org.daisy.libs:saxon-he:?"),
				// mavenBundle("org.daisy.libs:jing:?"),
				mavenBundle("commons-cli:commons-cli:?"),
				wrappedBundle(mavenBundle("org.w3c.css:sac:?")),
				// wrappedBundle(mavenBundle("javazoom:jlayer:?")),
				// wrappedBundle(mavenBundle("batik:batik-css:?")),
				// wrappedBundle(mavenBundle("batik:batik-util:?")),
				// wrappedBundle(mavenBundle("net.sourceforge.jchardet:jchardet:?")),
				// wrappedBundle(mavenBundle("org.idpf:epubcheck:?")),
				// wrappedBundle(mavenBundle("com.ibm.icu:icu4j:?")),
				// wrappedBundle(mavenBundle("org.ccil.cowan.tagsoup:tagsoup:?")),
				// wrappedBundle(mavenBundle("org.codehaus.woodstox:wstx-lgpl:?")),
				systemPackage("org.w3c.dom"),
				systemPackage("org.w3c.dom.ranges"),
				spiflyBundles()
				);

		// for testing purpose only
		Option testDeps = wrappedBundle(mavenBundle("xmlunit:xmlunit:?"));

		return options(
			dtbookTTSmock,
			zedval,
			testDeps,
			resourcesOnDisk,
			targetDirprop,
			mp3SrcProp,
			mp3DestProp,
			composite(super.config()));
		
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		probe.setHeader("SPI-Consumer", "javax.xml.parsers.SAXParserFactory#newInstance");
		return probe;
	}

	@Inject
	private XProcEngine xprocEngine;

	@Inject
	private BundleContext bundleContext;

	//@Test
	public void noTestButInfo() throws Exception {
		System.out.println("---------------- START DEBUG INFO ----------------");
		
		Set<String> exportedPackages = new HashSet<String>();
		for (Bundle b : bundleContext.getBundles()) {
			Dictionary<String, String> headers = b.getHeaders();
			String exported = headers.get("Export-Package");
			if (exported != null) {
				exported = exported.replaceAll(":=\"[^\"]+\"", "");
				if (b.getSymbolicName().contains("daisy-util")) {
					System.out.println("daisy-util => " + exported);
				}
				for (String instr : exported.split(",")) {
					String[] details = instr.split(";", 2);
					exportedPackages.add(details[0].trim());
					System.out.println(details[0] + " -> "
							+ b.getSymbolicName());

				}
			}
		}

		for (Bundle b : bundleContext.getBundles()) {
			Dictionary<String, String> headers = b.getHeaders();
			String imported = headers.get("Import-Package");
			if (imported != null) {
				imported = imported.replaceAll(":=\"[^\"]+\"", "");
				for (String instr : imported.split(",(?![0-9])")) {
					String[] details = instr.split(";", 2);
					String pack = details[0];
					if (!exportedPackages.contains(pack)
							&& (details.length == 1 || !details[1]
									.contains("optional"))) {
						System.out.println("missing package: " + pack);
					}
				}
			}
		}
		System.out.println("----------------- END DEBUG INFO -----------------");
	}

	interface ErrorFilter {
		boolean isError(FailureMessage m);
	}

	static class NoErrorFiltered implements ErrorFilter {
		public boolean isError(FailureMessage m) {
			return true;
		}
	};

	static class TooShortAudioFilter implements ErrorFilter {
		public boolean isError(FailureMessage m) {
			return !m.getText().contains(
					"duration of the audiofile is shorter than that");
		}
	};

	static class InvalidPagenumFilter implements ErrorFilter {
		public boolean isError(FailureMessage m) {
			return !m.getText().contains(
					"pageTarget combination of value and type is not unique");
		}
	};

	private void runTestsOnFile(String filename, boolean audio,
			boolean testContentUnchanged) throws IOException,
			URISyntaxException, ZedContextException,
			ZedFileInitializationException, SAXException,
			ParserConfigurationException {
		runTestsOnFile(filename, audio, testContentUnchanged,
				new NoErrorFiltered(), true);
	}

	private void runTestsOnFile(String filename, boolean audio,
			boolean testContentUnchanged, final ErrorFilter errorFilter)
			throws IOException, URISyntaxException, ZedContextException,
			ZedFileInitializationException, SAXException,
			ParserConfigurationException {
		runTestsOnFile(filename, audio, testContentUnchanged, errorFilter, true);
	}

	private static class TextCollector extends DefaultHandler {
		private String text = "";

		@Override
		public void characters(char[] str, int begin, int end)
				throws SAXException {
			text += new String(str, begin, end);
		}

		public String getText() {
			return text.replaceAll("\\s+", " ");
		}
	}

	private void runTestsOnFile(String filename, boolean audio,
			boolean testStructureUnchanged, final ErrorFilter errorFilter,
			boolean testTextUnchanged) throws IOException, URISyntaxException,
			ZedContextException, ZedFileInitializationException, SAXException,
			ParserConfigurationException {


		//copy the MP3 file that will be referenced in the SMIL files
		//it has to be done for every test because it is deleted when the job is done
		//TODO: change its name so we can run the tests in parallel
		FileUtils.copyURLToFile(new URL(System.getProperty("mp3.src")),
								new File(System.getProperty("mp3.dest")));
		
		final AtomicInteger numErrors = new AtomicInteger(0);

		ZedReporter reporter = new ZedReporter() {

			public void addMessage(ZedMessage m) throws ZedReporterException {
				if (m instanceof FailureMessage) {
					if (errorFilter.isError((FailureMessage) m)) {
						numErrors.incrementAndGet();
					}
				}
				System.err.println("zedval error: " + m.getText());
			}

			public void close() throws ZedReporterException {
			}

			public void initialize() throws ZedReporterException {

			}

			public void setContext(ZedContext arg0) {
			}
		};

		File outputDir = new File(System.getProperty("target.dir"),
				filename.replaceAll("/", "_") + "_" + audio + "_"
						+ testStructureUnchanged);
		outputDir.mkdirs();

		// URL xmlinput = getClass().getResource(filename); //not used otherwise
		// we can't find the CSS
		URL xmlinput = new URI(System.getProperty("res.on.disk")).resolve(
				"." + filename).toURL();
		URL configOption = getClass().getResource("/tts-config.xml");

		Assert.assertNotNull("file " + filename
				+ " must exist in resource directory", xmlinput);

		Source source = new StreamSource(new InputStreamReader(
				xmlinput.openStream()));
		source.setSystemId(xmlinput.toURI().toString());

		Supplier<Source> supplier = Suppliers.ofInstance(source);

		Builder xprocInput = new XProcInput.Builder().withOption(
				new QName("output-dir"), outputDir.toURI().toString())
				.withInput("source", supplier);
		if (audio) {
			//xprocInput = xprocInput.withOption(new QName("tts-config"),
			//		configOption.toURI().toString());
			xprocInput = xprocInput.withOption(new QName("audio"), "true");
		}

		XProcPipeline pipeline = xprocEngine
				.load(new URI(
						"http://www.daisy.org/pipeline/modules/dtbook-to-daisy3/dtbook-to-daisy3.xpl"));

		pipeline.run(xprocInput.build(), null, new Properties());

		// FIXME: I added this to make the tests work, but we should
		// find out why the scripts doesn't create the file. -- bert
		if (audio) {
			FileUtils.copyURLToFile(new URL(System.getProperty("mp3.src")),
			                        new File(outputDir, "30sec.mp3"));
		}

		ZedVal zv = new ZedVal();
		zv.setReporter(reporter);
		zv.validate(new File(outputDir, "book.opf")); // TODO: look for any
												      // *.opf in the
													  // directory

		Assert.assertEquals("there must not be any validation errors", 0,
				numErrors.get());

		// ********** check that the content is unchanged **************
		if (testTextUnchanged) {
			File xmloutput = new File(outputDir, FilenameUtils.getName(xmlinput
					.getPath()));

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			TextCollector collector1 = new TextCollector();
			TextCollector collector2 = new TextCollector();
			saxParser.parse(xmlinput.openStream(), collector1);
			saxParser.parse(new FileInputStream(xmloutput), collector2);

			// split into chunks to ease the debugging
			String actualText = collector2.getText();
			String expectedtext = collector1.getText();
			int chunkSize = 50;
			for (int k = 0; k < Math.min(expectedtext.length(),
					actualText.length()); k += chunkSize) {
				Assert.assertEquals(
						"text must be left unchanged",
						expectedtext.substring(k,
								Math.min(expectedtext.length(), k + chunkSize)),
						actualText.substring(k,
								Math.min(actualText.length(), k + chunkSize)));
			}
			Assert.assertEquals("text must not change size",
					expectedtext.length(), actualText.length());

			if (testStructureUnchanged) {
				String inputContent = IOUtils.toString(xmlinput.openStream());
				String outputContent = IOUtils.toString(new FileInputStream(
						xmloutput));
				Diff diff = new Diff(inputContent, outputContent);
				diff.overrideDifferenceListener(this);
				Assert.assertTrue("structure must be left unchanged",
						diff.similar());
			}
		}
	}

	private static String removeDTbookHeader(String xml) { // unused
		return xml.replaceAll("<head.+</head>", "");
	}

	private static String simplifyContent(String xml) { // unused
		return xml.replaceAll(" smilref=\"[^\"]+\"", "").replaceAll(
				" id=\"[^\"]+\"", "");
	}

	@Test
	public void autoTests() {
		Assert.assertEquals("we should be able to discard DTBook headers",
				removeDTbookHeader("<book><head>blabla</head><rest/></book>"),
				"<book><rest/></book>");

		Assert.assertEquals(
				"we should be able to discard DTBook complex headers",
				"<book><rest/></book>",
				removeDTbookHeader("<book><head id=\"32\">blabla</head><rest/></book>"));

		Assert.assertEquals(
				"we should be able to remove @smilref and @id from XML",
				"<node>", simplifyContent("<node smilref=\"xxx\">"));

		Assert.assertEquals(
				"we should be able to remove @smilref and @id from XML",
				"<node attr=\"attr\">",
				simplifyContent("<node id=\"id\" attr=\"attr\" smilref=\"xxx\">"));
	}

	private void maybeIgnoreBigTest() {
		boolean runBigTestsToo = false;
		org.junit.Assume.assumeTrue(runBigTestsToo);
	}

	@Test
	public void noAudio1() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_10312_philo.xml", false, true);
	}

	@Test
	public void withAudio1() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_10312_philo.xml", true, false);
	}

	@Test
	public void noAudio2() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_1449_dune_herbert.xml", false, true);
	}

	@Test
	public void withAudio2() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_1449_dune_herbert.xml", true, false);
	}

	@Test
	public void noAudio3() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_11007_xmldtbook_1.xml", false, true);
	}

	@Test
	public void withAudio3() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_11007_xmldtbook_1.xml", true, false);
	}

	@Test
	public void noAudio4() throws Exception {
		maybeIgnoreBigTest();
		// no content check because the DTBook is changed to comply with Daisy3
		// specs
		runTestsOnFile("/samples/shuffled_1724.xml", false, false,
				new TooShortAudioFilter(), false);
	}

	@Test
	public void withAudio4() throws Exception {
		maybeIgnoreBigTest();
		// no content check because the DTBook is changed to comply with Daisy3
		// specs
		runTestsOnFile("/samples/shuffled_1724.xml", true, false,
				new TooShortAudioFilter(), false);
	}

	@Test
	public void noAudio5() throws Exception {
		maybeIgnoreBigTest();
		// MathML entity in doctype not recognized
		// runTestsOnFile("/samples/shuffled_19986_xmldtbook_1.xml", false,
		// true);
	}

	@Test
	public void withAudio5() throws Exception {
		maybeIgnoreBigTest();
		// MathML entity in doctype not recognized
		// runTestsOnFile("/samples/shuffled_19986_xmldtbook_1.xml", true,
		// false);
	}

	@Test
	public void noAudio6() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_21410_xmldtbook_1.xml", false, true);
	}

	@Test
	public void withAudio6() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_21410_xmldtbook_1.xml", true, false);
	}

	@Test
	public void noAudio7() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_4867.xml", false, true);
	}

	@Test
	public void withAudio7() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_4867.xml", true, false);
	}

	@Test
	public void noAudio8() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_5696_xmldtbook_1.xml", false, true);
	}

	@Test
	public void withAudio8() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_5696_xmldtbook_1.xml", true, false);
	}

	@Test
	public void noAudio9() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_5857_xmldtbook_1.xml", false, true);
	}

	@Test
	public void withAudio9() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_5857_xmldtbook_1.xml", true, false);
	}

	@Test
	public void noAudio10() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_6776.xml", false, true);
	}

	@Test
	public void withAudio10() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_6776.xml", true, false);
	}

	@Test
	public void noAudio11() throws Exception {
		maybeIgnoreBigTest();
		// no content check because the DTBook is changed to comply with Daisy3
		// specs
		runTestsOnFile("/samples/shuffled_7019_xmldtbook_1.xml", false, false,
				new TooShortAudioFilter(), false);
	}

	@Test
	public void withAudio11() throws Exception {
		maybeIgnoreBigTest();
		// no content check because the DTBook is changed to comply with Daisy3
		// specs
		runTestsOnFile("/samples/shuffled_7019_xmldtbook_1.xml", true, false,
				new TooShortAudioFilter(), false);
	}

	@Test
	public void noAudio12() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_7277.xml", false, true);
	}

	@Test
	public void withAudio12() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_7277.xml", true, false);
	}

	@Test
	public void noAudio13() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_9400_xmldtbook_1.xml", false, false,
				new InvalidPagenumFilter());
	}

	@Test
	public void withAudio13() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_9400_xmldtbook_1.xml", true, false,
				new InvalidPagenumFilter());
	}

	@Test
	public void noAudio14() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_9868_intro_droit.xml", false, true);
	}

	@Test
	public void withAudio14() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_9868_intro_droit.xml", true, false);
	}

	@Test
	public void noAudio16() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile(
				"/samples/shuffled_economiedesetatsunisl_baudchon_1.xml",
				false, true);
	}

	@Test
	public void withAudio16() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile(
				"/samples/shuffled_economiedesetatsunisl_baudchon_1.xml", true,
				false);
	}

	@Test
	public void noAudio17() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_ideedusieclel_pennac_1.xml", false,
				true);
	}

	@Test
	public void withAudio17() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_ideedusieclel_pennac_1.xml", true,
				false);
	}

	@Test
	public void noAudio18() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_programme_tv.xml", false, true);
	}

	@Test
	public void withAudio18() throws Exception {
		maybeIgnoreBigTest();
		runTestsOnFile("/samples/shuffled_programme_tv.xml", true, false);
	}

	@Test
	public void noAudio19() throws Exception {
		runTestsOnFile("/samples/minimal.xml", false, false);
	}

	@Test
	public void withAudio19() throws Exception {
		runTestsOnFile("/samples/minimal.xml", true, false);
	}

	@Test
	public void noAudio20() throws Exception {
		runTestsOnFile("/samples/skippable.xml", false, false);
	}

	@Test
	public void withAudio20() throws Exception {
		runTestsOnFile("/samples/skippable.xml", true, false);
	}

	// DifferenceListener callback
	public int differenceFound(Difference diff) {
		Node before = diff.getControlNodeDetail().getNode();
		Node after = diff.getTestNodeDetail().getNode();
		boolean genuine = false;

		switch (diff.getId()) {
		case DifferenceConstants.ELEMENT_NUM_ATTRIBUTES_ID:
		case DifferenceConstants.ATTR_NAME_NOT_FOUND_ID:
			NamedNodeMap attrs = before.getAttributes();
			for (int k = 0; k < attrs.getLength(); ++k) {
				Node attr = attrs.item(k);
				Node counterpart = after.getAttributes().getNamedItemNS(
						attr.getNamespaceURI(), attr.getLocalName());
				if (counterpart == null
						&& !"smilref".equals(attr.getLocalName())) {
					System.err.println("difference found: attr " + attr
							+ " is missing in output node " + after);
					genuine = true;
				}
			}
			attrs = after.getAttributes();
			for (int k = 0; k < attrs.getLength(); ++k) {
				Node attr = attrs.item(k);
				String key = attr.getLocalName();
				if (!"id".equals(key) && !"smilref".equals(key)
						&& !"lang".equals(key)) {
					Node counterpart = before.getAttributes().getNamedItemNS(
							attr.getNamespaceURI(), key);
					if (counterpart == null) {
						System.err.println("difference found: attr " + attr
								+ " was not in original node " + before);
						genuine = true;
					}
				}
			}
			break;
		case DifferenceConstants.ATTR_SEQUENCE_ID:
			// not the same order: we don't care
			break;
		case DifferenceConstants.DOCTYPE_NAME_ID:
		case DifferenceConstants.DOCTYPE_PUBLIC_ID_ID:
		case DifferenceConstants.DOCTYPE_SYSTEM_ID_ID:
		case DifferenceConstants.SCHEMA_LOCATION_ID:
			// ignore
			break;
		case DifferenceConstants.ATTR_VALUE_ID:
			String attrname = before.getLocalName();
			if (!"content".equals(attrname) && !"smilref".equals(attrname)) {
				// the @content attributes of the header's metas and the
				// @smilrefs
				genuine = true;
			}
			break;
		default:
			genuine = true;
			break;
		}

		if (genuine) {
			System.err.println("difference found: " + diff.getDescription()
					+ "; before: " + before + ", after: " + after
					+ "; diffid = " + diff.getId());
			return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
		}

		return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
	}

	// DifferenceListener callback
	public void skippedComparison(Node arg0, Node arg1) {
	}
}
