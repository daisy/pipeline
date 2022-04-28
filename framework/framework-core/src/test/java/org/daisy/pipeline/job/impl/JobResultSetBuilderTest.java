package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.JobURIUtils;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.XProcScript;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class JobResultSetBuilderTest {
        URIMapper mapper;       
        XProcScript script;
        JobResultSet.Builder builder ;
        XProcOutput output;
        XProcInput input;
        String sysId="dir/file.xml";
        String dir="option/";
        String oldIoBase="";
        BoundXProcScript bound;
        @Before
        public void setUp() throws IOException{
                script= new Mock.ScriptGenerator.Builder().withOutputPorts(2).withOptionOutputsFile(1).withOptionOutputsDir(1).build().generate();
                URI tmp=new File(System.getProperty("java.io.tmpdir")).toURI();
                oldIoBase=System.getProperty("org.daisy.pipeline.data");
                System.setProperty("org.daisy.pipeline.data", new File(tmp).toString());
                mapper = new URIMapper(tmp.resolve("inputs/"),tmp.resolve("outputs/"));
                builder = new JobResultSet.Builder();

                String outName = Mock.ScriptGenerator.getOutputName(0);
                XProcOutput outs = new XProcOutput.Builder().withOutput(outName,Mock.getResultProvider(sysId)).build();
                XProcDecorator trans=XProcDecorator.from(script,mapper);
                output=trans.decorate(outs);

                QName optDir=Mock.ScriptGenerator.getOptionOutputDirName(0);
                QName optName=Mock.ScriptGenerator.getOptionOutputFileName(0);
                input = new XProcInput.Builder().withOption(optDir,dir).withOption(optName,sysId).build();
                input=trans.decorate(input);
                Mock.populateDir((String)input.getOptions().get(optDir));
                bound=BoundXProcScript.from(script,input,output);
        }


        @After
        public void tearDown() {
                QName optDir=Mock.ScriptGenerator.getOptionOutputDirName(0);
                IOHelper.deleteDir(new File((String)input.getOptions().get(optDir)));
                if(oldIoBase!=null)
                        System.setProperty("org.daisy.pipeline.data", oldIoBase);
                                
        }

        @Test 
        public void ouputPort() throws Exception{

                String outName = Mock.ScriptGenerator.getOutputName(0);
                Supplier<Result> res=output.getResultProvider(outName);
                res.get();
                
                JobResultSetBuilder.collectOutputs(script,output,mapper,builder);
                JobResultSet rSet=builder.build();
                List<JobResult> jobs=Lists.newLinkedList(rSet.getResults(outName));
                Assert.assertEquals(mapper.mapOutput(URI.create(sysId)),jobs.get(0).getPath());
                Assert.assertEquals(sysId,jobs.get(0).getIdx().toString());

        }

        @Test 
        public void ouputPortNullCheck() throws Exception{

                String outName = Mock.ScriptGenerator.getOutputName(0);
                XProcOutput output = new XProcOutput.Builder().build();
                
                JobResultSetBuilder.collectOutputs(script,output,mapper,builder);
                JobResultSet rSet=builder.build();
                List<JobResult> jobs=Lists.newLinkedList(rSet.getResults(outName));
                Assert.assertEquals(jobs.size(),0);

        }

        @Test 
        public void ouputPortSequence() throws Exception{

                String outName = Mock.ScriptGenerator.getOutputName(1);
                Supplier<Result> res=output.getResultProvider(outName);
                res.get();
                res.get();
                
                JobResultSetBuilder.collectOutputs(script,output,mapper,builder);
                JobResultSet rSet=builder.build();
                List<JobResult> jobs=Lists.newLinkedList(rSet.getResults(outName));
                Assert.assertEquals(jobs.size(),2);

        }

        @Test
        public void dynamicProviderResults() throws Exception{

                String outName = Mock.ScriptGenerator.getOutputName(0);
                DynamicResultProvider res=(DynamicResultProvider) output.getResultProvider(outName);
                res.get();
                res.get();
                List<JobResult> jobs=JobResultSetBuilder.buildJobResult(res,mapper,"xml");
                Assert.assertEquals(jobs.size(),2);
                Assert.assertEquals(mapper.mapOutput(URI.create(sysId)),jobs.get(0).getPath());
                Assert.assertEquals(sysId,jobs.get(0).getIdx().toString());
                Assert.assertEquals("xml",jobs.get(0).getMediaType());
                
        }

        @Test(expected=IllegalArgumentException.class)
        public void nonDynamicProviderResults() throws Exception{
                String outName = Mock.ScriptGenerator.getOutputName(0);
                // undecorated output
                XProcOutput output = new XProcOutput.Builder().withOutput(outName, Mock.getResultProvider(sysId)).build();
                Supplier<Result> res= output.getResultProvider(outName);
                List<JobResult> jobs=JobResultSetBuilder.buildJobResult(res,mapper,"xml");
                Assert.assertEquals(mapper.mapOutput(URI.create(sysId)),jobs.get(0).getPath());
                Assert.assertEquals(sysId,jobs.get(0).getIdx().toString());
                Assert.assertEquals("xml",jobs.get(0).getMediaType());
                
        }

        @Test
        public void optionsOutputFile() throws Exception{
                QName optName=Mock.ScriptGenerator.getOptionOutputFileName(0);
                JobResultSetBuilder.collectOptions(script,input,mapper,builder);
                JobResultSet rSet=builder.build();
                List<JobResult> jobs=Lists.newLinkedList(rSet.getResults(optName));
                Assert.assertEquals(mapper.mapOutput(URI.create(sysId)),jobs.get(0).getPath());
                Assert.assertEquals(sysId,jobs.get(0).getIdx().toString());
                
        }

        @Test
        public void optionsOutputDirSize() throws Exception{
                QName optName=Mock.ScriptGenerator.getOptionOutputDirName(0);
                JobResultSetBuilder.collectOptions(script,input,mapper,builder);
                JobResultSet rSet=builder.build();
                List<JobResult> jobs=Lists.newLinkedList(rSet.getResults(optName));
                Assert.assertEquals(3,jobs.size());

                
        }

        @Test
        public void optionsOutputURIS() throws Exception{
                QName optName=Mock.ScriptGenerator.getOptionOutputDirName(0);
                JobResultSetBuilder.collectOptions(script,input,mapper,builder);
                JobResultSet rSet=builder.build();
                List<JobResult> jobs=Lists.newLinkedList(rSet.getResults(optName));
                HashSet<URI> uris= new HashSet<URI>();
                uris.add(mapper.mapOutput(URI.create(dir+"dos.xml")));
                uris.add(mapper.mapOutput(URI.create(dir+"uno.xml")));
                uris.add(mapper.mapOutput(URI.create(dir+"tres.xml")));

                Assert.assertTrue(uris.contains(jobs.get(0).getPath()));
                Assert.assertTrue(uris.contains(jobs.get(1).getPath()));
                Assert.assertTrue(uris.contains(jobs.get(2).getPath()));

                
        }

        @Test
        public void optionsOutputIdx() throws Exception{
                QName optName=Mock.ScriptGenerator.getOptionOutputDirName(0);
                JobResultSetBuilder.collectOptions(script,input,mapper,builder);
                JobResultSet rSet=builder.build();
                List<JobResult> jobs=Lists.newLinkedList(rSet.getResults(optName));
                HashSet<String> uris= new HashSet<String>();
                uris.add(dir+"dos.xml");
                uris.add(dir+"uno.xml");
                uris.add(dir+"tres.xml");

                Assert.assertTrue(uris.contains(jobs.get(0).getIdx().toString()));
                Assert.assertTrue(uris.contains(jobs.get(1).getIdx().toString()));
                Assert.assertTrue(uris.contains(jobs.get(2).getIdx().toString()));

                
        }

        @Test
        public void newResultSet() throws Exception{
                String outName = Mock.ScriptGenerator.getOutputName(0);
                Supplier<Result> res=output.getResultProvider(outName);
                res.get();
                JobResultSet rSet=JobResultSetBuilder.newResultSet(script, input, output, mapper);
                Assert.assertEquals(5,rSet.getResults().size());
        }
        @Test
        public void getSize() throws Exception{
                //create tmp file
                File tmp=null;
                try{
                        tmp= File.createTempFile("dp2_test",".txt");
                        char[] data = new char[1024];
                        FileWriter fw =new FileWriter(tmp); 
                        fw.write(data);
                        fw.close();
                        //write 1024 bytes
                        //get size == 1024
                        JobResult res = JobResultSetBuilder.singleResult(tmp.toURI(),new URIMapper(URI.create(""),URI.create("")),"");

                        Assert.assertEquals("wrong result size",1024l,res.getSize());
                }catch(Exception e){
                        throw e;
                }finally{
                        if (tmp!=null){
                                tmp.delete();
                        }
                }
        }
        @Test(expected = RuntimeException.class) 
        public void getSizeNonExistingFile() throws Exception{
                        URL fake= new URL("file:/Idontexist.txt");
                        JobResult res = JobResultSetBuilder.singleResult(fake.toURI(),new URIMapper(URI.create(""),URI.create("")),"");
                        res.getSize();
        }
}
