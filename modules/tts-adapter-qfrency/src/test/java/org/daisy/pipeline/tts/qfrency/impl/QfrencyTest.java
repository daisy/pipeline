package org.daisy.pipeline.tts.qfrency.impl;

import java.io.StringReader;
import java.util.Collection;

import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.xml.sax.InputSource;

/*
 * Unit test for the Qfrency adapter.
 * You must have a speech server running on localhost to run these tests. You also nead the client program in your path as "qfrency-client". 
 */


public class QfrencyTest {
	QfrencyEngine tts;

	private String format(String str) {
		return str;
	}

	private String format(String str, String speakerName) {
		return "\\voice{" + speakerName + "}" + str;
	}

	static private int getSize(AudioInputStream audio) {
		return Math.toIntExact(
			audio.getFrameLength() * audio.getFormat().getFrameSize());
	}

	@Before
	public void setUp() throws SynthesisException, InterruptedException {
		tts = new QfrencyEngine(new QfrencyService(), "qfrency-client", "localhost", 0);
	}

	@Test
	public void getVoiceNames() throws SynthesisException, InterruptedException {
		Collection<Voice> voices = tts.getAvailableVoices();
		Assert.assertTrue("some voices must be found", voices.size() > 0);
	}

	public Voice getVoice() throws SynthesisException, InterruptedException {
		Collection<Voice> voices = tts.getAvailableVoices();
		if (voices.size()>0)
			return voices.iterator().next();
		return null;
	}
	
	@Test
	public void simpleSpeak() throws SynthesisException, InterruptedException, SaxonApiException {
		TTSResource r = tts.allocateThreadResources();
		Voice voice = getVoice();
		Assert.assertTrue("At least one voice must be available", voice!=null);
		int size = getSize(
			tts.synthesize(
				parseSSML("<s xmlns=\"http://www.w3.org/2001/10/synthesis\">this is a test<s>"),
				voice, r).audio);
		tts.releaseThreadResources(r);

		Assert.assertTrue("audio output must be big enough", size > 2000);
	}

	private static final Processor proc = new Processor(false);

	private static XdmNode parseSSML(String ssml) throws SaxonApiException {
		return proc.newDocumentBuilder().build(new SAXSource(new InputSource(new StringReader(ssml))));
	}
}
