package org.daisy.pipeline.synthesize;

import java.io.StringReader;
import java.util.Collection;
import java.util.Locale;

import javax.xml.transform.sax.SAXSource;

import junit.framework.Assert;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.Voice.MarkSupport;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.synthesize.VoiceConfigExtension;
import org.junit.Test;
import org.xml.sax.InputSource;

public class ConfigReaderTest {

	static Processor Proc = new Processor(false);
	static String docDirectory = "file:///doc/";

	public static VoiceConfigExtension initConfigReader(String xmlstr)
	        throws SaxonApiException {
		DocumentBuilder builder = Proc.newDocumentBuilder();
		SAXSource source = new SAXSource(new InputSource(new StringReader("<config>" + xmlstr
		        + "</config>")));
		source.setSystemId(docDirectory + "uri");
		XdmNode document = builder.build(source);

		VoiceConfigExtension ext = new VoiceConfigExtension();
		new ConfigReader(Proc, document, ext);
		return ext;
	}

	@Test
	public void voices() throws SaxonApiException {
		VoiceConfigExtension cr = initConfigReader("<voice engine=\"engine\" name=\"voice-name\" gender=\"male\" lang=\"en\" priority=\"42\"/>");

		Collection<VoiceInfo> voices = cr.getVoiceDeclarations();

		Assert.assertFalse(voices.isEmpty());

		VoiceInfo v = voices.iterator().next();
		Assert.assertEquals(new Locale("en"), v.language);
		Assert.assertEquals("voice-name", v.voice.name);
		Assert.assertEquals("engine", v.voice.engine);
		Assert.assertEquals(VoiceInfo.Gender.MALE_ADULT, v.gender);
		Assert.assertEquals(42, (int) v.priority);
		Assert.assertEquals(MarkSupport.DEFAULT, v.voice.getMarkSupport());
	}
}
