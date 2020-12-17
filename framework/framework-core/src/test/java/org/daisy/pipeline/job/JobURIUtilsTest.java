package org.daisy.pipeline.job;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobURIUtilsTest   {
	JobId id;
	String oldIoBase="";
	File jobsDir;

	@Before
	public void setUp() {
		oldIoBase = System.getProperty("org.daisy.pipeline.data");
		File tmpdir = new File(System.getProperty("java.io.tmpdir"));
		System.setProperty("org.daisy.pipeline.data", tmpdir.toString());
		jobsDir = new File(tmpdir, "jobs");
		id= JobIdFactory.newId();
		
	}

	@After
	public void tearDown(){
		if(oldIoBase!=null)
			System.setProperty("org.daisy.pipeline.data", oldIoBase);
	}

	@Test
	public void outputUriMapper() throws Exception{
		URIMapper mapper = JobURIUtils.newOutputURIMapper(id.toString());
		Assert.assertEquals(URI.create(""),mapper.getInputBase());
		String commonBase=jobsDir.toURI().toString()+id.toString()+"/";
		Assert.assertEquals(URI.create(commonBase+JobURIUtils.IO_OUTPUT_SUBDIR+"/"),mapper.getOutputBase());

	}
	
	@Test
	public void idBasedUriMapper() throws Exception{
		URIMapper mapper = JobURIUtils.newURIMapper(id.toString());
		String commonBase=jobsDir.toURI().toString()+id.toString()+"/";
		Assert.assertEquals(URI.create(commonBase+JobURIUtils.IO_DATA_SUBDIR+"/"),mapper.getInputBase());
		Assert.assertEquals(URI.create(commonBase+JobURIUtils.IO_OUTPUT_SUBDIR+"/"),mapper.getOutputBase());

	}

	@Test
	public void getLogFile() throws Exception{
		URI expected= jobsDir.toURI().resolve(URI.create(String.format("%s/%s.log",id.toString(), id.toString())));
		Assert.assertEquals(JobURIUtils.getLogFile(id.toString()).toURI(), expected);

	}
	@Test
	public void getLogFileExsists() throws Exception{
		Assert.assertTrue(JobURIUtils.getLogFile(id.toString()).exists());

	}

	@Test
	public void getJobBase() throws Exception{
		URI expected= jobsDir.toURI().resolve(URI.create(String.format("%s/", id.toString())));
		Assert.assertEquals(JobURIUtils.getJobBase(id.toString()),expected);
	}

	@Test
	public void getJobContextDir() throws Exception{
		File context = JobURIUtils.getJobContextDir(id.toString());
		Assert.assertEquals(new File(JobURIUtils.getJobBaseDir(id.toString()), JobURIUtils.IO_DATA_SUBDIR).getAbsolutePath(), context.getAbsolutePath());
	}

	@Test
	public void getJobOutputDir() throws Exception{
		File output=JobURIUtils.getJobOutputDir(id.toString());
		Assert.assertEquals(new File(JobURIUtils.getJobBaseDir(id.toString()), JobURIUtils.IO_OUTPUT_SUBDIR).getAbsolutePath(), output.getAbsolutePath());
	}
}
