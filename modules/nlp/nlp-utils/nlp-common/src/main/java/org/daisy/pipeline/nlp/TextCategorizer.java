package org.daisy.pipeline.nlp;

import java.io.IOException;

/**
 * Categorize input strings. Two modes are possible:
 * 
 * - FULL_MATCH: try to categorize the whole input
 * 
 * - PREFIX_MATCH: try to categorize the longest prefix match
 */
public abstract class TextCategorizer {
	protected MatchMode mMatchMode = MatchMode.FULL_MATCH;

	public enum Category {
		COMMON,
		UNKNOWN,
		PROPER_NOUN,
		PROPER_NOUN_PLACE,
		PROPER_NOUN_NAME,
		ABBREVIATION,
		ACRONYM,
		PUNCTUATION,
		EMAIL_ADDR,
		WEB_LINK,
		QUANTITY,
		ORDINAL,
		TIME,
		QUOTE,
		DATE,
		SPACE,
		CURRENCY,
		DIMENSIONS,
		MEASURE,
		PHONE_NUMBER,
		RANGE,
		IDENTIFIER,
		NUMBERING_ITEM
	};

	public static boolean isProperNoun(Category c) {
		switch (c) {
		case PROPER_NOUN:
		case PROPER_NOUN_PLACE:
		case PROPER_NOUN_NAME:
			return true;
		default:
			return false;
		}
	}

	public static boolean isSpeakable(Category c) {
		switch (c) {
		case SPACE:
		case PUNCTUATION:
		case QUOTE:
			return false;
		default:
			return true;
		}
	}

	public static class CategorizedWord {
		public Category category;
		public String word;
	}

	public enum MatchMode {
		FULL_MATCH,
		PREFIX_MATCH
	}

	public void init(MatchMode mode) throws IOException {
		mMatchMode = mode;
	}

	/**
	 * Called after init()
	 */
	public abstract void compile();

	public abstract void resetContext();

	/**
	 * Can return false before returning true, but not the other way around.
	 */
	public abstract boolean threadsafe();

	/**
	 * Both the fullcase and lower case versions are provided to prevent us from
	 * performing the conversion everytime.
	 * 
	 * @param fullcase fullcase version of the string to match
	 * @param lowercase lowercase version of the string to match
	 * @return null if the word cannot be categorized, otherwise the
	 *         CategorizedWord will the contain the longest match (i.e. a
	 *         substring of @param fullcase) depending on the match mode.
	 */
	public abstract CategorizedWord categorize(String fullcase, String lowercase);
}
