package org.daisy.pipeline.tts.config;

import java.io.StringReader;

import javax.xml.transform.sax.SAXSource;

import junit.framework.Assert;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.junit.After;
import org.junit.Test;
import org.xml.sax.InputSource;

public class ConfigReaderTest {

	static Processor Proc = new Processor(false);
	static String docDirectory = "file:///doc/";

	public static ConfigReader initConfigReader(String xmlstr) throws SaxonApiException {
		DocumentBuilder builder = Proc.newDocumentBuilder();
		SAXSource source = new SAXSource(new InputSource(new StringReader("<config>" + xmlstr
		        + "</config>")));
		source.setSystemId(docDirectory + "uri");
		XdmNode document = builder.build(source);

		return new ConfigReader(Proc, document);
	}

	@After
	public void resetSystemProperties() {
		System.setProperty(ConfigReader.HostProtectionProperty, "true");
	}

	@Test
	public void withoutProtection() throws SaxonApiException {
		System.setProperty(ConfigReader.HostProtectionProperty, "false");
		ConfigReader cr = initConfigReader("<property key=\"key1\" value=\"val1\"/><property key=\"key2\" value=\"val2\"/>");
		Assert.assertEquals("val1", cr.getAllProperties().get("key1"));
		Assert.assertEquals("val2", cr.getDynamicProperties().get("key2"));
		Assert.assertEquals(2, cr.getAllProperties().size());
		Assert.assertEquals(2, cr.getDynamicProperties().size());
	}

	@Test
	public void withProtection() throws SaxonApiException {
		System.setProperty(ConfigReader.HostProtectionProperty, "true");
		ConfigReader cr = initConfigReader("<property key=\"key1\" value=\"val1\"/><property key=\"key2\" value=\"val2\"/>");
		Assert.assertEquals(0, cr.getAllProperties().size());
		Assert.assertEquals(2, cr.getDynamicProperties().size());
	}
}
