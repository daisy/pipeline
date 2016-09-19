package org.daisy.pipeline.synthesize;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import junit.framework.Assert;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.AudioBufferTracker;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.Voice.MarkSupport;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.UnknownLanguage;
import org.daisy.pipeline.tts.synthesize.IPipelineLogger;
import org.daisy.pipeline.tts.synthesize.SSMLtoAudio;
import org.daisy.pipeline.tts.synthesize.TTSLog;
import org.daisy.pipeline.tts.synthesize.TTSLogImpl;
import org.daisy.pipeline.tts.synthesize.VoiceConfigExtension;
import org.junit.Test;
import org.xml.sax.InputSource;

import com.google.common.base.Optional;

/**
 * Those tests are mostly to check the code coverage
 * 
 * TODO: cover the cases when the algorithm fallbacks on another TTS engine
 */
public class SSMLtoAudioTest implements IPipelineLogger, URIResolver {
	static Processor Proc = new Processor(false);

	private class CustomVoiceConfig extends VoiceConfigExtension {
		public static final String Engine = "engine";
		public static final String VoiceName = "voicename";

		@Override
		public Collection<VoiceInfo> getVoiceDeclarations() {
			try {
				return Arrays.asList(new VoiceInfo(Engine, VoiceName, MarkSupport.DEFAULT, "en",
				        Gender.MALE_ADULT, 1.0f));
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
			props.put("threads.number", "1");
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
		public String getVersion() {
			return "tts-version";
		}

		@Override
		public URL getSSMLxslTransformerURL() {
			return this.getClass().getResource("/valid-sheet.xsl");
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
		@Override
		public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
		        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
		        AudioBufferAllocator bufferAllocator, boolean retry)
		        throws SynthesisException, InterruptedException, MemoryException {

			int size = 8192;
			AudioBuffer res = bufferAllocator.allocateBuffer(size);

			if (endingMark() != null) {
				marks.add(new Mark(endingMark(), size - 512));
			}

			return Arrays.asList(res);
		}

		@Override
		public AudioFormat getAudioOutputFormat() {
			return new AudioFormat(8000, 8, 1, true, true);
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
		public String endingMark() {
			if (handleMark)
				return "mark";
			return null;
		}

	}

	static class DefaultAudioEncoder implements AudioEncoder {
		int count = 0;

		@Override
		public Optional<String> encode(Iterable<AudioBuffer> pcm, AudioFormat audioFormat,
		        File outputDir, String filePrefix, EncodingOptions options) throws Throwable {
			synchronized (this) {
				++count;
			}
			return Optional.of("uri");
		}

		@Override
		public EncodingOptions parseEncodingOptions(Map<String, String> params) {
			return null;
		}

		@Override
		public void test(EncodingOptions options) throws Exception {
		}
	}

	public void runTest(TTSService ttsservice, DynamicMarkHandler markHandler,
	        AudioEncoder audioEncoder, int expectedGeneralErrors, int expectedSentErrors)
	        throws SynthesisException, InterruptedException, SaxonApiException {
		runTest(ttsservice, markHandler, audioEncoder, expectedGeneralErrors,
		        expectedSentErrors, new CustomVoiceConfig());
	}

	public void runTest(TTSService ttsservice, DynamicMarkHandler markHandler,
	        AudioEncoder audioEncoder, int expectedGeneralErrors, int expectedSentErrors,
	        VoiceConfigExtension config) throws SynthesisException, InterruptedException,
	        SaxonApiException {
		runTest(ttsservice, markHandler, audioEncoder, expectedGeneralErrors,
		        expectedSentErrors, config, "<ssml xml:lang=\"en\" id=\"s1\">test</ssml>",
		        "<ssml xml:lang=\"en\" id=\"s2\">test</ssml>");
	}

	public void runTest(TTSService ttsservice, DynamicMarkHandler markHandler,
	        AudioEncoder audioEncoder, int expectedGeneralErrors, int expectedSentErrors,
	        VoiceConfigExtension config, String... ssml) throws SynthesisException,
	        InterruptedException, SaxonApiException {

		for (boolean withEndingMark = true; withEndingMark; withEndingMark = !withEndingMark) {
			markHandler.enableMark(withEndingMark);

			TTSRegistry registry = new TTSRegistry();
			registry.addTTS(ttsservice);

			AudioBufferTracker tracker = new AudioBufferTracker();
			TTSLog logs = new TTSLogImpl();

			SSMLtoAudio ssmlToAudio = new SSMLtoAudio(new File("/tmp/"), registry, this,
			        tracker, Proc, this, config, logs);

			for (String text : ssml) {
				ssmlToAudio.dispatchSSML(SSMLinXML(text));
			}
			ssmlToAudio.endSection();

			AudioServices audioRegistry = new AudioServices();
			audioRegistry.addEncoder(audioEncoder);

			ssmlToAudio.blockingRun(audioRegistry);

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

			Assert.assertEquals("There should not be unreleased encoding bytes", 0, tracker
			        .getUnreleasedEncondingMem());
			Assert.assertEquals("There should not be unreleased TTS bytes", 0, tracker
			        .getUnreleasedTTSMem());
			Assert.assertEquals("There must be the right number of general errors",
			        expectedGeneralErrors, logs.readonlyGeneralErrors().size());
			Assert.assertEquals("There must be the right number of sentence-level errors",
			        expectedSentErrors, sentErrors);
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
	public void invalidStylesheet() throws SynthesisException, InterruptedException,
	        SaxonApiException {
		DefaultTTSService service = new DefaultTTSService() {
			@Override
			public URL getSSMLxslTransformerURL() {
				return this.getClass().getResource("/invalid-sheet.xsl");
			}
		};
		service.engine = new DefaultTTSEngine(service);

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 2);

		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
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
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {
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
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {
				throw new MemoryException(5000);
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
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {
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
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {
				Thread.sleep(6000);
				return Collections.EMPTY_LIST;
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
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {

				if (sentence.contains(key))
					throw new InterruptedException();

				return super.synthesize(sentence, xmlSentence, voice, threadResources, marks,
						expectedMarks, bufferAllocator, retry);
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
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {

				if (sentence.contains(key))
					throw new SynthesisException("error");

				return super.synthesize(sentence, xmlSentence, voice, threadResources, marks,
				        expectedMarks, bufferAllocator, retry);
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
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {

				if (sentence.contains(key)) {
					Thread.sleep(3000);
				}

				return super.synthesize(sentence, xmlSentence, voice, threadResources, marks,
				        expectedMarks, bufferAllocator, retry);
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 0, 2,
		        new CustomVoiceConfig(), ssml);
		Assert.assertEquals("No PCM chunk can be produced", 0, encoder.count);
	}

	@Test
	public void errorInSynthesis3() throws SynthesisException, InterruptedException,
	        SaxonApiException {

		final String key = "__key__";
		String ssml = "<ssml id=\"s1\" xml:lang=\"en\">" + key + "</ssml>";

		DefaultTTSService service = new DefaultTTSService();
		service.engine = new DefaultTTSEngine(service) {

			@Override
			public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
			        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
			        AudioBufferAllocator bufferAllocator, boolean retry)
			        throws SynthesisException, InterruptedException, MemoryException {

				if (sentence.contains(key))
					throw new MemoryException(5000);

				return super.synthesize(sentence, xmlSentence, voice, threadResources, marks,
				        expectedMarks, bufferAllocator, retry);
			}

		};

		DefaultAudioEncoder encoder = new DefaultAudioEncoder();
		runTest(service, (DynamicMarkHandler) service.engine, encoder, 0, 1,
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
			public Optional<String> encode(Iterable<AudioBuffer> pcm, AudioFormat audioFormat,
			        File outputDir, String filePrefix, EncodingOptions options) {
				throw new RuntimeException();
			}

		};

		runTest(service, (DynamicMarkHandler) service.engine, encoder, 2, 0);

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
			public Optional<String> encode(Iterable<AudioBuffer> pcm, AudioFormat audioFormat,
			        File outputDir, String filePrefix, EncodingOptions options)
			        throws Throwable {

				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					interrupted.set(true);
					throw e;
				}

				return Optional.of("uri");
			}
		};

		runTest(service, (DynamicMarkHandler) service.engine, encoder, 1, 0,
		        new VoiceConfigForSingleThread(),
		        "<ssml xml:lang=\"en\" id=\"s1\">test</ssml>");
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

	@Override
	public void printInfo(String message) {
		System.out.println(message);
	}

	@Override
	public void printDebug(String message) {
		System.out.println(message);
	}

	@Override
	public Source resolve(String arg0, String arg1) throws TransformerException {
		return null;
	}
}
