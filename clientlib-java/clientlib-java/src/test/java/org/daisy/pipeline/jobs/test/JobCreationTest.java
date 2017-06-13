package org.daisy.pipeline.jobs.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.daisy.pipeline.client.Pipeline2Exception;
import org.daisy.pipeline.client.filestorage.JobStorage;
import org.daisy.pipeline.client.models.Argument;
import org.daisy.pipeline.client.models.Job;
import org.daisy.pipeline.client.utils.XML;
import org.junit.After;
import org.junit.Before;


//import javax.xml.XMLConstants;
//import javax.xml.namespace.NamespaceContext;
//import javax.xml.xpath.XPathConstants;
//import javax.xml.xpath.XPathExpression;
//import javax.xml.xpath.XPathExpressionException;
//import javax.xml.xpath.XPathFactory;
//import org.w3c.dom.Document;
//import org.w3c.dom.NodeList;

import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;


public class JobCreationTest {
	
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
	private Document loadXmlResource(String href) {
		String resource = loadResource(href);
		return XML.getXml(resource);
	}
	
	public TemporaryFolder testFolder;
	public File jobStorageDir;
	
	@Before
	public void populateTestFolder() throws IOException {
		testFolder = new TemporaryFolder();
		testFolder.create();
		jobStorageDir = testFolder.newFolder("jobs");
		File sourceFolder = new File(resources, "jobs");
		JobStorageTest.copyFolder(sourceFolder, jobStorageDir);
	}
	
	@After
	public void tearDown() {
		testFolder.delete();
	}

	@Test
	public void testCreateJob() {
		Job job = null;
		try {
			job = new Job(loadXmlResource("scripts/test.xml"));
		} catch (Pipeline2Exception e) {
			assert false;
		}
		job.setId("job3");
		JobStorage jobStorage = new JobStorage(job, jobStorageDir, null);
		assertNotNull(job);
		assertNotNull(jobStorage);
	}

	@Test
	public void testBuildJobBoolean() {
		Job job = null;
		try {
			job = new Job(loadXmlResource("scripts/test.xml"));
		} catch (Pipeline2Exception e) {
			assert false;
		}
		job.setId("job3");

		// set to a boolean value
		Argument argBoolean = job.getArgument("boolean");
		argBoolean.set(true);
		assertEquals(1, argBoolean.size());
		assertEquals("true", argBoolean.get());
		assertEquals(true, argBoolean.getAsBoolean());
		assertEquals(null, argBoolean.getAsInteger());
		assertEquals(null, argBoolean.getAsDouble());
		assertArrayEquals(new String[]{"true"}, argBoolean.getAsList().toArray());
	}

	@Test
	public void testBuildJobString() {
		Job job = null;
		try {
			job = new Job(loadXmlResource("scripts/test.xml"));
		} catch (Pipeline2Exception e) {
			assert false;
		}
		job.setId("job3");

		// set to a string value
		Argument arg = job.getArgument("string");
		arg.set("value");
		assertEquals(1, arg.size());
		assertEquals("value", arg.get());
		assertNull(arg.getAsBoolean());
		assertNull(arg.getAsInteger());
		assertNull(arg.getAsDouble());
	}

	@Test
	public void testBuildJobUnset() {
		Job job = null;
		try {
			job = new Job(loadXmlResource("scripts/test.xml"));
		} catch (Pipeline2Exception e) {
			assert false;
		}
		job.setId("job3");

		// set to a value, and then set to a undefined value (null)
		Argument arg = job.getArgument("string");
		arg.set("value");
		assertEquals(1, arg.size());
		assertEquals("value", arg.get());
		arg.unset();;
		assertEquals(0, arg.size());
		assertNull(arg.get());
		assertNull(arg.getAsBoolean());
		assertNull(arg.getAsInteger());
		assertNull(arg.getAsDouble());
		assertNull(arg.getAsList());
	}

	@Test
	public void testBuildJobLists() {
		Job job = null;
		try {
			job = new Job(loadXmlResource("scripts/test.xml"));
		} catch (Pipeline2Exception e) {
			assert false;
		}
		job.setId("job3");
		
		Argument arg = job.getArgument("string-list");
		List<String> stringList = new ArrayList<String>();
		stringList.add("foo");
		stringList.add("bar");
		stringList.add("baz");
		arg.setAll(stringList);
		assertEquals(3, arg.size());

		List<String> otherStringList = new ArrayList<String>();
		otherStringList.add("1");
		otherStringList.add("2");
		otherStringList.add("3");
		arg.setAll(stringList);
		assertEquals(3, arg.size());
		for (String value : otherStringList) {
			arg.add(value);
		}
		assertEquals(6, arg.size());
		arg.setAll(stringList);
		assertEquals(3, arg.size());
		
		arg.unset();
		for (int i = 1; i <= 10; i++) {
			arg.add(i);
		}
		assertEquals(10, arg.size());
	}
	
}
