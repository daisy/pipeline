package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.job.impl.URITranslatorHelper;
import org.daisy.pipeline.job.impl.XProcDecorator;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class XProcDecoratorTest   {
	URIMapper mapper;
	String testFile="dir/myfile.xml";
	String testFile2="dir/myfile2.xml";
	String testDir="dir";
	@Before
	public void setUp() throws IOException {

		URI tmp=new File(System.getProperty("java.io.tmpdir")).toURI();
		mapper = new URIMapper(tmp.resolve("inputs/"),tmp.resolve("outputs/"));
	}

	@Test
	public void testResolveInputPorts() throws IOException {
		//inputs from the script definition
		XProcScript mscript= new Mock.ScriptGenerator.Builder().withInputs(1).build().generate();
		Supplier<Source> srcProv= Mock.getSourceProvider(testFile);
		Supplier<Source> srcProv2= Mock.getSourceProvider(testFile2);
		//adding a value to the input option
		String optName=Mock.ScriptGenerator.getInputName(0);
		XProcInput input = new XProcInput.Builder().
				withInput(optName, srcProv).withInput(optName, srcProv2).build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript,mapper);
		trans.decorateInputPorts(mscript,input,builder);

		XProcInput newInput = builder.build();
		List<Supplier<Source>> providers = Lists.newLinkedList(newInput.getInputs(optName));
		URI res1 = URI.create(providers.get(0).get().getSystemId());
		URI expected=URI.create(mapper.getInputBase().toString()+testFile);
		Assert.assertEquals(res1,expected);

		URI res2 = URI.create(providers.get(1).get().getSystemId());
		URI expected2=URI.create(mapper.getInputBase().toString()+testFile2);
		Assert.assertEquals(res2,expected2);
	}
	
	@Test
	public void testResolveInputPortGenerated() throws IOException {
		//inputs from the script definition
		XProcScript mscript= new Mock.ScriptGenerator.Builder().withInputs(1).build().generate();
		Supplier<Source> srcProv= Mock.getSourceProvider(null);
		Supplier<Source> srcProv2= Mock.getSourceProvider(null);
		//adding a value to the input option
		String optName=Mock.ScriptGenerator.getInputName(0);
		XProcInput input = new XProcInput.Builder().
				withInput(optName, srcProv).withInput(optName, srcProv2).build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript,mapper);
		trans.decorateInputPorts(mscript,input,builder);

		XProcInput newInput = builder.build();
		List<Supplier<Source>> providers = Lists.newLinkedList(newInput.getInputs(optName));
		URI res1 = URI.create(providers.get(0).get().getSystemId());
		URI expected=URI.create(mapper.getInputBase()+optName+"-0.xml");
		Assert.assertEquals(res1,expected);

		URI res2 = URI.create(providers.get(1).get().getSystemId());
		URI expected2=URI.create(mapper.getInputBase()+optName+"-1.xml");
		Assert.assertEquals(res2,expected2);

	}

	//test space errors doesnt make much sense now that we accept multiple uris with 
	//a separator. That means that suspected uris have to be checked before comming into the framework
	
       /* @Test(expected=RuntimeException.class)*/
	//public void testResolveInputPortURIError() throws IOException {
		////inputs from the script definition
		//XProcScript mscript= new Mock.ScriptGenerator.Builder().withInputs(1).build().generate();
		//Provider<Source> srcProv= Mock.getSourceProvider("with space.xml");
		////adding a value to the input option
		//String optName=Mock.ScriptGenerator.getInputName(0);
		//XProcInput input = new XProcInput.Builder().
				//withInput(optName, srcProv).build();

		//XProcInput.Builder builder = new XProcInput.Builder();
		//XProcDecorator trans=XProcDecorator.from(mscript,mapper);
		//trans.decorateInputPorts(mscript,input,builder);

       /* }*/

	@Test
	public void testResolveOptionsInput() throws IOException {
		//inputs from the script definition
		XProcScript mscript= new Mock.ScriptGenerator.Builder().withOptionInputs(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		//adding a value to the input option
		QName optName=Mock.ScriptGenerator.getOptionInputName(0);
		XProcInput input = new XProcInput.Builder()
				.withOption(optName, testFile).build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript,mapper);
		trans.decorateInputOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();
		URI res1 = URI.create((String)newInput.getOptions().get(optName));
		URI expected=URI.create(mapper.getInputBase()+testFile);
		Assert.assertEquals(res1,expected);
	}

	@Test
	public void testResolveOptionsInputSequence() throws IOException {
		//inputs from the script definition
		XProcScript mscript= new Mock.ScriptGenerator.Builder().withOptionInputs(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(mscript.getXProcPipelineInfo().getOptions());
		//adding a value to the input option
		QName optName=Mock.ScriptGenerator.getOptionInputName(0);
		XProcInput input = new XProcInput.Builder()
				.withOption(optName, testFile+XProcOptionMetadata.DEFAULT_SEPARATOR+testFile2).build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript,mapper);
		trans.decorateInputOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();
		String res= (String)newInput.getOptions().get(optName);
		String expected1=URI.create(mapper.getInputBase()+testFile).toString();
		String expected2=URI.create(mapper.getInputBase()+testFile2).toString();
		Assert.assertEquals(res,expected1+XProcOptionMetadata.DEFAULT_SEPARATOR+expected2);
	}
	//@Test(expected=RuntimeException.class)
	//public void testResolveOptionsInputURIError() throws IOException {
		////inputs from the script definition

		//XProcScript script= new Mock.ScriptGenerator.Builder().withOptionInputs(1).build().generate();
		//Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		////spaces makes uris sad
		//String testFile="dir/my file.xml";
		//QName optName=Mock.ScriptGenerator.getOptionInputName(0);
		////adding a value to the input option
		//XProcInput input = new XProcInput.Builder()
				//.withOption(optName,testFile).build();

		//XProcInput.Builder builder = new XProcInput.Builder();
		//XProcDecorator trans=XProcDecorator.from(script,mapper);
		//trans.decorateInputOptions(optionInfos,input,builder);

	//}

	@Test
	public void testResolveOptionsInputEmpty() throws IOException {
		//it should just ignore them, rite?

		XProcScript script= new Mock.ScriptGenerator.Builder().withOptionInputs(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		//no settings for the input
		QName optName=Mock.ScriptGenerator.getOptionInputName(0);
		XProcInput input = new XProcInput.Builder().build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		trans.decorateInputOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();

		Assert.assertEquals("",newInput.getOptions().get(optName));
	}

	@Test
	public void testResolveOptionsOutputsCopy() throws IOException {

		XProcScript script= new Mock.ScriptGenerator.Builder().withOptionOther(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		QName optName=Mock.ScriptGenerator.getRegularOptionName(0);
		//adding a value to the input option
		XProcInput input = new XProcInput.Builder().withOption(optName,"cosa").build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		trans.copyOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();

		Assert.assertEquals("cosa",newInput.getOptions().get(optName));
	}

	@Test
	public void testResolveOptionsOutputsFile() throws IOException {

		XProcScript script= new Mock.ScriptGenerator.Builder().withOptionOutputsFile(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		QName optName=Mock.ScriptGenerator.getOptionOutputFileName(0);
		//adding a value to the input option
		XProcInput input = new XProcInput.Builder().withOption(optName,testFile).build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		trans.decorateOutputOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();

		URI expected=URI.create(mapper.getOutputBase()+testFile);
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	@Test
	public void testResolveOptionsOutputsDir() throws IOException {

		XProcScript script= new Mock.ScriptGenerator.Builder().withOptionOutputsDir(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		QName optName=Mock.ScriptGenerator.getOptionOutputDirName(0);
		//adding a value to the input option
		XProcInput input = new XProcInput.Builder().withOption(optName,testDir).build();
		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		trans.decorateOutputOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();

		URI expected=URI.create(mapper.getOutputBase()+testDir);
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	@Test
	public void testResolveOptionsOutputsGeneratedFile() throws IOException {

		XProcScript script= new Mock.ScriptGenerator.Builder().withOptionOutputsFile(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		QName optName=Mock.ScriptGenerator.getOptionOutputFileName(0);
		//adding a value to the input option
		XProcInput input = new XProcInput.Builder().withOption(optName,"").build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		trans.decorateOutputOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();
		String generated=URITranslatorHelper.generateOptionOutput(script.getXProcPipelineInfo().getOption(optName),script);
		URI expected=URI.create(mapper.getOutputBase()+generated);
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	@Test
	public void testResolveOptionsOutputsGeneratedDir() throws IOException {

		XProcScript script= new Mock.ScriptGenerator.Builder().withOptionOutputsDir(1).build().generate();
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		QName optName=Mock.ScriptGenerator.getOptionOutputDirName(0);
		//adding a value to the input option
		XProcInput input = new XProcInput.Builder().withOption(optName,"").build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		trans.decorateOutputOptions(optionInfos,input,builder);

		XProcInput newInput = builder.build();
		String generated=URITranslatorHelper.generateOptionOutput(script.getXProcPipelineInfo().getOption(optName),script);
		URI expected=URI.create(mapper.getOutputBase()+generated);
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	//test space errors doesnt make much sense now that we accept multiple uris with 
	//a separator. That means that suspected uris have to be checked before comming into the framework
	
	//@Test(expected=RuntimeException.class)
	//public void testResolveOptionsOutputsURIError() throws IOException {

		//XProcScript script= new Mock.ScriptGenerator.Builder().withOptionOutputsFile(1).build().generate();
		//Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		//QName optName=Mock.ScriptGenerator.getOptionOutputFileName(0);
		////adding a value to the input option
		//XProcInput input = new XProcInput.Builder().withOption(optName,"with space.xml").build();

		//XProcInput.Builder builder = new XProcInput.Builder();
		//XProcDecorator trans=XProcDecorator.from(script,mapper);
		//trans.decorateOutputOptions(optionInfos,input,builder);

	//}

	/**
	 * Tests 'translateInputs'. The details are tested in the rest of the methods of this class/
	 * This test is just to check that we go through all the kind of options
	 *
	 * @see org.daisy.pipeline.job.impl.XProcDecorator#decorate(XProcInput)
	 */
	@Test
	public void translateInputs() throws Exception {
		XProcScript script= new Mock.ScriptGenerator.Builder().withOptionInputs(1).withOptionOther(1).withOptionOutputsDir(1).withOptionOutputsFile(1).build().generate();
		QName optIn      = Mock.ScriptGenerator.getOptionInputName(0);
		QName optReg     = Mock.ScriptGenerator.getRegularOptionName(0);
		QName optOutFile = Mock.ScriptGenerator.getOptionOutputFileName(0);
		QName optOutDir  = Mock.ScriptGenerator.getOptionOutputDirName(0);

		XProcInput input = new XProcInput.Builder()
			.withOption(optIn,"dir/input.xml")
			.withOption(optReg,"value")
			.withOption(optOutFile,"dir/output.xml")
			.withOption(optOutDir,"outs")
			.build();

		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcInput iTrans=trans.decorate(input);

		Assert.assertEquals(iTrans.getOptions().get(optIn),mapper.getInputBase()+"dir/input.xml");
		Assert.assertEquals(iTrans.getOptions().get(optOutFile),mapper.getOutputBase()+"dir/output.xml");
		Assert.assertEquals(iTrans.getOptions().get(optOutDir),mapper.getOutputBase()+"outs");
		Assert.assertEquals(iTrans.getOptions().get(optReg),"value");

	}

	@Test 
	public void ouputPortFile() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/file.xml")).build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+"dir/file.xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());


	}

	@Test 
	public void ouputSeqPortFiles() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/file.xml")).build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected2=(mapper.getOutputBase()+"dir/file-1.xml");
		
		//Assert.assertEquals(expected.toString(),res.provide().getSystemId());
		//discard one
		res.get();
		Assert.assertEquals(expected2.toString(),res.get().getSystemId());

	}

	@Test 
	public void ouputPortDir() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/")).build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+"dir/"+outName+".xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputSeqPortDir() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/")).build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+"dir/"+outName+"-1.xml");
		//discard first
		res.get();
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputPortEmptyString() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,null).build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+".xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputPortEmptyNull() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+".xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputSeqPortEmptyNull() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+"-1.xml");
		res.get();	
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputPortSeqEmptyString() throws Exception{
		XProcScript script= new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,null).build();
		XProcDecorator trans=XProcDecorator.from(script,mapper);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+"-1.xml");
		res.get();	
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}




}
