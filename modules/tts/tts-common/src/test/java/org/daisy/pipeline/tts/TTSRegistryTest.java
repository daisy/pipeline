package org.daisy.pipeline.tts;

import java.util.ArrayList;
import java.util.Collection;
import static java.util.Collections.EMPTY_LIST;
import static java.util.Collections.singletonList;
import java.util.Iterator;
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
		Iterator<Voice> vv = vm.findAvailableVoices("acapela", "claire", null, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "acapela", "claire", null, null));
		Assert.assertEquals("acapela", v.getEngine());
		Assert.assertEquals("claire", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Iterator<Voice> vv = vm.findAvailableVoices("vendor", "voice1", null, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor", "voice1", null, null));
		Assert.assertEquals("vendor", v.getEngine());
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, en, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, null));
		Assert.assertEquals("low-prio", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Gender male = Gender.of("male-adult");
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, en, male).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, male));
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, male));
		Assert.assertEquals("lowprio2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, en, male));
		Assert.assertEquals("female-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, en, male));
		Assert.assertEquals("lowprio1", v.getName());
		Assert.assertFalse(vv.hasNext());
		Gender female = Gender.of("female-adult");
		vv = vm.findAvailableVoices(null, null, en, female).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, female));
		Assert.assertEquals("female-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, female));
		Assert.assertEquals("lowprio1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, en, female));
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, en, female));
		Assert.assertEquals("lowprio2", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", null, en, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, en, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, en, null));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertFalse(vv.hasNext());
		vv = vm.findAvailableVoices("vendor2", null, en, null).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor2", null, en, null));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor2", null, en, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Gender male = Gender.of("male-adult");
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", null, en, male).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, en, male));
		Assert.assertEquals("vendor1", v.getEngine());
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, en, male));
		Assert.assertEquals("vendor1", v.getEngine());
		Assert.assertEquals("low-prio", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, en, male));
		Assert.assertEquals("vendor2", v.getEngine());
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Gender female = Gender.of("female-adult");
		Iterator<Voice> vv = vm.findAvailableVoices("any-vendor", "any-voice", en, female).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", en, female));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Gender female = Gender.of("female-adult");
		Iterator<Voice> vv = vm.findAvailableVoices("vendor", null, en, female).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", null, en, female));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", null, en, female));
		Assert.assertEquals("wrongvoice2", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Gender male = Gender.of("male-adult");
		Iterator<Voice> vv = vm.findAvailableVoices("wrong-vendor", null, en, male).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "wrong-vendor", null, en, male));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en_US = VoiceInfo.tagToLocale("en-us");
		Gender male = Gender.of("male-adult");
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, en_US, male).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertFalse(vv.hasNext());
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
		Locale en_US = VoiceInfo.tagToLocale("en-us");
		Gender male = Gender.of("male-adult");
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, en_US, male).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertEquals("voice1", v.getName());
		v = vm.findSecondaryVoice(v);
		Assert.assertNotNull(v);
		Assert.assertEquals("vendor2", v.getEngine());
		Assert.assertTrue("voice2".equals(v.getName()) || "voice3".equals(v.getName()));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en_US, male));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, en_US, male));
		Assert.assertEquals("voice3", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Iterator<Voice> vv = vm.findAvailableVoices("vendor", "voice1", null, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", "voice1", null, null));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", "voice1", null, null));
		Assert.assertEquals("wrong-voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", "voice1", null, null));
		Assert.assertEquals("wrong-voice2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", "voice1", null, null));
		Assert.assertEquals("wrong-voice3", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en_US = VoiceInfo.tagToLocale("en-us");
		Gender male = Gender.of("male-adult");
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", null, en_US, male).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, en_US, male));
		Assert.assertEquals("voice1", v.getName());
		v = vm.findSecondaryVoice(v);
		Assert.assertNotNull(v);
		Assert.assertEquals("voice2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, en_US, male));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, en_US, male));
		Assert.assertEquals("voice3", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, en_US, male));
		Assert.assertEquals("wrong-choice", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", "voice1", null, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", "voice1", null, null));
		// fallback should never be the same as the primary voice
		v = vm.findSecondaryVoice(v);
		Assert.assertNull(v);
		Assert.assertFalse(vv.hasNext());
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
		Locale fr = VoiceInfo.tagToLocale("fr");
		Gender female = Gender.of("female-adult");
		Iterator<Voice> vv = vm.findAvailableVoices("any-vendor", "any-voice", fr, female).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", fr, female));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale fr = VoiceInfo.tagToLocale("fr");
		Gender female = Gender.of("female-adult");
		Iterator<Voice> vv = vm.findAvailableVoices("any-vendor", "any-voice", fr, female).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", fr, female));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", fr, female));
		Assert.assertEquals("wrongvoice", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Iterator<Voice> vv = vm.findAvailableVoices("Vendor1", "voice1", null, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "Vendor1", "voice1", null, null));
		Assert.assertEquals("Vendor1", v.getEngine());
		Assert.assertEquals("Voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
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
		Locale en = VoiceInfo.tagToLocale("en");
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, en, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
		Gender male = Gender.of("male-adult");
		vv = vm.findAvailableVoices(null, null, en, male).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, en, male));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
	}
}
