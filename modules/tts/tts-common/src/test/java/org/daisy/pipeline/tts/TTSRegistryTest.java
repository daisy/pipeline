package org.daisy.pipeline.tts;

import java.util.ArrayList;
import java.util.Collection;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;

import org.junit.Assert;
import org.junit.Test;

public class TTSRegistryTest {

	private static class SimplifiedService implements TTSService {

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

	private static class SimplifiedProcessor extends TTSEngine {

		private Collection<Voice> mVoices;

		SimplifiedProcessor(Collection<Voice> voices) {
			super(new SimplifiedService());
			mVoices = voices;
		}

		@Override
		public Collection<Voice> getAvailableVoices() throws SynthesisException,
		        InterruptedException {
			return mVoices;
		}

		@Override
		public SynthesisResult synthesize(XdmNode sentence, Voice voice,
		        TTSResource threadResources)
		        throws SynthesisException, InterruptedException {
			return null;
		}
	}

	static Configuration Conf = new Processor(false).getUnderlyingConfiguration();

	@Test
	public void simpleInit() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("acapela", "claire"));
		}
		List<VoiceInfo> voiceInfoFromConfig = EMPTY_LIST;
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("acapela", "claire", null, null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("acapela", v.engine);
		Assert.assertEquals("claire", v.name);
	}

	@Test
	public void customVoice() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "en", Gender.of("male-adult"), 10));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("vendor", "voice1", null, null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void onlyLanguage() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("v2", "wrong-lang1"));
			availableVoices.add(new Voice("v", "low-prio"));
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("v2", "wrong-lang2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("v2", "wrong-lang1", "fr", Gender.of("male-adult"), 15));
			voiceInfoFromConfig.add(new VoiceInfo("v", "low-prio", "en", Gender.of("male-adult"), 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("v2", "wrong-lang2", "fr", Gender.of("male-adult"), 15));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en", null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void withGenderAndLang() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "male-voice"));
			availableVoices.add(new Voice("vendor", "female-voice"));
			availableVoices.add(new Voice("vendor", "fr-voice"));
			availableVoices.add(new Voice("vendor", "lowprio1"));
			availableVoices.add(new Voice("vendor", "lowprio2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "male-voice", "en", Gender.of("male-adult"), 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "female-voice", "en", Gender.of("female-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "fr-voice", "fr", Gender.of("female-adult"), 15));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "lowprio1", "en", Gender.of("female-adult"), 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "lowprio2", "en", Gender.of("male-adult"), 5));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("male-voice", v.name);
		v = vm.findAvailableVoice(null, null, "en", "female-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("female-voice", v.name);
	}

	@Test
	public void withVendorAndLang() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1"));
			availableVoices.add(new Voice("vendor2", "voice2"));
			availableVoices.add(new Voice("vendor1", "voice-fr"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", "en", Gender.of("male-adult"), 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice2", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice-fr", "fr", Gender.of("male-adult"), 15));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("vendor1", null, "en", null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor1", v.engine);
		Assert.assertEquals("voice1", v.name);
		v = vm.findAvailableVoice("vendor2", null, "en", null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor2", v.engine);
		Assert.assertEquals("voice2", v.name);
	}

	@Test
	public void withVendorAndLangAndGender() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor2", "male-voice"));
			availableVoices.add(new Voice("vendor1", "male-voice"));
			availableVoices.add(new Voice("vendor1", "wrong"));
			availableVoices.add(new Voice("vendor1", "low-prio"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "male-voice", "en", Gender.of("male-adult"), 100));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "male-voice", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "wrong", "fr", Gender.of("male-adult"), 100));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "low-prio", "en", Gender.of("male-adult"), 5));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("vendor1", null, "en", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor1", v.engine);
		Assert.assertEquals("male-voice", v.name);
	}

	@Test
	public void voiceNotFound() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice", "fr", Gender.of("male-adult"), 100));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("any-vendor", "any-voice", "en", "female-adult", exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void approximateMatch1() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice1"));
			availableVoices.add(new Voice("another-vendor", "wrongvoice2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice1", "fr", Gender.of("male-adult"), 100));
			voiceInfoFromConfig.add(new VoiceInfo("another-vendor", "wrongvoice2", "en", Gender.of("male-adult"), 200));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("vendor", null, "en", "female-adult", exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void approximateMatch2() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice", "fr", Gender.of("male-adult"), 100));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("wrong-vendor", null, "en", "male-adult", exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void langVariantPriority() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice-a"));
			availableVoices.add(new Voice("vendor", "voice-b"));
			availableVoices.add(new Voice("vendor", "voice-c"));
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "voice-d"));
			availableVoices.add(new Voice("vendor", "voice-e"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-a", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-b", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-c", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "en-us", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-d", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-e", "en", Gender.of("male-adult"), 10));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en-us", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void voiceFallback1() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1"));
			availableVoices.add(new Voice("vendor2", "wrong-choice"));
			availableVoices.add(new Voice("vendor2", "voice3"));
			availableVoices.add(new Voice("vendor2", "voice2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", "en", Gender.of("male-adult"), 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "wrong-choice", "en", Gender.of("male-adult"), 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice3", "en", Gender.of("female-adult"), 19));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice2", "en", Gender.of("male-adult"), 18));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en-us", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("voice1", v.name);
		v = vm.findSecondaryVoice(v);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor2", v.engine);
		Assert.assertTrue("voice2".equals(v.name) || "voice3".equals(v.name));
	}

	@Test
	public void voiceFallback2() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "wrong-voice1"));
			availableVoices.add(new Voice("vendor", "voice2"));
			availableVoices.add(new Voice("vendor", "wrong-voice2"));
			availableVoices.add(new Voice("another-vendor", "wrong-voice3"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "en", Gender.of("male-adult"), 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrong-voice1", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice2", "en", Gender.of("male-adult"), 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrong-voice2", "en", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("another-vendor", "wrong-voice3", "en", Gender.of("male-adult"), 50));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("vendor", "voice1", null, null, exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("voice2", v.name);
	}

	@Test
	public void voiceFallback3() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1"));
			availableVoices.add(new Voice("vendor2", "wrong-choice"));
			availableVoices.add(new Voice("vendor2", "voice3"));
			availableVoices.add(new Voice("vendor1", "voice2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", "en", Gender.of("male-adult"), 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "wrong-choice", "en", Gender.of("male-adult"), 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice3", "en", Gender.of("male-adult"), 19));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice2", "en", Gender.of("male-adult"), 10));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("vendor1", null, "en-us", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("voice1", v.name);
		v = vm.findSecondaryVoice(v);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor1", v.engine);
		Assert.assertEquals("voice2", v.name);
	}

	@Test
	public void voiceFallback4() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1", Locale.forLanguageTag("en"), Gender.of("male-adult")));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", "en", Gender.of("male-adult"), 20));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("vendor1", "voice1", null, null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		// fallback should never be the same as the primary voice
		v = vm.findSecondaryVoice(v);
		Assert.assertNull(v);
	}

	@Test
	public void multiLangVoice() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "*", Gender.of("male-adult"), 10));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("any-vendor", "any-voice", "fr", "female-adult", exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void multiLangVoicePriority() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "*", Gender.of("male-adult"), 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice", "fr", Gender.of("male-adult"), 5));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("any-vendor", "any-voice", "fr", "female-adult", exactMatch);
		Assert.assertFalse(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}

	@Test
	public void caseInsensitivity() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("Vendor1", "Voice1"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("Vendor1", "Voice1", "*", Gender.of("male-adult"), 10));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice("Vendor1", "voice1", null, null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("Vendor1", v.engine);
		Assert.assertEquals("Voice1", v.name);
	}

	@Test
	public void voiceWithKnownLanguageAndUnknownGender() throws UnknownLanguage {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1", Locale.forLanguageTag("en"), Gender.ANY));
		}
		List<VoiceInfo> voiceInfoFromConfig = EMPTY_LIST;
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		boolean[] exactMatch = new boolean[1];
		Voice v = vm.findAvailableVoice(null, null, "en", null, exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
		v = vm.findAvailableVoice(null, null, "en", "male-adult", exactMatch);
		Assert.assertTrue(exactMatch[0]);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor", v.engine);
		Assert.assertEquals("voice1", v.name);
	}
}
