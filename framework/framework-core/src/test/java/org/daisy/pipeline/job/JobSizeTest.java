package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class JobSizeTest   {


        String oldIoBase="";
        JobId id;
        String logTxt="This is a log";//
        String inputTxt="Bacon ipsum dolor sit amet pastrami swine andouille short loin pork chop tongue ham beef ribs leberkas prosciutto";
        File contextDir;
        File outputDir;
        

        @Before
        public void setUp() throws IOException{
                URI tmp=new File(System.getProperty("java.io.tmpdir")).toURI();
                oldIoBase = System.getProperty("org.daisy.pipeline.data");
                System.setProperty("org.daisy.pipeline.data", Files.createTempDir().toString());
                id=JobIdFactory.newId();
                //create fake data
                //log
                File log = JobURIUtils.getLogFile(id.toString());
                Files.createParentDirs(log);
                Files.write(logTxt.getBytes(),log);
                //input
                contextDir= JobURIUtils.getJobContextDir(id.toString());
                File input1=new File(contextDir,"input1.txt");
                File input2=new File(new File(contextDir,"folder"),"input2.txt");
                Files.createParentDirs(input2);
                Files.write(inputTxt.getBytes(),input1);
                Files.write(inputTxt.getBytes(),input2);
                //output
                outputDir= JobURIUtils.getJobOutputDir(id.toString());
                File output=new File(new File(outputDir,"folder"),"outout.txt");
                Files.createParentDirs(output);
                Files.write(inputTxt.getBytes(),output);

        }

        @After
        public void tearDown() {
                if(oldIoBase!=null){
                        for (File f : Files.fileTreeTraverser().postOrderTraversal(new File("org.daisy.pipeline.data"))) {
                                f.delete();
                        }
                        System.setProperty("org.daisy.pipeline.data", oldIoBase);
                }
        }

        @Test
        public void testLogSize(){
               long res=JobSize.getLogSize(id);
               Assert.assertEquals("Job log size ",(long)logTxt.getBytes().length,res);
               Assert.assertEquals("Job non existing file size ",0,JobSize.getLogSize(JobIdFactory.newId()));
        }

        @Test
        public void testContextSize() throws IOException {
               long res=JobSize.getContextSize(id);
               Assert.assertEquals("JobsSizeCalculartorulate directory size",(long)inputTxt.getBytes().length*2,res);
        }

        @Test
        public void testOutputSize() throws IOException {
               long res=JobSize.getOutputSize(id);
               Assert.assertEquals("JobsSizeCalculartorulate directory size",(long)inputTxt.getBytes().length,res);
        }
        @Test
        public void testDirSize(){
               long res=JobSize.getDirSize(contextDir);
               Assert.assertEquals("JobsSizeCalculartorulate directory size",(long)inputTxt.getBytes().length*2,res);
        }

        @Test
        public void getSum(){
               JobSize size=new JobSize(null,1,2,3);
               Assert.assertEquals("size sum",6,size.getSum());
        }

        @Test
        public void getTotal(){
               JobSize size=new JobSize(null,1,2,3);
               LinkedList<JobSize> list=new LinkedList<JobSize>();
               list.add(size);
               list.add(size);
               list.add(size);
               Assert.assertEquals("size sum",18,JobSize.getTotal(list));
        }
}
