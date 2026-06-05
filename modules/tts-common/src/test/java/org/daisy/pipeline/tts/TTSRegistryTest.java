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

import org.daisy.pipeline.css.speech.VoiceFamilyList;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.VoiceInfo.Gender;

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
	static Locale EN = Locale.forLanguageTag("en");
	static Locale EN_US = Locale.forLanguageTag("en-US");
	static Locale EN_IN = Locale.forLanguageTag("en-IN");
	static Locale FR = Locale.forLanguageTag("fr");
	static Gender MALE = Gender.MALE_ADULT;
	static Gender FEMALE = Gender.FEMALE_ADULT;

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
	public void customVoice() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", EN, MALE, 10));
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
	public void onlyLanguage() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("v2", "wrong-lang1"));
			availableVoices.add(new Voice("v", "low-prio"));
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("v2", "wrong-lang2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("v2", "wrong-lang1", FR, MALE, 15));
			voiceInfoFromConfig.add(new VoiceInfo("v", "low-prio", EN, MALE, 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("v2", "wrong-lang2", FR, MALE, 15));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, EN, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, null));
		Assert.assertEquals("low-prio", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void withGenderAndLang() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "male-voice"));
			availableVoices.add(new Voice("vendor", "female-voice"));
			availableVoices.add(new Voice("vendor", "fr-voice"));
			availableVoices.add(new Voice("vendor", "lowprio1"));
			availableVoices.add(new Voice("vendor", "lowprio2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "male-voice", EN, MALE, 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "female-voice", EN, FEMALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "fr-voice", FR, FEMALE, 15));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "lowprio1", EN, FEMALE, 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "lowprio2", EN, MALE, 5));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, EN, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, MALE));
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, MALE));
		Assert.assertEquals("lowprio2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, EN, MALE));
		Assert.assertEquals("female-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, EN, MALE));
		Assert.assertEquals("lowprio1", v.getName());
		Assert.assertFalse(vv.hasNext());
		vv = vm.findAvailableVoices(null, null, EN, FEMALE).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, FEMALE));
		Assert.assertEquals("female-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, FEMALE));
		Assert.assertEquals("lowprio1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, EN, FEMALE));
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, EN, FEMALE));
		Assert.assertEquals("lowprio2", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void withVendorAndLang() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1"));
			availableVoices.add(new Voice("vendor2", "voice2"));
			availableVoices.add(new Voice("vendor1", "voice-fr"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", EN, MALE, 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice2", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice-fr", FR, MALE, 15));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", null, EN, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, EN, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, EN, null));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertFalse(vv.hasNext());
		vv = vm.findAvailableVoices("vendor2", null, EN, null).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor2", null, EN, null));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor2", null, EN, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void withVendorAndNameAndLang() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice-a"));
			availableVoices.add(new Voice("vendor", "voice-b"));
			availableVoices.add(new Voice("vendor", "voice-c"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-a", EN, MALE, 1));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-b", EN, MALE, 1));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-c", EN, MALE, 1));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("vendor", "voice-b", EN, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertEquals("voice-b", v.getName());
		Assert.assertTrue(vm.matches(v, "vendor", "voice-b", EN, null));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vv.hasNext());
		vv = vm.findAvailableVoices("vendor", "voice-b", FR, null).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertEquals("voice-b", v.getName());
		Assert.assertFalse(vm.matches(v, "vendor", "voice-b", FR, null));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void withVendorAndLangAndGender() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor2", "male-voice"));
			availableVoices.add(new Voice("vendor1", "male-voice"));
			availableVoices.add(new Voice("vendor1", "wrong"));
			availableVoices.add(new Voice("vendor1", "low-prio"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "male-voice", EN, MALE, 100));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "male-voice", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "wrong", FR, MALE, 100));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "low-prio", EN, MALE, 5));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", null, EN, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, EN, MALE));
		Assert.assertEquals("vendor1", v.getEngine());
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, EN, MALE));
		Assert.assertEquals("vendor1", v.getEngine());
		Assert.assertEquals("low-prio", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, EN, MALE));
		Assert.assertEquals("vendor2", v.getEngine());
		Assert.assertEquals("male-voice", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void voiceNotFound() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice", FR, MALE, 100));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("any-vendor", "any-voice", EN, FEMALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", EN, FEMALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void approximateMatch1() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice1"));
			availableVoices.add(new Voice("another-vendor", "wrongvoice2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice1", FR, MALE, 100));
			voiceInfoFromConfig.add(new VoiceInfo("another-vendor", "wrongvoice2", EN, MALE, 200));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("vendor", null, EN, FEMALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", null, EN, FEMALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor", null, EN, FEMALE));
		Assert.assertEquals("wrongvoice2", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void approximateMatch2() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice", FR, MALE, 100));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("wrong-vendor", null, EN, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "wrong-vendor", null, EN, MALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void langVariant() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice-a"));
			availableVoices.add(new Voice("vendor", "voice-b"));
			availableVoices.add(new Voice("vendor", "voice-c"));
			availableVoices.add(new Voice("vendor", "voice-d", EN_IN, MALE));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-a", EN, MALE, 0));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-b", EN_US, MALE, 0));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-c", EN_IN, MALE, 0));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, EN, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, MALE));
		Assert.assertEquals("voice-a", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, MALE));
		Assert.assertEquals("voice-d", v.getName());
		Assert.assertFalse(vv.hasNext());
		vv = vm.findAvailableVoices(null, null, EN_US, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN_US, MALE));
		Assert.assertEquals("voice-b", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN_US, MALE));
		Assert.assertEquals("voice-a", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN_US, MALE));
		Assert.assertEquals("voice-d", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void langVariantPriority() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice-a"));
			availableVoices.add(new Voice("vendor", "voice-b"));
			availableVoices.add(new Voice("vendor", "voice-c"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-a", EN, MALE, 1));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-b", EN_US, MALE, 2));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice-c", EN_IN, MALE, 0));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, EN_IN, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN_IN, MALE));
		// the "en" voice wins because it has a higher priority
		// the higher priority "en-US" voice is not a match
		Assert.assertEquals("voice-a", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN_IN, MALE));
		Assert.assertEquals("voice-c", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void voiceFallback1() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1"));
			availableVoices.add(new Voice("vendor2", "wrong-choice"));
			availableVoices.add(new Voice("vendor2", "voice3"));
			availableVoices.add(new Voice("vendor2", "voice2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", EN, MALE, 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "wrong-choice", EN, MALE, 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice3", EN, FEMALE, 19));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice2", EN, MALE, 18));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, EN_US, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN_US, MALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue("voice2".equals(v.getName()) || "wrong-choice".equals(v.getName()));
		Assert.assertTrue(vm.matches(v, null, null, EN_US, MALE));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue("voice2".equals(v.getName()) || "wrong-choice".equals(v.getName()));
		Assert.assertTrue(vm.matches(v, null, null, EN_US, MALE));
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, null, null, EN_US, MALE));
		Assert.assertEquals("voice3", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void voiceFallback2() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "wrong-voice1"));
			availableVoices.add(new Voice("vendor", "voice2"));
			availableVoices.add(new Voice("vendor", "wrong-voice2"));
			availableVoices.add(new Voice("another-vendor", "wrong-voice3"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", EN, MALE, 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrong-voice1", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice2", EN, MALE, 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrong-voice2", EN, MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("another-vendor", "wrong-voice3", EN, MALE, 50));
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
	public void voiceFallback3() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1"));
			availableVoices.add(new Voice("vendor2", "wrong-choice"));
			availableVoices.add(new Voice("vendor2", "voice3"));
			availableVoices.add(new Voice("vendor1", "voice2"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", EN, MALE, 20));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "wrong-choice", EN, MALE, 5));
			voiceInfoFromConfig.add(new VoiceInfo("vendor2", "voice3", EN, MALE, 19));
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice2", EN, MALE, 10));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", null, EN_US, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, EN_US, MALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", null, EN_US, MALE));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, EN_US, MALE));
		Assert.assertEquals("voice3", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "vendor1", null, EN_US, MALE));
		Assert.assertEquals("wrong-choice", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void voiceFallback4() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor1", "voice1", EN, MALE));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor1", "voice1", EN, MALE, 20));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("vendor1", "voice1", null, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, "vendor1", "voice1", null, null));
		// fallback should never be the same as the primary voice
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void multiLangVoice() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "*", MALE, 10));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("any-vendor", "any-voice", FR, FEMALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", FR, FEMALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void multiLangVoicePriority() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1"));
			availableVoices.add(new Voice("vendor", "wrongvoice"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "voice1", "*", MALE, 10));
			voiceInfoFromConfig.add(new VoiceInfo("vendor", "wrongvoice", FR, MALE, 5));
		}
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices("any-vendor", "any-voice", FR, FEMALE).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", FR, FEMALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertFalse(vm.matches(v, "any-vendor", "any-voice", FR, FEMALE));
		Assert.assertEquals("wrongvoice", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void caseInsensitivity() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("Vendor1", "Voice1"));
		}
		List<VoiceInfo> voiceInfoFromConfig = new ArrayList<VoiceInfo>(); {
			voiceInfoFromConfig.add(new VoiceInfo("Vendor1", "Voice1", "*", MALE, 10));
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
	public void voiceWithKnownLanguageAndUnknownGender() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice1", EN, Gender.ANY));
		}
		List<VoiceInfo> voiceInfoFromConfig = EMPTY_LIST;
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, EN, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
		vv = vm.findAvailableVoices(null, null, EN, MALE).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, MALE));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertFalse(vv.hasNext());
	}

	@Test
	public void orderOfAvailableVoices() {
		List<Voice> availableVoices = new ArrayList<>(); {
			availableVoices.add(new Voice("vendor", "voice3", EN, MALE));
			availableVoices.add(new Voice("vendor", "voice1", EN, MALE));
			availableVoices.add(new Voice("vendor", "voice2", EN, MALE));
		}
		List<VoiceInfo> voiceInfoFromConfig = EMPTY_LIST;
		VoiceManager vm = new VoiceManager(
			singletonList(new SimplifiedProcessor(availableVoices)),
			voiceInfoFromConfig);
		Iterator<Voice> vv = vm.findAvailableVoices(null, null, EN, null).iterator();
		Assert.assertTrue(vv.hasNext());
		Voice v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, null));
		Assert.assertEquals("voice3", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, null));
		Assert.assertEquals("voice1", v.getName());
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, null, null, EN, null));
		Assert.assertEquals("voice2", v.getName());
		Assert.assertFalse(vv.hasNext());

		VoiceFamilyList f = VoiceFamilyList.of("male 1");
		vv = vm.findAvailableVoices(EN, f).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, EN, f));
		Assert.assertEquals("voice3", v.getName());
		f = VoiceFamilyList.of("male 2");
		vv = vm.findAvailableVoices(EN, f).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, EN, f));
		Assert.assertEquals("voice1", v.getName());
		f = VoiceFamilyList.of("male 3");
		vv = vm.findAvailableVoices(EN, f).iterator();
		Assert.assertTrue(vv.hasNext());
		v = vv.next();
		Assert.assertTrue(vm.matches(v, EN, f));
		Assert.assertEquals("voice2", v.getName());
	}
}
