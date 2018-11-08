package org.daisy.pipeline.script.impl.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;
import org.daisy.pipeline.script.impl.parser.StaxXProcScriptParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * The Class XProcScriptParserTest.
 */
public class XProcScriptParserTest {
	public static class MockScriptService extends XProcScriptService{
		public URL url;

		/**
		 * @param url
		 */
		public MockScriptService(URL url) {
			this.url = url;
		}

		@Override
		public URL getURL() {
			return this.url;
		}

		@Override
		public String toString() {
			return String.format("MockedXProcScriptService[url=%s]",this.url);
		}

	} 

	 /** The scp. */
	 XProcScript scp;
	 
	 /**
	  * Sets the up.
	  *
	  * @throws URISyntaxException the uRI syntax exception
	  */
	 @Before
	 public void setUp() throws URISyntaxException {
	 
	 
		 URL url=this.getClass().getClassLoader().getResource("script.xpl");
		 StaxXProcScriptParser parser = new StaxXProcScriptParser();
		 parser.setFactory(XMLInputFactory.newInstance());
		 scp = parser.parse(new MockScriptService(url)); //Try to fix this using a service
	 
	 
	 }
	 
	 /**
	  * Test description.
	  *
	  * @throws URISyntaxException the uRI syntax exception
	  */
	 @Test
	 public void testDescription() throws URISyntaxException {
		 assertEquals("Unit Test Script", scp.getName());
		 assertEquals("detail description", scp.getDescription());
		 assertEquals("http://example.org/unit-test-script", scp.getHomepage());
	 }
	 
	 /**
	  * Test port metadata is set.
	  */
	 @Test
	 public void testPortMetadataIsSet() {
		 for (XProcPortInfo port : scp.getXProcPipelineInfo().getInputPorts()) {
			 assertNotNull("port '" + port.getName() + "' has no metadata",
					 scp.getPortMetadata(port.getName()));
		 }
		 for (XProcPortInfo port : scp.getXProcPipelineInfo().getOutputPorts()) {
			 assertNotNull("port '" + port.getName() + "' has no metadata",
					 scp.getPortMetadata(port.getName()));
		 }
		 //TODO test parameter ports
	 }
	 
	 /**
	  * Test input port.
	  */
	 @Test
	 public void testInputPort() {
		 XProcPortMetadata port = scp.getPortMetadata("source");
		 assertEquals("application/x-dtbook+xml", port.getMediaType());
		 assertEquals("source name", port.getNiceName());
		 assertEquals("source description", port.getDescription());
	 
	 }
	 
	 @Test
	 public void testInputPortRequired() {
		 XProcPortMetadata port = scp.getPortMetadata("source");
                 assertTrue(port.isRequired());
		 port = scp.getPortMetadata("source2");
                 Assert.assertFalse(port.isRequired());
	 
	 }

	 /**
	  * Test missing input metadata.
	  */
	 @Test
	 public void testMissingInputMetadata() {
		 XProcPortMetadata meta = scp.getPortMetadata("source2");
		 assertNotNull(meta);
		 assertNull(meta.getMediaType());
		 assertNull(meta.getNiceName());
		 assertNull(meta.getDescription());
	 }
	 
	 /**
	  * Test output port.
	  */
	 @Test
	 public void testOutputPort() {
		 XProcPortMetadata port = scp.getPortMetadata("result");
		 port.getDescription();
		 assertEquals("application/x-dtbook+xml", port.getMediaType());
		 assertEquals("result name", port.getNiceName());
		 assertEquals("result description", port.getDescription());
	 
	 }
	 
	 /**
	  * Test missing output metadata.
	  */
	 @Test
	 public void testMissingOutputMetadata() {
		 XProcPortMetadata meta = scp.getPortMetadata("result2");
		 assertNotNull(meta);
		 assertNull(meta.getMediaType());
		 assertNull(meta.getNiceName());
		 assertNull(meta.getDescription());
	 }

	 /**
	  * Test missing output metadata.
	  */
	 @Test
	 public void testOutputPortPrimary() {
                 assertTrue("when primary is given true it's set", scp.getPortMetadata("result").isPrimary());
                 assertTrue("when primary is given false it's set", !scp.getPortMetadata("result2").isPrimary());
                 assertTrue("when primary is not given is set to true", scp.getPortMetadata("result3").isPrimary());
	 }
	 
	 /**
	  * Test parameter port.
	  */
	 //@Test
	 //public void testParameterPort() {
		 //// TODO test parameter metadata
	 //}
	 
	 /**
	  * Test missing parameter metadata.
	  */
	 //@Test
	 //public void testMissingParameterMetadata() {
		 //// TODO test missing parameter metadata
	 //}
	 
	 /**
	  * Test option.
	  */
	 @Test
	 public void testOption() {
		 XProcOptionMetadata opt = scp.getOptionMetadata(new QName("option1"));
		 assertEquals("dtbook:mydatatype", opt.getType());
		 //assertEquals(Direction.OUTPUT, opt.getDirection());
		 assertEquals("Option 1", opt.getNiceName());
	 }
	 
	 /**
	  * Test info.
	  */
	 @Test
	 public void testInfo() {
		 // just a simple test to see if is correctly set
		 XProcPipelineInfo info = scp.getXProcPipelineInfo();
		 assertNotNull(info);
		 XProcPortInfo port = info.getInputPort("source");
		assertEquals("source", port.getName());
		assertTrue(port.isPrimary());
		assertTrue(port.isSequence());
	}

	 /**
	  * input file sets
	  */
	 @Test
	 public void inputFilesets() {
		 //input-filesets
		assertTrue(Iterables.contains(this.scp.getInputFilesets(),"dtbook"));
		assertTrue(Iterables.contains(this.scp.getInputFilesets(),"epub3"));
		assertEquals(2,Iterables.size(this.scp.getInputFilesets()));
		 
	}

	 /**
	  * output file sets
	  */
	 @Test
	 public void outputFilesets() {
		 //output-filesets
		assertTrue(Iterables.contains(this.scp.getOutputFilesets(),"html"));
		assertTrue(Iterables.contains(this.scp.getOutputFilesets(),"zedai"));
		assertEquals(2,Iterables.size(this.scp.getOutputFilesets()));
		 
	}
	 /**
	  * Test primary options.
	  */
	 @Test
	 public void testOptionPrimary() {
		 XProcOptionMetadata opt = scp.getOptionMetadata(new QName("option1"));
                 assertTrue("when primary is given true it's set", opt.isPrimary());
		 opt = scp.getOptionMetadata(new QName("option2"));
                 assertTrue("when primary is given false it's set", !opt.isPrimary());
		 opt = scp.getOptionMetadata(new QName("option3"));
                 assertTrue("when primary is not given is set to true",opt.isPrimary() );
	 }
	 /**
	  * Test number of options.
	  */
	 @Test
	 public void testOptionsCount() {
                 assertEquals("There are 3 options",3,Iterables.size(scp.getXProcPipelineInfo().getOptions()));
	 }
}
