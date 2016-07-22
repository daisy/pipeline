package org.daisy.pipeline.job.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.impl.JobUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JobUtilsTest {
        private static final String XML_ERR = "<d:validation-status xmlns:d='http://www.daisy.org/ns/pipeline/data' result='error'/>";
        private static final String XML_OK = "<d:validation-status xmlns:d='http://www.daisy.org/ns/pipeline/data' result='ok'/>";
        private static final String XML_INVALID = "<d:validation-status xmlns:d='http://www.daisy.org/ns/pipeline/data' invalid='ok'/>";

        File ok;
        File err;
        File invalid;

        @Before
        public void setUp() throws IOException {
                ok=File.createTempFile("dp2test",".xml");
                err=File.createTempFile("dp2test",".xml");
                invalid=File.createTempFile("dp2test",".xml");

                FileWriter okWriter=new FileWriter(ok);
                okWriter.write(XML_OK);
                okWriter.close();

                FileWriter errWriter=new FileWriter(err);
                errWriter.write(XML_ERR);
                errWriter.close();

                FileWriter invalidWriter=new FileWriter(invalid);
                invalidWriter.write(XML_INVALID);
                invalidWriter.close();
        }

        @After
        public void tearDown(){
                ok.delete();
                err.delete();
                invalid.delete();
        }

        @Test
        public void withoutValidationPort(){
                //should return valid when no validation port is present
                JobResultSet results=new JobResultSet.Builder().build();
                boolean valid=JobUtils.checkValidPort(results);
                Assert.assertTrue("should return valid when no validation port is present",valid);
        }
        
        @Test
        public void validationPortOk(){
                //should return valid when no validation port is present
                JobResultSet results=new JobResultSet.Builder().addResult("validation-status",new JobResult.Builder().withPath(ok.toURI()).build()).build();
                boolean valid=JobUtils.checkValidPort(results);
                Assert.assertTrue("contents are ok but t'is not valid",valid);
        }

        @Test
        public void validationPortError(){
                //should return valid when no validation port is present
                JobResultSet results=new JobResultSet.Builder().addResult("validation-status",new JobResult.Builder().withPath(err.toURI()).build()).build();
                boolean valid=JobUtils.checkValidPort(results);
                Assert.assertFalse("contents are err but it's valid",valid);
        }

        @Test(expected =RuntimeException.class)
        public void invalidValidationXml(){
               //should return valid when no validation port is present
                JobResultSet results=new JobResultSet.Builder().addResult("validation-status",new JobResult.Builder().withPath(invalid.toURI()).build()).build();
                JobUtils.checkValidPort(results);
        }
        @Test
        public void multipleValidationOk(){
                
                JobResultSet results=new JobResultSet.Builder()
                        .addResult("validation-status",new JobResult.Builder().withPath(ok.toURI()).build())
                        .addResult("validation-status",new JobResult.Builder().withPath(ok.toURI()).build())
                        .build();
                boolean valid=JobUtils.checkValidPort(results);
                Assert.assertTrue("contents are ok but t'is not valid",valid);
        }
        @Test
        public void multipleValidationErr(){
                
                JobResultSet results=new JobResultSet.Builder()
                        .addResult("validation-status",new JobResult.Builder().withPath(ok.toURI()).build())
                        .addResult("validation-status",new JobResult.Builder().withPath(err.toURI()).build())
                        .build();
                boolean valid=JobUtils.checkValidPort(results);
                Assert.assertFalse("contents are err but it is valid",valid);
        }
}
