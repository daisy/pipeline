package org.daisy.pipeline.script.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.net.URISyntaxException;

import javax.xml.stream.XMLInputFactory;

import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.datatypes.ValidationResult;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.XProcScriptService;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

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
	 Script scp;
	 
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
		 DatatypeRegistry datatypes = new DatatypeRegistry();
		 datatypes.register(new DatatypeService("dtbook:mydatatype") {
			public Document asDocument() throws Exception {
				throw new UnsupportedOperationException("Not implemented");
			}
			public ValidationResult validate(String content) {
				return ValidationResult.valid();
			}});
		 parser.setDatatypeRegistry(datatypes);
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
	  * Test input port.
	  */
	 @Test
	 public void testInputPort() {
		 ScriptPort port = scp.getInputPort("source");
		 assertEquals("application/x-dtbook+xml", port.getMediaType());
		 assertEquals("source name", port.getNiceName());
		 assertEquals("source description", port.getDescription());
	 
	 }
	 
	 @Test
	 public void testInputPortRequired() {
		 ScriptPort port = scp.getInputPort("source");
		 assertTrue(port.isRequired());
		 port = scp.getInputPort("source2");
		 Assert.assertFalse(port.isRequired());
	 }

	 /**
	  * Test missing input metadata.
	  */
	 @Test
	 public void testMissingInputMetadata() {
		 ScriptPort port = scp.getInputPort("source2");
		 assertNotNull(port);
		 assertNull(port.getMediaType());
		 assertNull(port.getNiceName());
		 assertNull(port.getDescription());
	 }
	 
	 /**
	  * Test output port.
	  */
	 @Test
	 public void testOutputPort() {
		 ScriptPort port = scp.getOutputPort("result");
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
		 ScriptPort port = scp.getOutputPort("result2");
		 assertNotNull(port);
		 assertNull(port.getMediaType());
		 assertNull(port.getNiceName());
		 assertNull(port.getDescription());
	 }

	 /**
	  * Test missing output metadata.
	  */
	 @Test
	 public void testOutputPortPrimary() {
		 assertTrue("when primary is given true it's set", scp.getOutputPort("result").isPrimary());
		 assertTrue("when primary is given false it's set", !scp.getOutputPort("result2").isPrimary());
		 assertTrue("when primary is not given and it is not the only output port it is set to false",
		            !scp.getOutputPort("result3").isPrimary());
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
		 ScriptOption opt = scp.getOption("option1");
		 assertEquals("dtbook:mydatatype", opt.getType().getId());
		 //assertEquals(Direction.OUTPUT, opt.getDirection());
		 assertEquals("Option 1", opt.getNiceName());
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
		 ScriptOption opt = scp.getOption("option1");
                 assertTrue("when primary is given true it's set", opt.isPrimary());
		 opt = scp.getOption("option2");
                 assertTrue("when primary is given false it's set", !opt.isPrimary());
		 opt = scp.getOption("option3");
                 assertTrue("when primary is not given is set to true",opt.isPrimary() );
	 }
	 /**
	  * Test number of options.
	  */
	 @Test
	 public void testOptionsCount() {
                 assertEquals("There are 3 options",3,Iterables.size(scp.getOptions()));
	 }
}
