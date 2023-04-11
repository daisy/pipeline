package org.daisy.pipeline.job;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStreamWriter;

import javax.xml.transform.stream.StreamResult;

import org.daisy.common.xproc.XProcOutput;
import org.daisy.pipeline.job.impl.Mock;
import org.daisy.pipeline.job.impl.XProcDecorator;
import org.daisy.pipeline.script.XProcScript;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StatusPortTest {

        private static final String XML_ERR = "<d:status xmlns:d='http://www.daisy.org/ns/pipeline/data' result='error'/>";
        private static final String XML_OK = "<d:status xmlns:d='http://www.daisy.org/ns/pipeline/data' result='ok'/>";
        private static final String XML_INVALID = "<d:status xmlns:d='http://www.daisy.org/ns/pipeline/data' invalid='ok'/>";
        private static final String MEDIA_TYPE_STATUS_XML = "application/vnd.pipeline.status+xml";

        XProcScript script;
        XProcOutput outputs;

        @Before
        public void setUp() throws IOException {
                script= new Mock.ScriptGenerator.Builder()
                        .withOutputPort("status", MEDIA_TYPE_STATUS_XML, true, false)
                        .build().generate();
                outputs = XProcDecorator.from(script, null).decorate(new XProcOutput.Builder().build());
        }

        void writeStatus(String statusXml) throws IOException, UnsupportedEncodingException {
                OutputStreamWriter statusWriter = new OutputStreamWriter(
                        ((StreamResult)outputs.getResultProvider("status").get()).getOutputStream(), "UTF-8");
                statusWriter.write(statusXml);
                statusWriter.close();
        }

        @Test
        public void withoutStatusPort() {
                boolean ok = AbstractJob.checkStatusPort(script, outputs);
                Assert.assertTrue("should return true when no status port is present",ok);
        }
        
        @Test
        public void statusPortOk() throws IOException, UnsupportedEncodingException {
                writeStatus(XML_OK);
                boolean ok = AbstractJob.checkStatusPort(script, outputs);
                Assert.assertTrue("should return true when status document says 'ok'",ok);
        }

        @Test
        public void statusPortError() throws IOException, UnsupportedEncodingException {
                writeStatus(XML_ERR);
                boolean ok = AbstractJob.checkStatusPort(script, outputs);
                Assert.assertFalse("should return false when status document says 'error'",ok);
        }

        @Test(expected =RuntimeException.class)
        public void invalidStatusXml() throws IOException, UnsupportedEncodingException {
                writeStatus(XML_INVALID);
                AbstractJob.checkStatusPort(script, outputs);
        }
        @Test
        public void multipleStatusOk() throws IOException, UnsupportedEncodingException {
                writeStatus(XML_OK);
                writeStatus(XML_OK);
                boolean ok = AbstractJob.checkStatusPort(script, outputs);
                Assert.assertTrue("should return true if all status documents say 'ok'",ok);
        }
        @Test
        public void multipleStatusErr() throws IOException, UnsupportedEncodingException {
                writeStatus(XML_OK);
                writeStatus(XML_ERR);
                boolean ok = AbstractJob.checkStatusPort(script, outputs);
                Assert.assertFalse("should return false if at least one status documents say 'error'",ok);
        }
}
