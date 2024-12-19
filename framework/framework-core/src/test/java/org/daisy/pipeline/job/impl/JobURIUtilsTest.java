package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobURIUtilsTest {

	static JobId id;
	static String oldIoBase="";
	static File tmpdir;
	static File jobsDir;
	static URI jobsDirURI;

	@BeforeClass
	public static void setUp() {
		oldIoBase = System.getProperty("org.daisy.pipeline.data");
		try {
			tmpdir = Files.createTempDirectory(null).toFile();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.setProperty("org.daisy.pipeline.data", tmpdir.toString());
		jobsDir = new File(tmpdir, "jobs");
		jobsDirURI = URI.create(jobsDir.toURI().toString() + "/");
		id = JobIdFactory.newId();
	}

	@AfterClass
	public static void tearDown(){
		try {
			FileUtils.deleteDirectory(tmpdir);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		if (oldIoBase != null)
			System.setProperty("org.daisy.pipeline.data", oldIoBase);
	}

	@Test
	public void testJobContextDir() throws Exception {
		Assert.assertEquals(new File(jobsDirURI.resolve(URI.create(String.format("%s/%s/", id.toString(), JobURIUtils.IO_DATA_SUBDIR)))),
		                    JobURIUtils.getJobContextDir(id.toString()));
	}

	@Test
	public void getLogFile() throws Exception{
		URI expected = jobsDirURI.resolve(URI.create(String.format("%s/%s.log", id.toString(), id.toString())));
		Assert.assertEquals(JobURIUtils.getLogFile(id.toString()).toURI(), expected);
	}

	@Test
	public void getLogFileExsists() throws Exception{
		Assert.assertTrue(JobURIUtils.getLogFile(id.toString()).exists());
	}

	@Test
	public void getJobBase() throws Exception{
		URI expected = jobsDirURI.resolve(URI.create(String.format("%s/", id.toString())));
		Assert.assertEquals(JobURIUtils.getJobBaseDir(id.toString()).toURI(), expected);
	}

	@Test
	public void getJobContextDir() throws Exception{
		File context = JobURIUtils.getJobContextDir(id.toString());
		Assert.assertEquals(new File(JobURIUtils.getJobBaseDir(id.toString()), JobURIUtils.IO_DATA_SUBDIR).getAbsolutePath(),
		                    context.getAbsolutePath());
	}

	@Test
	public void getJobOutputDir() throws Exception{
		File output = JobURIUtils.getJobOutputDir(id.toString());
		Assert.assertEquals(new File(JobURIUtils.getJobBaseDir(id.toString()), JobURIUtils.IO_OUTPUT_SUBDIR).getAbsolutePath(),
		                    output.getAbsolutePath());
	}
}
