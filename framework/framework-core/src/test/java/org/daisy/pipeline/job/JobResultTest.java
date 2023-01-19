package org.daisy.pipeline.job;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.net.URL;

import org.junit.Assert;
import org.junit.Test;

public class JobResultTest {

        @Test
        public void stripPrefix() {
                String idx = "mything/file.xml";
                String idxStripped = "file.xml";
                Assert.assertEquals(idxStripped,
                                    new JobResult(idx, URI.create("foo"), null).strip().getIdx());
        }

        @Test
        public void getSize() throws Exception {
                File tmp = null;
                try {
                        tmp = File.createTempFile("dp2_test", ".txt");
                        char[] data = new char[1024];
                        FileWriter fw = new FileWriter(tmp);
                        fw.write(data);
                        fw.close();
                        JobResult res = new JobResult(tmp.toString(), tmp.toURI(), null);
                        Assert.assertEquals("wrong result size", 1024l, res.getSize());
                } finally {
                        if (tmp != null){
                                tmp.delete();
                        }
                }
        }

        @Test(expected = RuntimeException.class)
        public void getSizeNonExistingFile() throws Exception {
            URL fake = new URL("file:/Idontexist.txt");
            JobResult res = new JobResult(fake.toString(), fake.toURI(), null);
            res.getSize();
        }
}
