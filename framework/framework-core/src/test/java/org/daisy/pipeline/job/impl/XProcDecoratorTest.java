package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.Source;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.xml.sax.InputSource;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class XProcDecoratorTest   {
	URIMapper mapper;
	String testFile="dir/myfile.xml";
	String testFile2="dir/myfile2.xml";
	JobResources resources = new JobResources() {
			public Iterable<String> getNames() {
				return Lists.newArrayList(testFile, testFile2);
			}
			public Supplier<InputStream> getResource(String name) {
				throw new UnsupportedOperationException();
			}};
	String testDir="dir";
	@Before
	public void setUp() throws IOException {

		URI tmp=new File(System.getProperty("java.io.tmpdir")).toURI();
		mapper = new URIMapper(tmp.resolve("inputs/"),tmp.resolve("outputs/"));
	}

	@Test
	public void notEmpty() {
		Assert.assertTrue(XProcDecorator.notEmpty("hola"));
		Assert.assertFalse(XProcDecorator.notEmpty(""));
		Assert.assertFalse(XProcDecorator.notEmpty("''"));
		Assert.assertFalse(XProcDecorator.notEmpty("\"\""));
		Assert.assertFalse(XProcDecorator.notEmpty(null));
	}

	@Test
	public void testResolveInputPorts() throws IOException {
		//inputs from the script definition
		XProcScript mscript = new Mock.ScriptGenerator.Builder().withInputs(1).build().generate();
		Source src= Mock.getSource(testFile);
		Source src2= Mock.getSource(testFile2);
		//adding a value to the input option
		String optName=Mock.ScriptGenerator.getInputName(0);
		ScriptInput input = new ScriptInput.Builder(resources)
			.withInput(optName, src)
			.withInput(optName, src2)
			.build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript, mapper, null);
		trans.decorateInputPorts(mscript,input,builder);

		XProcInput newInput = builder.build();
		List<Supplier<Source>> sources = Lists.newLinkedList(newInput.getInputs(optName));
		URI res1 = URI.create(sources.get(0).get().getSystemId());
		URI expected=URI.create(mapper.getInputBase().toString()+testFile);
		Assert.assertEquals(res1,expected);

		URI res2 = URI.create(sources.get(1).get().getSystemId());
		URI expected2=URI.create(mapper.getInputBase().toString()+testFile2);
		Assert.assertEquals(res2,expected2);
	}
	
	@Test
	public void testResolveInputPortGenerated() throws IOException {
		//inputs from the script definition
		XProcScript mscript = new Mock.ScriptGenerator.Builder().withInputs(1).build().generate();
		//adding a value to the input option
		String optName=Mock.ScriptGenerator.getInputName(0);
		ScriptInput input = new ScriptInput.Builder()
			.withInput(optName, new SAXSource(new InputSource(new StringReader("foo"))))
			.withInput(optName, new SAXSource(new InputSource(new StringReader("bar"))))
			.build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript, mapper, null);
		trans.decorateInputPorts(mscript,input,builder);

		XProcInput newInput = builder.build();
		List<Supplier<Source>> sources = Lists.newLinkedList(newInput.getInputs(optName));
		URI res1 = URI.create(sources.get(0).get().getSystemId());
		URI expected=URI.create(mapper.getInputBase()+optName+"-0.xml");
		Assert.assertEquals(res1,expected);

		URI res2 = URI.create(sources.get(1).get().getSystemId());
		URI expected2=URI.create(mapper.getInputBase()+optName+"-1.xml");
		Assert.assertEquals(res2,expected2);

	}

	@Test
	public void testResolveOptionsInput() throws IOException {
		//inputs from the script definition
		XProcScript mscript = new Mock.ScriptGenerator.Builder().withOptionInputs(1).build().generate();
		//adding a value to the input option
		QName optName=Mock.ScriptGenerator.getOptionInputName(0);
		ScriptInput input = new ScriptInput.Builder(resources).withInput(optName.getLocalPart(), Mock.getSource(testFile)).build();
		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript, mapper, null);
		trans.decorateInputOptions(mscript, input, builder);

		XProcInput newInput = builder.build();
		URI res1 = URI.create((String)newInput.getOptions().get(optName));
		URI expected=URI.create(mapper.getInputBase()+testFile);
		Assert.assertEquals(expected, res1);
	}

	@Test
	public void testResolveOptionsInputSequence() throws IOException {
		//inputs from the script definition
		XProcScript mscript = new Mock.ScriptGenerator.Builder().withOptionInputs(1).build().generate();
		//adding a value to the input option
		QName optName=Mock.ScriptGenerator.getOptionInputName(0);
		ScriptInput input = new ScriptInput.Builder(resources)
			.withInput(optName.getLocalPart(), Mock.getSource(testFile))
			.withInput(optName.getLocalPart(), Mock.getSource(testFile2))
			.build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(mscript, mapper, null);
		trans.decorateInputOptions(mscript, input, builder);

		XProcInput newInput = builder.build();
		String res= (String)newInput.getOptions().get(optName);
		String expected1=URI.create(mapper.getInputBase()+testFile).toString();
		String expected2=URI.create(mapper.getInputBase()+testFile2).toString();
		Assert.assertEquals(res, expected1 + XProcOptionMetadata.DEFAULT_SEPARATOR + expected2);
	}

	@Test
	public void testResolveOptionsInputEmpty() throws IOException {
		//it should just ignore them, rite?

		XProcScript script = new Mock.ScriptGenerator.Builder().withOptionInputs(1).build().generate();
		//no settings for the input
		QName optName=Mock.ScriptGenerator.getOptionInputName(0);
		ScriptInput input = new ScriptInput.Builder().build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		trans.decorateInputOptions(script, input, builder);

		XProcInput newInput = builder.build();

		Assert.assertEquals("",newInput.getOptions().get(optName));
	}

	@Test
	public void testResolveOptionsOutputsCopy() throws IOException {

		XProcScript script = new Mock.ScriptGenerator.Builder().withOptionOther(1).build().generate();
		QName optName=Mock.ScriptGenerator.getRegularOptionName(0);
		//adding a value to the input option
		ScriptInput input = new ScriptInput.Builder().withOption(optName.getLocalPart(), "cosa").build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		trans.decorateInputOptions(script, input, builder);

		XProcInput newInput = builder.build();

		Assert.assertEquals("cosa",newInput.getOptions().get(optName));
	}

	@Test
	public void testResolveOptionsOutputsFile() throws IOException {

		XProcScript script = new Mock.ScriptGenerator.Builder().withOptionOutputsFile(1).build().generate();
		QName optName=Mock.ScriptGenerator.getOptionOutputFileName(0);
		// adding a value to the input does not have an effect
		ScriptInput input = new ScriptInput.Builder().withOption(optName.getLocalPart(), testFile).build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		trans.decorateOutputOptions(script, input, builder);

		XProcInput newInput = builder.build();

		URI expected = URI.create(mapper.getOutputBase() + "option-output-file-0.xml");
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	@Test
	public void testResolveOptionsOutputsDir() throws IOException {

		XProcScript script = new Mock.ScriptGenerator.Builder().withOptionOutputsDir(1).build().generate();
		QName optName=Mock.ScriptGenerator.getOptionOutputDirName(0);
		// adding a value to the input does not have an effect
		ScriptInput input = new ScriptInput.Builder().withOption(optName.getLocalPart(), testDir).build();
		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		trans.decorateOutputOptions(script, input, builder);

		XProcInput newInput = builder.build();

		URI expected = URI.create(mapper.getOutputBase() + "option-output-dir-0/");
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	@Test
	public void testResolveOptionsOutputsGeneratedFile() throws IOException {

		XProcScript script = new Mock.ScriptGenerator.Builder().withOptionOutputsFile(1).build().generate();
		QName optName=Mock.ScriptGenerator.getOptionOutputFileName(0);
		//adding a value to the input option
		ScriptInput input = new ScriptInput.Builder().withOption(optName.getLocalPart(), "").build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		trans.decorateOutputOptions(script, input, builder);

		XProcInput newInput = builder.build();
		URI expected = URI.create(mapper.getOutputBase() + "option-output-file-0.xml");
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	@Test
	public void testResolveOptionsOutputsGeneratedDir() throws IOException {

		XProcScript script = new Mock.ScriptGenerator.Builder().withOptionOutputsDir(1).build().generate();
		QName optName=Mock.ScriptGenerator.getOptionOutputDirName(0);
		//adding a value to the input option
		ScriptInput input = new ScriptInput.Builder().withOption(optName.getLocalPart(), "").build();

		XProcInput.Builder builder = new XProcInput.Builder();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		trans.decorateOutputOptions(script, input, builder);

		XProcInput newInput = builder.build();
		URI expected = URI.create(mapper.getOutputBase() + "option-output-dir-0/");
		URI reslut=URI.create( (String)newInput.getOptions().get(optName) );
		Assert.assertEquals(expected,reslut);
	}

	/**
	 * Tests 'translateInputs'. The details are tested in the rest of the methods of this class/
	 * This test is just to check that we go through all the kind of options
	 *
	 * @see org.daisy.pipeline.job.impl.XProcDecorator#decorate(ScriptInput)
	 */
	@Test
	public void translateInputs() throws Exception {
		XProcScript script = new Mock.ScriptGenerator.Builder().withOptionInputs(1).withOptionOther(1).withOptionOutputsDir(1).withOptionOutputsFile(1).build().generate();
		QName optIn      = Mock.ScriptGenerator.getOptionInputName(0);
		QName optReg     = Mock.ScriptGenerator.getRegularOptionName(0);
		QName optOutFile = Mock.ScriptGenerator.getOptionOutputFileName(0);
		QName optOutDir  = Mock.ScriptGenerator.getOptionOutputDirName(0);

		ScriptInput input = new ScriptInput.Builder(resources)
			.withInput(optIn.getLocalPart(), Mock.getSource(testFile))
			.withOption(optReg.getLocalPart(), "value")
			.withOption(optOutFile.getLocalPart(), "dir/output.xml") // no effect
			.withOption(optOutDir.getLocalPart(), "outs")            // no effect
			.build();

		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcInput iTrans=trans.decorate(input);

		Assert.assertEquals(iTrans.getOptions().get(optIn), mapper.getInputBase() + testFile);
		Assert.assertEquals(iTrans.getOptions().get(optOutFile), mapper.getOutputBase() + "option-output-file-0.xml");
		Assert.assertEquals(iTrans.getOptions().get(optOutDir), mapper.getOutputBase() + "option-output-dir-0/");
		Assert.assertEquals(iTrans.getOptions().get(optReg),"value");

	}

	@Test 
	public void ouputPortFile() throws Exception{
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/file.xml")).build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+"dir/file.xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());


	}

	@Test 
	public void ouputSeqPortFiles() throws Exception{
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/file.xml")).build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
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
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/")).build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+"dir/"+outName+".xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputSeqPortDir() throws Exception{
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider("dir/")).build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+"dir/"+outName+"-1.xml");
		//discard first
		res.get();
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputPortEmptyString() throws Exception{
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,null).build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+".xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputPortEmptyNull() throws Exception{
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+".xml");
		
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputSeqPortEmptyNull() throws Exception{
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+"-1.xml");
		res.get();	
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}

	@Test 
	public void ouputPortSeqEmptyString() throws Exception{
		XProcScript script = new Mock.ScriptGenerator.Builder().withOutputPorts(1).build().generate();
		String outName = Mock.ScriptGenerator.getOutputName(0);

		XProcOutput outs = new XProcOutput.Builder().withOutput(outName,null).build();
		XProcDecorator trans=XProcDecorator.from(script, mapper, null);
		XProcOutput decorated=trans.decorate(outs);
		
		Supplier<Result> res=decorated.getResultProvider(outName);
		String expected=(mapper.getOutputBase()+outName+"/"+outName+"-1.xml");
		res.get();	
		Assert.assertEquals(expected.toString(),res.get().getSystemId());
	}
}
