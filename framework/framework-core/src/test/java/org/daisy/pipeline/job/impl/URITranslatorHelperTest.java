package org.daisy.pipeline.job.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.impl.URITranslatorHelper;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class URITranslatorHelperTest   {
	String testFile="dir/file.xml";

	XProcScript mscript;
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
	public void notEmpty(){
		Assert.assertTrue(URITranslatorHelper.notEmpty("hola"));
		Assert.assertFalse(URITranslatorHelper.notEmpty(""));
		Assert.assertFalse(URITranslatorHelper.notEmpty("''"));
		Assert.assertFalse(URITranslatorHelper.notEmpty("\"\""));
		Assert.assertFalse(URITranslatorHelper.notEmpty(null));
	}


	/**
	 * Tests 'getTranslatableOptionFilter'.
	 *
	 * @see org.daisy.pipeline.job.impl.URITranslatorHelper#getTranslatableOptionFilter(XProcScript)
	 */
	@Test
	public void getTranslatableOptionFilter() throws Exception {

		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getTranslatableOptionFilter(mscript));	
		Assert.assertEquals(5,filtered.size());
		//check we have the ones we expect
		Set<QName> names= Sets.newHashSet();
		names.add(optIn);
		names.add(optOutDir);
		names.add(optOutFile);
		names.add(optOutNA);
		names.add(optTemp);
		for(XProcOptionInfo inf:filtered)
			Assert.assertTrue(String.format("Name %s should've been filtered out",inf.getName()),names.contains(inf.getName()));

			
	}


	/**
	 * Tests 'getOutputOptionFilter'.
	 *
	 * @see org.daisy.pipeline.job.impl.URITranslatorHelper#getOutputOptionFilter(XProcScript)
	 */
	@Test
	public void getOutputOptionFilter() throws Exception {
		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getOutputOptionFilter(mscript));	

		Assert.assertEquals(3,filtered.size());
		//check we have the ones we expect
		Set<QName> names= Sets.newHashSet();
		names.add(optOutDir);
		names.add(optOutFile);
		names.add(optTemp);
		for(XProcOptionInfo inf:filtered)
			Assert.assertTrue(String.format("Name %s should've been filtered",inf.getName()),names.contains(inf.getName()));
	}

	/**
	 * Tests 'getTranslatableOutputOptionsFilter'.
	 *
	 * @see org.daisy.pipeline.job.impl.URITranslatorHelper#getTranslatableOutputOptionsFilter(XProcScript)
	 */
	@Test
	public void getTranslatableOutputOptionsFilter() throws Exception {
		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getTranslatableOutputOptionsFilter(mscript));	

		Assert.assertEquals(3,filtered.size());
		//check we have the ones we expect
		Set<QName> names= Sets.newHashSet();
		names.add(optOutDir);
		names.add(optOutFile);
		names.add(optTemp);
		for(XProcOptionInfo inf:filtered)
			Assert.assertTrue(String.format("Name %s should've been filtered",inf.getName()),names.contains(inf.getName()));
	}

	@Test
	public void getResultOptionsFilter() throws Exception {
		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getResultOptionsFilter(mscript));	

		Assert.assertEquals(2,filtered.size());
		//check we have the ones we expect
		Set<QName> names= Sets.newHashSet();
		names.add(optOutDir);
		names.add(optOutFile);
		for(XProcOptionInfo inf:filtered)
			Assert.assertTrue(String.format("Name %s should've been filtered",inf.getName()),names.contains(inf.getName()));
	}
	/**
	 * Tests 'getTranslatableInputOptionsFilter'.
	 *
	 * @see org.daisy.pipeline.job.impl.URITranslatorHelper#getTranslatableInputOptionsFilter(XProcScript)
	 */
	@Test
	public void getTranslatableInputOptionsFilter() throws Exception {
		List<XProcOptionInfo> infos=Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		Collection<XProcOptionInfo> filtered=Collections2.filter(infos,URITranslatorHelper.getTranslatableInputOptionsFilter(mscript));	
		Assert.assertEquals(2,filtered.size());
		Assert.assertEquals(Lists.newArrayList(filtered).get(1).getName(),optOutNA);
		Assert.assertEquals(Lists.newArrayList(filtered).get(0).getName(),optIn);
	}
	
	@Test 
	public void getDynamicResultProviderPartsNullProvider() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultProviderParts(outName,null,"");
		Assert.assertEquals(outName+"/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
	}
	
	@Test 
	public void getDynamicResultProviderPartsEmpty() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultProviderParts(outName,Mock.getResultProvider("").get().getSystemId(),"");
		Assert.assertEquals(outName+"/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
		
	}
		
	@Test 
	public void getDynamicResultProviderPartsFile() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultProviderParts(outName,Mock.getResultProvider("dir/file.opf").get().getSystemId(),"");
		Assert.assertEquals("dir/file",parts[0]);
		Assert.assertEquals(".opf",parts[1]);
		
	}

	@Test 
	public void getDynamicResultProviderPartsFileNoExtension() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultProviderParts(outName,Mock.getResultProvider("dir/file").get().getSystemId(),"");
		Assert.assertEquals("dir/file",parts[0]);
		Assert.assertEquals("",parts[1]);
		
	}

	@Test 
	public void getDynamicResultProviderPartsFileNoExtensionAndDotsInPath() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultProviderParts(outName,Mock.getResultProvider("di.r/file").get().getSystemId(),"");
		Assert.assertEquals("di.r/file",parts[0]);
		Assert.assertEquals("",parts[1]);
		
	}

	@Test 
	public void getDynamicResultProviderPartsDir() throws Exception{

		String outName = Mock.ScriptGenerator.getOutputName(0);
		String[] parts= URITranslatorHelper.getDynamicResultProviderParts(outName,Mock.getResultProvider("dir/").get().getSystemId(),"");
		Assert.assertEquals("dir/"+outName,parts[0]);
		Assert.assertEquals(".xml",parts[1]);
		
	}
	
}
