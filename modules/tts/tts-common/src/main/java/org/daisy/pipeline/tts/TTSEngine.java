package org.daisy.pipeline.tts;

import java.util.Collection;
import java.util.List;

import javax.sound.sampled.AudioFormat;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
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
	 * @param sentence is the sentence to synthesize. It is the serialized
	 *            result of the SSML conversion performed by the XSLT whose URL
	 *            is returned by TTSService.getSSMLxslTransformerURL().
	 * @param xmlSentence is the XML version of @param sentence. It should not
	 *            be used unless one needs information that has been lost during
	 *            the serialization process (e.g. marks or CSS)
	 * @param voice is the voice the synthesizer must use. It is guaranteed to
	 *            be one of those returned by getAvailableVoices(). This
	 *            parameter can't be null.
	 * @param threadResources is the object returned by
	 *            allocateThreadResource(). It may contain small persistent
	 *            buffers, opened file streams, TCP connections and so on. The
	 *            boolean field 'released' is guaranteed to be false, i.e. the
	 *            resource provided is always valid and will remain so during
	 *            the call.
	 * @param marks are the returned pairs (markName, offset-in-output)
	 *            corresponding to the ssml:marks of @param sentence. The order
	 *            must be kept. The provided list is always empty. The
	 *            offset-in-outputs are relative to the new buffers returned by
	 *            synthesize(). That is, they start at 0. If the service doesn't
	 *            handle SSML marks (i.e. endingmark() returns null). This
	 *            parameter may be set to null.
	 * @param expectedMarks are the mark names extracted from @param xmlSentence. The
	 * 	          TTS engine can use them if it doesn't play well with complicated/long
	 *            mark names, but it will harder to detect errors. It does include
	 *            the TTS provider's ending mark. The ending-mark can be set to anything if
	 *            the TTS processor or @param voice cannot handle marks. 
	 * @param bufferAllocator is the object that the TTS Service must use to
	 *            allocate new audio buffers.
	 * @param retry is true when this is not the first time the thread attempts
	 *            to synthesize @param sentence. In such cases, you may
	 *            reinitialize the connection to remote TTS servers if there are
	 *            any.
	 * 
	 * 
	 * @return a list of adjacent PCM chunks produced by the TTS processor.
	 */
	abstract public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
	        Voice voice, TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
	        AudioBufferAllocator bufferAllocator, boolean retry) throws SynthesisException,
	        InterruptedException, MemoryException;

	/**
	 * @return the audio format (sample rate etc.) of the data produced by
	 *         synthesize(). The engine is assumed to use the same audio format
	 *         every time. It is okay to return null before the first call to
	 *         synthesize(), though it is better to return non-null values for
	 *         optimization purposes. It must, however, return a non-null value
	 *         once synthesize() has been called. Must be thread-safe.
	 */
	abstract public AudioFormat getAudioOutputFormat();

	/**
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
	 * @return the name of the mark that will be added to check whether all the
	 *         SSML have been successfully synthesized. TTS processors that
	 *         cannot handle marks must return null. Must be thread-safe.
	 */
	public String endingMark() {
		return null; //marks not handled
	}
}
