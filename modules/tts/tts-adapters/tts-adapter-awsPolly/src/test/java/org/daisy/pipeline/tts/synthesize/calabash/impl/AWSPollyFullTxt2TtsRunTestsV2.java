package org.daisy.pipeline.tts.synthesize.calabash.impl;

import static java.nio.file.Files.newDirectoryStream;
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
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadableDocument;
import com.xmlcalabash.io.WritableDocument;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;

@RunWith(Parameterized.class)
public class AWSPollyFullTxt2TtsRunTestsV2 {

	private final String bookName;

	public AWSPollyFullTxt2TtsRunTestsV2(String bookName) {
		super();
		this.bookName = bookName;
	}

	@Parameters(name = "{0}.ssml.xml")
	public static List<String> data() throws FileNotFoundException, IOException, URISyntaxException {
		DirectoryStream<Path> files = newDirectoryStream(Paths.get(getTestResourceFilePath("dtbooks")), "*.ssml.xml");
		return StreamSupport.stream(spliteratorUnknownSize(files.iterator(), Spliterator.ORDERED), false)
				.map(p -> p.toFile().getName())
				.map(s -> s.substring(0, s.length() - 9))
				.collect(toList());
	}

	@Test
	public void testBook() throws Throwable {
		testBook(bookName);
	}

	public void testBook(String bookName) throws Throwable {
		TTSService ttsService = new AWSPollyTTSService();
		URIResolver uriResolver = TransformerFactory.newDefaultInstance().getURIResolver();
		TTSRegistry ttsRegistry = new TTSRegistry();
		ttsRegistry.setURIResolver(uriResolver);
		ttsRegistry.addTTS(ttsService);
		AudioServices audioServices = new AudioServices();
		audioServices.addEncoder(new LameEncoder());
		SynthesizeProvider provider = new SynthesizeProvider();
		provider.setURIResolver(uriResolver);
		provider.setTTSRegistry(ttsRegistry);
		provider.setAudioServices(audioServices);
		activate(ttsService);
		XProcConfiguration config = new XProcConfiguration("he", false);
		// should add a config here
		XProcRuntime runtime = new XProcRuntime(config);
		Step step = new Step(runtime,null,new QName("","qname"),"name");
		XAtomicStep atomicStep = new XAtomicStep(runtime,step,null) {
		    public QName getType() {
		        return new QName("","qname");
		    }
		};
		XProcStep procStep = provider.newStep(runtime, atomicStep);
		procStep.setInput("source", new ReadableDocument(runtime, null, getTestResourceFilePath("dtbooks/" + bookName + ".ssml.xml"), "file:relative", null));
		procStep.setInput("config", new ReadableDocument(runtime, null, getTestResourceFilePath("tts-default-config.xml"), "file:relative", null));
		Path outputDir = Paths.get("last-tested-results");
		if (!outputDir.toFile().exists()) {
			outputDir.toFile().mkdir();
		}
		procStep.setOutput("result", new WritableDocument(runtime, outputDir.resolve("pollyTest" + bookName).toUri().toString(), null));
		procStep.setOutput("status", new WritableDocument(runtime, outputDir.resolve("pollyStatus" + bookName).toUri().toString(), null));
		procStep.setOutput("log", new WritableDocument(runtime, outputDir.resolve("pollyLog" + bookName).toUri().toString(), null));
		TestsMessageBus msgBus = new TestsMessageBus("testSimpleDtBook" + bookName, Level.TRACE); // need one appender assigned to this thread
		try {
			procStep.run();
			System.out.println("Test run " + bookName + " finished. The MessageAppender logs are:");
			for (Message msg: msgBus.getAll()) {
				Throwable ex = msg.getThrowable();
				System.out.println("" + msg.getTimeStamp() + ' ' + msg.getLine() + ':' + msg.getColumn() + ' ' + msg.getLevel() + ' ' + msg.getOwnerId() + ':' + msg.getSequence() + (ex == null? '-':'!') + ' ' + msg.getText());
				if (ex != null) {
					ex.printStackTrace();
				}
			}
			System.out.println("End of " + bookName + " MessageAppender logs.");
			assertThat("Encoding was not successful", procStep,
					allOf(
							matchTransformed(s -> getErrorCounter(s), is(0), "Errors counter"),
							matchTransformed(s -> getBufferTracker(s).getUnreleasedTTSMem(), is(0), "Reserved TTS buffers"),
							matchTransformed(s -> getBufferTracker(s).getUnreleasedEncondingMem(), is(0), "Reserved encoding buffers")
						));
		} finally {
			msgBus.close(); // close the appender to allow more tests to be run
		}
	}

	public static String getTestResourceFilePath(String resource) throws FileNotFoundException, URISyntaxException {
		URL url = AWSPollyFullTxt2TtsRunTests.class.getClassLoader().getResource(resource);
		if (url == null) {
			throw new FileNotFoundException("Could not find resource " + resource + " to return it as a file");
		}
		return new File(url.toURI()).getAbsolutePath();
	}

	private void activate(TTSService svc) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Method m = AWSPollyTTSService.class.getDeclaredMethod("loadSSMLadapter");
		m.setAccessible(true);
		m.invoke(svc);
		m.setAccessible(false);
	}

	private AudioBufferTracker getBufferTracker(XProcStep step) {
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

	private Integer getErrorCounter(XProcStep step) {
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
