package org.daisy.pipeline.job;

import org.junit.Assert;
import org.junit.Test;

public class IndexTest {

        @Test
        public void stripPrefix() throws Exception {
                String idx="mything/file.xml";
                String idxStripped="file.xml";
                Assert.assertEquals("The stripped index is ok",new Index(idx).stripPrefix().toString(),idxStripped);
                
        }
        
}
