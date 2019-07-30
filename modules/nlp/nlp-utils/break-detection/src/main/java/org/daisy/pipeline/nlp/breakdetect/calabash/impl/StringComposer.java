package org.daisy.pipeline.nlp.breakdetect.calabash.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.daisy.pipeline.nlp.lexing.LexService.Sentence;
import org.daisy.pipeline.nlp.lexing.LexService.TextBoundaries;

/**
 * This class transforms references to a single string (i.e. two indexes) into a
 * list of references to an array of strings (i.e. a list of four indexes). It
 * is used for splitting XML content (array of strings) with the LexService
 * interface (single string inputs).
 */
public class StringComposer {

	static public class TextPointer {
		// inclusive index of the segment in which the first character is
		public int firstSegment;
		// inclusive index of the first character
		public int firstIndex;
		// inclusive index of the segment in which the last character is
		public int lastSegment;
		// EXLUSIVE index of the last character
		public int lastIndex;

		public boolean properNoun;
	}

	static public class SentencePointer {
		public TextPointer boundaries;
		public List<TextPointer> content; //optional
	}

	private List<SentencePointer> mSentencePointers = new ArrayList<SentencePointer>();
	private List<TextPointer> mWordPointers = new ArrayList<TextPointer>();
	private int[] mCoordinates = new int[2];
	private Matcher mSpaceMatcher = Pattern.compile("[\\p{Z}\\s]+").matcher("");
	private Matcher mSpaceMatcherEnd = Pattern.compile("[\\p{Z}\\s]+$").matcher("");

	public String concat(Collection<String> segments) {
		StringBuilder sb = new StringBuilder();
		for (String segment : segments) {
			if (segment != null)
				sb.append(segment);
		}
		return sb.toString();
	}

	/**
	 * The result cannot be kept after another call as it might be recycled.
	 * 
	 * The function trims the sentences and the words when making the pointers.
	 * No pointers is made for empty sentences and empty words.
	 * 
	 * isLexMark[i] means that the corresponding segment at index "i" can be
	 * ignored, since it is a lexing mark, rather than a true part of the
	 * original text.
	 */
	public List<SentencePointer> makePointers(List<Sentence> sentences, List<String> segments,
	        boolean[] isLexMark) {

		//pre-allocate some pointers
		int maxContentSize = 0;
		for (Sentence s : sentences)
			if (s.words != null)
				maxContentSize += s.words.size();
		for (int size = mWordPointers.size(); size < maxContentSize; ++size)
			mWordPointers.add(new TextPointer());

		//build a reference free of any lexer-helper
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < segments.size(); ++i) {
			if (isLexMark[i])
				for (int k = segments.get(i).length(); k > 0; --k)
					sb.append(" ");
			else if (segments.get(i) != null)
				sb.append(segments.get(i));
		}
		String ref = sb.toString();

		//make the pointers
		int contentIndex = 0;
		int nrOfSents = 0;
		int left, right;
		int[] coord;
		for (int i = 0; i < sentences.size(); ++i) {
			if (mSentencePointers.size() == nrOfSents) {
				SentencePointer sp = new SentencePointer();
				sp.boundaries = new TextPointer();
				mSentencePointers.add(sp);
			}
			Sentence sentence = sentences.get(i);
			SentencePointer sp = mSentencePointers.get(nrOfSents);

			left = trimLeft(sentence.boundaries.left, ref);
			right = trimRight(sentence.boundaries.right, ref);
			if (left >= right) {
				//empty sentence
				continue;
			}

			coord = findLeft(segments, left);
			sp.boundaries.firstSegment = coord[0];
			sp.boundaries.firstIndex = coord[1];

			coord = findRight(segments, right);
			sp.boundaries.lastSegment = coord[0];
			sp.boundaries.lastIndex = coord[1];
			++nrOfSents;

			if (sentence.words == null || sentence.words.size() == 0) {
				sp.content = null;
			} else {
				int contentLeft = contentIndex;
				for (int j = 0; j < sentence.words.size(); ++j) {
					TextBoundaries word = sentence.words.get(j);
					TextPointer tp = mWordPointers.get(contentIndex);

					left = trimLeft(word.left, ref);
					right = trimRight(word.right, ref);

					if (left >= right) {
						continue; //empty word
					}

					coord = findLeft(segments, left);
					tp.firstSegment = coord[0];
					tp.firstIndex = coord[1];

					coord = findRight(segments, right);
					tp.lastSegment = coord[0];
					tp.lastIndex = coord[1];

					++contentIndex;
				}
				if (contentLeft != contentIndex)
					sp.content = mWordPointers.subList(contentLeft, contentIndex);
				else
					sp.content = null;
			}
		}

		return mSentencePointers.subList(0, nrOfSents);
	}

	private int trimLeft(int left, String ref) {
		mSpaceMatcher.reset(ref.substring(left));
		if (mSpaceMatcher.lookingAt()) {
			left += mSpaceMatcher.end();
		}
		return left;
	}

	private int trimRight(int right, String ref) {
		mSpaceMatcherEnd.reset(ref.substring(0, right));
		if (mSpaceMatcherEnd.find()) {
			right = mSpaceMatcherEnd.start();
		}
		return right;
	}

	private int[] findLeft(List<String> segments, int index) {
		int res[] = findInArray(segments, index);
		if (res[1] == segments.get(res[0]).length()) {
			res[0]++;
			res[1] = 0;
		}
		return res;
	}

	private int[] findRight(List<String> segments, int index) {
		int[] res = findInArray(segments, index - 1);
		res[1]++;
		if (res[1] == 0) {
			res[0]--;
			res[1] = segments.get(res[0]).length();
		}
		return res;
	}

	private int[] findInArray(List<String> segments, int index) {
		mCoordinates[0] = 0;
		while ((segments.get(mCoordinates[0]) == null)
		        || (index >= segments.get(mCoordinates[0]).length())) {
			if (segments.get(mCoordinates[0]) != null)
				index -= segments.get(mCoordinates[0]).length();
			++mCoordinates[0];
		}
		mCoordinates[1] = index;
		return mCoordinates;
	}
}
