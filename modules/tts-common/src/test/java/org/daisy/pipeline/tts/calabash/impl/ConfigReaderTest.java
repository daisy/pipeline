package org.daisy.pipeline.tts.calabash.impl;

import java.io.StringReader;
import java.util.Collection;
import java.util.Locale;

import javax.xml.transform.sax.SAXSource;

import junit.framework.Assert;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.LanguageRange;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.config.VoiceConfigExtension;

import org.junit.Test;

import org.xml.sax.InputSource;

public class ConfigReaderTest {

	static Processor Proc = new Processor(false);
	static String docDirectory = "file:///doc/";

	public static VoiceConfigExtension initConfigReader(String xmlstr) throws SaxonApiException {
		DocumentBuilder builder = Proc.newDocumentBuilder();
		SAXSource source = new SAXSource(new InputSource(new StringReader("<config>" + xmlstr + "</config>")));
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
		boolean found = false;
		for (VoiceInfo v : voices)
			if ("voice-name".equals(v.voiceName)) {
				Assert.assertEquals(new LanguageRange("en"), v.language);
				Assert.assertEquals("engine", v.voiceEngine);
				Assert.assertEquals(Gender.MALE_ADULT, v.gender);
				Assert.assertEquals(42, (int) v.priority);
				found = true;
				break;
			}
		Assert.assertTrue(found);
	}
}
