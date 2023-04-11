package org.daisy.pipeline.job;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

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
                String idx = "mything/file.xml";
                String idxStripped = "file.xml";
                Assert.assertEquals(idxStripped,
                                    new JobResult(idx, resultFile, null).strip().getIdx());
        }

        @Test
        public void getSize() throws Exception {
                JobResult res = new JobResult(resultFile.toString(), resultFile, null);
                Assert.assertEquals("wrong result size", 1024l, res.getSize());
        }

        @Test(expected = RuntimeException.class)
        public void creatJobResultFromNonExistingFile() throws Exception {
            File fake = new File("/Idontexist.txt");
            new JobResult(fake.toString(), fake, null);
        }
}
