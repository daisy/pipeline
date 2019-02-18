package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.models.Alive;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Argument.Kind;
import org.daisy.pipeline.client.models.DataType;
import org.daisy.pipeline.client.models.Job.Priority;
import org.daisy.pipeline.client.models.Job.Status;
import org.daisy.pipeline.client.models.Message;
import org.daisy.pipeline.client.models.Message.Level;
import org.daisy.pipeline.client.models.Result;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.models.datatypes.EnumType;
import org.daisy.pipeline.client.models.datatypes.RegexType;
import org.daisy.pipeline.client.utils.NamespaceContextMap;
import org.daisy.pipeline.client.utils.XML;
import org.junit.Assert;
import org.junit.Test;
import static org.hamcrest.CoreMatchers.*;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ParsingTest {

	private File resources = new File("src/test/resources/");
	private String loadResource(String href) {
		File scriptXmlFile = new File(resources, href);
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(scriptXmlFile.getPath()));
			return new String(encoded, Charset.defaultCharset());
		} catch (IOException e) {
			assertTrue("Failed to read "+scriptXmlFile.getPath(), false);
			return null;
		}
	}
	private Document loadResourceXml(String href) {
		String xmlString = loadResource(href);
		return XML.getXml(xmlString);
	}

	@Test
	public void getScripts() {
		try {

			List<Script> scripts = Script.parseScriptsXml(loadResourceXml("responses/scripts.xml"));
			if (scripts.size() == 0)
				fail("no scripts in response");
			if (scripts.get(0).getId() == null || scripts.get(0).getId().length() == 0)
				fail("empty script id");
			if (scripts.get(0).getNicename() == null || scripts.get(0).getNicename().length() == 0)
				fail("empty nicename id");
			if (scripts.get(0).getDescription() == null || scripts.get(0).getDescription().length() == 0)
				fail("empty script description");

			String[] orderedScripts = new String[]{"daisy202-to-epub3", "dtbook-to-epub3", "dtbook-to-zedai", "zedai-to-epub3", "zedai-to-pef"};
			for (int s = 0; s < orderedScripts.length; s++) {
				if (!orderedScripts[s].equals(scripts.get(s).getId()))
					fail("scripts list must be ordered alphabetically by id ("+scripts.get(s).getId()+" at position "+s+")");
			}

			assertNotNull(scripts.get(0).getId());

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseAliveResponse() {
		try {
			Alive alive = new Alive(loadResourceXml("responses/alive.xml"));

			assertNotNull(alive);
			assertEquals(false, alive.authentication);
			assertEquals(true, alive.localfs);
			assertEquals(false, alive.error);
			assertEquals("1.6", alive.version);

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testParseAndSerializeJobXml() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job1.xml"));
			responsesJobsJob1ParseTestHelper(job); // correctly loaded from disk
			
			job = new Job(job.toXml());
			responsesJobsJob1ParseTestHelper(job); // correctly serialized as XML

			job = new Job(loadResourceXml("responses/jobs/job2.xml"));

			List<Message> messages = job.getMessages();
			Result result = job.getResult();
			Result outputDirResult = job.getResult("output-dir");
			List<Result> results = job.getResults().get(outputDirResult);

			assertEquals(36, messages.size());
			assertNotNull(result);
			assertEquals("results.zip", result.filename);
			assertEquals("result", result.prettyRelativeHref);
			assertEquals("", result.relativeHref);
			assertEquals("output-dir.zip", outputDirResult.filename);
			assertEquals("ValentinHaüythefatheroftheeducationfortheblind.epub", results.get(0).prettyRelativeHref);
			assertEquals("option/output-dir/idx/output-dir/ValentinHaüythefatheroftheeducationfortheblind.epub", results.get(0).relativeHref);
			assertEquals("mimetype", results.get(25).filename);
			assertEquals("epub/mimetype", results.get(25).prettyRelativeHref);
			assertEquals("option/output-dir/idx/output-dir/epub/mimetype", results.get(25).relativeHref);
			assertEquals("http://localhost:8181/ws/jobs/313ded3e-37b7-4de6-831c-05f8639326f5/result/option/output-dir/idx/output-dir/epub/mimetype", results.get(25).href);
			assertEquals("OpenDyslexic-Regular.otf", results.get(19).filename);
			assertEquals("epub/EPUB/Content/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", results.get(19).prettyRelativeHref);
			assertEquals("option/output-dir/idx/output-dir/epub/EPUB/Content/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", results.get(19).relativeHref);

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	private void responsesJobsJob1ParseTestHelper(Job job) throws Pipeline2Exception {
		assertNotNull(job);
		assertEquals("job1", job.getId());
		assertEquals("http://localhost:8181/ws/jobs/job1", job.getHref());
		assertEquals(Status.SUCCESS, job.getStatus());
		assertNotNull(job.getScript());
		assertEquals("http://localhost:8181/ws/scripts/dtbook-to-zedai", job.getScript().getHref());
		assertEquals("dtbook-to-zedai", job.getScript().getId());
		assertEquals("DTBook to ZedAI", job.getScript().getNicename());
		assertEquals("Transforms DTBook XML into ZedAI XML.", job.getScript().getDescription());
		//			assertNotNull(job.messagesNode);
		//			assertNotNull(job.resultsNode);
		assertEquals("http://localhost:8181/ws/jobs/job1/log", job.getLogHref());

		List<Message> messages = job.getMessages();
		Result result = job.getResult();

		assertEquals(62, messages.size());

		assertNull(result.from);
		assertNull(result.file);
		assertEquals("application/zip", result.mimeType);
		assertEquals(new Long(178080), result.size);
		assertNull(result.name);
		assertEquals("http://localhost:8181/ws/jobs/job1/result", result.href);
		assertEquals("result", result.prettyRelativeHref);
		assertEquals("", result.relativeHref);
		assertEquals("results.zip", result.filename);

		SortedMap<Result, List<Result>> allResults = job.getResults();
		assertEquals(1, allResults.size());

		result = job.getResult("output-dir");
		List<Result> results = job.getResults("output-dir");
		assertEquals("option", result.from);
		assertEquals(null, result.file);
		assertEquals("application/zip", result.mimeType);
		assertEquals(new Long(178080), result.size);
		assertEquals("output-dir", result.name);
		assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir", result.href);
		assertEquals("option/output-dir", result.prettyRelativeHref);
		assertEquals("output-dir.zip", result.filename);
		assertEquals(4, results.size());

		assertEquals(null, results.get(1).from);
		assertEquals("/responses/jobs/job1/result/option/output-dir/idx/output-dir/valentin.jpg", results.get(1).file);
		assertEquals(null, results.get(1).mimeType);
		assertEquals(new Long(25740), results.get(1).size);
		assertEquals(null, results.get(1).name);
		assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output-dir/valentin.jpg", results.get(1).href);
		assertEquals("valentin.jpg", results.get(1).prettyRelativeHref);
		assertEquals("valentin.jpg", results.get(1).filename);

		assertEquals(null, results.get(2).from);
		assertEquals("/responses/jobs/job1/result/option/output-dir/idx/output-dir/zedai-mods.xml", results.get(2).file);
		assertEquals(null, results.get(2).mimeType);
		assertEquals(new Long(442), results.get(2).size);
		assertEquals(null, results.get(2).name);
		assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output-dir/zedai-mods.xml", results.get(2).href);
		assertEquals("zedai-mods.xml", results.get(2).prettyRelativeHref);
		assertEquals("zedai-mods.xml", results.get(2).filename);

		assertEquals(null, results.get(3).from);
		assertEquals("/responses/jobs/job1/result/option/output-dir/idx/output-dir/zedai.xml", results.get(3).file);
		assertEquals(null, results.get(3).mimeType);
		assertEquals(new Long(151891), results.get(3).size);
		assertEquals(null, results.get(3).name);
		assertEquals("http://localhost:8181/ws/jobs/job1/result/option/output-dir/idx/output-dir/zedai.xml", results.get(3).href);
		assertEquals("zedai.xml", results.get(3).prettyRelativeHref);
		assertEquals("zedai.xml", results.get(3).filename);
	}
	
	@Test
	public void testParseScriptArguments() {
		try {
			Script script = new Script(loadResourceXml("scripts/dtbook-to-zedai.xml"));
			assertNotNull(script);
			assertEquals("http://localhost:8181/ws/scripts/dtbook-to-zedai", script.getHref());
			assertNotNull(script.getArgument("assert-valid"));
			assertEquals("false", script.getArgument("assert-valid").getDefaultValue());
			assertEquals(Boolean.FALSE, script.getArgument("assert-valid").getDefaultValueAsBoolean());
			
		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseJobNotFound() {
		try {
			String jobXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
							+"<error query=\"http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.4.5\" "
							+"xmlns=\"http://www.daisy.org/ns/pipeline/data\">"
							+"<description>Job not found</description>"
							+"<trace>java.lang.Throwable: Job not found\n"
							+"    at org.daisy.pipeline.webservice.impl.GenericResource.getErrorRepresentation(GenericResource.java:36)\n"
							+"    at org.daisy.pipeline.webservice.impl.JobResource.getResource(JobResource.java:78)\n"
							+"    at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
							+"    …\n"
							+"    at java.base/java.lang.Thread.run(Thread.java:844)\n"
							+"</trace></error>";
			Job job = new Job(XML.getXml(jobXml));
			assertNotNull(job);
			
		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseJobRequest() {
		try {
			Job job = new Job(loadResourceXml("responses/jobRequest.xml"));

			assertNotNull(job);

			for (Argument arg : job.getArguments()) {
				assertNotNull(arg.getName());
				assertNotNull(arg.getNicename());
				assertNotNull(arg.getDesc());
				assertNotNull(arg.getRequired());
				assertNotNull(arg.getSequence());
				assertNotNull(arg.getOrdered());
				assertNotNull(arg.getMediaTypes());
				assertNotNull(arg.getType());
				assertNotNull(arg.getKind());

				if ("source".equals(arg.getName())) {
					assertEquals(Kind.input, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("dtbook-basic.xml", arg.get());
				}
				else if ("mods-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-mods-file.xml", arg.get());
				}
				else if ("css-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-css-file.css", arg.get());
				}
				else if ("zedai-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-zedai-file.xml", arg.get());
				}
				else fail("Unknown argument: "+arg.getName());
			}

			Document jobRequest = job.toJobRequestXml(false);

			Job reparsedJob = new Job(jobRequest);
			for (Argument arg : reparsedJob.getArguments()) {
				assertNotNull(arg.getName());
				assertNotNull(arg.getNicename());
				assertNotNull(arg.getDesc());
				assertNotNull(arg.getRequired());
				assertNotNull(arg.getSequence());
				assertNotNull(arg.getOrdered());
				assertNotNull(arg.getMediaTypes());
				assertNotNull(arg.getType());
				assertNotNull(arg.getKind());

				if ("source".equals(arg.getName())) {
					assertEquals(Kind.input, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("dtbook-basic.xml", arg.get());
				}
				else if ("mods-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-mods-file.xml", arg.get());
				}
				else if ("css-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-css-file.css", arg.get());
				}
				else if ("zedai-filename".equals(arg.getName())) {
					assertEquals(Kind.option, arg.getKind());
					assertEquals(1, arg.size());
					assertEquals("the-zedai-file.xml", arg.get());
				}
				else fail("Unknown argument: "+arg.getName());
			}

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testOutputPorts() {
		try {
			Script script = new Script(loadResourceXml("responses/scripts/dtbook-validator.xml"));

			assertNotNull(script);
			
			assertEquals(1, script.getInputFilesets().size());
			assertEquals(0, script.getOutputFilesets().size());
			assertEquals("dtbook", script.getInputFilesets().get(0));

			for (Argument arg : script.getArguments()) {
				assertNotNull(arg.getName());
				assertNotNull(arg.getNicename());
				assertNotNull(arg.getDesc());
				assertNotNull(arg.getRequired());
				assertNotNull(arg.getSequence());
				assertNotNull(arg.getOrdered());
				assertNotNull(arg.getMediaTypes());
				assertNotNull(arg.getType());
				assertNotNull(arg.getKind());

				assertEquals(arg.getName()+" has a decription", true, arg.getDesc().length() > 0);
				assertEquals(arg.getName()+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));

				if ("result".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(true, arg.getRequired());
					assertEquals(false, arg.getSequence());
				}
				else if ("schematron-report".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(true, arg.getRequired());
					assertEquals(false, arg.getSequence());
				}
				else if ("relaxng-report".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(false, arg.getRequired());
					assertEquals(true, arg.getSequence());
				}
				else if ("html-report".equals(arg.getName())) {
					assertEquals(Kind.output, arg.getKind());
					assertEquals(true, arg.getOrdered());
					assertEquals(true, arg.getRequired());
					assertEquals(false, arg.getSequence());
				}
			}

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

	@Test
	public void testGetResults() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job1.xml"));
			assertNotNull(job);

			Result epubResult = job.getResult("output-dir", "F00000 - Don't Worry, Be Happy Lyrics.epub.xml");
			assertNotNull(epubResult);

			// test XML does not contain absolute file URLs, so we prepend the absolute path here
			epubResult.file = resources.toURI().toString()+epubResult.file;

			File file = epubResult.asFile();
			assertNotNull(file);

			String text = epubResult.asText();
			assertNotNull(text);

			Document xml = epubResult.asXml();
			assertNotNull(xml);

			assertTrue(file.exists());
			assertEquals("F00000 - Don't Worry, Be Happy Lyrics.epub.xml", file.getName());
			assertEquals("<TEST/>\n", text);
			assertEquals("TEST", xml.getDocumentElement().getLocalName());

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testParseDataTypes() {
			Map<String, String> dataTypes = DataType.getDataTypes(loadResourceXml("responses/datatypes.xml"));
			assertNotNull(dataTypes);
			
			assertEquals(2, dataTypes.size());
			assertEquals("http://localhost:8181/ws/datatypes/braille-translator", dataTypes.get("braille-translator"));
			assertEquals("http://localhost:8181/ws/datatypes/test-regex", dataTypes.get("test-regex"));
			
			DataType testRegexDataType = DataType.getDataType(loadResourceXml("responses/datatypes/test-regex.xml"));
			assertEquals("test-regex", testRegexDataType.id);
			assertTrue("test-regex is regex", testRegexDataType instanceof RegexType);
			RegexType testRegex = (RegexType)testRegexDataType;
			assertEquals("\\d", testRegex.regex);
			assertEquals("Documentation in english.", testRegex.getDescription());
			assertEquals("Documentation in english.", testRegex.descriptions.get("en"));
			assertEquals("Dokumentasjon på norsk.\n\nFlere linjer.\n\nEnda flere linjer.", testRegex.descriptions.get("no"));
			assertFalse(testRegex.pattern.matcher("").matches());
			assertTrue(testRegex.pattern.matcher("1").matches());
			assertFalse(testRegex.pattern.matcher("a").matches());
			assertFalse(testRegex.pattern.matcher("123").matches());
			DataType brailleTranslatorDataType = DataType.getDataType(loadResourceXml("responses/datatypes/braille-translator.xml"));
			assertEquals("braille-translator", brailleTranslatorDataType.id);
			assertTrue("braille-translator is enum", brailleTranslatorDataType instanceof EnumType);
			EnumType brailleTranslator = (EnumType)brailleTranslatorDataType;
			assertNotNull(brailleTranslator.values);
			assertEquals(7, brailleTranslator.values.size());
			assertEquals("celia", brailleTranslator.values.get(0).name);
			assertEquals("dedicon", brailleTranslator.values.get(1).name);
			assertEquals("mtm", brailleTranslator.values.get(2).name);
			assertEquals("nlb", brailleTranslator.values.get(3).name);
			assertEquals("nota", brailleTranslator.values.get(4).name);
			assertEquals("sbs", brailleTranslator.values.get(5).name);
			assertEquals("value-with-no-nicename-or-description", brailleTranslator.values.get(6).name);
			assertEquals("Celia", brailleTranslator.values.get(0).getNicename());
			assertEquals("Dedicon", brailleTranslator.values.get(1).getNicename());
			assertEquals("MTM", brailleTranslator.values.get(2).getNicename());
			assertEquals("NLB", brailleTranslator.values.get(3).getNicename());
			assertEquals("Nota", brailleTranslator.values.get(4).getNicename());
			assertEquals("SBS", brailleTranslator.values.get(5).getNicename());
			assertEquals("value-with-no-nicename-or-description", brailleTranslator.values.get(6).getNicename());
			assertEquals("Celia", brailleTranslator.values.get(0).nicenames.get("en"));
			assertEquals("Dedicon", brailleTranslator.values.get(1).nicenames.get("en"));
			assertEquals("MTM", brailleTranslator.values.get(2).nicenames.get("en"));
			assertEquals("NLB", brailleTranslator.values.get(3).nicenames.get("en"));
			assertEquals("Nota", brailleTranslator.values.get(4).nicenames.get("en"));
			assertEquals("SBS", brailleTranslator.values.get(5).nicenames.get("en"));
			assertEquals(null, brailleTranslator.values.get(6).nicenames.get("en"));
			assertEquals("西莉亞", brailleTranslator.values.get(0).nicenames.get("ch"));
			assertEquals("我奉獻 (defined at top)", brailleTranslator.values.get(1).nicenames.get("ch"));
			assertEquals("中號的", brailleTranslator.values.get(2).nicenames.get("ch"));
			assertEquals("立升", brailleTranslator.values.get(3).nicenames.get("ch"));
			assertEquals("諾塔", brailleTranslator.values.get(4).nicenames.get("ch"));
			assertEquals("小號", brailleTranslator.values.get(5).nicenames.get("ch"));
			assertEquals(null, brailleTranslator.values.get(6).nicenames.get("ch"));
			assertEquals("", brailleTranslator.values.get(0).getDescription());
			assertEquals("", brailleTranslator.values.get(1).getDescription());
			assertEquals("", brailleTranslator.values.get(2).getDescription());
			assertEquals("", brailleTranslator.values.get(3).getDescription());
			assertEquals("", brailleTranslator.values.get(4).getDescription());
			assertEquals("The Swiss library for the blind.\n\n**Markdown allowed!**", brailleTranslator.values.get(5).getDescription());
			assertEquals("", brailleTranslator.values.get(6).getDescription());
			
			DataType genericDataType = DataType.getDataType(loadResourceXml("responses/scripts/dtbook-to-epub3.xml"));
			assertEquals("dtbook-to-epub3", genericDataType.id);
			assertTrue("should be generic datatype (i.e. unrecognized XML grammar)", genericDataType.getClass().equals(DataType.class));
	}

	@Test
	public void testXPath() {
		XPathFactory factory = XPathFactory.newInstance();
		javax.xml.xpath.XPath xpath = factory.newXPath();
		NamespaceContext context = new NamespaceContextMap(
				"foo", "http://foo", 
				"bar", "http://bar");

		xpath.setNamespaceContext(context);


		Document xml = XML.getXml("<foo:data xmlns:foo='http://foo' xmlns:bar='http://bar'><bar:foo bar=\"hello\" /></foo:data>");
		try {
			XPathExpression compiled = xpath.compile("/foo:data/bar:foo/attribute::bar");
			NodeList nodeList = (NodeList)compiled.evaluate(xml, XPathConstants.NODESET);
			assertEquals("bar=\"hello\"", nodeList.item(0).toString());
		} catch (XPathExpressionException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testXPathContext() {
		Map<String, String> mappings = new HashMap<String, String>();
		mappings.put("foo", "http://foo");
		mappings.put("altfoo", "http://foo");
		mappings.put("bar", "http://bar");
		mappings.put(XMLConstants.XML_NS_PREFIX,XMLConstants.XML_NS_URI);

		NamespaceContext context = new NamespaceContextMap(mappings);
		for (Map.Entry<String, String> entry : mappings.entrySet()) {
			String prefix = entry.getKey();
			String namespaceURI = entry.getValue();

			Assert.assertEquals("namespaceURI", namespaceURI, context.getNamespaceURI(prefix));
			boolean found = false;
			Iterator<?> prefixes = context.getPrefixes(namespaceURI);
			while (prefixes.hasNext()) {
				if (prefix.equals(prefixes.next())) {
					found = true;
					break;
				}
				try {
					prefixes.remove();
					Assert.fail("rw");
				} catch (UnsupportedOperationException e) {
				}
			}
			Assert.assertTrue("prefix: " + prefix, found);
			Assert.assertNotNull("prefix: " + prefix, context.getPrefix(namespaceURI));
		}

		Map<String, String> ctxtMap = ((NamespaceContextMap) context).getMap();
		for (Map.Entry<String, String> entry : mappings.entrySet()) {
			Assert.assertEquals(entry.getValue(), ctxtMap.get(entry.getKey()));
		}
	}

	@Test
	public void testXPathModify() {
		NamespaceContextMap context = new NamespaceContextMap();

		try {
			Map<String, String> ctxtMap = context.getMap();
			ctxtMap.put("a", "b");
			Assert.fail("rw");
		} catch (UnsupportedOperationException e) {
		}

		try {
			Iterator<String> it = context.getPrefixes(XMLConstants.XML_NS_URI);
			it.next();
			it.remove();
			Assert.fail("rw");
		} catch (UnsupportedOperationException e) {
		}
	}

	@Test
	public void testXPathConstants() {
		NamespaceContext context = new NamespaceContextMap();
		Assert.assertEquals(XMLConstants.XML_NS_URI, context.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
		Assert.assertEquals(XMLConstants.XML_NS_PREFIX, context.getPrefix(XMLConstants.XML_NS_URI));
		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
	}
	
	@Test
	public void testUpdatedMessagesSyntax_1() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job3.xml"));
			assertNotNull(job);
			
			assertEquals("job-id-03", job.getId());
			assertEquals(Status.SUCCESS, job.getStatus());
			assertEquals(Priority.low, job.getPriority());
			assertEquals("simple-dtbook-1", job.getNicename());
			assertEquals(new Integer(7), job.getQueuePosition());
			
			assertEquals("dtbook-to-zedai", job.getScript().getId());
			assertEquals("http://example.org/ws/scripts/dtbook-to-zedai", job.getScript().getHref());
			assertEquals("http://example.org/ws/scripts/dtbook-to-zedai", job.getScriptHref());
			assertEquals("DTBook to ZedAI", job.getScript().getNicename());
			assertEquals("Transforms DTBook XML into ZedAI XML.", job.getScript().getDescription());
			assertEquals("1.1.2", job.getScript().getVersion());
			
			assertEquals(new BigDecimal(1.0), job.getProgress());
			assertEquals(Level.WARNING, job.getMessages().get(0).level);
			assertEquals(new Integer(22), job.getMessages().get(0).sequence);
			assertEquals("Warning about this job", job.getMessages().get(0).text);
			
			assertEquals("http://example.org/ws/jobs/job-id-03/log", job.getLogHref());
			
			assertEquals("http://example.org/ws/jobs/job-id-03/result", job.getResult().href);
			assertEquals("zip", job.getResult().mimeType);
			
			assertEquals("http://example.org/ws/jobs/job-id-03/result/option/output-dir", job.getResult("output-dir").href);
			assertEquals("option", job.getResult("output-dir").from);
			assertEquals("zip", job.getResult("output-dir").mimeType);
			assertEquals("Output directory", job.getResult("output-dir").nicename);
			assertEquals("http://example.org/ws/jobs/job-id-03/result/option/idx/output-dir/file-1.xhtml", job.getResults("output-dir").get(0).href);
			assertEquals("application/xml", job.getResults("output-dir").get(0).mimeType);
			assertEquals(new Long(110), job.getResults("output-dir").get(0).size);
			
			assertEquals("http://example.org/ws/jobs/job-id-03/result/port/result", job.getResult("result").href);
			assertEquals("port", job.getResult("result").from);
			assertEquals("zip", job.getResult("result").mimeType);
			assertEquals("XML Results", job.getResult("result").nicename);
			assertEquals("http://example.org/ws/jobs/job-id-03/result/port/idx/result/result-1.xml", job.getResults("result").get(0).href);
			assertEquals("application/xml", job.getResults("result").get(0).mimeType);
			assertEquals(new Long(1024), job.getResults("result").get(0).size);
			assertEquals("http://example.org/ws/jobs/job-id-03/result/port/idx/result/result-2.xml", job.getResults("result").get(1).href);
			assertEquals("application/xml", job.getResults("result").get(1).mimeType);
			assertEquals(new Long(54321), job.getResults("result").get(1).size);
			
		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testUpdatedMessagesSyntax_2() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job4.xml"));
			assertNotNull(job);
			
			assertEquals("http://localhost:8181/ws/jobs/cb34a1c5-7a93-411f-afaf-4d554a7645d9", job.getHref());
		    assertEquals("cb34a1c5-7a93-411f-afaf-4d554a7645d9", job.getId());
		    assertEquals(Priority.medium, job.getPriority());
		    assertEquals(Status.ERROR, job.getStatus());
		    
		    assertEquals("http://localhost:8181/ws/scripts/dtbook-to-zedai", job.getScript().getHref());
		    assertEquals("dtbook-to-zedai", job.getScript().getId());
		    assertThat(Arrays.asList("dtbook"), is(job.getScript().getInputFilesets()));
		    assertThat(Arrays.asList("zedai"), is(job.getScript().getOutputFilesets()));
		    
		    assertEquals("DTBook to ZedAI", job.getScript().getNicename());
		    assertEquals("Transforms DTBook XML into ZedAI XML.", job.getScript().getDescription());
		    assertEquals("2.0.0", job.getScript().getVersion());
		    assertEquals("http://daisy.github.io/pipeline/modules/dtbook-to-zedai", job.getScript().getHomepage());
		    
		    assertEquals("One or more DTBook files to be transformed. In the case of multiple files, a merge will be performed.", job.getScript().getArgument("source").getDesc());
		    assertThat(Arrays.asList("application/x-dtbook+xml"), is(job.getScript().getArgument("source").getMediaTypes()));
		    assertEquals("DTBook file(s)", job.getScript().getArgument("source").getNicename());
		    assertEquals(true, job.getScript().getArgument("source").getOrdered());
		    assertEquals(true, job.getScript().getArgument("source").getRequired());
		    assertEquals(true, job.getScript().getArgument("source").getSequence());
		    assertEquals("anyFileURI", job.getScript().getArgument("source").getType());
		    assertEquals(Arrays.asList("550283.xml"), job.getScript().getArgument("source").getAsList());

		    assertEquals("Filename for the generated ZedAI file", job.getScript().getArgument("zedai-filename").getDesc());
		    assertThat(Arrays.asList(), is(job.getScript().getArgument("zedai-filename").getMediaTypes()));
		    assertEquals("ZedAI filename", job.getScript().getArgument("zedai-filename").getNicename());
		    assertEquals(true, job.getScript().getArgument("zedai-filename").getOrdered());
		    assertEquals(false, job.getScript().getArgument("zedai-filename").getRequired());
		    assertEquals(false, job.getScript().getArgument("zedai-filename").getSequence());
		    assertEquals("string", job.getScript().getArgument("zedai-filename").getType());
		    assertEquals("", job.getScript().getArgument("zedai-filename").get());

		    assertEquals("Whether to stop processing and raise an error on validation issues.", job.getScript().getArgument("assert-valid").getDesc());
		    assertThat(Arrays.asList(), is(job.getScript().getArgument("assert-valid").getMediaTypes()));
		    assertEquals("Assert validity", job.getScript().getArgument("assert-valid").getNicename());
		    assertEquals(true, job.getScript().getArgument("assert-valid").getOrdered());
		    assertEquals(false, job.getScript().getArgument("assert-valid").getRequired());
		    assertEquals(false, job.getScript().getArgument("assert-valid").getSequence());
		    assertEquals("boolean", job.getScript().getArgument("assert-valid").getType());
		    assertEquals("true", job.getScript().getArgument("assert-valid").get());

		    assertEquals("Filename for the generated MODS file", job.getScript().getArgument("mods-filename").getDesc());
		    assertThat(Arrays.asList(), is(job.getScript().getArgument("mods-filename").getMediaTypes()));
		    assertEquals("MODS filename", job.getScript().getArgument("mods-filename").getNicename());
		    assertEquals(true, job.getScript().getArgument("mods-filename").getOrdered());
		    assertEquals(false, job.getScript().getArgument("mods-filename").getRequired());
		    assertEquals(false, job.getScript().getArgument("mods-filename").getSequence());
		    assertEquals("string", job.getScript().getArgument("mods-filename").getType());
		    assertEquals("", job.getScript().getArgument("mods-filename").get());

		    assertEquals("Filename for the generated CSS file", job.getScript().getArgument("css-filename").getDesc());
		    assertThat(Arrays.asList(), is(job.getScript().getArgument("css-filename").getMediaTypes()));
		    assertEquals("CSS filename", job.getScript().getArgument("css-filename").getNicename());
		    assertEquals(true, job.getScript().getArgument("css-filename").getOrdered());
		    assertEquals(false, job.getScript().getArgument("css-filename").getRequired());
		    assertEquals(false, job.getScript().getArgument("css-filename").getSequence());
		    assertEquals("string", job.getScript().getArgument("css-filename").getType());
		    assertEquals("", job.getScript().getArgument("css-filename").get());

		    assertEquals("Language code of the input document.", job.getScript().getArgument("lang").getDesc());
		    assertThat(Arrays.asList(), is(job.getScript().getArgument("lang").getMediaTypes()));
		    assertEquals("Language code", job.getScript().getArgument("lang").getNicename());
		    assertEquals(true, job.getScript().getArgument("lang").getOrdered());
		    assertEquals(false, job.getScript().getArgument("lang").getRequired());
		    assertEquals(false, job.getScript().getArgument("lang").getSequence());
		    assertEquals("string", job.getScript().getArgument("lang").getType());
		    assertEquals("", job.getScript().getArgument("lang").get());

		    assertEquals("Include any referenced external resources like images and CSS-files to the output.", job.getScript().getArgument("copy-external-resources").getDesc());
		    assertThat(Arrays.asList(), is(job.getScript().getArgument("copy-external-resources").getMediaTypes()));
		    assertEquals("Copy external resources", job.getScript().getArgument("copy-external-resources").getNicename());
		    assertEquals(true, job.getScript().getArgument("copy-external-resources").getOrdered());
		    assertEquals(false, job.getScript().getArgument("copy-external-resources").getRequired());
		    assertEquals(false, job.getScript().getArgument("copy-external-resources").getSequence());
		    assertEquals("boolean", job.getScript().getArgument("copy-external-resources").getType());
		    assertEquals("true", job.getScript().getArgument("copy-external-resources").get());

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testUpdatedMessagesSyntax_3() {
		try {
			Job job = new Job(loadResourceXml("responses/jobs/job5.xml"));
			assertNotNull(job);
			
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c", job.getHref());
			assertEquals("d5aceca9-b5ac-40ec-9c08-09622f35656c", job.getId());
			assertEquals(Priority.medium, job.getPriority());
			assertEquals(Status.SUCCESS, job.getStatus());
			
			assertEquals("http://localhost:8181/ws/scripts/epub3-to-daisy202", job.getScript().getHref());
			assertEquals("epub3-to-daisy202", job.getScript().getId());
			assertThat(Arrays.asList("epub3"), is(job.getScript().getInputFilesets()));
			assertThat(Arrays.asList("daisy202"), is(job.getScript().getOutputFilesets()));
			
			assertEquals("EPUB 3 to DAISY 2.02", job.getScript().getNicename());
			assertEquals("Transforms an EPUB 3 publication into DAISY 2.02.", job.getScript().getDescription());
			assertEquals("2.0.0", job.getScript().getVersion());
			assertEquals("http://daisy.github.io/pipeline/modules/epub3-to-daisy202", job.getScript().getHomepage());
			
			assertEquals("The EPUB 3 you want to convert to DAISY 2.02.\n\nYou may alternatively use the EPUB package document (the OPF-file) if your input is a unzipped/\"exploded\" version of an EPUB.", job.getArgument("epub").getDesc());
			assertThat(Arrays.asList("application/epub+zip", "application/oebps-package+xml"), is(job.getArgument("epub").getMediaTypes()));
			assertEquals("EPUB 3 Publication", job.getArgument("epub").getNicename());
			assertEquals(true, job.getArgument("epub").getOrdered());
			assertEquals(true, job.getArgument("epub").getRequired());
			assertEquals(false, job.getArgument("epub").getSequence());
			assertEquals("anyFileURI", job.getArgument("epub").getType());
			assertEquals("C00000.epub", job.getArgument("epub").get());
			
			assertEquals("Whether to abort on validation issues.", job.getArgument("validation").getDesc());
			assertThat(Arrays.asList(), is(job.getArgument("validation").getMediaTypes()));
			assertEquals("Validation", job.getArgument("validation").getNicename());
			assertEquals(true, job.getArgument("validation").getOrdered());
			assertEquals(false, job.getArgument("validation").getRequired());
			assertEquals(false, job.getArgument("validation").getSequence());
			assertEquals("string", job.getArgument("validation").getType());
			assertEquals("off", job.getArgument("validation").get());
			
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/log", job.getLogHref());
			
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result", job.getResult().href);
			assertEquals("application/zip", job.getResult().mimeType);
			assertEquals(new Long(532149), job.getResult().size);
			
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir", job.getResult("output-dir").href);
			assertEquals("option", job.getResult("output-dir").from);
			assertEquals("application/zip", job.getResult("output-dir").mimeType);
			assertEquals(new Long(532149), job.getResult("output-dir").size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-01-cover.html", job.getResults("output-dir").get(0).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-01-cover.html", job.getResults("output-dir").get(0).href);
			assertEquals(new Long(1917), job.getResults("output-dir").get(0).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-02-toc.html", job.getResults("output-dir").get(1).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-02-toc.html", job.getResults("output-dir").get(1).href);
			assertEquals(new Long(3685), job.getResults("output-dir").get(1).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-03-frontmatter.html", job.getResults("output-dir").get(2).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-03-frontmatter.html", job.getResults("output-dir").get(2).href);
			assertEquals(new Long(21921), job.getResults("output-dir").get(2).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-04-chapter.html", job.getResults("output-dir").get(3).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-04-chapter.html", job.getResults("output-dir").get(3).href);
			assertEquals(new Long(3840), job.getResults("output-dir").get(3).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-05-chapter.html", job.getResults("output-dir").get(4).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-05-chapter.html", job.getResults("output-dir").get(4).href);
			assertEquals(new Long(10871), job.getResults("output-dir").get(4).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-06-chapter.html", job.getResults("output-dir").get(5).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-06-chapter.html", job.getResults("output-dir").get(5).href);
			assertEquals(new Long(104313), job.getResults("output-dir").get(5).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-07-rearnotes.html", job.getResults("output-dir").get(6).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-07-rearnotes.html", job.getResults("output-dir").get(6).href);
			assertEquals(new Long(1127), job.getResults("output-dir").get(6).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-08-chapter.html", job.getResults("output-dir").get(7).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-08-chapter.html", job.getResults("output-dir").get(7).href);
			assertEquals(new Long(21052), job.getResults("output-dir").get(7).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-09-part.html", job.getResults("output-dir").get(8).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-09-part.html", job.getResults("output-dir").get(8).href);
			assertEquals(new Long(1296), job.getResults("output-dir").get(8).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-10-chapter.html", job.getResults("output-dir").get(9).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-10-chapter.html", job.getResults("output-dir").get(9).href);
			assertEquals(new Long(923), job.getResults("output-dir").get(9).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-11-conclusion.html", job.getResults("output-dir").get(10).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-11-conclusion.html", job.getResults("output-dir").get(10).href);
			assertEquals(new Long(15217), job.getResults("output-dir").get(10).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/C00000-12-footnotes.html", job.getResults("output-dir").get(11).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/C00000-12-footnotes.html", job.getResults("output-dir").get(11).href);
			assertEquals(new Long(19389), job.getResults("output-dir").get(11).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/css/accessibility.css", job.getResults("output-dir").get(12).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/css/accessibility.css", job.getResults("output-dir").get(12).href);
			assertEquals(new Long(2126), job.getResults("output-dir").get(12).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/css/fonts/opendyslexic/LICENSE.txt", job.getResults("output-dir").get(13).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/css/fonts/opendyslexic/LICENSE.txt", job.getResults("output-dir").get(13).href);
			assertEquals(new Long(2746), job.getResults("output-dir").get(13).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-Bold.otf", job.getResults("output-dir").get(14).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-Bold.otf", job.getResults("output-dir").get(14).href);
			assertEquals(new Long(46816), job.getResults("output-dir").get(14).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-BoldItalic.otf", job.getResults("output-dir").get(15).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-BoldItalic.otf", job.getResults("output-dir").get(15).href);
			assertEquals(new Long(74924), job.getResults("output-dir").get(15).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-Italic.otf", job.getResults("output-dir").get(16).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-Italic.otf", job.getResults("output-dir").get(16).href);
			assertEquals(new Long(69628), job.getResults("output-dir").get(16).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", job.getResults("output-dir").get(17).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexic-Regular.otf", job.getResults("output-dir").get(17).href);
			assertEquals(new Long(46464), job.getResults("output-dir").get(17).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexicMono-Regular.otf", job.getResults("output-dir").get(18).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/css/fonts/opendyslexic/OpenDyslexicMono-Regular.otf", job.getResults("output-dir").get(18).href);
			assertEquals(new Long(53888), job.getResults("output-dir").get(18).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/images/valentin.jpg", job.getResults("output-dir").get(19).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/images/valentin.jpg", job.getResults("output-dir").get(19).href);
			assertEquals(new Long(25740), job.getResults("output-dir").get(19).size);

			assertEquals("file:/home/…/daisy-pipeline/data/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/output/output-dir/C00000/ncc.html", job.getResults("output-dir").get(20).file);
			assertEquals("http://localhost:8181/ws/jobs/d5aceca9-b5ac-40ec-9c08-09622f35656c/result/option/output-dir/idx/output-dir/C00000/ncc.html", job.getResults("output-dir").get(20).href);
			assertEquals(new Long(4266), job.getResults("output-dir").get(20).size);

		} catch (Pipeline2Exception e) {
			fail(e.getMessage());
		}
	}

}
