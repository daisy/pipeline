package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.Pipeline2Logger;
import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Callback;
import org.daisy.pipeline.client.models.Callback.Type;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.models.Script;
import org.daisy.pipeline.client.utils.XML;
import org.daisy.pipeline.client.utils.XPath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JobStorageTest {

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

	public TemporaryFolder testFolder;
	public File jobStorageDir;

	@Before
	public void populateTestFolder() throws IOException {
		testFolder = new TemporaryFolder();
		testFolder.create();
		jobStorageDir = testFolder.newFolder("jobs");
		File sourceFolder = new File(resources, "jobs");
		copyFolder(sourceFolder, jobStorageDir);
	}

	@After
	public void tearDown() {
//		testFolder.delete();
	}

	public static void copyFolder(File sourceFolder, File destinationFolder) throws IOException {
		if (sourceFolder.isDirectory()) {
			if (!destinationFolder.exists()) {
				destinationFolder.mkdir();
			}
			String files[] = sourceFolder.list();
			for (String file : files) {
				File srcFile = new File(sourceFolder, file);
				File destFile = new File(destinationFolder, file);
				copyFolder(srcFile, destFile);
			}

		} else {
			Files.copy(sourceFolder.toPath(), destinationFolder.toPath());
		}
	}

	@Test
	public void testListAndLoadJobs() {
		List<String> jobs = JobStorage.listJobs(jobStorageDir);
		assertEquals(2, jobs.size());
		assertEquals("job1", jobs.get(0));
		assertEquals("job2", jobs.get(1));

		Job job = JobStorage.loadJob(jobs.get(0), jobStorageDir);

		assertEquals("job1", job.getId());
		assertEquals("Nicename", job.getNicename());
		assertEquals("\nshort description\n\nlong description\n", job.getDescription());
		assertEquals("http://localhost:8181/ws/scripts/dtbook-to-epub3", job.getScriptHref());

		Script script = job.getScript();
		assertEquals("http://localhost:8181/ws/scripts/dtbook-to-epub3", script.getHref());
		assertEquals("dtbook-to-epub3", script.getId());
		assertEquals(5, script.getInputs().size());

		assertNotNull(script.getArgument("source"));
		assertNotNull(script.getArgument("language"));
		assertNotNull(script.getArgument("assert-valid"));
		assertNotNull(script.getArgument("tts-config"));
		assertNotNull(script.getArgument("audio"));
		assertEquals(script.getArgument("source"), job.getArgument("source"));
		assertEquals(script.getArgument("language"), job.getArgument("language"));
		assertEquals(script.getArgument("assert-valid"), job.getArgument("assert-valid"));
		assertEquals(script.getArgument("tts-config"), job.getArgument("tts-config"));
		assertEquals(script.getArgument("audio"), job.getArgument("audio"));

		assertEquals(2, job.getArgument("source").size());
		assertEquals(1, job.getArgument("language").size());
		assertEquals(1, job.getArgument("assert-valid").size());
		assertEquals(1, job.getArgument("audio").size());

		assertEquals("hauy_valid.xml", job.getArgument("source").getAsList().get(0));
		assertEquals("dtbook.2005.basic.css", job.getArgument("source").getAsList().get(1));
		assertEquals("en", job.getArgument("language").get());
		assertEquals("true", job.getArgument("assert-valid").get());
		assertEquals("true", job.getArgument("audio").get());
		assertEquals(true, job.getArgument("assert-valid").getAsBoolean());
		assertEquals(true, job.getArgument("audio").getAsBoolean());

		job = JobStorage.loadJob(jobs.get(1), jobStorageDir);

		assertEquals("job2", job.getId());
		assertEquals("Other nicename", job.getNicename());
		assertEquals(null, job.getDescription());
		assertEquals("http://localhost:8181/ws/scripts/dtbook-to-epub3", job.getScriptHref());
		assertEquals("dtbook-to-epub3", job.getScript().getId());

		assertEquals(3, job.getInputs().size());
		assertNotNull(job.getArgument("source"));
		assertNull(job.getArgument("language"));
		assertNotNull(job.getArgument("assert-valid"));
		assertNotNull(job.getArgument("audio"));

		assertEquals(1, job.getArgument("source").size());
		assertEquals(1, job.getArgument("assert-valid").size());
		assertEquals(1, job.getArgument("audio").size());

		assertEquals("hauy_valid.xml", job.getArgument("source").get());
		assertEquals(true, job.getArgument("assert-valid").getAsBoolean());
		assertEquals(false, job.getArgument("audio").getAsBoolean());
	}

	@Test
	public void testDeleteJob() {
		List<String> jobsBefore = JobStorage.listJobs(jobStorageDir);

		Job job2 = JobStorage.loadJob("job2", jobStorageDir);
		job2.getJobStorage().delete();

		List<String> jobsAfter = JobStorage.listJobs(jobStorageDir);

		assertEquals(2, jobsBefore.size());
		assertEquals(1, jobsAfter.size());
		assertTrue(jobsAfter.contains("job1"));
		assertFalse(jobsAfter.contains("job2"));
	}

	@Test
	public void testStoreJob() {
		List<String> jobsBefore = JobStorage.listJobs(jobStorageDir);

		Path textFilePath = null;
		jobStorageDir = testFolder.newFolder("jobs");
		try {
			textFilePath = Files.createTempFile("test", "test");
			String text = "this is a test";
			Files.write(textFilePath, text.getBytes());

		} catch (IOException e) {
			Pipeline2Logger.logger().error("Unable to store XML for job", e);
			assertTrue(false);
		}

		Job newJob = new Job();
		try {
			String scriptXmlString = loadResource("scripts/dtbook-to-epub3.xml");
			Document scriptXml = XML.getXml(scriptXmlString);
			Script script = new Script(scriptXml);
			newJob.setScript(script);

		} catch (Pipeline2Exception e) {
			assertTrue(false);
		}
		assertNotNull(newJob.getScript());
		assertNotNull(newJob.getScript().getArgument("source"));
		assertEquals(Argument.Kind.input, newJob.getScript().getArgument("source").getKind());
		assertEquals(Argument.Kind.option, newJob.getScript().getArgument("assert-valid").getKind());
		newJob.setId("job3");
		JobStorage jobStorage = new JobStorage(newJob, jobStorageDir, null);
		newJob.getArgument("source").add(textFilePath.toFile(), jobStorage);
		newJob.getArgument("assert-valid").set(false);
		List<Callback> callbacks = new ArrayList<Callback>();
		callbacks.add(new Callback("http://example.com/1", Type.status, "1"));
		callbacks.add(new Callback("http://example.com/2", Type.messages, "2"));
		newJob.setCallback(callbacks);
		jobStorage.save();

		List<String> jobsAfter = JobStorage.listJobs(jobStorageDir);

		assertEquals(2, jobsBefore.size());
		assertEquals(3, jobsAfter.size());
		assertTrue(jobsAfter.contains("job1"));
		assertTrue(jobsAfter.contains("job2"));
		assertTrue(jobsAfter.contains("job3"));
		assertTrue(new File(jobStorageDir, "job3/job.xml").isFile());

		File jobXmlFile = new File(jobStorageDir, "job3/job.xml");
		String jobXmlString = null;
		List<Node> elements = null;
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(jobXmlFile.getPath()));
			jobXmlString = new String(encoded, Charset.defaultCharset());

			Document serializedXml = XML.getXml(jobXmlString);
			elements = XPath.selectNodes("//*", serializedXml, XPath.dp2ns);

		} catch (IOException e) {
			assertTrue("Failed to read "+jobXmlFile.getPath(), false);

		} catch (Pipeline2Exception e) {
			assertTrue("Failed to read "+jobXmlFile.getPath(), false);
		}

		for (Node element : elements) {
			assertNull("Elements should be in the default namespace (element name: "+element.getLocalName()+" has the namespace prefix '"+element.getPrefix()+"')", element.getPrefix());
			assertEquals("The default namespace for all elements should be 'http://www.daisy.org/ns/pipeline/data' (element name: "+element.getLocalName()+")", "http://www.daisy.org/ns/pipeline/data", element.getNamespaceURI());
			assertFalse("No element should have the local name 'null' (parent:"+element.getParentNode().getLocalName()+")", "null".equals(element.getLocalName()));
		}
	}

	@Test
	public void testFiles() {
		Job job = new Job();
		try {
			String scriptXmlString = loadResource("scripts/dtbook-to-epub3.xml");
			Document scriptXml = XML.getXml(scriptXmlString);
			Script script = new Script(scriptXml);
			job.setScript(script);

		} catch (Pipeline2Exception e) {
			assertTrue(false);
		}
		job.setId("filesJob");
		JobStorage jobStorage = new JobStorage(job, jobStorageDir, null);

		File xmlFile = new File(jobStorageDir, "job1/context/hauy_valid.xml");
		File cssFile = new File(jobStorageDir, "job1/context/dtbook.2005.basic.css");

		Argument source = job.getArgument("source");
		source.set(xmlFile, jobStorage);
		assertEquals(1, source.size());
		assertEquals("hauy_valid.xml", source.get());

		jobStorage.addContextFile(cssFile, "css/style.css");
		source.set(cssFile, jobStorage);
		assertEquals(1, source.size());
		assertEquals("css/style.css", source.get());

		source.add(xmlFile, jobStorage);
		assertEquals(2, source.size());
		assertEquals("hauy_valid.xml", source.getAsList().get(1));

		Argument outputDir = job.getArgument("output-dir");
		outputDir.set(testFolder.getRoot(), jobStorage);
		assertEquals(1, outputDir.size());
		try {
			if (System.getProperty("os.name").toLowerCase().startsWith("mac os x"))
				assertEquals("file:"+testFolder.getRoot().getAbsolutePath()+"/", outputDir.get());
			else
				assertEquals("file:"+testFolder.getRoot().getCanonicalPath()+"/", outputDir.get());
		} catch (IOException e) {
			assertTrue(false);
		}
	}

}
