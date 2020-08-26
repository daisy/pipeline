package org.daisy.pipeline.tts;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import org.junit.Assert;
import org.junit.Test;

public class TTSRegistryTest {

	private static class SimplifiedService extends AbstractTTSService {

		@Override
		public TTSEngine newEngine(Map<String, String> params) throws SynthesisException,
		        InterruptedException {
			return null;
		}

		@Override
		public String getName() {
			return "simplified-service";
		}

	}

	private static class SimplifiedProcessor extends SimpleTTSEngine {

		private Collection<Voice> mVoices;

		SimplifiedProcessor(String xslt, String... voices) throws MalformedURLException {
			super(new SimplifiedService());
			mVoices = new ArrayList<Voice>();
			for (String v : voices) {
				String[] parts = v.split(":");
				mVoices.add(new Voice(parts[0], parts[1]));
			}
		}

		@Override
		public AudioFormat getAudioOutputFormat() {
			return null;
		}

		@Override
		public Collection<Voice> getAvailableVoices() throws SynthesisException,
		        InterruptedException {
			return mVoices;
		}

		@Override
		public Collection<AudioBuffer> synthesize(String sentence,
		        Voice voice, TTSResource threadResources, List<Mark> marks,
		        AudioBufferAllocator bufferAllocator, boolean retry)
		        throws SynthesisException, InterruptedException, MemoryException {
			return null;
		}
	}

	static Configuration Conf = new Processor(false).getUnderlyingConfiguration();

	private static String registerVoice(String engine, String name, String lang,
	        String gender, float priority, List<VoiceInfo> extraVoices) {

		try {
			extraVoices.add(new VoiceInfo(engine, name, lang, Gender.of(gender), priority));
		} catch (UnknownLanguage e) {
			e.printStackTrace();
		}

		return engine + ":" + name;
	}

	private static VoiceManager initVoiceManager(Collection<VoiceInfo> extraVoices,
	        String... voices) throws MalformedURLException {
		return new VoiceManager(Arrays.<TTSEngine> asList(new SimplifiedProcessor(
		        "/empty-ssml-adapter.xsl", voices)), extraVoices);
	}

	@Test
	public void simpleInit() throws MalformedURLException {
		VoiceManager vm = initVoiceManager(Collections.EMPTY_LIST, "acapela:claire");
		boolean[] perfectMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("acapela", "claire", null, null, perfectMatch);
		Assert.assertTrue(perfectMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("acapela", v.engine);
		Assert.assertEquals("claire", v.name);
	}

	@Test
	public void customVoice() throws MalformedURLException {
		String vendor = "vendor";
		String voiceName = "voice1";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname = registerVoice(vendor, voiceName, "en", "male-adult", 10, extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(vendor, voiceName, null, null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor, v.engine);
		Assert.assertEquals(voiceName, v.name);
	}

	@Test
	public void onlyLanguage() throws MalformedURLException {
		String vendor = "vendor";
		String voiceName = "voice1";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname0 = registerVoice("v2", "wrong-lang1", "fr", "male-adult", 15,
		        extraVoices);
		String fullname1 = registerVoice("v", "low-prio", "en", "male-adult", 5, extraVoices);
		String fullname2 = registerVoice(vendor, voiceName, "en", "male-adult", 10,
		        extraVoices);
		String fullname3 = registerVoice("v2", "wrong-lang2", "fr", "male-adult", 15,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname0, fullname1, fullname2,
		        fullname3);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en", null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor, v.engine);
		Assert.assertEquals(voiceName, v.name);
	}

	@Test
	public void withGenderAndLang() throws MalformedURLException {
		String vendor = "vendor";
		String maleVoice = "male-voice";
		String femaleVoice = "female-voice";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor, maleVoice, "en", "male-adult", 5, extraVoices);
		String fullname2 = registerVoice(vendor, femaleVoice, "en", "female-adult", 10,
		        extraVoices);
		String fullname3 = registerVoice(vendor, "fr-voice", "fr", "female-adult", 15,
		        extraVoices);
		String fullname4 = registerVoice(vendor, "lowprio1", "en", "female-adult", 5,
		        extraVoices);
		String fullname5 = registerVoice(vendor, "lowprio2", "en", "male-adult", 5,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3,
		        fullname4, fullname5);
		boolean[] exactMatch = new boolean[1];

		Voice v = vm.findAvailableVoice(null, null, "en", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(maleVoice, v.name);

		v = vm.findAvailableVoice(null, null, "en", "female-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(femaleVoice, v.name);
	}

	@Test
	public void withVendorAndLang() throws MalformedURLException {
		String vendor1 = "vendor1";
		String vendor2 = "vendor2";
		String voice1 = "voice1";
		String voice2 = "voice2";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor1, voice1, "en", "male-adult", 5, extraVoices);
		String fullname2 = registerVoice(vendor2, voice2, "en", "male-adult", 10, extraVoices);
		String fullname3 = registerVoice(vendor1, "voice-fr", "fr", "male-adult", 15,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3);
		boolean[] exactMatch = new boolean[1];

		Voice v = vm.findAvailableVoice(vendor1, null, "en", null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor1, v.engine);
		Assert.assertEquals(voice1, v.name);

		v = vm.findAvailableVoice(vendor2, null, "en", null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor2, v.engine);
		Assert.assertEquals(voice2, v.name);
	}

	@Test
	public void withVendorAndLangAndGender() throws MalformedURLException {
		String vendor1 = "vendor1";
		String vendor2 = "vendor2";
		String maleVoice = "male-voice";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor2, maleVoice, "en", "male-adult", 100,
		        extraVoices);
		String fullname2 = registerVoice(vendor1, maleVoice, "en", "male-adult", 10,
		        extraVoices);
		String fullname3 = registerVoice(vendor1, "wrong", "fr", "male-adult", 100,
		        extraVoices);
		String fullname4 = registerVoice(vendor1, "low-prio", "en", "male-adult", 5,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3,
		        fullname4);
		boolean[] exactMatch = new boolean[1];

		Voice v = vm.findAvailableVoice("vendor1", null, "en", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor1, v.engine);
		Assert.assertEquals(maleVoice, v.name);
	}

	@Test
	public void voiceNotFound() throws MalformedURLException {
		String vendor = "vendor";
		String voiceName = "voice1";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor, voiceName, "en", "male-adult", 10,
		        extraVoices);
		String fullname2 = registerVoice(vendor, "wrongvoice", "fr", "male-adult", 100,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("any-vendor", "any-voice", "en", "female-adult",
		        exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor, vendor);
		Assert.assertEquals(voiceName, v.name);
	}

	@Test
	public void approximateMatch1() throws MalformedURLException {
		String vendor = "vendor";
		String voiceName = "voice1";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor, voiceName, "en", "male-adult", 10,
		        extraVoices);
		String fullname2 = registerVoice(vendor, "wrongvoice1", "fr", "male-adult", 100,
		        extraVoices);
		String fullname3 = registerVoice("another-vendor", "wrongvoice2", "en", "male-adult",
		        200, extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(vendor, null, "en", "female-adult", exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor, vendor);
		Assert.assertEquals(voiceName, v.name);
	}

	@Test
	public void approximateMatch2() throws MalformedURLException {
		String vendor = "vendor";
		String voiceName = "voice1";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor, voiceName, "en", "male-adult", 10,
		        extraVoices);
		String fullname2 = registerVoice(vendor, "wrongvoice", "fr", "male-adult", 100,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("wrong-vendor", null, "en", "male-adult", exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor, vendor);
		Assert.assertEquals(voiceName, v.name);
	}

	@Test
	public void langVariantPriority() throws MalformedURLException {
		String vendor = "vendor";
		String voiceName = "voice1";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor, "voice-a", "en", "male-adult", 10,
		        extraVoices);
		String fullname2 = registerVoice(vendor, "voice-b", "en", "male-adult", 10,
		        extraVoices);
		String fullname3 = registerVoice(vendor, "voice-c", "en", "male-adult", 10,
		        extraVoices);
		String fullname4 = registerVoice(vendor, voiceName, "en-us", "male-adult", 10,
		        extraVoices);
		String fullname5 = registerVoice(vendor, "voice-d", "en", "male-adult", 10,
		        extraVoices);
		String fullname6 = registerVoice(vendor, "voice-e", "en", "male-adult", 10,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3,
		        fullname4, fullname5, fullname6);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en-us", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(voiceName, v.name);
	}

	@Test
	public void voiceFallback1() throws MalformedURLException {
		String vendor1 = "vendor1";
		String vendor2 = "vendor2";
		String firstChoice = "voice1";
		String secondChoice = "voice2";
		String thirdChoice = "voice3";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor1, firstChoice, "en", "male-adult", 20,
		        extraVoices);
		String fullname2 = registerVoice(vendor2, "wrong-choice", "en", "male-adult", 5,
		        extraVoices);
		String fullname3 = registerVoice(vendor2, thirdChoice, "en", "female-adult", 19,
		        extraVoices);
		String fullname4 = registerVoice(vendor2, secondChoice, "en", "male-adult", 18,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3,
		        fullname4);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en-us", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(firstChoice, v.name);

		v = vm.findSecondaryVoice(v);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor2, v.engine);
		Assert.assertTrue(secondChoice.equals(v.name) || thirdChoice.equals(v.name));
	}

	@Test
	public void voiceFallback2() throws MalformedURLException {
		String vendor = "vendor";
		String wantedVoice = "voice1";
		String availableVoice = "voice2";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		registerVoice(vendor, wantedVoice, "en", "male-adult", 20, extraVoices);
		String fullname1 = registerVoice(vendor, "wrong-voice1", "en", "male-adult", 10,
		        extraVoices);
		String fullname2 = registerVoice(vendor, availableVoice, "en", "male-adult", 20,
		        extraVoices);
		String fullname3 = registerVoice(vendor, "wrong-voice2", "en", "male-adult", 10,
		        extraVoices);
		String fullname4 = registerVoice("another-vendor", "wrong-voice3", "en", "male-adult",
		        50, extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3,
		        fullname4);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(vendor, wantedVoice, null, null, exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(availableVoice, v.name);
	}
	
	@Test
	public void voiceFallback3() throws MalformedURLException {
		String vendor1 = "vendor1";
		String vendor2 = "vendor2";
		String firstChoice = "voice1";
		String secondChoice = "voice2";
		String thirdChoice = "voice3";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor1, firstChoice, "en", "male-adult", 20,
		        extraVoices);
		String fullname2 = registerVoice(vendor2, "wrong-choice", "en", "male-adult", 5,
		        extraVoices);
		String fullname3 = registerVoice(vendor2, thirdChoice, "en", "male-adult", 19,
		        extraVoices);
		String fullname4 = registerVoice(vendor1, secondChoice, "en", "male-adult", 10,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2, fullname3,
		        fullname4);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(vendor1, null, "en-us", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(firstChoice, v.name);

		v = vm.findSecondaryVoice(v);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor1, v.engine);
		Assert.assertEquals(secondChoice, v.name);
	}
	
	@Test
	public void multiLangVoice() throws MalformedURLException {
		String vendor = "vendor";
		String voiceName = "voice1";

		List<VoiceInfo> extraVoices = new ArrayList<VoiceInfo>();
		String fullname1 = registerVoice(vendor, voiceName, "*", "male-adult", 10,
		        extraVoices);
		String fullname2 = registerVoice(vendor, "wrongvoice", "fr", "male-adult", 5,
		        extraVoices);

		VoiceManager vm = initVoiceManager(extraVoices, fullname1, fullname2);

		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("any-vendor", "any-voice", "fr", "female-adult",
		        exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals(vendor, vendor);
		Assert.assertEquals(voiceName, v.name);
	}
}
