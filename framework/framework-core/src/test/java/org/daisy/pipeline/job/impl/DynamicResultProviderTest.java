package org.daisy.pipeline.job.impl;

import java.io.File;
import java.net.URI;

import org.daisy.pipeline.job.impl.DynamicResultProvider;
import org.daisy.pipeline.job.URIMapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.Result;

public class DynamicResultProviderTest {
	DynamicResultProvider provider;
	@Before
	public void setUp(){
		provider = new DynamicResultProvider(Mock.getResultProvider("/tmp/file.xml"), "irrelevant", "irrelevant", new URIMapper(null, URI.create("")));
	}

	@Test
	public void testGenerateFirst(){
		Result result=provider.get();
		Assert.assertEquals("/tmp/file.xml",result.getSystemId());
	}

	@Test
	public void testGenerateSecond(){
		Result result=provider.get();
		result=provider.get();
		Assert.assertEquals("/tmp/file-1.xml",result.getSystemId());
	}

	@Test(expected=UnsupportedOperationException.class)
	public void testModifyResultObject(){
		provider.get().setSystemId("sth");
	}
}
