package org.daisy.pipeline.job.impl;

import javax.xml.namespace.QName;

import org.daisy.pipeline.script.Script;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DynamicResultProviderPartsTest   {
	String testFile="dir/file.xml";

	Script mscript;
	QName  optIn      ;
        QName  optReg     ;
        QName  optOutFile ;
        QName  optOutDir  ;
        QName  optOutNA  ;
        QName  optTemp;



	@Before 
	public void setUp(){

		mscript= new Mock.ScriptGenerator.Builder().withOptionOutputsNA(1).withOptionInputs(1).withOptionOther(1).withOptionOutputsDir(1).withOptionOutputsFile(1).withOptionTemp(1).build().generate();
		//options names
		optIn      = Mock.ScriptGenerator.getOptionInputName(0);
		optReg     = Mock.ScriptGenerator.getRegularOptionName(0);
		optOutFile = Mock.ScriptGenerator.getOptionOutputFileName(0);
		optOutDir  = Mock.ScriptGenerator.getOptionOutputDirName(0);
		optOutNA  = Mock.ScriptGenerator.getOptionOutputNAName(0);
		optTemp = Mock.ScriptGenerator.getOptionTempName(0);
	}
	
	@Test 
	public void getDynamicResultProviderPartsNullProvider() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts = DynamicResultProvider.getDynamicResultProviderParts(outName, null, "");
		Assert.assertEquals(outName+"/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
	}
	
	@Test 
	public void getDynamicResultProviderPartsEmpty() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts = DynamicResultProvider.getDynamicResultProviderParts(outName, Mock.getResultProvider("").get().getSystemId(), "");
		Assert.assertEquals(outName+"/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
		
	}
		
	@Test 
	public void getDynamicResultProviderPartsFile() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts = DynamicResultProvider.getDynamicResultProviderParts(outName, Mock.getResultProvider("dir/file.opf").get().getSystemId(), "");
		Assert.assertEquals("dir/file",parts[0]);
		Assert.assertEquals(".opf",parts[1]);
		
	}

	@Test 
	public void getDynamicResultProviderPartsFileNoExtension() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts = DynamicResultProvider.getDynamicResultProviderParts(outName, Mock.getResultProvider("dir/file").get().getSystemId(), "");
		Assert.assertEquals("dir/file",parts[0]);
		Assert.assertEquals("",parts[1]);
		
	}

	@Test 
	public void getDynamicResultProviderPartsFileNoExtensionAndDotsInPath() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts = DynamicResultProvider.getDynamicResultProviderParts(outName, Mock.getResultProvider("di.r/file").get().getSystemId(), "");
		Assert.assertEquals("di.r/file",parts[0]);
		Assert.assertEquals("",parts[1]);
		
	}

	@Test 
	public void getDynamicResultProviderPartsDir() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts = DynamicResultProvider.getDynamicResultProviderParts(outName, Mock.getResultProvider("dir/").get().getSystemId(), "");
		Assert.assertEquals("dir/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
		
	}
	
}
