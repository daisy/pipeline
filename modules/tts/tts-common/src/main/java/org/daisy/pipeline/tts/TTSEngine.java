package org.daisy.pipeline.tts;

import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.saxon.xslt.ThreadUnsafeXslTransformer;
import org.daisy.common.saxon.xslt.XslTransformCompiler;
import org.daisy.pipeline.audio.AudioUtils;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;

/**
 * Classes that inherit from TTSEngine are the ones that deal with adapting
 * external TTS processors (e.g. eSpeak and SAPI) to the Pipeline interface,
 * which is used notably by the TextToPcm threads in the conversion from SSML to
 * mp3. TTSEngines are meant to be allocated for every new session so that the
 * end user can change the TTSEngine's parameters between two Pipeline jobs.
 * Most of the methods of TTSEngine must be thread-safe.
 */
public abstract class TTSEngine {

	public static class SynthesisResult {
		/**
		 * The audio produced by the TTS processor. The {@link AudioFormat} of
		 * the stream must be the same every time the same voice is used.
		 */
		public final AudioInputStream audio;
		/**
		 * The mark offsets (in bytes) corresponding to the
		 * <code>ssml:mark</code> elements of the input SSML. The order must be
		 * kept. The offsets are relative to the {@link AudioInputStream}. That
		 * is, they start at 0. May be <code>null</code> if the TTS processor
		 * doesn't handle SSML marks.
		 */
		public final List<Integer> marks;
		public SynthesisResult(AudioInputStream audio) {
			this(audio, null);
		}
		public SynthesisResult(AudioInputStream audio, List<Integer> marks) {
			this.audio = audio;
			this.marks = marks;
		}
	}

	/**
	 * @param provider is the service from which the TTSEngine has been
	 *            allocated.
	 */
	protected TTSEngine(TTSService provider) {
		mProvider = provider;
	}

	/**
	 * @return the service from which the TTSEngine has been allocated.
	 */
	public TTSService getProvider() {
		return mProvider;
	}

	protected TTSService mProvider;

	/**
	 * This method must be thread-safe. But @param threadResources is here to
	 * prevent the service from locking internal resources.
	 * 
	 * @param sentence is the sentence to synthesize, as an SSML node. The node
	 *                 is expected to be an {@code s} element with an
	 *                 {@code xml:lang} attribute. It is however advised that
	 *                 implementations do not assume this is the case. It can
	 *                 also not be assumed that the node is a root element.
	 * @param voice is the voice the synthesizer must use. It is guaranteed to
	 *            be one of those returned by getAvailableVoices(). This
	 *            parameter can't be null.
	 * @param threadResources is the object returned by
	 *            allocateThreadResource(). It may contain small persistent
	 *            buffers, opened file streams, TCP connections and so on. The
	 *            boolean field 'released' is guaranteed to be false, i.e. the
	 *            resource provided is always valid and will remain so during
	 *            the call.
	 *
	 * @return a {@link SynthesisResult} object containing the audio data and the
	 *         mark offsets.
	 */
	abstract public SynthesisResult synthesize(XdmNode sentence,
	                                           Voice voice,
	                                           TTSResource threadResources)
		throws SynthesisException, InterruptedException;

	/**
	 * Return the list of available voices for this engine. Voices that come before other voices in
	 * the list are prioritized by {@link VoiceManager} when there are no other discriminating
	 * factors.
	 *
	 * Need not be thread-safe. This method is called from the main thread.
	 */
	abstract public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException;

	/**
	 * This method must be thread-safe. It allocates new resources (such as TCP
	 * connections) unique for each thread. Allocations can be made on-the-fly
	 * from different threads. It must not catch InterruptuedExceptions.
	 * 
	 * @return the resources. Must not be null.
	 * @throws SynthesisException
	 */
	public TTSResource allocateThreadResources() throws SynthesisException,
	        InterruptedException {
		return new TTSResource();
	}

	/**
	 * This method must be thread-safe. Deallocations may be performed from
	 * different threads but are always performed in the same thread as the one
	 * exploiting @param resources.
	 * 
	 * @param resources is the object returned by allocateThreadResource()
	 */
	public void releaseThreadResources(TTSResource resources) throws SynthesisException,
	        InterruptedException {
	}

	/**
	 * Force interruption of the execution of synthesize() when the thread-level
	 * interruption is not enough to make synthesize() finish. Must be
	 * thread-safe, although the method must not wait for locks.
	 * 
	 * @param resource is the same as the one provided to synthesize()
	 */
	public void interruptCurrentWork(TTSResource resource) {
	}

	/**
	 * Need not be thread-safe. This method is called from the main thread.
	 */
	public int getOverallPriority() {
		return 1;
	}

	/**
	 * @return the number of text-to-pcm threads reserved for this TTS
	 *         processor. Different values than zero make only sense if the TTS
	 *         speed is limited by the number of opened resources rather than by
	 *         the number of cores. In such cases, we want to maximize the time
	 *         spent on using the TTS resources by avoiding using threads for
	 *         something else (e.g. audio encoding or calling other TTS
	 *         processors).
	 */
	public int reservedThreadNum() {
		return 0;
	}

	/**
	 * @return the average number of milliseconds the TTS processors is expected
	 *         to spend to process a single word on a single CPU core. It
	 *         doesn't need to be accurate. It is used for tuning the timeouts.
	 */
	public int expectedMillisecPerWord() {
		return 100;
	}

	/**
	 * @return {@code true} if the TTS engine handles SSML marks, {@code false}
	 *         otherwise. Must be thread-safe.
	 */
	public boolean handlesMarks() {
		return false;
	}

	/**
	 * Whether the TTS engine handles
	 * <a href="https://www.w3.org/TR/speech-synthesis11/#S3.2.4"><code>prosody</code></a>
	 * elements with a {@code rate} attribute. If this method returns {@code true}, the
	 * engine is assumed to support all of the following values:
	 *
	 * <ul>
	 *   <li>"x-slow", "slow", "medium", "fast", "x-fast", or "default". </li>
	 *   <li>A non-negative percentage, which acts as a multiplier of the default
	 *   rate.</li>
	 *   <li>A number that represents speaking rate in words per minute. Note that this
	 *   does not come from the SSML specification, but it is a value of the CSS
	 *   <a href="https://www.w3.org/TR/CSS2/aural.html#propdef-speech-rate"><code>speech-rate</code></a>
	 *   property.</li>
	 * </ul>
	 *
	 * All values, except for absolute numbers, are relative to the default rate, which
	 * is determined by the {@code org.daisy.pipeline.tts.speech-rate} property.
	 */
	public boolean handlesSpeakingRate() {
		return false;
	}

	/* -------------------------------------------- */
	/*               HELPER FUNCTIONS               */
	/* -------------------------------------------- */

	/**
	 * Transform an SSML node to a string using a given XSLT and parameter map
	 *
	 * If this method is called from {@link #synthesize}, the XSLT should not
	 * assume that the SSML node is a root element.
	 */
	protected String transformSsmlNodeToString(XdmNode ssml, URL xslt, Map<String,Object> params)
			throws IOException, SaxonApiException {
		return compileXslt(xslt, ssml.getUnderlyingNode().getConfiguration())
			.transformToString(ssml, params);
	}

	/**
	 * Compile an XSLT.
	 */
	private ThreadUnsafeXslTransformer compileXslt(URL xslt, Configuration config)
			throws SaxonApiException, IOException {
		Map<URL,ThreadUnsafeXslTransformer> cache = compiledXslts.get().get(config);
		if (cache == null) {
			cache = new HashMap<URL,ThreadUnsafeXslTransformer>();
			compiledXslts.get().put(config, cache);
		}
		ThreadUnsafeXslTransformer transformer = cache.get(xslt);
		if (transformer == null) {
			transformer = new XslTransformCompiler(config)
				.compileStylesheet(xslt.openStream())
				.newTransformer();
			cache.put(xslt, transformer);
		}
		return transformer;
	}

	// Normally the same thread uses only one Configuration so
	// ThreadLocal<Map<URL,ThreadUnsafeXslTransformer>> would also work, but we do it this way to be safe.
	private static ThreadLocal<Map<Configuration,Map<URL,ThreadUnsafeXslTransformer>>> compiledXslts
		= ThreadLocal.withInitial(() -> {
				return new HashMap<Configuration,Map<URL,ThreadUnsafeXslTransformer>>(); });

	/**
	 * Create an {@link AudioInputStream} from an {@link AudioFormat} and the audio data.
	 */
	protected static AudioInputStream createAudioStream(AudioFormat format, byte[] data) {
		return AudioUtils.createAudioStream(format, data);
	}

	protected static AudioInputStream createAudioStream(AudioFormat format, ByteArrayInputStream data) {
		return AudioUtils.createAudioStream(format, data);
	}

	/**
	 * Create a {@link AudioInputStream} from a {@link InputStream}.
	 *
	 * This is to work around a bug in {@link javax.sound.sampled.AudioSystem}
	 * which may return {@link AudioInputStream} with a wrong {@link
	 * AudioInputStream#getFrameLength()}.
	 */
	protected static AudioInputStream createAudioStream(InputStream stream)
			throws UnsupportedAudioFileException, IOException {
		return AudioUtils.createAudioStream(stream);
	}
}
