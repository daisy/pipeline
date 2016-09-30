package org.daisy.pipeline.nlp.lexing;

import java.util.List;
import java.util.Locale;

public interface LexService {

	public static final int MinSpecializedLexQuality = 10;

	public class LexerInitException extends Exception {
		public LexerInitException(String message, Throwable cause) {
			super(message, cause);
		}

		public LexerInitException(String message) {
			super(message);
		}

		public LexerInitException(Throwable cause) {
			super(cause);
		}

	}

	public class TextBoundaries {
		public int left; //inclusive
		public int right; //exclusive
	}

	public class Sentence {
		public List<TextBoundaries> words; //optional
		public TextBoundaries boundaries;
		//                    ^^^^^^^^^^^
		// this is necessary because the
		// punctuation marks and spaces are not
		// necessarily included in the content
	}

	/**
	 * LexerTokens are objects returned by the LexerRegistry to use LexServices
	 * in multi-thread contexts. Each LexServices will be bound to one single
	 * LexerToken for each run of lexing job so that the LexerTokens can use
	 * internal resources without any lock.
	 */
	public abstract static class LexerToken {
		/**
		 * This methods allows the LexerToken to share resources with other
		 * tokens of the same LexService but of different running jobs. Such
		 * resources must be thread-safe. The method needs not to be
		 * thread-safe.
		 * 
		 * @return false if the resources for @param lang have not been
		 *         initialized from the resources of @param other.
		 */
		public abstract void shareResourcesWith(LexerToken other, Locale lang);

		/**
		 * This method is called to perform a regular initialization of the
		 * resources bound to @param lang is some resources have not been
		 * already initialized with shareResourcesWith().
		 * 
		 * @param lang
		 * @throws LexerInitException
		 */
		public abstract void addLang(Locale lang) throws LexerInitException;

		/**
		 * Split a given text into sentences and words assuming the text is
		 * written in language @param lang.
		 * 
		 * @param lang is guaranteed to have been added with addLang() if
		 *            getOverallQuality() returns a negative number. Otherwise
		 *            it can be any language or null if the language is unknown.
		 * 
		 * @param errors is a list of non-critical parsing errors that occurred
		 *            during the process (e.g. unknown kind of words).
		 */
		public abstract List<Sentence> split(String text, Locale lang,
		        List<String> parsingErrors);

		/**
		 * @param lexService is the LexService which the token belongs to.
		 */
		public LexerToken(LexService lexService) {
			mLexService = lexService;
		}

		/**
		 * @return the LexService which the token belongs to.
		 */
		public LexService getLexService() {
			return mLexService;
		}

		private LexService mLexService;
	}

	public LexerToken newToken();

	/**
	 * @return a score for the given language. The higher the better. Zero or
	 *         negative if the language is not supported.
	 */
	public int getLexQuality(Locale lang);

	/**
	 * So far it is only used for logging.
	 */
	public String getName();

	/**
	 * The global initialization is called before the first creation of a
	 * LexerToken, the first time the pipeline is run or after a call to
	 * globalRelease().
	 * 
	 * @throws LexerInitException
	 */
	public void globalInit() throws LexerInitException;

	/**
	 * This method is called when there is no longer tokens bound to the
	 * LexService.
	 */
	public void globalRelease();

	/**
	 * @return an integer to compare Lexers when no particular language is
	 *         provided. The greater the best. Negative integer means that the
	 *         lexer cannot handle every language.
	 */
	public int getOverallQuality();
}
