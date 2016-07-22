package org.daisy.pipeline.job.impl;

import java.net.URI;

import org.daisy.pipeline.job.URIMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class URIMapperTest   {
	URI baseIn=URI.create("file:/in/");
	URI baseOut=URI.create("file:/out/");
	URI empty=URI.create("");
	URI uri1=URI.create("one/");
	@Before
	public void setUp() {
				
	}

	@Test
	public void mapEmpty() throws Exception{
		URIMapper mapper=new URIMapper(empty,empty);
		Assert.assertEquals(mapper.mapInput(uri1),uri1);
	}	
	@Test
	public void unmapEmpty() throws Exception{
		URIMapper mapper=new URIMapper(empty,empty);
		Assert.assertEquals(mapper.unmapInput(uri1),uri1);
	}	
	@Test
	public void mapOutput() throws Exception{
		URIMapper mapper=new URIMapper(baseIn,baseOut);
		Assert.assertEquals(mapper.mapOutput(uri1).toString(),baseOut.toString()+uri1.toString());
	}	
	@Test
	public void unmapOutput() throws Exception{
		URIMapper mapper=new URIMapper(baseIn,baseOut);
		Assert.assertEquals(mapper.unmapOutput(mapper.mapOutput(uri1)),uri1);
	}	
}
