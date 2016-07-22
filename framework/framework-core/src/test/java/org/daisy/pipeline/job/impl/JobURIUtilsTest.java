package org.daisy.pipeline.job.impl;

import java.io.File;
import java.net.URI;

import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.job.impl.JobURIUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobURIUtilsTest   {
	JobId id;
	String oldIoBase="";
	File tmpdir;

	@Before
	public void setUp() {
		oldIoBase=System.getProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE);	
		tmpdir= new File(System.getProperty("java.io.tmpdir"));
		System.setProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE,tmpdir.toString());	
		id= JobIdFactory.newId();
		
	}

	@After
	public void tearDown(){
		if(oldIoBase!=null)
			System.setProperty(JobURIUtils.ORG_DAISY_PIPELINE_IOBASE,oldIoBase);	
	}

	@Test
	public void outputUriMapper() throws Exception{
		URIMapper mapper = JobURIUtils.newOutputURIMapper(id);
		Assert.assertEquals(URI.create(""),mapper.getInputBase());
		String commonBase=tmpdir.toURI().toString()+id.toString()+"/";
		Assert.assertEquals(URI.create(commonBase+JobURIUtils.IO_OUTPUT_SUBDIR+"/"),mapper.getOutputBase());

	}
	
	@Test
	public void idBasedUriMapper() throws Exception{
		URIMapper mapper = JobURIUtils.newURIMapper(id);
		String commonBase=tmpdir.toURI().toString()+id.toString()+"/";
		Assert.assertEquals(URI.create(commonBase+JobURIUtils.IO_DATA_SUBDIR+"/"),mapper.getInputBase());
		Assert.assertEquals(URI.create(commonBase+JobURIUtils.IO_OUTPUT_SUBDIR+"/"),mapper.getOutputBase());

	}

	@Test
	public void getLogFile() throws Exception{
		URI expected= tmpdir.toURI().resolve(URI.create(String.format("%s/%s.log",id.toString(),id.toString())));
		Assert.assertEquals(JobURIUtils.getLogFile(id),expected);

	}
	@Test
	public void getLogFileExsists() throws Exception{
		Assert.assertTrue(new File(JobURIUtils.getLogFile(id)).exists());

	}

	@Test
	public void getJobBase() throws Exception{
		URI expected= tmpdir.toURI().resolve(URI.create(String.format("%s/",id.toString())));
		Assert.assertEquals(JobURIUtils.getJobBase(id),expected);
	}

	@Test
	public void getJobContextDir() throws Exception{
                File context=JobURIUtils.getJobContextDir(id);
		Assert.assertEquals(new File(JobURIUtils.getJobBaseFile(id),JobURIUtils.IO_DATA_SUBDIR).getAbsolutePath(),context.getAbsolutePath());
	}

	@Test
	public void getJobOutputDir() throws Exception{
                File output=JobURIUtils.getJobOutputDir(id);
		Assert.assertEquals(new File(JobURIUtils.getJobBaseFile(id),JobURIUtils.IO_OUTPUT_SUBDIR).getAbsolutePath(),output.getAbsolutePath());
	}
}
