package org.daisy.pipeline.job;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JobResultTest {

        private static File resultFile;

        @BeforeClass
        public static void setUp() throws IOException {
                resultFile = File.createTempFile("res", null);
                char[] data = new char[1024];
                FileWriter fw = new FileWriter(resultFile);
                fw.write(data);
                fw.close();
        }

        @AfterClass
        public static void tearDown() throws IOException {
                resultFile.delete();
        }

        @Test
        public void stripPrefix() {
                URI path = URI.create("mything/file.xml");
                URI pathStripped = URI.create("file.xml");
                Assert.assertEquals(pathStripped,
                                    new JobResult(path, resultFile, null).strip().getPath());
        }

        @Test
        public void getSize() throws Exception {
                JobResult res = new JobResult(resultFile.toURI(), resultFile, null);
                Assert.assertEquals("wrong result size", 1024l, res.getSize());
        }

        @Test(expected = RuntimeException.class)
        public void creatJobResultFromNonExistingFile() throws Exception {
            File fake = new File("/Idontexist.txt");
            new JobResult(fake.toURI(), fake, null);
        }
}
