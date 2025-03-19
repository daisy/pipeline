package org.daisy.pipeline.tts.synthesize.calabash.impl;

import java.util.Collection;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;

class TimedTTSExecutor {

	private static final int FIRST_CHARACTERS = 25000;
	private static final int SHORT_SENTENCE_THRESHOLD = 25;
	private Map<TTSEngine,Integer> totalCharacters = new HashMap<>(); // only count first 2500 characters
	private Map<TTSEngine,Integer> maxMicrosecPerCharacter = new HashMap<>();
	private Map<TTSEngine,Integer> maxMillisecOfShortSentence = new HashMap<>(); // shorter than 25 characters
	
	private final static Logger logger = LoggerFactory.getLogger(TimedTTSExecutor.class);

	/**
	 * The maximum number of milliseconds the TTS engine is allowed to spend on a single word. This
	 * takes into account {@see TTSEngine#expectedMillisecPerWord()}, the number of words processed
	 * so far, and the time the engine has spent on previous sentences. A large enough safety factor
	 * is taken into account for long words and other deviations.
	 */
	private long maximumMillisec(TTSEngine engine, int sentenceSize) {
		int wordCount = 1 + sentenceSize / 6; // ~6 characters/word
		Integer n;
		logger.info("****** maximumMillisec sentenceSize: {}", sentenceSize);
		synchronized(totalCharacters) {
			n = totalCharacters.get(engine);
			logger.info("****** maximumMillisec totalCharacters.get(engine): {}", n);
			Long safeMillisec = null;
			if (n == null || n < FIRST_CHARACTERS)
				// initially base timeout on engine.expectedMillisecPerWord()
				// adding initial offset because the processing time is not always directly
				// proportional to the length of the sentence
				safeMillisec = 5000L + 10 * engine.expectedMillisecPerWord() * wordCount;
			logger.info("****** maximumMillisec safeMillisec: {}", safeMillisec);
			if (n == null)
				return safeMillisec;
			else {
				// in the long run base timeout on maxMicrosecPerCharacter and maxMillisecOfShortSentence
				Integer estimatedMicrosec = maxMicrosecPerCharacter.get(engine) * sentenceSize;
				Integer minMillisec = maxMillisecOfShortSentence.get(engine);
				logger.info("****** maximumMillisec estimatedMicrosec: {}", estimatedMicrosec);
				logger.info("****** maximumMillisec minMillisec: {}", minMillisec);
				if (minMillisec != null) {
					if (estimatedMicrosec < minMillisec * 1000)
						estimatedMicrosec = minMillisec * 1000;
				} else if (sentenceSize < SHORT_SENTENCE_THRESHOLD)
					estimatedMicrosec = maxMicrosecPerCharacter.get(engine) * SHORT_SENTENCE_THRESHOLD;

				logger.info("****** maximumMillisec estimatedMicrosec: {}", estimatedMicrosec);
				long retVal = 3 * (Math.floorDiv(estimatedMicrosec - 1, 1000) + 1);
				if (n < FIRST_CHARACTERS) {
					logger.info("****** maximumMillisec interpolate: {}", n);
					// interpolate
					return Math.floorDiv(safeMillisec * (FIRST_CHARACTERS - n) + retVal * n - 1, FIRST_CHARACTERS) + 1;
				} else {
					return retVal;
				}
			}
		}
	}

	public Collection<AudioBuffer> synthesizeWithTimeout(
		TTSTimeout timeout, TTSTimeout.ThreadFreeInterrupter interrupter, TTSLog.Entry log,
		String sentence, XdmNode xmlSentence, int sentenceSize, TTSEngine engine, Voice voice,
		TTSResource threadResources, List<Mark> marks, List<String> expectedMarks,
		AudioBufferAllocator bufferAllocator, boolean retry
	) throws SynthesisException, MemoryException, TimeoutException {
		String id = xmlSentence.getAttributeValue(new QName("id"));
		long startTime = System.currentTimeMillis();
		long timeoutMillis = maximumMillisec(engine, sentenceSize);
		int timeoutSec = Math.floorDiv(((int)timeoutMillis) - 1, 1000) + 1; // ensure seconds are rounded up
		if (timeoutSec > 0) {
			logger.info("Calculated time-out for {} synthesis: {} ms ({} secs)", id, timeoutMillis, timeoutSec);
		} else {
			logger.warn("Calculated time-out for {} synthesis was invalid! Corrected to 1 sec from {} ms ({} secs)", id, timeoutMillis, timeoutSec);
			timeoutSec = 1;
		}
		if (log != null)
			log.setTimeout(timeoutSec);
		try {
			logger.info("Synthesizing {} via {} with {} seconds timeout", id, engine.getClass().getName(), timeoutSec);
			timeout.enableForCurrentThread(interrupter, timeoutSec);
			Collection<AudioBuffer> result = engine.synthesize(
				sentence, xmlSentence, voice, threadResources, marks, expectedMarks, bufferAllocator, retry);
			Long millisecElapsed = System.currentTimeMillis() - startTime;
			if (log != null)
				log.setTimeElapsed((float)millisecElapsed / 1000);
			synchronized(totalCharacters) {
				int max = maxMicrosecPerCharacter.getOrDefault(engine, 1000); // don't go below 1 ms
				if (sentenceSize < SHORT_SENTENCE_THRESHOLD) {
						if (millisecElapsed > maxMillisecOfShortSentence.getOrDefault(engine, max * SHORT_SENTENCE_THRESHOLD)) {
							maxMillisecOfShortSentence.put(engine, millisecElapsed.intValue());
						}
				}
				Long microsecElapsedPerCharacter = millisecElapsed * 1000 / ((sentenceSize > 0)? sentenceSize:1);
				if (microsecElapsedPerCharacter > max) {
					maxMicrosecPerCharacter.put(engine, microsecElapsedPerCharacter.intValue());
				} else if (!maxMicrosecPerCharacter.containsKey(engine)) {
					maxMicrosecPerCharacter.put(engine, max);
				}
				Integer n = totalCharacters.getOrDefault(engine, 0);
				if (n < FIRST_CHARACTERS)
					totalCharacters.put(engine, n + sentenceSize);
			}
			logger.info("***** synthesizeWithTimeout return");
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
			System.err.println("Timeout happenned after " + seconds + "s");
		}

		/**
		 * @return The timeout in seconds.
		 */
		public int getSeconds() {
			return seconds;
		}
	}
}
