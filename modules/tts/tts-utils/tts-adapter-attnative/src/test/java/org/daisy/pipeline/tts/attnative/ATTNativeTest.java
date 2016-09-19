package org.daisy.pipeline.tts.attnative;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Before running those tests, you must start an ATT server, e.g. :
 * 
 * $./TTSServer -c 8888 -config tts.cfg
 */
public class ATTNativeTest {

	private static String Host = "localhost";
	private static int Port = 8888;
	
	private static class SingleThreadListener implements ATTLibListener {

		int totalSize = 0;
		List<String> markNames = new ArrayList<String>();
		List<Integer> markPos = new ArrayList<Integer>();

		@Override
		public void onRecvAudio(Object handler, ByteBuffer audioBuffer, int size) {
			totalSize += size;
		}

		@Override
		public void onRecvMark(Object handler, String name) {
			markNames.add(name);
			markPos.add(totalSize);
		}
	}

	private static class MultiThreadsListener implements ATTLibListener {
		Map<Object, AtomicInteger> sizes = new HashMap<Object, AtomicInteger>();

		@Override
		public void onRecvAudio(Object handler, ByteBuffer audioBuffer, int size) {
			sizes.get(handler).addAndGet(size);
		}

		@Override
		public void onRecvMark(Object handler, String name) {
		}
	}

	static private void speak(Object handler, long connection, String text) {
		ATTLib.speak(handler, connection,
		        UTF8Converter.convertToUTF8(text, new byte[1]).buffer);
	}

	@Before
	public void setUp() {
		ATTHelpers.loadATT();
	}

	@Test
	public void simpleConnection() {
		long connection = ATTLib.openConnection(Host, Port, 16000, 16);
		Assert.assertNotSame("we could connect", 0, connection);
		ATTLib.closeConnection(connection);
	}

	@Test
	public void getVoiceNames() {
		long connection = ATTLib.openConnection(Host, Port, 16000, 16);
		Assert.assertNotSame("we could connect", 0, connection);
		String[] vnames = ATTLib.getVoiceNames(connection);
		ATTLib.closeConnection(connection);
		Assert.assertTrue(vnames.length > 0);
	}

	@Test
	public void badSampleRate() {
		long connection = ATTLib.openConnection(Host, Port, 16666, 16);
		Assert.assertTrue(0 != connection);
		String[] vnames = ATTLib.getVoiceNames(connection);
		ATTLib.closeConnection(connection);
		Assert.assertEquals(vnames.length, 0);
	}

	@Test
	public void speakEasy() throws IOException {
		SingleThreadListener l = new SingleThreadListener();
		ATTLib.setListener(l);
		long connection = ATTLib.openConnection(Host, Port, 16000, 16);
		Assert.assertNotSame("we could connect", 0, connection);

		speak(this, connection, ATTHelpers.SSML("hello world"));
		ATTLib.closeConnection(connection);
		Assert.assertTrue(l.totalSize > 2000);
	}

	@Test
	public void oneBookmark() {
		SingleThreadListener l = new SingleThreadListener();
		ATTLib.setListener(l);

		long connection = ATTLib.openConnection(Host, Port, 16000, 16);
		Assert.assertNotSame("we could connect", 0, connection);

		String bmark = "bmark1";
		speak(this, connection,  ATTHelpers.SSML("around <mark name=\"" + bmark + "\"/> around"));
		ATTLib.closeConnection(connection);

		Assert.assertTrue(l.totalSize > 2000);
		Assert.assertTrue(1 == l.markNames.size());
		Assert.assertEquals(bmark, l.markNames.get(0));
		
		Assert.assertTrue(Math.abs(l.totalSize / 2 - l.markPos.get(0)) < 10000); //the mark is around the middle
	}

	@Test
	public void twoBookmarks() {
		SingleThreadListener l = new SingleThreadListener();
		ATTLib.setListener(l);
		long connection = ATTLib.openConnection(Host, Port, 16000, 16);
		Assert.assertNotSame("we could connect", 0, connection);
		
		String[] vnames = ATTLib.getVoiceNames(connection);
		Assert.assertTrue("some voices are availables", vnames.length > 0);

		String bmark1 = "bmark1";
		String bmark2 = "bmark-two";

		speak(this, connection,  ATTHelpers.SSML("one two three <mark name=\"" + bmark1
		        + "\"/> four five <mark name=\"" + bmark2 + "\"/>", vnames[0]));
		ATTLib.closeConnection(connection);

		Assert.assertTrue(l.totalSize > 200);
		Assert.assertTrue(2 == l.markNames.size());
		Assert.assertEquals(bmark1, l.markNames.get(0));
		Assert.assertEquals(bmark2, l.markNames.get(1));
		Assert.assertTrue(l.markPos.get(1) - l.markPos.get(0) < l.markPos.get(0));
	}

	static private int[] findSize(final String[] sentences, int startShift)
	        throws InterruptedException {

		final int[] foundSize = new int[sentences.length];
		Thread[] threads = new Thread[sentences.length];

		final MultiThreadsListener listener = new MultiThreadsListener();
		ATTLib.setListener(listener);

		for (int i = 0; i < threads.length; ++i) {
			final int j = i;
			threads[i] = new Thread() {
				public void run() {
					long connection = ATTLib.openConnection(Host, Port, 16000, 16);
					if (connection != 0) {
						speak(this, connection, sentences[j]);
						ATTLib.closeConnection(connection);
						foundSize[j] = listener.sizes.get(this).get();
					}
				}
			};
		}

		for (Thread th : threads)
			listener.sizes.put(th, new AtomicInteger(0));

		for (int i = startShift; i < threads.length; ++i)
			threads[i].start();
		for (int i = 0; i < startShift; ++i)
			threads[i].start();

		for (Thread t : threads)
			t.join();

		return foundSize;
	}

	@Test
	public void multithreadedSpeak() throws InterruptedException {
		final String[] sentences = new String[]{
				 ATTHelpers.SSML("short"),  ATTHelpers.SSML("regular size"),  ATTHelpers.SSML("a bit longer size"),
				 ATTHelpers.SSML("very much longer sentence")
		};

		int[] size1 = findSize(sentences, 0);
		int[] size2 = findSize(sentences, 2);
		for (int i = 0; i < sentences.length; ++i) {
			Assert.assertTrue(0 != size1[i]);
		}

		Assert.assertArrayEquals(size1, size2);
	}

	@Test
	public void unicode() {
		SingleThreadListener l = new SingleThreadListener();
		ATTLib.setListener(l);

		long connection = ATTLib.openConnection(Host, Port, 16000, 16);
		Assert.assertTrue(0 != connection);

		speak(this, connection,  ATTHelpers.SSML("𝄞𝄞𝄞𝄞 水水水水水 𝄞水𝄞水𝄞水𝄞水 test 国Ø家Ť标准 ĜæŘ ß ŒÞ ๕"));
		ATTLib.closeConnection(connection);

		Assert.assertTrue(l.totalSize > 2000);
	}

	@Test
	public void multiVoices() {
		SingleThreadListener l = new SingleThreadListener();
		ATTLib.setListener(l);

		long connection = ATTLib.openConnection(Host, Port, 16000, 16);
		Assert.assertTrue(0 != connection);
		String[] vnames = ATTLib.getVoiceNames(connection);
		Set<Integer> sizes = new HashSet<Integer>();
		for (int i = 0; i < vnames.length; ++i) {
			l.totalSize = 0;
			speak(this, connection,  ATTHelpers.SSML("hello world", vnames[i]));
			sizes.add(l.totalSize);
		}
		ATTLib.closeConnection(connection);
		Assert.assertTrue(vnames.length > 0);

		//the diversity will be very low if the voice names are not properly retrieved or used
		float diversity = Float.valueOf(sizes.size()) / vnames.length;

		Assert.assertTrue(diversity >= 0.6);
	}
}
