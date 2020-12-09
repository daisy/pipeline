package org.daisy.pipeline.tts.synthesize.calabash.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.Voice;

import net.sf.saxon.s9api.XdmNode;

class TimedTTSExecutor {

	private static final int FIRST_CHARACTERS = 2500;
	private Map<TTSEngine,Integer> totalCharacters
		= Collections.synchronizedMap(new HashMap<>()); // only count first 2500 characters
	private Map<TTSEngine,Integer> maxMicrosecPerCharacter
		= Collections.synchronizedMap(new HashMap<>());

	/**
	 * The maximum number of milliseconds the TTS engine is allowed to spend on a single word. This
	 * takes into account {@see TTSEngine#expectedMillisecPerWord()}, the number of words processed
	 * so far, and the time the engine has spent on previous sentences. A large enough safety factor
	 * is taken into account for long words and other deviations.
	 */
	private int maximumMillisec(TTSEngine engine, int sentenceSize) {
		// adding an offset because the processing time is not always directly proportional to the
		// length of the sentence
		int offset = 1000; // 1 sec
		int wordCount = 1 + sentenceSize / 6; // ~6 characters/word
		Integer n = totalCharacters.get(engine);
		if (n == null)
			// initially base timeout on engine.expectedMillisecPerWord()
			return offset + 10 * engine.expectedMillisecPerWord() * wordCount;
		else {
			// in the long run base timeout on maxActualMicrosecPerCharacter
			Integer maxActualMicrosecPerCharacter = maxMicrosecPerCharacter.get(engine);
			if (n < FIRST_CHARACTERS)
				// interpolate
				return offset
					+ 3 * n * maxActualMicrosecPerCharacter * sentenceSize / 1000 / FIRST_CHARACTERS
					+ 10 * (FIRST_CHARACTERS - n) * engine.expectedMillisecPerWord() * wordCount / FIRST_CHARACTERS;
			else
				return offset + 3 * maxActualMicrosecPerCharacter * sentenceSize / 1000;
		}
	}

	public Collection<AudioBuffer> synthesizeWithTimeout(
		TTSTimeout timeout, TTSTimeout.ThreadFreeInterrupter interrupter, TTSLog.Entry log,
		String sentence, XdmNode xmlSentence, int sentenceSize, TTSEngine engine, Voice voice,
		TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
		AudioBufferAllocator bufferAllocator, boolean retry
	) throws SynthesisException, MemoryException, TimeoutException {
		long startTime = System.currentTimeMillis();
		int timeoutSec = maximumMillisec(engine, sentenceSize) / 1000;
		if (log != null)
			log.setTimeout(timeoutSec);
		try {
			timeout.enableForCurrentThread(interrupter, timeoutSec);
			Collection<AudioBuffer> result = engine.synthesize(
				sentence, xmlSentence, voice, threadResources, marks, expectedMarks, bufferAllocator, retry);
			long millisecElapsed = System.currentTimeMillis() - startTime;
			if (log != null)
				log.setTimeElapsed((float)millisecElapsed / 1000);
			synchronized(maxMicrosecPerCharacter) {
				Long m = millisecElapsed * 1000 / sentenceSize;
				int n = 1000; // don't go below 1 ms
				if (maxMicrosecPerCharacter.containsKey(engine))
					n = maxMicrosecPerCharacter.get(engine);
				else
					maxMicrosecPerCharacter.put(engine, n);
				if (m > n)
					maxMicrosecPerCharacter.put(engine, m.intValue());
			}
			synchronized(totalCharacters) {
				Integer n = totalCharacters.get(engine);
				if (n == null) n = 0;
				if (n < FIRST_CHARACTERS)
					totalCharacters.put(engine, n + sentenceSize);
			}
			return result;
		} catch (InterruptedException e) {
			// assuming that the thread was interrupted by the timeout
			throw new TimeoutException(timeoutSec, e);
		} finally {
			timeout.disable();
		}
	}

	static class TimeoutException extends RuntimeException {

		private final int seconds;

		private TimeoutException(int seconds, InterruptedException cause) {
			super("TTS timed out after " + seconds + " seconds", cause);
			this.seconds = seconds;
		}

		/**
		 * @return The timeout in seconds.
		 */
		public int getSeconds() {
			return seconds;
		}
	}
}
