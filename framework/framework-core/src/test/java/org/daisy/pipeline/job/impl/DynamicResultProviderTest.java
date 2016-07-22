package org.daisy.pipeline.job.impl;

import org.daisy.pipeline.job.impl.DynamicResultProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.Result;

public class DynamicResultProviderTest {
	DynamicResultProvider provider;
	String pref1="/tmp/file";
	String suf1=".xml";
	@Before
	public void setUp(){
		provider = new DynamicResultProvider(pref1,suf1);
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
