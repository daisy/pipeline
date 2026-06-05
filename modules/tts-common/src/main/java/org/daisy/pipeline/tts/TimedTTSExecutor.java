package org.daisy.pipeline.tts;

import static java.lang.Math.addExact;
import static java.lang.Math.multiplyExact;
import static java.lang.Math.toIntExact;
import java.util.HashMap;
import java.util.Map;

import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;

import net.sf.saxon.s9api.XdmNode;

public class TimedTTSExecutor {

	private static final int FIRST_CHARACTERS = 2500;
	private static final int SHORT_SENTENCE_THRESHOLD = 25;
	private Map<TTSEngine,Integer> totalCharacters = new HashMap<>(); // only count first 2500 characters
	private Map<TTSEngine,Integer> maxMillisecPerCharacter = new HashMap<>();
	private Map<TTSEngine,Integer> maxMillisecOfShortSentence = new HashMap<>(); // shorter than 25 characters

	/**
	 * The maximum number of milliseconds the TTS engine is allowed to spend on a sentence. This
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
				// in the long run base timeout on maxMillisecPerCharacter and maxMillisecOfShortSentence
				Integer estimatedMillisec = multiplyExact(maxMillisecPerCharacter.get(engine), sentenceSize);
				Integer minMillisec = maxMillisecOfShortSentence.get(engine);
				if (minMillisec != null) {
					if (estimatedMillisec < minMillisec)
						estimatedMillisec = minMillisec;
				} else if (sentenceSize < SHORT_SENTENCE_THRESHOLD)
					estimatedMillisec = multiplyExact(maxMillisecPerCharacter.get(engine), SHORT_SENTENCE_THRESHOLD);
				if (n < FIRST_CHARACTERS)
					// interpolate
					return toIntExact(
						addExact(
							multiplyExact((long)safeMillisec, FIRST_CHARACTERS - n) / FIRST_CHARACTERS,
							multiplyExact((long)estimatedMillisec, 3 * n) / FIRST_CHARACTERS));
				else
					return multiplyExact(3, estimatedMillisec);
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
		// add 1 so it can never be 0 seconds
		timeoutSec += 1;
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
				Long millisecElapsedPerCharacter = sentenceSize == 0
					? 0
					: millisecElapsed / sentenceSize;
				int max = 1; // don't go below 1 ms
				if (maxMillisecPerCharacter.containsKey(engine))
					max = maxMillisecPerCharacter.get(engine);
				else
					maxMillisecPerCharacter.put(engine, max);
				if (millisecElapsedPerCharacter > max)
					maxMillisecPerCharacter.put(engine, toIntExact(millisecElapsedPerCharacter));
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

	public static class TimeoutException extends RuntimeException {

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
