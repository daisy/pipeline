package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import junit.framework.Assert;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioEncoderService;
import static org.daisy.pipeline.audio.AudioFileTypes.MP3;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.audio.AudioUtils;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.calabash.impl.EncodingThread.EncodingException;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSLog;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;

import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;

/**
 * Those tests are mostly to check the code coverage
 * 
 * TODO: cover the cases when the algorithm fallbacks on another TTS engine
 */
public class SSMLtoAudioTest {
	static Processor Proc = new Processor(false);
	static Logger Logger = LoggerFactory.getLogger(SSMLtoAudioTest.class);
	static URL SsmlTransformer = SSMLtoAudioTest.class.getResource("/valid-sheet.xsl");

	private class CustomVoiceConfig extends VoiceConfigExtension {
		public static final String Engine = "engine";
		public static final String VoiceName = "voicename";

		@Override
		public Collection<VoiceInfo> getVoiceDeclarations() {
			try {
				return Arrays.asList(new VoiceInfo(Engine, VoiceName, "en", Gender.MALE_ADULT, 1.0f));
			} catch (UnknownLanguage e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		public Map<String, String> getAllProperties() {
			return Collections.EMPTY_MAP;
		}

		@Override
		public Map<String, String> getStaticProperties() {
			return Collections.EMPTY_MAP;
		}

		@Override
		public Map<String, String> getDynamicProperties() {
			return Collections.EMPTY_MAP;
		}
	}

	private class VoiceConfigForSingleThread extends CustomVoiceConfig {
		@Override
		public Map<String, String> getAllProperties() {
			Map<String, String> props = new HashMap<String, String>();
			props.put("org.daisy.pipeline.tts.threads.number", "1");
			return props;
		}
	}

	private XdmNode SSMLinXML(String ssml) throws SaxonApiException {
		DocumentBuilder builder = Proc.newDocumentBuilder();
		SAXSource source = new SAXSource(new InputSource(new StringReader(ssml)));
		return (XdmNode) builder.build(source).axisIterator(Axis.CHILD).next();
	}

	static class DefaultTTSService implements TTSService {

		public TTSEngine engine;

		@Override
		public String getName() {
			return "tts-name";
		}

		@Override
		public TTSEngine newEngine(Map<String, String> params) throws Throwable {
			return engine;
		}
	}

	interface DynamicMarkHandler {
		void enableMark(boolean enable);
	}

	static class DefaultTTSEngine extends TTSEngine implements DynamicMarkHandler {

		private final AudioFormat audioFormat = new AudioFormat(8000, 8, 1, true, true);

		@Override
		public SynthesisResult synthesize(XdmNode sentence, Voice voice, TTSResource threadResources)
			throws SynthesisException, InterruptedException {

			int size = 8192;
			List<Integer> marks = new ArrayList<>();
			if (handlesMarks())
				for (int i = TextToPcmThread.getMarkNames(sentence).size(); i > 0; i--)
					marks.add(size - i * 512);
			return new SynthesisResult(createAudioStream(audioFormat, new byte[size]), marks);
		}

		protected String sentenceToString(XdmNode sentence) {
			try {
				return transformSsmlNodeToString(sentence, SsmlTransformer, new HashMap<>());
			} catch (IOException e) {
				throw new RuntimeException(e);
			} catch (SaxonApiException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public Collection<Voice> getAvailableVoices() throws SynthesisException,
		        InterruptedException {
			return Arrays.asList(new Voice(CustomVoiceConfig.Engine,
			        CustomVoiceConfig.VoiceName));
		}

		protected DefaultTTSEngine(TTSService provider) {
			super(provider);
		}

		private boolean handleMark = false;

		@Override
		public void enableMark(boolean yesOrNo) {
			handleMark = yesOrNo;
		}

		@Override
		public boolean handlesMarks() {
			return handleMark;
		}
	}

	static class DefaultAudioEncoder implements AudioEncoderService {
		int count = 0;
		@Override
		public boolean supportsFileType(AudioFileFormat.Type fileType) {
			return MP3.equals(fileType);
		}
		@Override
		public Optional<AudioEncoder> newEncoder(Map<String,String> params) {
			return Optional.of(
				new AudioEncoder() {
					@Override
					public AudioClip encode(AudioInputStream pcm, AudioFileFormat.Type outputFileType, File outputFile)
							throws Throwable {
						synchronized (DefaultAudioEncoder.this) {
							++count;
						}
						return new AudioClip(outputFile, Duration.ZERO, AudioUtils.getDuration(pcm));
					}
				}
			);
		}
	}

	public void runTest(TTSService ttsservice, DynamicMarkHandler markHandler,
	        AudioEncoderService audioEncoder, int expectedGeneralErrors, int expectedSentErrors)
	        throws SynthesisException, EncodingException, InterruptedException, SaxonApiException {
		runTest(ttsservice, markHandler, audioEncoder, expectedGeneralErrors,
		        expectedSentErrors, new CustomVoiceConfig());
	}

	public void runTest(TTSService ttsservice, DynamicMarkHandler markHandler,
	        AudioEncoderService audioEncoder, int expectedGeneralErrors, int expectedSentErrors,
	        VoiceConfigExtension config) throws SynthesisException, EncodingException,
	        InterruptedException, SaxonApiException {
		runTest(ttsservice, markHandler, audioEncoder, expectedGeneralErrors,
		        expectedSentErrors, config, "<ssml xml:lang=\"en\" id=\"s1\">test</ssml>",
		        "<ssml xml:lang=\"en\" id=\"s2\">test</ssml>");
	}

	public void runTest(TTSService ttsservice, DynamicMarkHandler markHandler,
	        AudioEncoderService audioEncoder, int expectedGeneralErrors, int expectedSentErrors,
	        VoiceConfigExtension config, String... ssml) throws SynthesisException,
	        InterruptedException, SaxonApiException, EncodingException {

		for (boolean withEndingMark = true; withEndingMark; withEndingMark = !withEndingMark) {
			markHandler.enableMark(withEndingMark);

			TTSRegistry registry = new TTSRegistry();
			registry.addTTS(ttsservice);

			AudioFootprintMonitor monitor = new AudioFootprintMonitor();
			TTSLog logs = new TTSLog();

			SSMLtoAudio ssmlToAudio = new SSMLtoAudio(new File("/tmp/"), MP3, registry, Logger,
			        monitor, Proc, config, logs);

			for (String text : ssml) {
				ssmlToAudio.dispatchSSML(SSMLinXML(text), null);
			}

			AudioServices audioRegistry = new AudioServices();
			audioRegistry.addEncoderService(audioEncoder);

			EncodingException exception = null;
			try {
				ssmlToAudio.blockingRun(audioRegistry);
			} catch (EncodingException e) {
				exception = e;
			}

			for (TTSLog.Error err : logs.readonlyGeneralErrors()) {
				System.out.println(err.getErrorCode() + ": " + err.getMessage());
			}
			int sentErrors = 0;
			for (Entry<String, TTSLog.Entry> entry : logs.getEntries()) {
				for (TTSLog.Error err : entry.getValue().getReadOnlyErrors()) {
					System.out.println(err.getErrorCode() + ": " + err.getMessage());
					++sentErrors;
				}
			}

			Assert.assertEquals("There should not be unreleased encoding bytes", 0, monitor
			        .getUnreleasedEncondingMem());
			Assert.assertEquals("There should not be unreleased TTS bytes", 0, monitor
			        .getUnreleasedTTSMem());
			Assert.assertEquals("There must be the right number of general errors",
			        expectedGeneralErrors, logs.readonlyGeneralErrors().size());
			Assert.assertEquals("There must be the right number of sentence-level errors",
			        expectedSentErrors, sentErrors);

			if (exception != null) throw exception;
		}
	}

	@Test
	public void regularRun() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service);

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 0, 0);

		Assert.assertTrue("At least one PCM chunk must have been produced", encoder.count > 0);
	}

	@Test
	public void errorInEngineInstanciation() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService() {
			@Override
			public TTSEngine newEngine(Map<String, String> params) throws Throwable {
				throw new RuntimeException("error");
			}

		};
		service.engine = new DefaultTTSEngine(service);

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInVoiceEnumeration1() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {
			@Override
			public Collection<Voice> getAvailableVoices() throws SynthesisException,
			        InterruptedException {
				throw new SynthesisException("error");
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInVoiceEnumeration2() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {
			@Override
			public Collection<Voice> getAvailableVoices() throws SynthesisException,
			        InterruptedException {
				throw new InterruptedException();
			}
		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void timeoutInVoiceEnumeration() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {
			@Override
			public Collection<Voice> getAvailableVoices() throws SynthesisException,
			        InterruptedException {
				Thread.sleep(5000);
				return Collections.EMPTY_LIST;
			}
		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInTestingSynthesis1() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice,
					TTSResource threadResources) throws SynthesisException,
					InterruptedException {
				throw new SynthesisException("error");
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInTestingSynthesis2() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice,
					TTSResource threadResources) throws SynthesisException,
					InterruptedException {
				throw new SynthesisException("");
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInTestingSynthesis3() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice,
					TTSResource threadResources) throws SynthesisException,
					InterruptedException {
				throw new InterruptedException();
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void timeoutInTestingSynthesis() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice,
					TTSResource threadResources) throws SynthesisException,
					InterruptedException {
				Thread.sleep(31000);
				return super.synthesize(sentence, voice, threadResources);
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInSynthesis1() throws SynthesisException, InterruptedException,
	        SaxonApiException {

		final String key = "__key__";
		String ssml = "<ssml id=\"s1\" xml:lang=\"en\">" + key + "</ssml>";

		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice,
					TTSResource threadResources) throws SynthesisException,
					InterruptedException {

				if (sentenceToString(sentence).contains(key))
					throw new InterruptedException();

				return super.synthesize(sentence, voice, threadResources);
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 0, 2,
		        new CustomVoiceConfig(), ssml);
		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInSynthesis2() throws SynthesisException, InterruptedException,
	        SaxonApiException {

		final String key = "__key__";
		String ssml = "<ssml id=\"s1\" xml:lang=\"en\">" + key + "</ssml>";

		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice,
					TTSResource threadResources) throws SynthesisException,
					InterruptedException {

				if (sentenceToString(sentence).contains(key))
					throw new SynthesisException("error");

				return super.synthesize(sentence, voice, threadResources);
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 0, 2,
		        new CustomVoiceConfig(), ssml);
		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void timeoutInSynthesis() throws SynthesisException, InterruptedException,
	        SaxonApiException {

		final String key = "__key__";
		String ssml = "<ssml id=\"s1\" xml:lang=\"en\">" + key + "</ssml>";

		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public SynthesisResult synthesize(XdmNode sentence, Voice voice,
					TTSResource threadResources) throws SynthesisException,
					InterruptedException {

				if (sentenceToString(sentence).contains(key)) {
					Thread.sleep(10000);
				}

				return super.synthesize(sentence, voice, threadResources);
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 0, 2,
		        new CustomVoiceConfig(), ssml);
		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInEncoding() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service);
		DefaultAudioEncoder encoder = new DefaultAudioEncoder() {
			@Override
			public Optional<AudioEncoder> newEncoder(Map<String,String> params) {
				return Optional.of(
					new AudioEncoder() {
						@Override
						public AudioClip encode(AudioInputStream pcm, AudioFileFormat.Type outputFileType, File outputFile)
								throws Throwable {
							throw new RuntimeException();
						}
					}
				);
			}
		};

		try {
			runTest(service, (DynamicMarkHandler) service.engine, encoder, 2, 0);
			Assert.fail("EncodingException expected");
		} catch (EncodingException e) {}

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void timeoutInEncoding() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		final AtomicBoolean interrupted = new AtomicBoolean(false);
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service);
		DefaultAudioEncoder encoder = new DefaultAudioEncoder() {
			@Override
			public Optional<AudioEncoder> newEncoder(Map<String,String> params) {
				return Optional.of(
					new AudioEncoder() {
						@Override
						public AudioClip encode(AudioInputStream pcm, AudioFileFormat.Type outputFileType, File outputFile)
								throws Throwable {
							try {
								Thread.sleep(5000);
								return new AudioClip(outputFile, Duration.ZERO, AudioUtils.getDuration(pcm));
							} catch (InterruptedException e) {
								interrupted.set(true);
								throw e;
							}
						}
					}
				);
			}
		};

		try {
			runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 0,
			        new VoiceConfigForSingleThread(),
			        "<ssml xml:lang=\"en\" id=\"s1\">test</ssml>");
			Assert.fail("EncodingException expected");
		} catch (EncodingException e) {}

		Assert.assertTrue("The audio encoding should have been interrupted", interrupted.get());
	}

	@Test
	public void errorInAllocateResources() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {
			@Override
			public TTSResource allocateThreadResources() throws SynthesisException,
			        InterruptedException {
				throw new SynthesisException("error");
			}
		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void timeoutInAllocateResources() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {
			@Override
			public TTSResource allocateThreadResources() throws SynthesisException,
			        InterruptedException {
				Thread.sleep(3000);
				return new TTSResource();
			}
		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInReleaseResources() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {
			@Override
			public void releaseThreadResources(TTSResource r) throws SynthesisException,
			        InterruptedException {
				throw new SynthesisException("error");
			}
		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 2, 0,
		        new VoiceConfigForSingleThread());
	}

	@Test
	public void timeoutInReleaseResources() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {
			@Override
			public void releaseThreadResources(TTSResource r) throws SynthesisException,
			        InterruptedException {
				Thread.sleep(4000);
			}
		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 2, 0,
		        new VoiceConfigForSingleThread());
	}
}
