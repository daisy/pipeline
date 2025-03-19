package org.daisy.pipeline.tts.synthesize.calabash.impl;

import static java.nio.file.Files.newDirectoryStream;
import static java.util.Collections.emptyMap;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.StreamSupport;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;

import org.daisy.common.messaging.AbstractMessageAccessor;
import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.audio.lame.impl.LameEncoder;
import org.daisy.pipeline.tts.AudioBufferTracker;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.awsPolly.impl.AWSPollyTTSService;
import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadableDocument;
import com.xmlcalabash.io.WritableDocument;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.runtime.XAtomicStep;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;
import net.sf.saxon.s9api.QName;

@RunWith(JUnitParamsRunner.class)
public class AWSPollyFullTxt2TtsRunTestsV3 {

	public static List<String> data() throws FileNotFoundException, IOException, URISyntaxException {
		DirectoryStream<Path> files = newDirectoryStream(Paths.get(getTestResourceFilePath("dtbooks")), "*.ssml.xml");
		return StreamSupport.stream(spliteratorUnknownSize(files.iterator(), Spliterator.ORDERED), false)
				.map(p -> p.toFile().getName())
				.map(s -> s.substring(0, s.length() - 9))
				.collect(toList());
	}

	@ClassRule
	public static DaisyCoreRule daisyCoreRule = DaisyCoreRule.withTTSService(() -> createTtsService());

	@Rule
	// should add a config here
	public DaisyCoreRule.XProcRuntimeRule runtimeRule = daisyCoreRule.getRuntimeRule();

	public static TTSService createTtsService() {
		try {
			TTSService ttsService = new AWSPollyTTSService();
			activate(ttsService);
			return ttsService;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	@Parameters(method = "data")
	@TestCaseName("{0}.ssml.xml")
	public void testBook(String bookName) throws Throwable {
		Path outputDir = Paths.get("last-tested-results");
		if (!outputDir.toFile().exists()) {
			outputDir.toFile().mkdir();
		}
		XProcStep procStep = runtimeRule.createStep(new QName("", "qname"),
				"name",
				Map.of(
						"source", getTestResourceFilePath("dtbooks/" + bookName + ".ssml.xml"),
						"config", getTestResourceFilePath("tts-default-config.xml")),
				Map.of(
						"result", outputDir.resolve("pollyTest" + bookName).toUri().toString(),
						"status", outputDir.resolve("pollyStatus" + bookName).toUri().toString(),
						"log", outputDir.resolve("pollyLog" + bookName).toUri().toString()),
				emptyMap(),
				emptyMap()
			);
		procStep.run();
		System.out.println("Test run " + bookName + " finished. The MessageAppender logs are:");
		for (Message msg: runtimeRule.getMessages()) {
			Throwable ex = msg.getThrowable();
			System.out.println("" + msg.getTimeStamp() + ' ' + msg.getLine() + ':' + msg.getColumn() + ' ' + msg.getLevel() + ' ' + msg.getOwnerId() + ':' + msg.getSequence() + (ex == null? '-':'!') + ' ' + msg.getText());
			if (ex != null) {
				ex.printStackTrace();
			}
		}
		System.out.println("End of " + bookName + " MessageAppender logs.");
		assertThat("Encoding was not successful", procStep,
				allOf(
						matchTransformed(s -> runtimeRule.getErrorCounter(s), is(0), "Errors counter"),
						matchTransformed(s -> runtimeRule.getBufferTracker(s).getUnreleasedTTSMem(), is(0), "Reserved TTS buffers"),
						matchTransformed(s -> runtimeRule.getBufferTracker(s).getUnreleasedEncondingMem(), is(0), "Reserved encoding buffers")
					));
	}

	public static String getTestResourceFilePath(String resource) throws FileNotFoundException, URISyntaxException {
		URL url = AWSPollyFullTxt2TtsRunTests.class.getClassLoader().getResource(resource);
		if (url == null) {
			throw new FileNotFoundException("Could not find resource " + resource + " to return it as a file");
		}
		return new File(url.toURI()).getAbsolutePath();
	}

	public static class DaisyCoreRule extends ExternalResource {

		private URIResolver uriResolver;
		private TTSRegistry ttsRegistry;
		private SynthesizeProvider provider;
		private Supplier<? extends Iterable<TTSService>> services;

		public DaisyCoreRule(Supplier<? extends Iterable<TTSService>> services) {
			this.services = services;
		}

		public static DaisyCoreRule withTTSService(Supplier<TTSService> service) {
			return new DaisyCoreRule(() -> List.of(service.get()));
		}

		protected URIResolver createResolver() {
			return TransformerFactory.newDefaultInstance().getURIResolver();
		}

		protected TTSRegistry createRegistry(URIResolver resolver) {
			TTSRegistry registry = new TTSRegistry();
			registry.setURIResolver(resolver);
			return registry;
		}

		protected SynthesizeProvider createProvider(TTSRegistry registry, URIResolver resolver) {
			AudioServices audioServices = new AudioServices();
			audioServices.addEncoder(new LameEncoder());
			SynthesizeProvider provider = new SynthesizeProvider();
			provider.setURIResolver(resolver);
			provider.setTTSRegistry(registry);
			provider.setAudioServices(audioServices);
			services.get().forEach(registry::addTTS);
			return provider;
		}

		public static class TestsMessageBus extends AbstractMessageAccessor {

			private final MessageAppender msg;
			private final Iterable<Message> msgList;
			private final List<Consumer<Integer>> callbacks = new ArrayList<>();

			public TestsMessageBus(String ownerId) {
				this(ownerId, Level.TRACE);
			}

			public TestsMessageBus(String ownerId, Level threshold) {
				super(threshold);
				msg = new MessageBuilder().withOwnerId(ownerId).onUpdated(this::notify).build();
				msgList = Arrays.asList((Message)msg);
			}

			@Override
			protected Iterable<Message> allMessages() {
				return msgList;
			}

			@Override
			public void listen(Consumer<Integer> callback) {
				callbacks.add(callback);
				
			}

			@Override
			public void unlisten(Consumer<Integer> callback) {
				callbacks.remove(callback);
			}

			public void close() {
				msg.close();
			}

			private void notify(Integer value) {
				callbacks.forEach(c -> c.accept(value));
			}

		}

		public class XProcRuntimeRule implements TestRule {

			private final XProcConfiguration config;
			private XProcRuntime runtime;
			private TestsMessageBus msgBus;

			protected XProcRuntimeRule(XProcConfiguration config) {
				this.config = config;
			}

			public XProcStep createStep(QName type, String name,
					Map<String, String> inputFiles, Map<String, String> outputFiles,
					Map<QName, RuntimeValue> options, Map<QName, RuntimeValue> parameters) {
				Step step = new Step(runtime, null, type, name);
				XAtomicStep atomicStep = new XAtomicStep(runtime, step, null) {
				    public QName getType() {
				    	return step.getDeclaredType();
				    }
				};
				XProcStep procStep = provider.newStep(runtime, atomicStep);
				inputFiles.forEach((s, p) -> procStep.setInput(s, new ReadableDocument(runtime, null, p, "file:relative", null)));
				outputFiles.forEach((s, p) -> procStep.setOutput(s, new WritableDocument(runtime, p, null)));
				// TODO: add options and parameters
				return procStep;
			}

			public List<Message> getMessages() {
				return msgBus.getAll();
			}

			public int getErrorCounter(XProcStep step) {
				try {
					Field attr = SynthesizeStep.class.getDeclaredField("mErrorCounter");
					attr.setAccessible(true);
					int ret = attr.getInt(step);
					attr.setAccessible(false);
					return ret;
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			public AudioBufferTracker getBufferTracker(XProcStep step) {
				try {
					Field attr = SynthesizeStep.class.getDeclaredField("mAudioBufferTracker");
					attr.setAccessible(true);
					Object ret = attr.get(step);
					attr.setAccessible(false);
					return (AudioBufferTracker) ret;
				} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Statement apply(Statement base, org.junit.runner.Description description) {
		        return new Statement() {
		            @Override
		            public void evaluate() throws Throwable {
						runtime = createRuntime(config);
						try {
							msgBus = new TestsMessageBus(description.getDisplayName(), Level.TRACE); // need one appender assigned to this thread
							try {
								base.evaluate();
							} finally {
								msgBus.close(); // close the appender to allow more tests to be run
							}
						} finally {
							runtime.close();
						}
		            }
		        };
			}

		}

		protected XProcRuntime createRuntime(XProcConfiguration config) {
			// should add a config here
			return new XProcRuntime(config);
		}

		@Override
		protected void before() throws Throwable {
			uriResolver = createResolver();
			ttsRegistry = createRegistry(uriResolver);
			provider = createProvider(ttsRegistry, uriResolver);
		}

		public XProcRuntimeRule getRuntimeRule() {
			return getRuntimeRule(new XProcConfiguration("he", false));
		}

		public XProcRuntimeRule getRuntimeRule(XProcConfiguration config) {
			return new XProcRuntimeRule(config);
		}

		@Override
		protected void after() {
		}
		
	}

	private static void activate(TTSService svc) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = AWSPollyTTSService.class.getDeclaredMethod("loadSSMLadapter");
		m.setAccessible(true);
		m.invoke(svc);
		m.setAccessible(false);
	}

	private static class LambdaMatcher<T, U> extends FeatureMatcher<T, U> {

		private final Function<T, U> lambda;

		public LambdaMatcher(Function<T, U> lambda, Matcher<? super U> subMatcher, String featureName) {
			this(lambda, subMatcher, featureName, featureName);
		}

		public LambdaMatcher(Function<T, U> lambda, Matcher<? super U> subMatcher, String featureDescription, String featureName) {
			super(subMatcher, featureDescription, featureName);
			this.lambda = lambda;
		}

		@Override
		protected U featureValueOf(T actual) {
			return lambda.apply(actual);
		}
		
	}

	public static <T, U> Matcher<T> matchTransformed(Function<T, U> lambda, Matcher<? super U> matcher, String name) {
		return new LambdaMatcher<>(lambda, matcher, name);
	}

	private static class FullDescribeAllOf<T> extends DiagnosingMatcher<T> {
		private final Iterable<Matcher<? super T>> matchers;

		public FullDescribeAllOf(Iterable<Matcher<? super T>> matchers) {
			this.matchers = matchers;
		}

		@Override
		public boolean matches(Object o, Description mismatch) {
			boolean result = true;
			for (Matcher<? super T> matcher : matchers) {
				if (!matcher.matches(o)) {
					if (!result) {
						mismatch.appendText(", ");
					}
					mismatch.appendDescriptionOf(matcher).appendText(" ");
					matcher.describeMismatch(o, mismatch);
					result = false;
				}
			}
			return result;
		}

		@Override
		public void describeTo(Description description) {
			description.appendList("(", " " + "and" + " ", ")", matchers);
		}

	}

    /**
     * Creates a matcher that matches if the examined object matches <b>ALL</b> of the specified matchers and whose error explanation contains all the failures.
     * For example:
     * <pre>assertThat("myValue", allOf(startsWith("my"), containsString("Val")))</pre>
     */
    @SafeVarargs
    public static <T> Matcher<T> allOf(Matcher<? super T>... matchers) {
        return new FullDescribeAllOf<>(Arrays.asList(matchers));
    }

}
