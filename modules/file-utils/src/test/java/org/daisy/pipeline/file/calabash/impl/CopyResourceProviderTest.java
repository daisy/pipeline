package org.daisy.pipeline.file.calabash.impl;

import java.io.File;
import java.net.URI;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.runtime.XAtomicStep;

public class CopyResourceProviderTest  {
	CopyResourceProvider.CopyResource step;
	String user=null;
	String local=null;
	File tmp;

	@Before
	public void setUp(){
		user=System.getProperty("com.xmlcalabash.config.user");
		local=System.getProperty("com.xmlcalabash.config.local");
		System.setProperty("com.xmlcalabash.config.user","");
		System.setProperty("com.xmlcalabash.config.local","");
		XProcRuntime xproc= Mocks.getXProcRuntime();
		XAtomicStep xstep = Mocks.getAtomicStep(xproc); 
		step = new CopyResourceProvider.CopyResource(xproc,xstep);
		tmp=new File(System.getProperty("java.io.tmpdir"));

	}

	@After
	public void tearDown(){

		if(user!=null){
			System.setProperty("com.xmlcalabash.config.user",user);
		}else{
			System.setProperty("com.xmlcalabash.config.user",".calabash");
		}
		if(local!=null){
			System.setProperty("com.xmlcalabash.config.local",local);
		}else{
			System.setProperty("com.xmlcalabash.config.local",".calabash");
		}
	}

	@Test (expected = XProcException.class)
	public void nonFileTarget(){
		step.getFile(URI.create("http://google.com"),null);
	}

	@Test 
	public void regularFile(){
		File myFile=new File(tmp,"tmp.xml");
		File dst=step.getFile(myFile.toURI(),null);
		Assert.assertEquals(dst.toURI(),myFile.toURI());
	}

	@Test 
	public void fileNameBySource(){
		File dst=step.getFile(tmp.toURI(),URI.create("http://google.com/myfile.xml"));
		Assert.assertEquals(dst.toURI(),tmp.toURI().resolve("myfile.xml"));
	}

	@Test 
	public void fileNameByRelativeSource(){
		File dst=step.getFile(tmp.toURI(),URI.create("myfile.xml"));
		Assert.assertEquals(dst.toURI(),tmp.toURI().resolve("myfile.xml"));
	}


}
