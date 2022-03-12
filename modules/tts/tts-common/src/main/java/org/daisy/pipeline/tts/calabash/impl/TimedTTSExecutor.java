package org.daisy.pipeline.tts.calabash.impl;

import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.toIntExact;
import java.util.HashMap;
import java.util.Map;

import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.Voice;

import net.sf.saxon.s9api.XdmNode;

class TimedTTSExecutor {

	private static final int FIRST_CHARACTERS = 2500;
	private static final int SHORT_SENTENCE_THRESHOLD = 25;
	private Map<TTSEngine,Integer> totalCharacters = new HashMap<>(); // only count first 2500 characters
	private Map<TTSEngine,Integer> maxMicrosecPerCharacter = new HashMap<>();
	private Map<TTSEngine,Integer> maxMillisecOfShortSentence = new HashMap<>(); // shorter than 25 characters

	/**
	 * The maximum number of milliseconds the TTS engine is allowed to spend on a single word. This
	 * takes into account {@link TTSEngine#expectedMillisecPerWord()}, the number of words processed
	 * so far, and the time the engine has spent on previous sentences. A large enough safety factor
	 * is taken into account for long words and other deviations.
	 */
	private int maximumMillisec(TTSEngine engine, int sentenceSize) {
		int wordCount = 1 + sentenceSize / 6; // ~6 characters/word
		Integer n;
		synchronized(totalCharacters) {
			n = totalCharacters.get(engine);
			Integer safeMillisec = null;
			if (n == null || n < FIRST_CHARACTERS)
				// initially base timeout on engine.expectedMillisecPerWord()
				// adding initial offset because the processing time is not always directly
				// proportional to the length of the sentence
				safeMillisec = addExact(5000,
				                        multiplyExact(wordCount,
				                                      multiplyExact(10,
				                                                    engine.expectedMillisecPerWord())));
			if (n == null)
				return safeMillisec;
			else {
				// in the long run base timeout on maxMicrosecPerCharacter and maxMillisecOfShortSentence
				Integer estimatedMicrosec = multiplyExact(maxMicrosecPerCharacter.get(engine), sentenceSize);
				Integer minMillisec = maxMillisecOfShortSentence.get(engine);
				if (minMillisec != null) {
					if (estimatedMicrosec < multiplyExact(minMillisec, 1000))
						estimatedMicrosec = multiplyExact(minMillisec, 1000);
				} else if (sentenceSize < SHORT_SENTENCE_THRESHOLD)
					estimatedMicrosec = multiplyExact(maxMicrosecPerCharacter.get(engine), SHORT_SENTENCE_THRESHOLD);
				if (n < FIRST_CHARACTERS)
					// interpolate
					return toIntExact(
						addExact(
							multiplyExact((long)safeMillisec, FIRST_CHARACTERS - n) / FIRST_CHARACTERS,
							multiplyExact((long)estimatedMicrosec, 3 * n) / 1000 / FIRST_CHARACTERS));
				else
					return multiplyExact(3, estimatedMicrosec) / 1000;
			}
		}
	}

	public SynthesisResult synthesizeWithTimeout(
		TTSTimeout timeout, TTSTimeout.ThreadFreeInterrupter interrupter, TTSLog.Entry log,
		XdmNode sentence, int sentenceSize, TTSEngine engine, Voice voice,
		TTSResource threadResources
	) throws SynthesisException, TimeoutException {
		long startTime = System.currentTimeMillis();
		int timeoutSec = maximumMillisec(engine, sentenceSize) / 1000;
		if (log != null)
			log.setTimeout(timeoutSec);
		timeout.enableForCurrentThread(interrupter, timeoutSec);
		try {
			SynthesisResult result = engine.synthesize(
				sentence, voice, threadResources);
			Long millisecElapsed = System.currentTimeMillis() - startTime;
			if (log != null)
				log.setTimeElapsed((float)millisecElapsed / 1000);
			synchronized(totalCharacters) {
				Long microsecElapsedPerCharacter = sentenceSize == 0
					? 0
					: multiplyExact(millisecElapsed, 1000) / sentenceSize;
				int max = 1000; // don't go below 1 ms
				if (maxMicrosecPerCharacter.containsKey(engine))
					max = maxMicrosecPerCharacter.get(engine);
				else
					maxMicrosecPerCharacter.put(engine, max);
				if (microsecElapsedPerCharacter > max)
					maxMicrosecPerCharacter.put(engine, toIntExact(microsecElapsedPerCharacter));
				if (sentenceSize < SHORT_SENTENCE_THRESHOLD
				    && maxMillisecOfShortSentence.containsKey(engine)
				    && millisecElapsed > maxMillisecOfShortSentence.get(engine))
					maxMillisecOfShortSentence.put(engine, toIntExact(millisecElapsed));
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
