package org.daisy.pipeline.script.impl;

import java.io.File;
import java.net.URI;

import javax.xml.transform.Result;

import  org.daisy.pipeline.job.impl.Mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DynamicResultProviderTest {

	DynamicResultProvider provider;

	@Before
	public void setUp(){
		provider = new DynamicResultProvider(Mock.getResultProvider("/tmp/file.xml"), "irrelevant", "irrelevant", new File("/"));
	}

	@Test
	public void testGenerateFirst(){
		Result result=provider.get();
		Assert.assertEquals("file:/tmp/file.xml", result.getSystemId());
	}

	@Test
	public void testGenerateSecond(){
		Result result=provider.get();
		result=provider.get();
		Assert.assertEquals("file:/tmp/file-1.xml", result.getSystemId());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testModifyResultObject(){
		provider.get().setSystemId("sth");
	}
}
