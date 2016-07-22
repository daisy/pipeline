package org.daisy.pipeline.updater;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;

public class OutputParserTest {

        @Test
        public void testNext() throws Exception {
                
        }
        @Test
        public void testParseLineInfo() throws Exception {
                String info="[INFO] This is an info message";
                OutputParser.LogLine ll =new OutputParser(null,null).parseLine(info);
                Assert.assertFalse("It is not gibberish",ll.gibberish);
                Assert.assertEquals("The level is info",ll.level,"INFO");
                Assert.assertEquals("The message is captured",ll.message,"This is an info message");
                
        }
        @Test
        public void testPaseLineError() throws Exception {
                String error="[ERROR] This is an error message";
                OutputParser.LogLine ll =new OutputParser(null,null).parseLine(error);
                Assert.assertFalse("It is not gibberish",ll.gibberish);
                Assert.assertEquals("The level is error",ll.level,"ERROR");
                Assert.assertEquals("The message is captured",ll.message,"This is an error message");
                
        }
        @Test
        public void testPaseLineGibbrish() throws Exception {
                String gibberish="This is a method without level";
                OutputParser.LogLine ll =new OutputParser(null,null).parseLine(gibberish);
                Assert.assertTrue("It is  gibberish",ll.gibberish);
                
        }
        
        @Test
        public void testParse() throws Exception {
                String lines="[INFO] One\n[ERROR] Two";
                InputStream is = new ByteArrayInputStream(lines.getBytes("UTF-8"));
                final StringBuffer info=new StringBuffer();
                final StringBuffer error=new StringBuffer();
                UpdaterObserver obs=new UpdaterObserver(){
                                @Override
                                public void info(String msg) {
                                        info.append(msg);

                                }
                                @Override
                                public void error(String msg) {
                                        error.append(msg);

                                }
                };
                new OutputParser(is,obs).parse();
                Assert.assertEquals("We got the info", info.toString(),"One");
                Assert.assertEquals("We got the error", error.toString(),"Two");
        }
}


