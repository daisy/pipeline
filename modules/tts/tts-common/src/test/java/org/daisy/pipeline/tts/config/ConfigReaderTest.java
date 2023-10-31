package org.daisy.pipeline.tts.config;

import java.io.StringReader;
import java.util.ArrayList;
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

import org.junit.Test;

import org.xml.sax.InputSource;

public class ConfigReaderTest {

	static Processor Proc = new Processor(false);
	static String docDirectory = "file:///doc/";

	@Test
	public void testVoiceDeclarations() throws SaxonApiException {
		DocumentBuilder builder = Proc.newDocumentBuilder();
		SAXSource source = new SAXSource(
			new InputSource(
				new StringReader("<config>" +
				                 "  <voice engine=\"engine-1\" name=\"voice-1\" lang=\"fr\" gender=\"female\" priority=\"1\"/>" +
				                 "</config>")));
		source.setSystemId(docDirectory + "uri");
		XdmNode document = builder.build(source);
		VoiceConfigExtension ext = new VoiceConfigExtension();
		ConfigReader cr = new ConfigReader(Proc, document, ext);
		Collection<VoiceInfo> voices = ext.getVoiceDeclarations();
		Assert.assertEquals(2, voices.size());
		Collection<VoiceInfo> expectedVoices = new ArrayList<>();
		expectedVoices.add(new VoiceInfo("mock-tts", "mock-en", new Locale("en"), Gender.MALE_ADULT, 5));
		expectedVoices.add(new VoiceInfo("engine-1", "voice-1", new Locale("fr"), Gender.FEMALE_ADULT, 1));
		Assert.assertEquals(expectedVoices, voices);
	}
}
